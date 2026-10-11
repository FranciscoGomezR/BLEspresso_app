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

// 0x1401 machine status: raw unsigned codes (byte0 = mode, byte1 = state).
// No label/icon/color mapping here; the UI owns it (docs/UX/README.md §8.2).
data class MachineStatus(
    val mode: Int,
    val state: Int
)

data class CharDataClass(
    val charData : Float,
    val connectionState: ConnectionState
)
