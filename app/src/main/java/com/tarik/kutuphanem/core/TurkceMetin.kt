package com.tarik.kutuphanem.core

import java.text.Collator
import java.util.Locale

private val TR = Locale("tr", "TR")

/** Turkce alfabeye gore siralama (C, G, I, O, S, U dogru yere duser). */
val turkceKarsilastirici: Comparator<String> by lazy {
    val collator = Collator.getInstance(TR).apply { strength = Collator.PRIMARY }
    Comparator { a, b -> collator.compare(a, b) }
}

/**
 * Aramada kullanilan normalizasyon.
 *
 * Iki sorunu birden cozer:
 *  1. Turkce'de "I" harfinin kucugu "i" degil "ı"dir; varsayilan lowercase() yanlis eslesir.
 *  2. Kullanici Turkce karakter yazmadan arar: "sisli" -> "Şişli", "cocuk" -> "Çocuk".
 */
fun String.aramaIcinNormalize(): String {
    val kucuk = lowercase(TR)
    val sb = StringBuilder(kucuk.length)
    for (ch in kucuk) {
        sb.append(
            when (ch) {
                'ç' -> 'c'
                'ğ' -> 'g'
                'ı' -> 'i'
                'ö' -> 'o'
                'ş' -> 's'
                'ü' -> 'u'
                'â' -> 'a'
                'î' -> 'i'
                'û' -> 'u'
                else -> ch
            }
        )
    }
    return sb.toString()
}

/** Aranan metnin, hedef metinlerden herhangi birinde gecip gecmedigi. */
fun aramaEslesiyorMu(aranan: String, vararg alanlar: String?): Boolean {
    val anahtar = aranan.trim().aramaIcinNormalize()
    if (anahtar.isEmpty()) return true
    return alanlar.any { it != null && it.aramaIcinNormalize().contains(anahtar) }
}
