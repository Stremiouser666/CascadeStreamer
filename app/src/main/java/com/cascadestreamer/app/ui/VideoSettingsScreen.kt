package com.cascadestreamer.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AspectRatio {
    FILL, FIT, STRETCH, CUSTOM
}

data class VideoSettings(
    val brightness: Float = 1.0f,
    val contrast: Float = 1.0f,
    val saturation: Float = 1.0f,
    val aspectRatio: AspectRatio = AspectRatio.FIT,
    val customWidth: Float = 1.0f,
    val customHeight: Float = 1.0f
)

@Composable
fun VideoSettingsScreen(
    onBack: () -> Unit
) {
    val brightness = remember { mutableStateOf(1.0f) }
    val contrast = remember { mutableStateOf(1.0f) }
    val saturation = remember { mutableStateOf(1.0f) }
    val aspectRatio = remember { mutableStateOf(AspectRatio.FIT) }
    val customWidth = remember { mutableStateOf(1.0f) }
    val customHeight = remember { mutableStateOf(1.0f) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Video Settings",
            fontSize = 28.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        VideoSliderSetting(
            label = "Brightness",
            value = brightness.value,
            onValueChange = { brightness.value = it },
            range = 0.5f..2.0f
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        VideoSliderSetting(
            label = "Contrast",
            value = contrast.value,
            onValueChange = { contrast.value = it },
            range = 0.5f..2.0f
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        VideoSliderSetting(
            label = "Color/Saturation",
            value = saturation.value,
            onValueChange = { saturation.value = it },
            range = 0.0f..2.0f
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Aspect Ratio",
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        listOf(
            AspectRatio.FIT to "Fit",
            AspectRatio.FILL to "Fill",
            AspectRatio.STRETCH to "Stretch",
            AspectRatio.CUSTOM to "Custom"
        ).forEach { (ratio, label) ->
            AspectRatioItem(
                label = label,
                isSelected = aspectRatio.value == ratio,
                onClick = { aspectRatio.value = ratio }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (aspectRatio.value == AspectRatio.CUSTOM) {
            VideoSliderSetting(
                label = "Width Adjustment",
                value = customWidth.value,
                onValueChange = { customWidth.value = it },
                range = 0.5f..2.0f
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            VideoSliderSetting(
                label = "Height Adjustment",
                value = customHeight.value,
                onValueChange = { customHeight.value = it },
                range = 0.5f..2.0f
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        Text(
            "Back",
            fontSize = 16.sp,
            color = Color(0xFF64B5F6),
            modifier = Modifier
                .background(Color.DarkGray)
                .clickable { onBack() }
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun VideoSliderSetting(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                fontSize = 16.sp,
                color = Color.White
            )
            Text(
                String.format("%.1f", value),
                fontSize = 14.sp,
                color = Color(0xFF64B5F6)
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )
    }
}

@Composable
fun AspectRatioItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) Color(0xFF64B5F6) else Color.DarkGray
            )
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 16.sp,
            color = if (isSelected) Color.Black else Color.White
        )
        if (isSelected) {
            Text(
                "✓",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End)
            )
        }
    }
}
