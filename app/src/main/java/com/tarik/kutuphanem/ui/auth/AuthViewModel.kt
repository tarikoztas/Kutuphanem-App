package com.tarik.kutuphanem.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tarik.kutuphanem.core.AppResult
import com.tarik.kutuphanem.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val adSoyad: String = "",
    val eposta: String = "",
    val sifre: String = "",
    val sifreTekrar: String = "",
    val yukleniyor: Boolean = false,
    val hata: String? = null,
    val bilgi: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun adSoyadDegisti(deger: String) = _state.update { it.copy(adSoyad = deger, hata = null) }
    fun epostaDegisti(deger: String) = _state.update { it.copy(eposta = deger, hata = null) }
    fun sifreDegisti(deger: String) = _state.update { it.copy(sifre = deger, hata = null) }
    fun sifreTekrarDegisti(deger: String) = _state.update { it.copy(sifreTekrar = deger, hata = null) }
    fun mesajGosterildi() = _state.update { it.copy(hata = null, bilgi = null) }

    /** Ekran degisiminde onceki formun verisi tasinmasin diye. */
    fun formuTemizle() = _state.update { AuthUiState(eposta = it.eposta) }

    fun girisYap() {
        val s = _state.value
        val hata = epostaHatasi(s.eposta) ?: sifreHatasi(s.sifre)
        if (hata != null) {
            _state.update { it.copy(hata = hata) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(yukleniyor = true, hata = null) }
            when (val sonuc = authRepository.girisYap(s.eposta, s.sifre)) {
                is AppResult.Error -> _state.update { it.copy(yukleniyor = false, hata = sonuc.message) }
                // Basarili olursa oturum akisi tetiklenir, yonlendirmeyi MainActivity yapar.
                else -> _state.update { it.copy(yukleniyor = false) }
            }
        }
    }

    fun kayitOl() {
        val s = _state.value
        val hata = adSoyadHatasi(s.adSoyad)
            ?: epostaHatasi(s.eposta)
            ?: sifreHatasi(s.sifre)
            ?: if (s.sifre != s.sifreTekrar) "Şifreler birbiriyle aynı değil." else null
        if (hata != null) {
            _state.update { it.copy(hata = hata) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(yukleniyor = true, hata = null) }
            when (val sonuc = authRepository.kayitOl(s.adSoyad, s.eposta, s.sifre)) {
                is AppResult.Error -> _state.update { it.copy(yukleniyor = false, hata = sonuc.message) }
                else -> _state.update { it.copy(yukleniyor = false) }
            }
        }
    }

    fun sifremiUnuttum() {
        val eposta = _state.value.eposta
        val hata = epostaHatasi(eposta)
        if (hata != null) {
            _state.update { it.copy(hata = "Sıfırlama bağlantısı için önce e-postanı yaz.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(yukleniyor = true) }
            val sonuc = authRepository.sifreSifirlamaGonder(eposta)
            _state.update {
                when (sonuc) {
                    is AppResult.Error -> it.copy(yukleniyor = false, hata = sonuc.message)
                    else -> it.copy(yukleniyor = false, bilgi = "Sıfırlama bağlantısı e-postana gönderildi.")
                }
            }
        }
    }

    private fun adSoyadHatasi(deger: String) =
        if (deger.trim().length < 3) "Ad soyad en az 3 karakter olmalı." else null

    private fun epostaHatasi(deger: String) = when {
        deger.isBlank() -> "E-posta boş olamaz."
        !android.util.Patterns.EMAIL_ADDRESS.matcher(deger.trim()).matches() ->
            "Geçerli bir e-posta yaz."
        else -> null
    }

    private fun sifreHatasi(deger: String) =
        if (deger.length < 6) "Şifre en az 6 karakter olmalı." else null
}
