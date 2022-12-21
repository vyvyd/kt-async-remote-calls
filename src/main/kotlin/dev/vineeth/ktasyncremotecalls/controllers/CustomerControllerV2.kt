package dev.vineeth.ktasyncremotecalls.controllers

import dev.vineeth.ktasyncremotecalls.exceptions.CustomerNotFound
import dev.vineeth.ktasyncremotecalls.persistance.CustomerStore
import dev.vineeth.ktasyncremotecalls.api.OrderDTO
import dev.vineeth.ktasyncremotecalls.api.Problem
import kotlinx.coroutines.*
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.util.*


private val logger = LoggerFactory.getLogger(CustomerControllerV2::class.java)

@RestController
class CustomerControllerV2(
    @Qualifier("ordersRestTemplate") private val restTemplate: RestTemplate,
    private val customerStore: CustomerStore
) {

    @GetMapping("/v2/customer/{customerId}/totalOrderAmount")
    fun ordersForCustomer(
        @PathVariable("customerId") customerId: String
    ) : ResponseEntity<String> {

        val totalOrderSum = customerStore
            .getOrderIdsForCustomerId(customerId)
            .map { calculateTotalOrderAmount(it) }
            .onFailure { throw (it) }

        return ResponseEntity.ok("$totalOrderSum")
    }

    private fun calculateTotalOrderAmount(orderIds: List<Int>): BigDecimal {
        return runBlocking {
            val jobs = orderIds.map { orderId ->
                async {
                    val currentTime = System.currentTimeMillis()
                    val orderDTO = withContext(Dispatchers.IO) {
                        logger.info("Making call for order ${orderId}")
                        restTemplate.getForEntity("/orders/${orderId}/details", OrderDTO::class.java)
                    }
                    orderDTO.body.also {
                        logger.info("Recieved response for order ${orderId}. Took ${System.currentTimeMillis() - currentTime}")
                    }
                }
            }
            val orderDTOS = jobs.awaitAll()
            orderDTOS.fold(BigDecimal.ZERO) { acc, o -> acc + checkNotNull(o).amount }
            }
        }

    @ExceptionHandler(CustomerNotFound::class)
    @ResponseStatus(value= HttpStatus.NOT_FOUND)
    @ResponseBody
    fun customerNotInListOfKnownCustomers(exception: CustomerNotFound): ErrorResponse {
        return Problem(HttpStatusCode.valueOf(404), "customer with id ${exception.customerId} not found");
    }

}