package com.tarik.kutuphanem.ui.manual

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tarik.kutuphanem.core.aramaEslesiyorMu
import com.tarik.kutuphanem.ui.components.KapakGorseli

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitapFormEkrani(
    onGeri: () -> Unit,
    onKaydedildi: () -> Unit,
    onIsimleAra: () -> Unit,
    viewModel: KitapFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    // Gorsel secme hatalari ViewModel'e ugramadan dogrudan burada gosterilir.
    var kapakHatasi by remember { mutableStateOf<String?>(null) }

    val kapakSecici = rememberKapakSecici(
        onKapakHazir = viewModel::kapakSecildi,
        onHata = { kapakHatasi = it }
    )

    LaunchedEffect(kapakHatasi) {
        kapakHatasi?.let {
            snackbarHostState.showSnackbar(it)
            kapakHatasi = null
        }
    }

    LaunchedEffect(state.kaydedildi) {
        if (state.kaydedildi) onKaydedildi()
    }

    LaunchedEffect(state.hata, state.bilgi) {
        val mesaj = state.hata ?: state.bilgi
        if (mesaj != null) {
            snackbarHostState.showSnackbar(mesaj)
            viewModel.mesajGosterildi()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.duzenlemeMi) "Kitabı düzenle" else "Kitap ekle") },
                navigationIcon = {
                    IconButton(onClick = onGeri) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.araniyor) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    text = "Kitap bilgileri aranıyor…",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                KapakBolumu(
                    kapakUrl = state.kapakUrl,
                    kapakBase64 = state.kapakBase64,
                    baslik = state.baslik,
                    onKamera = kapakSecici.kameraIleCek,
                    onGaleri = kapakSecici.galeridenSec,
                    onKaldir = viewModel::kapakKaldirildi
                )

                OutlinedTextField(
                    value = state.baslik,
                    onValueChange = viewModel::baslikDegisti,
                    label = { Text("Kitap adı") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )

                OneriliAlan(
                    deger = state.yazarlar,
                    etiket = "Yazar",
                    ipucu = "Birden fazlaysa virgülle ayır",
                    oneriler = state.oneriler.yazarlar,
                    onDegisim = viewModel::yazarlarDegisti
                )

                OneriliAlan(
                    deger = state.yayinevi,
                    etiket = "Yayınevi",
                    ipucu = null,
                    oneriler = state.oneriler.yayinevleri,
                    onDegisim = viewModel::yayineviDegisti
                )

                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.sayfaSayisi,
                        onValueChange = viewModel::sayfaDegisti,
                        label = { Text("Sayfa") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.width(120.dp)
                    )

                    OutlinedTextField(
                        value = state.isbn,
                        onValueChange = viewModel::isbnDegisti,
                        label = { Text("ISBN") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = viewModel::isbnIleDoldurmayiDene, enabled = !state.araniyor) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("ISBN'den doldur", modifier = Modifier.padding(start = 6.dp))
                    }
                    TextButton(onClick = onIsimleAra) {
                        Text("İsimle ara")
                    }
                }

                OutlinedTextField(
                    value = state.aciklama,
                    onValueChange = viewModel::aciklamaDegisti,
                    label = { Text("Açıklama") },
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Default
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )

                Button(
                    onClick = viewModel::kaydet,
                    enabled = !state.kaydediliyor && state.baslik.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .height(52.dp)
                ) {
                    if (state.kaydediliyor) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Kaydet")
                    }
                }
            }
        }
    }
}

@Composable
private fun KapakBolumu(
    kapakUrl: String,
    kapakBase64: String?,
    baslik: String,
    onKamera: () -> Unit,
    onGaleri: () -> Unit,
    onKaldir: () -> Unit
) {
    Row(modifier = Modifier.padding(top = 16.dp)) {
        KapakGorseli(
            kapakUrl = kapakUrl,
            kapakBase64 = kapakBase64,
            baslik = baslik,
            modifier = Modifier
                .width(104.dp)
                .height(152.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Kapak",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Kitap kataloglarda yoksa kapağı kendin çekebilirsin.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onKamera, contentPadding = PaddingValues(12.dp)) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Kamerayla çek")
                }
                OutlinedButton(onClick = onGaleri, contentPadding = PaddingValues(12.dp)) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Galeriden seç")
                }
                if (kapakBase64 != null || kapakUrl.isNotBlank()) {
                    OutlinedButton(onClick = onKaldir, contentPadding = PaddingValues(12.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Kapağı kaldır")
                    }
                }
            }
        }
    }
}

/**
 * Kullanicinin kendi kayitlarindan gelen onerilerle calisan metin alani.
 * Turkce yayinevi adlarini her seferinde bastan yazmayi engeller.
 */
@Composable
private fun OneriliAlan(
    deger: String,
    etiket: String,
    ipucu: String?,
    oneriler: List<String>,
    onDegisim: (String) -> Unit
) {
    val eslesenler = remember(deger, oneriler) {
        if (deger.isBlank()) {
            emptyList()
        } else {
            oneriler.filter { it != deger && aramaEslesiyorMu(deger, it) }.take(3)
        }
    }

    Column(modifier = Modifier.padding(top = 12.dp)) {
        OutlinedTextField(
            value = deger,
            onValueChange = onDegisim,
            label = { Text(etiket) },
            supportingText = ipucu?.let { { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (eslesenler.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 6.dp)
            ) {
                eslesenler.forEach { oneri ->
                    AssistChip(
                        onClick = { onDegisim(oneri) },
                        label = { Text(oneri, maxLines = 1) }
                    )
                }
            }
        }
    }
}
