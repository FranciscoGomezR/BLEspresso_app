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

    // TLDR: Replaced 4-line copy-paste blocks (userInput/originalValue/modified/clickedOnce) that repeated
    //       in every LaunchedEffect whenever BLE data arrived. Centralises BLE→UI sync into one call so adding
    //       a new field never requires updating multiple screens.
    fun loadValue(value: String) {
        userInput = TextFieldValue(value)
        originalValue = value
        modified = false
        clickedOnce = false
    }

    // TLDR: Extracted from the file-level processTextFieldInput() function that lived in RemoteControlScreen.kt.
    //       Moved here so every BasicTextField calls cXxx_TB.onValueChange() instead of a global helper,
    //       keeping validation logic co-located with the data it guards.
    //       On first tap it clears the old value, then accepts only numeric/decimal input and tracks
    //       whether the value differs from the last BLE-loaded original.
    fun onValueChange(newText: TextFieldValue) {
        if (newText.text.isNullOrBlank()) {
            userInput = TextFieldValue("0.0")
            modified = originalValue != "0.0"
        } else {
            if (!clickedOnce) {
                userInput = TextFieldValue()
                clickedOnce = true
            } else {
                if (newText.text.all { it.isDigit() || it == '.' }) {
                    val numericValue = newText.text.toFloatOrNull()
                    if (numericValue != null) {
                        userInput = newText
                        modified = newText.text != originalValue
                    } else {
                        userInput = TextFieldValue("0.0")
                        clickedOnce = false
                        modified = originalValue != "0.0"
                    }
                }
            }
        }
    }

    // TLDR: Extracted from the file-level processKeyboardDoneEvent() function that lived in RemoteControlScreen.kt.
    //       Moved here alongside onValueChange() so all text-field logic is owned by CTextBox.
    //       Resets the first-click flag, then clamps the entered value to [minInValue, maxInValue]
    //       and updates the modified flag accordingly.
    fun onDone() {
        clickedOnce = false
        if (userInput.text.isNullOrBlank()) {
            userInput = TextFieldValue("0.0")
            modified = originalValue != "0.0"
        } else {
            if (userInput.text.all { it.isDigit() || it == '.' }) {
                val numericValue = userInput.text.toFloatOrNull()
                if (numericValue != null && numericValue > maxInValue) {
                    userInput = TextFieldValue(maxInValue.toString())
                    modified = maxInValue.toString() != originalValue
                } else if (numericValue != null && numericValue < minInValue) {
                    userInput = TextFieldValue(minInValue.toString())
                    modified = minInValue.toString() != originalValue
                } else {
                    modified = userInput.text != originalValue
                }
            }
        }
    }
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
