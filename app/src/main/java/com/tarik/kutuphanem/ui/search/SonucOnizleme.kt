package com.tarik.kutuphanem.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.tarik.kutuphanem.domain.model.Kitap
import com.tarik.kutuphanem.ui.components.KapakGorseli

/**
 * Arama sonucunun detay onizlemesi.
 *
 * Kitap henuz kutuphanede olmadigi icin normal detay ekrani (Firestore'dan okuyor)
 * kullanilamiyor; bu yuzden ayni bilgileri gosteren bir alt sayfa acilir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SonucOnizleme(
    kitap: Kitap,
    ekleniyor: Boolean,
    onEkle: () -> Unit,
    onKapat: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onKapat,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row {
                KapakGorseli(
                    kapakUrl = kitap.kapakUrl,
                    kapakBase64 = null,
                    baslik = kitap.baslik,
                    modifier = Modifier
                        .width(120.dp)
                        .height(176.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text(text = kitap.baslik, style = MaterialTheme.typography.titleLarge)

                    if (kitap.yazarMetni.isNotBlank()) {
                        Text(
                            text = kitap.yazarMetni,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            BilgiSatiri("Yayınevi", kitap.yayinevi)
            BilgiSatiri("Sayfa sayısı", kitap.sayfaSayisi?.toString().orEmpty())
            BilgiSatiri("ISBN", kitap.isbn)

            if (kitap.aciklama.isNotBlank()) {
                Text(
                    text = "Açıklama",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
                Text(text = kitap.aciklama, style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(
                    text = "Bu kitap için açıklama bulunamadı.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Button(
                onClick = onEkle,
                enabled = !ekleniyor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .height(52.dp)
            ) {
                if (ekleniyor) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Rafa ekle", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun BilgiSatiri(etiket: String, deger: String) {
    if (deger.isBlank()) return

    Row(
        modifier = Modifier.padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = etiket,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(text = deger, style = MaterialTheme.typography.bodyMedium)
    }
}
