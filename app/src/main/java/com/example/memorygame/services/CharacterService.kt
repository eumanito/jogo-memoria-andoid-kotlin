package com.example.memorygame.services

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CharacterService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://rickandmortyapi.com/api/") // Replace with your API base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(CharacterApiService::class.java)

    private  fun getSingleCharacter(characterId: Int): Character {
        return service.getCharacter(characterId)
    }

    /*A palavra-chave suspend está relacionada a funções usadas em corrotinas, que são uma forma de escrever código assíncrono e sem bloqueio.
    *
    * Chamar a função getRandomCharacter para pegar os dados do personagem
    * */
    fun getRandomCharacter(): Character {
        val randomId = (1..42).random()
        return getSingleCharacter(randomId)
    }
}