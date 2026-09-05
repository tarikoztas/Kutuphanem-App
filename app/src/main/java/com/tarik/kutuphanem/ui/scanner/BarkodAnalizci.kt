package com.tarik.kutuphanem.ui.scanner

import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * Kamera karelerini ML Kit'e verir ve okunan kodu yukari bildirir.
 *
 * Sadece kitap barkodu turleri dinlenir (EAN-13/EAN-8/UPC-A).
 *
 * Onemli: okuma butun kareye degil, ekrandaki cerceveye karsilik gelen
 * bolgeye (ROI) sinirlidir. Cerceve ile buradaki oranlar ayni degerlerden
 * beslendigi icin kullanicinin gordugu alan ile okunan alan birebir ortusur.
 */
class BarkodAnalizci(
    private val roiGenislikOrani: Float,
    private val roiYukseklikOrani: Float,
    private val onBarkod: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val tarayici = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A
            )
            .build()
    )

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val kare = imageProxy.image
        if (kare == null) {
            imageProxy.close()
            return
        }

        val donus = imageProxy.imageInfo.rotationDegrees

        // rotationDegrees verildiginde ML Kit boundingBox'i "dik" (ekrandaki gibi)
        // koordinatlarda dondurur; olculeri de ona gore hesapliyoruz.
        val dikGenislik = if (donus == 90 || donus == 270) imageProxy.height else imageProxy.width
        val dikYukseklik = if (donus == 90 || donus == 270) imageProxy.width else imageProxy.height

        val roi = ilgiAlani(dikGenislik, dikYukseklik)

        tarayici.process(InputImage.fromMediaImage(kare, donus))
            .addOnSuccessListener { barkodlar ->
                barkodlar
                    .firstOrNull { barkod ->
                        val kutu = barkod.boundingBox
                        kutu != null && roi.contains(kutu.centerX(), kutu.centerY())
                    }
                    ?.rawValue
                    ?.let(onBarkod)
            }
            .addOnCompleteListener {
                // Kare her durumda kapatilmali, yoksa kamera akisi tikanir.
                imageProxy.close()
            }
    }

    private fun ilgiAlani(genislik: Int, yukseklik: Int): Rect {
        val roiG = genislik * roiGenislikOrani
        val roiY = yukseklik * roiYukseklikOrani
        val sol = (genislik - roiG) / 2f
        val ust = (yukseklik - roiY) / 2f
        return Rect(
            sol.toInt(),
            ust.toInt(),
            (sol + roiG).toInt(),
            (ust + roiY).toInt()
        )
    }
}
