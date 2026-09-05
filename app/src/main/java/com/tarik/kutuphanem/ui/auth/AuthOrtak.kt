package com.tarik.kutuphanem.ui.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun SifreAlani(
    deger: String,
    etiket: String,
    onDegisim: (String) -> Unit,
    imeAction: ImeAction = ImeAction.Done,
    modifier: Modifier = Modifier
) {
    var gorunur by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = deger,
        onValueChange = onDegisim,
        label = { Text(etiket) },
        singleLine = true,
        visualTransformation = if (gorunur) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        trailingIcon = {
            IconButton(onClick = { gorunur = !gorunur }) {
                Icon(
                    imageVector = if (gorunur) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (gorunur) "Şifreyi gizle" else "Şifreyi göster"
                )
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun EpostaAlani(
    deger: String,
    onDegisim: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = deger,
        onValueChange = onDegisim,
        label = { Text("E-posta") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        modifier = modifier.fillMaxWidth()
    )
}
