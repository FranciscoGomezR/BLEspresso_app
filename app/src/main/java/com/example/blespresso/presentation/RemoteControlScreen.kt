package com.example.blespresso.presentation

import android.bluetooth.BluetoothAdapter
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Unspecified
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.blespresso.data.ConnectionState
import com.example.blespresso.presentation.permissions.PermissionUtils
import com.example.blespresso.presentation.permissions.SystemBroadcastReceiver
import com.example.blespresso.ui.theme.accent
import com.example.blespresso.ui.theme.accent_indic
import com.example.blespresso.ui.theme.background
import com.example.blespresso.ui.theme.background_indic
import com.example.blespresso.ui.theme.surface
import com.example.blespresso.ui.theme.surface_indic
import com.example.blespresso.ui.theme.textprimary
import com.example.blespresso.ui.theme.textprimary_indic
import com.example.blespresso.ui.theme.textsecondary
import com.example.blespresso.ui.theme.textsecondary_indic
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay

/*  USEFUL LINKS:
    - Double taps in buttons: https://www.kodeco.com/34465692-gestures-in-jetpack-compose-getting-started
    - Jetpack compose - padding: http://joebirch.co/android/exporing-jetpack-compose-padding-modifier/
    - Jetpack compose: https://www.jetpackcompose.net/compose-layout-row-and-column


    - ChatGPT:  https://chat.openai.com/c/d97c6491-c358-4d1b-8962-8f4605ab354d
 */

@Immutable
data class RemoteControlScreenTokens(
    // --- Typography ---
    val ButtonType2_Font: TextUnit = 14.sp,
    val TParameter_fontsize: TextUnit = 14.sp,
    val TableParam_fontsize: TextUnit = 14.sp,
    val ParamValue_fontsize: TextUnit = 16.sp,
    val MinMaxParam_fontsize: TextUnit = 12.sp,
)

/**
 * Layout constants are intentionally centralized to make it easy to add new
 * parameters/rows/buttons without hunting for magic numbers.
 */
private object RemoteControlLayout {
    val contentWidth = 380.dp
    val titleFontSize = 17.sp
    val titleDividerHeight = 1.dp

    val sectionPadding = 5.dp
    val sectionInsetStart = 10.dp
    val rowHeight = 40.dp
    val buttonRowHeight = 50.dp
    val buttonHeight = 40.dp

    // Text field heights used across the screen
    val textFieldHeight = 28.dp
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalComposeUiApi::class)
@Composable
fun RemoteControlScreen(
    navController: NavController,
    onBluetoothStateChanged:()->Unit,
    viewModel: RemoteControlViewModel = hiltViewModel() //// video 1-> Time-Stamp: 1:16:40
    ,tokens: RemoteControlScreenTokens = RemoteControlScreenTokens()
){
    //this function will run if there is a change from the phone's control panel
    SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_STATE_CHANGED)
    {bluetoothState ->
        //if action is null then we can return-> return@SystemBroadcastReceiver
        val action = bluetoothState?.action ?: return@SystemBroadcastReceiver
        if (action == BluetoothAdapter.ACTION_STATE_CHANGED){
            //Check for bluetooth connection
            onBluetoothStateChanged()
        }
    }
    // video 1-> Time-Stamp: 1:15:00
    //get permissions for our app to run
    val permissionState = rememberMultiplePermissionsState(permissions = PermissionUtils.permissions)
    val lifecycleOwner = LocalLifecycleOwner.current
    val bleConnectionState = viewModel.connectionState

    var MoveToOtherScreen by remember { mutableStateOf(false) }

    val cBoilerTB = remember { CTextBox() }
    cBoilerTB.maxInValue = 150.0f
    cBoilerTB.minInValue = 0.0f

    var cPreinfusionPWR_TB = remember { CTextBox() }
    cPreinfusionPWR_TB.maxInValue = 50.0f
    cPreinfusionPWR_TB.minInValue = 0.0f
    var cPreinfusionTMR_TB = remember { CTextBox() }
    cPreinfusionTMR_TB.maxInValue = 15.0f
    cPreinfusionTMR_TB.minInValue = 0.0f
    val cInfusionPWR_TB = remember { CTextBox() }
    cInfusionPWR_TB.maxInValue = 100.0f
    cInfusionPWR_TB.minInValue = 0.0f
    val cInfusionTMR_TB  = remember { CTextBox() }
    cInfusionTMR_TB.maxInValue = 15.0f
    cInfusionTMR_TB.minInValue = 1.0f
    val cDeclinPrPWR_TB = remember { CTextBox() }
    cDeclinPrPWR_TB.maxInValue = 100.0f
    cDeclinPrPWR_TB.minInValue = 70.0f
    val cDeclinPrTMR_TB = remember { CTextBox() }
    cDeclinPrTMR_TB.maxInValue = 15.0f
    cDeclinPrTMR_TB.minInValue = 0.0f

    //Controller Parameter
    val cPterm_TB = remember { CTextBox() }
    cPterm_TB.maxInValue = 100.0f
    cPterm_TB.minInValue = 0.0f
    val cIterm_TB = remember { CTextBox() }
    cIterm_TB.maxInValue = 20.0f
    cIterm_TB.minInValue = 0.0f
    val cIlimit_TB = remember { CTextBox() }
    cIlimit_TB.maxInValue = 500.0f
    cIlimit_TB.minInValue = 0.0f
    val cIwindup_TB = remember { CTextBox() }
    cIwindup_TB.maxInValue = 1.0f
    cIwindup_TB.minInValue = 0.0f
    val cDterm_TB = remember { CTextBox() }
    cDterm_TB.maxInValue = 10.0f
    cDterm_TB.minInValue = 0.0f
    val cLPFterm_TB = remember { CTextBox() }
    cLPFterm_TB.maxInValue = 250.0f
    cLPFterm_TB.minInValue = 5.0f
    val cGainterm_TB = remember { CTextBox() }
    cGainterm_TB.maxInValue = 100.0f
    cGainterm_TB.minInValue = 0.0f

    DisposableEffect(
        key1 = lifecycleOwner,
        effect = {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    Log.d("ScreenNavigation", "Disposable Effect - cycle event ON_START")
                    // video 1-> Time-Stamp: 1:17:40
                    //request permissions
                    permissionState.launchMultiplePermissionRequest()
                    //this is the case when user already had a connection and minimize the app, then connection state will be disconnected
                    if (permissionState.allPermissionsGranted && bleConnectionState == ConnectionState.Disconnected) {
                        //then reconnect
                        viewModel.reconnect()
                    }
                }
                // video 1-> Time-Stamp: 1:18:30
                /*
                if (event == Lifecycle.Event.ON_STOP && !MoveToOtherScreen) {
                    Log.d("ScreenNavigation", "Disposable Effect - cycle event ON_STOP")
                    if (bleConnectionState == ConnectionState.Connected) {
                        viewModel.disconnect()
                    }
                }
                */

                if (event == Lifecycle.Event.ON_STOP ) {
                    Log.d("ScreenNavigation", "Disposable Effect - cycle event ON_STOP")
                    if (bleConnectionState == ConnectionState.Connected) {
                        viewModel.disconnect()
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    )
    // video 1-> Time-Stamp: 1:19:25
    LaunchedEffect(
        key1 = permissionState.allPermissionsGranted
    )
    {
        if (permissionState.allPermissionsGranted) {
            Log.d("ScreenNavigation", "Launched Effect - permission granted")
            //if all permission are granted
            if (bleConnectionState == ConnectionState.Uninitialized) {
                Log.d("ScreenNavigation", "Trigger a BLE connection")
                //this function will trigger if connection is uninitialized
                viewModel.initiazeConnection()
            }
        }
    }

    // MutableState to trigger the long-running task
    var passTempValModeltoVar by remember { mutableStateOf(false) }
    LaunchedEffect(passTempValModeltoVar)
    {
        delay(500L)
        cBoilerTB.userInput = TextFieldValue(viewModel.boilerSetpointTemp.toString())
        cBoilerTB.clickedOnce = false
        passTempValModeltoVar = false
    }
    var passRxvalfromBLEtoVar by remember { mutableStateOf(false) }
    LaunchedEffect(passRxvalfromBLEtoVar)
    {
        delay(1500L)
        Log.d("ScreenBREWconfiguration", "PASS INFO TO TEXTBOX")
        cPreinfusionPWR_TB.userInput =
            TextFieldValue(viewModel.brewConfigData.PreInfussionPwr.toString())
        cPreinfusionPWR_TB.originalValue = viewModel.brewConfigData.PreInfussionPwr.toString()
        cPreinfusionPWR_TB.modified = false
        cPreinfusionPWR_TB.clickedOnce = false
        //-----------------------------------------------------------------------------------------
        cPreinfusionTMR_TB.userInput =
            TextFieldValue(viewModel.brewConfigData.PreInfussionTmr.toString())
        cPreinfusionTMR_TB.originalValue = viewModel.brewConfigData.PreInfussionTmr.toString()
        cPreinfusionTMR_TB.modified = false
        cPreinfusionTMR_TB.clickedOnce = false
        //-----------------------------------------------------------------------------------------
        cInfusionPWR_TB.userInput =
            TextFieldValue(viewModel.brewConfigData.InfussionPwr.toString())
        cInfusionPWR_TB.originalValue = viewModel.brewConfigData.InfussionPwr.toString()
        cInfusionPWR_TB.modified = false
        cInfusionPWR_TB.clickedOnce = false
        //-----------------------------------------------------------------------------------------
        cInfusionTMR_TB.userInput =
            TextFieldValue(viewModel.brewConfigData.InfussionTmr.toString())
        cInfusionTMR_TB.originalValue = viewModel.brewConfigData.InfussionTmr.toString()
        cInfusionTMR_TB.modified = false
        cInfusionTMR_TB.clickedOnce = false
        //-----------------------------------------------------------------------------------------
        cDeclinPrPWR_TB.userInput =
            TextFieldValue(viewModel.brewConfigData.DecliningPwr.toString())
        cDeclinPrPWR_TB.originalValue = viewModel.brewConfigData.DecliningPwr.toString()
        cDeclinPrPWR_TB.modified = false
        cDeclinPrPWR_TB.clickedOnce = false
        //-----------------------------------------------------------------------------------------
        cDeclinPrTMR_TB.userInput =
            TextFieldValue(viewModel.brewConfigData.DecliningTmr.toString())
        cDeclinPrTMR_TB.originalValue = viewModel.brewConfigData.DecliningTmr.toString()
        cDeclinPrTMR_TB.modified = false
        cDeclinPrTMR_TB.clickedOnce = false

        // Reset the flag
        passRxvalfromBLEtoVar = false
    }

    // Auto-load brew config and boiler temp when connection is established
    LaunchedEffect(bleConnectionState) {
        if (bleConnectionState == ConnectionState.Connected && !viewModel.hasAutoLoadedBrewConfig) {
            Log.d("ScreenBREWconfiguration", "Auto-loading brew config on connection")
            viewModel.rxReadBrewConfiguration()
            passRxvalfromBLEtoVar = true
            viewModel.hasAutoLoadedBrewConfig = true
            delay(500L)
            // Also auto-load boiler temperature
            Log.d("ScreenNavigation", "Auto-loading boiler temperature on connection")
            viewModel.rxNewBoilerTargetTemp()
            passTempValModeltoVar = true
        } else if (bleConnectionState != ConnectionState.Connected) {
            viewModel.hasAutoLoadedBrewConfig = false // Reset for next connection
        }
    }

    // Reset the auto-load flag when the composable is disposed (user navigates away)
    DisposableEffect(Unit) {
        onDispose {
            viewModel.hasAutoLoadedBrewConfig = false
        }
    }

    // video 1-> Time-Stamp: 1:20:00
    //this box will be shown when trying to connect to a BLE device
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background_indic),
        contentAlignment = Alignment.Center
    ) {
        if (bleConnectionState == ConnectionState.CurrentlyInitializing ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(accent_indic, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(220.dp),
                    color = textsecondary_indic,
                    strokeWidth = 10.dp
                )
                if (viewModel.initialazingMessage != null) {
                    Text(
                        text = viewModel.initialazingMessage!!,
                        //text = " Scanning\n for\n Espresso Machine",
                        color = textprimary_indic,
                        textAlign = TextAlign.Center
                    )

                }
            }
        } else if (!permissionState.allPermissionsGranted ) //if not all permission are granted
        {
            // video 1-> Time-Stamp: 1:22:50
            Text(
                text = "Go to app setting and allow missing permissions.",
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(10.dp),
                textAlign = TextAlign.Center
            )
        } else if (viewModel.errorMessage != null ) {
            // video 1-> Time-Stamp: 1:24:00
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = viewModel.errorMessage!!
                )
                Button(
                    onClick = {
                        if (permissionState.allPermissionsGranted) {
                            viewModel.initiazeConnection()
                        }
                    }
                ) {
                    Text(
                        text = "Try again."
                    )
                }
            }
        } else if (bleConnectionState == ConnectionState.Connected) {
            var isFocused by remember { mutableStateOf(false) }

            var newTargetTemperature by remember {
                mutableStateOf(0f)
            }

            // Get the software keyboard controller
            val keyboardController = LocalSoftwareKeyboardController.current
            val context = LocalContext.current
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                //  FIRST THIRD OF SCREEN
                //  BOILER TEMPERATURE INDICATOR     ---------------------------------------------------
                //  ------------------------------------------------------------------------------------
                Row(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxWidth()
                ) {
                    // Content for the first row
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(background_indic),
                        contentAlignment = Alignment.Center
                    ) {
                        //Log.d("TemperatureScreen","Print Temp Value: ${viewModel.boilerWaterTemp}")
                        BoilerTemperatureProgressIndicator(
                            modifier = Modifier
                                .size(280.dp)
                                .background(background_indic),
                            boilerWaterTemp = viewModel.boilerWaterTemp,
                            boilerTargetTemp = viewModel.boilerSetpointTemp,
                            primaryColor = accent_indic,
                            secondaryColor = surface_indic,
                            primaryTextColor = textprimary_indic,
                            secondaryTextColor = textsecondary_indic,
                            circleRadius = 280f,
                        )
                    }
                }
                //  SECOND THIRD OF SCREEN
                //  BOILER TEMP & BREW CONFIGURATION ---------------------------------------------------
                //  ------------------------------------------------------------------------------------
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(background)
                        .padding(top = 1.dp, bottom = 1.dp) // Add spacing between rows
                ) {
                    // Content for the second row
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        //  BOILER CONFIGURATION MENU
                        //  ----------------------------------------------------------------------------
                        Row(    // Title Row
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Machine Configuration",
                                modifier = Modifier.padding(start = 10.dp),
                                style = TextStyle(
                                    fontSize = 17.sp,
                                    color = textprimary
                                )
                            )
                        }
                        Divider( //  TITLE DIVIDER   ---------------------------------------------------
                            color = textprimary,
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .size(width = 380.dp, height = 1.dp)
                        )
                        Row(
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .size(width = 380.dp, height = 60.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            //A  Row containing column has a span/weight of 8f
                            Column( // 1 COLUMN → Target Temperature text------------------------
                                modifier = Modifier.weight(3f).padding(start = 15.dp, top = 2.dp)
                            ) {
                                Text( text = "Brew Temperature:",
                                    style = TextStyle(fontSize = tokens.TParameter_fontsize, color = textsecondary), modifier = Modifier.height(30.dp).wrapContentHeight(align = Alignment.CenterVertically)
                                )
                                Text( text = "Steam Temperature:",
                                    style = TextStyle(fontSize = tokens.TParameter_fontsize, color = textsecondary), modifier = Modifier.height(30.dp).wrapContentHeight(align = Alignment.CenterVertically)
                                )
                            }
                            Column( // 2 COLUMN → Target Temperature text------------------------
                                modifier = Modifier.weight(2f).padding(start = 15.dp, top = 2.dp)
                            ) {
                                Text( text = "(0 - 100°C)",
                                    style = TextStyle(fontSize = tokens.MinMaxParam_fontsize, color = textsecondary, fontStyle = FontStyle.Italic), modifier = Modifier.height(30.dp).wrapContentHeight(align = Alignment.CenterVertically)
                                )
                                Text( text = "(100 - 140°C)",
                                    style = TextStyle(fontSize = tokens.MinMaxParam_fontsize, color = textsecondary, fontStyle = FontStyle.Italic), modifier = Modifier.height(30.dp).wrapContentHeight(align = Alignment.CenterVertically)
                                )
                            }
                            Column( // 3 COLUMN → 2f Target Value--------------------------
                                modifier = Modifier.weight(2f).padding(top = 2.dp),
                                horizontalAlignment = Alignment. CenterHorizontally
                            ) {
                                BasicTextField(
                                    value = cBoilerTB.userInput,
                                    onValueChange = { newText ->
                                        processTextFieldInput(newText, cBoilerTB) },
                                    cursorBrush = SolidColor(Unspecified),
                                    textStyle = TextStyle(color = textprimary, fontSize = tokens.ParamValue_fontsize,textAlign = TextAlign.Center),
                                    modifier = Modifier.height(28.dp).background(surface).wrapContentHeight(align = Alignment.CenterVertically)
                                                        .fillMaxWidth().clip(RoundedCornerShape(6.dp) ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        processKeyboardDoneEvent(cBoilerTB)
                                        keyboardController?.hide() })
                                )
                                BasicTextField(
                                    value = cBoilerTB.userInput,
                                    onValueChange = { newText ->
                                        processTextFieldInput(newText, cBoilerTB) },
                                    cursorBrush = SolidColor(Unspecified),
                                    textStyle = TextStyle(color = textprimary, fontSize = tokens.ParamValue_fontsize,textAlign = TextAlign.Center),
                                    modifier = Modifier.height(28.dp).background(surface).wrapContentHeight(align = Alignment.CenterVertically)
                                        .fillMaxWidth().clip(RoundedCornerShape(6.dp) ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        processKeyboardDoneEvent(cBoilerTB)
                                        keyboardController?.hide() })
                                )
                            }
                            Column( // 4 COLUMN → 1f Units--------------------------
                                modifier = Modifier.weight(1f).padding(top = 2.dp),
                                horizontalAlignment = Alignment. CenterHorizontally
                            ) {
                                Text(text = "°C",
                                    style = TextStyle(fontSize = tokens.TableParam_fontsize, color = textsecondary), modifier = Modifier.height(30.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                Text(text = "°C",
                                    style = TextStyle(fontSize = tokens.TableParam_fontsize, color = textsecondary), modifier = Modifier.height(30.dp).wrapContentHeight(align = Alignment.CenterVertically))
                            }

                        }
                        //  BREW CONFIGURATION MENU
                        //  ----------------------------------------------------------------------------
                        //  TABLE PARAMETER TITLE   ----------------------------------------------------
                        Row(
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .size(width = 380.dp, height = 18.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {}
                        /*
                        Divider( //  TITLE DIVIDER   ---------------------------------------------------
                            color = textsecondary,
                            modifier = Modifier.padding(start = 10.dp).size(width = 380.dp, height = 1.dp)
                        )*/
                        //  Brew Table Content  --------------------------------------------------------
                        Row( //thiw row configure the size of the space allocated for the Bre table
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .size(width = 380.dp, height = 180.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly ,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column( // 1 COLUMN → Parameters------------------------
                                modifier = Modifier.weight(3f).padding(start = 15.dp, top = 2.dp)
                            ) {
                                Text(
                                    text = "Pre-Infusion",
                                    style = TextStyle(fontSize = tokens.TParameter_fontsize, color = textprimary),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                Text(
                                    text = "",
                                    style = TextStyle(fontSize = tokens.TParameter_fontsize, color = textprimary),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                Text(
                                    text = "Infusion",
                                    style = TextStyle(fontSize = tokens.TParameter_fontsize, color = textprimary),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                Text(
                                    text = "",
                                    style = TextStyle(fontSize = tokens.TParameter_fontsize, color = textprimary),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                Text(
                                    text = "Declining Pressure",
                                    style = TextStyle(fontSize = tokens.TParameter_fontsize, color = textprimary),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                Text(
                                    text = "",
                                    style = TextStyle(fontSize = tokens.TParameter_fontsize, color = textprimary),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                            }
                            Column( // 2 COLUMN → Input Range--------------------------
                                modifier = Modifier.weight(2f).padding(top = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "(" + cPreinfusionPWR_TB.minInValue.toString() + " - " + cPreinfusionPWR_TB.maxInValue.toString() + ")",
                                    style = TextStyle(fontSize = tokens.MinMaxParam_fontsize, color = textsecondary,fontStyle = FontStyle.Italic),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                Text(
                                    text = "(" + cPreinfusionTMR_TB.minInValue.toString() + " - " + cPreinfusionTMR_TB.maxInValue.toString() + ")",
                                    style = TextStyle(fontSize = tokens.MinMaxParam_fontsize, color = textsecondary,fontStyle = FontStyle.Italic),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                Text(
                                    text = "(" + cInfusionPWR_TB.minInValue.toString() + " - " + cInfusionPWR_TB.maxInValue.toString() + ")",
                                    style = TextStyle(fontSize = tokens.MinMaxParam_fontsize, color = textsecondary,fontStyle = FontStyle.Italic),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                Text(
                                    text = "(" + cInfusionTMR_TB.minInValue.toString() + " - " + cInfusionTMR_TB.maxInValue.toString() + ")",
                                    style = TextStyle(fontSize = tokens.MinMaxParam_fontsize, color = textsecondary,fontStyle = FontStyle.Italic),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                Text(
                                    text = "(" + cDeclinPrPWR_TB.minInValue.toString() + " - " + cDeclinPrPWR_TB.maxInValue.toString() + ")",
                                    style = TextStyle(fontSize = tokens.MinMaxParam_fontsize, color = textsecondary,fontStyle = FontStyle.Italic),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                Text(
                                    text = "(" + cDeclinPrTMR_TB.minInValue.toString() + " - " + cDeclinPrTMR_TB.maxInValue.toString() + ")",
                                    style = TextStyle(fontSize = tokens.MinMaxParam_fontsize, color = textsecondary,fontStyle = FontStyle.Italic),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                            }
                            Column( // 3 COLUMN → VALUE----------------------------
                                modifier = Modifier.weight(2f).padding(top = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                BasicTextField(// Pre Infusion POWER INPUT
                                    value = cPreinfusionPWR_TB.userInput,
                                    onValueChange = { newText ->
                                        processTextFieldInput(newText, cPreinfusionPWR_TB) },
                                    cursorBrush = SolidColor(accent),
                                    textStyle = TextStyle(color = textprimary, fontSize = tokens.ParamValue_fontsize,textAlign = TextAlign.Center),
                                    modifier = Modifier.height(28.dp).background(surface).wrapContentHeight(align = Alignment.CenterVertically),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        processKeyboardDoneEvent(cPreinfusionPWR_TB)
                                        keyboardController?.hide() })
                                )
                                BasicTextField( // Pre Infusion TIMER INPUT
                                    value = cPreinfusionTMR_TB.userInput,
                                    onValueChange = { newText ->
                                        processTextFieldInput(newText, cPreinfusionTMR_TB)
                                    },
                                    cursorBrush = SolidColor(accent),
                                    textStyle = TextStyle(color = textprimary, fontSize = tokens.ParamValue_fontsize,textAlign = TextAlign.Center),
                                    modifier = Modifier.height(28.dp).background(surface).wrapContentHeight(align = Alignment.CenterVertically),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        processKeyboardDoneEvent(cPreinfusionTMR_TB)
                                        keyboardController?.hide()
                                    })
                                )
                                BasicTextField( //Infusion POWER INPUT
                                    value = cInfusionPWR_TB.userInput,
                                    onValueChange = { newText ->
                                        processTextFieldInput(newText, cInfusionPWR_TB)
                                    },
                                    cursorBrush = SolidColor(accent),
                                    textStyle = TextStyle(color = textprimary, fontSize = tokens.ParamValue_fontsize,textAlign = TextAlign.Center),
                                    modifier = Modifier.height(28.dp).background(surface).wrapContentHeight(align = Alignment.CenterVertically),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        processKeyboardDoneEvent(cInfusionPWR_TB)
                                        keyboardController?.hide()
                                    })
                                )
                                BasicTextField( // Infusion TIMER INPUT
                                    value = cInfusionTMR_TB.userInput,
                                    onValueChange = { newText ->
                                        processTextFieldInput(newText, cInfusionTMR_TB)
                                    },
                                    cursorBrush = SolidColor(accent),
                                    textStyle = TextStyle(color = textprimary, fontSize = tokens.ParamValue_fontsize,textAlign = TextAlign.Center),
                                    modifier = Modifier.height(28.dp).background(surface).wrapContentHeight(align = Alignment.CenterVertically),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        processKeyboardDoneEvent(cInfusionTMR_TB)
                                        keyboardController?.hide()
                                    })
                                )
                                BasicTextField( //  Declining Pressure POWER INPUT
                                    value = cDeclinPrPWR_TB.userInput,
                                    onValueChange = { newText ->
                                        processTextFieldInput(newText, cDeclinPrPWR_TB)
                                    },
                                    cursorBrush = SolidColor(accent),
                                    textStyle = TextStyle(color = textprimary, fontSize = tokens.ParamValue_fontsize,textAlign = TextAlign.Center),
                                    modifier = Modifier.height(28.dp).background(surface).wrapContentHeight(align = Alignment.CenterVertically),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        processKeyboardDoneEvent(cDeclinPrPWR_TB)
                                        keyboardController?.hide()
                                    })
                                )
                                BasicTextField( // Declining Pressure TIME INPUT
                                    value = cDeclinPrTMR_TB.userInput,
                                    onValueChange = { newText ->
                                        processTextFieldInput(newText, cDeclinPrTMR_TB)
                                    },
                                    cursorBrush = SolidColor(accent),
                                    textStyle = TextStyle(color = textprimary, fontSize = tokens.ParamValue_fontsize,textAlign = TextAlign.Center),
                                    modifier = Modifier.height(28.dp).background(surface).wrapContentHeight(align = Alignment.CenterVertically),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        processKeyboardDoneEvent(cDeclinPrTMR_TB)
                                        keyboardController?.hide()
                                    })
                                )
                            }
                            Column(// 4 COLUMN → Units----------------------------
                                modifier = Modifier.weight(1f).padding(top = 2.dp, start = 12.dp),
                            ) {
                                Text(
                                    text = "%",
                                    style = TextStyle(fontSize = tokens.TableParam_fontsize, color = textprimary),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                Text(
                                    text = "sec",
                                    style = TextStyle(fontSize = tokens.TableParam_fontsize, color = textprimary),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                Text(
                                    text = "%",
                                    style = TextStyle(fontSize = tokens.TableParam_fontsize, color = textprimary),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                Text(
                                    text = "sec",
                                    style = TextStyle(fontSize = tokens.TableParam_fontsize, color = textprimary),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                Text(
                                    text = "%",
                                    style = TextStyle(fontSize = tokens.TableParam_fontsize, color = textprimary),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                Text(
                                    text = "sec",
                                    style = TextStyle(fontSize = tokens.TableParam_fontsize, color = textprimary),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                            }
                        }
                        //  ROW to allocate the following buttons:
                        //  To READ and WRITE --------------------------------------------
                        Row(
                            modifier = Modifier
                                .padding(start = 0.dp, top = 10.dp)
                                .size(width = 380.dp, height = 50.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column( // 1 COLUMN allocates →  1/3 space of the column --------------------------
                                modifier = Modifier
                                    .weight(0.4f)
                                    .padding(top = 0.dp, start = 5.dp, end = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {}

                            Column( // 1 COLUMN → Button To Read --------------------------
                                modifier = Modifier
                                    .weight(0.3f)
                                    .padding(top = 0.dp, start = 5.dp, end = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Button(
                                    onClick = {
                                        keyboardController?.hide()      // Hide the keyboard when the button is clicked
                                        /* Handle button click → READ Temperature and Brew Configuration */
                                        Toast.makeText(context, "Reading Machine configuration", Toast.LENGTH_SHORT).show()
                                        passTempValModeltoVar = true
                                        passRxvalfromBLEtoVar = true
                                        //viewModel.rxNewBoilerTargetTemp()
                                        //viewModel.rxReadBrewConfiguration()
                                        Log.d("ScreenBREWconfiguration", "Read configuration Button PRESSED")
                                        //Log.d("ScreenBREWconfiguration","Printing in screen: ${viewModel.brewConfigData.PreInfussionPwr}")
                                    },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = accent),
                                    modifier = Modifier
                                        .fillMaxWidth(1f)
                                        .height(40.dp)
                                        .wrapContentHeight()
                                ) {
                                    Text(
                                        text = "Read", style = TextStyle(fontSize = tokens.ButtonType2_Font, color = textprimary)
                                    )
                                }
                            }
                            Column( // 2 COLUMN → Button to Write --------------------------
                                modifier = Modifier
                                    .weight(0.3f)
                                    .padding(top = 0.dp, start = 5.dp, end = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Button(
                                    onClick = {},
                                    colors = ButtonDefaults.buttonColors(backgroundColor = accent),
                                    modifier = Modifier
                                        .padding(start = 0.dp, top = 0.dp)
                                        .fillMaxWidth(1f)
                                        .height(40.dp)
                                        .wrapContentHeight()
                                ) {
                                    Text(text = "Write",
                                        style = TextStyle(
                                            fontSize = tokens.ButtonType2_Font,
                                            color = textprimary
                                        ),
                                        modifier = Modifier.pointerInput(Unit) {
                                            detectTapGestures(
                                                onTap = {
                                                    Toast.makeText(context, "click", Toast.LENGTH_SHORT).show()
                                                },
                                                onDoubleTap = {
                                                    /* Handle button click → WRITE Brew Configuration */
                                                    keyboardController?.hide()      // Hide the keyboard when the button is clicked
                                                    var anyModified = false
                                                    // Handle temperature write
                                                    if (cBoilerTB.modified && cBoilerTB.userInput.text.isNotBlank()) {
                                                        newTargetTemperature = cBoilerTB.userInput.text.toFloat()
                                                        //cBoilerTB.clickedOnce = false
                                                        anyModified = true
                                                        Log.d("RemoteControlScreen", "SAVED: $newTargetTemperature C")
                                                        viewModel.boilerSetpointTemp = newTargetTemperature
                                                        viewModel.txNewBoilerTargetTemp()
                                                        //Toast.makeText(context, "Sending new temperature", Toast.LENGTH_LONG).show()
                                                    }

                                                    // Check which fields have been modified
                                                    if (cPreinfusionPWR_TB.modified && cPreinfusionPWR_TB.userInput.text.isNotBlank()) {
                                                        viewModel.txNewBrewConfigData.PreInfussionPwr = cPreinfusionPWR_TB.userInput.text.toFloat()
                                                        anyModified = true
                                                    }
                                                    if (cPreinfusionTMR_TB.modified && cPreinfusionTMR_TB.userInput.text.isNotBlank()) {
                                                        viewModel.txNewBrewConfigData.PreInfussionTmr = cPreinfusionTMR_TB.userInput.text.toFloat()
                                                        anyModified = true
                                                    }
                                                    if (cInfusionPWR_TB.modified && cInfusionPWR_TB.userInput.text.isNotBlank()) {
                                                        viewModel.txNewBrewConfigData.InfussionPwr = cInfusionPWR_TB.userInput.text.toFloat()
                                                        anyModified = true
                                                    }
                                                    if (cInfusionTMR_TB.modified && cInfusionTMR_TB.userInput.text.isNotBlank()) {
                                                        viewModel.txNewBrewConfigData.InfussionTmr = cInfusionTMR_TB.userInput.text.toFloat()
                                                        anyModified = true
                                                    }
                                                    if (cDeclinPrPWR_TB.modified && cDeclinPrPWR_TB.userInput.text.isNotBlank()) {
                                                        viewModel.txNewBrewConfigData.DecliningPwr = cDeclinPrPWR_TB.userInput.text.toFloat()
                                                        anyModified = true
                                                    }
                                                    if (cDeclinPrTMR_TB.modified && cDeclinPrTMR_TB.userInput.text.isNotBlank()) {
                                                        viewModel.txNewBrewConfigData.DecliningTmr = cDeclinPrTMR_TB.userInput.text.toFloat()
                                                        anyModified = true
                                                    }

                                                    if (anyModified) {
                                                        // Reset modified flags
                                                        cBoilerTB.modified = false
                                                        cPreinfusionPWR_TB.modified = false
                                                        cPreinfusionTMR_TB.modified = false
                                                        cInfusionPWR_TB.modified = false
                                                        cInfusionTMR_TB.modified = false
                                                        cDeclinPrPWR_TB.modified = false
                                                        cDeclinPrTMR_TB.modified = false

                                                        Log.d(
                                                            "RemoteControlScreen", "SAVED: \n" +
                                                                    "Target Temperature:${viewModel.boilerSetpointTemp}\n" +
                                                                    "PreinfusionPWR:${viewModel.txNewBrewConfigData.PreInfussionPwr}\n" +
                                                                    "PreinfusionTMR:${viewModel.txNewBrewConfigData.PreInfussionTmr}\n" +
                                                                    "InfusionPWR:${viewModel.txNewBrewConfigData.InfussionPwr}\n" +
                                                                    "InfusionTMR:${viewModel.txNewBrewConfigData.InfussionTmr}\n" +
                                                                    "DecliningPWR:${viewModel.txNewBrewConfigData.DecliningPwr}\n" +
                                                                    "DecliningTMR:${viewModel.txNewBrewConfigData.DecliningTmr}\n"
                                                        )
                                                        viewModel.txNewBrewConfiguration()
                                                        Toast.makeText(context, "Sending modified BREW configuration", Toast.LENGTH_LONG).show()
                                                    } else {
                                                        Toast.makeText(context, "No fields have been modified", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            )
                                        }

                                    )
                                }
                            }
                        }
                        //  ROW to allocate the following buttons:
                        //  BUTTON: To go CONTROLLER  page --------------------------------------------
                        Row(
                            modifier = Modifier
                                .padding(start = 0.dp, top = 0.dp)
                                .size(width = 380.dp, height = 50.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column( // 1 COLUMN → empty 1/3 space --------------------------
                                modifier = Modifier
                                    .weight(0.4f)
                                    .padding(top = 0.dp, start = 5.dp, end = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {}
                            Column( // 2 COLUMN → Button: CONTROLLER-PARAMETERS --------------------------
                                modifier = Modifier
                                    .weight(0.3f)
                                    .padding(top = 0.dp, start = 5.dp, end = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Button(
                                    onClick = {
                                        MoveToOtherScreen = true
                                        navController.navigate(Screen.BlespressoCtrlParamScreen.route) {
                                            popUpTo(Screen.BlespressoMainScreen.route) {
                                                inclusive = true
                                            }
                                        }
                                        //TODO("Enter Controller parameter window using navController")
                                    },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = accent),
                                    modifier = Modifier
                                        .padding(start = 0.dp, top = 0.dp)
                                        .fillMaxWidth(1f)
                                        .height(40.dp)
                                        .wrapContentHeight()
                                ) {
                                    Text(
                                        text = "Controller", style = TextStyle(fontSize = tokens.ButtonType2_Font, color = textprimary)
                                    )
                                }
                            }
                            Column( // 3 COLUMN → empty 1/3 space --------------------------
                                modifier = Modifier
                                    .weight(0.3f)
                                    .padding(top = 0.dp, start = 5.dp, end = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {}
                        }
                    }
                }
            }
            // video 1-> Time-Stamp: 1:25:10
        } else if (bleConnectionState == ConnectionState.Disconnected) {
            Button(onClick = {
                viewModel.initiazeConnection()
            }) {
                Text(
                    text = "Initialize again"
                )
            }
        }

    }

}

fun processTextFieldInput(newText: TextFieldValue, cTextfield: CTextBox) {
    if (newText.text.isNullOrBlank()) {
        cTextfield.userInput = TextFieldValue("0.0")
        cTextfield.modified = cTextfield.originalValue != "0.0"
    } else {
        if (!cTextfield.clickedOnce) {
            cTextfield.userInput = TextFieldValue() // Clear the text on the first click
            cTextfield.clickedOnce = true // Set the flag to true
        } else {
            //previously the code to control max & min values was here
            //code section moved to the next fnc
            if (newText.text.all { it.isDigit() || it == '.' }) {
                val numericValue = newText.text.toFloatOrNull()
                if (numericValue != null) {
                    cTextfield.userInput = newText
                    // Set modified flag if the value differs from original
                    cTextfield.modified = newText.text != cTextfield.originalValue
                } else {
                    cTextfield.userInput = TextFieldValue("0.0")
                    cTextfield.clickedOnce = false
                    cTextfield.modified = cTextfield.originalValue != "0.0"
                }
            }
        }
    }
}

fun processKeyboardDoneEvent(cTextfield: CTextBox)
{
    cTextfield.clickedOnce = false
    if(cTextfield.userInput.text.isNullOrBlank())
    {
        cTextfield.userInput = TextFieldValue("0.0")
        cTextfield.modified = cTextfield.originalValue != "0.0"
    }else{
        if (cTextfield.userInput.text.all { it.isDigit() || it == '.' }) {
            val numericValue = cTextfield.userInput.text.toFloatOrNull()
            if (numericValue != null &&  numericValue>cTextfield.maxInValue)
            {
                cTextfield.userInput = TextFieldValue(cTextfield.maxInValue.toString())
                cTextfield.modified = cTextfield.maxInValue.toString() != cTextfield.originalValue
            } else if(numericValue != null && numericValue < cTextfield.minInValue)
            {
                cTextfield.userInput = TextFieldValue(cTextfield.minInValue.toString())
                cTextfield.modified = cTextfield.minInValue.toString() != cTextfield.originalValue
            } else{
                cTextfield.modified = cTextfield.userInput.text != cTextfield.originalValue
            }
        }
    }
}



