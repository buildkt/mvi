package com.buildkt.showcase.domain

import kotlinx.coroutines.flow.Flow

interface AddressRepository {
    fun obtainAddresses(): Flow<List<Address>>

    suspend fun obtainAddress(id: Long): Address?

    suspend fun insertAddress(
        street: String,
        city: String,
        zip: String,
        country: String,
    ): Long

    suspend fun updateAddress(address: Address): Long
}
