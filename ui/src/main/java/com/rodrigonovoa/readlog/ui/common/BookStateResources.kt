package com.rodrigonovoa.readlog.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodrigonovoa.readlog.domain.model.BookState
import com.rodrigonovoa.readlog.ui.R
import com.rodrigonovoa.readlog.ui.theme.color_chip
import com.rodrigonovoa.readlog.ui.theme.color_on_surface

@StringRes
fun bookStateStringRes(state: BookState): Int = when (state) {
    BookState.IN_PROGRESS -> R.string.book_state_in_progress
    BookState.COMPLETED -> R.string.book_state_completed
    BookState.DROPPED -> R.string.book_state_dropped
    BookState.PAUSED -> R.string.book_state_paused
}

@Composable
fun BookStateChip(
    state: BookState,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(bookStateStringRes(state)),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = color_on_surface,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color_chip)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}
