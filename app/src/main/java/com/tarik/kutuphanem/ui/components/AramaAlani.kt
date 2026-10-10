package com.tarik.kutuphanem.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AramaAlani(
    deger: String,
    onDegisim: (String) -> Unit,
    modifier: Modifier = Modifier,
    ipucu: String = "Kitap, yazar veya yayınevi ara"
) {
    OutlinedTextField(
        value = deger,
        onValueChange = onDegisim,
        placeholder = { Text(ipucu) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (deger.isNotEmpty()) {
                IconButton(onClick = { onDegisim("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Aramayı temizle")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        modifier = modifier.fillMaxWidth()
    )
}
