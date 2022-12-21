package dev.vineeth.ktasyncremotecalls.exceptions

/**
 * This exception is thrown when the customer id is
 * not in the list of known customer ids defined in
 * CustomerStore.kt
 */
class CustomerNotFound(
    val customerId: String
) : Exception()