package com.example.blespresso.presentation

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

//Youtube video1: https://www.youtube.com/watch?v=1xz9zCqVC40&ab_channel=KApps
//Video1 - TimeStamp: 2:00
@Composable
fun BoilerTemperatureProgressIndicator(
    modifier: Modifier = Modifier,
    boilerWaterTemp:Float,
    boilerTempSetpoint:Float,
    primaryColor: Color,
    secondaryColor:Color,
    primaryTextColor: Color,
    secondaryTextColor:Color,
    minValue:Int = 0,
    maxValue:Int = 150,
    circleRadius:Float,
) {
    //Video1 - TimeStamp: 2:40
    //state variables
    var circleCenter by remember {
        mutableStateOf(Offset.Zero)
    }

    var positionValue by remember {
        mutableStateOf(boilerWaterTemp)
    }

    var positionTargetTemp by remember {
        mutableStateOf(Offset.Zero)
    }
    var positionStartIndicatorTemp by remember {
        mutableStateOf(Offset.Zero)
    }
    var positionEndIndicatorTemp by remember {
        mutableStateOf(Offset.Zero)
    }

    Box(
        modifier = modifier
    ){
        //Video1 - TimeStamp: 3:30
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ){
            //Video1 - TimeStamp: 4:00
            // variables to do calculations
            val width = size.width
            val height = size.height
            val circleThickness = width / 20f
            circleCenter = Offset(x = width/2f, y = height/2f)
            positionValue = boilerWaterTemp

            // circle in the center of the indicator
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        primaryColor.copy(0.85f),
                        secondaryColor.copy(0.25f)
                    )
                ),
                radius = circleRadius,
                center = circleCenter
            )

            //Video1 - TimeStamp: 9:00
            drawCircle(
                style = Stroke(
                    width = circleThickness
                ),
                color = secondaryColor,
                radius = circleRadius,
                center = circleCenter
            )

            //Video1 - TimeStamp: 6:30
            drawArc(
                color = primaryColor,
                startAngle = 135f,       //0 degrees is at the right, the increment CW
                sweepAngle = (270f/(maxValue)) * positionValue,
                style = Stroke(
                    width = circleThickness,
                    cap = StrokeCap.Round
                ),
                useCenter = false,
                size = Size(
                    width = circleRadius * 2f,
                    height = circleRadius * 2f
                ),
                //Video1 - TimeStamp: 8:20
                topLeft = Offset(
                    (width - circleRadius * 2f)/2f,
                    (height - circleRadius * 2f)/2f
                )

            )
            //Video1 - TimeStamp: 10:00
            val outerRadius = circleRadius + circleThickness/5f
            val gap = 30f
            for (i in 0 .. (maxValue-minValue))
            {
                var linecolor = primaryColor.copy(alpha = 0.5f)
                when{
                    i > 105 && i<=boilerTempSetpoint -> linecolor = Color.Red
                    i < boilerTempSetpoint-minValue -> linecolor = primaryColor
                    else                          -> primaryColor.copy(alpha = 0.4f)
                }
                //val color = if(i < boilerTempSetpoint-minValue) primaryColor else primaryColor.copy(alpha = 0.5f)
                val angleInDegrees = (45f+(i.toFloat()*270f/(maxValue-minValue)))
                val angleInRad = angleInDegrees * PI / 180f + PI/2f

                val yGapAdjustment = cos(angleInDegrees * PI / 180f)*gap
                val xGapAdjustment = -sin(angleInDegrees * PI / 180f)*gap

                val start = Offset(
                    x = (outerRadius * cos(angleInRad) + circleCenter.x + xGapAdjustment).toFloat(),
                    y = (outerRadius * sin(angleInRad) + circleCenter.y + yGapAdjustment).toFloat()
                )

                val end = Offset(
                    x = (outerRadius * cos(angleInRad) + circleCenter.x + xGapAdjustment).toFloat(),
                    y = (outerRadius * sin(angleInRad) + circleThickness + circleCenter.y + yGapAdjustment).toFloat()
                )

                if( i == 0)
                {
                    positionStartIndicatorTemp = end
                }else{}
                if( i == (boilerTempSetpoint.toInt()))
                {
                    val target = Offset(
                        x = ((outerRadius*1.25) * cos(angleInRad) + circleCenter.x + xGapAdjustment).toFloat(),
                        y = ((outerRadius*1.25) * sin(angleInRad) + circleThickness + circleCenter.y + yGapAdjustment).toFloat()
                    )
                    positionTargetTemp = target
                }else{}
                if( i == ((maxValue-minValue)-1) )
                {
                    positionEndIndicatorTemp = end
                }else{}

                    rotate(
                    angleInDegrees,
                    pivot = start
                ){
                    drawLine(
                        color = linecolor,
                        start = start,
                        end = end,
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }

            //Video1 - TimeStamp: 18:30
            drawContext.canvas.nativeCanvas.apply {
                drawIntoCanvas {
                    drawText(
                        "$positionValue°C",
                        circleCenter.x,
                        circleCenter.y - 10.dp.toPx(),
                        Paint().apply {
                            textSize = 46.sp.toPx()
                            textAlign = Paint.Align.CENTER
                            color = primaryTextColor.toArgb()
                            isFakeBoldText = true
                        }
                    )
                    drawText(
                        "Target: $boilerTempSetpoint°C",
                        circleCenter.x,
                        circleCenter.y + 35.dp.toPx()/1f,
                        Paint().apply {
                            textSize = 16.sp.toPx()
                            textAlign = Paint.Align.CENTER
                            color = primaryTextColor.toArgb()
                            isFakeBoldText = true
                        }
                    )
                    drawLine(
                        circleCenter.x+(circleRadius*.65f),
                        circleCenter.y+(circleRadius*.1f),
                        circleCenter.x-(circleRadius*.65f),
                        circleCenter.y+(circleRadius*.1f),
                        Paint().apply{
                            color = primaryTextColor.toArgb()
                            strokeWidth = 8f
                        }

                    )
                    drawText(
                        "$boilerTempSetpoint°C",
                        positionTargetTemp.x,
                        positionTargetTemp.y,
                        Paint().apply {
                            textSize = 8.sp.toPx()
                            textAlign = Paint.Align.CENTER
                            color = secondaryTextColor.toArgb()
                            isFakeBoldText = true
                        }
                    )
                    drawText(
                        "$minValue°C",
                        positionStartIndicatorTemp.x+15,
                        positionStartIndicatorTemp.y+30,
                        Paint().apply {
                            textSize = 8.sp.toPx()
                            textAlign = Paint.Align.CENTER
                            color = secondaryTextColor.toArgb()
                            isFakeBoldText = true
                        }
                    )
                    drawText(
                        "$maxValue°C",
                        positionEndIndicatorTemp.x-20,
                        positionEndIndicatorTemp.y+30,
                        Paint().apply {
                            textSize = 8.sp.toPx()
                            textAlign = Paint.Align.CENTER
                            color = secondaryTextColor.toArgb()
                            isFakeBoldText = true
                        }
                    )

                }
            }

        }
    }
}
//Video1 - TimeStamp: 5:15
//this show the canvas in  split window
/*
@Preview(showBackground = true)
@Composable
fun Preview() {
    BoilerTemperatureProgressIndicator(
        modifier = Modifier
            .size(350.dp)
            .background(darkGray),
        boilerWaterTemp = 25f,
        boilerTempSetpoint = 100f,
        primaryColor = Cyan800,
        secondaryColor = gray,
        circleRadius = 330f,
    )
}
*/