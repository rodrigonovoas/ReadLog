package com.rodrigonovoa.readlog.ui.bookcollection

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.ui.R
import com.rodrigonovoa.readlog.ui.theme.ReadLogTheme
import com.rodrigonovoa.readlog.ui.theme.color_chip
import com.rodrigonovoa.readlog.ui.theme.color_on_surface
import com.rodrigonovoa.readlog.ui.theme.color_on_surface_variant
import com.rodrigonovoa.readlog.ui.theme.color_primary
import com.rodrigonovoa.readlog.ui.theme.color_secondary
import com.rodrigonovoa.readlog.ui.theme.color_surface
import com.rodrigonovoa.readlog.ui.theme.color_surface_variant
import com.rodrigonovoa.readlog.ui.theme.color_track

@Composable
fun BookCollectionScreen(
    books: List<Book>,
    modifier: Modifier = Modifier,
    onAddBookClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color_surface)
            .safeDrawingPadding(),
    ) {
        HeaderSection(showTitle = books.isNotEmpty())

        if (books.isEmpty()) {
            EmptyCollectionSection(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                items(books, key = { it.bookId }) { book ->
                    BookCard(book = book)
                }
            }
        }

        AddBookButton(
            onClick = onAddBookClick,
            modifier = Modifier
                .padding(bottom = 28.dp, top = 20.dp)
                .align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun HeaderSection(
    showTitle: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = stringResource(R.string.book_collection_greeting),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = color_on_surface_variant,
            )
            if (showTitle) {
                Text(
                    text = stringResource(R.string.book_collection_title),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 26.sp,
                    color = color_on_surface,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color_chip),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = color_on_surface_variant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun BookCard(
    book: Book,
    modifier: Modifier = Modifier,
) {
    val progress = if (book.numPages > 0) {
        book.currentPage.toFloat() / book.numPages.toFloat()
    } else {
        0f
    }
    val color = bookColor(book.bookId)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(color_surface_variant)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Book spine
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color)
                .padding(horizontal = 5.dp, vertical = 6.dp),
        ) {
            Text(
                text = book.title,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 9.sp,
                lineHeight = 12.sp,
                color = color_surface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Info
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = book.title,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = color_on_surface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = book.author,
                fontSize = 13.sp,
                color = color_on_surface_variant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )

            // Progress bar
            Row(
                modifier = Modifier.padding(top = 10.dp),
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
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(
                        R.string.book_collection_progress_pct,
                        (progress * 100).toInt()
                    ),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = color_on_surface_variant,
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Clock action button
        IconButton(
            onClick = { },
            modifier = Modifier
                .size(35.dp)
                .clip(CircleShape)
                .background(color_primary),
        ) {
            ClockIcon(tint = color_surface)
        }
    }
}

private fun bookColor(bookId: Int): Color {
    return when (bookId % 3) {
        0 -> color_primary
        1 -> color_secondary
        else -> color_on_surface_variant
    }
}

@Composable
private fun ClockIcon(
    tint: Color,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.Canvas(
        modifier = modifier.size(20.dp),
    ) {
        val strokeWidth = 1.5.dp.toPx()
        drawCircle(
            color = tint,
            radius = size.minDimension / 2 - strokeWidth / 2,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
        )
        // Hour hand
        drawLine(
            color = tint,
            start = center,
            end = center.copy(y = center.y - size.minDimension * 0.22f),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
        // Minute hand
        drawLine(
            color = tint,
            start = center,
            end = center.copy(x = center.x + size.minDimension * 0.15f, y = center.y + size.minDimension * 0.09f),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
    }
}

@Composable
private fun AddBookButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color_chip,
            contentColor = color_on_surface,
        ),
        contentPadding = PaddingValues(horizontal = 26.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.book_collection_add_book),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun EmptyCollectionSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_empty_collection),
            contentDescription = null,
            modifier = Modifier.size(100.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.book_collection_empty_title),
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            color = color_on_surface,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.book_collection_empty_subtitle),
            fontSize = 14.sp,
            color = color_on_surface_variant,
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun BookCollectionScreenPreview() {
    val previewBooks = listOf(
        Book(
            bookId = 1,
            title = "Cien años de soledad",
            author = "Gabriel García Márquez",
            genre = "Novel",
            releaseDate = "1967",
            numPages = 340,
            currentPage = 231,
        ),
        Book(
            bookId = 2,
            title = "Las palabras y las cosas",
            author = "Michel Foucault",
            genre = "Philosophy",
            releaseDate = "1966",
            numPages = 300,
            currentPage = 102,
        ),
        Book(
            bookId = 3,
            title = "El nombre del viento",
            author = "Patrick Rothfuss",
            genre = "Fantasy",
            releaseDate = "2007",
            numPages = 662,
            currentPage = 80,
        ),
    )

    ReadLogTheme {
        BookCollectionScreen(books = previewBooks)
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun BookCollectionScreenEmptyPreview() {
    ReadLogTheme {
        BookCollectionScreen(books = emptyList())
    }
}
