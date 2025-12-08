package com.buildkt.feature.address.domain

data class Address(
    val id: Long,
    val street: String,
    val city: String,
    val zip: String,
    val country: String,
)
