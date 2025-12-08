package com.buildkt.showcase

import android.content.Context
import androidx.room.Room
import com.buildkt.feature.address.data.AddressRepositoryImpl
import com.buildkt.feature.address.data.local.AddressDao
import com.buildkt.feature.address.data.local.AddressDatabase
import com.buildkt.feature.address.domain.AddressRepository
import com.buildkt.feature.restaurants.data.RestaurantRepositoryImpl
import com.buildkt.feature.restaurants.domain.RestaurantRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@EntryPoint
@InstallIn(ActivityComponent::class)
interface SampleAppEntryPoint {

    fun getAddressRepository(): AddressRepository

    fun getRestaurantsRepository(): RestaurantRepository
}

@Module
@InstallIn(SingletonComponent::class)
object SampleAppDataModule {

    @Provides
    @Singleton
    fun provideAddressDatabase(@ApplicationContext context: Context): AddressDatabase = Room
        .databaseBuilder(context, AddressDatabase::class.java, "playground_database")
        .build()

    @Provides
    @Singleton
    fun provideAddressDao(database: AddressDatabase): AddressDao = database.addressDao()

    @Provides
    @Singleton
    fun provideAddressRepository(addressDao: AddressDao): AddressRepository =
        AddressRepositoryImpl(addressDao)

    @Provides
    @Singleton
    fun provideRestaurantRepository(): RestaurantRepository =
        RestaurantRepositoryImpl()
}