package com.tarik.kutuphanem.data.remote.api

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
}
