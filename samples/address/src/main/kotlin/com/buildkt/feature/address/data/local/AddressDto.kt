package com.buildkt.feature.address.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.buildkt.feature.address.domain.Address

@Entity(tableName = "addresses")
data class AddressDto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val street: String,
    val city: String,
    val zip: String,
    val country: String,
) {
    fun toDomain(): Address =
        Address(
            id = id,
            street = street,
            city = city,
            zip = zip,
            country = country,
        )
}

fun Address.toDto(): AddressDto =
    AddressDto(
        id = id,
        street = street,
        city = city,
        zip = zip,
        country = country,
    )
