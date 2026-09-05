package com.tarik.kutuphanem.data.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore yol semasini tek yerde toplar:
 *   users/{uid}/libraries/{libId}/books/{bookId}/media/cover
 */
@Singleton
class FirestoreKaynak @Inject constructor(
    private val db: FirebaseFirestore
) {
    fun kullanici(uid: String): DocumentReference =
        db.collection(KOLEKSIYON_KULLANICILAR).document(uid)

    fun kutuphaneler(uid: String): CollectionReference =
        kullanici(uid).collection(KOLEKSIYON_KUTUPHANELER)

    fun kutuphane(uid: String, kutuphaneId: String): DocumentReference =
        kutuphaneler(uid).document(kutuphaneId)

    fun kitaplar(uid: String, kutuphaneId: String): CollectionReference =
        kutuphane(uid, kutuphaneId).collection(KOLEKSIYON_KITAPLAR)

    fun kitap(uid: String, kutuphaneId: String, kitapId: String): DocumentReference =
        kitaplar(uid, kutuphaneId).document(kitapId)

    /** Base64 kapak: liste sorgusuna dahil olmamasi icin ayri alt dokumanda. */
    fun kapak(uid: String, kutuphaneId: String, kitapId: String): DocumentReference =
        kitap(uid, kutuphaneId, kitapId).collection(KOLEKSIYON_MEDYA).document(DOKUMAN_KAPAK)

    companion object {
        const val KOLEKSIYON_KULLANICILAR = "users"
        const val KOLEKSIYON_KUTUPHANELER = "libraries"
        const val KOLEKSIYON_KITAPLAR = "books"
        const val KOLEKSIYON_MEDYA = "media"
        const val DOKUMAN_KAPAK = "cover"
    }
}
