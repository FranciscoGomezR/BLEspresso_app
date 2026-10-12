package com.example.blespresso

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.example.blespresso.presentation.Navigation
import com.example.blespresso.ui.theme.BlespressoAppTheme
import com.example.blespresso.ui.theme.ThemePrefs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// Best youtube tutorial: https://www.youtube.com/watch?v=qyG-SDfYNBE&t=422s&ab_channel=KApps
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    //inject BLE adapter to the gradle from the APP module
    //here the app/mobile will check the BLE connection
    @Inject lateinit var bluetoothAdapter: BluetoothAdapter

    //life cycle of this function is -> invoke when App starts
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Saved light/dark choice (TODO #13), applied before the first frame
        ThemePrefs.load(this)

        setContent {
            BlespressoAppTheme{
                Navigation(
                    onBluetoothStateChanged = {
                        showBluetoothDialog()
                    }
                )
            }
        }
    }

    //life cycle of this function is -> invoke when user minimize the App
    override fun onStart() {
        super.onStart()
        //Check BLE is enable
        showBluetoothDialog()
    }

    private var isBluetoothDialogAlreadyShown=false
    private fun showBluetoothDialog(){
        //First let's check if BLE is not enable
        if(!bluetoothAdapter.isEnabled){
            //Check if dialog is not already display
            if (!isBluetoothDialogAlreadyShown){
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                //Prompt dialog to enable BLE
                startBluetoothIntentForResult.launch(enableBluetoothIntent)
                //Enable FLAG to show it once a the time
                isBluetoothDialogAlreadyShown = true
            }

        }
    }

    //check for the result fo the user's click
    private val startBluetoothIntentForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            isBluetoothDialogAlreadyShown = false
            //if user denies BLE to be enable
            if(result.resultCode != Activity.RESULT_OK)
            {
                //Keep showing dialog -> Asking permission to enable it
                showBluetoothDialog()
            }
        }
}

