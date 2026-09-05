package com.tarik.kutuphanem.ui.books

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.aspectRatio
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tarik.kutuphanem.core.AppResult
import com.tarik.kutuphanem.domain.model.Kitap
import com.tarik.kutuphanem.ui.components.BosDurum
import com.tarik.kutuphanem.ui.components.KapakGorseli
import com.tarik.kutuphanem.ui.components.OkunduRozeti
import com.tarik.kutuphanem.ui.components.OnayDiyalogu
import com.tarik.kutuphanem.ui.components.YuklemeKutusu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitapListesiEkrani(
    onGeri: () -> Unit,
    onKitapAc: (Kitap) -> Unit,
    onBarkodTara: () -> Unit,
    onElleEkle: () -> Unit,
    onIsimleAra: () -> Unit,
    viewModel: KitapListesiViewModel = hiltViewModel()
) {
    val durum by viewModel.durum.collectAsStateWithLifecycle()
    val arama by viewModel.arama.collectAsStateWithLifecycle()
    val izgara by viewModel.izgaraGorunumu.collectAsStateWithLifecycle()
    val kapaklar by viewModel.kapaklar.collectAsStateWithLifecycle()
    val mesaj by viewModel.mesaj.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var silinecek by remember { mutableStateOf<Kitap?>(null) }
    var ekleMenusu by remember { mutableStateOf(false) }

    LaunchedEffect(mesaj) {
        mesaj?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mesajGosterildi()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.kutuphaneAdi, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onGeri) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::gorunumDegistir) {
                        Icon(
                            imageVector = if (izgara) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                            contentDescription = if (izgara) "Liste görünümü" else "Izgara görünümü"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { ekleMenusu = true }) {
                Icon(Icons.Default.Add, contentDescription = "Kitap ekle")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AramaAlani(
                deger = arama,
                onDegisim = viewModel::aramaDegisti,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when (val d = durum) {
                is AppResult.Loading -> YuklemeKutusu()

                is AppResult.Error -> Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(d.message, color = MaterialTheme.colorScheme.error)
                }

                is AppResult.Success -> {
                    val kitaplar = d.data
                    when {
                        kitaplar.isEmpty() && arama.isNotBlank() -> BosDurum(
                            ikon = Icons.Default.Search,
                            baslik = "Sonuç yok",
                            aciklama = "\"$arama\" ile eşleşen kitap bulunamadı."
                        )

                        kitaplar.isEmpty() -> BosDurum(
                            ikon = Icons.AutoMirrored.Outlined.MenuBook,
                            baslik = "Bu raf henüz boş",
                            aciklama = "Sağ alttaki + ile barkod okutarak ya da elle kitap ekleyebilirsin."
                        )

                        izgara -> LazyVerticalGrid(
                            columns = GridCells.Adaptive(148.dp),
                            contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 96.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(kitaplar, key = { it.id }) { kitap ->
                                KitapIzgaraKarti(
                                    kitap = kitap,
                                    kapakBase64 = kapaklar[kitap.id],
                                    onKapakIste = viewModel::kapakIste,
                                    onTikla = { onKitapAc(kitap) },
                                    onOkunduDegistir = { viewModel.okunduDegistir(kitap) },
                                    onSil = { silinecek = kitap }
                                )
                            }
                        }

                        else -> LazyColumn(
                            contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 96.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(kitaplar, key = { it.id }) { kitap ->
                                KitapListeSatiri(
                                    kitap = kitap,
                                    kapakBase64 = kapaklar[kitap.id],
                                    onKapakIste = viewModel::kapakIste,
                                    onTikla = { onKitapAc(kitap) },
                                    onOkunduDegistir = { viewModel.okunduDegistir(kitap) },
                                    onSil = { silinecek = kitap }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (ekleMenusu) {
        KitapEklemeSecenekleri(
            onBarkod = { ekleMenusu = false; onBarkodTara() },
            onIsimleAra = { ekleMenusu = false; onIsimleAra() },
            onElle = { ekleMenusu = false; onElleEkle() },
            onKapat = { ekleMenusu = false }
        )
    }

    silinecek?.let { kitap ->
        OnayDiyalogu(
            baslik = "Kitap silinsin mi?",
            metin = "\"${kitap.baslik}\" bu kütüphaneden kalıcı olarak silinecek.",
            onOnayla = { viewModel.sil(kitap) },
            onKapat = { silinecek = null }
        )
    }
}

@Composable
private fun AramaAlani(
    deger: String,
    onDegisim: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = deger,
        onValueChange = onDegisim,
        placeholder = { Text("Kitap, yazar veya yayınevi ara") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (deger.isNotEmpty()) {
                IconButton(onClick = { onDegisim("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Aramayı temizle")
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default,
        shape = RoundedCornerShape(28.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun KitapIzgaraKarti(
    kitap: Kitap,
    kapakBase64: String?,
    onKapakIste: (String) -> Unit,
    onTikla: () -> Unit,
    onOkunduDegistir: () -> Unit,
    onSil: () -> Unit
) {
    KapakIstegi(kitap, kapakBase64, onKapakIste)

    Card(onClick = onTikla, modifier = Modifier.fillMaxWidth()) {
        Box {
            KapakGorseli(
                kapakUrl = kitap.kapakUrl,
                kapakBase64 = kapakBase64,
                baslik = kitap.baslik,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.68f)
            )
            IconButton(
                onClick = onSil,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Kitabı sil",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(modifier = Modifier.padding(10.dp)) {
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
            OkunduRozeti(
                okundu = kitap.okundu,
                onDegistir = onOkunduDegistir,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun KitapListeSatiri(
    kitap: Kitap,
    kapakBase64: String?,
    onKapakIste: (String) -> Unit,
    onTikla: () -> Unit,
    onOkunduDegistir: () -> Unit,
    onSil: () -> Unit
) {
    KapakIstegi(kitap, kapakBase64, onKapakIste)

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
                if (kitap.yayinevi.isNotBlank()) {
                    Text(
                        text = kitap.yayinevi,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                OkunduRozeti(
                    okundu = kitap.okundu,
                    onDegistir = onOkunduDegistir,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            IconButton(onClick = onSil) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Kitabı sil",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Kart ekranda gorundugunde, sadece gerekliyse base64 kapagi ister. */
@Composable
private fun KapakIstegi(
    kitap: Kitap,
    kapakBase64: String?,
    onKapakIste: (String) -> Unit
) {
    LaunchedEffect(kitap.id, kitap.yerelKapakVar) {
        if (kitap.yerelKapakVar && kitap.kapakUrl.isBlank() && kapakBase64 == null) {
            onKapakIste(kitap.id)
        }
    }
}
