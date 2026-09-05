package com.tarik.kutuphanem.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tarik.kutuphanem.domain.model.Kitap
import com.tarik.kutuphanem.ui.components.BosDurum
import com.tarik.kutuphanem.ui.components.KapakGorseli

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IsimleAramaEkrani(
    onGeri: () -> Unit,
    onEklendi: () -> Unit,
    viewModel: IsimleAramaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val klavye = LocalSoftwareKeyboardController.current
    // Sonuca dokununca acilan detay onizlemesi
    var onizlenen by remember { mutableStateOf<Kitap?>(null) }

    fun aramayiBaslat() {
        klavye?.hide()
        viewModel.ara()
    }

    LaunchedEffect(state.eklendi) {
        if (state.eklendi) onEklendi()
    }

    LaunchedEffect(state.mesaj) {
        state.mesaj?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mesajGosterildi()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("İsimle ara") },
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
            OutlinedTextField(
                value = state.sorgu,
                onValueChange = viewModel::sorguDegisti,
                label = { Text("Kitap adı veya yazar") },
                placeholder = { Text("Örn: Tutunamayanlar Oğuz Atay") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                // Klavyedeki arama tusuna basinca da aramayi baslat.
                keyboardActions = KeyboardActions(onSearch = { aramayiBaslat() }),
                trailingIcon = {
                    IconButton(onClick = { aramayiBaslat() }) {
                        Icon(Icons.Default.Search, contentDescription = "Ara")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            if (state.araniyor || state.ekleniyor) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            when {
                state.sonuclar.isEmpty() && state.arandiMi && !state.araniyor -> BosDurum(
                    ikon = Icons.Default.Search,
                    baslik = "Sonuç yok",
                    aciklama = "Bu kitap kataloglarda bulunmuyor olabilir. Bilgileri elle girerek ekleyebilirsin."
                )

                state.sonuclar.isEmpty() -> BosDurum(
                    ikon = Icons.Default.Search,
                    baslik = "Kitabı ara",
                    aciklama = "Barkod okunmuyorsa kitap adını veya yazarını yazıp arayabilirsin."
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.sonuclar) { kitap ->
                        SonucSatiri(
                            kitap = kitap,
                            onTikla = { onizlenen = kitap },
                            onEkle = { viewModel.ekle(kitap) }
                        )
                    }
                }
            }
        }
    }

    onizlenen?.let { secili ->
        SonucOnizleme(
            kitap = secili,
            ekleniyor = state.ekleniyor,
            onEkle = { viewModel.ekle(secili) },
            onKapat = { onizlenen = null }
        )
    }
}

@Composable
private fun SonucSatiri(kitap: Kitap, onTikla: () -> Unit, onEkle: () -> Unit) {
    Card(onClick = onTikla, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KapakGorseli(
                kapakUrl = kitap.kapakUrl,
                kapakBase64 = null,
                baslik = kitap.baslik,
                modifier = Modifier
                    .width(52.dp)
                    .height(78.dp)
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
                if (kitap.yayinevi.isNotBlank()) {
                    Text(
                        text = kitap.yayinevi,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onEkle) {
                Icon(Icons.Default.Add, contentDescription = "Rafa ekle")
            }
        }
    }
}
