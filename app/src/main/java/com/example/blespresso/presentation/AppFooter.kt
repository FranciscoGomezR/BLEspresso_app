package com.example.blespresso.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blespresso.BuildConfig
import com.example.blespresso.R
import com.example.blespresso.ui.theme.background
import com.example.blespresso.ui.theme.textsecondary

// TLDR: Fixed footer on all screens (STATUS #14): "V<versionName> · <company_name>".
@Composable
fun AppFooter(modifier: Modifier = Modifier) {
    Text(
        text = "V${BuildConfig.VERSION_NAME} · ${stringResource(R.string.company_name)}",
        color = textsecondary,
        fontSize = 11.sp,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .background(background)
            .navigationBarsPadding()
            .padding(vertical = 4.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun AppFooterPreview() {
    AppFooter()
}

/*
AppFooter.kt (new): one small centred line, "V0.1 · FranciscoGomezR". It uses the theme's background and secondary text colours, so it switches with light/dark mode, and it sits above the phone's navigation bar.
strings.xml: added company_name = "FranciscoGomezR".
build.gradle: versionName changed from "1.0" to "0.1". The footer reads it automatically, so later version bumps show up without code changes.
*/

