# kt-async-remote-calls

## Problem Statement
Parallelize multiple outbound API calls from one Spring-Boot based service to another service using Kotlin Coroutines.

## Constraints 

### On the service making the outbound call

1. Will be written with **SpringBoot** and **Kotlin**
2. Will use a **RestTemplate** (not the reactive WebClient)

### On the remote service recieving the outbound call
4. It **does not have a batched endpoint** and expects multiple calls
5. It is **slow**

## Sequence diagram

<!--
```
@startuml firstDiagram

@startuml
actor       "User"            as 1
entity      "Buyer Service"   as 2
entity      "Order Service"   as 3
1 -> 2 : Get sum of all order prices
2 -> 3 : GET /order/1/details
2 -> 3 : GET /order/2/details
2 -> 3 : GET /order/3/details
2 -> 3 : GET /order/4/details
2 -> 2 : Calculate total order price
2 -> 1 : Sum of all order prices
@enduml
		
@enduml
```
-->

![](https://www.planttext.com/api/plantuml/svg/VT2z2i8m58RXFLVnqLrAKYS7AOeuEbGlu90UeP0safm8lNlxjnIrsGxlCtoaZ491KkiKMV41yyiUaKEs7A08hRYJHlebdrBF0HM7TsCvcubParkelqYXD7P761nmPK5CBVmJv1pyf5zXi56P4HKZkjoHJodNUSr2ZVjTpMOjDkj-NSTnPt8sEHA6UA7LkXdf0LL_rfVSrSD_VW00)

## Tools 

- Kotlin Coroutines 
- Mockoon (https://mockoon.com/) to simulate remote server
- Locust (https://locust.io/) for load-testing and measuring response times

## Approach 

- Two controllers are written, one which parallelizes outbound calls using Kotlin coroutines, and the other one does not. 

- The response times were then measured using a simple load test 

## Observation 

We see a massive improvment in API response time with the use of kotlin coroutines for parallelizing outbound API calls. 




