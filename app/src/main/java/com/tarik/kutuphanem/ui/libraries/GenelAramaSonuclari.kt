package com.tarik.kutuphanem.ui.libraries

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tarik.kutuphanem.domain.model.KutuphaneKitabi
import com.tarik.kutuphanem.ui.components.BosDurum
import com.tarik.kutuphanem.ui.components.KapakGorseli
import com.tarik.kutuphanem.ui.components.OkunduRozeti

/** Tum kutuphanelerde yapilan aramanin sonuc listesi; her satir kitabin kutuphanesini de gosterir. */
@Composable
fun GenelAramaSonuclari(
    arama: String,
    sonuclar: List<KutuphaneKitabi>,
    kapaklar: Map<String, String>,
    onKapakIste: (kutuphaneId: String, kitapId: String) -> Unit,
    onKitapAc: (KutuphaneKitabi) -> Unit,
    onOkunduDegistir: (KutuphaneKitabi) -> Unit,
    modifier: Modifier = Modifier
) {
    if (sonuclar.isEmpty()) {
        BosDurum(
            ikon = Icons.Default.Search,
            baslik = "Sonuç yok",
            aciklama = "\"$arama\" ile eşleşen kitap hiçbir kütüphanede bulunamadı.",
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "${sonuclar.size} sonuç",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(sonuclar, key = { "${it.kutuphaneId}/${it.kitap.id}" }) { sonuc ->
            SonucSatiri(
                sonuc = sonuc,
                kapakBase64 = kapaklar["${sonuc.kutuphaneId}/${sonuc.kitap.id}"],
                onKapakIste = onKapakIste,
                onTikla = { onKitapAc(sonuc) },
                onOkunduDegistir = { onOkunduDegistir(sonuc) }
            )
        }
    }
}

@Composable
private fun SonucSatiri(
    sonuc: KutuphaneKitabi,
    kapakBase64: String?,
    onKapakIste: (String, String) -> Unit,
    onTikla: () -> Unit,
    onOkunduDegistir: () -> Unit
) {
    val kitap = sonuc.kitap

    LaunchedEffect(sonuc.kutuphaneId, kitap.id, kitap.yerelKapakVar) {
        if (kitap.yerelKapakVar && kitap.kapakUrl.isBlank() && kapakBase64 == null) {
            onKapakIste(sonuc.kutuphaneId, kitap.id)
        }
    }

    Card(
        onClick = onTikla,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            KapakGorseli(
                kapakUrl = kitap.kapakUrl,
                kapakBase64 = kapakBase64,
                baslik = kitap.baslik,
                modifier = Modifier
                    .width(58.dp)
                    .height(86.dp)
                    .clip(RoundedCornerShape(6.dp))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = kitap.baslik,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (kitap.yazarMetni.isNotBlank()) {
                    Text(
                        text = kitap.yazarMetni,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Genel aramanin asil amaci: kitabin hangi kutuphanede oldugunu gostermek.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LibraryBooks,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = sonuc.kutuphaneAdi,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                OkunduRozeti(
                    okundu = kitap.okundu,
                    onDegistir = onOkunduDegistir,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
