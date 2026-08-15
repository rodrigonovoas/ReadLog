package com.rodrigonovoa.readlog.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rodrigonovoa.readlog.ui.theme.color_on_surface_variant
import com.rodrigonovoa.readlog.ui.theme.color_primary
import com.rodrigonovoa.readlog.ui.theme.color_secondary
import com.rodrigonovoa.readlog.ui.theme.color_surface

@Composable
fun BookCover(
    coverUrl: String,
    title: String,
    author: String,
    bookId: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
) {
    if (coverUrl.isNotEmpty()) {
        AsyncImage(
            model = coverUrl,
            contentDescription = contentDescription,
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp)),
            contentScale = contentScale,
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
                .background(bookCoverColor(bookId)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Top decorative accent lines
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(color_surface.copy(alpha = 0.5f)),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(color_surface.copy(alpha = 0.3f)),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    color = color_surface,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )

                if (author.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = author,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 8.sp,
                        lineHeight = 10.sp,
                        color = color_surface.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom decorative accent line (page edge hint)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(color_surface.copy(alpha = 0.4f)),
                )
            }
        }
    }
}

private fun bookCoverColor(bookId: Int): Color {
    return when (bookId % 3) {
        0 -> color_primary
        1 -> color_secondary
        else -> color_on_surface_variant
    }
}
