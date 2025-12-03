package com.buildkt.showcase.data

import com.buildkt.showcase.data.local.AddressDao
import com.buildkt.showcase.data.local.AddressDto
import com.buildkt.showcase.data.local.toDto
import com.buildkt.showcase.domain.Address
import com.buildkt.showcase.domain.AddressRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AddressRepositoryImpl(
    private val addressDao: AddressDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AddressRepository {
    override fun obtainAddresses(): Flow<List<Address>> =
        addressDao
            .getAllAddresses()
            .map { addresses -> addresses.map { address -> address.toDomain() } }

    override suspend fun obtainAddress(id: Long): Address? =
        withContext(context = dispatcher) {
            addressDao.loadAddress(id = id)?.toDomain()
        }

    override suspend fun insertAddress(
        street: String,
        city: String,
        zip: String,
        country: String,
    ): Long =
        withContext(context = dispatcher) {
            addressDao.upsertAddress(address = AddressDto(
                street = street,
                city = city,
                zip = zip,
                country = country
            )
            )
        }

    override suspend fun updateAddress(address: Address): Long =
        withContext(context = dispatcher) {
            addressDao.upsertAddress(address = address.toDto())
        }
}
