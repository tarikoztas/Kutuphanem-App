package com.tarik.kutuphanem.core.isbn

/**
 * ISBN yardimcilari.
 *
 * Neden gerekli: Turkce baskilarin bir kismi kataloglarda ISBN-10 ile kayitli,
 * kitabin arkasindaki barkod ise ISBN-13 basar. Iki varyantla da sorgulayinca
 * "bulunamadi" sonuclari belirgin sekilde azaliyor.
 */
object IsbnUtils {

    /** Tire, bosluk ve benzeri ayraclari atar; X harfini buyutur. */
    fun temizle(ham: String): String =
        ham.filter { it.isDigit() || it == 'x' || it == 'X' }.uppercase()

    fun gecerliMi(isbn: String): Boolean {
        val temiz = temizle(isbn)
        return when (temiz.length) {
            10 -> gecerliMi10(temiz)
            13 -> gecerliMi13(temiz)
            else -> false
        }
    }

    fun gecerliMi13(isbn: String): Boolean {
        if (isbn.length != 13 || isbn.any { !it.isDigit() }) return false
        val toplam = isbn.take(12).mapIndexed { i, c ->
            (c - '0') * if (i % 2 == 0) 1 else 3
        }.sum()
        val kontrol = (10 - toplam % 10) % 10
        return kontrol == isbn[12] - '0'
    }

    fun gecerliMi10(isbn: String): Boolean {
        if (isbn.length != 10) return false
        var toplam = 0
        for (i in 0 until 9) {
            val c = isbn[i]
            if (!c.isDigit()) return false
            toplam += (c - '0') * (10 - i)
        }
        val son = isbn[9]
        val sonDeger = when {
            son == 'X' -> 10
            son.isDigit() -> son - '0'
            else -> return false
        }
        toplam += sonDeger
        return toplam % 11 == 0
    }

    /** ISBN-10 -> ISBN-13 (978 on eki + yeni kontrol hanesi). */
    fun on13eCevir(isbn10: String): String? {
        val temiz = temizle(isbn10)
        if (!gecerliMi10(temiz)) return null
        val govde = "978" + temiz.take(9)
        val toplam = govde.mapIndexed { i, c -> (c - '0') * if (i % 2 == 0) 1 else 3 }.sum()
        val kontrol = (10 - toplam % 10) % 10
        return govde + kontrol
    }

    /** ISBN-13 -> ISBN-10 (yalnizca 978 ile baslayanlar cevrilebilir). */
    fun on10aCevir(isbn13: String): String? {
        val temiz = temizle(isbn13)
        if (!gecerliMi13(temiz) || !temiz.startsWith("978")) return null
        val govde = temiz.substring(3, 12)
        var toplam = 0
        govde.forEachIndexed { i, c -> toplam += (c - '0') * (10 - i) }
        val kalan = (11 - toplam % 11) % 11
        val kontrol = if (kalan == 10) "X" else kalan.toString()
        return govde + kontrol
    }

    /**
     * Sorgulanacak butun varyantlar (once verilen hali, sonra donusturulmus hali).
     * Gecersiz bir kod verilirse bile temizlenmis hali denenir; bazi eski kayitlarda
     * kontrol hanesi hatali basilmis olabiliyor.
     */
    fun varyantlar(ham: String): List<String> {
        val temiz = temizle(ham)
        if (temiz.isEmpty()) return emptyList()

        val liste = linkedSetOf(temiz)
        when (temiz.length) {
            10 -> on13eCevir(temiz)?.let { liste.add(it) }
            13 -> on10aCevir(temiz)?.let { liste.add(it) }
        }
        return liste.toList()
    }

    /** Bandrol/fiyat barkodlarini elemek icin: kitap barkodlari 978 veya 979 ile baslar. */
    fun kitapBarkoduMu(kod: String): Boolean {
        val temiz = temizle(kod)
        return when (temiz.length) {
            13 -> temiz.startsWith("978") || temiz.startsWith("979")
            10 -> gecerliMi10(temiz)
            else -> false
        }
    }
}
