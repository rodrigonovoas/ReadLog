package com.rodrigonovoa.readlog.ui.booksession

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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
import com.rodrigonovoa.readlog.ui.theme.color_session_background_bottom
import com.rodrigonovoa.readlog.ui.theme.color_session_background_top
import com.rodrigonovoa.readlog.ui.theme.color_surface
import com.rodrigonovoa.readlog.ui.theme.color_surface_variant
import com.rodrigonovoa.readlog.ui.theme.color_track

@Composable
fun BookSessionScreen(
    modifier: Modifier = Modifier,
    bookTitle: String = "Cien años de soledad",
    elapsedTime: String = "24:18",
    onBackClick: () -> Unit = {},
) {
    var isMusicOn by remember { mutableStateOf(true) }

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
            SessionHeader(
                bookTitle = bookTitle,
                isMusicOn = isMusicOn,
                onToggleMusic = { isMusicOn = !isMusicOn },
                onBackClick = onBackClick,
            )

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
                Text(
                    text = elapsedTime,
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
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color_on_surface),
                        )
                    }
                    SessionActionButton(
                        label = stringResource(R.string.book_session_pause),
                        buttonSize = 76.dp,
                        containerColor = color_primary,
                        elevated = true,
                    ) {
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
                    }
                }
            }

            SessionAnnotationsSheet()
        }
    }
}

@Composable
private fun SessionHeader(
    bookTitle: String,
    isMusicOn: Boolean,
    onToggleMusic: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 24.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
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

            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(color_primary),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = bookTitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = color_on_surface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        val musicToggleContentDescription = stringResource(
            if (isMusicOn) {
                R.string.book_session_music_on_content_description
            } else {
                R.string.book_session_music_off_content_description
            }
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.55f))
                .clickable(onClick = onToggleMusic)
                .semantics { contentDescription = musicToggleContentDescription },
            contentAlignment = Alignment.Center,
        ) {
            MusicToggleIcon(
                isOn = isMusicOn,
                tint = if (isMusicOn) color_on_surface else color_on_surface_variant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun MusicToggleIcon(
    isOn: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
    iconSize: Dp = 16.dp,
) {
    Canvas(modifier = modifier.size(iconSize)) {
        val strokeWidth = 1.4.dp.toPx()
        val noteHeadRadiusX = size.minDimension * 0.19f
        val noteHeadRadiusY = size.minDimension * 0.15f
        val headCenter = Offset(x = size.width * 0.36f, y = size.height * 0.78f)
        val stemX = headCenter.x + noteHeadRadiusX * 0.85f
        val stemTopY = size.height * 0.14f

        drawOval(
            color = tint,
            topLeft = Offset(headCenter.x - noteHeadRadiusX, headCenter.y - noteHeadRadiusY),
            size = androidx.compose.ui.geometry.Size(noteHeadRadiusX * 2, noteHeadRadiusY * 2),
        )
        drawLine(
            color = tint,
            start = Offset(stemX, headCenter.y),
            end = Offset(stemX, stemTopY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = Offset(stemX, stemTopY),
            end = Offset(stemX + size.width * 0.28f, stemTopY + size.height * 0.16f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )

        if (!isOn) {
            drawLine(
                color = tint,
                start = Offset(size.width * 0.08f, size.height * 0.08f),
                end = Offset(size.width * 0.92f, size.height * 0.92f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
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
                .background(containerColor),
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
private fun SessionAnnotationsSheet(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 20.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(color_surface_variant)
            .navigationBarsPadding()
            .padding(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 26.dp),
    ) {
        Text(
            text = stringResource(R.string.book_session_annotations_label),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = color_on_surface,
            modifier = Modifier.padding(top = 14.dp, bottom = 10.dp),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(color_surface)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = stringResource(R.string.book_session_annotation_placeholder),
                    fontSize = 13.sp,
                    color = color_placeholder,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color_primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(
                        R.string.book_session_send_annotation_content_description
                    ),
                    tint = color_surface,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun BookSessionScreenPreview() {
    ReadLogTheme {
        BookSessionScreen()
    }
}
