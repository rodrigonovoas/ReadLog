package com.rodrigonovoa.readlog.ui.addbook

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rodrigonovoa.readlog.ui.R
import com.rodrigonovoa.readlog.ui.theme.ReadLogTheme
import com.rodrigonovoa.readlog.ui.theme.color_chip
import com.rodrigonovoa.readlog.ui.theme.color_on_surface
import com.rodrigonovoa.readlog.ui.theme.color_on_surface_variant
import com.rodrigonovoa.readlog.ui.theme.color_outline
import com.rodrigonovoa.readlog.ui.theme.color_placeholder
import com.rodrigonovoa.readlog.ui.theme.color_primary
import com.rodrigonovoa.readlog.ui.theme.color_surface
import com.rodrigonovoa.readlog.ui.theme.color_surface_variant
import com.rodrigonovoa.readlog.ui.theme.color_track
import com.rodrigonovoa.readlog.ui.theme.color_transparent

@Composable
fun AddBookScreen(
    state: AddBookUiState,
    onIntent: (AddBookIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = !state.showExitConfirmation) {
        onIntent(AddBookIntent.OnBackClicked)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color_surface)
            .safeDrawingPadding(),
    ) {
        AddBookHeader(
            onBackClick = { onIntent(AddBookIntent.OnBackClicked) },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, top = 20.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            AddBookModeSelector(
                selectedMode = state.selectedMode,
                onModeSelected = { onIntent(AddBookIntent.OnModeSelected(it)) },
            )

            when (state.selectedMode) {
                AddBookMode.Manual -> {
                    CoverPicker(
                        coverUri = state.coverUri,
                        onClick = { onIntent(AddBookIntent.LaunchCoverPicker) },
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        AddBookTextField(
                            label = stringResource(R.string.add_book_field_title_label),
                            value = state.title,
                            placeholder = stringResource(R.string.add_book_field_title_placeholder),
                            onValueChange = { onIntent(AddBookIntent.OnTitleChanged(it)) },
                        )
                        AddBookTextField(
                            label = stringResource(R.string.add_book_field_author_label),
                            value = state.author,
                            placeholder = stringResource(R.string.add_book_field_author_placeholder),
                            onValueChange = { onIntent(AddBookIntent.OnAuthorChanged(it)) },
                        )
                        AddBookTextField(
                            label = stringResource(R.string.add_book_field_pages_label),
                            value = state.pages,
                            placeholder = stringResource(R.string.add_book_field_pages_placeholder),
                            onValueChange = { onIntent(AddBookIntent.OnPagesChanged(it)) },
                            keyboardType = KeyboardType.Number,
                        )
                        AddBookTextField(
                            label = stringResource(R.string.add_book_field_current_page_label),
                            value = state.currentPage,
                            placeholder = stringResource(R.string.add_book_field_current_page_placeholder),
                            onValueChange = { onIntent(AddBookIntent.OnCurrentPageChanged(it)) },
                            keyboardType = KeyboardType.Number,
                        )
                    }

                    if (state.currentPage.isNotEmpty()) {
                        ReadingProgressBar(progressPercentage = state.progressPercentage)
                    }

                    if (state.errorMessage != null) {
                        Text(
                            text = state.errorMessage,
                            color = color_primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }

                AddBookMode.Scan -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.add_book_scan_empty_title),
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = color_on_surface,
                        )
                    }
                }
            }
        }

        if (state.selectedMode == AddBookMode.Manual) {
            Button(
                onClick = { onIntent(AddBookIntent.OnAddBookClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 28.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                enabled = state.isSubmitEnabled && !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = color_primary,
                    contentColor = color_surface,
                    disabledContainerColor = color_primary.copy(alpha = 0.5f),
                    disabledContentColor = color_surface.copy(alpha = 0.7f),
                ),
                contentPadding = PaddingValues(horizontal = 24.dp),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = color_surface,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.add_book_submit),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        if (state.showExitConfirmation) {
            AlertDialog(
                onDismissRequest = { onIntent(AddBookIntent.OnDismissExitClicked) },
                title = {
                    Text(
                        text = stringResource(R.string.add_book_exit_dialog_title),
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                text = {
                    Text(text = stringResource(R.string.add_book_exit_dialog_message))
                },
                confirmButton = {
                    TextButton(
                        onClick = { onIntent(AddBookIntent.OnConfirmExitClicked) },
                    ) {
                        Text(text = stringResource(R.string.add_book_exit_dialog_yes))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { onIntent(AddBookIntent.OnDismissExitClicked) },
                    ) {
                        Text(text = stringResource(R.string.add_book_exit_dialog_no))
                    }
                },
            )
        }
    }
}

@Composable
private fun AddBookHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(color_chip),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = stringResource(R.string.add_book_back_content_description),
                tint = color_on_surface,
                modifier = Modifier.size(18.dp),
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = stringResource(R.string.add_book_title),
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            color = color_on_surface,
        )
    }
}

@Composable
private fun AddBookModeSelector(
    selectedMode: AddBookMode,
    onModeSelected: (AddBookMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color_chip)
            .padding(4.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .shadow(
                    elevation = if (selectedMode == AddBookMode.Manual) 6.dp else 0.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = color_on_surface.copy(alpha = 0.08f),
                    spotColor = color_on_surface.copy(alpha = 0.08f),
                )
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (selectedMode == AddBookMode.Manual) color_surface_variant else color_transparent
                )
                .clickable { onModeSelected(AddBookMode.Manual) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.add_book_manual_tab),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedMode == AddBookMode.Manual) color_on_surface else color_on_surface_variant,
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (selectedMode == AddBookMode.Scan) color_surface_variant else color_transparent
                )
                .shadow(
                    elevation = if (selectedMode == AddBookMode.Scan) 6.dp else 0.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = color_on_surface.copy(alpha = 0.08f),
                    spotColor = color_on_surface.copy(alpha = 0.08f),
                )
                .clickable { onModeSelected(AddBookMode.Scan) },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BarcodeIcon(
                color = if (selectedMode == AddBookMode.Scan) color_on_surface else color_on_surface_variant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.add_book_scan_tab),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedMode == AddBookMode.Scan) color_on_surface else color_on_surface_variant,
            )
        }
    }
}

@Composable
private fun CoverPicker(
    coverUri: Uri?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (coverUri != null) {
            AsyncImage(
                model = coverUri,
                contentDescription = stringResource(R.string.add_book_cover_content_description),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
        } else {
            val strokeWidth = 1.5.dp
            val cornerRadius = 16.dp
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRoundRect(
                            color = color_outline,
                            cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                            style = Stroke(
                                width = strokeWidth.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
                            ),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AddCoverIcon(color = color_placeholder)
                    Text(
                        text = stringResource(R.string.add_book_cover_placeholder),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = color_placeholder,
                    )
                }
            }
        }
    }
}

@Composable
private fun AddBookTextField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.24.sp,
            color = color_on_surface_variant,
        )

        Spacer(modifier = Modifier.height(6.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
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
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = color_placeholder,
                                fontSize = 15.sp,
                            )
                        }
                        innerTextField()
                    }
                }
            },
        )
    }
}

@Composable
private fun BarcodeIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(14.dp)) {
        val widths = listOf(1.4f, 0.7f, 1.4f, 0.7f, 1.4f, 0.7f, 1.4f)
        val gaps = listOf(1f, 1.2f, 1f, 1.2f, 1f, 0.9f)
        var x = 0f
        widths.forEachIndexed { index, width ->
            val barWidth = width.dp.toPx()
            drawRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(x, 1.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(barWidth, 12.dp.toPx()),
            )
            if (index < gaps.size) {
                x += barWidth + gaps[index].dp.toPx()
            }
        }
    }
}

@Composable
private fun AddCoverIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(26.dp)) {
        val strokeWidth = 1.5.dp.toPx()
        drawRoundRect(
            color = color,
            style = Stroke(width = strokeWidth),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(size.width / 2f, 8.dp.toPx()),
            end = androidx.compose.ui.geometry.Offset(size.width / 2f, 18.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(8.dp.toPx(), size.height / 2f),
            end = androidx.compose.ui.geometry.Offset(18.dp.toPx(), size.height / 2f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun ReadingProgressBar(
    progressPercentage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color_track),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((progressPercentage / 100f).coerceIn(0f, 1f))
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color_primary),
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.book_collection_progress_pct, progressPercentage),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color_on_surface_variant,
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun AddBookScreenPreview() {
    ReadLogTheme {
        AddBookScreen(
            state = AddBookUiState(
                title = "One Hundred Years of Solitude",
                author = "Gabriel García Márquez",
                pages = "340",
                currentPage = "231",
                progressPercentage = 67,
                isSubmitEnabled = true,
            ),
            onIntent = {},
        )
    }
}
