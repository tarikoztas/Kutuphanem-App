package com.tarik.kutuphanem.ui.libraries

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tarik.kutuphanem.core.AppResult
import com.tarik.kutuphanem.domain.model.Kutuphane
import com.tarik.kutuphanem.ui.components.BosDurum
import com.tarik.kutuphanem.ui.components.OnayDiyalogu
import com.tarik.kutuphanem.ui.components.YuklemeKutusu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KutuphanelerEkrani(
    onKutuphaneAc: (Kutuphane) -> Unit,
    viewModel: KutuphanelerViewModel = hiltViewModel()
) {
    val durum by viewModel.durum.collectAsStateWithLifecycle()
    val mesaj by viewModel.mesaj.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var formAcik by remember { mutableStateOf(false) }
    var duzenlenen by remember { mutableStateOf<Kutuphane?>(null) }
    var silinecek by remember { mutableStateOf<Kutuphane?>(null) }
    var cikisSoruluyor by remember { mutableStateOf(false) }

    LaunchedEffect(mesaj) {
        mesaj?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mesajGosterildi()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kütüphanelerim") },
                actions = {
                    IconButton(onClick = { cikisSoruluyor = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Çıkış yap")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { duzenlenen = null; formAcik = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Kütüphane ekle") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val d = durum) {
                is AppResult.Loading -> YuklemeKutusu()

                is AppResult.Error -> HataDurumu(d.message)

                is AppResult.Success -> {
                    if (d.data.isEmpty()) {
                        BosDurum(
                            ikon = Icons.Outlined.LibraryBooks,
                            baslik = "Henüz kütüphanen yok",
                            aciklama = "Sağ alttaki düğmeyle ilk rafını oluştur. Örneğin \"Ev Kitaplığı\" ya da \"Okunacaklar\"."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(d.data, key = { it.id }) { kutuphane ->
                                KutuphaneKarti(
                                    kutuphane = kutuphane,
                                    onTikla = { onKutuphaneAc(kutuphane) },
                                    onDuzenle = { duzenlenen = kutuphane; formAcik = true },
                                    onSil = { silinecek = kutuphane }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (formAcik) {
        KutuphaneAdiDiyalogu(
            baslangicDeger = duzenlenen?.ad.orEmpty(),
            duzenlemeMi = duzenlenen != null,
            onKaydet = { ad ->
                val mevcut = duzenlenen
                if (mevcut == null) viewModel.ekle(ad) else viewModel.yenidenAdlandir(mevcut.id, ad)
            },
            onKapat = { formAcik = false; duzenlenen = null }
        )
    }

    silinecek?.let { kutuphane ->
        OnayDiyalogu(
            baslik = "Kütüphane silinsin mi?",
            metin = "\"${kutuphane.ad}\" ve içindeki ${kutuphane.kitapSayisi} kitap kalıcı olarak silinecek.",
            onOnayla = { viewModel.sil(kutuphane) },
            onKapat = { silinecek = null }
        )
    }

    if (cikisSoruluyor) {
        OnayDiyalogu(
            baslik = "Çıkış yapılsın mı?",
            metin = "Kitapların hesabında kayıtlı kalır; tekrar giriş yaptığında hepsi geri gelir.",
            onaylaMetni = "Çıkış yap",
            onOnayla = { viewModel.cikisYap() },
            onKapat = { cikisSoruluyor = false }
        )
    }
}

@Composable
private fun KutuphaneKarti(
    kutuphane: Kutuphane,
    onTikla: () -> Unit,
    onDuzenle: () -> Unit,
    onSil: () -> Unit
) {
    var menuAcik by remember { mutableStateOf(false) }

    Card(
        onClick = onTikla,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 4.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.LibraryBooks,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            ) {
                Text(text = kutuphane.ad, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (kutuphane.kitapSayisi == 0) "Boş" else "${kutuphane.kitapSayisi} kitap",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(onClick = { menuAcik = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Seçenekler")
                }
                DropdownMenu(expanded = menuAcik, onDismissRequest = { menuAcik = false }) {
                    DropdownMenuItem(
                        text = { Text("Yeniden adlandır") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = { menuAcik = false; onDuzenle() }
                    )
                    DropdownMenuItem(
                        text = { Text("Sil") },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = { menuAcik = false; onSil() }
                    )
                }
            }
        }
    }
}

@Composable
private fun HataDurumu(mesaj: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = mesaj,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}
