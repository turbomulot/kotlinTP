package fr.mobilune.cookingassistant.data

import androidx.room.Database
import androidx.room.RoomDatabase
import fr.mobilune.cookingassistant.model.*

@Database(entities = [Ingredient::class, Recipe::class, RecipeIngredientCrossRef::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cookingDao(): CookingDao
}