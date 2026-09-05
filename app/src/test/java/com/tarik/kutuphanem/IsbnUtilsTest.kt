package com.tarik.kutuphanem

import com.tarik.kutuphanem.core.isbn.IsbnUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IsbnUtilsTest {

    @Test
    fun `tireli isbn temizlenir`() {
        assertEquals("9789750718533", IsbnUtils.temizle("978-975-07-1853-3"))
        assertEquals("080442957X", IsbnUtils.temizle("0-8044-2957-x"))
    }

    @Test
    fun `gecerli isbn13 dogrulanir`() {
        assertTrue(IsbnUtils.gecerliMi13("9789750718533"))
        assertFalse(IsbnUtils.gecerliMi13("9789750718534"))
    }

    @Test
    fun `gecerli isbn10 dogrulanir`() {
        assertTrue(IsbnUtils.gecerliMi10("080442957X"))
        assertFalse(IsbnUtils.gecerliMi10("0804429570"))
    }

    @Test
    fun `isbn10 isbn13e cevrilir`() {
        assertEquals("9780804429573", IsbnUtils.on13eCevir("080442957X"))
    }

    @Test
    fun `isbn13 isbn10a cevrilir`() {
        assertEquals("080442957X", IsbnUtils.on10aCevir("9780804429573"))
    }

    @Test
    fun `979 ile baslayan isbn13 isbn10a cevrilemez`() {
        // 979 on ekinin ISBN-10 karsiligi yoktur.
        assertNull(IsbnUtils.on10aCevir("9791234567896"))
    }

    @Test
    fun `varyantlar her iki bicimi de icerir`() {
        val varyantlar = IsbnUtils.varyantlar("080442957X")
        assertTrue(varyantlar.contains("080442957X"))
        assertTrue(varyantlar.contains("9780804429573"))
    }

    @Test
    fun `kitap barkodu 978 veya 979 ile baslar`() {
        assertTrue(IsbnUtils.kitapBarkoduMu("9789750718533"))
        // Bandrol veya urun barkodu: kitap degil.
        assertFalse(IsbnUtils.kitapBarkoduMu("8690504081234"))
    }
}
