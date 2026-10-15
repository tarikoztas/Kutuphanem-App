package com.tarik.kutuphanem.data.remote.api

import com.tarik.kutuphanem.data.remote.dto.GoogleBooksYanit
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksService {

    /**
     * @param sorgu "isbn:9789750718533" ya da serbest metin ("intitle:... inauthor:...").
     * @param ulke  TR verilince Turkiye katalogundan sonuc doner; bazi baskilar
     *              yalnizca bu parametreyle gorunur hale geliyor.
     */
    @GET("books/v1/volumes")
    suspend fun ara(
        @Query("q") sorgu: String,
        @Query("country") ulke: String? = "TR",
        @Query("maxResults") enFazla: Int = 20
    ): GoogleBooksYanit
}
