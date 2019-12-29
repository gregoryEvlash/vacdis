# vacdis

Test assignment

Written using Akka actors, AkkaHttp, Cats and Circe for json.

I do not add example of request and response, cause you asked not to expose it, for future candidates cant google it.
I used params name structure as it was in assignment example

###In order to run project via sources:

clone it

sbt run ${FILE_PATH_OF_SHOWS.CSV}

application starts localhost:8085 

environment variables HOST and PORT allows you override host and port

Also added assembly plugin that allows you to assembly project into jar file

sbt assembly

run it with java -jar target/scala-2.12/inventory-assembly-0.0.1.jar ${FILE_PATH_OF_SHOWS.CSV}

##CLI
On application start you are able to path arguments the same as in assignment description. Pass it like

${PARAM_NAME} ${PARAM_VALUE}

##Routes

###Get inventory overview
request param in kebab case

POST host:port/inventory/overview

{ "performance-date": "2019-12-31" }

response in snake case, structure is the same as was in assignment example 

###Buy a ticket
POST host:port/inventory/book
{
  "title": "Test",
  "date": "2019-12-31",
  "amount": 3
}

response
{
  "show" : {
    "title": "Test",
     "date": "2019-12-31"
  },
  "amount": 3
}

###Errors
There possible several errors responses if for example you want to book more tickets than available for today or left.
Or you try to book show that has already passed 

##About architecture:
As we know the ideal architecture doesnt exists. But i tried to keep balance between current business needs and solution that flexible with ability easy extends if this business requirements changed.

I choose approach to pre evaluate all necessary information about show and create a time table on the moment applying file.
It allows us make less computation on overview request, also if some conditions had changed, eg capacity, daily availability or show is declined for that day (public holiday eg.), we can easily remove it from time table.

Conditions of date serving, capacity, daily avail i put to the conf.
Price values also added to configuration.

###Db storage
I created a db mock, so service responsible for db storage info is just saving it in self memory collection 

###Services
Main service is Inventory service, its responsible for validation, aggregation info and saving general data.

Performance service is an actor, cause its suitable for stateful service. 
There easy manage how much tickets sold today, easy to rest value and provide availability info. Besides it can be scalable for diff DB or region of sale eg. At midnight service reset sold ticket into 0.

##Validation
On http request, it validates date format and amount.