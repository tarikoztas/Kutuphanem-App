package com.tarik.kutuphanem.ui.scanner

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tarik.kutuphanem.core.isbn.IsbnUtils
import java.util.concurrent.Executors

/**
 * Cerceve olculeri. Hem ekrandaki dikdortgen hem de ML Kit'in okudugu bolge
 * bu iki degerden beslenir; boylece "cercevenin icindekini okur" davranisi
 * gorsel olarak da dogru olur.
 */
private const val ROI_GENISLIK_ORANI = 0.86f
private const val ROI_YUKSEKLIK_ORANI = 0.26f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarayiciEkrani(
    onGeri: () -> Unit,
    onIsbnOkundu: (String) -> Unit,
    onElleGir: () -> Unit
) {
    val context = LocalContext.current
    val yasamDongusu = LocalLifecycleOwner.current
    val dokunsalGeriBildirim = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }

    var izinVar by remember { mutableStateOf(context.kameraIzniVarMi()) }
    var izinReddedildi by remember { mutableStateOf(false) }
    var fenerAcik by remember { mutableStateOf(false) }
    var kameraKontrol by remember { mutableStateOf<CameraControl?>(null) }
    // Ayni barkodun art arda okunmasini engelleyen kilit.
    var okumaKilidi by remember { mutableStateOf(false) }
    var uyari by remember { mutableStateOf<String?>(null) }

    val izinIsteyici = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { verildi ->
        izinVar = verildi
        izinReddedildi = !verildi
    }

    LaunchedEffect(Unit) {
        if (!izinVar) izinIsteyici.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(uyari) {
        uyari?.let {
            snackbarHostState.showSnackbar(it)
            uyari = null
            okumaKilidi = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Barkodu okut") },
                navigationIcon = {
                    IconButton(onClick = onGeri) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    if (izinVar) {
                        IconButton(
                            onClick = {
                                fenerAcik = !fenerAcik
                                kameraKontrol?.enableTorch(fenerAcik)
                            }
                        ) {
                            Icon(
                                imageVector = if (fenerAcik) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = if (fenerAcik) "Feneri kapat" else "Feneri aç"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            if (!izinVar) {
                IzinGerekli(
                    reddedildi = izinReddedildi,
                    onIzinIste = { izinIsteyici.launch(Manifest.permission.CAMERA) },
                    onAyarlar = { context.uygulamaAyarlariniAc() },
                    onElleGir = onElleGir
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Onizleme 3:4 oraninda sabitlendi. Kamera goruntusu de 4:3 istendigi
                    // icin kutu ile goruntu birebir ortusur; cerceve oranlari boylece
                    // dogrudan goruntu koordinatlarina cevrilebiliyor.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 4f)
                    ) {
                        KameraOnizleme(
                            onKameraHazir = { kameraKontrol = it },
                            onBarkod = { ham ->
                                if (!okumaKilidi) {
                                    okumaKilidi = true
                                    if (IsbnUtils.kitapBarkoduMu(ham)) {
                                        dokunsalGeriBildirim.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onIsbnOkundu(IsbnUtils.temizle(ham))
                                    } else {
                                        uyari = "Bu bir kitap barkodu değil (bandrol veya fiyat etiketi olabilir)."
                                    }
                                }
                            },
                            yasamDongusuSahibi = yasamDongusu
                        )

                        CerceveKaplamasi()
                    }

                    Text(
                        text = "Barkodu çerçevenin içine getir",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 20.dp)
                    )

                    FilledTonalButton(
                        onClick = onElleGir,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Icon(Icons.Default.Keyboard, contentDescription = null)
                        Text("Barkod okunmuyor, elle gireyim", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}

/** Cerceve disini karartir ve okunan bolgeyi tam olarak isaretler. */
@Composable
private fun CerceveKaplamasi() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val genislik = size.width
        val yukseklik = size.height
        val cerceveG = genislik * ROI_GENISLIK_ORANI
        val cerceveY = yukseklik * ROI_YUKSEKLIK_ORANI
        val sol = (genislik - cerceveG) / 2f
        val ust = (yukseklik - cerceveY) / 2f
        val perde = Color.Black.copy(alpha = 0.55f)

        drawRect(perde, Offset(0f, 0f), Size(genislik, ust))
        drawRect(perde, Offset(0f, ust + cerceveY), Size(genislik, yukseklik - ust - cerceveY))
        drawRect(perde, Offset(0f, ust), Size(sol, cerceveY))
        drawRect(perde, Offset(sol + cerceveG, ust), Size(genislik - sol - cerceveG, cerceveY))

        drawRoundRect(
            color = Color.White,
            topLeft = Offset(sol, ust),
            size = Size(cerceveG, cerceveY),
            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

@Composable
private fun KameraOnizleme(
    onKameraHazir: (CameraControl) -> Unit,
    onBarkod: (String) -> Unit,
    yasamDongusuSahibi: LifecycleOwner
) {
    val analizIsleyici = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { analizIsleyici.shutdown() }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val onizleme = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            // Onizleme ve analiz ayni en-boy oraninda olmali; aksi halde
            // cerceve ile okunan bolge kayar.
            val oranSecici = ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                .build()

            val saglayiciGelecegi = ProcessCameraProvider.getInstance(ctx)
            saglayiciGelecegi.addListener({
                val saglayici = saglayiciGelecegi.get()

                val onizlemeKullanimi = Preview.Builder()
                    .setResolutionSelector(oranSecici)
                    .build()
                    .also { it.setSurfaceProvider(onizleme.surfaceProvider) }

                val analiz = ImageAnalysis.Builder()
                    .setResolutionSelector(oranSecici)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(
                            analizIsleyici,
                            BarkodAnalizci(
                                roiGenislikOrani = ROI_GENISLIK_ORANI,
                                roiYukseklikOrani = ROI_YUKSEKLIK_ORANI,
                                onBarkod = onBarkod
                            )
                        )
                    }

                try {
                    saglayici.unbindAll()
                    val kamera = saglayici.bindToLifecycle(
                        yasamDongusuSahibi,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        onizlemeKullanimi,
                        analiz
                    )
                    onKameraHazir(kamera.cameraControl)
                } catch (e: Exception) {
                    // Kamera baska bir uygulamada aciksa veya cihaz desteklemiyorsa
                    // onizleme bos kalir; kullanici elle girise yonlenebilir.
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            onizleme
        }
    )
}

@Composable
private fun IzinGerekli(
    reddedildi: Boolean,
    onIzinIste: () -> Unit,
    onAyarlar: () -> Unit,
    onElleGir: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Barkod okumak için kamera izni gerekiyor.",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (reddedildi) {
                "İzni reddettin. Ayarlardan kamera iznini açabilir ya da kitabı elle ekleyebilirsin."
            } else {
                "İzin verdiğinde kamera açılır; görüntü cihazdan dışarı çıkmaz."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Button(
            onClick = if (reddedildi) onAyarlar else onIzinIste,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(if (reddedildi) "Ayarları aç" else "İzin ver")
        }

        TextButton(onClick = onElleGir) {
            Text("Kitabı elle ekle")
        }
    }
}

private fun Context.kameraIzniVarMi(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

private fun Context.uygulamaAyarlariniAc() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )
}
