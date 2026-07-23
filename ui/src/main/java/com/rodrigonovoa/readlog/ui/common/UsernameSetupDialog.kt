package com.rodrigonovoa.readlog.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rodrigonovoa.readlog.ui.R
import com.rodrigonovoa.readlog.ui.theme.color_error
import com.rodrigonovoa.readlog.ui.theme.color_on_surface
import com.rodrigonovoa.readlog.ui.theme.color_on_surface_variant
import com.rodrigonovoa.readlog.ui.theme.color_primary
import com.rodrigonovoa.readlog.ui.theme.color_surface

@Composable
fun UsernameSetupDialog(
    state: UsernameSetupState,
    onUsernameChange: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = color_surface,
            modifier = Modifier.widthIn(max = 320.dp),
        ) {
            Column(
                modifier = Modifier.padding(start = 24.dp, top = 28.dp, end = 24.dp, bottom = 22.dp),
            ) {
                Text(
                    text = stringResource(R.string.username_setup_title),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = color_on_surface,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.username_setup_description),
                    fontSize = 14.sp,
                    color = color_on_surface_variant,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(18.dp))
                OutlinedTextField(
                    value = state.username,
                    onValueChange = onUsernameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.username_setup_label)) },
                    singleLine = true,
                    enabled = !state.isChecking,
                    isError = state.errorMessageRes != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = color_on_surface,
                        unfocusedTextColor = color_on_surface,
                        focusedBorderColor = color_primary,
                    ),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "@${state.username}",
                    fontSize = 13.sp,
                    color = color_on_surface_variant,
                )
                state.errorMessageRes?.let { errorRes ->
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(errorRes),
                        fontSize = 13.sp,
                        color = color_error,
                    )
                }
                Spacer(Modifier.height(22.dp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    enabled = !state.isChecking,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = color_primary,
                        contentColor = color_surface,
                    ),
                ) {
                    if (state.isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = color_surface,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.username_setup_confirm),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}
