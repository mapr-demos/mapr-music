# How to manage arrays operations

This document explains how to manage arrays operations on JSON documents, that stored in MapR-DB. Here you can find 
examples of arrays operations, which are performed using MapR-DB shell tool and OJAI Driver.

## Sample document
Examples use the following sample JSON document:
```
{
  "_id": "id1",
  "nested_doc_field": {
       "array_field_a": [{"boolean":false}, {"decimal": 123.456}]
       },
  "array_field_b": ["MapR wins"]
}
```
Note: Each of the examples depends on modified document as result of executing the previous example. Thus, if you want 
to try the examples it is recommended to follow them step by step.

You can use the following command to insert such document usin MapR-DB shell:
```
insert /table_name --value '{ "_id": "id1", "nested_doc_field": { "array_field_a": [{"boolean":false}, {"decimal": 123.456}] }, "array_field_b": ["MapR wins"] }'
```

## Arrays operations examples

Update operations are performed with mutations. These mutation operations are specified with sub-commands that are used 
to update and add document fields, or merge sub-documents to existing documents.

1. Adding new element to the array

The `$append` sub-command allows to add one or more elements to the array. The append command is a read-modify-write type 
operation that is used to append a given value to an existing binary, string or array type field. 
If there is type mismatch in any intermediate field specified in fieldpath with that in the document, it fails with an 
error.

MapR-DB shell example:
```
update /table_name --id id1 --m '{ "$append":[{"nested_doc_field.array_field_a":{"some_field": "some_value"}},{"array_field_b":["First", "Second"]}] }'
```

The example above is equivalent to the following code, which uses OJAI Driver:
```
    ...
    
    // Create an OJAI connection to MapR cluster
    Connection connection = DriverManager.getConnection("ojai:mapr:");

    // Get an instance of OJAI DocumentStore
    final DocumentStore store = connection.getStore("/table_name");

    String documentId = "id1";
    
    // Create an instance of Map, which represents nested document. That document will be appended to the array
    Map<String, String> newArrayElement = new HashMapr<>();
    newArrayElement.put("some_field", "some_value");
    
    // Create a DocumentMutation to add new array elements. Note that to append elements to the array you have to 
    // specify values as lists, even if there is only one element to append
    DocumentMutation mutation = connection.newMutation()
        .append("nested_doc_field.array_field_a", Collections.singletonList(newArrayElement))
        .append("array_field_b", Arrays.asList("First", "Second"));
    
    // Update the Document
    store.update(documentId, mutation);

    // Close this instance of OJAI DocumentStore
    store.close();

    // Close the OJAI connection and release any resources held by the connection
    connection.close();
    
    ...
```

Result:
```
{
  "_id" : "id1",
  "array_field_b" : [ "MapR wins", "First", "Second" ],
  "nested_doc_field" : {
    "array_field_a" : [ {
      "boolean" : false
    }, {
      "decimal" : 123.456
    }, {
      "some_field" : "some_value"
    } ]
  }
}

```

2. Updating an array element

The set command allows to update separate array elements. `$set` is a read-modify-write operation where the type of 
the existing value validated at the specified FieldPath before applying the mutation. If the field does not exist in the 
corresponding document in the document store, it is created. If the field exists but is not of the same type as the type 
of new value, then the entire mutation fails. The list of field paths and values need to be provided as an array of 
comma-separated key-value pairs. If there is a single field path, array representation is not required.

MapR-DB shell example:
```
update /table_name --id id1 --m '{ "$set":[{"nested_doc_field.array_field_a[0].boolean":true},{"array_field_b[2]":"Updated"}] }'
```
Note, that element of array is referenced by it's index. 

The example above is equivalent to the following code, which uses OJAI Driver:
```
    ...
    
    // Create an OJAI connection to MapR cluster
    Connection connection = DriverManager.getConnection("ojai:mapr:");

    // Get an instance of OJAI DocumentStore
    final DocumentStore store = connection.getStore("/table_name");

    String documentId = "id1";
    
    // Create a DocumentMutation to update the first element of 'array_field_a' array and third element of 
    // 'array_field_b' array
    DocumentMutation mutation = connection.newMutation()
        .set("nested_doc_field.array_field_a[0].boolean", true)
        .set("array_field_b[2]", "Updated");
    
    // Update the Document
    store.update(documentId, mutation);

    // Close this instance of OJAI DocumentStore
    store.close();

    // Close the OJAI connection and release any resources held by the connection
    connection.close();
    
    ...
```

Result:
```
{
  "_id" : "id1",
  "array_field_b" : [ "MapR wins", "First", "Updated" ],
  "nested_doc_field" : {
    "array_field_a" : [ {
      "boolean" : true
    }, {
      "decimal" : 123.456
    }, {
      "some_field" : "some_value"
    } ]
  }
}

```

3. Remove an element from the array

The set command allows to update separate array elements. `$set` is a read-modify-write operation where the type of 
the existing value validated at the specified FieldPath before applying the mutation. If the field does not exist in the 
corresponding document in the document store, it is created. If the field exists but is not of the same type as the type 
of new value, then the entire mutation fails. The list of field paths and values need to be provided as an array of 
comma-separated key-value pairs. If there is a single field path, array representation is not required.

MapR-DB shell example:
```
update /table_name --id id1 --m '{ "$delete":["nested_doc_field.array_field_a[2]","array_field_b[2]"] }'
```
Note, that element of array is referenced by it's index. 

The example above is equivalent to the following code, which uses OJAI Driver:
```
    ...
    
    // Create an OJAI connection to MapR cluster
    Connection connection = DriverManager.getConnection("ojai:mapr:");

    // Get an instance of OJAI DocumentStore
    final DocumentStore store = connection.getStore("/table_name");

    String documentId = "id1";
    
    // Create a DocumentMutation to delte the third elements of 'array_field_a' and 'array_field_b' arrays
    DocumentMutation mutation = connection.newMutation()
        .delete("nested_doc_field.array_field_a[2]")
        .delete("array_field_b[2]");
    
    // Update the Document
    store.update(documentId, mutation);

    // Close this instance of OJAI DocumentStore
    store.close();

    // Close the OJAI connection and release any resources held by the connection
    connection.close();
    
    ...
```

Result:
```
{
  "_id" : "id1",
  "array_field_b" : [ "MapR wins", "First" ],
  "nested_doc_field" : {
    "array_field_a" : [ {
      "boolean" : true
    }, {
      "decimal" : 123.456
    } ]
  }
}

```
   

