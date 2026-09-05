package com.tarik.kutuphanem.ui.libraries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tarik.kutuphanem.core.AppResult
import com.tarik.kutuphanem.domain.model.Kutuphane
import com.tarik.kutuphanem.domain.repository.AuthRepository
import com.tarik.kutuphanem.domain.repository.KutuphaneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KutuphanelerViewModel @Inject constructor(
    private val kutuphaneRepository: KutuphaneRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val durum: StateFlow<AppResult<List<Kutuphane>>> = kutuphaneRepository.kutuphaneler()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppResult.Loading
        )

    private val _mesaj = MutableStateFlow<String?>(null)
    val mesaj: StateFlow<String?> = _mesaj.asStateFlow()

    fun mesajGosterildi() {
        _mesaj.value = null
    }

    fun ekle(ad: String) {
        if (ad.isBlank()) return
        viewModelScope.launch {
            when (val sonuc = kutuphaneRepository.ekle(ad)) {
                is AppResult.Error -> _mesaj.value = sonuc.message
                else -> Unit
            }
        }
    }

    fun yenidenAdlandir(kutuphaneId: String, yeniAd: String) {
        if (yeniAd.isBlank()) return
        viewModelScope.launch {
            when (val sonuc = kutuphaneRepository.yenidenAdlandir(kutuphaneId, yeniAd)) {
                is AppResult.Error -> _mesaj.value = sonuc.message
                else -> Unit
            }
        }
    }

    fun sil(kutuphane: Kutuphane) {
        viewModelScope.launch {
            when (val sonuc = kutuphaneRepository.sil(kutuphane.id)) {
                is AppResult.Error -> _mesaj.value = sonuc.message
                else -> _mesaj.value = "\"${kutuphane.ad}\" silindi."
            }
        }
    }

    fun cikisYap() = authRepository.cikisYap()
}
