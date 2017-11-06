# Working with MapR-DB

## MapR DB Shell utility

The mapr dbshell is a tool that allows you to create and perform basic manipulation of JSON tables and documents. 
You can run dbshell by executing `mapr dbshell` command on one of the nodes of MapR cluster.
As `mapr` user in a terminal enter the following commands to get familiar with the shell

```
$ mapr dbshell

maprdb mapr:> jsonoptions --pretty true --withtags false

maprdb mapr:> find /apps/users --limit 2
```

To get a list of supported dbshell commands, run `help` at the shell prompt:
```
maprdb mapr:> help
* ! - Allows execution of operating system (OS) commands
* // - Inline comment markers (start of line only)
* ; - Inline comment markers (start of line only)
* cat - Print the content of the specified file on the standard output
* cd - Change the current directory to the specified path.
* clear - Clears the console
* cls - Clears the console
* create - Create a json table at the given path.
* date - Displays the local date and time
* debug - Sets/shows the debug mode.
* delete - Delete a document from the table.
* desc - Describes the properties of a table.
* drop - Deletes a MapR-DB json table.
* exists - Returns true if the table exists.
* exit - Exits the shell
* find - Retrieves one or more documents from the table.
* findbyid - Retrieves a single document from the table.
* help - List all commands usage
* indexscan - Scans secondary indexes and returns the document ID and the values of the indexed fields.
* insert - Inserts or replaces a document into the table.
* jsonoptions - Sets/shows the Json output options.
* list - Lists all tables in a folder or matching the specified pattern.
* ls - Lists files and folders.
* mkdir - Create a directory at the specified path.
* pwd - Print the absolute path of the current working directory.
* quit - Exits the shell
* replace - Replace a document based on condition.
* script - Parses the specified resource file and executes its commands
* system properties - Shows the shell's properties
* tableoptions - Sets/shows the MapR-DB Table access options.
* update - Update field in a single document.
* version - Displays shell version
```

#### Create Document

To add documents to JSON tables, use the `insert` command. Specify the ID of the document in one of two ways:
* As the value of the `_id` field in the document
* As the value of the `--id` parameter of the insert command

##### Syntax
With `_id` field:
```
insert --table <table path> --value '{"_id": "<row-key", < table field >}'
```

With `--id` parameter:
```
insert --table <table path> --id <row-key> --value '{< table field >}'
```

##### Examples
With `_id` field:
```
maprdb mapr:> insert /apps/users --value '{"_id":"kblock", 
                              "first_name":"Ken", 
                              "last_name":"Block"}'
```

With `--id` parameter:
```
maprdb mapr:> insert /apps/users --id kblock --value '{"first_name":"Ken", 
                                                "last_name":"Block"}'
```
#### Update document
Update operations are performed with mutations. These mutation operations are specified with sub-commands that are used 
to update and add document fields, or merge sub-documents to existing documents.

##### Syntax
```
update <table path> <parameter> {"<sub-command>": <operation> }
```

##### Examples
Lets change user's first name from the previous example:
```
maprdb mapr:> update /apps/users --id kblock --m '{ "$set" : [ {"first_name" : "John" } ] }'
```

#### Delete Document
The dbshell `delete` command is used to delete a single JSON document. To delete a document, specify the path of the 
table where the document is located and the ID of the document. Deletes of documents are based based on row-key. If the 
condition evaluates to true, the row is deleted. If the operation fails or an exception is thrown, an error message 
is displayed at the terminal.

##### Syntax
```
delete <table path> --id <row-key>
```

##### Examples
Delete a document by ID:
```
maprdb mapr:> delete /apps/users --id kblock
```

## MapR Drill
Drill is a low-latency distributed query engine for large-scale datasets, including structured and 
semi-structured/nested data. Inspired by Google’s Dremel, Drill is designed to scale to several thousands of nodes and 
query petabytes of data at interactive speeds that BI/Analytics environments require.

Drill includes a distributed execution environment, purpose built for large-scale data processing. At the core of Drill 
is the "Drillbit" service which is responsible for accepting requests from the client, processing the queries, and 
returning results to the client.

In order to get familiar with Drill we will use Drill shell (SQLLine).

Open a terminal and run sqlline:
```
$ sqlline

sqlline> !connect jdbc:drill:zk=hostname:5181
```

Where `hostname` is a node where Zookeeper is running, then enter the `mapr` username and password when prompted.

You can now execute SQL queries:
```
sqlline> select _id, first_name, last_name from dfs.`/apps/users` where first_name = 'Ken';
sqlline> select name from dfs.`/apps/albums` where rating > 3 order by name limit 10;
```
