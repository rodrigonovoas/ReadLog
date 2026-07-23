package com.rodrigonovoa.readlog.ui.userprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodrigonovoa.readlog.ui.R
import com.rodrigonovoa.readlog.ui.theme.ReadLogTheme
import com.rodrigonovoa.readlog.ui.theme.color_chip
import com.rodrigonovoa.readlog.ui.theme.color_error
import com.rodrigonovoa.readlog.ui.theme.color_on_primary_container
import com.rodrigonovoa.readlog.ui.theme.color_on_surface
import com.rodrigonovoa.readlog.ui.theme.color_on_surface_variant
import com.rodrigonovoa.readlog.ui.theme.color_outline
import com.rodrigonovoa.readlog.ui.theme.color_primary
import com.rodrigonovoa.readlog.ui.theme.color_secondary
import com.rodrigonovoa.readlog.ui.theme.color_surface
import com.rodrigonovoa.readlog.ui.theme.color_surface_variant
import java.util.Locale

@Composable
fun UserProfileScreen(
    uiState: UserProfileUiState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
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
                .padding(top = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(26.dp),
        ) {
            AvatarSection(uiState = uiState, onLikeClick = onLikeClick)
            StatsCard(uiState = uiState)
            WeeklyStatsRow(uiState = uiState)
            CollectionSection(uiState = uiState)
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
        horizontalArrangement = Arrangement.SpaceBetween,
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
                contentDescription = stringResource(R.string.user_profile_back_content_description),
                tint = color_on_surface,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = stringResource(R.string.user_profile_title),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = color_on_surface,
        )
        Spacer(modifier = Modifier.size(36.dp))
    }
}

@Composable
private fun AvatarSection(
    uiState: UserProfileUiState,
    modifier: Modifier = Modifier,
    onLikeClick: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = CircleShape,
                    ambientColor = color_secondary,
                    spotColor = color_secondary,
                )
                .clip(CircleShape)
                .background(color_secondary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = color_surface,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = uiState.userName,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = color_on_surface,
            )
            if (!uiState.isOwnProfile) {
                IconButton(
                    onClick = onLikeClick,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = if (uiState.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(
                            if (uiState.isLiked) {
                                R.string.user_profile_unfavorite_content_description
                            } else {
                                R.string.user_profile_favorite_content_description
                            },
                        ),
                        tint = if (uiState.isLiked) color_error else color_on_surface_variant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        Text(
            text = uiState.username,
            fontSize = 13.sp,
            color = color_on_surface_variant,
            modifier = Modifier.padding(top = 2.dp),
        )
        if (uiState.hasLikeError) {
            Text(
                text = stringResource(R.string.user_profile_like_error_message),
                fontSize = 11.sp,
                color = color_error,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun StatsCard(
    uiState: UserProfileUiState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(color_surface_variant)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatItem(
            value = uiState.likesCount,
            label = stringResource(R.string.user_profile_likes_label),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatItem(
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = String.format(Locale.getDefault(), "%,d", value),
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 19.sp,
            color = color_on_surface,
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color_on_surface_variant,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun WeeklyStatsRow(
    uiState: UserProfileUiState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        WeeklyStatCard(
            label = stringResource(R.string.user_profile_sessions_this_week_label),
            value = uiState.weeklySessionsCount.toString(),
            containerColor = color_primary,
            labelColor = color_on_primary_container,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
        WeeklyStatCard(
            label = stringResource(R.string.user_profile_time_this_week_label),
            value = uiState.weeklyTimeLabel,
            containerColor = color_on_surface,
            labelColor = color_outline,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun WeeklyStatCard(
    label: String,
    value: String,
    containerColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .padding(16.dp),
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = labelColor,
        )
        Text(
            text = value,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            color = color_surface,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun CollectionSection(
    uiState: UserProfileUiState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.user_profile_collection_title),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = color_on_surface,
            )
            Text(
                text = stringResource(
                    R.string.user_profile_collection_count,
                    uiState.collectionBooks.size,
                ),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = color_on_surface_variant,
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(uiState.collectionBooks) { index, book ->
                MiniBookSpine(
                    title = book.title,
                    color = miniBookSpineColor(index),
                    modifier = Modifier
                        .width(90.dp)
                        .height(150.dp),
                )
            }
        }
    }
}

@Composable
private fun MiniBookSpine(
    title: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(color)
            .border(
                width = 1.dp,
                color = color_on_surface.copy(alpha = 0.12f),
                shape = RoundedCornerShape(2.dp),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(color_surface.copy(alpha = 0.55f)),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(color_surface.copy(alpha = 0.35f)),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(44.dp)
                .clip(CircleShape)
                .background(color_on_surface.copy(alpha = 0.18f)),
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color_chip)
                .padding(horizontal = 3.dp, vertical = 5.dp),
        ) {
            Text(
                text = title,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 7.5.sp,
                lineHeight = 9.sp,
                color = color_on_surface,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(color_surface.copy(alpha = 0.35f)),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(color_surface.copy(alpha = 0.55f)),
            )
        }
    }
}

private fun miniBookSpineColor(index: Int): Color {
    return when (index % 3) {
        0 -> color_primary
        1 -> color_secondary
        else -> color_on_surface_variant
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun UserProfileScreenPreview() {
    ReadLogTheme {
        UserProfileScreen(uiState = sampleUserProfileUiState)
    }
}
