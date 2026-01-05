package fr.mobilune.cookingassistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.mobilune.cookingassistant.data.CookingDao
import fr.mobilune.cookingassistant.data.NetworkClient
import fr.mobilune.cookingassistant.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CookingViewModel(
    private val dao: CookingDao,
    private val api: NetworkClient
) : ViewModel() {

    val ingredients = dao.getAllIngredients().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val recipes = dao.getRecipesWithIngredients().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _searchResults = MutableStateFlow<List<RecipeWithIngredients>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _onlineIngredients = MutableStateFlow<List<Ingredient>>(emptyList())
    val onlineIngredients = _onlineIngredients.asStateFlow()

    fun addIngredient(name: String, type: FoodType) {
        viewModelScope.launch {
            dao.insertIngredient(Ingredient(name = name, type = type))
        }
    }

    fun addRecipe(name: String, selectedIngredients: List<Ingredient>) {
        viewModelScope.launch {
            val recipeId = dao.insertRecipe(Recipe(name = name))
            selectedIngredients.forEach { ingredient ->
                dao.insertRecipeCrossRef(RecipeIngredientCrossRef(recipeId, ingredient.id))
            }
        }
    }

    fun searchRecipes(query: String) {
        viewModelScope.launch {
            dao.searchRecipesByName(query).collect { results ->
                _searchResults.value = results
            }
        }
    }

    fun searchRecipesByIngredients(selectedIngredients: List<Ingredient>) {
        viewModelScope.launch {
            dao.getRecipesWithIngredients().collect { allRecipes ->
                val filtered = allRecipes.filter { recipeWithIng ->
                    val recipeIngredientIds = recipeWithIng.ingredients.map { it.id }
                    selectedIngredients.all { it.id in recipeIngredientIds }
                }
                _searchResults.value = filtered
            }
        }
    }

    fun fetchOnlineIngredients() {
        viewModelScope.launch {
            val result = api.fetchIngredients()
            _onlineIngredients.value = result
        }
    }
}