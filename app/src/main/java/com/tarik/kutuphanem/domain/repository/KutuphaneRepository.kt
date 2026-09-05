package com.tarik.kutuphanem.domain.repository

import com.tarik.kutuphanem.core.AppResult
import com.tarik.kutuphanem.domain.model.Kutuphane
import kotlinx.coroutines.flow.Flow

interface KutuphaneRepository {

    /** Canli liste: Firestore'daki her degisiklikte yeniden yayinlanir. */
    fun kutuphaneler(): Flow<AppResult<List<Kutuphane>>>

    suspend fun ekle(ad: String): AppResult<String>

    suspend fun yenidenAdlandir(kutuphaneId: String, yeniAd: String): AppResult<Unit>

    /** Kutuphaneyi ve icindeki tum kitaplari siler. */
    suspend fun sil(kutuphaneId: String): AppResult<Unit>
}
