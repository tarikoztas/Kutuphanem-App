package com.tarik.kutuphanem.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

/** Firestore'da metin olarak tutulan kapagi Bitmap'e cevirir. */
fun String.base64ToBitmap(): Bitmap? = try {
    val bytes = Base64.decode(this, Base64.NO_WRAP)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
} catch (e: IllegalArgumentException) {
    null
}

/**
 * Kapagi Firestore dokumanina sigacak boyuta indirir.
 *
 * Once uzun kenari [enBuyukKenar] olacak sekilde olceklenir, sonra JPEG kalitesi
 * kademeli dusurulerek [enBuyukBayt] sinirinin altina cekilir.
 */
fun Bitmap.kapakIcinSikistir(
    enBuyukKenar: Int = 700,
    enBuyukBayt: Int = 150 * 1024
): String {
    val olcek = minOf(
        1f,
        enBuyukKenar.toFloat() / maxOf(width, height).toFloat()
    )
    val kucuk = if (olcek < 1f) {
        Bitmap.createScaledBitmap(this, (width * olcek).toInt(), (height * olcek).toInt(), true)
    } else {
        this
    }

    var kalite = 80
    var bayt: ByteArray
    do {
        val cikti = ByteArrayOutputStream()
        kucuk.compress(Bitmap.CompressFormat.JPEG, kalite, cikti)
        bayt = cikti.toByteArray()
        kalite -= 15
    } while (bayt.size > enBuyukBayt && kalite >= 30)

    return Base64.encodeToString(bayt, Base64.NO_WRAP)
}
