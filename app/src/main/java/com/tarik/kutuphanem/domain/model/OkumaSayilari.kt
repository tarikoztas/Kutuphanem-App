package com.tarik.kutuphanem.domain.model

/** Bir kutuphanedeki kitaplarin okundu / okunmadi dagilimi. */
data class OkumaSayilari(
    val toplam: Int = 0,
    val okunan: Int = 0
) {
    val okunmayan: Int get() = toplam - okunan
}

fun List<Kitap>.okumaSayilari() = OkumaSayilari(toplam = size, okunan = count { it.okundu })

/** Genel aramada bir kitabi hangi kutuphaneden geldigiyle birlikte tasir. */
data class KutuphaneKitabi(
    val kutuphaneId: String,
    val kutuphaneAdi: String,
    val kitap: Kitap
)
