package com.buildkt.showcase

import android.content.Context
import androidx.room.Room
import com.buildkt.showcase.data.AddressRepositoryImpl
import com.buildkt.showcase.data.local.AddressDao
import com.buildkt.showcase.data.local.AddressDatabase
import com.buildkt.showcase.domain.AddressRepository
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
interface PlaygroundAppEntryPoint {

    fun getAddressRepository(): AddressRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DeliveryAppDataModule {

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
}