package com.tarik.kutuphanem.domain.repository

import com.tarik.kutuphanem.core.AppResult
import com.tarik.kutuphanem.domain.model.Kitap
import kotlinx.coroutines.flow.Flow

/** Kamerayla cekilen kapagin kaydetme sirasinda ne olacagi. */
sealed interface KapakDegisikligi {
    data object Dokunma : KapakDegisikligi
    data object Kaldir : KapakDegisikligi
    data class Yeni(val base64: String) : KapakDegisikligi
}

/** Form ekranindaki otomatik tamamlama onerileri (kullanicinin kendi kayitlarindan). */
data class Oneriler(
    val yazarlar: List<String> = emptyList(),
    val yayinevleri: List<String> = emptyList()
)

interface KitapRepository {

    fun kitaplar(kutuphaneId: String): Flow<AppResult<List<Kitap>>>

    fun kitap(kutuphaneId: String, kitapId: String): Flow<AppResult<Kitap>>

    suspend fun ekle(
        kutuphaneId: String,
        kitap: Kitap,
        kapak: KapakDegisikligi = KapakDegisikligi.Dokunma
    ): AppResult<String>

    suspend fun guncelle(
        kutuphaneId: String,
        kitap: Kitap,
        kapak: KapakDegisikligi = KapakDegisikligi.Dokunma
    ): AppResult<Unit>

    suspend fun okunduDegistir(kutuphaneId: String, kitapId: String, okundu: Boolean): AppResult<Unit>

    suspend fun sil(kutuphaneId: String, kitapId: String): AppResult<Unit>

    /** Base64 kapak ayri dokumanda; sadece ihtiyac duyuldugunda cekilir. */
    suspend fun kapakGetir(kutuphaneId: String, kitapId: String): AppResult<String?>

    suspend fun oneriler(kutuphaneId: String): AppResult<Oneriler>

    /** Ayni ISBN kutuphanede zaten var mi? (mukerrer ekleme uyarisi icin) */
    suspend fun isbnVarMi(kutuphaneId: String, isbn: String): AppResult<Kitap?>
}
