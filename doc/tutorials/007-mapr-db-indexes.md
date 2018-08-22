# MapR-DB Indexes

Indexes, (or secondary indexes), available on MapR-DB JSON allow queries with condition and/or sort to be more efficient. Without index, queries must do a full table scan (read all the document of a table).

The home page, with the list of Albums provide some query options to the user:

* filter `albums` by language
* order `albums` by title (ascending/desdencing), release date (ascending/descending)
* find `albums` and `artists` by short name ( `slug_name` and `slug_postfix` attributes)

Let's create indexes on the `albums` and `artists` tables, connect to your MapR cluster and run the following commands:

```
maprcli table index add -path /apps/albums -index idx_language -indexedfields 'language' -includedfields 'name,slug_name,slug_postfix,barcode,format,country,catalog_numbers,released_date,cover_image_url,artists'
maprcli table index add -path /apps/albums -index idx_name_asc -indexedfields 'name' -includedfields 'slug_name,slug_postfix,barcode,format,country,catalog_numbers,released_date,cover_image_url,artists'
maprcli table index add -path /apps/albums -index idx_name_desc -indexedfields 'name:desc' -includedfields 'slug_name,slug_postfix,barcode,format,country,catalog_numbers,released_date,cover_image_url,artists'
maprcli table index add -path /apps/albums -index idx_released_date_asc -indexedfields 'released_date' -includedfields 'name,slug_name,slug_postfix,barcode,format,country,catalog_numbers,cover_image_url,artists'
maprcli table index add -path /apps/albums -index idx_released_date_desc -indexedfields 'released_date:desc' -includedfields 'name,slug_name,slug_postfix,barcode,format,country,catalog_numbers,cover_image_url,artists'
maprcli table index add -path /apps/albums -index slug_name -indexedfields 'slug_name' -includedfields 'name,slug_postfix,barcode,format,country,catalog_numbers,released_date,cover_image_url,artists'
maprcli table index add -path /apps/albums -index idx_slug -indexedfields 'slug_name,slug_postfix' -includedfields 'name,barcode,format,country,catalog_numbers,released_date,cover_image_url,artists'
maprcli table index add -path /apps/artists -index idx_slug -indexedfields 'slug_name,slug_postfix' -includedfields 'name,albums'
```

When creating an index, you have to:
* select the indexed field (`-indexedfields`) and the sort order of the index key
* select the other fields included in the index. This allow to execute [covering index queries](https://maprdocs.mapr.com/home/Drill/covering-noncovering-queries.html)

Look at the MapR Documentation chapter about [MapR-DB JSON Secondary Indexes](https://maprdocs.mapr.com/home/MapR-DB/Indexes/indexes-types.html)

## Using indexes

MapR-DB OJAI and Apache Drill use their internal query optimizer to use or not index depending of the size of the table and type of query.

Let's do a test, use Apache Drill web interface (http://<mapr-cluster>:8047) and execute the following query:

```sql
select `name` from dfs.`/apps/albums`
where `language` = 'fra'
limit 3
```

Once you have executed the query you can look at the execution plan.

1. Click on **Profiles** tab.
1. Click on *Physical Plan** tab.
1. Look in the plan and you shoud see that the `idx_language` index is used. You should see something like

```
...
[tableName=maprfs:///apps/albums, condition=(language = "fra"), indexName=idx_language], columns=[`language`, `name`]]]
...
```

You can run different queries with fields that are not indexed, or different projections and you will see that the index is not used.



---

Next : [Create a REST API](008-create-a-rest-api.md)