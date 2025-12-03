package com.buildkt.showcase.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {
    /**
     * Deletes a specific address from the database.
     *
     * @param address The address to be deleted.
     */
    @Delete
    suspend fun deleteAddress(address: AddressDto)

    /**
     * Retrieves a single address by its unique ID.
     *
     * @param id The ID of the address.
     * @return The Address object, or null if not found.
     */
    @Query("SELECT * FROM addresses WHERE id = :id")
    suspend fun getAddressById(id: Long): AddressDto?

    /**
     * Retrieves all addresses from the database, ordered by country and city.
     * The results are provided as a Flow, which will automatically emit new lists
     * whenever the data in the 'addresses' table changes.
     *
     * @return A Flow emitting a list of all addresses.
     */
    @Query("SELECT * FROM addresses ORDER BY country, city ASC")
    fun getAllAddresses(): Flow<List<AddressDto>>

    /**
     * Inserts or updates an address. If an address with the same primary key exists, it's updated.
     *
     * @param address The address to be inserted or updated.
     * @return The rowId of the item that was inserted or updated. This will be the new auto-generated ID for an insert.
     */
    @Upsert
    suspend fun upsertAddress(address: AddressDto): Long

    @Query("SELECT * FROM addresses WHERE id = :id LIMIT 1")
    fun loadAddress(id: Long): AddressDto?
}
