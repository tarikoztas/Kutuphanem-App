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
