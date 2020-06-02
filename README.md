# MDPages Search

A project for DB classes at AGH UST, Poland.

Developed by:
- Hubert Balcerzak, [@HubertBalcerzak](https://github.com/HubertBalcerzak)
- Krzysztof Widenka, [@krzwid](https://github.com/krzwid)

## Topic of the project
Main topic of this project was to add full text search module basing on ElasticSearch to an existing application, MDPages, which offers features like editing and browsing the articles written in markdown language. It is written in kotlin language with Spring Framework.

## About MDPages
MDPages is a wiki engine designed to work with articles written in Markdown. Users can browse articles, create or edit existing ones and organize related articles into groups. On top of that this project provides extensive security configuration, with permissions, user management and many more administrative tools. It is meant for small communities, game wikis, documentations, etc. Currently, some features are not available, as the MDPages is undergoing a full rewrite. MDPages consits of two separate modules, backend named mdpages-core, which provides REST API, and frontend application, which was not used for this search engine demonstration. REST API documentation in it's current state is available [here](https://mdpages.snet.ovh/apidocs/).

## About ElasticSearch
Elasticsearch is a distributed, open source search and analytics engine for all types of data, including textual, numerical, geospatial, structured, and unstructured. Elasticsearch is built on Apache Lucene and was first released in 2010 by Elasticsearch N.V. (now known as Elastic). Known for its simple REST APIs, distributed nature, speed, and scalability, Elasticsearch is the central component of the Elastic Stack, a set of open source tools for data ingestion, enrichment, storage, analysis, and visualization. Commonly referred to as the ELK Stack (after Elasticsearch, Logstash, and Kibana), the Elastic Stack now includes a rich collection of lightweight shipping agents known as Beats for sending data to Elasticsearch.

## Setup

### Prerequisites
* JDK 1.8 or higher
* Empty MySQL or other JDBC-compatible database
* ElasticSearch node

1. Compile application and generate .jar file. It can be done via `./gradlew bootjar` command.
2. Configure datasources and other appliation properties. This can be done two ways:
* Via envionment variables

Following env variables must be set before running the application:

DB_HOST - address of the mysql database
DB_USER - username for the mysql database
DB_PASS - password for the mysql database
DB_NAME - mysql database name

JWT_SECRET - secret used in JWT token signing
ELASTIC_HOST - IP address of the ElasticSearch node. Set to 127.0.0.1 by default
ELASTIC_PORT - port of the ElasticSearch node. Set to 9200 by default

* Via custom application.properties file 

Custom application.properties file can be placed next to the jar file. Template for such properties file can be found [here](https://github.com/HubertBalcerzak/mdpages-core/blob/master/src/main/resources/application.properties)


3. Start MDPages application with command `java -jar jarName.jar`
4. By default database and ElasticSearch index are empty. To make creating demonstration data easier, it is possible to upload a set of documents with `upload_test_documents.py` script.


## Search engine
Search engine accepts four types of queries:

```title:[Your Title]``` - searching only by title

![](https://pad.snet.ovh/uploads/upload_8c53bb3e74e4465e222b0a92bc2c4401.png)

```date:[< or > or nothing][DD/MM/YYYY]``` - searching only by date ("<" / ">" will search for articles modified before/after given date, otherwise engine will search for articles modified on given date)

![](https://pad.snet.ovh/uploads/upload_51fc740cf6a8dc2dcac6a4c26253cbea.png)

```[Any Phrase]```  - searching by titles and content of the articles

![](https://pad.snet.ovh/uploads/upload_b11738a6645c2b2f44cfdb926303bd97.png)

```date:[< or > or nothing][DD/MM/YYYY] [Any Phrase] ``` - filtering by date and searching by titles and content of the articles
![](https://pad.snet.ovh/uploads/upload_4c1bb38fd1d481712a79a19f8922ee7f.png)


ElasticSearch gives each search result a score, assigned by number of occurencies and other factors, depending on the query. Search results will be ordered by this score. Score assigned based on the title is 5 times more important than score from article content.


Operators between each word (can be combined):
-    | or nothing (Default) - OR operator. For example, a query `capital of Hungary` or `capital | of | Hungary` is interpreted as `capital`OR`of`OR`Hungary`.
- \+ - AND operator. For example, a query string of `capital + of + Hungary` is interpreted as `capital` AND `of` AND `Hungary`
- `"[Any Phrase]"` - search engine will search for exact phrase 
- \-  - NOT operator
- () - Precedence operators. It is possible to arrange other operators and specify their order using parenthesis
## Implementation

### Important classes

Communication with ElasticSearch node is handled by `Java High Level REST Client` library, which is configured [here](https://github.com/HubertBalcerzak/mdpages-core/blob/master/src/main/kotlin/pl/starchasers/mdpages/configutation/ElasticSearchConfiguration.kt).

REST API endpoint is exposed by the [SearchController](https://github.com/HubertBalcerzak/mdpages-core/blob/master/src/main/kotlin/pl/starchasers/mdpages/content/SearchController.kt) class. It accepts query string, which is what user wrote in the search input, and pagination information. Pagination was implemented to minimize data transfer between backend server and client browser and between backend server and ElasticSearch node.

ElasticSearch queries are composed and executed by the [SearchService](https://github.com/HubertBalcerzak/mdpages-core/blob/master/src/main/kotlin/pl/starchasers/mdpages/content/SearchService.kt) class. It is responsible for indexing new documents, deleting documents from ElasticSearch index and handling all suported query types by translating then into query objects understood by ElasticSearch. Results received from search query are composed with richer data from MySQL database before being sent back to client.


### Search endpoint specification

GET http://localhost:8090/api/search

Query parameters:

| Parameter name | Type |Optional| Description |
| -------- | -------- |-| -------- |
| query     | String     |True| Query string, what user wrote in the search input. If empty, will return all stored documents.     |
| page|Integer|True| Page number for pagination. Defaults to 0.|
|size|Integer|True|Page size for pagination. Endpoint will return at most this many documents at once. Defaults to 10.|


## Usage of ElasticSearch
2857 companies reportedly use Elasticsearch in their tech stacks, including Uber, Slack, and Instacart.

![](https://pad.snet.ovh/uploads/upload_9b57fe2acab1dbf8617b983339a6d4ad.png)
