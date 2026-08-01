package com.rodrigonovoa.readlog.ui.addbook

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.rodrigonovoa.readlog.domain.model.BookState
import com.rodrigonovoa.readlog.ui.R
import com.rodrigonovoa.readlog.ui.common.bookStateStringRes
import com.rodrigonovoa.readlog.ui.theme.ReadLogTheme
import com.rodrigonovoa.readlog.ui.theme.color_chip
import com.rodrigonovoa.readlog.ui.theme.color_on_surface
import com.rodrigonovoa.readlog.ui.theme.color_on_surface_variant
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
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(state.selectedMode) {
        if (state.selectedMode == AddBookMode.Scan && !state.isEditMode) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                onIntent(AddBookIntent.OnCameraPermissionResult(true))
            } else {
                onIntent(AddBookIntent.RequestCameraPermission)
            }
        }
    }

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
            isEditMode = state.isEditMode,
            onBackClick = { onIntent(AddBookIntent.OnBackClicked) },
        )

        if (!state.isEditMode) {
            AddBookModeSelector(
                modifier = Modifier.padding(start = 24.dp, top = 20.dp, end = 24.dp),
                selectedMode = state.selectedMode,
                onModeSelected = { onIntent(AddBookIntent.OnModeSelected(it)) },
            )
        }

        when (state.selectedMode) {
            AddBookMode.Manual -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 24.dp, top = 20.dp, end = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    if (state.coverUrl.isNotEmpty()) {
                        AsyncImage(
                            model = state.coverUrl,
                            contentDescription = stringResource(R.string.add_book_cover_content_description),
                            modifier = Modifier
                                .width(100.dp)
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .align(Alignment.CenterHorizontally),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        )
                    }

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

                        BookStateDropdown(
                            selectedState = state.state,
                            onStateSelected = { onIntent(AddBookIntent.OnStateChanged(it)) },
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
                        val buttonTextRes = if (state.isEditMode) {
                            R.string.add_book_save_changes
                        } else {
                            R.string.add_book_submit
                        }
                        Text(
                            text = stringResource(buttonTextRes),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            AddBookMode.Scan -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                ) {
                    if (state.hasCameraPermission) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(R.string.add_book_scan_title),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                                color = color_on_surface,
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(24.dp)),
                            ) {
                                BarcodeScanner(
                                    onBarcodeDetected = {
                                        onIntent(AddBookIntent.OnBarcodeScanned(it))
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                )

                                if (state.isScanning) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color_on_surface.copy(alpha = 0.6f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                        ) {
                                            CircularProgressIndicator(
                                                color = color_surface,
                                                strokeWidth = 2.dp,
                                            )
                                            Text(
                                                text = stringResource(R.string.add_book_scan_searching),
                                                color = color_surface,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                            )
                                        }
                                    }
                                }

                                state.scanError?.let { error ->
                                    val messageRes = when (error) {
                                        ScanError.Network -> R.string.add_book_scan_network_error
                                        ScanError.NotFound -> R.string.add_book_scan_book_not_found
                                        ScanError.Unknown -> R.string.add_book_scan_invalid_barcode
                                    }
                                    ScanErrorOverlay(
                                        message = stringResource(messageRes),
                                        onRetry = { onIntent(AddBookIntent.OnScanRetryClicked) },
                                        onDismiss = { onIntent(AddBookIntent.OnScanErrorDismissed) },
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = stringResource(R.string.add_book_scan_instructions),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = color_on_surface_variant,
                            )

                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                thickness = 1.dp,
                                color = color_track,
                            )

                            AddBookTextField(
                                label = stringResource(R.string.add_book_scan_isbn_label),
                                value = state.manualIsbn,
                                placeholder = stringResource(R.string.add_book_scan_isbn_placeholder),
                                onValueChange = { onIntent(AddBookIntent.OnManualIsbnChanged(it)) },
                                keyboardType = KeyboardType.Number,
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { onIntent(AddBookIntent.OnManualIsbnSearchClicked) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                enabled = state.isManualIsbnSearchEnabled && !state.isScanning,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = color_primary,
                                    contentColor = color_surface,
                                    disabledContainerColor = color_primary.copy(alpha = 0.5f),
                                    disabledContentColor = color_surface.copy(alpha = 0.7f),
                                ),
                            ) {
                                Text(
                                    text = stringResource(R.string.add_book_scan_search_button),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(0.8f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.add_book_scan_no_permission),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = color_on_surface,
                                )
                                Button(
                                    onClick = { onIntent(AddBookIntent.RequestCameraPermission) },
                                    shape = RoundedCornerShape(28.dp),
                                ) {
                                    Text(
                                        text = stringResource(R.string.add_book_scan_grant_permission),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
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
    isEditMode: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleRes = if (isEditMode) R.string.add_book_edit_title else R.string.add_book_title
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
            text = stringResource(titleRes),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookStateDropdown(
    selectedState: BookState,
    onStateSelected: (BookState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val expanded = remember { mutableStateOf(false) }
    val options = BookState.entries

    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = { expanded.value = it },
        modifier = modifier,
    ) {
        AddBookTextField(
            label = stringResource(R.string.add_book_field_state_label),
            value = stringResource(bookStateStringRes(selectedState)),
            placeholder = "",
            onValueChange = {},
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            readOnly = true,
        )
        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(bookStateStringRes(option))) },
                    onClick = {
                        onStateSelected(option)
                        expanded.value = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
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
    readOnly: Boolean = false,
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
            readOnly = readOnly,
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
private fun ScanErrorOverlay(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color_on_surface.copy(alpha = 0.6f))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(color_surface)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = message,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = color_on_surface,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.add_book_exit_dialog_no),
                        color = color_on_surface_variant,
                    )
                }
                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(28.dp),
                ) {
                    Text(
                        text = stringResource(R.string.add_book_scan_retry),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
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
