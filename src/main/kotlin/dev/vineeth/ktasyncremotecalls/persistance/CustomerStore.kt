package dev.vineeth.ktasyncremotecalls.persistance

import dev.vineeth.ktasyncremotecalls.exceptions.CustomerNotFound
import org.springframework.stereotype.Component
import java.util.*
@Component
class CustomerStore {

    private val customerIdHavingLessOrders = "1"
    private val customerIdHavingMoreOrders = "2"
    private val customerIdHavingMostOrders = "3"

    private val ordersForCustomers = mapOf(
        customerIdHavingLessOrders to (1..2).map { Random().nextInt() },
        customerIdHavingMoreOrders to (1..20).map { Random().nextInt() },
        customerIdHavingMostOrders to (1..83).map { Random().nextInt() },
    )

    fun getOrderIdsForCustomerId(customerId: String) : Result<List<Int>> {
        return when (val orderIds = ordersForCustomers[customerId]) {
            null -> Result.failure(CustomerNotFound(customerId))
            else -> Result.success(orderIds)
        }
    }
}