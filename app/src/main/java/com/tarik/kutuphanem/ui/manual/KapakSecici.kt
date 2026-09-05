package com.tarik.kutuphanem.ui.manual

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.tarik.kutuphanem.core.kapakIcinSikistir
import com.tarik.kutuphanem.core.olcekliBitmapOku
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Kamera/galeri secimini yoneten yardimci.
 *
 * Goruntu once cihazda kucultulup JPEG'e sikistirilir, sonra base64'e cevrilir;
 * Firestore dokumanina bu metin yazilir (Cloud Storage gerekmez).
 */
class KapakSeciciDurumu(
    val kameraIleCek: () -> Unit,
    val galeridenSec: () -> Unit
)

@Composable
fun rememberKapakSecici(
    onKapakHazir: (String) -> Unit,
    onHata: (String) -> Unit
): KapakSeciciDurumu {
    val context = LocalContext.current
    val kapsam = rememberCoroutineScope()
    var kameraHedefi by remember { mutableStateOf<Uri?>(null) }

    fun isle(uri: Uri?) {
        if (uri == null) return
        kapsam.launch {
            val base64 = withContext(Dispatchers.Default) {
                context.olcekliBitmapOku(uri)?.kapakIcinSikistir()
            }
            if (base64 == null) onHata("Görsel okunamadı, başka bir fotoğraf dene.") else onKapakHazir(base64)
        }
    }

    val galeriSecici = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> isle(uri) }

    val kameraSecici = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { basarili -> if (basarili) isle(kameraHedefi) }

    // Manifest'te CAMERA izni tanimli oldugu icin, fotograf cekme niyeti de
    // iznin verilmis olmasini bekler.
    val izinIsteyici = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { verildi ->
        if (verildi) {
            val hedef = context.gecicKapakHedefi()
            kameraHedefi = hedef
            kameraSecici.launch(hedef)
        } else {
            onHata("Kapak çekmek için kamera izni gerekiyor.")
        }
    }

    return remember {
        KapakSeciciDurumu(
            kameraIleCek = {
                if (context.kameraIzniVar()) {
                    val hedef = context.gecicKapakHedefi()
                    kameraHedefi = hedef
                    kameraSecici.launch(hedef)
                } else {
                    izinIsteyici.launch(Manifest.permission.CAMERA)
                }
            },
            galeridenSec = {
                galeriSecici.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }
}

private fun Context.kameraIzniVar(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

private fun Context.gecicKapakHedefi(): Uri {
    val klasor = File(cacheDir, "kapaklar").apply { mkdirs() }
    val dosya = File(klasor, "kapak_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(this, "$packageName.fileprovider", dosya)
}
