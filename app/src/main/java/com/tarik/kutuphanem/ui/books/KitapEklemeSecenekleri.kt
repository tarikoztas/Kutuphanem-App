package com.tarik.kutuphanem.ui.books

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitapEklemeSecenekleri(
    onBarkod: () -> Unit,
    onIsimleAra: () -> Unit,
    onElle: () -> Unit,
    onKapat: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onKapat) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = "Kitap ekle",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
            )

            Secenek(
                ikon = Icons.Default.QrCodeScanner,
                baslik = "Barkod tara",
                aciklama = "Kitabın arkasındaki barkodu okut, bilgiler otomatik gelsin",
                onTikla = onBarkod
            )
            Secenek(
                ikon = Icons.Default.Search,
                baslik = "İsimle ara",
                aciklama = "Barkod okunmuyorsa kitap adı veya yazarla ara",
                onTikla = onIsimleAra
            )
            Secenek(
                ikon = Icons.Default.Edit,
                baslik = "Elle ekle",
                aciklama = "Bilgileri kendin yaz, kapağı kamerayla çek",
                onTikla = onElle
            )
        }
    }
}

@Composable
private fun Secenek(
    ikon: ImageVector,
    baslik: String,
    aciklama: String,
    onTikla: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTikla)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ikon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(text = baslik, style = MaterialTheme.typography.titleMedium)
            Text(
                text = aciklama,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
