package com.example.blespresso.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.blespresso.ui.theme.accent_indic
import com.example.blespresso.ui.theme.background_indic
import com.example.blespresso.ui.theme.textprimary_indic

@Composable
fun StartScreen(navController: NavController){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background_indic),
        contentAlignment = Alignment.Center
    ){
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(accent_indic, CircleShape)
                .clickable {
                    //Navigate
                    navController.navigate(Screen.ConnectScreen.route) {
                        popUpTo(Screen.StartScreen.route){
                            inclusive = true
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ){
            Text(
                text = "CONNECT \nTO ESPRESSO \nMACHINE",
                modifier = Modifier,
                    //.rotate(-90f),
                color = textprimary_indic,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}
