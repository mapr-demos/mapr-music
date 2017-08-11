# MapR Music


Project Structure (proposal):
* doc : documentation such as how to, code snipplet
  * resources : images, and other resources used by the documentation, wiki, issues
* core-application
  * webapp : the modules/services used by the Web application (REST, front-end)
  * streaming : the modules/services to stream data, for example Change Data Capture programs
  * processing : the modules/services used to process data for example Spark & Machine Learning
* reporting-analytics : the Apache SQL scripts and reports sources use for analytics