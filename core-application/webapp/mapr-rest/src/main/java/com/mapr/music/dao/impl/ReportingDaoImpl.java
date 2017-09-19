package com.mapr.music.dao.impl;

import com.mapr.music.dao.ReportingDao;
import com.mapr.music.model.Pair;

import javax.annotation.Resource;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Named("reportingDao")
public class ReportingDaoImpl implements ReportingDao {

  Logger logger = Logger.getLogger(ReportingDaoImpl.class.toString());

  @Resource(lookup = "java:/datasources/mapr-music-drill")
  DataSource ds;

  Connection connection;

  @Override
  /**
   * Return the most common area with artists.
   */
  public List<Pair> getTopAreaForArtists(int numberOfRows) {

    String sql = "SELECT `area` AS `area`, COUNT(1) AS `count` " +
            " FROM dfs.`/apps/artists`" +
            " GROUP BY `area` ORDER BY 2 DESC LIMIT " + numberOfRows;

    List<Pair> pairs = populatePaiFromSQL(sql);


    return pairs;
  }


  @Override
  public List<Pair> getTopLanguagesForAlbum(int numberOfRows) {
    String sql = "SELECT l.`name` as `language`, COUNT(1) as `count` " +
            " FROM dfs.`/apps/albums` AS a " +
            " LEFT JOIN dfs.`/apps/languages` AS l ON l.`_id` = a.`language` " +
            " GROUP BY l.`name` ORDER BY 2 DESC LIMIT " + numberOfRows;
    List<Pair> pairs = populatePaiFromSQL(sql);
    return pairs;
  }

  @Override
  public List<Pair> getNumberOfAlbumsPerYear(int numberOfRows) {
    String sql =  "SELECT EXTRACT(YEAR FROM released_date) AS `year`, COUNT(1) AS `count` "+
                  " FROM (SELECT TO_DATE(released_date) AS `released_date`, `name`, `_id` FROM dfs.`/apps/albums` "+
                  " WHERE released_date IS NOT NULL ORDER BY released_date DESC) "+
                  " GROUP BY EXTRACT(YEAR from released_date) LIMIT "+ numberOfRows;
    List<Pair> pairs = populatePaiFromSQL(sql);
    return pairs;
  }


  /**
   * get the connection from the datasouce.
   *
   * @return the JDBC Connection
   * @throws SQLException
   */
  private Connection getConnection() throws SQLException {
    if (connection == null) {
      connection = ds.getConnection();
    }
    return connection;
  }


  /**
   * Execute the SQL statement and return a list of K/V
   * @param sql
   * @return
   */
  private List<Pair> populatePaiFromSQL(String sql) {
    List<Pair> pairs = new ArrayList<Pair>();
    try {

      logger.info("Executing SQL :\n\t"+ sql);

      Statement st = getConnection().createStatement();
      ResultSet rs = st.executeQuery(sql);
      while (rs.next()) {
        String label = rs.getString(1);
        if (label == null || label.trim().isEmpty()) {
          label = "Unknown";
        }
        pairs.add(new Pair(label, rs.getString(2)));
      }
      rs.close();
      st.close();
      connection.close();

    } catch (Exception e) {
      e.printStackTrace();
      // TODO: Manage exception
    }
    return pairs;
  }

}
