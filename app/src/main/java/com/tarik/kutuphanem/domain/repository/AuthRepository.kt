package com.tarik.kutuphanem.domain.repository

import com.tarik.kutuphanem.core.AppResult
import com.tarik.kutuphanem.domain.model.Kullanici
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** Oturum durumu: null = giris yapilmamis. Uygulama boyunca canli dinlenir. */
    val oturumAkisi: Flow<Kullanici?>

    val mevcutKullaniciId: String?

    suspend fun kayitOl(adSoyad: String, eposta: String, sifre: String): AppResult<Unit>

    suspend fun girisYap(eposta: String, sifre: String): AppResult<Unit>

    suspend fun sifreSifirlamaGonder(eposta: String): AppResult<Unit>

    fun cikisYap()
}
