# Creating Recommendation Engine

This document explains how to create Recommendation Engine using Spark MLlib's Alternating Least Squares algorithm to 
make Albums/Artists recommendations.

### Collaborative Filtering with Spark

MapR Music Recommendation Engine is based on Collaborative filtering. Collaborative filtering algorithms recommend items 
based on preference information from many users. This approach is based on similarity; the basic idea is people who 
liked similar items in the past will like similar items in the future. In the example below, Ted likes albums A, B, and 
C. Carol also likes albums A, B and C. Bob likes albums A and B. To recommend an album to Bob, we calculate that users 
who liked B also liked C, so C is a possible recommendation for Bob.

Users Item Rating Matrix:

| User/Album    | Album A | Album B | Album C |
| ------------- | ------- | ------- | ------- |
| Ted           | 5.0     | 5.0     | 5.0     |
| Carol         | 5.0     | 5.0     | 5.0     |
| Bob           | 5.0     | 5.0     | ???     |


Spark MLlib implements a collaborative filtering algorithm called Alternating Least Squares (ALS).

### Training data

ALS requires rating dataset to build `MatrixFactorizationModel`. MapR Music App uses `/apps/albums_ratings` and 
`/apps/artists_ratings` JSON tables, which contain documents in the following format:
```
{
  "_id" : "04a459de-cdbe-49a8-8438-9cfbedadc769",
  "document_id" : "05771ef2-9116-4144-918c-4d8e2ade6cec",
  "rating" : 5,
  "user_id" : "abergstrom"
}
```

### Implementation

1. Loading data via MapR-DB OJAI Connector for Apache Spark
MapR-DB OJAI Connector for Apache Spark allows us to load rating data from MapR-DB JSON Tables into Spark Dataset:
```
val ds = spark.loadFromMapRDB(tableName)
      .map(row => MaprRating(row.getAs[String]("_id"), row.getAs[String]("user_id"), row.getAs[String]("document_id"),
        row.getAs[Double]("rating")))
```

2. Adding anonymous user ratings
We will add anonymous user ratings to the training data to allow get recommendations for unauthorized MapR Music App users:
```
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
```

Anonymous user rates each document according to it's aggregated rating, thus such user behaves as average user.

3. Mapping to `org.apache.spark.mllib.recommendation.Rating`
`org.apache.spark.mllib.recommendation.ALS` requires training data to be dataset of `org.apache.spark.mllib.recommendation.Rating`, 
which has following properties:
* `user : Int` - user identifier;
* `product : Int` - product identifier;
* `rating : Double` - rating;

First of all, we need to convert our string identifiers into integer ones. We will do it using `org.apache.spark.ml.feature.StringIndexer`:
```
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
```

After that we can easily map ratings datasets to `org.apache.spark.mllib.recommendation.Rating`.

4. Training the model
```
    val ratingsDatasetWithNumericIds = ratingsDataset.map(mapToRating)

    // Randomly split ratings RDD into training data RDD and test data RDD
    val splits = ratingsDatasetWithNumericIds.randomSplit(Array(MlTrainingDataWeight, MlTestDataWeight), 0L)
    val trainingRatingsRDD = splits(0).rdd.cache()
    val testRatingsRDD = splits(1).rdd.cache()

    // Build a ALS user product matrix model
    val model = new ALS().setRank(MlAlsRank).setIterations(MlAlsIterations).run(trainingRatingsRDD)
```

5. Get recommendations

MapR Music App uses `/apps/recommendations` JSON table to store users' recommendations in the following format:
```
{
  "_id" : "abergstrom",
  "recommended_albums" : [ "0147ae5f-775e-4d2d-90f3-d945459201fd", "02c147df-37f2-4fef-a88f-1f743aa06ef9", "05771ef2-9116-4144-918c-4d8e2ade6cec", "0257ff8e-0e61-446f-b102-7cfa0fa7f4d3", "03fa133e-c1b6-4ba9-a610-9a5c2e0a6fee", "03900035-38f1-4bd5-8dd0-8ecf2f1df3c7", "06c2ccf1-59a8-421e-89bc-342e2b0d746b", "00327e39-61f8-48b6-ab7d-0d374182ce1b", "037081d0-a0b7-4126-87a0-7f1668e73c88", "06ce6c38-da57-4f28-81af-ca5c1c40d233" ],
  "recommended_artists" : [ "6acf2c42-9000-4a80-90af-99491e3b97ed", "b7f3d518-f575-449f-a884-00668d5e609e", "25bf053c-609f-4523-8a7c-1fedc2ff74e6", "296c6ef1-3c31-4474-af5e-72f3b7c21324", "77122242-04d2-45ca-a4aa-520848ba3c17", "f7c46d72-b6d1-4134-a34a-1f054b1aceb9", "e2469dfc-545f-45fb-a605-6650cd694ec5", "c16e8b96-8def-483b-9e84-4dee8c1b266c", "a1428f68-29a2-40a6-99bc-ae54207f40a0", "4d9d3718-2261-448d-9239-68ac50ba16a2" ]
}
```

`MatrixFactorizationModel#recommendProductsForUsers` method allows us to get users recommendations as ``. 
Lets map it to our `Recommendation` case class:
```
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
```

6. Storing the recommendations
MapR-DB OJAI Connector for Apache Spark allows us to easily store recommendations data into MapR-DB JSON Tables:
```
    // Get user albums recommendations
    val albumsRecommendationsRDD = computeRecommended(sparkSession, AlbumsRatingsTableName)

    // Get user artists recommendations
    val artistsRecommendationsRDD = computeRecommended(sparkSession, ArtistsRatingsTableName)

    val albumsDataFrame = sparkSession.createDataFrame(albumsRecommendationsRDD).toDF("_id", "recommended_albums")
    val artistsDataFrame = sparkSession.createDataFrame(artistsRecommendationsRDD).toDF("_id", "recommended_artists")

    val resultDataFrame = albumsDataFrame.join(artistsDataFrame, Seq("_id"))
    resultDataFrame.saveToMapRDB(RecommendationsTableName)
```

### Running the Engine manually

[RecommendationEngine.scala](https://github.com/mapr-demos/mapr-music/blob/feature/Recommendation_engine/core-application/processing/recommendation-engine/src/main/scala/com/mapr/recommendation/engine/RecommendationEngine.scala)
represents separate Spark job, which can be run manually from Dev machine. You have to be sure that you have MapR Client 
properly [installed and configured](https://github.com/mapr-demos/mapr-music/blob/feature/Recommendation_engine/doc/how-to-deploy-to-wildfly.md#installing-and-configuring-mapr-client). 
Use the following commands to run the engine:
```
$ cd core-application/processing/recommendation-engine
$ mvn clean install scala:run
```

After job completion you can get user recommendations using UI, REST client or `curl`:
```
$ export USERNAME='aleannon'
$ export DEFAULT_USER_PASSWORD='music'
$ curl -u ${USERNAME}:${DEFAULT_USER_PASSWORD} -X GET http://localhost:8080/mapr-music-rest/api/1.0/albums/00327e39-61f8-48b6-ab7d-0d374182ce1b/recommended?limit=10 | python -m json.tool
```

### Recommendation Engine automated approaches
Recommendation Engine can be improved to run in automatic mode in several ways. Keep in mind, that using Alternating 
Least Squares (ALS) algorithm assumes the rebuilding of entire model. If you want Recommendation Engine to be updated 
while receiving data([Online Machine Learning](https://en.wikipedia.org/wiki/Online_machine_learning)) you should 
consider other approaches to create the Engine. Spark MLLib has limited streaming machine learning options. 
There's a [streaming linear regression](https://spark.apache.org/docs/latest/mllib-linear-methods.html#streaming-linear-regression) 
and a [streaming K-Means](https://spark.apache.org/docs/latest/mllib-clustering.html#streaming-clustering), which allow 
to do model updating in real time.

Thus, you have following options to run Recommendation Engine in automatic mode:
1. Retrain the Engine by schedule
2. Use CDC to trigger Engine retraining

You can create Changelog and add it to rating table. After that you are free to use Kafka Consumer and receive Change 
Data records to trigger Engine retraining. For more information about CDC refer 
[Change Data Capture](https://github.com/mapr-demos/mapr-music/blob/feature/Recommendation_engine/doc/change-data-capture.md) 
document.

3. Using Spark MLLib streaming machine learning options:
* [Streaming linear regression](https://spark.apache.org/docs/latest/mllib-linear-methods.html#streaming-linear-regression) 
* [Streaming K-Means](https://spark.apache.org/docs/latest/mllib-clustering.html#streaming-clustering)
