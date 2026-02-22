package com.example.blespresso.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.example.blespresso.data.RemoteControlReceiveManager
import com.example.blespresso.data.ble.RemoteControlBLEReceiveManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
//di package -> set gradle dependencies
//the gradle is dependency injection framework which you define how to provide sub-dependencies in this module file
//then the gradle will take care how to provide this dependencies to the APP.

// Tutorial 1 -> Youtube: https://www.youtube.com/watch?v=qyG-SDfYNBE&t=422s&ab_channel=KApps
// vide 1-> Time-Stamp: 1:05:00
object AppModule
{
    //We BluetoothAdapter to check if BLe is enable and establish connection
    @Provides
    @Singleton
    fun provideBluetoothAdapter(@ApplicationContext context: Context): BluetoothAdapter{
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE)as BluetoothManager
        return manager.adapter
    }

    @Provides
    @Singleton
    // video 1-> Time-Stamp: 1:06:00
    fun provideRemoteControlReceiveManager(
        @ApplicationContext context: Context,
        bluetoothAdapter: BluetoothAdapter
    ):RemoteControlReceiveManager{      //This function will return a "RemoteControlReceiveManager" interface
        return RemoteControlBLEReceiveManager(bluetoothAdapter, context)    //the concrete implementation of this is <-
    }
}