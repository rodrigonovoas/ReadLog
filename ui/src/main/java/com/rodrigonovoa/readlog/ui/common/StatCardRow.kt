package com.rodrigonovoa.readlog.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodrigonovoa.readlog.ui.theme.color_on_surface
import com.rodrigonovoa.readlog.ui.theme.color_on_surface_variant
import com.rodrigonovoa.readlog.ui.theme.color_surface_variant

data class StatCardItem(
    val label: String,
    val value: String,
    val isLongValue: Boolean = false,
)

enum class StatCardSize { REGULAR, LARGE }

private val StatCardCompactWidthThreshold = 360.dp

@Composable
fun StatCardRow(
    items: List<StatCardItem>,
    modifier: Modifier = Modifier,
    size: StatCardSize = StatCardSize.REGULAR,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val compact = maxWidth < StatCardCompactWidthThreshold
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
        ) {
            items.forEach { item ->
                StatCard(
                    label = item.label,
                    value = item.value,
                    isLongValue = item.isLongValue,
                    compact = compact,
                    size = size,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    isLongValue: Boolean,
    compact: Boolean,
    size: StatCardSize,
    modifier: Modifier = Modifier,
) {
    val labelFontSize = when {
        size == StatCardSize.LARGE && compact -> 10.5.sp
        size == StatCardSize.LARGE -> 12.sp
        compact -> 9.5.sp
        else -> 11.sp
    }
    val labelLineHeight = when {
        size == StatCardSize.LARGE && compact -> 12.sp
        size == StatCardSize.LARGE -> 14.sp
        compact -> 11.sp
        else -> 13.sp
    }
    val valueFontSize = when {
        size == StatCardSize.LARGE && compact && isLongValue -> 19.sp
        size == StatCardSize.LARGE && compact -> 22.sp
        size == StatCardSize.LARGE && isLongValue -> 23.sp
        size == StatCardSize.LARGE -> 27.sp
        compact && isLongValue -> 17.sp
        compact -> 20.sp
        isLongValue -> 20.sp
        else -> 24.sp
    }
    val minValueFontSize = if (size == StatCardSize.LARGE) 14.sp else 13.sp

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color_surface_variant)
            .padding(horizontal = if (compact) 8.dp else 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = label,
            fontSize = labelFontSize,
            lineHeight = labelLineHeight,
            fontWeight = FontWeight.SemiBold,
            color = color_on_surface_variant,
            textAlign = TextAlign.Center,
            maxLines = 3,
        )
        AutoSizeText(
            text = value,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            maxFontSize = valueFontSize,
            minFontSize = minValueFontSize,
            color = color_on_surface,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun AutoSizeText(
    text: String,
    maxFontSize: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    minFontSize: TextUnit = 13.sp,
) {
    var fontSize by remember(text, maxFontSize) { mutableStateOf(maxFontSize) }
    var readyToDraw by remember(text, maxFontSize) { mutableStateOf(false) }

    Text(
        text = text,
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        fontSize = fontSize,
        color = color,
        maxLines = 1,
        softWrap = false,
        modifier = modifier.drawWithContent { if (readyToDraw) drawContent() },
        onTextLayout = { result ->
            if (result.didOverflowWidth && fontSize.value > minFontSize.value) {
                fontSize = (fontSize.value - 1).sp
            } else {
                readyToDraw = true
            }
        },
    )
}
