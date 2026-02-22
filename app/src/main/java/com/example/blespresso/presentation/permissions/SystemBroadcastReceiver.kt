package com.example.blespresso.presentation.permissions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

// The SystemBroadcastReceiver listens for Bluetooth changes on the control panel of the phone
// update parameter:  onSystemEvent

@Composable
fun SystemBroadcastReceiver(systemAction:String,
                            onSystemEvent:(intent: Intent?)->Unit)
{
    val context = LocalContext.current
    val currentOnSystemEvent by rememberUpdatedState(onSystemEvent)

    //DisposableEffect -> because we want to trigger it once
    DisposableEffect(context, systemAction)
    {
        val intentFilter = IntentFilter(systemAction)
        val broadcast = object :BroadcastReceiver(){
            override fun onReceive(p0: Context?, intent: Intent?)
            {
                //
                currentOnSystemEvent(intent)
            }
        }
        //register Broadcast receiver
        context.registerReceiver(broadcast, intentFilter)
        //resource is free
        onDispose {
            context.unregisterReceiver(broadcast)
        }
    }
}