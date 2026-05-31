package com.example.shared.widgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldPrimary

@Composable
fun CategoryChip(
    name: String,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) GoldPrimary else Color.Transparent,
        label = "ChipBgColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.Black else MaterialTheme.colorScheme.onBackground,
        label = "ChipTextColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        label = "ChipBorderColor"
    )

    Text(
        text = name,
        color = textColor,
        fontSize = 13.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        modifier = modifier
            .bounceClick { onSelected() }
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = 1.2.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}
