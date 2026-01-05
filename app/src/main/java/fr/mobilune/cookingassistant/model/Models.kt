package fr.mobilune.cookingassistant.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.serialization.Serializable

enum class FoodType {
    fruit, vegetable, meat, starchy, other
}

@Entity
@Serializable
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: FoodType
)

@Entity
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(primaryKeys = ["recipeId", "ingredientId"])
data class RecipeIngredientCrossRef(
    val recipeId: Long,
    val ingredientId: Long
)

data class RecipeWithIngredients(
    @Embedded val recipe: Recipe,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = RecipeIngredientCrossRef::class,
            parentColumn = "recipeId",
            entityColumn = "ingredientId"
        )
    )
    val ingredients: List<Ingredient>
)