package com.rodrigonovoa.readlog.ui.booksession

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.rodrigonovoa.readlog.ui.R
import com.rodrigonovoa.readlog.ui.theme.ReadLogTheme
import com.rodrigonovoa.readlog.ui.theme.color_chip
import com.rodrigonovoa.readlog.ui.theme.color_on_surface
import com.rodrigonovoa.readlog.ui.theme.color_on_surface_variant
import com.rodrigonovoa.readlog.ui.theme.color_placeholder
import com.rodrigonovoa.readlog.ui.theme.color_primary
import com.rodrigonovoa.readlog.ui.theme.color_secondary
import com.rodrigonovoa.readlog.ui.theme.color_session_background_bottom
import com.rodrigonovoa.readlog.ui.theme.color_session_background_top
import com.rodrigonovoa.readlog.ui.theme.color_surface
import com.rodrigonovoa.readlog.ui.theme.color_surface_variant
import com.rodrigonovoa.readlog.ui.theme.color_track
import com.rodrigonovoa.readlog.ui.theme.color_transparent
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BookSessionScreen(
    modifier: Modifier = Modifier,
    uiState: BookSessionUiState = BookSessionUiState(),
    onIntent: (BookSessionIntent) -> Unit = {},
) {
    BackHandler(enabled = !uiState.showEndSessionDialog) {
        onIntent(BookSessionIntent.OnBackClicked)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(color_session_background_top, color_session_background_bottom),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-60).dp)
                .size(260.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(color_secondary.copy(alpha = 0.32f), color_secondary.copy(alpha = 0f)),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-100).dp, y = (-180).dp)
                .size(280.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(color_primary.copy(alpha = 0.22f), color_primary.copy(alpha = 0f)),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.exclude(WindowInsets.navigationBars)),
        ) {
            when {
                uiState.isLoading -> LoadingContent()
                uiState.loadError -> ErrorContent(
                    onRetry = { onIntent(BookSessionIntent.OnRetryLoadClicked) },
                    onBack = { onIntent(BookSessionIntent.OnBackClicked) },
                )
                else -> {
                    SessionHeader(
                        bookTitle = uiState.bookTitle,
                        sessionDateLabel = formatSessionDate(uiState.sessionDate),
                        currentPage = uiState.currentPage,
                        pendingPages = uiState.pendingPages,
                        totalPages = uiState.totalPages,
                        onBackClick = { onIntent(BookSessionIntent.OnBackClicked) },
                    )

                    SessionModeSelector(
                        selectedMode = uiState.selectedMode,
                        onModeSelected = { onIntent(BookSessionIntent.OnModeSelected(it)) },
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    )

                    when (uiState.selectedMode) {
                        BookSessionMode.Timer -> {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = stringResource(R.string.book_session_reading_time_label)
                                        .uppercase(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp,
                                    color = color_on_surface_variant,
                                )
                                SessionStatusLabel(status = uiState.sessionStatus)
                                Text(
                                    text = formatElapsedTime(uiState.elapsedSeconds),
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 64.sp,
                                    color = color_on_surface,
                                    modifier = Modifier.padding(top = 10.dp),
                                )
                                SessionWaveform(modifier = Modifier.padding(top = 10.dp))

                                Row(
                                    modifier = Modifier.padding(top = 26.dp),
                                    horizontalArrangement = Arrangement.spacedBy(22.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    SessionActionButton(
                                        label = stringResource(R.string.book_session_stop),
                                        buttonSize = 52.dp,
                                        containerColor = Color.White.copy(alpha = 0.6f),
                                        onClick = { onIntent(BookSessionIntent.OnStopClicked) },
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(color_on_surface),
                                        )
                                    }
                                    SessionActionButton(
                                        label = stringResource(
                                            if (uiState.isRunning) R.string.book_session_pause else R.string.book_session_play
                                        ),
                                        buttonSize = 76.dp,
                                        containerColor = color_primary,
                                        elevated = true,
                                        onClick = { onIntent(BookSessionIntent.OnPlayPauseClicked) },
                                    ) {
                                        if (uiState.isRunning) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(4.dp)
                                                        .height(14.dp)
                                                        .clip(RoundedCornerShape(1.5.dp))
                                                        .background(color_surface),
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .width(4.dp)
                                                        .height(14.dp)
                                                        .clip(RoundedCornerShape(1.5.dp))
                                                        .background(color_surface),
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = color_surface,
                                                modifier = Modifier.size(28.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        BookSessionMode.Manual -> {
                            ManualTimeEntryContent(
                                hours = uiState.manualHours,
                                minutes = uiState.manualMinutes,
                                dateMillis = uiState.manualDateMillis,
                                onHoursChanged = { onIntent(BookSessionIntent.OnManualHoursChanged(it)) },
                                onMinutesChanged = { onIntent(BookSessionIntent.OnManualMinutesChanged(it)) },
                                onDateChanged = { onIntent(BookSessionIntent.OnManualDateChanged(it)) },
                                onSaveClick = { onIntent(BookSessionIntent.OnSaveManualTimeClicked) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    SessionActionsSheet(
                        annotationText = uiState.annotationText,
                        onOpenAnnotationDialogClick = { onIntent(BookSessionIntent.OnOpenAnnotationDialogClicked) },
                        onOpenPageDialogClick = { onIntent(BookSessionIntent.OnOpenPageDialogClicked) },
                    )
                }
            }
        }
    }

    if (uiState.showEndSessionDialog) {
        EndSessionDialog(
            title = stringResource(R.string.book_session_end_dialog_title),
            message = stringResource(R.string.book_session_end_dialog_message),
            onDiscard = { onIntent(BookSessionIntent.OnDiscardSessionClicked) },
            onSaveAndFinish = { onIntent(BookSessionIntent.OnSaveAndFinishSessionClicked) },
            onKeepReading = { onIntent(BookSessionIntent.OnDismissEndSessionDialogClicked) },
        )
    }

    if (uiState.showAnnotationDialog) {
        AnnotationDialog(
            initialText = uiState.annotationText,
            onDismiss = { onIntent(BookSessionIntent.OnDismissAnnotationDialogClicked) },
            onSave = { text ->
                onIntent(BookSessionIntent.OnAnnotationTextChanged(text))
                onIntent(BookSessionIntent.OnDismissAnnotationDialogClicked)
            },
        )
    }

    if (uiState.showPageDialog) {
        UpdatePagesDialog(
            currentPage = uiState.currentPage,
            totalPages = uiState.totalPages,
            input = uiState.pageDialogInput,
            onInputChanged = { onIntent(BookSessionIntent.OnPageDialogInputChanged(it)) },
            onDismiss = { onIntent(BookSessionIntent.OnDismissPageDialogClicked) },
            onConfirm = { onIntent(BookSessionIntent.OnConfirmPageDialogClicked) },
        )
    }
}

private fun formatElapsedTime(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun formatSessionDate(millis: Long): String =
    SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(millis))

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = color_primary)
        Text(
            text = stringResource(R.string.book_session_loading),
            modifier = Modifier.padding(top = 16.dp),
            fontSize = 14.sp,
            color = color_on_surface_variant,
        )
    }
}

@Composable
private fun ErrorContent(
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.book_session_error_load),
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            color = color_on_surface,
            textAlign = TextAlign.Center,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TextButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColors(contentColor = color_on_surface_variant),
            ) {
                Text(stringResource(R.string.book_session_back_content_description))
            }
            Button(
                onClick = onRetry,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = color_primary,
                    contentColor = color_surface,
                ),
            ) {
                Text(stringResource(R.string.book_session_retry))
            }
        }
    }
}

@Composable
private fun SessionStatusLabel(status: BookSessionStatus) {
    val statusLabel = when (status) {
        BookSessionStatus.NotStarted -> stringResource(R.string.book_session_status_ready)
        BookSessionStatus.Reading -> stringResource(R.string.book_session_status_reading)
        BookSessionStatus.Paused -> stringResource(R.string.book_session_status_paused)
    }
    Row(
        modifier = Modifier.padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(
                    if (status == BookSessionStatus.Reading) color_primary else color_on_surface_variant,
                ),
        )
        Text(
            text = statusLabel,
            modifier = Modifier.padding(start = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color_on_surface_variant,
        )
    }
}

@Composable
private fun SessionHeader(
    bookTitle: String,
    sessionDateLabel: String,
    currentPage: Int,
    pendingPages: Int,
    totalPages: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 24.dp, end = 24.dp),
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
                contentDescription = stringResource(R.string.book_session_back_content_description),
                tint = color_on_surface,
                modifier = Modifier.size(18.dp),
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bookTitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = color_on_surface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = sessionDateLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = color_on_surface_variant,
                    maxLines = 1,
                )
            }
            if (totalPages > 0) {
                val displayPage = (currentPage + pendingPages).coerceAtMost(totalPages)
                Text(
                    text = stringResource(R.string.book_session_page_progress, displayPage, totalPages),
                    modifier = Modifier.padding(start = 16.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = color_on_surface_variant,
                )
            }
        }
    }
}

@Composable
private fun SessionModeSelector(
    selectedMode: BookSessionMode,
    onModeSelected: (BookSessionMode) -> Unit,
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
                    elevation = if (selectedMode == BookSessionMode.Timer) 6.dp else 0.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = color_on_surface.copy(alpha = 0.08f),
                    spotColor = color_on_surface.copy(alpha = 0.08f),
                )
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (selectedMode == BookSessionMode.Timer) color_surface_variant else color_transparent
                )
                .clickable { onModeSelected(BookSessionMode.Timer) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.book_session_tab_timer),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedMode == BookSessionMode.Timer) color_on_surface else color_on_surface_variant,
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (selectedMode == BookSessionMode.Manual) color_surface_variant else color_transparent
                )
                .shadow(
                    elevation = if (selectedMode == BookSessionMode.Manual) 6.dp else 0.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = color_on_surface.copy(alpha = 0.08f),
                    spotColor = color_on_surface.copy(alpha = 0.08f),
                )
                .clickable { onModeSelected(BookSessionMode.Manual) },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StopwatchIcon(
                tint = if (selectedMode == BookSessionMode.Manual) color_on_surface else color_on_surface_variant,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.book_session_tab_manual),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedMode == BookSessionMode.Manual) color_on_surface else color_on_surface_variant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualTimeEntryContent(
    hours: Int,
    minutes: Int,
    dateMillis: Long,
    onHoursChanged: (String) -> Unit,
    onMinutesChanged: (String) -> Unit,
    onDateChanged: (Long) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.book_session_manual_time_duration_label).uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            color = color_on_surface_variant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ManualTimeUnitBox(
                value = hours.toString().padStart(2, '0'),
                onValueChange = onHoursChanged,
                unit = stringResource(R.string.book_session_manual_time_hours_unit),
                modifier = Modifier.weight(1f),
            )
            ManualTimeUnitBox(
                value = minutes.toString().padStart(2, '0'),
                onValueChange = onMinutesChanged,
                unit = stringResource(R.string.book_session_manual_time_minutes_unit),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.book_session_manual_time_date_label).uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            color = color_on_surface_variant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color_surface_variant)
                .clickable { showDatePicker = true }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CalendarIcon(tint = color_on_surface_variant, modifier = Modifier.size(15.dp))
            Text(
                text = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())
                    .format(Date(dateMillis)),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = color_on_surface,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = color_primary, contentColor = color_surface),
        ) {
            Text(
                text = stringResource(R.string.book_session_save_button),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= System.currentTimeMillis()
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onDateChanged(it) }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.book_session_manual_time_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.book_session_manual_time_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun CommentIcon(
    tint: Color,
    modifier: Modifier = Modifier,
    iconSize: Dp = 16.dp,
) {
    Canvas(modifier = modifier.size(iconSize)) {
        val strokeWidth = 1.4.dp.toPx()
        val cornerRadius = size.minDimension * 0.18f

        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.1f, size.height * 0.1f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.8f, size.height * 0.6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = strokeWidth),
        )

        val tailTopY = size.height * 0.7f
        val tailBottomY = size.height * 0.9f
        val tailStartX = size.width * 0.3f
        val tailPointX = size.width * 0.16f
        val tailEndX = size.width * 0.46f

        drawLine(
            color = tint,
            start = Offset(tailStartX, tailTopY),
            end = Offset(tailPointX, tailBottomY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = Offset(tailPointX, tailBottomY),
            end = Offset(tailEndX, tailTopY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun StopwatchIcon(
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.4.dp.toPx()
        val radius = size.minDimension * 0.375f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(
            color = tint,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth),
        )
        drawLine(
            color = tint,
            start = center,
            end = Offset(center.x, center.y - radius * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = center,
            end = Offset(center.x + radius * 0.37f, center.y + radius * 0.23f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.375f, size.height * 0.1f),
            end = Offset(size.width * 0.625f, size.height * 0.1f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun CalendarIcon(
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.3.dp.toPx()
        val cornerRadius = size.minDimension * 0.14f

        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.09f, size.height * 0.19f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.82f, size.height * 0.72f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = strokeWidth),
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.09f, size.height * 0.42f),
            end = Offset(size.width * 0.91f, size.height * 0.42f),
            strokeWidth = strokeWidth,
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.28f, size.height * 0.1f),
            end = Offset(size.width * 0.28f, size.height * 0.27f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.72f, size.height * 0.1f),
            end = Offset(size.width * 0.72f, size.height * 0.27f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun SessionWaveform(modifier: Modifier = Modifier) {
    val bars = listOf(8.dp to 0.5f, 16.dp to 0.7f, 10.dp to 0.5f, 20.dp to 1f, 12.dp to 0.6f)
    Row(
        modifier = modifier.height(20.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        bars.forEach { (barHeight, alpha) ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color_primary.copy(alpha = alpha)),
            )
        }
    }
}

@Composable
private fun SessionActionButton(
    label: String,
    buttonSize: androidx.compose.ui.unit.Dp,
    containerColor: Color,
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    onClick: () -> Unit = {},
    icon: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(buttonSize)
                .let {
                    if (elevated) {
                        it.shadow(elevation = 14.dp, shape = CircleShape, ambientColor = color_primary, spotColor = color_primary)
                    } else {
                        it
                    }
                }
                .clip(CircleShape)
                .background(containerColor)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = color_on_surface_variant,
        )
    }
}

@Composable
private fun SessionActionsSheet(
    annotationText: String,
    onOpenAnnotationDialogClick: () -> Unit,
    onOpenPageDialogClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 20.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(color_surface_variant)
            .navigationBarsPadding()
            .padding(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 26.dp),
    ) {
        val hasAnnotation = annotationText.isNotBlank()
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            val updatePagesContentDescription =
                stringResource(R.string.book_session_update_pages_content_description)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.55f))
                        .clickable(onClick = onOpenPageDialogClick)
                        .semantics { contentDescription = updatePagesContentDescription },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = color_on_surface,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.book_session_pages_action),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = color_on_surface_variant,
                )
            }

            val commentContentDescription =
                stringResource(R.string.book_session_comment_content_description)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.55f))
                        .clickable(onClick = onOpenAnnotationDialogClick)
                        .semantics { contentDescription = commentContentDescription },
                    contentAlignment = Alignment.Center,
                ) {
                    CommentIcon(
                        tint = if (hasAnnotation) color_primary else color_on_surface,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.book_session_note_action),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = color_on_surface_variant,
                )
            }
        }
    }
}

@Composable
private fun EndSessionDialog(
    title: String,
    message: String,
    onDiscard: () -> Unit,
    onSaveAndFinish: () -> Unit,
    onKeepReading: () -> Unit,
) {
    Dialog(onDismissRequest = onKeepReading) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = color_surface,
            modifier = Modifier.widthIn(max = 320.dp),
        ) {
            Column(
                modifier = Modifier.padding(start = 24.dp, top = 28.dp, end = 24.dp, bottom = 22.dp),
            ) {
                Text(
                    text = title,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = color_on_surface,
                )
                Text(
                    text = message,
                    modifier = Modifier.padding(top = 18.dp),
                    fontSize = 14.sp,
                    color = color_on_surface_variant,
                    lineHeight = 20.sp,
                )
                Button(
                    onClick = onSaveAndFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = color_primary,
                        contentColor = color_surface,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.book_session_save_and_finish),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                TextButton(
                    onClick = onKeepReading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = color_on_surface_variant),
                ) {
                    Text(
                        text = stringResource(R.string.book_session_end_dialog_keep_reading),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                TextButton(
                    onClick = onDiscard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = color_on_surface_variant),
                ) {
                    Text(
                        text = stringResource(R.string.book_session_discard_session),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun AnnotationDialog(
    initialText: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var text by remember { mutableStateOf(initialText) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = color_surface,
            modifier = Modifier.widthIn(max = 320.dp),
        ) {
            Column(
                modifier = Modifier.padding(start = 24.dp, top = 26.dp, end = 24.dp, bottom = 22.dp),
            ) {
                Text(
                    text = stringResource(R.string.book_session_annotation_dialog_title),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 19.sp,
                    color = color_on_surface,
                )

                Spacer(modifier = Modifier.height(18.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(color_surface_variant)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.TopStart,
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.book_session_annotation_dialog_placeholder),
                            fontSize = 13.sp,
                            color = color_placeholder,
                        )
                    }
                    BasicTextField(
                        value = text,
                        onValueChange = { newText ->
                            if (newText.count { it == '\n' } < MAX_ANNOTATION_LINES) {
                                text = newText
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = MAX_ANNOTATION_LINES,
                        textStyle = TextStyle(fontSize = 13.sp, color = color_on_surface),
                        cursorBrush = SolidColor(color_on_surface),
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = color_on_surface_variant),
                    ) {
                        Text(
                            text = stringResource(R.string.book_session_annotation_dialog_cancel),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Button(
                        onClick = { onSave(text) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = color_primary, contentColor = color_surface),
                    ) {
                        Text(
                            text = stringResource(R.string.book_session_annotation_dialog_save),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

private const val MAX_ANNOTATION_LINES = 3

@Composable
private fun UpdatePagesDialog(
    currentPage: Int,
    totalPages: Int,
    input: String,
    onInputChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = color_surface,
            modifier = Modifier.widthIn(max = 320.dp),
        ) {
            Column(
                modifier = Modifier.padding(start = 24.dp, top = 26.dp, end = 24.dp, bottom = 22.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(color_chip),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = color_primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = stringResource(R.string.book_session_update_pages_dialog_title),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 19.sp,
                        color = color_on_surface,
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = stringResource(R.string.book_session_update_pages_current_label, currentPage),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = color_on_surface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.book_session_update_pages_total_label, totalPages),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = color_on_surface,
                )

                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = stringResource(R.string.book_session_update_pages_input_label).uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    color = color_on_surface_variant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(color_surface_variant)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicTextField(
                        value = input,
                        onValueChange = onInputChanged,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = color_on_surface,
                        ),
                        cursorBrush = SolidColor(color_on_surface),
                        decorationBox = { innerTextField ->
                            Box {
                                if (input.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.book_session_update_pages_input_placeholder),
                                        fontSize = 15.sp,
                                        color = color_placeholder,
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = color_on_surface_variant),
                    ) {
                        Text(
                            text = stringResource(R.string.book_session_update_pages_cancel),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = color_primary, contentColor = color_surface),
                    ) {
                        Text(
                            text = stringResource(R.string.book_session_update_pages_accept),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualTimeUnitBox(
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color_surface_variant)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.width(30.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = color_on_surface,
                textAlign = TextAlign.Center,
            ),
            cursorBrush = SolidColor(color_on_surface),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = unit,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color_on_surface_variant,
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun BookSessionScreenPreview() {
    ReadLogTheme {
        BookSessionScreen(uiState = BookSessionUiState(bookTitle = "Cien años de soledad"))
    }
}
