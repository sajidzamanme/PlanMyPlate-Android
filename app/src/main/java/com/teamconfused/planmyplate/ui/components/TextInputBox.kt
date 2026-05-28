package com.teamconfused.planmyplate.ui.components

import android.graphics.drawable.Icon
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.teamconfused.planmyplate.R

@Composable
fun InputTextBox(
    modifier: Modifier = Modifier,
    value: String? = null,
    onValueChange: ((String) -> Unit)? = null,
    placeholder: String = "",
    singleLine: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    leadingIcon: Icon? = null,
    showLeadingIcon: Boolean = false,
    showClearButton: Boolean = true,
) {
    // internal state used only when caller doesn't provide a controlled value
    var internal by rememberSaveable { mutableStateOf("") }
    val text = value ?: internal

    fun update(new: String) {
        if (onValueChange != null) onValueChange(new) else internal = new
    }

    OutlinedTextField(
        value = text,
        onValueChange = { update(it) },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
        singleLine = singleLine,
        leadingIcon =
            if (showLeadingIcon && leadingIcon != null) {
                {
                    leadingIcon
                }
            } else null,
        trailingIcon = {
            if (showClearButton && text.isNotEmpty()) {
                IconButton(onClick = { update("") }) {
                    Icon(
                        painter = painterResource(R.drawable.clear_icon),
                        contentDescription = "Clear"
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = imeAction
        ),
        shape = MaterialTheme.shapes.large
    )
}