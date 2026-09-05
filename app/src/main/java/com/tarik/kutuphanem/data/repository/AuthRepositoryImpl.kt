package com.tarik.kutuphanem.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.tarik.kutuphanem.core.AppResult
import com.tarik.kutuphanem.core.kullaniciMesaji
import com.tarik.kutuphanem.domain.model.Kullanici
import com.tarik.kutuphanem.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val kaynak: FirestoreKaynak
) : AuthRepository {

    override val oturumAkisi: Flow<Kullanici?> = callbackFlow {
        val dinleyici = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val kullanici = firebaseAuth.currentUser?.let {
                Kullanici(
                    id = it.uid,
                    adSoyad = it.displayName.orEmpty(),
                    eposta = it.email.orEmpty()
                )
            }
            trySend(kullanici)
        }
        auth.addAuthStateListener(dinleyici)
        awaitClose { auth.removeAuthStateListener(dinleyici) }
    }

    override val mevcutKullaniciId: String?
        get() = auth.currentUser?.uid

    override suspend fun kayitOl(adSoyad: String, eposta: String, sifre: String): AppResult<Unit> =
        try {
            val sonuc = auth.createUserWithEmailAndPassword(eposta.trim(), sifre).await()
            val kullanici = requireNotNull(sonuc.user) { "Kullanıcı oluşturulamadı." }

            // Ad soyad hem Auth profiline hem de Firestore dokumanina yazilir:
            // profil adi cihazlar arasi hemen gelir, Firestore dokumani ise kalici kayittir.
            kullanici.updateProfile(
                userProfileChangeRequest { displayName = adSoyad.trim() }
            ).await()

            kaynak.kullanici(kullanici.uid).set(
                mapOf(
                    "fullName" to adSoyad.trim(),
                    "email" to eposta.trim(),
                    "createdAt" to System.currentTimeMillis()
                )
            ).await()

            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.kullaniciMesaji(), e)
        }

    override suspend fun girisYap(eposta: String, sifre: String): AppResult<Unit> = try {
        auth.signInWithEmailAndPassword(eposta.trim(), sifre).await()
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e.kullaniciMesaji(), e)
    }

    override suspend fun sifreSifirlamaGonder(eposta: String): AppResult<Unit> = try {
        auth.sendPasswordResetEmail(eposta.trim()).await()
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e.kullaniciMesaji(), e)
    }

    override fun cikisYap() = auth.signOut()
}
