package com.tarik.kutuphanem.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.tarik.kutuphanem.core.AppResult
import com.tarik.kutuphanem.domain.model.Kitap
import com.tarik.kutuphanem.domain.repository.KitapAramaRepository
import com.tarik.kutuphanem.domain.repository.KitapRepository
import com.tarik.kutuphanem.ui.navigation.IsimleAramaRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IsimleAramaState(
    val sorgu: String = "",
    val araniyor: Boolean = false,
    val arandiMi: Boolean = false,
    val sonuclar: List<Kitap> = emptyList(),
    val ekleniyor: Boolean = false,
    val eklendi: Boolean = false,
    val mesaj: String? = null
)

@HiltViewModel
class IsimleAramaViewModel @Inject constructor(
    private val aramaRepository: KitapAramaRepository,
    private val kitapRepository: KitapRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val kutuphaneId = savedStateHandle.toRoute<IsimleAramaRoute>().kutuphaneId

    private val _state = MutableStateFlow(IsimleAramaState())
    val state: StateFlow<IsimleAramaState> = _state.asStateFlow()

    fun sorguDegisti(deger: String) = _state.update { it.copy(sorgu = deger) }

    fun mesajGosterildi() = _state.update { it.copy(mesaj = null) }

    fun ara() {
        val sorgu = _state.value.sorgu.trim()
        if (sorgu.length < 3) {
            _state.update { it.copy(mesaj = "En az 3 harf yaz.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(araniyor = true) }
            when (val sonuc = aramaRepository.isimleAra(sorgu)) {
                is AppResult.Error -> _state.update {
                    it.copy(araniyor = false, arandiMi = true, mesaj = sonuc.message)
                }

                is AppResult.Success -> _state.update {
                    it.copy(araniyor = false, arandiMi = true, sonuclar = sonuc.data)
                }

                else -> Unit
            }
        }
    }

    /** Secilen sonucu dogrudan rafa ekler; kullanici sonra detaydan duzenleyebilir. */
    fun ekle(kitap: Kitap) {
        viewModelScope.launch {
            _state.update { it.copy(ekleniyor = true) }
            when (val sonuc = kitapRepository.ekle(kutuphaneId, kitap)) {
                is AppResult.Error -> _state.update {
                    it.copy(ekleniyor = false, mesaj = sonuc.message)
                }

                else -> _state.update { it.copy(ekleniyor = false, eklendi = true) }
            }
        }
    }
}
