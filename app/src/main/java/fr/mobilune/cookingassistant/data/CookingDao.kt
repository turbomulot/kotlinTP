package fr.mobilune.cookingassistant.data

import androidx.room.*
import fr.mobilune.cookingassistant.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CookingDao {
    @Query("SELECT * FROM Ingredient")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient)

    @Transaction
    @Query("SELECT * FROM Recipe")
    fun getRecipesWithIngredients(): Flow<List<RecipeWithIngredients>>

    @Insert
    suspend fun insertRecipe(recipe: Recipe): Long

    @Insert
    suspend fun insertRecipeCrossRef(crossRef: RecipeIngredientCrossRef)

    @Transaction
    @Query("SELECT * FROM Recipe WHERE name LIKE '%' || :query || '%'")
    fun searchRecipesByName(query: String): Flow<List<RecipeWithIngredients>>
}