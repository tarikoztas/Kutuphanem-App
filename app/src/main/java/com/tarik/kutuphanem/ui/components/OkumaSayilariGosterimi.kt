package com.tarik.kutuphanem.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tarik.kutuphanem.domain.model.OkumaSayilari
import com.tarik.kutuphanem.ui.theme.LocalDurumRenkleri

/**
 * Okundu / okunmadi dagilimi.
 *  - ayrintili = false: yesil tik + sayi, kirmizi capraz + sayi (kutuphane kartlari)
 *  - ayrintili = true : "7 okundu  5 okunmadı" (kutuphane ici)
 */
@Composable
fun OkumaSayilariGosterimi(
    sayilar: OkumaSayilari,
    modifier: Modifier = Modifier,
    ayrintili: Boolean = false
) {
    val renkler = LocalDurumRenkleri.current
    val okunduMetin = if (ayrintili) "${sayilar.okunan} okundu" else "${sayilar.okunan}"
    val okunmadiMetin = if (ayrintili) "${sayilar.okunmayan} okunmadı" else "${sayilar.okunmayan}"

    Row(
        modifier = modifier.semantics {
            contentDescription = "${sayilar.okunan} kitap okundu, ${sayilar.okunmayan} kitap okunmadı"
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = renkler.okunduIcerik,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = okunduMetin,
            style = MaterialTheme.typography.labelLarge,
            color = renkler.okunduIcerik,
            modifier = Modifier.padding(start = 3.dp)
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = renkler.okunmadiIcerik,
            modifier = Modifier
                .padding(start = 14.dp)
                .size(16.dp)
        )
        Text(
            text = okunmadiMetin,
            style = MaterialTheme.typography.labelLarge,
            color = renkler.okunmadiIcerik,
            modifier = Modifier.padding(start = 3.dp)
        )
    }
}
