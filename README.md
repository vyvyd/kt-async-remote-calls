# kt-async-remote-calls

## Problem Statement
Parallelize multiple outbound API calls from one Spring-Boot based service to another service using Kotlin Coroutines.

## Test Setup and Sequence Diagram

We setup three customers

**Customer 1** having 1 order (meaning 1 outbound call will be made)   
**Customer 2** having 19 orders  (meaning 19 outbound calls will be made)  
**Customer 3** having 82 orders (meaning 82 outbound calls will be made)

We expose an endpoint in our service that would make 'n' outbound calls to a remote service where 'n' is the number of orders assigned to the customer. 

Each of these calls will respond with 'order details' that contain the total amount for that order. 

The exposed endpoint then sums up all the order amounts for all the orders, and responds with this information.

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


## Constraints

### On the service making the outbound call

1. Will be written with **SpringBoot** and **Kotlin**
2. Will use a **RestTemplate** (not the reactive WebClient)

### On the remote service recieving the outbound call
4. It **does not have a batched endpoint** and expects multiple calls
5. It is **slow** (fixed latency 400ms)

## Tools and Libraries

- Kotlin Coroutines 
- Mockoon (https://mockoon.com/) to simulate remote server
- Locust (https://locust.io/) for load-testing and measuring response times

## Approach

### Step 1: Capture baseline performance
- Fake a remote endpoint using Mockoon 
  - that responds with 'order details' for a particular order. 
  - The order details contains a field for the total order amount
  - There needs to be a fixed latency of 400ms.

- Implement an API endpoint in our service that 
  - makes sequential calls to the remote endpoint to get order details for a particular customer 
  - sum up all the total order amount for all the orders

- The response times are then measured for each customer

```sh
➜  ~ time http GET "http://localhost:8080/v1/customer/1/totalOrderAmount"
HTTP/1.1 200
Connection: keep-alive
Content-Length: 15
Content-Type: text/plain;charset=UTF-8
Date: Sat, 07 Jan 2023 07:21:51 GMT
Keep-Alive: timeout=60

Success(606.32)


http GET "http://localhost:8080/v1/customer/1/totalOrderAmount"  0.23s user 0.07s system 41% cpu 0.722 total

➜  ~ time http GET "http://localhost:8080/v1/customer/2/totalOrderAmount"
HTTP/1.1 200
Connection: keep-alive
Content-Length: 16
Content-Type: text/plain;charset=UTF-8
Date: Sat, 07 Jan 2023 07:22:52 GMT
Keep-Alive: timeout=60

Success(8985.92)


http GET "http://localhost:8080/v1/customer/2/totalOrderAmount"  0.28s user 0.08s system 4% cpu 8.565 total

➜  ~ time http GET "http://localhost:8080/v1/customer/3/totalOrderAmount"
HTTP/1.1 200
Connection: keep-alive
Content-Length: 17
Content-Type: text/plain;charset=UTF-8
Date: Sat, 07 Jan 2023 07:24:57 GMT
Keep-Alive: timeout=60

Success(39619.34)


http GET "http://localhost:8080/v1/customer/3/totalOrderAmount"  0.31s user 0.12s system 1% cpu 33.136 total
```

### Step 2: Using RestTemplates with Kotlin Coroutines

- We write a new controller that uses Kotlin Coroutines to make the outbound calls.

- The response times are then measured for each customer again

```sh
➜  ~ time http GET "http://localhost:8080/v2/customer/1/totalOrderAmount"
HTTP/1.1 200
Connection: keep-alive
Content-Length: 15
Content-Type: text/plain;charset=UTF-8
Date: Sat, 07 Jan 2023 07:26:27 GMT
Keep-Alive: timeout=60

Success(659.27)


http GET "http://localhost:8080/v2/customer/1/totalOrderAmount"  0.25s user 0.07s system 39% cpu 0.810 total

➜  ~ time http GET "http://localhost:8080/v2/customer/2/totalOrderAmount"
HTTP/1.1 200
Connection: keep-alive
Content-Length: 17
Content-Type: text/plain;charset=UTF-8
Date: Sat, 07 Jan 2023 07:26:54 GMT
Keep-Alive: timeout=60

Success(10916.75)


http GET "http://localhost:8080/v2/customer/2/totalOrderAmount"  0.30s user 0.09s system 46% cpu 0.841 total

➜  ~ time http GET "http://localhost:8080/v2/customer/3/totalOrderAmount"
HTTP/1.1 200
Connection: keep-alive
Content-Length: 17
Content-Type: text/plain;charset=UTF-8
Date: Sat, 07 Jan 2023 07:29:05 GMT
Keep-Alive: timeout=60

Success(40021.10)


http GET "http://localhost:8080/v2/customer/3/totalOrderAmount"  0.33s user 0.12s system 40% cpu 1.116 total
```

### Comparison of response-times

| Customer | Order Count | Without Parallelization (ms) | With Parallelization (ms) |
|----------|-------------|------------------------------|---------------------------|
| 1        | 2           | 0.722                        | 0.810 :orange_circle:     |
| 2        | 20          | 8.565                        | 0.841 :green_circle:      | 
 | 3        | 83          | 33.136                       | 1.116 :green_circle:      |


## Observation 

We see a massive improvement in API response time with the use of kotlin coroutines for parallelized outbound API calls. 

## Notes

1. The number of threads available to `Dispatcher.IO` has been set to 250.

2. RestTemplate default connection pool settings have been overridden. Please refer to https://medium.com/@yannic.luyckx/resttemplate-and-connection-pool-617ebd924f68 for more information.

## References

https://kotlinlang.org/docs/composing-suspending-functions.html#concurrent-using-async

