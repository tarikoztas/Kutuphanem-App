package com.tarik.kutuphanem.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.tarik.kutuphanem.core.AppResult
import com.tarik.kutuphanem.core.kullaniciMesaji
import com.tarik.kutuphanem.core.turkceKarsilastirici
import com.tarik.kutuphanem.domain.model.Kitap
import com.tarik.kutuphanem.domain.repository.KapakDegisikligi
import com.tarik.kutuphanem.domain.repository.KitapRepository
import com.tarik.kutuphanem.domain.repository.Oneriler
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KitapRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val kaynak: FirestoreKaynak
) : KitapRepository {

    private val uid: String? get() = auth.currentUser?.uid

    override fun kitaplar(kutuphaneId: String): Flow<AppResult<List<Kitap>>> {
        val kullaniciId = uid ?: return flowOf(AppResult.Error(OTURUM_YOK))

        return callbackFlow {
            trySend(AppResult.Loading)

            val kayit = kaynak.kitaplar(kullaniciId, kutuphaneId)
                .addSnapshotListener { snapshot, hata ->
                    when {
                        hata != null -> trySend(AppResult.Error(hata.kullaniciMesaji(), hata))

                        snapshot != null -> {
                            val liste = snapshot.documents
                                .map { it.toKitap() }
                                .sortedWith(compareBy(turkceKarsilastirici) { it.baslik })
                            trySend(AppResult.Success(liste))
                        }
                    }
                }

            awaitClose { kayit.remove() }
        }
    }

    override fun kitap(kutuphaneId: String, kitapId: String): Flow<AppResult<Kitap>> {
        val kullaniciId = uid ?: return flowOf(AppResult.Error(OTURUM_YOK))

        return callbackFlow {
            trySend(AppResult.Loading)

            val kayit = kaynak.kitap(kullaniciId, kutuphaneId, kitapId)
                .addSnapshotListener { doc, hata ->
                    when {
                        hata != null -> trySend(AppResult.Error(hata.kullaniciMesaji(), hata))
                        doc == null || !doc.exists() -> trySend(AppResult.Error("Kitap bulunamadı, silinmiş olabilir."))
                        else -> trySend(AppResult.Success(doc.toKitap()))
                    }
                }

            awaitClose { kayit.remove() }
        }
    }

    override suspend fun ekle(
        kutuphaneId: String,
        kitap: Kitap,
        kapak: KapakDegisikligi
    ): AppResult<String> = try {
        val kullaniciId = uid ?: throw IllegalStateException(OTURUM_YOK)
        val doc = kaynak.kitaplar(kullaniciId, kutuphaneId).document()
        val yerelKapakVar = kapak is KapakDegisikligi.Yeni

        // Kitap yazimi ve kutuphanedeki sayacin artisi tek batch'te; biri olup digeri
        // olmayan bir durum kalmasin.
        val batch = db.batch()
        batch.set(
            doc,
            kitap.copy(
                yerelKapakVar = yerelKapakVar,
                eklenmeZamani = System.currentTimeMillis()
            ).toMap()
        )
        batch.update(
            kaynak.kutuphane(kullaniciId, kutuphaneId),
            "bookCount",
            FieldValue.increment(1)
        )
        batch.commit().await()

        kapakYaz(kullaniciId, kutuphaneId, doc.id, kapak)
        AppResult.Success(doc.id)
    } catch (e: Exception) {
        AppResult.Error(e.kullaniciMesaji(), e)
    }

    override suspend fun guncelle(
        kutuphaneId: String,
        kitap: Kitap,
        kapak: KapakDegisikligi
    ): AppResult<Unit> = try {
        val kullaniciId = uid ?: throw IllegalStateException(OTURUM_YOK)

        val yerelKapakVar = when (kapak) {
            is KapakDegisikligi.Yeni -> true
            KapakDegisikligi.Kaldir -> false
            KapakDegisikligi.Dokunma -> kitap.yerelKapakVar
        }

        kaynak.kitap(kullaniciId, kutuphaneId, kitap.id)
            .set(kitap.copy(yerelKapakVar = yerelKapakVar).toMap())
            .await()

        kapakYaz(kullaniciId, kutuphaneId, kitap.id, kapak)
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e.kullaniciMesaji(), e)
    }

    override suspend fun okunduDegistir(
        kutuphaneId: String,
        kitapId: String,
        okundu: Boolean
    ): AppResult<Unit> = try {
        val kullaniciId = uid ?: throw IllegalStateException(OTURUM_YOK)
        kaynak.kitap(kullaniciId, kutuphaneId, kitapId)
            .update("isRead", okundu)
            .await()
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e.kullaniciMesaji(), e)
    }

    override suspend fun sil(kutuphaneId: String, kitapId: String): AppResult<Unit> = try {
        val kullaniciId = uid ?: throw IllegalStateException(OTURUM_YOK)

        val batch = db.batch()
        batch.delete(kaynak.kapak(kullaniciId, kutuphaneId, kitapId))
        batch.delete(kaynak.kitap(kullaniciId, kutuphaneId, kitapId))
        batch.update(
            kaynak.kutuphane(kullaniciId, kutuphaneId),
            "bookCount",
            FieldValue.increment(-1)
        )
        batch.commit().await()

        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e.kullaniciMesaji(), e)
    }

    override suspend fun kapakGetir(kutuphaneId: String, kitapId: String): AppResult<String?> = try {
        val kullaniciId = uid ?: throw IllegalStateException(OTURUM_YOK)
        val doc = kaynak.kapak(kullaniciId, kutuphaneId, kitapId).get().await()
        AppResult.Success(doc.getString(ALAN_KAPAK_BASE64))
    } catch (e: Exception) {
        AppResult.Error(e.kullaniciMesaji(), e)
    }

    override suspend fun oneriler(kutuphaneId: String): AppResult<Oneriler> = try {
        val kullaniciId = uid ?: throw IllegalStateException(OTURUM_YOK)
        val snapshot = kaynak.kitaplar(kullaniciId, kutuphaneId).get().await()

        val yazarlar = sortedSetOf<String>()
        val yayinevleri = sortedSetOf<String>()
        snapshot.documents.forEach { doc ->
            (doc.get("authors") as? List<*>)?.forEach { yazar ->
                (yazar as? String)?.takeIf { it.isNotBlank() }?.let { yazarlar.add(it) }
            }
            doc.getString("publisher")?.takeIf { it.isNotBlank() }?.let { yayinevleri.add(it) }
        }

        AppResult.Success(
            Oneriler(
                yazarlar = yazarlar.sortedWith(turkceKarsilastirici),
                yayinevleri = yayinevleri.sortedWith(turkceKarsilastirici)
            )
        )
    } catch (e: Exception) {
        AppResult.Error(e.kullaniciMesaji(), e)
    }

    override suspend fun isbnVarMi(kutuphaneId: String, isbn: String): AppResult<Kitap?> = try {
        val kullaniciId = uid ?: throw IllegalStateException(OTURUM_YOK)
        if (isbn.isBlank()) {
            AppResult.Success(null)
        } else {
            val snapshot = kaynak.kitaplar(kullaniciId, kutuphaneId)
                .whereEqualTo("isbn", isbn)
                .limit(1)
                .get()
                .await()
            AppResult.Success(snapshot.documents.firstOrNull()?.toKitap())
        }
    } catch (e: Exception) {
        AppResult.Error(e.kullaniciMesaji(), e)
    }

    private suspend fun kapakYaz(
        kullaniciId: String,
        kutuphaneId: String,
        kitapId: String,
        kapak: KapakDegisikligi
    ) {
        val ref = kaynak.kapak(kullaniciId, kutuphaneId, kitapId)
        when (kapak) {
            KapakDegisikligi.Dokunma -> Unit
            KapakDegisikligi.Kaldir -> ref.delete().await()
            is KapakDegisikligi.Yeni -> ref.set(
                mapOf(
                    ALAN_KAPAK_BASE64 to kapak.base64,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
        }
    }

    private fun Kitap.toMap(): Map<String, Any?> = mapOf(
        "title" to baslik.trim(),
        "authors" to yazarlar.map { it.trim() }.filter { it.isNotEmpty() },
        "publisher" to yayinevi.trim(),
        "pageCount" to sayfaSayisi,
        "isbn" to isbn.trim(),
        "description" to aciklama.trim(),
        "coverUrl" to kapakUrl.trim(),
        "hasLocalCover" to yerelKapakVar,
        "isRead" to okundu,
        "addedAt" to if (eklenmeZamani == 0L) System.currentTimeMillis() else eklenmeZamani
    )

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toKitap() = Kitap(
        id = id,
        baslik = getString("title").orEmpty(),
        yazarlar = (get("authors") as? List<String>).orEmpty(),
        yayinevi = getString("publisher").orEmpty(),
        sayfaSayisi = getLong("pageCount")?.toInt(),
        isbn = getString("isbn").orEmpty(),
        aciklama = getString("description").orEmpty(),
        kapakUrl = getString("coverUrl").orEmpty(),
        yerelKapakVar = getBoolean("hasLocalCover") ?: false,
        okundu = getBoolean("isRead") ?: false,
        eklenmeZamani = getLong("addedAt") ?: 0L
    )

    private companion object {
        const val OTURUM_YOK = "Oturum bulunamadı. Lütfen tekrar giriş yap."
        const val ALAN_KAPAK_BASE64 = "base64"
    }
}
