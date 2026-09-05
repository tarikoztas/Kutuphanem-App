package com.tarik.kutuphanem.ui.navigation

import kotlinx.serialization.Serializable

/** Navigation Compose'un tip guvenli rotalari (nav 2.8+). */

@Serializable
data object GirisRoute

@Serializable
data object KayitRoute

@Serializable
data object KutuphanelerRoute

@Serializable
data class KitapListesiRoute(val kutuphaneId: String, val kutuphaneAdi: String)

@Serializable
data class KitapDetayRoute(val kutuphaneId: String, val kitapId: String)

/**
 * Ekleme ve duzenleme ayni ekran:
 *  - kitapId null  -> yeni kitap
 *  - isbn dolu     -> barkoddan gelen on doldurma
 */
@Serializable
data class KitapFormRoute(
    val kutuphaneId: String,
    val kitapId: String? = null,
    val isbn: String? = null
)

@Serializable
data class TarayiciRoute(val kutuphaneId: String)

@Serializable
data class IsimleAramaRoute(val kutuphaneId: String)
