package com.tarik.kutuphanem.core

/**
 * Repository katmanindan UI'a donen tek tip sonuc sarmalayicisi.
 * Hata mesajlari kullaniciya dogrudan gosterilebilecek sekilde Turkce uretilir.
 */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val message: String, val cause: Throwable? = null) : AppResult<Nothing>
    data object Loading : AppResult<Nothing>
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Error -> this
    AppResult.Loading -> AppResult.Loading
}

fun <T> AppResult<T>.dataOrNull(): T? = (this as? AppResult.Success)?.data
