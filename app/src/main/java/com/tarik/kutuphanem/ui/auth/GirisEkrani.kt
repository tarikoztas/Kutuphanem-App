package com.tarik.kutuphanem.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun GirisEkrani(
    onKayitaGit: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.hata, state.bilgi) {
        val mesaj = state.hata ?: state.bilgi
        if (mesaj != null) {
            snackbarHostState.showSnackbar(mesaj)
            viewModel.mesajGosterildi()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 48.dp)
                    .size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Kütüphanem",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = "Kitaplarını rafla, barkodla ekle, okuduklarını işaretle.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            EpostaAlani(deger = state.eposta, onDegisim = viewModel::epostaDegisti)

            SifreAlani(
                deger = state.sifre,
                etiket = "Şifre",
                onDegisim = viewModel::sifreDegisti,
                modifier = Modifier.padding(top = 12.dp)
            )

            Button(
                onClick = viewModel::girisYap,
                enabled = !state.yukleniyor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .height(52.dp)
            ) {
                if (state.yukleniyor) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Giriş Yap")
                }
            }

            TextButton(
                onClick = viewModel::sifremiUnuttum,
                enabled = !state.yukleniyor,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text("Şifremi unuttum")
            }

            TextButton(
                onClick = {
                    viewModel.formuTemizle()
                    onKayitaGit()
                },
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text("Hesabın yok mu? Kayıt ol")
            }
        }
    }
}
