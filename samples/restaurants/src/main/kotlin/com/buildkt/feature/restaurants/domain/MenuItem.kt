package com.buildkt.feature.restaurants.domain

data class MenuItem(
    val id: String,
    val name: String,
    val description: String,
    val price: String,
    val originalPrice: String? = null,
    val imageUrl: String,
    val tags: List<String> = emptyList(),
)