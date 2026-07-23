package com.rodrigonovoa.readlog.ui.bookcollection

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.rodrigonovoa.readlog.ui.common.ConfirmationDialog
import com.rodrigonovoa.readlog.ui.common.UsernameSetupDialog
import com.rodrigonovoa.readlog.ui.theme.ReadLogTheme
import com.rodrigonovoa.readlog.ui.theme.color_chip
import com.rodrigonovoa.readlog.ui.theme.color_error_container
import com.rodrigonovoa.readlog.ui.theme.color_on_surface
import com.rodrigonovoa.readlog.ui.theme.color_on_surface_variant
import com.rodrigonovoa.readlog.ui.theme.color_primary
import com.rodrigonovoa.readlog.ui.theme.color_secondary
import com.rodrigonovoa.readlog.ui.theme.color_surface
import com.rodrigonovoa.readlog.ui.theme.color_surface_variant
import com.rodrigonovoa.readlog.ui.theme.color_track
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val addedOnDateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

private fun formatMillis(format: SimpleDateFormat, millis: Long): String = format.format(Date(millis))

@Composable
fun BookCollectionScreen(
    uiState: BookCollectionUiState,
    modifier: Modifier = Modifier,
    onAddBookClick: () -> Unit = {},
    onEditIconClick: (Int) -> Unit = {},
    onDeleteIconClick: (Int) -> Unit = {},
    onSessionClick: (Int) -> Unit = {},
    onBookClick: (Int) -> Unit = {},
    onDismissDialog: () -> Unit = {},
    onConfirmEdit: (Int) -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onUsernameChange: (String) -> Unit = {},
    onUsernameConfirm: () -> Unit = {},
) {
    val books = uiState.books
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color_surface)
            .safeDrawingPadding(),
    ) {
        HeaderSection(
            showTitle = books.isNotEmpty(),
            greeting = if (uiState.greetingResId != 0) {
                stringResource(uiState.greetingResId, uiState.userName)
            } else {
                ""
            },
            onProfileClick = onProfileClick,
            onSearchClick = onSearchClick,
        )

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
                    BookCard(
                        book = book,
                        onEditClick = { onEditIconClick(book.bookId) },
                        onDeleteClick = { onDeleteIconClick(book.bookId) },
                        onSessionClick = { onSessionClick(book.bookId) },
                        onCardClick = { onBookClick(book.bookId) },
                    )
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

    uiState.activeDialog?.let { dialog ->
        when (dialog.type) {
            BookDialogType.EDIT -> EditBookDialog(
                bookTitle = dialog.bookTitle,
                onDismiss = onDismissDialog,
                onConfirm = { onConfirmEdit(dialog.bookId) },
            )
            BookDialogType.DELETE -> DeleteBookDialog(
                bookTitle = dialog.bookTitle,
                onDismiss = onDismissDialog,
                onConfirm = onConfirmDelete,
            )
        }
    }

    uiState.usernameSetup?.let { usernameSetupState ->
        UsernameSetupDialog(
            state = usernameSetupState,
            onUsernameChange = onUsernameChange,
            onConfirm = onUsernameConfirm,
        )
    }
}

@Composable
private fun HeaderSection(
    showTitle: Boolean = true,
    greeting: String,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
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
                text = greeting,
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

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color_chip)
                    .clickable(onClick = onSearchClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(
                        R.string.book_collection_search_icon_content_description
                    ),
                    tint = color_on_surface_variant,
                    modifier = Modifier.size(20.dp),
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color_chip)
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(
                        R.string.book_collection_profile_icon_content_description
                    ),
                    tint = color_on_surface_variant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun BookCard(
    book: Book,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSessionClick: () -> Unit,
    onCardClick: () -> Unit,
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
            .clickable(onClick = onCardClick)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Info
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(
                    R.string.book_collection_added_on,
                    formatMillis(addedOnDateFormat, book.creationDate),
                ),
                fontSize = 9.sp,
                color = color_on_surface_variant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = book.title,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = color_on_surface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = book.author,
                fontSize = 13.sp,
                color = color_on_surface_variant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
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

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(BookRowActionButtonSize)
                    .clip(CircleShape)
                    .background(color_chip)
                    .clickable(onClick = onEditClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(
                        R.string.book_collection_edit_icon_content_description
                    ),
                    tint = color_primary,
                    modifier = Modifier.size(12.dp),
                )
            }
            Box(
                modifier = Modifier
                    .size(BookRowActionButtonSize)
                    .clip(CircleShape)
                    .background(color_error_container)
                    .clickable(onClick = onDeleteClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(
                        R.string.book_collection_delete_icon_content_description
                    ),
                    tint = color_primary,
                    modifier = Modifier.size(12.dp),
                )
            }

            // Clock action button
            Box(
                modifier = Modifier
                    .size(BookRowActionButtonSize)
                    .clip(CircleShape)
                    .background(color_primary)
                    .clickable(onClick = onSessionClick),
                contentAlignment = Alignment.Center,
            ) {
                ClockIcon(tint = color_surface, iconSize = 16.dp)
            }
        }
    }
}

private val BookRowActionButtonSize = 32.dp

private fun bookColor(bookId: Int): Color {
    return when (bookId % 3) {
        0 -> color_primary
        1 -> color_secondary
        else -> color_on_surface_variant
    }
}

@Composable
private fun EditBookDialog(
    bookTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    ConfirmationDialog(
        icon = Icons.Default.Edit,
        iconContainerColor = color_chip,
        title = stringResource(R.string.book_collection_edit_dialog_title),
        message = stringResource(R.string.book_collection_edit_dialog_message, bookTitle),
        confirmLabel = stringResource(R.string.book_collection_edit),
        dismissLabel = stringResource(R.string.book_collection_cancel),
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
private fun DeleteBookDialog(
    bookTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    ConfirmationDialog(
        icon = Icons.Default.Delete,
        iconContainerColor = color_error_container,
        title = stringResource(R.string.book_collection_delete_dialog_title),
        message = stringResource(R.string.book_collection_delete_dialog_message, bookTitle),
        confirmLabel = stringResource(R.string.book_collection_delete),
        dismissLabel = stringResource(R.string.book_collection_cancel),
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
private fun ClockIcon(
    tint: Color,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp,
) {
    androidx.compose.foundation.Canvas(
        modifier = modifier.size(iconSize),
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
        BookCollectionScreen(
            uiState = BookCollectionUiState(
                books = previewBooks,
                greetingResId = R.string.book_collection_greeting_afternoon,
                userName = "George",
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun BookCollectionScreenEmptyPreview() {
    ReadLogTheme {
        BookCollectionScreen(
            uiState = BookCollectionUiState(
                greetingResId = R.string.book_collection_greeting_afternoon,
                userName = "George",
            )
        )
    }
}
