package com.example.blespresso.presentation

import android.bluetooth.BluetoothAdapter
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.blespresso.data.ConnectionState
import com.example.blespresso.presentation.permissions.SystemBroadcastReceiver
import com.example.blespresso.ui.theme.ButtonType1_Font
import com.example.blespresso.ui.theme.ButtonType2_Font
import com.example.blespresso.ui.theme.MinMaxParam_fontsize
import com.example.blespresso.ui.theme.ParamValue_fontsize
import com.example.blespresso.ui.theme.TParameter_fontsize
import com.example.blespresso.ui.theme.TableParam_fontsize
import com.example.blespresso.ui.theme.TableTitle_fontsize
import com.example.blespresso.ui.theme.accent
import com.example.blespresso.ui.theme.background
import com.example.blespresso.ui.theme.surface
import com.example.blespresso.ui.theme.textprimary
import com.example.blespresso.ui.theme.textsecondary
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalComposeUiApi::class, ExperimentalPermissionsApi::class)
@Composable
fun BlespressoCtrlParamScreen(
    navController: NavController,
    onBluetoothStateChanged:()->Unit,
    viewModel: RemoteControlViewModel = hiltViewModel()
) {
    //screen content
    // TLDR: Replaced 14 lines of per-field min/max assignments (e.g. cPterm_TB.maxInValue = 100.0f) with a single
    //       apply{} block referencing companion object constants. Bounds are now defined once in RemoteControlViewModel;
    //       changing a range only requires editing one constant instead of hunting across both screen files.
    val cPterm_TB   = remember { CTextBox().apply { minInValue = RemoteControlViewModel.PID_P_MIN;      maxInValue = RemoteControlViewModel.PID_P_MAX } }
    val cIterm_TB   = remember { CTextBox().apply { minInValue = RemoteControlViewModel.PID_I_MIN;      maxInValue = RemoteControlViewModel.PID_I_MAX } }
    val cIlimit_TB  = remember { CTextBox().apply { minInValue = RemoteControlViewModel.PID_ILIMIT_MIN; maxInValue = RemoteControlViewModel.PID_ILIMIT_MAX } }
    val cDterm_TB   = remember { CTextBox().apply { minInValue = RemoteControlViewModel.PID_D_MIN;      maxInValue = RemoteControlViewModel.PID_D_MAX } }
    val cPboost_TB  = remember { CTextBox().apply { minInValue = RemoteControlViewModel.PID_PBOOST_MIN; maxInValue = RemoteControlViewModel.PID_PBOOST_MAX } }
    val cIboost_TB  = remember { CTextBox().apply { minInValue = RemoteControlViewModel.PID_IBOOST_MIN; maxInValue = RemoteControlViewModel.PID_IBOOST_MAX } }

    // Get the software keyboard controller
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    // LaunchedEffect that runs only once when this composable is first composed
    LaunchedEffect(Unit) {
        // Perform an action, such as initializing data or calling a ViewModel function
        Log.d("ScreenNavigation", "Trigger a BLE connection")
        viewModel.initiazeConnection()  // Or any other operation you want to run once
    }
    //this function will run if there is a change from the phone's control panel
    SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_STATE_CHANGED)
    { bluetoothState ->
        //if action is null then we can return-> return@SystemBroadcastReceiver
        val action = bluetoothState?.action ?: return@SystemBroadcastReceiver
        Log.d("ScreenController", "change from the phone's control panel ")
        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            //Check for bluetooth connection
            onBluetoothStateChanged()
        }
    }

    // When the BLE device becomes Connected (state delivered by subscribeToChanges when this screen
    // initialises its own ViewModel), request the PID configuration automatically.
    LaunchedEffect(viewModel.connectionState) {
        if (viewModel.connectionState == ConnectionState.Connected) {
            viewModel.rxReadPIDConfiguration()
        }
    }

    // TLDR: Replaced a boolean flag (passRxvalfromBLEtoVar) + LaunchedEffect(flag) { delay(1500L); copy fields... }
    //       that forced the composable to manually copy every pIDConfigData field into text boxes after an artificial wait.
    //       Now collects the pidConfigLoaded SharedFlow emitted by the ViewModel once BLE data is ready;
    //       CTextBox.loadValue() handles the 4-property reset in one call per field.
    LaunchedEffect(Unit) {
        viewModel.pidConfigLoaded.collect {
            Log.d("ScreenPIDconfiguration", "Passing PID configurations from BLE to Screen FieldText")
            cPterm_TB.loadValue(viewModel.pIDConfigData.cfgPterm.toString())
            cIterm_TB.loadValue(viewModel.pIDConfigData.cfgIterm.toString())
            cIlimit_TB.loadValue(viewModel.pIDConfigData.cfgImaxterm.toString())
            cDterm_TB.loadValue(viewModel.pIDConfigData.cfgDterm.toString())
            cPboost_TB.loadValue(viewModel.pIDConfigData.cfgPboost.toString())
            cIboost_TB.loadValue(viewModel.pIDConfigData.cfgIboost.toString())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)

    ) {
        Row(
            modifier = Modifier
                .weight(1f)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(background)
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
                            modifier = Modifier.padding(start = 10.dp, top = 2.dp)
                                .size(width = 380.dp, height = 30.dp),
                            style = TextStyle(
                                fontSize = TableTitle_fontsize,
                                color = textprimary
                            )
                        )
                    }
                    //  TABLE PARAMETER TITLE   ----------------------------------------------------
                    Row(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(width = 380.dp, height = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column( // 1 COLUMN → Parameters------------------------
                            modifier = Modifier.weight(3f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Parameter",
                                style = TextStyle(
                                    fontSize = TableParam_fontsize,
                                    color = textprimary
                                )
                            )
                        }
                        Column( // 2 COLUMN → Minimum--------------------------
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Min.",
                                style = TextStyle(
                                    fontSize = TableParam_fontsize,
                                    color = textprimary
                                )
                            )
                        }
                        Column( // 3 COLUMN → Input----------------------------
                            modifier = Modifier.weight(2f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Value",
                                style = TextStyle(
                                    fontSize = TableParam_fontsize,
                                    color = textprimary
                                )
                            )
                        }
                        Column( // 4 COLUMN → Maximum--------------------------
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Max.",
                                style = TextStyle(
                                    fontSize = TableParam_fontsize,
                                    color = textprimary
                                )
                            )
                        }
                        Column( // 5 COLUMN → Units----------------------------
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Unit",
                                style = TextStyle(
                                    fontSize = TableParam_fontsize,
                                    color = textprimary
                                )
                            )
                        }
                    }
                    Divider( //  TITLE DIVIDER   ---------------------------------------------------
                        color = textprimary,
                        modifier = Modifier.padding(start = 10.dp)
                            .size(width = 380.dp, height = 1.dp)
                    )
                    //  Brew Table Content  --------------------------------------------------------
                    Row(
                        //Table SIZE
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(width = 380.dp, height = 220.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column( // 1 COLUMN → Parameters------------------------
                            modifier = Modifier.weight(3f)
                                .padding(start = 15.dp, top = 2.dp)
                        ) {
                            Text(
                                text = "Proportional",
                                style = TextStyle(
                                    fontSize = TParameter_fontsize,
                                    color = textprimary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )
                            Text(
                                text = "Integral",
                                style = TextStyle(
                                    fontSize = TParameter_fontsize,
                                    color = textprimary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )
                            Text(
                                text = "Integral Limit",
                                style = TextStyle(
                                    fontSize = TParameter_fontsize,
                                    color = textprimary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )
                            Text(
                                text = "Derivative",
                                style = TextStyle(
                                    fontSize = TParameter_fontsize,
                                    color = textprimary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )
                            Text(
                                text = "P-Gain Boost",
                                style = TextStyle(
                                    fontSize = TParameter_fontsize,
                                    color = textprimary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )
                            Text(
                                text = "I-Gain Boost",
                                style = TextStyle(
                                    fontSize = TParameter_fontsize,
                                    color = textprimary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )
                        }
                        Column( // 2 COLUMN → Minimum--------------------------
                            modifier = Modifier.weight(1f).padding(top = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = cPterm_TB.minInValue.toString(),
                                style = TextStyle(
                                    fontSize = MinMaxParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            ) //Proportional
                            Text(
                                text = cIterm_TB.minInValue.toString(),
                                style = TextStyle(
                                    fontSize = MinMaxParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            ) //Integral
                            Text(
                                text = cIlimit_TB.minInValue.toString(),
                                style = TextStyle(
                                    fontSize = MinMaxParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            ) //Integral Limit
                            Text(
                                text = cDterm_TB.minInValue.toString(),
                                style = TextStyle(
                                    fontSize = MinMaxParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            ) //Derivative
                            Text(
                                text = cPboost_TB.minInValue.toString(),
                                style = TextStyle(
                                    fontSize = MinMaxParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )  //P-Gain Boost
                            Text(
                                text = cIboost_TB.minInValue.toString(),
                                style = TextStyle(
                                    fontSize = MinMaxParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )  //I-Gain Boost
                        }
                        Column( // 3 COLUMN → Input----------------------------
                            modifier = Modifier.weight(2f).padding(top = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // TLDR: Replaced processTextFieldInput(newText, cXxx_TB) global function calls with
                            //       cXxx_TB.onValueChange(newText) and processKeyboardDoneEvent(cXxx_TB) with
                            //       cXxx_TB.onDone() across all 6 BasicTextFields in this column.
                            //       The logic was moved into CTextBox methods so it lives with the data it operates on.
                            //       The onFocusChanged LPF clamping was also removed: onDone() already covers that case.
                            BasicTextField( // Proportional Term INPUT
                                value = cPterm_TB.userInput,
                                onValueChange = { newText ->
                                    cPterm_TB.onValueChange(newText)
                                },
                                cursorBrush = SolidColor(textprimary),
                                textStyle = TextStyle(
                                    color = textprimary,
                                    fontSize = ParamValue_fontsize,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.height(30.dp)
                                    .background(surface)
                                    .wrapContentHeight(align = Alignment.CenterVertically),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    cPterm_TB.onDone()
                                    keyboardController?.hide()
                                })
                            )
                            BasicTextField( // Integral Term INPUT
                                value = cIterm_TB.userInput,
                                onValueChange = { newText ->
                                    cIterm_TB.onValueChange(newText)
                                },
                                cursorBrush = SolidColor(textprimary),
                                textStyle = TextStyle(
                                    color = textprimary,
                                    fontSize = ParamValue_fontsize,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.height(30.dp)
                                    .background(surface)
                                    .wrapContentHeight(align = Alignment.CenterVertically),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    cIterm_TB.onDone()
                                    keyboardController?.hide()
                                })
                            )
                            BasicTextField( // Integral limit Term INPUT
                                value = cIlimit_TB.userInput,
                                onValueChange = { newText ->
                                    cIlimit_TB.onValueChange(newText)
                                },
                                cursorBrush = SolidColor(textprimary),
                                textStyle = TextStyle(
                                    color = textprimary,
                                    fontSize = ParamValue_fontsize,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.height(30.dp)
                                    .background(surface)
                                    .wrapContentHeight(align = Alignment.CenterVertically),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    cIlimit_TB.onDone()
                                    keyboardController?.hide()
                                })
                            )
                            BasicTextField( // Derivatve Term INPUT
                                value = cDterm_TB.userInput,
                                onValueChange = { newText ->
                                    cDterm_TB.onValueChange(newText)
                                },
                                cursorBrush = SolidColor(textprimary),
                                textStyle = TextStyle(
                                    color = textprimary,
                                    fontSize = ParamValue_fontsize,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.height(30.dp)
                                    .background(surface)
                                    .wrapContentHeight(align = Alignment.CenterVertically),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    cDterm_TB.onDone()
                                    keyboardController?.hide()
                                })
                            )
                            BasicTextField( // P-Gain Boost INPUT
                                value = cPboost_TB.userInput,
                                onValueChange = { newText ->
                                    cPboost_TB.onValueChange(newText)
                                },
                                cursorBrush = SolidColor(textprimary),
                                textStyle = TextStyle(
                                    color = textprimary,
                                    fontSize = ParamValue_fontsize,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.height(30.dp)
                                    .background(surface)
                                    .wrapContentHeight(align = Alignment.CenterVertically),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    cPboost_TB.onDone()
                                    keyboardController?.hide()
                                })
                            )
                            BasicTextField( // I-Gain Boost INPUT
                                value = cIboost_TB.userInput,
                                onValueChange = { newText ->
                                    cIboost_TB.onValueChange(newText)
                                },
                                cursorBrush = SolidColor(textprimary),
                                textStyle = TextStyle(
                                    color = textprimary,
                                    fontSize = ParamValue_fontsize,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.height(30.dp)
                                    .background(surface)
                                    .wrapContentHeight(align = Alignment.CenterVertically),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    cIboost_TB.onDone()
                                    keyboardController?.hide()
                                })
                            )
                        }
                        Column( // 4 COLUMN → Maximum--------------------------
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = cPterm_TB.maxInValue.toString(),
                                style = TextStyle(
                                    fontSize = MinMaxParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            ) //Proportional
                            Text(
                                text = cIterm_TB.maxInValue.toString(),
                                style = TextStyle(
                                    fontSize = MinMaxParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            ) //Integral
                            Text(
                                text = cIlimit_TB.maxInValue.toString(),
                                style = TextStyle(
                                    fontSize = MinMaxParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            ) //Integral Limit
                            Text(
                                text = cDterm_TB.maxInValue.toString(),
                                style = TextStyle(
                                    fontSize = MinMaxParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            ) //Derivative
                            Text(
                                text = cPboost_TB.maxInValue.toString(),
                                style = TextStyle(
                                    fontSize = MinMaxParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )  //P-Gain Boost
                            Text(
                                text = cIboost_TB.maxInValue.toString(),
                                style = TextStyle(
                                    fontSize = MinMaxParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )  //I-Gain Boost
                        }
                        Column(
                            // 5 COLUMN → Units----------------------------
                            modifier = Modifier.weight(1f)
                                .padding(top = 2.dp, start = 12.dp),
                            //horizontalAlignment = Alignment. CenterHorizontally
                        ) {
                            Text(
                                text = "",
                                style = TextStyle(
                                    fontSize = TableParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            ) //Proportional
                            Text(
                                text = "",
                                style = TextStyle(
                                    fontSize = TableParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            ) //Integral
                            Text(
                                text = "",
                                style = TextStyle(
                                    fontSize = TableParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            ) //Integral Limit
                            Text(
                                text = "",
                                style = TextStyle(
                                    fontSize = TableParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            ) //Derivative
                            Text(
                                text = "",
                                style = TextStyle(
                                    fontSize = TableParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )  //P-Gain Boost
                            Text(
                                text = "",
                                style = TextStyle(
                                    fontSize = TableParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )  //I-Gain Boost
                        }
                    }
                    //  ROW to allocate the following buttons:
                    //  To READ and WRITE --------------------------------------------
                    Row(
                        modifier = Modifier
                            .padding(start = 0.dp, top = 10.dp)
                            .size(width = 380.dp, height = 60.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column( // 1 COLUMN allocates →  1/3 space of the column --------------------------
                            modifier = Modifier
                                .weight(0.33f)
                                .padding(top = 0.dp, start = 10.dp, end = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {}
                        /* Main button section */
                        Column( // 2 COLUMN → Button To Read --------------------------
                            modifier = Modifier
                                .weight(0.33f)
                                .padding(top = 0.dp, start = 10.dp, end = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                // TLDR: Removed passRxvalfromBLEtoVar = true assignment. Previously the flag triggered
                                //       a LaunchedEffect in the composable to delay and then copy BLE data into fields.
                                //       Now rxReadPIDConfiguration() emits pidConfigLoaded when data is ready, so
                                //       the button only needs to call the ViewModel function.
                                onClick = {
                                    viewModel.rxReadPIDConfiguration()
                                    Toast.makeText(context, "Reading PID configuration", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = accent),
                                modifier = Modifier
                                    .fillMaxWidth(1f)
                                    .height(40.dp)
                                    .wrapContentHeight()
                            ) {
                                Text(
                                    text = "Read",
                                    style = TextStyle(
                                        fontSize = ButtonType2_Font,
                                        color = textprimary
                                    )
                                )
                            }
                        }
                        Column( // 3 COLUMN allocates →  1/3 space of the column --------------------------
                            modifier = Modifier
                                .weight(0.33f)
                                .padding(top = 0.dp, start = 10.dp, end = 10.dp),
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
                                        fontSize = ButtonType2_Font,
                                        color = textprimary
                                    ),
                                    modifier = Modifier.pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = {
                                                Toast.makeText(
                                                    context,
                                                    "click",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            onDoubleTap = {
                                                // TLDR: Replaced a 40-line block that checked modified && isNotBlank() for
                                                //       each of the 7 PID fields, called the matching txWritePid*() directly,
                                                //       and reset the flag inline. Moved all that logic to ViewModel.writePIDConfig().
                                                //       Unmodified fields are passed as null (skipped); the composable only
                                                //       clears modified flags and shows the toast after the ViewModel returns.
                                                keyboardController?.hide()
                                                val anyModified = viewModel.writePIDConfig(
                                                    pTerm  = if (cPterm_TB.modified   && cPterm_TB.userInput.text.isNotBlank())   cPterm_TB.userInput.text.toFloat()   else null,
                                                    iTerm  = if (cIterm_TB.modified   && cIterm_TB.userInput.text.isNotBlank())   cIterm_TB.userInput.text.toFloat()   else null,
                                                    iMax   = if (cIlimit_TB.modified  && cIlimit_TB.userInput.text.isNotBlank())  cIlimit_TB.userInput.text.toFloat()  else null,
                                                    dTerm  = if (cDterm_TB.modified   && cDterm_TB.userInput.text.isNotBlank())   cDterm_TB.userInput.text.toFloat()   else null,
                                                    pBoost = if (cPboost_TB.modified  && cPboost_TB.userInput.text.isNotBlank())  cPboost_TB.userInput.text.toFloat()  else null,
                                                    iBoost = if (cIboost_TB.modified  && cIboost_TB.userInput.text.isNotBlank())  cIboost_TB.userInput.text.toFloat()  else null,
                                                )
                                                if (anyModified) {
                                                    listOf(cPterm_TB, cIterm_TB, cIlimit_TB, cDterm_TB, cPboost_TB, cIboost_TB).forEach { it.modified = false }
                                                    Toast.makeText(context, "Sending modified PID configuration", Toast.LENGTH_LONG).show()
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
                    //  BUTTON: To go Back to main page --------------------------------------------
                    Row(
                        modifier = Modifier
                            .padding(start = 0.dp, top = 0.dp)
                            .size(width = 380.dp, height = 60.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column( // 1 COLUMN → empty 1/3 space --------------------------
                            modifier = Modifier
                                .weight(0.33f)
                                .padding(top = 0.dp, start = 10.dp, end = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {}
                        Column( // 2 COLUMN → Button To GO-BACK --------------------------
                            modifier = Modifier
                                .weight(0.33f)
                                .padding(top = 0.dp, start = 10.dp, end = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = {
                                    navController.navigate(Screen.BlespressoMainScreen.route) {
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
                                    text = "Main",
                                    style = TextStyle(
                                        fontSize = ButtonType1_Font,
                                        color = textprimary
                                    )
                                )
                            }
                        }
                        Column( // 3 COLUMN → empty 1/3 space --------------------------
                            modifier = Modifier
                                .weight(0.33f)
                                .padding(top = 0.dp, start = 10.dp, end = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {}
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {

        }
    }
}
