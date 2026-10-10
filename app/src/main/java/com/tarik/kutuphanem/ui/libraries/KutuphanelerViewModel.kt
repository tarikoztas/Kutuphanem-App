package com.tarik.kutuphanem.ui.libraries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tarik.kutuphanem.core.AppResult
import com.tarik.kutuphanem.core.aramaEslesiyorMu
import com.tarik.kutuphanem.core.dataOrNull
import com.tarik.kutuphanem.core.turkceKarsilastirici
import com.tarik.kutuphanem.domain.model.Kitap
import com.tarik.kutuphanem.domain.model.Kutuphane
import com.tarik.kutuphanem.domain.model.KutuphaneKitabi
import com.tarik.kutuphanem.domain.model.OkumaSayilari
import com.tarik.kutuphanem.domain.model.okumaSayilari
import com.tarik.kutuphanem.domain.repository.AuthRepository
import com.tarik.kutuphanem.domain.repository.KitapRepository
import com.tarik.kutuphanem.domain.repository.KutuphaneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class KutuphanelerViewModel @Inject constructor(
    private val kutuphaneRepository: KutuphaneRepository,
    private val kitapRepository: KitapRepository,
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

    private val _arama = MutableStateFlow("")
    val arama: StateFlow<String> = _arama.asStateFlow()

    /**
     * kutuphaneId -> kitaplar. Her kutuphanenin kitap akisi dinlenir; bu sayede hem
     * okundu sayaclari hem genel arama ayni veriden, cevrimdisi da calisarak beslenir.
     * Henuz yuklenmemis kutuphane haritada yer almaz.
     *
     * Sadece kutuphane kimlikleri degisince dinleyiciler yeniden kurulur (kitap sayaci
     * degisimi durum'u tetikler ama kimlik listesi ayni kalir).
     */
    private val kitaplarByKutuphane: StateFlow<Map<String, List<Kitap>>> = durum
        .map { sonuc -> sonuc.dataOrNull().orEmpty().map { it.id } }
        .distinctUntilChanged()
        .flatMapLatest { idler -> kitaplariDinle(idler) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )

    /** Kutuphane karti rozetleri icin: kutuphaneId -> okundu / okunmadi dagilimi. */
    val sayilar: StateFlow<Map<String, OkumaSayilari>> = kitaplarByKutuphane
        .map { harita -> harita.mapValues { (_, kitaplar) -> kitaplar.okumaSayilari() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )

    /** Genel arama sonuclari; arama bossa bos liste doner. */
    val sonuclar: StateFlow<List<KutuphaneKitabi>> =
        combine(_arama, durum, kitaplarByKutuphane) { anahtar, kutuphaneler, harita ->
            if (anahtar.isBlank()) return@combine emptyList()

            kutuphaneler.dataOrNull().orEmpty().flatMap { kutuphane ->
                harita[kutuphane.id].orEmpty()
                    .filter { aramaEslesiyorMu(anahtar, it.baslik, it.yazarMetni, it.yayinevi) }
                    .map { KutuphaneKitabi(kutuphane.id, kutuphane.ad, it) }
            }.sortedWith(compareBy(turkceKarsilastirici) { it.kitap.baslik })
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Kamerayla cekilmis kapaklarin onbellegi: "kutuphaneId/kitapId" -> base64. */
    private val _kapaklar = MutableStateFlow<Map<String, String>>(emptyMap())
    val kapaklar: StateFlow<Map<String, String>> = _kapaklar.asStateFlow()
    private val kapakIstenenler = mutableSetOf<String>()

    private fun kitaplariDinle(idler: List<String>): Flow<Map<String, List<Kitap>>> {
        // combine() bos listede hic deger uretmez; bos durumu elle veriyoruz.
        if (idler.isEmpty()) return flowOf(emptyMap())

        val akislar = idler.map { id ->
            kitapRepository.kitaplar(id).map { sonuc -> id to sonuc.dataOrNull() }
        }
        return combine(akislar) { ciftler ->
            ciftler.mapNotNull { (id, kitaplar) -> kitaplar?.let { id to it } }.toMap()
        }
    }

    fun aramaDegisti(deger: String) {
        _arama.value = deger
    }

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

    fun okunduDegistir(sonuc: KutuphaneKitabi) {
        viewModelScope.launch {
            val kayit = kitapRepository.okunduDegistir(
                sonuc.kutuphaneId,
                sonuc.kitap.id,
                !sonuc.kitap.okundu
            )
            if (kayit is AppResult.Error) _mesaj.value = kayit.message
        }
    }

    /** Sonuc satiri gorunur oldugunda cagrilir; ayni kitap icin tek sefer istek yapilir. */
    fun kapakIste(kutuphaneId: String, kitapId: String) {
        val anahtar = "$kutuphaneId/$kitapId"
        if (!kapakIstenenler.add(anahtar)) return
        viewModelScope.launch {
            val base64 = (kitapRepository.kapakGetir(kutuphaneId, kitapId) as? AppResult.Success)?.data
            if (base64 != null) {
                _kapaklar.update { it + (anahtar to base64) }
            } else {
                kapakIstenenler.remove(anahtar)
            }
        }
    }

    fun cikisYap() = authRepository.cikisYap()
}
