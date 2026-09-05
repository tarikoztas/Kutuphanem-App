package com.tarik.kutuphanem.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tarik.kutuphanem.core.AppResult
import com.tarik.kutuphanem.domain.model.Kitap
import com.tarik.kutuphanem.ui.components.KapakGorseli
import com.tarik.kutuphanem.ui.components.OkunduRozeti
import com.tarik.kutuphanem.ui.components.OnayDiyalogu
import com.tarik.kutuphanem.ui.components.YuklemeKutusu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitapDetayEkrani(
    onGeri: () -> Unit,
    onDuzenle: (String) -> Unit,
    viewModel: KitapDetayViewModel = hiltViewModel()
) {
    val durum by viewModel.durum.collectAsStateWithLifecycle()
    val kapakBase64 by viewModel.kapakBase64.collectAsStateWithLifecycle()
    val mesaj by viewModel.mesaj.collectAsStateWithLifecycle()
    val silindi by viewModel.silindi.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var silmeOnayi by remember { mutableStateOf(false) }

    LaunchedEffect(silindi) {
        if (silindi) onGeri()
    }

    LaunchedEffect(mesaj) {
        mesaj?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mesajGosterildi()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kitap") },
                navigationIcon = {
                    IconButton(onClick = onGeri) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { onDuzenle(viewModel.kitapId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Düzenle")
                    }
                    IconButton(onClick = { silmeOnayi = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Sil")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val d = durum) {
                is AppResult.Loading -> YuklemeKutusu()

                is AppResult.Error -> Text(
                    text = d.message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp)
                )

                is AppResult.Success -> KitapIcerigi(
                    kitap = d.data,
                    kapakBase64 = kapakBase64,
                    onOkunduDegistir = { viewModel.okunduDegistir(d.data) }
                )
            }
        }
    }

    if (silmeOnayi) {
        OnayDiyalogu(
            baslik = "Kitap silinsin mi?",
            metin = "Bu kitap kütüphaneden kalıcı olarak silinecek.",
            onOnayla = { viewModel.sil() },
            onKapat = { silmeOnayi = false }
        )
    }
}

@Composable
private fun KitapIcerigi(
    kitap: Kitap,
    kapakBase64: String?,
    onOkunduDegistir: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row {
            KapakGorseli(
                kapakUrl = kitap.kapakUrl,
                kapakBase64 = kapakBase64,
                baslik = kitap.baslik,
                modifier = Modifier
                    .width(140.dp)
                    .height(206.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(text = kitap.baslik, style = MaterialTheme.typography.titleLarge)

                if (kitap.yazarMetni.isNotBlank()) {
                    Text(
                        text = kitap.yazarMetni,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                OkunduRozeti(
                    okundu = kitap.okundu,
                    onDegistir = onOkunduDegistir,
                    modifier = Modifier.padding(top = 12.dp)
                )
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
            Text(
                text = kitap.aciklama,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun BilgiSatiri(etiket: String, deger: String) {
    if (deger.isBlank()) return

    Row(modifier = Modifier.padding(vertical = 5.dp)) {
        Text(
            text = etiket,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(text = deger, style = MaterialTheme.typography.bodyMedium)
    }
}
