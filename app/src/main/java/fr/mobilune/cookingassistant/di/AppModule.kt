package fr.mobilune.cookingassistant.di

import androidx.room.Room
import fr.mobilune.cookingassistant.data.AppDatabase
import fr.mobilune.cookingassistant.data.NetworkClient
import fr.mobilune.cookingassistant.viewmodel.CookingViewModel
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "cooking_db")
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<AppDatabase>().cookingDao() }

    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    single { NetworkClient(get()) }

    viewModel { CookingViewModel(get(), get()) }
}