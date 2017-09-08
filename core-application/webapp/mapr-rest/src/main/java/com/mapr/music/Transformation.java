package com.mapr.music;

import org.ojai.Document;
import org.ojai.DocumentStream;
import org.ojai.store.Connection;
import org.ojai.store.DocumentStore;
import org.ojai.store.DriverManager;

/**
 * Created by tgrall on 08/09/2017.
 */
public class Transformation {

  public static void main(String[] args) {


    System.out.println("==== Start Application ===");


    // Create an OJAI connection to MapR cluster
    final Connection connection = DriverManager.getConnection("ojai:mapr:");

    // Get an instance of OJAI DocumentStore
    final DocumentStore store = connection.getStore("/apps/albums");


    // fetch all OJAI Documents from this store
    final DocumentStream stream = store.find();
    int i = 0;
    for (final Document album : stream) {
      // Print the OJAI Document
      i++;
      System.out.println(  album.getId()   );

      // copy artist_list into artits

      // System.out.println(  );

      album.set("artists",  album.getList("artist_list"));
      album.set("tracks",  album.getList("track_list"));
      store.insertOrReplace( album );

    }

    store.flush();
    System.out.println(i);

    // Close this instance of OJAI DocumentStore
    store.close();

    // close the OJAI connection and release any resources held by the connection
    connection.close();

    System.out.println("==== End Application ===");


  }


}
