package com.tarik.kutuphanem.ui.manual

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.tarik.kutuphanem.core.AppResult
import com.tarik.kutuphanem.core.dataOrNull
import com.tarik.kutuphanem.domain.model.Kitap
import com.tarik.kutuphanem.domain.repository.KapakDegisikligi
import com.tarik.kutuphanem.domain.repository.KitapAramaRepository
import com.tarik.kutuphanem.domain.repository.KitapRepository
import com.tarik.kutuphanem.domain.repository.Oneriler
import com.tarik.kutuphanem.ui.navigation.KitapFormRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KitapFormState(
    val baslik: String = "",
    val yazarlar: String = "",
    val yayinevi: String = "",
    val sayfaSayisi: String = "",
    val isbn: String = "",
    val aciklama: String = "",
    val kapakUrl: String = "",
    /** Cihazda cekilmis kapak (base64). */
    val kapakBase64: String? = null,
    val duzenlemeMi: Boolean = false,
    val araniyor: Boolean = false,
    val kaydediliyor: Boolean = false,
    val kaydedildi: Boolean = false,
    val hata: String? = null,
    val bilgi: String? = null,
    val oneriler: Oneriler = Oneriler()
)

@HiltViewModel
class KitapFormViewModel @Inject constructor(
    private val kitapRepository: KitapRepository,
    private val aramaRepository: KitapAramaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val rota = savedStateHandle.toRoute<KitapFormRoute>()
    private val kutuphaneId = rota.kutuphaneId
    private val kitapId = rota.kitapId

    private var mevcutKitap: Kitap? = null
    private var kapakDegisikligi: KapakDegisikligi = KapakDegisikligi.Dokunma

    private val _state = MutableStateFlow(KitapFormState(duzenlemeMi = kitapId != null))
    val state: StateFlow<KitapFormState> = _state.asStateFlow()

    init {
        viewModelScope.launch { onerileriYukle() }

        when {
            kitapId != null -> mevcutKitabiYukle(kitapId)
            !rota.isbn.isNullOrBlank() -> isbnIleDoldur(rota.isbn)
        }
    }

    fun baslikDegisti(v: String) = _state.update { it.copy(baslik = v) }
    fun yazarlarDegisti(v: String) = _state.update { it.copy(yazarlar = v) }
    fun yayineviDegisti(v: String) = _state.update { it.copy(yayinevi = v) }
    fun sayfaDegisti(v: String) = _state.update { it.copy(sayfaSayisi = v.filter { c -> c.isDigit() }) }
    fun isbnDegisti(v: String) = _state.update { it.copy(isbn = v) }
    fun aciklamaDegisti(v: String) = _state.update { it.copy(aciklama = v) }

    fun mesajGosterildi() = _state.update { it.copy(hata = null, bilgi = null) }

    fun kapakSecildi(base64: String) {
        kapakDegisikligi = KapakDegisikligi.Yeni(base64)
        // Cekilen kapak, API kapaginin onune gecer.
        _state.update { it.copy(kapakBase64 = base64, kapakUrl = "") }
    }

    fun kapakKaldirildi() {
        kapakDegisikligi = KapakDegisikligi.Kaldir
        _state.update { it.copy(kapakBase64 = null, kapakUrl = "") }
    }

    /** Kullanici ISBN'i elle yazip "bilgileri getir" dedigi durum. */
    fun isbnIleDoldurmayiDene() {
        val isbn = _state.value.isbn
        if (isbn.isBlank()) {
            _state.update { it.copy(hata = "Önce ISBN numarasını yaz.") }
            return
        }
        isbnIleDoldur(isbn)
    }

    fun kaydet() {
        val s = _state.value
        if (s.baslik.isBlank()) {
            _state.update { it.copy(hata = "Kitap adı boş olamaz.") }
            return
        }

        val kitap = Kitap(
            id = kitapId.orEmpty(),
            baslik = s.baslik,
            yazarlar = s.yazarlar.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            yayinevi = s.yayinevi,
            sayfaSayisi = s.sayfaSayisi.toIntOrNull(),
            isbn = s.isbn,
            aciklama = s.aciklama,
            kapakUrl = s.kapakUrl,
            yerelKapakVar = mevcutKitap?.yerelKapakVar ?: false,
            okundu = mevcutKitap?.okundu ?: false,
            eklenmeZamani = mevcutKitap?.eklenmeZamani ?: 0L
        )

        viewModelScope.launch {
            _state.update { it.copy(kaydediliyor = true, hata = null) }

            val sonuc = if (kitapId == null) {
                kitapRepository.ekle(kutuphaneId, kitap, kapakDegisikligi)
            } else {
                kitapRepository.guncelle(kutuphaneId, kitap, kapakDegisikligi)
            }

            when (sonuc) {
                is AppResult.Error -> _state.update {
                    it.copy(kaydediliyor = false, hata = sonuc.message)
                }

                else -> _state.update { it.copy(kaydediliyor = false, kaydedildi = true) }
            }
        }
    }

    private fun mevcutKitabiYukle(id: String) {
        viewModelScope.launch {
            val sonuc = kitapRepository.kitap(kutuphaneId, id).first { it !is AppResult.Loading }
            when (sonuc) {
                is AppResult.Error -> _state.update { it.copy(hata = sonuc.message) }

                is AppResult.Success -> {
                    val kitap = sonuc.data
                    mevcutKitap = kitap
                    _state.update {
                        it.copy(
                            baslik = kitap.baslik,
                            yazarlar = kitap.yazarMetni,
                            yayinevi = kitap.yayinevi,
                            sayfaSayisi = kitap.sayfaSayisi?.toString().orEmpty(),
                            isbn = kitap.isbn,
                            aciklama = kitap.aciklama,
                            kapakUrl = kitap.kapakUrl
                        )
                    }
                    if (kitap.yerelKapakVar) {
                        val kapak = kitapRepository.kapakGetir(kutuphaneId, id).dataOrNull()
                        if (kapak != null) _state.update { it.copy(kapakBase64 = kapak) }
                    }
                }

                else -> Unit
            }
        }
    }

    private fun isbnIleDoldur(isbn: String) {
        viewModelScope.launch {
            _state.update { it.copy(araniyor = true, isbn = isbn, hata = null) }

            // Ayni kitap rafta duruyor mu? Mukerrer eklemeyi bastan uyaralim.
            val mevcut = kitapRepository.isbnVarMi(kutuphaneId, isbn).dataOrNull()

            when (val sonuc = aramaRepository.isbnIleAra(isbn)) {
                is AppResult.Error -> _state.update {
                    it.copy(araniyor = false, hata = sonuc.message)
                }

                is AppResult.Success -> {
                    val bulunan = sonuc.data
                    _state.update { s ->
                        if (bulunan == null) {
                            s.copy(
                                araniyor = false,
                                bilgi = "Aradığınız kitap bulunamadı. Bilgileri elle yazabilirsin."
                            )
                        } else {
                            s.copy(
                                araniyor = false,
                                baslik = s.baslik.ifBlank { bulunan.baslik },
                                yazarlar = s.yazarlar.ifBlank { bulunan.yazarMetni },
                                yayinevi = s.yayinevi.ifBlank { bulunan.yayinevi },
                                sayfaSayisi = s.sayfaSayisi.ifBlank {
                                    bulunan.sayfaSayisi?.toString().orEmpty()
                                },
                                aciklama = s.aciklama.ifBlank { bulunan.aciklama },
                                kapakUrl = s.kapakUrl.ifBlank { bulunan.kapakUrl },
                                bilgi = mevcut?.let { "\"${it.baslik}\" bu rafta zaten var." }
                            )
                        }
                    }
                }

                else -> Unit
            }
        }
    }

    private suspend fun onerileriYukle() {
        val oneriler = kitapRepository.oneriler(kutuphaneId).dataOrNull() ?: return
        _state.update { it.copy(oneriler = oneriler) }
    }
}
