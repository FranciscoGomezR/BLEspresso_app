package com.example.blespresso.presentation

import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.blespresso.data.ConnectionState
import com.example.blespresso.presentation.permissions.PermissionUtils
import com.example.blespresso.presentation.permissions.SystemBroadcastReceiver
import com.example.blespresso.ui.theme.accent_indic
import com.example.blespresso.ui.theme.background_indic
import com.example.blespresso.ui.theme.textprimary_indic
import com.example.blespresso.ui.theme.textsecondary_indic
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

// TLDR: STATUS #15 - owns permissions + scan/connect progress + error/retry + scan-timeout prompt.
//       Moved out of RemoteControlScreen. Leaves to the main screen on Connected or Demo mode.
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ConnectScreen(
    navController: NavController,
    onBluetoothStateChanged: () -> Unit,
    viewModel: RemoteControlViewModel
) {
    SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_STATE_CHANGED) { bluetoothState ->
        val action = bluetoothState?.action ?: return@SystemBroadcastReceiver
        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            onBluetoothStateChanged()
        }
    }
    val permissionState = rememberMultiplePermissionsState(permissions = PermissionUtils.permissions)
    val lifecycleOwner = LocalLifecycleOwner.current
    val bleConnectionState = viewModel.connectionState

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                permissionState.launchMultiplePermissionRequest()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted && bleConnectionState == ConnectionState.Uninitialized) {
            Log.d("ScreenNavigation", "ConnectScreen - trigger a BLE connection")
            viewModel.initiazeConnection()
        }
    }

    LaunchedEffect(bleConnectionState) {
        if (bleConnectionState == ConnectionState.Connected) {
            navigateToMain(navController)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background_indic),
        contentAlignment = Alignment.Center
    ) {
        when {
            !permissionState.allPermissionsGranted -> Text(
                text = "Go to app setting and allow missing permissions.",
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(10.dp),
                textAlign = TextAlign.Center
            )
            viewModel.isScanTimeout -> AlertDialog(
                onDismissRequest = {},
                title = { Text(text = "No espresso machine found") },
                text = { Text(text = "Retry the scan, or continue without a machine (demo mode: " +
                        "all values 0, Read / Write disabled). Restart the app to leave demo mode.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.retryConnection() }) { Text(text = "Retry") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.enterDemoMode()
                        navigateToMain(navController)
                    }) { Text(text = "Continue without machine") }
                }
            )
            viewModel.errorMessage != null -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = viewModel.errorMessage!!)
                Button(onClick = { viewModel.retryConnection() }) {
                    Text(text = "Try again.")
                }
            }
            else -> Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(accent_indic, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(220.dp),
                    color = textsecondary_indic,
                    strokeWidth = 10.dp
                )
                viewModel.initialazingMessage?.let {
                    Text(text = it, color = textprimary_indic, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

private fun navigateToMain(navController: NavController) {
    navController.navigate(Screen.BlespressoMainScreen.route) {
        popUpTo(Screen.ConnectScreen.route) { inclusive = true }
    }
}
