package com.floristeriaakasia.backend.feature.product.domain.model

import java.math.BigDecimal

data class Money(
    val amount: BigDecimal,
    val currency: String = "COP"
) {
    init {
        require(amount >= BigDecimal.ZERO) { "Amount cannot be negative" }
    }
    operator fun compareTo(other: Money): Int {
        require(this.currency == other.currency) { "Cannot compare different currencies" }
        return this.amount.compareTo(other.amount)
    }
}
