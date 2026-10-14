package com.example.blespresso.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Tutorial 1 -> Youtube: https://www.youtube.com/watch?v=qyG-SDfYNBE&t=422s&ab_channel=KApps
@Composable
fun Navigation(
    onBluetoothStateChanged:()->Unit    //pass var that contains bluetooth changes
)
{
    val navController = rememberNavController()
    // Footer (STATUS #14) below NavHost: screens fill weighted Box, never overlapped.
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            // Tutorial1_Time_stamp: 4:20
            NavHost(navController = navController, startDestination = Screen.StartScreen.route) {
                composable(Screen.StartScreen.route){
                    StartScreen(navController = navController)
                }
                composable(Screen.BlespressoMainScreen.route){
                    RemoteControlScreen(
                        navController = navController,
                        onBluetoothStateChanged
                    )
                }
                composable(Screen.BlespressoCtrlParamScreen.route){
                    BlespressoCtrlParamScreen(
                        navController = navController,
                        onBluetoothStateChanged
                    )
                }
            }
        }
        AppFooter()
    }

}


// Tutorial1_Time_stamp: 4:06
sealed class Screen(val route:String)
{
    object StartScreen:Screen("start_screen")
    object BlespressoMainScreen:Screen("BLE_espresso_main_screen")
    object BlespressoCtrlParamScreen:Screen("BLE_espresso_CtrlParameters_screen")
}

