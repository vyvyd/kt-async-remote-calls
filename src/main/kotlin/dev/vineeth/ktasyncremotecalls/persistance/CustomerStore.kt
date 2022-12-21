package dev.vineeth.ktasyncremotecalls.persistance

import dev.vineeth.ktasyncremotecalls.exceptions.CustomerNotFound
import org.springframework.stereotype.Component
import java.util.*
@Component
class CustomerStore {

    private val CUSTOMER_WITH_LESS_ORDERS = "1"
    private val CUSTOMER_WITH_MORE_ORDERS = "2"
    private val CUSTOMER_WITH_MASSIVE_ORDERS = "3"

    private val ordersForCustomers = mapOf(
        CUSTOMER_WITH_LESS_ORDERS to (1..2).map { Random().nextInt() },
        CUSTOMER_WITH_MORE_ORDERS to (1..20).map { Random().nextInt() },
        CUSTOMER_WITH_MASSIVE_ORDERS to (1..83).map { Random().nextInt() },
    )

    fun getOrderIdsForCustomerId(customerId: String) : Result<List<Int>> {
        return when (val orderIds = ordersForCustomers[customerId]) {
            null -> Result.failure(CustomerNotFound(customerId))
            else -> Result.success(orderIds)
        }
    }
}