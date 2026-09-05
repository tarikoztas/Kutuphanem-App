package com.tarik.kutuphanem.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri

/**
 * Galeri veya kameradan gelen goruntuyu bellege sigacak sekilde okur.
 *
 * Telefon kameralari 12 MP'nin uzerinde kare uretir; tam boy okumak
 * OutOfMemory ile sonuclanabilir. Bu yuzden once boyut olculur, sonra
 * inSampleSize ile kucultulmus haliyle okunur ve EXIF donusu duzeltilir.
 */
fun Context.olcekliBitmapOku(uri: Uri, hedefKenar: Int = 1400): Bitmap? = try {
    val olcuSecenek = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, olcuSecenek)
    }

    val enBuyukKenar = maxOf(olcuSecenek.outWidth, olcuSecenek.outHeight)
    var ornekleme = 1
    while (enBuyukKenar / ornekleme > hedefKenar) {
        ornekleme *= 2
    }

    val okumaSecenek = BitmapFactory.Options().apply { inSampleSize = ornekleme }
    val bitmap = contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, okumaSecenek)
    }

    bitmap?.let { exifDonusunuUygula(uri, it) }
} catch (e: Exception) {
    null
}

private fun Context.exifDonusunuUygula(uri: Uri, bitmap: Bitmap): Bitmap {
    val yon = try {
        contentResolver.openInputStream(uri)?.use { akis ->
            ExifInterface(akis).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } ?: ExifInterface.ORIENTATION_NORMAL
    } catch (e: Exception) {
        ExifInterface.ORIENTATION_NORMAL
    }

    val aci = when (yon) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> return bitmap
    }

    val matris = Matrix().apply { postRotate(aci) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matris, true)
}
