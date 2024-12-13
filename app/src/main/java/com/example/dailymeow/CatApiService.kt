package com.example.dailymeow

import retrofit2.http.GET

data class CatImage(
    val id: String,
    val url: String
)

interface CatApiService {
    @GET("images/search")
    suspend fun getRandomCat(): List<CatImage>
}
