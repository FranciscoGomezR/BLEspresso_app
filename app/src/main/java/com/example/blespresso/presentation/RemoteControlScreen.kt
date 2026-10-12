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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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
import com.example.blespresso.ui.theme.surface_indic
import com.example.blespresso.ui.theme.textprimary
import com.example.blespresso.ui.theme.textprimary_indic
import com.example.blespresso.ui.theme.textsecondary
import com.example.blespresso.ui.theme.textsecondary_indic
import com.example.blespresso.ui.theme.on_accent
import com.example.blespresso.ui.theme.icon_off
import com.example.blespresso.ui.theme.surface
import com.example.blespresso.ui.theme.textbox_background
import com.example.blespresso.ui.theme.textbox_text
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

    // TLDR: Replaced 18 lines of per-field min/max assignments (e.g. cBrewTempPresetTB.maxInValue = 150.0f) that scattered
    //       domain knowledge across the composable. Now bounds are read from RemoteControlViewModel companion object
    //       constants so both screens share a single source of truth for all parameter ranges.
    val cBrewTempPresetTB = remember { CTextBox().apply { minInValue = RemoteControlViewModel.BREW_TEMP_PRESET_MIN; maxInValue = RemoteControlViewModel.BREW_TEMP_PRESET_MAX } }

    val cSteamTempPresetTB = remember { CTextBox().apply { minInValue = RemoteControlViewModel.STEAM_TEMP_PRESET_MIN; maxInValue = RemoteControlViewModel.STEAM_TEMP_PRESET_MAX } }

    var cPreinfusionPWR_TB = remember { CTextBox().apply { minInValue = RemoteControlViewModel.PRE_INF_PWR_MIN; maxInValue = RemoteControlViewModel.PRE_INF_PWR_MAX } }
    var cPreinfusionTMR_TB = remember { CTextBox().apply { minInValue = RemoteControlViewModel.PRE_INF_TMR_MIN; maxInValue = RemoteControlViewModel.PRE_INF_TMR_MAX } }
    val cInfusionPWR_TB = remember { CTextBox().apply { minInValue = RemoteControlViewModel.INF_PWR_MIN; maxInValue = RemoteControlViewModel.INF_PWR_MAX } }
    val cInfusionTMR_TB  = remember { CTextBox().apply { minInValue = RemoteControlViewModel.INF_TMR_MIN; maxInValue = RemoteControlViewModel.INF_TMR_MAX } }
    val cDeclinPrPWR_TB = remember { CTextBox().apply { minInValue = RemoteControlViewModel.DECL_PWR_MIN; maxInValue = RemoteControlViewModel.DECL_PWR_MAX } }
    val cDeclinPrTMR_TB = remember { CTextBox().apply { minInValue = RemoteControlViewModel.DECL_TMR_MIN; maxInValue = RemoteControlViewModel.DECL_TMR_MAX } }

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

    // TLDR: Replaced two separate LaunchedEffect blocks driven by boolean flags (passTempValModeltoVar delay(500L)
    //       and passRxvalfromBLEtoVar delay(1500L)) plus a LaunchedEffect(bleConnectionState) auto-load block and
    //       a DisposableEffect that reset the flag on dispose — 4 separate side-effects in the composable.
    //       Now a single collector reacts to the machineConfigLoaded SharedFlow emitted by the ViewModel when data
    //       is ready. CTextBox.loadValue() replaces the 4-property manual assignment per field.
    LaunchedEffect(Unit) {
        viewModel.machineConfigLoaded.collect {
            Log.d("ScreenBREWconfiguration", "PASS INFO TO TEXTBOX")
            cBrewTempPresetTB.loadValue(viewModel.brewTempPreset.toString())
            cSteamTempPresetTB.loadValue(viewModel.steamTempPreset.toString())
            cPreinfusionPWR_TB.loadValue(viewModel.brewConfigData.PreInfussionPwr.toString())
            cPreinfusionTMR_TB.loadValue(viewModel.brewConfigData.PreInfussionTmr.toString())
            cInfusionPWR_TB.loadValue(viewModel.brewConfigData.InfussionPwr.toString())
            cInfusionTMR_TB.loadValue(viewModel.brewConfigData.InfussionTmr.toString())
            cDeclinPrPWR_TB.loadValue(viewModel.brewConfigData.DecliningPwr.toString())
            cDeclinPrTMR_TB.loadValue(viewModel.brewConfigData.DecliningTmr.toString())
        }
    }

    // When the BLE device becomes Connected, request machine configuration automatically.
    // Mirrors the PID auto-load pattern in BlespressoCtrlParamScreen.
    LaunchedEffect(bleConnectionState) {
        if (bleConnectionState == ConnectionState.Connected) {
            viewModel.rxReadMachineConfiguration()
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
        } else {
            var isFocused by remember { mutableStateOf(false) }

            // Get the software keyboard controller
            val keyboardController = LocalSoftwareKeyboardController.current
            // TODO #10 busy lock (brewing / steaming): buttons disabled, textboxes read-only
            val isBusy = viewModel.isMachineBusy
            val busyButtonColors = ButtonDefaults.buttonColors(backgroundColor = accent, disabledBackgroundColor = surface)
            val busyButtonTextColor = if (isBusy) icon_off else on_accent
            val context = LocalContext.current
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                //  TOP BAR: 0x1401 mode + state (TODO #9, docs/UX/README.md §8.5) ---------------------
                StatusTopBar(status = viewModel.machineStatus)
                //  FIRST THIRD OF SCREEN
                //  BOILER TEMPERATURE INDICATOR + STATUS SLOTS -------------------------------------------
                //  ------------------------------------------------------------------------------------
                Row(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxWidth()
                ) {
                    // Content for the first row: dial + 16 dp + slot column, centred as one group
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(background_indic),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //Log.d("TemperatureScreen","Print Temp Value: ${viewModel.boilerWaterTemp}")
                        BoilerTemperatureProgressIndicator(
                            modifier = Modifier
                                .size(280.dp)
                                .background(background_indic),
                            boilerWaterTemp = viewModel.boilerWaterTemp,
                            boilerTempSetpoint = viewModel.boilerTempSetpoint,
                            primaryColor = accent_indic,
                            secondaryColor = surface_indic,
                            primaryTextColor = textprimary_indic,
                            secondaryTextColor = textsecondary_indic,
                            circleRadius = 280f,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        StatusSlotColumn(status = viewModel.machineStatus)
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
                                Text( text = "(${RemoteControlViewModel.BREW_TEMP_PRESET_MIN.toInt()} - ${RemoteControlViewModel.BREW_TEMP_PRESET_MAX.toInt()}°C)",
                                    style = TextStyle(fontSize = tokens.MinMaxParam_fontsize, color = textsecondary, fontStyle = FontStyle.Italic), modifier = Modifier.height(30.dp).wrapContentHeight(align = Alignment.CenterVertically)
                                )
                                Text( text = "(${RemoteControlViewModel.STEAM_TEMP_PRESET_MIN.toInt()} - ${RemoteControlViewModel.STEAM_TEMP_PRESET_MAX.toInt()}°C)",
                                    style = TextStyle(fontSize = tokens.MinMaxParam_fontsize, color = textsecondary, fontStyle = FontStyle.Italic), modifier = Modifier.height(30.dp).wrapContentHeight(align = Alignment.CenterVertically)
                                )
                            }
                            Column( // 3 COLUMN → 2f Target Value--------------------------
                                modifier = Modifier.weight(2f).padding(top = 2.dp),
                                horizontalAlignment = Alignment. CenterHorizontally
                            ) {
                                // TLDR: Replaced processTextFieldInput(newText, cXxx_TB) global function calls with
                                //       cXxx_TB.onValueChange(newText) and processKeyboardDoneEvent(cXxx_TB) with
                                //       cXxx_TB.onDone() across all BasicTextFields on this screen.
                                //       The logic was moved into CTextBox methods so it lives with the data it
                                //       operates on, and the global helper functions in RemoteControlScreen.kt were removed.
                                BasicTextField(
                                    readOnly = isBusy,
                                    value = cBrewTempPresetTB.userInput,
                                    onValueChange = { newText ->
                                        cBrewTempPresetTB.onValueChange(newText) },
                                    cursorBrush = SolidColor(Unspecified),
                                    textStyle = TextStyle(color = textbox_text, fontSize = tokens.ParamValue_fontsize,textAlign = TextAlign.Center),
                                    modifier = Modifier.height(28.dp).background(textbox_background).wrapContentHeight(align = Alignment.CenterVertically)
                                                        .fillMaxWidth().clip(RoundedCornerShape(6.dp) ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        cBrewTempPresetTB.onDone()
                                        keyboardController?.hide() })
                                )
                                BasicTextField(
                                    readOnly = isBusy,
                                    value = cSteamTempPresetTB.userInput,
                                    onValueChange = { newText ->
                                        cSteamTempPresetTB.onValueChange(newText) },
                                    cursorBrush = SolidColor(Unspecified),
                                    textStyle = TextStyle(color = textbox_text, fontSize = tokens.ParamValue_fontsize,textAlign = TextAlign.Center),
                                    modifier = Modifier.height(28.dp).background(textbox_background).wrapContentHeight(align = Alignment.CenterVertically)
                                        .fillMaxWidth().clip(RoundedCornerShape(6.dp) ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        cSteamTempPresetTB.onDone()
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
                                // TLDR: Same onValueChange/onDone pattern as the boiler text fields above.
                                //       Applies to all 6 brew-config fields in this column.
                                BasicTextField(// Pre Infusion POWER INPUT
                                    readOnly = isBusy,
                                    value = cPreinfusionPWR_TB.userInput,
                                    onValueChange = { newText ->
                                        cPreinfusionPWR_TB.onValueChange(newText) },
                                    cursorBrush = SolidColor(accent),
                                    textStyle = TextStyle(color = textbox_text, fontSize = tokens.ParamValue_fontsize,textAlign = TextAlign.Center),
                                    modifier = Modifier.height(28.dp).background(textbox_background).wrapContentHeight(align = Alignment.CenterVertically),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        cPreinfusionPWR_TB.onDone()
                                        keyboardController?.hide() })
                                )
                                BasicTextField( // Pre Infusion TIMER INPUT
                                    readOnly = isBusy,
                                    value = cPreinfusionTMR_TB.userInput,
                                    onValueChange = { newText ->
                                        cPreinfusionTMR_TB.onValueChange(newText)
                                    },
                                    cursorBrush = SolidColor(accent),
                                    textStyle = TextStyle(color = textbox_text, fontSize = tokens.ParamValue_fontsize,textAlign = TextAlign.Center),
                                    modifier = Modifier.height(28.dp).background(textbox_background).wrapContentHeight(align = Alignment.CenterVertically),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        cPreinfusionTMR_TB.onDone()
                                        keyboardController?.hide()
                                    })
                                )
                                BasicTextField( //Infusion POWER INPUT
                                    readOnly = isBusy,
                                    value = cInfusionPWR_TB.userInput,
                                    onValueChange = { newText ->
                                        cInfusionPWR_TB.onValueChange(newText)
                                    },
                                    cursorBrush = SolidColor(accent),
                                    textStyle = TextStyle(color = textbox_text, fontSize = tokens.ParamValue_fontsize,textAlign = TextAlign.Center),
                                    modifier = Modifier.height(28.dp).background(textbox_background).wrapContentHeight(align = Alignment.CenterVertically),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        cInfusionPWR_TB.onDone()
                                        keyboardController?.hide()
                                    })
                                )
                                BasicTextField( // Infusion TIMER INPUT
                                    readOnly = isBusy,
                                    value = cInfusionTMR_TB.userInput,
                                    onValueChange = { newText ->
                                        cInfusionTMR_TB.onValueChange(newText)
                                    },
                                    cursorBrush = SolidColor(accent),
                                    textStyle = TextStyle(color = textbox_text, fontSize = tokens.ParamValue_fontsize,textAlign = TextAlign.Center),
                                    modifier = Modifier.height(28.dp).background(textbox_background).wrapContentHeight(align = Alignment.CenterVertically),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        cInfusionTMR_TB.onDone()
                                        keyboardController?.hide()
                                    })
                                )
                                BasicTextField( //  Declining Pressure POWER INPUT
                                    readOnly = isBusy,
                                    value = cDeclinPrPWR_TB.userInput,
                                    onValueChange = { newText ->
                                        cDeclinPrPWR_TB.onValueChange(newText)
                                    },
                                    cursorBrush = SolidColor(accent),
                                    textStyle = TextStyle(color = textbox_text, fontSize = tokens.ParamValue_fontsize,textAlign = TextAlign.Center),
                                    modifier = Modifier.height(28.dp).background(textbox_background).wrapContentHeight(align = Alignment.CenterVertically),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        cDeclinPrPWR_TB.onDone()
                                        keyboardController?.hide()
                                    })
                                )
                                BasicTextField( // Declining Pressure TIME INPUT
                                    readOnly = isBusy,
                                    value = cDeclinPrTMR_TB.userInput,
                                    onValueChange = { newText ->
                                        cDeclinPrTMR_TB.onValueChange(newText)
                                    },
                                    cursorBrush = SolidColor(accent),
                                    textStyle = TextStyle(color = textbox_text, fontSize = tokens.ParamValue_fontsize,textAlign = TextAlign.Center),
                                    modifier = Modifier.height(28.dp).background(textbox_background).wrapContentHeight(align = Alignment.CenterVertically),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        cDeclinPrTMR_TB.onDone()
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
                        //  ROW to allocate the following buttons (TODO #9: one row, directly under
                        //  the brew table): CONTROLLER (left), READ and WRITE (right) ----------------
                        Text( // TODO #10 busy lock message (space reserved, no layout jump)
                            text = if (isBusy) "Machine busy" else "",
                            style = TextStyle(fontSize = 13.sp, color = textprimary, textAlign = TextAlign.Center),
                            modifier = Modifier.padding(top = 8.dp).size(width = 380.dp, height = 18.dp)
                        )
                        Row(
                            modifier = Modifier
                                .padding(start = 0.dp, top = 14.dp, bottom = 14.dp)
                                .size(width = 380.dp, height = 50.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column( // 1 COLUMN → Button: CONTROLLER-PARAMETERS --------------------------
                                modifier = Modifier
                                    .weight(0.4f)
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
                                    },
                                    enabled = !isBusy,
                                    colors = busyButtonColors,
                                    modifier = Modifier
                                        .fillMaxWidth(1f)
                                        .height(40.dp)
                                        .wrapContentHeight()
                                ) {
                                    Text(
                                        text = "Controller", style = TextStyle(fontSize = tokens.ButtonType2_Font, color = busyButtonTextColor)
                                    )
                                }
                            }

                            Column( // 1 COLUMN → Button To Read --------------------------
                                modifier = Modifier
                                    .weight(0.3f)
                                    .padding(top = 0.dp, start = 5.dp, end = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Button(
                                    // TLDR: Removed passTempValModeltoVar = true and passRxvalfromBLEtoVar = true assignments.
                                    //       Previously those flags triggered two separate LaunchedEffects to delay and copy
                                    //       BLE data into text fields. Now rxReadMachineConfiguration() handles the delay
                                    //       and emits machineConfigLoaded when data is ready.
                                    onClick = {
                                        keyboardController?.hide()      // Hide the keyboard when the button is clicked
                                        /* Handle button click → READ all 8 machine UUIDs via single GATT-safe queue */
                                        Toast.makeText(context, "Reading Machine configuration", Toast.LENGTH_SHORT).show()
                                        viewModel.rxReadMachineConfiguration()
                                        Log.d("ScreenBREWconfiguration", "Read configuration Button PRESSED")
                                    },
                                    enabled = !isBusy,
                                    colors = busyButtonColors,
                                    modifier = Modifier
                                        .fillMaxWidth(1f)
                                        .height(40.dp)
                                        .wrapContentHeight()
                                ) {
                                    Text(
                                        text = "Read", style = TextStyle(fontSize = tokens.ButtonType2_Font, color = busyButtonTextColor)
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
                                    enabled = !isBusy,
                                    colors = busyButtonColors,
                                    modifier = Modifier
                                        .padding(start = 0.dp, top = 0.dp)
                                        .fillMaxWidth(1f)
                                        .height(40.dp)
                                        .wrapContentHeight()
                                ) {
                                    Text(text = "Write",
                                        style = TextStyle(
                                            fontSize = tokens.ButtonType2_Font,
                                            color = busyButtonTextColor
                                        ),
                                        modifier = Modifier.pointerInput(isBusy) {
                                            detectTapGestures(
                                                onTap = onTap@{
                                                if (isBusy) return@onTap
                                                    Toast.makeText(context, "click", Toast.LENGTH_SHORT).show()
                                                },
                                                onDoubleTap = onDoubleTap@{
                                                if (isBusy) return@onDoubleTap
                                                    // TLDR: Replaced a 50-line block that checked modified && isNotBlank()
                                                    //       for each of the 8 machine-config fields, called the matching
                                                    //       txWrite*() function directly, and reset the flag inline.
                                                    //       Moved all that orchestration to ViewModel.writeMachineConfig().
                                                    //       Unmodified fields are passed as null (skipped); the composable
                                                    //       only clears modified flags and shows the toast after the call.
                                                    /* Handle button click → WRITE Brew & Temperature Configuration */
                                                    keyboardController?.hide()
                                                    val anyModified = viewModel.writeMachineConfig(
                                                        brewTempPreset  = if (cBrewTempPresetTB.modified    && cBrewTempPresetTB.userInput.text.isNotBlank())         cBrewTempPresetTB.userInput.text.toFloat()         else null,
                                                        steamTempPreset = if (cSteamTempPresetTB.modified       && cSteamTempPresetTB.userInput.text.isNotBlank())       cSteamTempPresetTB.userInput.text.toFloat()       else null,
                                                        preInfPwr  = if (cPreinfusionPWR_TB.modified  && cPreinfusionPWR_TB.userInput.text.isNotBlank())  cPreinfusionPWR_TB.userInput.text.toFloat()  else null,
                                                        preInfTmr  = if (cPreinfusionTMR_TB.modified  && cPreinfusionTMR_TB.userInput.text.isNotBlank())  cPreinfusionTMR_TB.userInput.text.toFloat()  else null,
                                                        infPwr     = if (cInfusionPWR_TB.modified     && cInfusionPWR_TB.userInput.text.isNotBlank())     cInfusionPWR_TB.userInput.text.toFloat()     else null,
                                                        infTmr     = if (cInfusionTMR_TB.modified     && cInfusionTMR_TB.userInput.text.isNotBlank())     cInfusionTMR_TB.userInput.text.toFloat()     else null,
                                                        declPwr    = if (cDeclinPrPWR_TB.modified     && cDeclinPrPWR_TB.userInput.text.isNotBlank())     cDeclinPrPWR_TB.userInput.text.toFloat()     else null,
                                                        declTmr    = if (cDeclinPrTMR_TB.modified     && cDeclinPrTMR_TB.userInput.text.isNotBlank())     cDeclinPrTMR_TB.userInput.text.toFloat()     else null,
                                                    )
                                                    if (anyModified) {
                                                        listOf(cBrewTempPresetTB, cSteamTempPresetTB, cPreinfusionPWR_TB, cPreinfusionTMR_TB,
                                                               cInfusionPWR_TB, cInfusionTMR_TB, cDeclinPrPWR_TB, cDeclinPrTMR_TB
                                                        ).forEach { it.modified = false }
                                                        Toast.makeText(context, "Sending modified configuration", Toast.LENGTH_LONG).show()
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
                    }
                }
            }
            // video 1-> Time-Stamp: 1:25:10
        }
    }

}

