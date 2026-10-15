package com.tarik.kutuphanem.data.remote.api

import com.tarik.kutuphanem.data.remote.dto.OpenLibraryAramaYaniti
import com.tarik.kutuphanem.data.remote.dto.OpenLibraryKitap
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenLibraryService {

    /** Cevap: {"ISBN:9750718533": { ... }} seklinde anahtarli sozluk. */
    @GET("api/books")
    suspend fun kitap(
        @Query("bibkeys") anahtar: String,
        @Query("format") bicim: String = "json",
        @Query("jscmd") komut: String = "data"
    ): Map<String, OpenLibraryKitap>

    /** Serbest metin aramasi; Google Books ulasilamadiginda yedek kaynak olarak kullanilir. */
    @GET("search.json")
    suspend fun ara(
        @Query("q") sorgu: String,
        @Query("limit") enFazla: Int = 20,
        @Query("fields") alanlar: String =
            "title,subtitle,author_name,publisher,number_of_pages_median,isbn,cover_i"
    ): OpenLibraryAramaYaniti
}
