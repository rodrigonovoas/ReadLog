package com.rodrigonovoa.readlog.ui.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodrigonovoa.readlog.ui.R
import com.rodrigonovoa.readlog.ui.theme.ReadLogTheme
import com.rodrigonovoa.readlog.ui.theme.color_on_surface
import com.rodrigonovoa.readlog.ui.theme.color_on_surface_variant
import com.rodrigonovoa.readlog.ui.theme.color_primary
import com.rodrigonovoa.readlog.ui.theme.color_secondary
import com.rodrigonovoa.readlog.ui.theme.color_surface

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    state: LoginUiState,
    onIntent: (LoginIntent) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color_surface),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            BookStack()

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.login_brand),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 40.sp,
                    color = color_on_surface,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.login_tagline),
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = color_on_surface_variant,
                    textAlign = TextAlign.Center,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Button(
                    onClick = {
                        onIntent(LoginIntent.OnGoogleSignInClicked)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = color_primary,
                        contentColor = color_surface,
                        disabledContainerColor = color_primary.copy(alpha = 0.5f),
                        disabledContentColor = color_surface.copy(alpha = 0.7f),
                    ),
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = color_surface,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        PersonIcon(
                            color = color_surface,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.login_continue_google),
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                TextButton(
                    onClick = {                     onIntent(LoginIntent.OnContinueOfflineClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(28.dp),
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = color_on_surface_variant,
                        disabledContentColor = color_on_surface_variant.copy(alpha = 0.5f),
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.login_continue_offline),
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                state.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Color(0xFFB00020),
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun BookStack(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Box(
            Modifier
                .rotate(-6f)
                .width(34.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(color_secondary)
        )
        Box(
            Modifier
                .width(34.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(color_primary)
        )
        Box(
            Modifier
                .rotate(6f)
                .width(34.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(color_on_surface_variant)
        )
    }
}

@Composable
private fun PersonIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawPersonIcon(color)
    }
}

private fun DrawScope.drawPersonIcon(color: Color) {
    val scale = size.minDimension / 20f
    drawCircle(
        color = color,
        radius = 4f * scale,
        center = Offset(10f * scale, 7f * scale),
    )
    val path = Path().apply {
        moveTo(2f * scale, 18f * scale)
        cubicTo(
            2f * scale, 13.6f * scale,
            5.6f * scale, 11f * scale,
            10f * scale, 11f * scale,
        )
        cubicTo(
            14.4f * scale, 11f * scale,
            18f * scale, 13.6f * scale,
            18f * scale, 18f * scale,
        )
        close()
    }
    drawPath(path = path, color = color, style = Fill)
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun LoginScreenPreview() {
    ReadLogTheme {
        LoginScreen(
            state = LoginUiState(),
            onIntent = {},
        )
    }
}
