package dev.vineeth.ktasyncremotecalls.exceptions

class CustomerNotFound(
    val customerId: String
) : Exception()