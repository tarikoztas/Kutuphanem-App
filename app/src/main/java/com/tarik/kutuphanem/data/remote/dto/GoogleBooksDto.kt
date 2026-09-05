package com.tarik.kutuphanem.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleBooksYanit(
    @SerialName("totalItems") val toplam: Int = 0,
    @SerialName("items") val ogeler: List<GoogleBooksOge> = emptyList()
)

@Serializable
data class GoogleBooksOge(
    @SerialName("volumeInfo") val bilgi: GoogleBooksBilgi? = null
)

@Serializable
data class GoogleBooksBilgi(
    @SerialName("title") val baslik: String? = null,
    @SerialName("subtitle") val altBaslik: String? = null,
    @SerialName("authors") val yazarlar: List<String> = emptyList(),
    @SerialName("publisher") val yayinevi: String? = null,
    @SerialName("publishedDate") val yayinTarihi: String? = null,
    @SerialName("description") val aciklama: String? = null,
    @SerialName("pageCount") val sayfaSayisi: Int? = null,
    @SerialName("language") val dil: String? = null,
    @SerialName("imageLinks") val gorseller: GoogleBooksGorsel? = null,
    @SerialName("industryIdentifiers") val kimlikler: List<GoogleBooksKimlik> = emptyList()
)

@Serializable
data class GoogleBooksGorsel(
    @SerialName("smallThumbnail") val kucuk: String? = null,
    @SerialName("thumbnail") val orta: String? = null
)

@Serializable
data class GoogleBooksKimlik(
    @SerialName("type") val tur: String? = null,
    @SerialName("identifier") val deger: String? = null
)
