package com.tarik.kutuphanem.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KayitEkrani(
    onGeri: () -> Unit,
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hesap Oluştur") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.formuTemizle()
                            onGeri()
                        }
                    ) {
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            OutlinedTextField(
                value = state.adSoyad,
                onValueChange = viewModel::adSoyadDegisti,
                label = { Text("Ad Soyad") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            EpostaAlani(
                deger = state.eposta,
                onDegisim = viewModel::epostaDegisti,
                modifier = Modifier.padding(top = 12.dp)
            )

            SifreAlani(
                deger = state.sifre,
                etiket = "Şifre",
                onDegisim = viewModel::sifreDegisti,
                imeAction = ImeAction.Next,
                modifier = Modifier.padding(top = 12.dp)
            )

            SifreAlani(
                deger = state.sifreTekrar,
                etiket = "Şifre (tekrar)",
                onDegisim = viewModel::sifreTekrarDegisti,
                modifier = Modifier.padding(top = 12.dp)
            )

            Button(
                onClick = viewModel::kayitOl,
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
                    Text("Kayıt Ol")
                }
            }

            TextButton(
                onClick = {
                    viewModel.formuTemizle()
                    onGeri()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Zaten hesabın var mı? Giriş yap")
            }
        }
    }
}
