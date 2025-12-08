package com.buildkt.feature.restaurants.domain

import java.util.UUID

data class RestaurantInfo(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val rating: String = "",
    val reviewCount: String = "",
    val deliveryFee: String = "",
    val deliveryInfo: String = "",
    val deliveryTime: String = "",
    val minOrderValue: String = "",
    val imageUrl: String = "",
)