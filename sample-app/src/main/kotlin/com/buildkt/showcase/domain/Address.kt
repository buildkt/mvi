package com.buildkt.showcase.domain

data class Address(
    val id: Long,
    val street: String,
    val city: String,
    val zip: String,
    val country: String,
)
