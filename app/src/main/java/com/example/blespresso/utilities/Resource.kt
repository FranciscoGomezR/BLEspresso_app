package com.example.blespresso.utilities
// Tutorial 1 -> Youtube: https://www.youtube.com/watch?v=qyG-SDfYNBE&t=422s&ab_channel=KApps
// Time-Stamp: 22:15

// this resource is a helper class, which can graped around some data
// this data can be shown in an UI, so we can inform the user of the current state
sealed class Resource<out T:Any>{
    data class Success<out T:Any> (val  data:T):Resource<T>()
    data class Error(val errorMessage: String):Resource<Nothing>()
    data class Loading<out T:Any>(val data:T? = null, val message: String? = null):Resource<T>()
    data class End<out T:Any> (val  data:T):Resource<T>()
}

