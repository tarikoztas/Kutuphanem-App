package com.tarik.kutuphanem.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * OpenLibrary "jscmd=data" cevabi, anahtari "ISBN:9789750718533" olan bir sozluk doner.
 * Bu yuzden servis Map<String, OpenLibraryKitap> olarak cozulur.
 */
@Serializable
data class OpenLibraryKitap(
    @SerialName("title") val baslik: String? = null,
    @SerialName("subtitle") val altBaslik: String? = null,
    @SerialName("authors") val yazarlar: List<OpenLibraryAd> = emptyList(),
    @SerialName("publishers") val yayinevleri: List<OpenLibraryAd> = emptyList(),
    @SerialName("number_of_pages") val sayfaSayisi: Int? = null,
    @SerialName("publish_date") val yayinTarihi: String? = null,
    @SerialName("cover") val kapak: OpenLibraryKapak? = null,
    @SerialName("notes") val notlar: String? = null
)

/** search.json cevabi: {"numFound": 31, "docs": [ ... ]} */
@Serializable
data class OpenLibraryAramaYaniti(
    @SerialName("docs") val ogeler: List<OpenLibraryAramaOgesi> = emptyList()
)

@Serializable
data class OpenLibraryAramaOgesi(
    @SerialName("title") val baslik: String? = null,
    @SerialName("subtitle") val altBaslik: String? = null,
    @SerialName("author_name") val yazarlar: List<String> = emptyList(),
    @SerialName("publisher") val yayinevleri: List<String> = emptyList(),
    @SerialName("number_of_pages_median") val sayfaSayisi: Int? = null,
    @SerialName("isbn") val isbnler: List<String> = emptyList(),
    @SerialName("cover_i") val kapakId: Long? = null
)

@Serializable
data class OpenLibraryAd(
    @SerialName("name") val ad: String? = null
)

@Serializable
data class OpenLibraryKapak(
    @SerialName("small") val kucuk: String? = null,
    @SerialName("medium") val orta: String? = null,
    @SerialName("large") val buyuk: String? = null
)
