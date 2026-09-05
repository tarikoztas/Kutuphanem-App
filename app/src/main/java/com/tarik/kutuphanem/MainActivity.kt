package com.tarik.kutuphanem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tarik.kutuphanem.ui.MainViewModel
import com.tarik.kutuphanem.ui.OturumDurumu
import com.tarik.kutuphanem.ui.components.YuklemeKutusu
import com.tarik.kutuphanem.ui.navigation.AppNavHost
import com.tarik.kutuphanem.ui.theme.KutuphanemTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            KutuphanemTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val oturum by viewModel.oturum.collectAsStateWithLifecycle()

                    when (oturum) {
                        OturumDurumu.Bilinmiyor -> YuklemeKutusu()

                        else -> {
                            val girisYapilmis = oturum == OturumDurumu.GirisVar
                            // Giris/cikista navigasyon agacini bastan kurar; boylece
                            // eski oturumun geri yigini (back stack) tasinmaz.
                            key(girisYapilmis) {
                                AppNavHost(girisYapilmis = girisYapilmis)
                            }
                        }
                    }
                }
            }
        }
    }
}
