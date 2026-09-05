package com.tarik.kutuphanem.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val AcikSema = lightColorScheme(
    primary = Kahve40,
    onPrimary = Color.White,
    primaryContainer = Kahve80,
    onPrimaryContainer = Color(0xFF2E1508),
    secondary = Amber40,
    secondaryContainer = Amber80,
    tertiary = Yesil40,
    tertiaryContainer = Yesil80,
    background = ArkaAcik,
    surface = YuzeyAcik,
    surfaceVariant = Color(0xFFF1E8E2)
)

private val KoyuSema = darkColorScheme(
    primary = Kahve80,
    onPrimary = Color(0xFF43220E),
    primaryContainer = Color(0xFF5E3A22),
    onPrimaryContainer = Kahve80,
    secondary = Amber80,
    secondaryContainer = Color(0xFF5C4318),
    tertiary = Yesil80,
    tertiaryContainer = Color(0xFF2C4F27),
    background = ArkaKoyu,
    surface = YuzeyKoyu,
    surfaceVariant = Color(0xFF3A322D)
)

/** Okundu/okunmadi rozetleri M3 semasinin disinda kaldigi icin ayri tasiniyor. */
data class DurumRenkleri(
    val okunduIcerik: Color,
    val okunduKap: Color,
    val okunmadiIcerik: Color,
    val okunmadiKap: Color
)

val LocalDurumRenkleri = staticCompositionLocalOf {
    DurumRenkleri(OkunduAcik, OkunduAcikKap, OkunmadiAcik, OkunmadiAcikKap)
}

@Composable
fun KutuphanemTheme(
    koyuTema: Boolean = isSystemInDarkTheme(),
    dinamikRenk: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dinamikRenk && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (koyuTema) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        koyuTema -> KoyuSema
        else -> AcikSema
    }

    val durumRenkleri = if (koyuTema) {
        DurumRenkleri(OkunduKoyu, OkunduKoyuKap, OkunmadiKoyu, OkunmadiKoyuKap)
    } else {
        DurumRenkleri(OkunduAcik, OkunduAcikKap, OkunmadiAcik, OkunmadiAcikKap)
    }

    CompositionLocalProvider(LocalDurumRenkleri provides durumRenkleri) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = KutuphanemTypography,
            content = content
        )
    }
}
