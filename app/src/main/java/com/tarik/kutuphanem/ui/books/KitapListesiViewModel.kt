package com.tarik.kutuphanem.ui.books

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.tarik.kutuphanem.core.AppResult
import com.tarik.kutuphanem.core.aramaEslesiyorMu
import com.tarik.kutuphanem.core.dataOrNull
import com.tarik.kutuphanem.core.map
import com.tarik.kutuphanem.domain.model.Kitap
import com.tarik.kutuphanem.domain.model.OkumaSayilari
import com.tarik.kutuphanem.domain.model.okumaSayilari
import com.tarik.kutuphanem.domain.repository.KitapRepository
import com.tarik.kutuphanem.ui.navigation.KitapListesiRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map as akisMap
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KitapListesiViewModel @Inject constructor(
    private val kitapRepository: KitapRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val rota = savedStateHandle.toRoute<KitapListesiRoute>()
    val kutuphaneId: String = rota.kutuphaneId
    val kutuphaneAdi: String = rota.kutuphaneAdi

    private val _arama = MutableStateFlow("")
    val arama: StateFlow<String> = _arama.asStateFlow()

    private val _izgaraGorunumu = MutableStateFlow(true)
    val izgaraGorunumu: StateFlow<Boolean> = _izgaraGorunumu.asStateFlow()

    private val _mesaj = MutableStateFlow<String?>(null)
    val mesaj: StateFlow<String?> = _mesaj.asStateFlow()

    /** Kamerayla cekilmis kapaklarin onbellegi: kitapId -> base64. */
    private val _kapaklar = MutableStateFlow<Map<String, String>>(emptyMap())
    val kapaklar: StateFlow<Map<String, String>> = _kapaklar.asStateFlow()
    private val kapakIstenenler = mutableSetOf<String>()

    /**
     * Arama istemci tarafinda: kutuphane basina kitap sayisi bir cihazda rahat tutulur,
     * boylece her tusa basista Firestore sorgusu gitmez ve cevrimdisi de calisir.
     */
    private val tumKitaplar: StateFlow<AppResult<List<Kitap>>> =
        kitapRepository.kitaplar(kutuphaneId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppResult.Loading
        )

    val durum: StateFlow<AppResult<List<Kitap>>> =
        combine(tumKitaplar, _arama) { sonuc, anahtar ->
            sonuc.map { liste ->
                liste.filter { kitap ->
                    aramaEslesiyorMu(anahtar, kitap.baslik, kitap.yazarMetni, kitap.yayinevi)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppResult.Loading
        )

    /** Arama filtresinden bagimsiz: kutuphanenin tamamindaki okundu / okunmadi dagilimi. */
    val okumaSayilari: StateFlow<OkumaSayilari?> = tumKitaplar
        .akisMap { sonuc -> sonuc.dataOrNull()?.okumaSayilari() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun aramaDegisti(deger: String) {
        _arama.value = deger
    }

    fun gorunumDegistir() {
        _izgaraGorunumu.update { !it }
    }

    fun mesajGosterildi() {
        _mesaj.value = null
    }

    /**
     * Okundu durumu Firestore'a yazilir; yerel onbellek aninda guncellendigi icin
     * rozetin rengi beklemeden degisir (gecikme telafisi).
     */
    fun okunduDegistir(kitap: Kitap) {
        viewModelScope.launch {
            val sonuc = kitapRepository.okunduDegistir(kutuphaneId, kitap.id, !kitap.okundu)
            if (sonuc is AppResult.Error) _mesaj.value = sonuc.message
        }
    }

    fun sil(kitap: Kitap) {
        viewModelScope.launch {
            when (val sonuc = kitapRepository.sil(kutuphaneId, kitap.id)) {
                is AppResult.Error -> _mesaj.value = sonuc.message
                else -> _mesaj.value = "\"${kitap.baslik}\" silindi."
            }
        }
    }

    /** Kart gorunur oldugunda cagrilir; ayni kitap icin tek sefer istek yapilir. */
    fun kapakIste(kitapId: String) {
        if (!kapakIstenenler.add(kitapId)) return
        viewModelScope.launch {
            val sonuc = kitapRepository.kapakGetir(kutuphaneId, kitapId)
            val base64 = (sonuc as? AppResult.Success)?.data
            if (base64 != null) {
                _kapaklar.update { it + (kitapId to base64) }
            } else {
                // Basarisiz olduysa tekrar denenebilsin.
                kapakIstenenler.remove(kitapId)
            }
        }
    }
}
