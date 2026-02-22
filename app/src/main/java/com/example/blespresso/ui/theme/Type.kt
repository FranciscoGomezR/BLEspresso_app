package com.example.blespresso.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val ButtonType1_Font =  14.sp
val ButtonType2_Font =  16.sp
val ButtonType3_Font =  28.sp

val TableTitle_fontsize =  18.sp
val TableParam_fontsize =  15.sp
val TParameter_fontsize =  15.sp
val ParamValue_fontsize =  16.sp
val MinMaxParam_fontsize =  14.sp

// Set of Material typography styles to start with
val Typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
    /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)