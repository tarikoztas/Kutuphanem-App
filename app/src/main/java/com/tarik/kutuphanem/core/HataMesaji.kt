package com.tarik.kutuphanem.core

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** Teknik istisnalari kullanicinin anlayacagi Turkce mesaja cevirir. */
fun Throwable.kullaniciMesaji(): String = when (this) {
    is FirebaseAuthWeakPasswordException ->
        "Şifre çok zayıf. En az 6 karakter kullan."

    is FirebaseAuthInvalidCredentialsException ->
        "E-posta veya şifre hatalı."

    is FirebaseAuthInvalidUserException ->
        "Bu e-posta ile kayıtlı bir hesap bulunamadı."

    is FirebaseAuthUserCollisionException ->
        "Bu e-posta zaten kayıtlı. Giriş yapmayı dene."

    is FirebaseFirestoreException -> when (code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "Bu veriye erişim izni yok. Oturumun kapanmış olabilir."

        FirebaseFirestoreException.Code.UNAVAILABLE ->
            "Sunucuya ulaşılamadı. İnternet bağlantını kontrol et."

        else -> "Veritabanı hatası: ${message ?: "bilinmeyen sorun"}"
    }

    // Kitap servislerinden donen HTTP durumlari: ham "503" yerine anlasilir metin.
    is HttpException -> when (code()) {
        404 -> "Aradığınız kitap bulunamadı."
        429 -> "Çok fazla istek yapıldı. Birkaç dakika sonra tekrar dene."
        in 500..599 -> "Kitap servisine şu an ulaşılamıyor. Bilgileri elle girebilirsin."
        else -> "Kitap servisi beklenmedik bir cevap verdi. Tekrar dene."
    }

    is SocketTimeoutException ->
        "Bağlantı zaman aşımına uğradı. Tekrar dene."

    is UnknownHostException ->
        "İnternete bağlanılamadı. Bağlantını kontrol et."

    is IOException ->
        "İnternet bağlantısı yok gibi görünüyor."

    else -> message ?: "Beklenmeyen bir hata oluştu."
}
