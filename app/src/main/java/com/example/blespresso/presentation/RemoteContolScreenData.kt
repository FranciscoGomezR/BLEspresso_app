package com.example.blespresso.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue

class CTextBox {
    var userInput by  mutableStateOf(TextFieldValue("0.0"))
    var clickedOnce by  mutableStateOf(false)// Flag to track if clicked once
    var hasFocus by  mutableStateOf(false)// Flag to track if focus
    var maxInValue: Float = 0.0f
    var minInValue: Float = 0.0f
    var originalValue: String = "0.0" // Store original value from BLE read
    var modified by mutableStateOf(false) // Flag to track if value was actually modified
}

class C_BrewConfigData {
    var PreInfussionPwr: Float = 0.0f
    var PreInfussionTmr: Float = 0.0f
    var InfussionPwr: Float = 0.0f
    var InfussionTmr: Float = 0.0f
    var DecliningPwr: Float = 0.0f
    var DecliningTmr: Float = 0.0f
}

class C_PIDConfigData {
    var cfgPterm: Float = 0.0f
    var cfgIterm: Float = 0.0f
    var cfgImaxterm: Float = 0.0f
    var cfgWindup: Float = 0.0f
    var cfgDterm: Float = 0.0f
    var cfgDlpfterm: Float = 0.0f
    var cfgGainterm: Float = 0.0f
}
