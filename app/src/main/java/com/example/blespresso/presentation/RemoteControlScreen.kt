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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.blespresso.data.ConnectionState
import com.example.blespresso.presentation.permissions.PermissionUtils
import com.example.blespresso.presentation.permissions.SystemBroadcastReceiver
import com.example.blespresso.ui.theme.ButtonType2_Font
import com.example.blespresso.ui.theme.Cyan800
import com.example.blespresso.ui.theme.MinMaxParam_fontsize
import com.example.blespresso.ui.theme.ParamValue_fontsize
import com.example.blespresso.ui.theme.TParameter_fontsize
import com.example.blespresso.ui.theme.TableParam_fontsize
import com.example.blespresso.ui.theme.colorButtonBox
import com.example.blespresso.ui.theme.colorButtonCtrlText
import com.example.blespresso.ui.theme.colorButtonText
import com.example.blespresso.ui.theme.color_1_3_BoxBackground
import com.example.blespresso.ui.theme.color_2_3_BoxBackground
import com.example.blespresso.ui.theme.color_2_3_BoxTitleDivider
import com.example.blespresso.ui.theme.color_2_3_BoxTitleText
import com.example.blespresso.ui.theme.color_3_3_BoxInputBackground
import com.example.blespresso.ui.theme.color_3_3_BoxInputText
import com.example.blespresso.ui.theme.color_3_3_BoxText
import com.example.blespresso.ui.theme.color_3_3_BoxTitleDivider
import com.example.blespresso.ui.theme.color_3_3_BoxTitleText
import com.example.blespresso.ui.theme.darkGray
import com.example.blespresso.ui.theme.gray
import com.example.blespresso.ui.theme.white
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay

/*  USEFUL LINKS:
    - Double taps in buttons: https://www.kodeco.com/34465692-gestures-in-jetpack-compose-getting-started
    - Jetpack compose - padding: http://joebirch.co/android/exporing-jetpack-compose-padding-modifier/
    - Jetpack compose: https://www.jetpackcompose.net/compose-layout-row-and-column


    - ChatGPT:  https://chat.openai.com/c/d97c6491-c358-4d1b-8962-8f4605ab354d
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalComposeUiApi::class)
@Composable
fun RemoteControlScreen(
    navController: NavController,
    onBluetoothStateChanged:()->Unit,
    viewModel: RemoteControlViewModel = hiltViewModel() //// video 1-> Time-Stamp: 1:16:40
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
                    }*/
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
        cPreinfusionPWR_TB.clickedOnce = false
        cPreinfusionTMR_TB.userInput =
            TextFieldValue(viewModel.brewConfigData.PreInfussionTmr.toString())
        cPreinfusionTMR_TB.clickedOnce = false
        cInfusionPWR_TB.userInput =
            TextFieldValue(viewModel.brewConfigData.InfussionPwr.toString())
        cInfusionPWR_TB.clickedOnce = false
        cInfusionTMR_TB.userInput =
            TextFieldValue(viewModel.brewConfigData.InfussionTmr.toString())
        cInfusionTMR_TB.clickedOnce = false
        cDeclinPrPWR_TB.userInput =
            TextFieldValue(viewModel.brewConfigData.DecliningPwr.toString())
        cDeclinPrPWR_TB.clickedOnce = false
        cDeclinPrTMR_TB.userInput =
            TextFieldValue(viewModel.brewConfigData.DecliningTmr.toString())
        cDeclinPrTMR_TB.clickedOnce = false
        // Reset the flag
        passRxvalfromBLEtoVar = false
    }

        // video 1-> Time-Stamp: 1:20:00
        //this box will be shown when trying to connect to a BLE device
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(darkGray),
            contentAlignment = Alignment.Center
        ) {
            if (bleConnectionState == ConnectionState.CurrentlyInitializing ) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .background(Cyan800, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(220.dp),
                        color = white,
                        strokeWidth = 10.dp
                    )
                    if (viewModel.initialazingMessage != null) {
                        Text(
                            text = viewModel.initialazingMessage!!,
                            //text = " Scanning\n for\n Espresso Machine",
                            color = white,
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
                                .background(color_1_3_BoxBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            //Log.d("TemperatureScreen","Print Temp Value: ${viewModel.boilerWaterTemp}")
                            BoilerTemperatureProgressIndicator(
                                modifier = Modifier
                                    .size(280.dp)
                                    .background(color_1_3_BoxBackground),
                                boilerWaterTemp = viewModel.boilerWaterTemp,
                                boilerTargetTemp = viewModel.boilerSetpointTemp,
                                primaryColor = Cyan800,
                                secondaryColor = gray,
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
                            .background(color_2_3_BoxBackground)
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
                                    text = "Boiler Configuration",
                                    modifier = Modifier.padding(start = 10.dp),
                                    style = TextStyle(
                                        fontSize = 17.sp,
                                        color = color_2_3_BoxTitleText
                                    )
                                )
                            }
                            Divider( //  TITLE DIVIDER   ---------------------------------------------------
                                color = color_2_3_BoxTitleDivider,
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .size(width = 380.dp, height = 1.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .size(width = 380.dp, height = 40.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column( // 1 COLUMN → Target Temperature text------------------------
                                    modifier = Modifier.weight(4f).padding(start = 15.dp, top = 2.dp)
                                ) {
                                    Text(
                                        text = "Target Temperature:",
                                        style = TextStyle(fontSize = TParameter_fontsize, color = color_3_3_BoxTitleText), modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically)
                                    )
                                }
                                Column( // 2 COLUMN → Target Value--------------------------
                                    modifier = Modifier.weight(2f).padding(top = 2.dp),
                                    horizontalAlignment = Alignment. CenterHorizontally
                                ) {
                                    BasicTextField(
                                        value = cBoilerTB.userInput,
                                        onValueChange = { newText ->
                                            processTextFieldInput(newText, cBoilerTB) },
                                        cursorBrush = SolidColor(Unspecified),
                                        textStyle = TextStyle(color = color_3_3_BoxInputText, fontSize = ParamValue_fontsize,textAlign = TextAlign.Center),
                                        modifier = Modifier.height(28.dp).background(color_3_3_BoxInputBackground).wrapContentHeight(align = Alignment.CenterVertically),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = {
                                            processKeyboardDoneEvent(cBoilerTB)
                                            keyboardController?.hide() })
                                    )
                                }
                                Column( // 3 COLUMN → Units--------------------------
                                    modifier = Modifier.weight(1f).padding(top = 2.dp),
                                    horizontalAlignment = Alignment. CenterHorizontally
                                ) {
                                    Text(
                                        text = "°C",
                                        style = TextStyle(fontSize = TableParam_fontsize, color = color_3_3_BoxText), modifier = Modifier.height(30.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                }
                                Column( // 5 COLUMN → Space----------------------------
                                    modifier = Modifier.weight(1f).padding(top = 2.dp, start = 12.dp),
                                    //horizontalAlignment = Alignment. CenterHorizontally
                                ) {
                                    /*space*/
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .padding(start = 0.dp, top = 5.dp)
                                    .size(width = 380.dp, height = 50.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly ,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column( // 1 COLUMN allocates →  1/3 space of the column --------------------------
                                    modifier = Modifier
                                        .weight(0.4f)
                                        .padding(top = 0.dp, start = 5.dp, end = 5.dp),
                                    horizontalAlignment = Alignment. CenterHorizontally
                                ) {}
                                Column( // 2 COLUMN allocates →  1/3 space of the column --------------------------
                                    modifier = Modifier
                                        .weight(0.3f)
                                        .padding(top = 0.dp, start = 5.dp, end = 5.dp),
                                    horizontalAlignment = Alignment. CenterHorizontally
                                ) {
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Reading Set temperature", Toast.LENGTH_SHORT).show()
                                            viewModel.rxNewBoilerTargetTemp()
                                            passTempValModeltoVar = true
                                        },
                                        colors = ButtonDefaults.buttonColors(backgroundColor = colorButtonBox),
                                        modifier = Modifier
                                            .fillMaxWidth(1f)
                                            .height(40.dp)
                                            .wrapContentHeight()
                                    ) {
                                        Text(text = "Read",
                                            style = TextStyle(fontSize = ButtonType2_Font, color = colorButtonText),
                                        )
                                    }
                                }
                                Column( // 3 COLUMN allocates →  1/3 space of the column --------------------------
                                    modifier = Modifier
                                        .weight(0.3f)
                                        .padding(top = 0.dp, start = 5.dp, end = 5.dp),
                                    horizontalAlignment = Alignment. CenterHorizontally
                                ) {
                                    Button(
                                        onClick = {
                                            keyboardController?.hide()      // Hide the keyboard when the button is clicked
                                            if (!(cBoilerTB.userInput.text == "New" || cBoilerTB.userInput.text == "")) {
                                                newTargetTemperature =
                                                    cBoilerTB.userInput.text.toFloat()
                                                cBoilerTB.clickedOnce = false
                                                Log.d("RemoteControlScreen", "SAVED: $newTargetTemperature C")
                                                viewModel.boilerSetpointTemp = newTargetTemperature
                                                viewModel.txNewBoilerTargetTemp()
                                                Toast.makeText(context, "Sending new temperature", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Provide a new temperature", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(backgroundColor = colorButtonBox),
                                        modifier = Modifier
                                            .fillMaxWidth(1f)
                                            .height(40.dp)
                                            .wrapContentHeight()
                                    ) {
                                        Text(text = "Write",
                                            style = TextStyle(fontSize = ButtonType2_Font, color = colorButtonText),
                                        )
                                    }
                                }
                            }
                            //  BREW CONFIGURATION MENU
                            //  ----------------------------------------------------------------------------
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = "Brew Configuration",
                                    modifier = Modifier.padding(start = 10.dp, top = 2.dp),
                                    style = TextStyle(
                                        fontSize = 17.sp,
                                        color = color_2_3_BoxTitleText
                                    )
                                )
                            }
                            //  TABLE PARAMETER TITLE   ----------------------------------------------------
                            Row(
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .size(width = 380.dp, height = 18.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column( // 1 COLUMN → Parameters------------------------
                                    modifier = Modifier.weight(3f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) { Text(text = "") }
                                Column( // 2 COLUMN → Minimum--------------------------
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "Min.", style = TextStyle(fontSize = TableParam_fontsize, color = color_3_3_BoxTitleText)) }
                                Column( // 3 COLUMN → Input----------------------------
                                    modifier = Modifier.weight(2f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {Text(text = "Value",style = TextStyle(fontSize = TableParam_fontsize, color = color_3_3_BoxTitleText)) }
                                Column( // 4 COLUMN → Maximum--------------------------
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {Text(text = "Max.",style = TextStyle(fontSize = TableParam_fontsize, color = color_3_3_BoxTitleText)) }
                                Column( // 5 COLUMN → Units----------------------------
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) { Text(text = "Unit",style = TextStyle(fontSize = TableParam_fontsize, color = color_3_3_BoxTitleText)) }
                            }
                            Divider( //  TITLE DIVIDER   ---------------------------------------------------
                                color = color_3_3_BoxTitleDivider,
                                modifier = Modifier.padding(start = 10.dp).size(width = 380.dp, height = 1.dp)
                            )
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
                                        style = TextStyle(fontSize = TParameter_fontsize, color = color_3_3_BoxTitleText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                    Text(
                                        text = "",
                                        style = TextStyle(fontSize = TParameter_fontsize, color = color_3_3_BoxTitleText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                    Text(
                                        text = "Infusion",
                                        style = TextStyle(fontSize = TParameter_fontsize, color = color_3_3_BoxTitleText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                    Text(
                                        text = "",
                                        style = TextStyle(fontSize = TParameter_fontsize, color = color_3_3_BoxTitleText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                    Text(
                                        text = "Declining Pressure",
                                        style = TextStyle(fontSize = TParameter_fontsize, color = color_3_3_BoxTitleText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                    Text(
                                        text = "",
                                        style = TextStyle(fontSize = TParameter_fontsize, color = color_3_3_BoxTitleText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                }
                                Column( // 2 COLUMN → Minimum--------------------------
                                    modifier = Modifier.weight(1f).padding(top = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = cPreinfusionPWR_TB.minInValue.toString(),
                                        style = TextStyle(fontSize = MinMaxParam_fontsize, color = color_3_3_BoxText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                    Text(
                                        text = cPreinfusionTMR_TB.minInValue.toString(),
                                        style = TextStyle(fontSize = MinMaxParam_fontsize, color = color_3_3_BoxText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                    Text(
                                        text = cInfusionPWR_TB.minInValue.toString(),
                                        style = TextStyle(fontSize = MinMaxParam_fontsize, color = color_3_3_BoxText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                    Text(
                                        text = cInfusionTMR_TB.minInValue.toString(),
                                        style = TextStyle(fontSize = MinMaxParam_fontsize, color = color_3_3_BoxText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                    Text(
                                        text = cDeclinPrPWR_TB.minInValue.toString(),
                                        style = TextStyle(fontSize = MinMaxParam_fontsize, color = color_3_3_BoxText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                    Text(
                                        text = cDeclinPrTMR_TB.minInValue.toString(),
                                        style = TextStyle(fontSize = MinMaxParam_fontsize, color = color_3_3_BoxText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                }
                                Column( // 3 COLUMN → VALUE----------------------------
                                    modifier = Modifier.weight(2f).padding(top = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    BasicTextField(// Pre Infusion POWER INPUT
                                        value = cPreinfusionPWR_TB.userInput,
                                        onValueChange = { newText ->
                                            processTextFieldInput(newText, cPreinfusionPWR_TB) },
                                        cursorBrush = SolidColor(Cyan800),
                                        textStyle = TextStyle(color = color_3_3_BoxInputText, fontSize = ParamValue_fontsize,textAlign = TextAlign.Center),
                                        modifier = Modifier.height(28.dp).background(color_3_3_BoxInputBackground).wrapContentHeight(align = Alignment.CenterVertically),
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
                                        cursorBrush = SolidColor(Cyan800),
                                        textStyle = TextStyle(color = color_3_3_BoxInputText, fontSize = ParamValue_fontsize,textAlign = TextAlign.Center),
                                        modifier = Modifier.height(28.dp).background(color_3_3_BoxInputBackground).wrapContentHeight(align = Alignment.CenterVertically),
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
                                        cursorBrush = SolidColor(Cyan800),
                                        textStyle = TextStyle(color = color_3_3_BoxInputText, fontSize = ParamValue_fontsize,textAlign = TextAlign.Center),
                                        modifier = Modifier.height(28.dp).background(color_3_3_BoxInputBackground).wrapContentHeight(align = Alignment.CenterVertically),
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
                                        cursorBrush = SolidColor(Cyan800),
                                        textStyle = TextStyle(color = color_3_3_BoxInputText, fontSize = ParamValue_fontsize,textAlign = TextAlign.Center),
                                        modifier = Modifier.height(28.dp).background(color_3_3_BoxInputBackground).wrapContentHeight(align = Alignment.CenterVertically),
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
                                        cursorBrush = SolidColor(Cyan800),
                                        textStyle = TextStyle(color = color_3_3_BoxInputText, fontSize = ParamValue_fontsize,textAlign = TextAlign.Center),
                                        modifier = Modifier.height(28.dp).background(color_3_3_BoxInputBackground).wrapContentHeight(align = Alignment.CenterVertically),
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
                                        cursorBrush = SolidColor(Cyan800),
                                        textStyle = TextStyle(color = color_3_3_BoxInputText, fontSize = ParamValue_fontsize,textAlign = TextAlign.Center),
                                        modifier = Modifier.height(28.dp).background(color_3_3_BoxInputBackground).wrapContentHeight(align = Alignment.CenterVertically),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = {
                                            processKeyboardDoneEvent(cDeclinPrTMR_TB)
                                            keyboardController?.hide()
                                        })
                                    )
                                }
                                Column( // 4 COLUMN → Maximum--------------------------
                                    modifier = Modifier.weight(1f).padding(top = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = cPreinfusionPWR_TB.maxInValue.toString(),
                                        style = TextStyle(fontSize = MinMaxParam_fontsize, color = color_3_3_BoxText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                    Text(
                                        text = cPreinfusionTMR_TB.maxInValue.toString(),
                                        style = TextStyle(fontSize = MinMaxParam_fontsize, color = color_3_3_BoxText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                    Text(
                                        text = cInfusionPWR_TB.maxInValue.toString(),
                                        style = TextStyle(fontSize = MinMaxParam_fontsize, color = color_3_3_BoxText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                    Text(
                                        text = cInfusionTMR_TB.maxInValue.toString(),
                                        style = TextStyle(fontSize = MinMaxParam_fontsize, color = color_3_3_BoxText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                    Text(
                                        text = cDeclinPrPWR_TB.maxInValue.toString(),
                                        style = TextStyle(fontSize = MinMaxParam_fontsize, color = color_3_3_BoxText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                    Text(
                                        text = cDeclinPrTMR_TB.maxInValue.toString(),
                                        style = TextStyle(fontSize = MinMaxParam_fontsize, color = color_3_3_BoxText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                }
                                Column(
                                    // 5 COLUMN → Units----------------------------
                                    modifier = Modifier.weight(1f).padding(top = 2.dp, start = 12.dp),
                                ) {
                                    Text(
                                        text = "%",
                                        style = TextStyle(fontSize = TableParam_fontsize, color = color_3_3_BoxText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                    Text(
                                        text = "sec",
                                        style = TextStyle(fontSize = TableParam_fontsize, color = color_3_3_BoxText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                    Text(
                                        text = "%",
                                        style = TextStyle(fontSize = TableParam_fontsize, color = color_3_3_BoxText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                    Text(
                                        text = "sec",
                                        style = TextStyle(fontSize = TableParam_fontsize, color = color_3_3_BoxText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                    Text(
                                        text = "%",
                                        style = TextStyle(fontSize = TableParam_fontsize, color = color_3_3_BoxText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
                                    Text(
                                        text = "sec",
                                        style = TextStyle(fontSize = TableParam_fontsize, color = color_3_3_BoxText),modifier = Modifier.height(28.dp).wrapContentHeight(align = Alignment.CenterVertically))
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
                                            /* Handle button click → READ Brew Configuration */
                                            viewModel.rxReadBrewConfiguration()
                                            keyboardController?.hide()      // Hide the keyboard when the button is clicked
                                            Log.d("ScreenBREWconfiguration", "Read configuration Button PRESSED")
                                            Toast.makeText(context, "Reading Brew configuration\"", Toast.LENGTH_SHORT).show()
                                            //Log.d("ScreenBREWconfiguration","Printing in screen: ${viewModel.brewConfigData.PreInfussionPwr}")
                                            passRxvalfromBLEtoVar = true
                                        },
                                        colors = ButtonDefaults.buttonColors(backgroundColor = colorButtonBox),
                                        modifier = Modifier
                                            .fillMaxWidth(1f)
                                            .height(40.dp)
                                            .wrapContentHeight()
                                    ) {
                                        Text(
                                            text = "Read", style = TextStyle(fontSize = ButtonType2_Font, color = colorButtonText)
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
                                        colors = ButtonDefaults.buttonColors(backgroundColor = colorButtonBox),
                                        modifier = Modifier
                                            .padding(start = 0.dp, top = 0.dp)
                                            .fillMaxWidth(1f)
                                            .height(40.dp)
                                            .wrapContentHeight()
                                    ) {
                                        Text(text = "Write",
                                            style = TextStyle(
                                                fontSize = ButtonType2_Font,
                                                color = colorButtonText
                                            ),
                                            modifier = Modifier.pointerInput(Unit) {
                                                detectTapGestures(
                                                    onTap = {
                                                        Toast.makeText(context, "click", Toast.LENGTH_SHORT).show()
                                                    },
                                                    onDoubleTap = {
                                                        /* Handle button click → WRITE Brew Configuration */
                                                        keyboardController?.hide()      // Hide the keyboard when the button is clicked
                                                        if (!(cPreinfusionPWR_TB.userInput.text == "" && cPreinfusionTMR_TB.userInput.text == ""
                                                                    && cInfusionPWR_TB.userInput.text == "" && cInfusionTMR_TB.userInput.text == ""
                                                                    && cDeclinPrPWR_TB.userInput.text == "" && cDeclinPrTMR_TB.userInput.text == "")
                                                        ) {
                                                            viewModel.txNewBrewConfigData.PreInfussionPwr =
                                                                cPreinfusionPWR_TB.userInput.text.toFloat()
                                                            viewModel.txNewBrewConfigData.PreInfussionTmr =
                                                                cPreinfusionTMR_TB.userInput.text.toFloat()
                                                            viewModel.txNewBrewConfigData.InfussionPwr =
                                                                cInfusionPWR_TB.userInput.text.toFloat()
                                                            viewModel.txNewBrewConfigData.InfussionTmr =
                                                                cInfusionTMR_TB.userInput.text.toFloat()
                                                            viewModel.txNewBrewConfigData.DecliningPwr =
                                                                cDeclinPrPWR_TB.userInput.text.toFloat()
                                                            viewModel.txNewBrewConfigData.DecliningTmr =
                                                                cDeclinPrTMR_TB.userInput.text.toFloat()

                                                            cPreinfusionPWR_TB.clickedOnce = false
                                                            cPreinfusionTMR_TB.clickedOnce = false
                                                            cInfusionPWR_TB.clickedOnce = false
                                                            cInfusionTMR_TB.clickedOnce = false
                                                            cDeclinPrPWR_TB.clickedOnce = false
                                                            cDeclinPrTMR_TB.clickedOnce = false
                                                            Log.d(
                                                                "RemoteControlScreen", "SAVED: \n" +
                                                                        "PreinfusionPWR:${viewModel.txNewBrewConfigData.PreInfussionPwr}\n" +
                                                                        "PreinfusionTMR:${viewModel.txNewBrewConfigData.PreInfussionTmr}\n" +
                                                                        "InfusionPWR:${viewModel.txNewBrewConfigData.InfussionPwr}\n" +
                                                                        "InfusionTMR:${viewModel.txNewBrewConfigData.InfussionTmr}\n" +
                                                                        "DecliningPWR:${viewModel.txNewBrewConfigData.DecliningPwr}\n" +
                                                                        "DecliningTMR:${viewModel.txNewBrewConfigData.DecliningTmr}\n"
                                                            )
                                                            viewModel.txNewBrewConfiguration()
                                                            Toast.makeText(context, "Sending new BREW configuration", Toast.LENGTH_LONG).show()
                                                        } else {
                                                            //Toast.makeText(context,"Provide a new temperature",Toast.LENGTH_SHORT).show()
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
                                        colors = ButtonDefaults.buttonColors(backgroundColor = colorButtonBox),
                                        modifier = Modifier
                                            .padding(start = 0.dp, top = 0.dp)
                                            .fillMaxWidth(1f)
                                            .height(40.dp)
                                            .wrapContentHeight()
                                    ) {
                                        Text(
                                            text = "Controller", style = TextStyle(fontSize = ButtonType2_Font, color = colorButtonCtrlText)
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
                    //  THIRD THIRD OF SCREEN
                    //  CONTROLLER CONFIGURATION     -------------------------------------------------------
                    //  ------------------------------------------------------------------------------------
                    /*  Controller Parameter
                Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(color_3_3_BoxBackground)
                    .padding(top = 1.dp, bottom = 1.dp) // Add spacing between rows
            ) { // Content for the third row
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(5.dp),
                ) {
                    //  PID-CONTROLLER CONFIGURATION MENU
                    //  ----------------------------------------------------------------------------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Controller Configuration",
                            modifier = Modifier.padding(start = 10.dp, top = 2.dp),
                            style = TextStyle(fontSize = 17.sp, color = color_3_3_BoxTitleText)
                        )
                    }
                    //  TABLE PARAMETER TITLE   ----------------------------------------------------
                    Row(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(width = 380.dp, height = 18.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly ,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column( // 1 COLUMN → Parameters------------------------
                            modifier = Modifier.weight(3f),
                            horizontalAlignment = Alignment. CenterHorizontally
                        ) { Text(text = "Parameter",style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText))
                        }
                        Column( // 2 COLUMN → Minimum--------------------------
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment. CenterHorizontally
                        ) { Text(text = "Min.",style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText))
                        }
                        Column( // 3 COLUMN → Input----------------------------
                            modifier = Modifier.weight(2f),
                            horizontalAlignment = Alignment. CenterHorizontally
                        ) { Text(text = "Value",style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText))
                        }
                        Column( // 4 COLUMN → Maximum--------------------------
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment. CenterHorizontally
                        ) { Text(text = "Max.",style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText))
                        }
                        Column( // 5 COLUMN → Units----------------------------
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment. CenterHorizontally
                        ) { Text(text = "Unit",style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText))
                        }
                    }
                    Divider( //  TITLE DIVIDER   ---------------------------------------------------
                        color = color_3_3_BoxTitleDivider,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(width = 380.dp, height = 1.dp)
                    )
                    //  Brew Table Content  --------------------------------------------------------
                    Row(    //Table SIZE
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(width = 380.dp, height = 134.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly ,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column( // 1 COLUMN → Parameters------------------------
                            modifier = Modifier
                                .weight(3f)
                                .padding(start = 15.dp, top = 2.dp)
                        ) {
                            Text(text = "Proportional",style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText))
                            Text(text = "Integral",style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText))
                            Text(text = "Integral Limit",style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText))
                            Text(text = "Integral Windup",style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText))
                            Text(text = "Derivative",style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText))
                            Text(text = "LPF Freq. Cut",style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText))
                            Text(text = "System Gain",style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText))
                        }
                        Column( // 2 COLUMN → Minimum--------------------------
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 2.dp),
                            horizontalAlignment = Alignment. CenterHorizontally
                        ) {
                            Text(text = cPterm_TB.minInValue.toString(),style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText)) //Proportional
                            Text(text = cIterm_TB.minInValue.toString(),style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText)) //Integral
                            Text(text = cIlimit_TB.minInValue.toString(),style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText)) //Integral Limit
                            Text(text = cIwindup_TB.minInValue.toString(),style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText)) //Integral Windup
                            Text(text = cDterm_TB.minInValue.toString(),style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText)) //Derivative
                            Text(text = cLPFterm_TB.minInValue.toString(),style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText))  //LPF Freq. Cut
                            Text(text = cGainterm_TB.minInValue.toString(),style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText))  //System Gain
                        }
                        Column( // 3 COLUMN → Input----------------------------
                            modifier = Modifier
                                .weight(2f)
                                .padding(top = 2.dp),
                            horizontalAlignment = Alignment. CenterHorizontally
                        ) {
                            BasicTextField( // Proportional Term INPUT
                                value = cPterm_TB.userInput,
                                onValueChange = { newText ->
                                    processTextFieldInput(newText,cPterm_TB) },
                                cursorBrush = SolidColor(Cyan800),
                                textStyle = TextStyle(color = color_3_3_BoxInputText, fontSize = 14.sp,textAlign = TextAlign.Center),
                                modifier = Modifier.background(color_3_3_BoxInputBackground),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    processKeyboardDoneEvent(cPterm_TB)
                                    keyboardController?.hide() })
                            )
                            BasicTextField( // Integral Term INPUT
                                value = cIterm_TB.userInput,
                                onValueChange = { newText -> processTextFieldInput(newText,cIterm_TB) },
                                cursorBrush = SolidColor(Cyan800),
                                textStyle = TextStyle(color = color_3_3_BoxInputText, fontSize = 14.sp,textAlign = TextAlign.Center),
                                modifier = Modifier.background(color_3_3_BoxInputBackground),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    processKeyboardDoneEvent(cIterm_TB)
                                    keyboardController?.hide() })
                            )
                            BasicTextField( // Integral limit Term INPUT
                                value = cIlimit_TB.userInput,
                                onValueChange = { newText ->  processTextFieldInput(newText,cIlimit_TB) },
                                cursorBrush = SolidColor(Cyan800),
                                textStyle = TextStyle(color = color_3_3_BoxInputText, fontSize = 14.sp,textAlign = TextAlign.Center),
                                modifier = Modifier.background(color_3_3_BoxInputBackground),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    processKeyboardDoneEvent(cIlimit_TB)
                                    keyboardController?.hide() })
                            )
                            BasicTextField( // Integral Windup INPUT
                                value = cIwindup_TB.userInput,
                                onValueChange = { newText -> processTextFieldInput(newText,cIwindup_TB) },
                                cursorBrush = SolidColor(Cyan800),
                                textStyle = TextStyle(color = color_3_3_BoxInputText, fontSize = 14.sp,textAlign = TextAlign.Center),
                                modifier = Modifier.background(color_3_3_BoxInputBackground),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    processKeyboardDoneEvent(cIwindup_TB)
                                    keyboardController?.hide() })
                            )
                            BasicTextField( // Derivatve Term INPUT
                                value = cDterm_TB.userInput,
                                onValueChange = { newText -> processTextFieldInput(newText,cDterm_TB) },
                                cursorBrush = SolidColor(Cyan800),
                                textStyle = TextStyle(color = color_3_3_BoxInputText, fontSize = 14.sp,textAlign = TextAlign.Center),
                                modifier = Modifier.background(color_3_3_BoxInputBackground),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    processKeyboardDoneEvent(cDterm_TB)
                                    keyboardController?.hide() })
                            )
                            BasicTextField( // Low-pass Filter Frequency INPUT
                                value = cLPFterm_TB.userInput,
                                onValueChange = { newText -> processTextFieldInput(newText,cLPFterm_TB) },
                                cursorBrush = SolidColor(Cyan800),
                                textStyle = TextStyle(color = color_3_3_BoxInputText, fontSize = 14.sp,textAlign = TextAlign.Center),
                                modifier = Modifier.background(color_3_3_BoxInputBackground).onFocusChanged { focusState ->
                                    isFocused = focusState.isFocused
                                    if (isFocused) { // The TextField is focused
                                    } else {// The TextField lost focus
                                        if ( cLPFterm_TB.userInput.text.toDouble() < cLPFterm_TB.minInValue)
                                            cLPFterm_TB.userInput = TextFieldValue(cLPFterm_TB.minInValue.toString())
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    processKeyboardDoneEvent(cLPFterm_TB)
                                    keyboardController?.hide() })
                            )
                            BasicTextField( // System Gain Term INPUT
                                value = cGainterm_TB.userInput,
                                onValueChange = { newText -> processTextFieldInput(newText,cGainterm_TB) },
                                cursorBrush = SolidColor(Cyan800),
                                textStyle = TextStyle(color = color_3_3_BoxInputText, fontSize = 14.sp,textAlign = TextAlign.Center),
                                modifier = Modifier.background(color_3_3_BoxInputBackground),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    processKeyboardDoneEvent(cGainterm_TB)
                                    keyboardController?.hide() })
                            )
                        }
                        Column( // 4 COLUMN → Maximum--------------------------
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 2.dp),
                            horizontalAlignment = Alignment. CenterHorizontally
                        ) {
                            Text(text = cPterm_TB.maxInValue.toString(),style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText)) //Proportional
                            Text(text = cIterm_TB.maxInValue.toString(),style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText)) //Integral
                            Text(text = cIlimit_TB.maxInValue.toString(),style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText)) //Integral Limit
                            Text(text = cIwindup_TB.maxInValue.toString(),style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText)) //Integral Windup
                            Text(text = cDterm_TB.maxInValue.toString(),style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText)) //Derivative
                            Text(text = cLPFterm_TB.maxInValue.toString(),style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText))  //LPF Freq. Cut
                            Text(text = cGainterm_TB.maxInValue.toString(),style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText))  //System Gain
                        }
                        Column( // 5 COLUMN → Units----------------------------
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 2.dp, start = 12.dp),
                            //horizontalAlignment = Alignment. CenterHorizontally
                        ) {
                            Text(text = "",style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText)) //Proportional
                            Text(text = "",style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText)) //Integral
                            Text(text = "",style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText)) //Integral Limit
                            Text(text = "",style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText)) //Integral Windup
                            Text(text = "",style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText)) //Derivative
                            Text(text = "Hz",style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText))  //LPF Freq. Cut
                            Text(text = "",style = TextStyle(fontSize = 13.sp, color = color_3_3_BoxText))  //System Gain
                        }
                    }
                    //  Buttons To Read and save Value  --------------------------------------------
                    Row(
                        modifier = Modifier
                            .padding(start = 10.dp, top = 2.dp)
                            .size(width = 380.dp, height = 50.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly ,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column( // 1 COLUMN → Button To Read --------------------------
                            modifier = Modifier
                                .weight(2f)
                                .padding(top = 0.dp),
                            horizontalAlignment = Alignment. CenterHorizontally
                        ) {}
                        Column( // 1 COLUMN → Button To Read --------------------------
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 0.dp),
                            horizontalAlignment = Alignment. CenterHorizontally
                        ) {
                            Button(onClick = {
                                /* Handle button click → read PID Configuration */
                                viewModel.rxReadPIDConfiguration()
                                keyboardController?.hide()      // Hide the keyboard when the button is clicked
                                Toast.makeText(context,"Reading PID configuration\"",Toast.LENGTH_SHORT).show()
                                //Log.d("ScreenBREWconfiguration","Printing in screen: ${viewModel.brewConfigData.PreInfussionPwr}")
                                runBlocking{
                                    coroutineScope {  // this: CoroutineScope
                                        launch {
                                            delay(500L)
                                            Log.d("ScreenPIDconfiguration","Read PID configuration")
                                            cPterm_TB.userInput = TextFieldValue(viewModel.PIDConfigData.cfgPterm.toString())
                                            cPterm_TB.clickedOnce = false
                                            cIterm_TB.userInput = TextFieldValue(viewModel.PIDConfigData.cfgIterm.toString())
                                            cIterm_TB.clickedOnce = false
                                            cIlimit_TB.userInput = TextFieldValue(viewModel.PIDConfigData.cfgImaxterm.toString())
                                            cIlimit_TB.clickedOnce = false
                                            cIwindup_TB.userInput = TextFieldValue(viewModel.PIDConfigData.cfgWindup.toString())
                                            cIwindup_TB.clickedOnce = false
                                            cDterm_TB.userInput = TextFieldValue(viewModel.PIDConfigData.cfgDterm.toString())
                                            cDterm_TB.clickedOnce = false
                                            cLPFterm_TB.userInput = TextFieldValue(viewModel.PIDConfigData.cfgDlpfterm.toString())
                                            cLPFterm_TB.clickedOnce = false
                                            cGainterm_TB.userInput = TextFieldValue(viewModel.PIDConfigData.cfgGainterm.toString())
                                            cGainterm_TB.clickedOnce = false
                                        }
                                    }
                                }
                            },
                                colors = ButtonDefaults.buttonColors(backgroundColor = colorButtonBox),
                                modifier = Modifier
                                    .padding(start = 0.dp, top = 0.dp)
                                    .fillMaxWidth(1f)
                                    .height(35.dp)
                                    .wrapContentHeight()
                            ) { Text(text = "Read", style = TextStyle(fontSize = 13.sp, color = colorButtonText))
                            }
                        }
                        Column( // 2 COLUMN → Button to Write --------------------------
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 2.dp),
                            horizontalAlignment = Alignment. CenterHorizontally
                        ) {
                            Button(
                                onClick = {},
                                colors = ButtonDefaults.buttonColors(backgroundColor = colorButtonBox),
                                modifier = Modifier
                                    .padding(start = 10.dp, top = 0.dp)
                                    .fillMaxWidth(0.9f)
                                    .height(35.dp)
                                    .wrapContentHeight()
                            ) {
                                Text(text = "Write", style = TextStyle(fontSize = 13.sp, color = colorButtonText),
                                    modifier = Modifier.pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = {Toast.makeText(context,"click",Toast.LENGTH_SHORT).show()},
                                            onDoubleTap = {
                                                /* Handle button click → WRITE Brew Configuration */
                                                keyboardController?.hide()      // Hide the keyboard when the button is clicked
                                                if(!(cPterm_TB.userInput.text.isNullOrBlank() && cIterm_TB.userInput.text.isNullOrBlank()
                                                            && cIlimit_TB.userInput.text.isNullOrBlank()  && cIwindup_TB.userInput.text.isNullOrBlank()
                                                            && cDterm_TB.userInput.text.isNullOrBlank()  && cLPFterm_TB.userInput.text.isNullOrBlank()
                                                            && cGainterm_TB.userInput.text.isNullOrBlank() ))
                                                {
                                                    viewModel.txNewPIDConfigData.cfgPterm = cPterm_TB.userInput.text.toFloat()
                                                    viewModel.txNewPIDConfigData.cfgIterm = cIterm_TB.userInput.text.toFloat()
                                                    viewModel.txNewPIDConfigData.cfgImaxterm =  cIlimit_TB.userInput.text.toFloat()
                                                    viewModel.txNewPIDConfigData.cfgWindup = cIwindup_TB.userInput.text.toFloat()
                                                    viewModel.txNewPIDConfigData.cfgDterm = cDterm_TB.userInput.text.toFloat()
                                                    viewModel.txNewPIDConfigData.cfgDlpfterm = cLPFterm_TB.userInput.text.toFloat()
                                                    viewModel.txNewPIDConfigData.cfgGainterm = cGainterm_TB.userInput.text.toFloat()

                                                    cPterm_TB.clickedOnce = false
                                                    cIterm_TB.clickedOnce = false
                                                    cIlimit_TB.clickedOnce = false
                                                    cIwindup_TB.clickedOnce = false
                                                    cDterm_TB.clickedOnce = false
                                                    cLPFterm_TB.clickedOnce = false
                                                    cGainterm_TB.clickedOnce = false
                                                    Log.d("RemoteControlScreen", "SAVED: \n"+
                                                            "P Term:${viewModel.txNewPIDConfigData.cfgPterm}\n" +
                                                            "I Term:${viewModel.txNewPIDConfigData.cfgIterm}\n" +
                                                            "Imax Term:${viewModel.txNewPIDConfigData.cfgImaxterm}\n" +
                                                            "I windup:${viewModel.txNewPIDConfigData.cfgWindup}\n" +
                                                            "D Term:${viewModel.txNewPIDConfigData.cfgDterm}\n" +
                                                            "D LPF:${viewModel.txNewPIDConfigData.cfgDlpfterm}\n"+
                                                            "PID gain:${viewModel.txNewPIDConfigData.cfgGainterm}\n")
                                                    viewModel.txNewPIDConfiguration()
                                                    Toast.makeText(context,"Sending new PID configuration",Toast.LENGTH_LONG).show()
                                                }else{
                                                    //Toast.makeText(context,"Provide a new temperature",Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                    }
                                )
                            }

                        }
                    }

                }
            }
             */
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
                } else {
                    cTextfield.userInput = TextFieldValue("0.0")
                    cTextfield.clickedOnce = false
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
    }else{
        if (cTextfield.userInput.text.all { it.isDigit() || it == '.' }) {
            val numericValue = cTextfield.userInput.text.toFloatOrNull()
            if (numericValue != null &&  numericValue>cTextfield.maxInValue)
            {
                cTextfield.userInput = TextFieldValue(cTextfield.maxInValue.toString())
            } else if(numericValue != null && numericValue < cTextfield.minInValue)
            {
                cTextfield.userInput = TextFieldValue(cTextfield.minInValue.toString())
            } else{ }
        }
    }
}





