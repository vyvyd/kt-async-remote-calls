package dev.vineeth.ktasyncremotecalls.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import dev.vineeth.ktasyncremotecalls.exceptions.CustomerNotFound
import dev.vineeth.ktasyncremotecalls.persistance.CustomerStore
import dev.vineeth.ktasyncremotecalls.api.OrderDTO
import dev.vineeth.ktasyncremotecalls.api.Problem
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.*
import java.io.IOException
import java.math.BigDecimal
import java.util.*


private val logger = LoggerFactory.getLogger(CustomerControllerWithParallelizationUsingRestTemplates::class.java)

@RestController
class CustomerControllerWithParallelizationUsingOkHttpClient(
    @Qualifier("ordersOkHttpClient") private val httpClient: OkHttpClient,
    @Qualifier("objectMapper") private val objectMapper: ObjectMapper,
    private val customerStore: CustomerStore
) {

    @GetMapping("/v22/customer/{customerId}/totalOrderAmount")
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
                        val request = Request.Builder()
                            .url("http://localhost:3005/orders/${orderId}/details")
                            .build()
                        httpClient.newCall(request).execute().use { response ->
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")

                            val responseBody = response.body
                            checkNotNull(responseBody)
                            objectMapper.readValue(responseBody.string(), OrderDTO::class.java)
                        }
                    }
                    orderDTO.also {
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