package com.tarik.kutuphanem.domain.model

data class Kitap(
    val id: String = "",
    val baslik: String = "",
    val yazarlar: List<String> = emptyList(),
    val yayinevi: String = "",
    val sayfaSayisi: Int? = null,
    val isbn: String = "",
    val aciklama: String = "",
    /** API'den gelen uzak kapak adresi (varsa). */
    val kapakUrl: String = "",
    /** Kullanici kamerayla kapak cektiyse true; goruntu ayri dokumanda tutulur. */
    val yerelKapakVar: Boolean = false,
    val okundu: Boolean = false,
    val eklenmeZamani: Long = 0L
) {
    val yazarMetni: String get() = yazarlar.joinToString(", ")
}
