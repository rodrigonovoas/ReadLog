package com.rodrigonovoa.readlog.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodrigonovoa.readlog.ui.R
import com.rodrigonovoa.readlog.ui.theme.color_on_surface
import com.rodrigonovoa.readlog.ui.theme.color_surface
import com.rodrigonovoa.readlog.ui.theme.color_surface_variant
import com.rodrigonovoa.readlog.ui.theme.color_track

private const val LANGUAGE_ENGLISH = "en"
private const val LANGUAGE_SPANISH = "es"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        LANGUAGE_ENGLISH to stringResource(R.string.language_english),
        LANGUAGE_SPANISH to stringResource(R.string.language_spanish),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = color_surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = stringResource(R.string.language_dialog_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = color_on_surface,
            )
        },
        text = {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                LanguageDropdownField(
                    value = options.firstOrNull { it.first == currentLanguage }?.second.orEmpty(),
                    expanded = expanded,
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    options.forEach { (languageTag, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                expanded = false
                                onLanguageSelected(languageTag)
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdownField(
    value: String,
    expanded: Boolean,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = {},
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color_surface_variant)
            .drawBehind {
                drawRoundRect(
                    color = color_track,
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx()),
                )
            },
        textStyle = TextStyle(
            color = color_on_surface,
            fontSize = 15.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
        ),
        singleLine = true,
        readOnly = true,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    innerTextField()
                }
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        },
    )
}
