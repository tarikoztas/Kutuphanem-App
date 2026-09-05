package com.tarik.kutuphanem.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tarik.kutuphanem.core.base64ToBitmap

/**
 * Kapak kaynak sirasi: API adresi -> cihazda cekilmis kapak (base64) -> yer tutucu.
 * Base64 kapak yalnizca ihtiyac duyulan kartlar icin yuklenir (bkz. KitapListesiViewModel).
 */
@Composable
fun KapakGorseli(
    kapakUrl: String,
    kapakBase64: String?,
    baslik: String,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(kapakBase64) { kapakBase64?.base64ToBitmap() }

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when {
            kapakUrl.isNotBlank() -> AsyncImage(
                model = kapakUrl,
                contentDescription = baslik,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            bitmap != null -> Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = baslik,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            else -> YerTutucuKapak(baslik)
        }
    }
}

@Composable
private fun YerTutucuKapak(baslik: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.fillMaxSize(0.4f)
        )
        Text(
            text = baslik,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(6.dp)
        )
    }
}
