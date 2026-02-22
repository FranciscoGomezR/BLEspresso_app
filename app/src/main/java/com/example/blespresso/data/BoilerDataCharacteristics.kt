package com.example.blespresso.data
// Tutorial 1 -> Youtube: https://www.youtube.com/watch?v=qyG-SDfYNBE&t=422s&ab_channel=KApps
// Time-Stamp: 23:30

// class with data properties
data class BoilerDataResult(
    val waterTemperature    : Float,
    val connectionState: ConnectionState
)

data class BoilerSetpoint(
    val targetTemperature    : Float,
    val connectionState: ConnectionState
)

data class CharDataClass(
    val charData : Float,
    val connectionState: ConnectionState
)
