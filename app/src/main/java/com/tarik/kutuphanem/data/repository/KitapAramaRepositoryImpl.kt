package com.tarik.kutuphanem.data.repository

import com.tarik.kutuphanem.core.AppResult
import com.tarik.kutuphanem.core.isbn.IsbnUtils
import com.tarik.kutuphanem.core.kullaniciMesaji
import com.tarik.kutuphanem.data.mapper.eksikleriDoldur
import com.tarik.kutuphanem.data.mapper.toKitap
import com.tarik.kutuphanem.data.mapper.yeterinceDolu
import com.tarik.kutuphanem.data.remote.api.GoogleBooksService
import com.tarik.kutuphanem.data.remote.api.OpenLibraryService
import com.tarik.kutuphanem.data.remote.dto.GoogleBooksBilgi
import com.tarik.kutuphanem.domain.model.Kitap
import com.tarik.kutuphanem.domain.repository.KitapAramaRepository
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ISBN arama zinciri. Ilk dolu sonucu dondurmek yerine alan alan birlestirir:
 * Google Books'ta baslik var ama sayfa sayisi yoksa, eksik alan OpenLibrary'den gelir.
 *
 * Sira:
 *  1. Google Books  q=isbn:<varyant>
 *  2. Google Books  q=<varyant>        (serbest metin; Turkce baskilarda sik ise yariyor)
 *  3. OpenLibrary   bibkeys=ISBN:<varyant>
 *
 * Hata politikasi: bir kaynagin patlamasi akisi durdurmaz. Sonuc bulunamadiysa
 * kullaniciya ham HTTP hatasi (503 gibi) degil, duruma uygun Turkce mesaj gider:
 *  - en az bir kaynak duzgun cevap verdiyse -> "bulunamadi" (Success(null))
 *  - hicbir kaynaga ulasilamadiysa          -> baglanti/servis hatasi
 */
@Singleton
class KitapAramaRepositoryImpl @Inject constructor(
    private val googleBooks: GoogleBooksService,
    private val openLibrary: OpenLibraryService
) : KitapAramaRepository {

    /** Tek bir kaynaktan donen cevabin ozeti. */
    private data class KaynakCevabi(
        val kitap: Kitap? = null,
        /** Sunucu duzgun cevap verdi (sonuc bos olabilir). */
        val cevapAlindi: Boolean = false,
        /** Aga hic cikilamadi ya da zaman asimi oldu. */
        val baglantiSorunu: Boolean = false,
        val hata: Throwable? = null
    )

    override suspend fun isbnIleAra(isbn: String): AppResult<Kitap?> {
        val varyantlar = IsbnUtils.varyantlar(isbn)
        if (varyantlar.isEmpty()) {
            return AppResult.Error("Geçersiz ISBN numarası.")
        }

        var birlesik: Kitap? = null
        var herhangiCevap = false
        var baglantiSorunu = false
        var sonHata: Throwable? = null

        fun isle(cevap: KaynakCevabi) {
            if (cevap.cevapAlindi) herhangiCevap = true
            if (cevap.baglantiSorunu) baglantiSorunu = true
            cevap.hata?.let { sonHata = it }
            birlesik = when {
                birlesik == null -> cevap.kitap
                cevap.kitap == null -> birlesik
                else -> birlesik!!.eksikleriDoldur(cevap.kitap)
            }
        }

        for (varyant in varyantlar) {
            // 1) Google Books - ISBN indeksi
            isle(googleIle(sorgu = "isbn:$varyant", isbn = varyant, isbnDogrula = false))
            if (birlesik.yeterliMi()) return AppResult.Success(birlesik)

            // 2) Google Books - serbest metin.
            //    Bu sorgu alakasiz kitaplar dondurebiliyor (olmayan bir ISBN icin bile
            //    sonuc geliyor), o yuzden gelen kaydin ISBN'i gercekten tutuyor mu diye
            //    kontrol ediyoruz.
            isle(googleIle(sorgu = varyant, isbn = varyant, isbnDogrula = true))
            if (birlesik.yeterliMi()) return AppResult.Success(birlesik)

            // 3) OpenLibrary
            isle(openLibraryIle(varyant))
            if (birlesik.yeterliMi()) return AppResult.Success(birlesik)
        }

        return when {
            birlesik != null -> AppResult.Success(birlesik)

            // En az bir kaynak "sonuc yok" dediyse bu bir hata degil, bulunamama halidir.
            herhangiCevap -> AppResult.Success(null)

            baglantiSorunu -> AppResult.Error(
                "İnternete bağlanılamadı. Bağlantını kontrol edip tekrar dene."
            )

            else -> AppResult.Error(
                sonHata?.kullaniciMesaji()
                    ?: "Kitap servisine şu an ulaşılamıyor. Bilgileri elle girebilirsin.",
                sonHata
            )
        }
    }

    override suspend fun isimleAra(sorgu: String): AppResult<List<Kitap>> {
        if (sorgu.isBlank()) return AppResult.Success(emptyList())

        return try {
            val yanit = withTimeoutOrNull(ZAMAN_ASIMI) { googleBooks.ara(sorgu = sorgu) }
                ?: return AppResult.Error("Arama zaman aşımına uğradı. Tekrar dene.")

            val sonuclar = yanit.ogeler
                .mapNotNull { it.bilgi }
                .map { it.toKitap(isbn = "") }
                .filter { it.baslik.isNotBlank() }

            AppResult.Success(sonuclar)
        } catch (e: Exception) {
            AppResult.Error(e.kullaniciMesaji(), e)
        }
    }

    private suspend fun googleIle(
        sorgu: String,
        isbn: String,
        isbnDogrula: Boolean
    ): KaynakCevabi = try {
        val yanit = withTimeoutOrNull(ZAMAN_ASIMI) { googleBooks.ara(sorgu = sorgu) }

        if (yanit == null) {
            KaynakCevabi(baglantiSorunu = true)
        } else {
            val bilgi = yanit.ogeler
                .mapNotNull { it.bilgi }
                .firstOrNull { !isbnDogrula || it.isbnTutuyorMu(isbn) }

            KaynakCevabi(kitap = bilgi?.toKitap(isbn), cevapAlindi = true)
        }
    } catch (e: IOException) {
        KaynakCevabi(baglantiSorunu = true, hata = e)
    } catch (e: Exception) {
        KaynakCevabi(hata = e)
    }

    private suspend fun openLibraryIle(isbn: String): KaynakCevabi = try {
        val yanit = withTimeoutOrNull(ZAMAN_ASIMI) {
            openLibrary.kitap(anahtar = "ISBN:$isbn")
        }

        if (yanit == null) {
            KaynakCevabi(baglantiSorunu = true)
        } else {
            KaynakCevabi(kitap = yanit.values.firstOrNull()?.toKitap(isbn), cevapAlindi = true)
        }
    } catch (e: IOException) {
        KaynakCevabi(baglantiSorunu = true, hata = e)
    } catch (e: Exception) {
        KaynakCevabi(hata = e)
    }

    /** Serbest metin aramasinda donen kaydin ISBN'i gercekten aranan kitaba mi ait? */
    private fun GoogleBooksBilgi.isbnTutuyorMu(isbn: String): Boolean {
        val beklenen = IsbnUtils.varyantlar(isbn).toSet()
        return kimlikler.any { IsbnUtils.temizle(it.deger.orEmpty()) in beklenen }
    }

    private fun Kitap?.yeterliMi(): Boolean = this != null && yeterinceDolu()

    private companion object {
        const val ZAMAN_ASIMI = 8_000L
    }
}
