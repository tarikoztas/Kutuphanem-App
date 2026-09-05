package com.tarik.kutuphanem.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.tarik.kutuphanem.core.AppResult
import com.tarik.kutuphanem.core.kullaniciMesaji
import com.tarik.kutuphanem.core.turkceKarsilastirici
import com.tarik.kutuphanem.domain.model.Kutuphane
import com.tarik.kutuphanem.domain.repository.KutuphaneRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KutuphaneRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val kaynak: FirestoreKaynak
) : KutuphaneRepository {

    private val uid: String? get() = auth.currentUser?.uid

    override fun kutuphaneler(): Flow<AppResult<List<Kutuphane>>> {
        val kullaniciId = uid ?: return flowOf(AppResult.Error(OTURUM_YOK))

        return callbackFlow {
            trySend(AppResult.Loading)

            val kayit = kaynak.kutuphaneler(kullaniciId)
                .addSnapshotListener { snapshot, hata ->
                    when {
                        hata != null -> trySend(AppResult.Error(hata.kullaniciMesaji(), hata))

                        snapshot != null -> {
                            val liste = snapshot.documents
                                .map { it.toKutuphane() }
                                .sortedWith(compareBy(turkceKarsilastirici) { it.ad })
                            trySend(AppResult.Success(liste))
                        }
                    }
                }

            awaitClose { kayit.remove() }
        }
    }

    override suspend fun ekle(ad: String): AppResult<String> = try {
        val kullaniciId = uid ?: throw IllegalStateException(OTURUM_YOK)
        val doc = kaynak.kutuphaneler(kullaniciId).document()
        doc.set(
            mapOf(
                "name" to ad.trim(),
                "bookCount" to 0L,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
        AppResult.Success(doc.id)
    } catch (e: Exception) {
        AppResult.Error(e.kullaniciMesaji(), e)
    }

    override suspend fun yenidenAdlandir(kutuphaneId: String, yeniAd: String): AppResult<Unit> = try {
        val kullaniciId = uid ?: throw IllegalStateException(OTURUM_YOK)
        kaynak.kutuphane(kullaniciId, kutuphaneId)
            .update("name", yeniAd.trim())
            .await()
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e.kullaniciMesaji(), e)
    }

    override suspend fun sil(kutuphaneId: String): AppResult<Unit> = try {
        val kullaniciId = uid ?: throw IllegalStateException(OTURUM_YOK)
        // Firestore alt koleksiyonlari kendiliginden silmez; kitaplari once biz temizliyoruz.
        kitaplariTemizle(kullaniciId, kutuphaneId)
        kaynak.kutuphane(kullaniciId, kutuphaneId).delete().await()
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e.kullaniciMesaji(), e)
    }

    private suspend fun kitaplariTemizle(kullaniciId: String, kutuphaneId: String) {
        while (true) {
            val sayfa = kaynak.kitaplar(kullaniciId, kutuphaneId)
                .limit(SAYFA_BOYU)
                .get()
                .await()

            if (sayfa.isEmpty) return

            // Her kitap icin 2 islem (kapak + kitap) -> 500'luk batch sinirini asmamak icin
            // sayfa boyu 200 tutuluyor.
            val batch = db.batch()
            sayfa.documents.forEach { doc ->
                batch.delete(kaynak.kapak(kullaniciId, kutuphaneId, doc.id))
                batch.delete(doc.reference)
            }
            batch.commit().await()

            if (sayfa.size() < SAYFA_BOYU) return
        }
    }

    private fun DocumentSnapshot.toKutuphane() = Kutuphane(
        id = id,
        ad = getString("name").orEmpty(),
        kitapSayisi = (getLong("bookCount") ?: 0L).toInt(),
        olusturmaZamani = getLong("createdAt") ?: 0L
    )

    private companion object {
        const val OTURUM_YOK = "Oturum bulunamadı. Lütfen tekrar giriş yap."
        const val SAYFA_BOYU = 200L
    }
}
