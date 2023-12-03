package com.example.memorygame.services

import retrofit2.http.GET
import retrofit2.http.Path
data class Character(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val origin: Origin,
    val location: Location,
    val image: String,
    val episode: List<String>,
    val url: String,
    val created: String
)
data class Origin(
    val name: String,
    val url: String
)

data class Location(
    val name: String,
    val url: String
)
interface CharacterApiService {
    @GET("characters/{id}")
    fun getCharacter(@Path("id") characterId: Int): Character
}