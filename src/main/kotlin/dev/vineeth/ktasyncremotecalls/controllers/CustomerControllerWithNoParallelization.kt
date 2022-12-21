package dev.vineeth.ktasyncremotecalls.controllers

import dev.vineeth.ktasyncremotecalls.exceptions.CustomerNotFound
import dev.vineeth.ktasyncremotecalls.persistance.CustomerStore
import dev.vineeth.ktasyncremotecalls.api.OrderDTO
import dev.vineeth.ktasyncremotecalls.api.Problem
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.util.*

@RestController
class CustomerControllerWithNoParallelization(
    @Qualifier("ordersRestTemplate") private val restTemplate: RestTemplate,
    private val customerStore: CustomerStore
) {

    @GetMapping("/v1/customer/{customerId}/totalOrderAmount")
    fun ordersForCustomer(
        @PathVariable("customerId") customerId: String
    ) : ResponseEntity<String> {

        val totalOrderSum = customerStore
            .getOrderIdsForCustomerId(customerId)
            .map { calculateTotalOrderAmount(it) }
            .onFailure { throw (it) }

        return ResponseEntity.ok("$totalOrderSum")
    }

    private fun calculateTotalOrderAmount(orderIds: List<Int>) : BigDecimal {
        var totalAmount = BigDecimal.ZERO
        orderIds.map { orderId ->
            val order = restTemplate
                .getForEntity("/orders/${orderId}/details", OrderDTO::class.java)
            totalAmount = totalAmount.plus(checkNotNull(order.body).amount)
        }
        return totalAmount
    }

    @ExceptionHandler(CustomerNotFound::class)
    @ResponseStatus(value= HttpStatus.NOT_FOUND)
    @ResponseBody
    fun customerNotInListOfKnownCustomers(exception: CustomerNotFound): ErrorResponse {
        return Problem(HttpStatusCode.valueOf(404), "customer with id ${exception.customerId} not found");
    }

}