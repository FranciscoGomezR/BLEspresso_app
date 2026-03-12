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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class, ExperimentalPermissionsApi::class)
@Composable
fun BlespressoCtrlParamScreen(
    navController: NavController,
    onBluetoothStateChanged:()->Unit,
    viewModel: RemoteControlViewModel = hiltViewModel()
) {
    //screen content
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

    var isFocused by remember { mutableStateOf(false) }

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

    // MutableState to trigger the long-running task
    var passRxvalfromBLEtoVar by remember { mutableStateOf(false) }

    LaunchedEffect(passRxvalfromBLEtoVar)
    {
        delay(1500L)
        Log.d(
            "ScreenPIDconfiguration",
            "Passing PID configurations from BLE to Screen FieldText"
        )

        cPterm_TB.userInput = TextFieldValue(viewModel.pIDConfigData.cfgPterm.toString())
        cPterm_TB.originalValue = viewModel.pIDConfigData.cfgPterm.toString()
        cPterm_TB.modified = false
        cPterm_TB.clickedOnce = false
        cIterm_TB.userInput = TextFieldValue(viewModel.pIDConfigData.cfgIterm.toString())
        cIterm_TB.originalValue = viewModel.pIDConfigData.cfgIterm.toString()
        cIterm_TB.modified = false
        cIterm_TB.clickedOnce = false
        cIlimit_TB.userInput = TextFieldValue(viewModel.pIDConfigData.cfgImaxterm.toString())
        cIlimit_TB.originalValue = viewModel.pIDConfigData.cfgImaxterm.toString()
        cIlimit_TB.modified = false
        cIlimit_TB.clickedOnce = false
        cIwindup_TB.userInput = TextFieldValue(viewModel.pIDConfigData.cfgWindup.toString())
        cIwindup_TB.originalValue = viewModel.pIDConfigData.cfgWindup.toString()
        cIwindup_TB.modified = false
        cIwindup_TB.clickedOnce = false
        cDterm_TB.userInput = TextFieldValue(viewModel.pIDConfigData.cfgDterm.toString())
        cDterm_TB.originalValue = viewModel.pIDConfigData.cfgDterm.toString()
        cDterm_TB.modified = false
        cDterm_TB.clickedOnce = false
        cLPFterm_TB.userInput = TextFieldValue(viewModel.pIDConfigData.cfgDlpfterm.toString())
        cLPFterm_TB.originalValue = viewModel.pIDConfigData.cfgDlpfterm.toString()
        cLPFterm_TB.modified = false
        cLPFterm_TB.clickedOnce = false
        cGainterm_TB.userInput = TextFieldValue(viewModel.pIDConfigData.cfgGainterm.toString())
        cGainterm_TB.originalValue = viewModel.pIDConfigData.cfgGainterm.toString()
        cGainterm_TB.modified = false
        cGainterm_TB.clickedOnce = false
        // Reset the flag
        passRxvalfromBLEtoVar = false
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
                                text = "Integral Windup",
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
                                text = "LPF Freq. Cut",
                                style = TextStyle(
                                    fontSize = TParameter_fontsize,
                                    color = textprimary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )
                            Text(
                                text = "System Gain",
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
                                text = cIwindup_TB.minInValue.toString(),
                                style = TextStyle(
                                    fontSize = MinMaxParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            ) //Integral Windup
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
                                text = cLPFterm_TB.minInValue.toString(),
                                style = TextStyle(
                                    fontSize = MinMaxParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )  //LPF Freq. Cut
                            Text(
                                text = cGainterm_TB.minInValue.toString(),
                                style = TextStyle(
                                    fontSize = MinMaxParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )  //System Gain
                        }
                        Column( // 3 COLUMN → Input----------------------------
                            modifier = Modifier.weight(2f).padding(top = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BasicTextField( // Proportional Term INPUT
                                value = cPterm_TB.userInput,
                                onValueChange = { newText ->
                                    processTextFieldInput(newText, cPterm_TB)
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
                                    processKeyboardDoneEvent(cPterm_TB)
                                    keyboardController?.hide()
                                })
                            )
                            BasicTextField( // Integral Term INPUT
                                value = cIterm_TB.userInput,
                                onValueChange = { newText ->
                                    processTextFieldInput(
                                        newText,
                                        cIterm_TB
                                    )
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
                                    processKeyboardDoneEvent(cIterm_TB)
                                    keyboardController?.hide()
                                })
                            )
                            BasicTextField( // Integral limit Term INPUT
                                value = cIlimit_TB.userInput,
                                onValueChange = { newText ->
                                    processTextFieldInput(
                                        newText,
                                        cIlimit_TB
                                    )
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
                                    processKeyboardDoneEvent(cIlimit_TB)
                                    keyboardController?.hide()
                                })
                            )
                            BasicTextField( // Integral Windup INPUT
                                value = cIwindup_TB.userInput,
                                onValueChange = { newText ->
                                    processTextFieldInput(
                                        newText,
                                        cIwindup_TB
                                    )
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
                                    processKeyboardDoneEvent(cIwindup_TB)
                                    keyboardController?.hide()
                                })
                            )
                            BasicTextField( // Derivatve Term INPUT
                                value = cDterm_TB.userInput,
                                onValueChange = { newText ->
                                    processTextFieldInput(
                                        newText,
                                        cDterm_TB
                                    )
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
                                    processKeyboardDoneEvent(cDterm_TB)
                                    keyboardController?.hide()
                                })
                            )
                            BasicTextField( // Low-pass Filter Frequency INPUT
                                value = cLPFterm_TB.userInput,
                                onValueChange = { newText ->
                                    processTextFieldInput(
                                        newText,
                                        cLPFterm_TB
                                    )
                                },
                                cursorBrush = SolidColor(textprimary),
                                textStyle = TextStyle(
                                    color = textprimary,
                                    fontSize = ParamValue_fontsize,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.height(30.dp)
                                    .background(surface)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                                    .onFocusChanged { focusState ->
                                        isFocused = focusState.isFocused
                                        if (isFocused) { // The TextField is focused
                                        } else {// The TextField lost focus
                                            if (cLPFterm_TB.userInput.text.toDouble() < cLPFterm_TB.minInValue)
                                                cLPFterm_TB.userInput =
                                                    TextFieldValue(cLPFterm_TB.minInValue.toString())
                                        }
                                    },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    processKeyboardDoneEvent(cLPFterm_TB)
                                    keyboardController?.hide()
                                })
                            )
                            BasicTextField( // System Gain Term INPUT
                                value = cGainterm_TB.userInput,
                                onValueChange = { newText ->
                                    processTextFieldInput(
                                        newText,
                                        cGainterm_TB
                                    )
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
                                    processKeyboardDoneEvent(cGainterm_TB)
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
                                text = cIwindup_TB.maxInValue.toString(),
                                style = TextStyle(
                                    fontSize = MinMaxParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            ) //Integral Windup
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
                                text = cLPFterm_TB.maxInValue.toString(),
                                style = TextStyle(
                                    fontSize = MinMaxParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )  //LPF Freq. Cut
                            Text(
                                text = cGainterm_TB.maxInValue.toString(),
                                style = TextStyle(
                                    fontSize = MinMaxParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )  //System Gain
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
                            ) //Integral Windup
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
                                text = "Hz",
                                style = TextStyle(
                                    fontSize = TableParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )  //LPF Freq. Cut
                            Text(
                                text = "",
                                style = TextStyle(
                                    fontSize = TableParam_fontsize,
                                    color = textsecondary
                                ),
                                modifier = Modifier.height(30.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )  //System Gain
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
                                onClick = {
                                    keyboardController?.hide()      // Hide the keyboard when the button is clicked
                                    passRxvalfromBLEtoVar = true
                                    /* Handle button click → read PID Configuration */
                                    viewModel.rxReadPIDConfiguration()
                                    Toast.makeText(context, "Reading PID configuration\"", Toast.LENGTH_SHORT).show()
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
                                                keyboardController?.hide()
                                                var anyModified = false

                                                // TLDR: Write PID Proportional term if user changed it
                                                if (cPterm_TB.modified && cPterm_TB.userInput.text.isNotBlank()) {
                                                    viewModel.txWritePidPTerm(cPterm_TB.userInput.text.toFloat())
                                                    Log.d("ControllerParamScreen", "P Term: ${cPterm_TB.userInput.text} -> Saved")
                                                    cPterm_TB.modified = false
                                                    anyModified = true
                                                }
                                                // TLDR: Write PID Integral term if user changed it
                                                if (cIterm_TB.modified && cIterm_TB.userInput.text.isNotBlank()) {
                                                    viewModel.txWritePidITerm(cIterm_TB.userInput.text.toFloat())
                                                    Log.d("ControllerParamScreen", "I Term: ${cIterm_TB.userInput.text} -> Saved")
                                                    cIterm_TB.modified = false
                                                    anyModified = true
                                                }
                                                // TLDR: Write PID Integral max clamp value if user changed it
                                                if (cIlimit_TB.modified && cIlimit_TB.userInput.text.isNotBlank()) {
                                                    viewModel.txWritePidIMax(cIlimit_TB.userInput.text.toFloat())
                                                    Log.d("ControllerParamScreen", "I Max Clamp: ${cIlimit_TB.userInput.text} -> Saved")
                                                    cIlimit_TB.modified = false
                                                    anyModified = true
                                                }
                                                // TLDR: Write PID anti-windup flag if user changed it
                                                if (cIwindup_TB.modified && cIwindup_TB.userInput.text.isNotBlank()) {
                                                    viewModel.txWritePidWindup(cIwindup_TB.userInput.text.toFloat())
                                                    Log.d("ControllerParamScreen", "I Windup: ${cIwindup_TB.userInput.text} -> Saved")
                                                    cIwindup_TB.modified = false
                                                    anyModified = true
                                                }
                                                // TLDR: Write PID Derivative term if user changed it
                                                if (cDterm_TB.modified && cDterm_TB.userInput.text.isNotBlank()) {
                                                    viewModel.txWritePidDTerm(cDterm_TB.userInput.text.toFloat())
                                                    Log.d("ControllerParamScreen", "D Term: ${cDterm_TB.userInput.text} -> Saved")
                                                    cDterm_TB.modified = false
                                                    anyModified = true
                                                }
                                                // TLDR: Write PID Derivative low-pass filter cutoff if user changed it
                                                if (cLPFterm_TB.modified && cLPFterm_TB.userInput.text.isNotBlank()) {
                                                    viewModel.txWritePidDLpf(cLPFterm_TB.userInput.text.toFloat())
                                                    Log.d("ControllerParamScreen", "D LPF: ${cLPFterm_TB.userInput.text} -> Saved")
                                                    cLPFterm_TB.modified = false
                                                    anyModified = true
                                                }
                                                // TLDR: Write overall PID gain multiplier if user changed it
                                                if (cGainterm_TB.modified && cGainterm_TB.userInput.text.isNotBlank()) {
                                                    viewModel.txWritePidGain(cGainterm_TB.userInput.text.toFloat())
                                                    Log.d("ControllerParamScreen", "Gain Term: ${cGainterm_TB.userInput.text} -> Saved")
                                                    cGainterm_TB.modified = false
                                                    anyModified = true
                                                }

                                                // TLDR: Show confirmation toast if any PID field was written, otherwise notify user nothing changed
                                                if (anyModified) {
                                                    Log.d("ControllerParamScreen", "Sent modified PID parameters")
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
