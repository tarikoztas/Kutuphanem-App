package com.tarik.kutuphanem.data.mapper

import com.tarik.kutuphanem.data.remote.dto.GoogleBooksBilgi
import com.tarik.kutuphanem.data.remote.dto.OpenLibraryKitap
import com.tarik.kutuphanem.domain.model.Kitap

/** Google Books kapak adresini duzeltir: https'e cevirir ve kivrik kenar efektini atar. */
private fun String.kapakAdresiniDuzelt(): String =
    replace("http://", "https://")
        .replace("&edge=curl", "")

fun GoogleBooksBilgi.toKitap(isbn: String): Kitap {
    val tamBaslik = listOfNotNull(
        baslik?.takeIf { it.isNotBlank() },
        altBaslik?.takeIf { it.isNotBlank() }
    ).joinToString(": ")

    val kimliktenIsbn = kimlikler
        .firstOrNull { it.tur == "ISBN_13" }
        ?.deger
        ?: kimlikler.firstOrNull { it.tur == "ISBN_10" }?.deger

    return Kitap(
        baslik = tamBaslik,
        yazarlar = yazarlar,
        yayinevi = yayinevi.orEmpty(),
        sayfaSayisi = sayfaSayisi?.takeIf { it > 0 },
        isbn = isbn.ifBlank { kimliktenIsbn.orEmpty() },
        aciklama = aciklama.orEmpty(),
        kapakUrl = (gorseller?.orta ?: gorseller?.kucuk).orEmpty().kapakAdresiniDuzelt()
    )
}

fun OpenLibraryKitap.toKitap(isbn: String): Kitap = Kitap(
    baslik = listOfNotNull(
        baslik?.takeIf { it.isNotBlank() },
        altBaslik?.takeIf { it.isNotBlank() }
    ).joinToString(": "),
    yazarlar = yazarlar.mapNotNull { it.ad }.filter { it.isNotBlank() },
    yayinevi = yayinevleri.mapNotNull { it.ad }.firstOrNull().orEmpty(),
    sayfaSayisi = sayfaSayisi?.takeIf { it > 0 },
    isbn = isbn,
    aciklama = notlar.orEmpty(),
    kapakUrl = (kapak?.buyuk ?: kapak?.orta ?: kapak?.kucuk)
        ?.kapakAdresiniDuzelt()
        ?: if (isbn.isNotBlank()) "https://covers.openlibrary.org/b/isbn/$isbn-L.jpg" else ""
)

/**
 * Iki kaynaktan gelen bilgiyi alan alan birlestirir: bu kayittaki bos alanlar
 * [digeri] ile doldurulur, dolu alanlara dokunulmaz.
 */
fun Kitap.eksikleriDoldur(digeri: Kitap?): Kitap {
    if (digeri == null) return this
    return copy(
        baslik = baslik.ifBlank { digeri.baslik },
        yazarlar = yazarlar.ifEmpty { digeri.yazarlar },
        yayinevi = yayinevi.ifBlank { digeri.yayinevi },
        sayfaSayisi = sayfaSayisi ?: digeri.sayfaSayisi,
        isbn = isbn.ifBlank { digeri.isbn },
        aciklama = aciklama.ifBlank { digeri.aciklama },
        kapakUrl = kapakUrl.ifBlank { digeri.kapakUrl }
    )
}

/** Kullaniciyi elle doldurma ekranina gondermeye gerek kalmayacak kadar dolu mu? */
fun Kitap.yeterinceDolu(): Boolean =
    baslik.isNotBlank() && yazarlar.isNotEmpty() && yayinevi.isNotBlank() && sayfaSayisi != null
