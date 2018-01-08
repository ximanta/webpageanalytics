
# Web Page Analytics

**A cloud native distributed micro service app for Web page analysis**

## Functional services

Web Page Analytics was decomposed into several core microservices.
All of them are independently deployable applications, organized around certain business domains.


#### Meta Data Extractor Service

Method	| Path	| Description	| User authenticated	| Available from UI
------------- | ------------------------- | ------------- |:-------------:|:----------------:|
POST	| /api/v1/metadataextractor/url/	| Submits a URL for analysis	|   | ×

## Infrastructure services
There's a bunch of common patterns in distributed systems, which could help us to make described core services work. [Spring cloud](http://projects.spring.io/spring-cloud/) provides powerful tools that enhance Spring Boot applications behaviour to implement those patterns. I'll cover them briefly.
Currently, Config server and API gateway has been added.



### Config service
[Spring Cloud Config](http://cloud.spring.io/spring-cloud-config/spring-cloud-config.html) is horizontally scalable centralized configuration service for distributed systems. It uses a pluggable repository layer that currently supports local storage, Git, and Subversion. 

In this project, I use `native profile`, which simply loads config files from the local classpath. You can see `shared` directory in [Config service resources](https://github.com/sqshq/PiggyMetrics/tree/master/config/src/main/resources). Now, when Notification-service requests it's configuration, Config service responses with `shared/notification-service.yml` and `shared/application.yml` (which is shared between all client applications).

##### Client side usage
Just build Spring Boot application with `spring-cloud-starter-config` dependency, autoconfiguration will do the rest.

Now you don't need any embedded properties in your application. Just provide `bootstrap.yml` with application name and Config service url:
```yml
spring:
  application:
    name: notification-service
  cloud:
    config:
      uri: http://config:8888
      fail-fast: true
```

##### With Spring Cloud Config, you can change app configuration dynamically. 
For example, [EmailService bean](https://github.com/sqshq/PiggyMetrics/blob/master/notification-service/src/main/java/com/piggymetrics/notification/service/EmailServiceImpl.java) was annotated with `@RefreshScope`. That means, you can change e-mail text and subject without rebuild and restart Notification service application.

First, change required properties in Config server. Then, perform refresh request to Notification service:
`curl -H "Authorization: Bearer #token#" -XPOST http://127.0.0.1:8000/notifications/refresh`

Also, you could use Repository [webhooks to automate this process](http://cloud.spring.io/spring-cloud-config/spring-cloud-config.html#_push_notifications_and_spring_cloud_bus)

##### Notes
- There are some limitations for dynamic refresh though. `@RefreshScope` doesn't work with `@Configuration` classes and doesn't affect `@Scheduled` methods
- `fail-fast` property means that Spring Boot application will fail startup immediately, if it cannot connect to the Config Service.
- There are significant [security notes](https://github.com/sqshq/PiggyMetrics#security) below

### API Gateway
As you can see, there are three core services, which expose external API to client. In a real-world systems, this number can grow very quickly as well as whole system complexity. Actually, hundreds of services might be involved in rendering of one complex webpage.

In theory, a client could make requests to each of the microservices directly. But obviously, there are challenges and limitations with this option, like necessity to know all endpoints addresses, perform http request for each peace of information separately, merge the result on a client side. Another problem is non web-friendly protocols which might be used on the backend.

Usually a much better approach is to use API Gateway. It is a single entry point into the system, used to handle requests by routing them to the appropriate backend service or by invoking multiple backend services and [aggregating the results](http://techblog.netflix.com/2013/01/optimizing-netflix-api.html). Also, it can be used for authentication, insights, stress and canary testing, service migration, static response handling, active traffic management.

Netflix opensourced [such an edge service](http://techblog.netflix.com/2013/06/announcing-zuul-edge-service-in-cloud.html), and now with Spring Cloud we can enable it with one `@EnableZuulProxy` annotation. In this project, I use Zuul to store static content (ui application) and to route requests to appropriate microservices. Here's a simple prefix-based routing configuration for Notification service:

```yml
zuul:
  routes:
    metadataextractor:
        path: /metadataextractor/**
        serviceId: notification-service
        stripPrefix: false

```

That means all requests starting with `/notifications` will be routed to Notification service. There is no hardcoded address, as you can see. Zuul uses [Service discovery](https://github.com/sqshq/PiggyMetrics/blob/master/README.md#service-discovery) mechanism to locate Notification service instances and also [Circuit Breaker and Load Balancer](https://github.com/sqshq/PiggyMetrics/blob/master/README.md#http-client-load-balancer-and-circuit-breaker), described below.


## How to run all the things?

-  Import the parent pom.xml to your IDE
-  Run WebPageMetadataExtractorApp.java present in metadataextractor module as a Spring Boot Application
- Use Postman to make a POST request to http://localhost:8080/api/v1/metadataextractor/url with the following request body
-  {
    "url" : "http://google.com"
   }


## ToDO
- Dockerize all services
- Add ReactJS frontend
- Configure Config server
- Configure Gateway
- Add Registry server