package com.mapr.recommendation.engine

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import com.mapr.db.spark.sql._
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.mllib.recommendation.{ALS, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object RecommendationEngine extends java.io.Serializable {

  val AlbumsRatingsTableName = "/apps/albums_ratings"
  val ArtistsRatingsTableName = "/apps/artists_ratings"
  val RecommendationsTableName = "/apps/recommendations"

  val AnonymousUserId = "anonymous"

  val MlTrainingDataWeight = 0.8
  val MlTestDataWeight = 0.2

  val MlAlsRank = 20
  val MlAlsIterations = 10

  val RecommendedDocumentsNum = 10

  def main(args: Array[String]) {

    val sparkSession = SparkSession.builder().appName("RecommendationEngine").master("local[*]").getOrCreate()

    // Get user albums recommendations
    val albumsRecommendationsRDD = computeRecommended(sparkSession, AlbumsRatingsTableName)

    // Get user artists recommendations
    val artistsRecommendationsRDD = computeRecommended(sparkSession, ArtistsRatingsTableName)

    val albumsDataFrame = sparkSession.createDataFrame(albumsRecommendationsRDD).toDF("_id", "recommended_albums")
    val artistsDataFrame = sparkSession.createDataFrame(artistsRecommendationsRDD).toDF("_id", "recommended_artists")

    val resultDataFrame = albumsDataFrame.join(artistsDataFrame, Seq("_id"))
    resultDataFrame.saveToMapRDB(RecommendationsTableName)

    sparkSession.stop()
  }

  def computeRecommended(spark: SparkSession, tableName: String): RDD[Recommendation] = {

    import spark.implicits._

    // Load ratings DataSet from MapR-DB JSON tables
    val ds = spark.loadFromMapRDB(tableName)
      .map(row => MaprRating(row.getAs[String]("_id"), row.getAs[String]("user_id"), row.getAs[String]("document_id"),
        row.getAs[Double]("rating")))

    // Add anonymous user rates
    val ratingsDs = addAnonymousUserRatings(spark, ds, AnonymousUserId)

    // Index string identifiers to find corresponding numeric identifiers(required by ML lib)
    val userIdIndxDf = indexStringIdentifier(ratingsDs.df, "user_id", "user_id_numeric")
    val docIdIndxDf = indexStringIdentifier(userIdIndxDf.df, "document_id", "document_id_numeric")
    val ratingsDataset = docIdIndxDf.as[MaprRatingEnhanced].cache()

    // Enhanced ratings dataset contains numeric identifiers as well as string identifier for users and documents
    val ratingsDatasetWithNumericIds = ratingsDataset.map(mapToRating)

    // Randomly split ratings RDD into training data RDD and test data RDD
    val splits = ratingsDatasetWithNumericIds.randomSplit(Array(MlTrainingDataWeight, MlTestDataWeight), 0L)
    val trainingRatingsRDD = splits(0).rdd.cache()
    val testRatingsRDD = splits(1).rdd.cache()

    // Build a ALS user product matrix model
    val model = new ALS().setRank(MlAlsRank).setIterations(MlAlsIterations).run(trainingRatingsRDD)

    // Map RDD of org.apache.spark.mllib.recommendation.Rating to the RDD of Recommendation
    val maprEnhancedRatingsArray = ratingsDataset.collect()
    val recommendationRDD = model.recommendProductsForUsers(RecommendedDocumentsNum)
      .map((userIdRatingTuple) => {

        val stringUserId = getCorrespondingStringUserIdentifier(userIdRatingTuple._1, maprEnhancedRatingsArray)
        val documentIdsArray = userIdRatingTuple._2
          .map(_.product)
          .map((numericId) => getCorrespondingStringDocumentIdentifier(numericId, maprEnhancedRatingsArray))
          .array

        Recommendation(stringUserId, documentIdsArray)

      })

    recommendationRDD
  }

  /**
    * Indexes string value in order to get corresponding unique numeric value.
    *
    * @param dataFrame original dataframe, whcih contains string column.
    * @param inputCol  name of column, which will be indexed.
    * @param outputCol name of resulting column, which will contain numeric value,
    * @return resulting dataframe with indexed string column.
    */
  def indexStringIdentifier(dataFrame: DataFrame, inputCol: String, outputCol: String): DataFrame = {

    val stringIndexer = new StringIndexer()
      .setInputCol(inputCol)
      .setOutputCol(outputCol)
    val model = stringIndexer.fit(dataFrame)

    model.transform(dataFrame)
  }

  def mapToRating(rating: MaprRatingEnhanced): Rating = {
    Rating(rating.user_id_numeric.toInt, rating.document_id_numeric.toInt, rating.rating)
  }

  /**
    * Adds anonymous user rates to the original dataset. Anonymous user represents non-existing user, for which
    * recommendations will be computed.
    *
    * @param spark  Spark session.
    * @param ds     original dataset.
    * @param userId anonymous user identifier.
    * @return resulting dataset, which contains anonymous user rates.
    */
  def addAnonymousUserRatings(spark: SparkSession, ds: Dataset[MaprRating], userId: String): Dataset[MaprRating] = {

    import spark.implicits._
    val documentIdRatingMap: Map[String, Array[MaprRating]] = ds.collect().groupBy(_.document_id)
    val anonymousUserRates = documentIdRatingMap.toStream.map((keyValue) => {
      val ratingSum = keyValue._2.map(_.rating).sum
      val ratingsNum = keyValue._2.length
      MaprRating(java.util.UUID.randomUUID().toString, userId, keyValue._1, ratingSum / ratingsNum)
    }).toList

    val anonymousUserRatesDataset = spark.createDataset[MaprRating](anonymousUserRates)

    ds.union(anonymousUserRatesDataset)
  }

  def getCorrespondingNumericUserIdentifier(stringId: String, array: Array[MaprRatingEnhanced]): Int = {
    array.find(rating => stringId.equals(rating.user_id)).get.user_id_numeric.toInt
  }

  def getCorrespondingStringUserIdentifier(numericId: Int, array: Array[MaprRatingEnhanced]): String = {
    array.find(rating => numericId.equals(rating.user_id_numeric.toInt)).get.user_id
  }

  def getCorrespondingStringDocumentIdentifier(numericId: Int, array: Array[MaprRatingEnhanced]): String = {
    array.find(rating => numericId.equals(rating.document_id_numeric.toInt)).get.document_id
  }

}

case class MaprRating(_id: String, user_id: String, document_id: String, rating: Double)

case class MaprRatingEnhanced(_id: String, user_id: String, user_id_numeric: Double,
                              document_id: String, document_id_numeric: Double, rating: Double)

case class Recommendation(id: String, recommended: Array[String])


@JsonIgnoreProperties(ignoreUnknown = true)
case class MaprRecommendation(@JsonProperty("_id") id: String,
                              @JsonProperty("recommended_albums") recommended_albums: Array[String],
                              @JsonProperty("recommended_artists") recommended_artists: Array[String])
