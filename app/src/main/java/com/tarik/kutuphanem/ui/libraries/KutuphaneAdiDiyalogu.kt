package com.tarik.kutuphanem.ui.libraries

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization

@Composable
fun KutuphaneAdiDiyalogu(
    baslangicDeger: String,
    duzenlemeMi: Boolean,
    onKaydet: (String) -> Unit,
    onKapat: () -> Unit
) {
    var ad by remember { mutableStateOf(baslangicDeger) }

    AlertDialog(
        onDismissRequest = onKapat,
        title = { Text(if (duzenlemeMi) "Kütüphaneyi yeniden adlandır" else "Yeni kütüphane") },
        text = {
            OutlinedTextField(
                value = ad,
                onValueChange = { ad = it },
                label = { Text("Kütüphane adı") },
                placeholder = { Text("Ev Kitaplığı") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onKaydet(ad.trim())
                    onKapat()
                },
                enabled = ad.isNotBlank()
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onKapat) { Text("İptal") }
        }
    )
}
