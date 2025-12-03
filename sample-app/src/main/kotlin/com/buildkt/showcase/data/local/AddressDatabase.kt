package com.buildkt.showcase.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AddressDto::class],
    version = 1,
    exportSchema = false, // Set to true for production apps to track schema history
)
abstract class AddressDatabase : RoomDatabase() {
    abstract fun addressDao(): AddressDao
}
