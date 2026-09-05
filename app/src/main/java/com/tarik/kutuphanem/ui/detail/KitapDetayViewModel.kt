package com.tarik.kutuphanem.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.tarik.kutuphanem.core.AppResult
import com.tarik.kutuphanem.core.dataOrNull
import com.tarik.kutuphanem.domain.model.Kitap
import com.tarik.kutuphanem.domain.repository.KitapRepository
import com.tarik.kutuphanem.ui.navigation.KitapDetayRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KitapDetayViewModel @Inject constructor(
    private val kitapRepository: KitapRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val rota = savedStateHandle.toRoute<KitapDetayRoute>()
    val kutuphaneId: String = rota.kutuphaneId
    val kitapId: String = rota.kitapId

    private val _kapakBase64 = MutableStateFlow<String?>(null)
    val kapakBase64: StateFlow<String?> = _kapakBase64.asStateFlow()

    private val _mesaj = MutableStateFlow<String?>(null)
    val mesaj: StateFlow<String?> = _mesaj.asStateFlow()

    private val _silindi = MutableStateFlow(false)
    val silindi: StateFlow<Boolean> = _silindi.asStateFlow()

    private var kapakIstendi = false

    val durum: StateFlow<AppResult<Kitap>> = kitapRepository.kitap(kutuphaneId, kitapId)
        .onEach { sonuc ->
            val kitap = sonuc.dataOrNull()
            if (kitap != null && kitap.yerelKapakVar && !kapakIstendi) {
                kapakIstendi = true
                _kapakBase64.value = kitapRepository.kapakGetir(kutuphaneId, kitapId).dataOrNull()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppResult.Loading
        )

    fun mesajGosterildi() {
        _mesaj.value = null
    }

    fun okunduDegistir(kitap: Kitap) {
        viewModelScope.launch {
            val sonuc = kitapRepository.okunduDegistir(kutuphaneId, kitap.id, !kitap.okundu)
            if (sonuc is AppResult.Error) _mesaj.value = sonuc.message
        }
    }

    fun sil() {
        viewModelScope.launch {
            when (val sonuc = kitapRepository.sil(kutuphaneId, kitapId)) {
                is AppResult.Error -> _mesaj.value = sonuc.message
                else -> _silindi.value = true
            }
        }
    }
}
