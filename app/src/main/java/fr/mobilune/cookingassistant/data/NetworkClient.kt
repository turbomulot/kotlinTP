package fr.mobilune.cookingassistant.data

import fr.mobilune.cookingassistant.model.Ingredient
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class NetworkClient(private val httpClient: HttpClient) {
    suspend fun fetchIngredients(): List<Ingredient> {
        return try {
            httpClient.get("https://dev-montpellier.fr/aliments.json").body()
        } catch (e: Exception) {
            emptyList()
        }
    }
}