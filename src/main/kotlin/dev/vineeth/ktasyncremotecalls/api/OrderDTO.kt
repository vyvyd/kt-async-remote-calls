package dev.vineeth.ktasyncremotecalls.api

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class OrderDTO(
    @JsonProperty("orderId") val orderId: String,
    @JsonProperty("amount")  val amount: BigDecimal
)