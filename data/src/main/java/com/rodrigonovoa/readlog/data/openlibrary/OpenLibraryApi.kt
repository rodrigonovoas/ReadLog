package com.rodrigonovoa.readlog.data.openlibrary

import retrofit2.http.GET
import retrofit2.http.Path

interface OpenLibraryApi {

    @GET("isbn/{isbn}.json")
    suspend fun getBookByIsbn(@Path("isbn") isbn: String): OpenLibraryIsbnResponse

    @GET("{authorKey}.json")
    suspend fun getAuthor(@Path("authorKey", encoded = true) authorKey: String): OpenLibraryAuthorResponse
}
