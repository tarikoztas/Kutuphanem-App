package com.tarik.kutuphanem.domain.repository

import com.tarik.kutuphanem.core.AppResult
import com.tarik.kutuphanem.domain.model.Kitap

/** Dis kaynaklardan (Google Books, OpenLibrary) kitap bilgisi cekme. */
interface KitapAramaRepository {

    /** Bulunamazsa Success(null) doner; bu bir hata degil, elle ekleme akisinin girisidir. */
    suspend fun isbnIleAra(isbn: String): AppResult<Kitap?>

    suspend fun isimleAra(sorgu: String): AppResult<List<Kitap>>
}
