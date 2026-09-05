package com.tarik.kutuphanem.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tarik.kutuphanem.ui.theme.LocalDurumRenkleri

/**
 * Tek dokunusla okundu/okunmadi degistiren rozet.
 * Yesil = okundu, kirmizi = okunmadi. Renkler temaya gore LocalDurumRenkleri'nden gelir.
 */
@Composable
fun OkunduRozeti(
    okundu: Boolean,
    onDegistir: () -> Unit,
    modifier: Modifier = Modifier,
    kisa: Boolean = false
) {
    val renkler = LocalDurumRenkleri.current
    val icerik = if (okundu) renkler.okunduIcerik else renkler.okunmadiIcerik
    val kap = if (okundu) renkler.okunduKap else renkler.okunmadiKap
    val metin = if (okundu) "Okundu" else "Okunmadı"

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(kap)
            .clickable(onClick = onDegistir)
            .padding(horizontal = if (kisa) 8.dp else 10.dp, vertical = 5.dp)
            .semantics { contentDescription = "$metin. Değiştirmek için dokun." },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (okundu) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = icerik,
            modifier = Modifier.size(if (kisa) 14.dp else 16.dp)
        )
        if (!kisa) {
            Text(
                text = metin,
                style = MaterialTheme.typography.labelSmall,
                color = icerik,
                modifier = Modifier.padding(start = 5.dp)
            )
        }
    }
}
