package com.buildkt.feature.restaurants.domain

data class RestaurantInfo(
    val id: Int = Int.MAX_VALUE,
    val name: String = "",
    val rating: String = "",
    val reviewCount: String = "",
    val deliveryFee: String = "",
    val deliveryInfo: String = "",
    val deliveryTime: String = "",
    val minOrderValue: String = "",
    val imageUrl: String = "",
)