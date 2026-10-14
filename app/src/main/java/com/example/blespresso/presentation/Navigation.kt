package com.example.blespresso.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
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
    // STATUS #15: one ViewModel for all screens. Created outside NavHost -> owner is the
    // activity, so connection state, demo flag and loaded values survive navigation.
    val viewModel: RemoteControlViewModel = hiltViewModel()
    // Footer (STATUS #14) below NavHost: screens fill weighted Box, never overlapped.
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            // Tutorial1_Time_stamp: 4:20
            NavHost(navController = navController, startDestination = Screen.StartScreen.route) {
                composable(Screen.StartScreen.route){
                    StartScreen(navController = navController)
                }
                composable(Screen.ConnectScreen.route){
                    ConnectScreen(
                        navController = navController,
                        onBluetoothStateChanged,
                        viewModel = viewModel
                    )
                }
                composable(Screen.BlespressoMainScreen.route){
                    RemoteControlScreen(
                        navController = navController,
                        onBluetoothStateChanged,
                        viewModel = viewModel
                    )
                }
                composable(Screen.BlespressoCtrlParamScreen.route){
                    BlespressoCtrlParamScreen(
                        navController = navController,
                        onBluetoothStateChanged,
                        viewModel = viewModel
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
    object ConnectScreen:Screen("connect_screen")
    object BlespressoMainScreen:Screen("BLE_espresso_main_screen")
    object BlespressoCtrlParamScreen:Screen("BLE_espresso_CtrlParameters_screen")
}

