package com.rodrigonovoa.readlog.ui.bookdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodrigonovoa.readlog.ui.R
import com.rodrigonovoa.readlog.ui.theme.ReadLogTheme
import com.rodrigonovoa.readlog.ui.theme.color_chip
import com.rodrigonovoa.readlog.ui.theme.color_on_surface
import com.rodrigonovoa.readlog.ui.theme.color_on_surface_variant
import com.rodrigonovoa.readlog.ui.theme.color_placeholder
import com.rodrigonovoa.readlog.ui.theme.color_primary
import com.rodrigonovoa.readlog.ui.theme.color_secondary
import com.rodrigonovoa.readlog.ui.theme.color_surface
import com.rodrigonovoa.readlog.ui.theme.color_surface_variant
import com.rodrigonovoa.readlog.ui.theme.color_track

@Composable
fun BookDetailScreen(
    modifier: Modifier = Modifier,
    uiState: BookDetailUiState = BookDetailUiState(),
    onBackClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color_surface)
            .safeDrawingPadding(),
    ) {
        TopBar(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            BookHeaderRow(uiState = uiState)
            StatsRow(uiState = uiState)
            MonthCalendar(uiState = uiState)
            RecentSessionsSection(sessions = uiState.recentSessions)
        }
    }
}

@Composable
private fun TopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp),
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
                contentDescription = stringResource(R.string.book_detail_back_content_description),
                tint = color_on_surface,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun BookHeaderRow(
    uiState: BookDetailUiState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(92.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(bookDetailColor(uiState.bookId))
                .padding(horizontal = 6.dp, vertical = 7.dp),
        ) {
            Text(
                text = uiState.bookTitle,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                color = color_surface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = uiState.bookTitle,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 25.sp,
                color = color_on_surface,
            )
            Text(
                text = uiState.bookAuthor,
                fontSize = 14.sp,
                color = color_on_surface_variant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.book_detail_reading_since, uiState.readingSinceLabel),
                fontSize = 12.sp,
                color = color_placeholder,
            )
        }
    }
}

private val StatsRowCompactWidthThreshold = 360.dp

@Composable
private fun StatsRow(
    uiState: BookDetailUiState,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val compact = maxWidth < StatsRowCompactWidthThreshold
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
        ) {
            StatCard(
                label = stringResource(R.string.book_detail_stat_sessions),
                value = uiState.sessionsCount.toString(),
                compact = compact,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = stringResource(R.string.book_detail_stat_total_time),
                value = uiState.totalTimeLabel,
                compact = compact,
                valueFontSize = if (compact) 17.sp else 20.sp,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = stringResource(R.string.book_detail_stat_days),
                value = uiState.daysReadingCount.toString(),
                compact = compact,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    valueFontSize: TextUnit = if (compact) 20.sp else 24.sp,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color_surface_variant)
            .padding(horizontal = if (compact) 8.dp else 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            fontSize = if (compact) 9.5.sp else 11.sp,
            lineHeight = if (compact) 11.sp else 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = color_on_surface_variant,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        AutoSizeText(
            text = value,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            maxFontSize = valueFontSize,
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

@Composable
private fun MonthCalendar(
    uiState: BookDetailUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = uiState.monthLabel,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = color_on_surface,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            uiState.monthDays.chunked(7).forEach { week ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    week.forEach { monthDay ->
                        MonthDayCell(monthDay = monthDay)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthDayCell(
    monthDay: BookDetailMonthDay,
    modifier: Modifier = Modifier,
) {
    val (boxColor, textColor) = when (monthDay.status) {
        BookDetailDayStatus.READ -> color_primary to color_surface
        BookDetailDayStatus.TODAY -> color_secondary to color_surface
        BookDetailDayStatus.NONE -> color_track to color_on_surface_variant
    }
    Box(
        modifier = modifier
            .size(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(boxColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = monthDay.day.toString(),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
        )
    }
}

@Composable
private fun RecentSessionsSection(
    sessions: List<BookDetailSession>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.book_detail_recent_sessions_title),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = color_on_surface,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        if (sessions.isEmpty()) {
            Text(
                text = stringResource(R.string.book_detail_no_sessions),
                fontSize = 13.sp,
                color = color_on_surface_variant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                sessions.forEach { session ->
                    RecentSessionCard(session = session)
                }
            }
        }
    }
}

@Composable
private fun RecentSessionCard(
    session: BookDetailSession,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color_surface_variant)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${session.dateLabel} · ${session.dayLabel}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = color_on_surface,
            )
            Text(
                text = session.durationLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color_primary,
            )
        }
        session.comment?.let { comment ->
            Text(
                text = comment,
                fontSize = 13.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = color_on_surface_variant,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

private fun bookDetailColor(bookId: Int): Color {
    return when (bookId % 3) {
        0 -> color_primary
        1 -> color_secondary
        else -> color_on_surface_variant
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun BookDetailScreenPreview() {
    ReadLogTheme {
        BookDetailScreen(uiState = sampleBookDetailUiState)
    }
}
