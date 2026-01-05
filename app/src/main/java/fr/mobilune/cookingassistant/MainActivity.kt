package fr.mobilune.cookingassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.mobilune.cookingassistant.model.FoodType
import fr.mobilune.cookingassistant.model.Ingredient
import fr.mobilune.cookingassistant.ui.theme.CookingAssistantTheme
import fr.mobilune.cookingassistant.viewmodel.CookingViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CookingAssistantTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val viewModel: CookingViewModel = koinViewModel()
    var currentScreen by remember { mutableStateOf("ingredients") }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == "ingredients",
                    onClick = { currentScreen = "ingredients" },
                    label = { Text("Aliments") },
                    icon = { Text("\uD83C\uDF4E") }
                )
                NavigationBarItem(
                    selected = currentScreen == "recipes",
                    onClick = { currentScreen = "recipes" },
                    label = { Text("Recettes") },
                    icon = { Text("\uD83D\uDCD6") }
                )
                NavigationBarItem(
                    selected = currentScreen == "search",
                    onClick = { currentScreen = "search" },
                    label = { Text("Recherche") },
                    icon = { Text("\uD83D\uDD0D") }
                )
                NavigationBarItem(
                    selected = currentScreen == "online",
                    onClick = { currentScreen = "online" },
                    label = { Text("En ligne") },
                    icon = { Text("\uD83C\uDF10") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentScreen) {
                "ingredients" -> IngredientsScreen(viewModel)
                "recipes" -> RecipesScreen(viewModel)
                "search" -> SearchScreen(viewModel)
                "online" -> OnlineScreen(viewModel)
            }
        }
    }
}

@Composable
fun FoodTypeImage(type: FoodType) {
    val imageRes = when (type) {
        FoodType.fruit -> R.drawable.fruit
        FoodType.vegetable -> R.drawable.vegetable
        FoodType.meat -> R.drawable.meat
        FoodType.starchy -> R.drawable.starchy
        FoodType.other -> R.drawable.other
    }
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = type.name,
        modifier = Modifier.size(40.dp)
    )
}

@Composable
fun IngredientsScreen(viewModel: CookingViewModel) {
    val ingredients by viewModel.ingredients.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Column {
        Button(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Ajouter un aliment")
        }
        LazyColumn {
            items(ingredients) { ingredient ->
                ListItem(
                    headlineContent = { Text(ingredient.name) },
                    leadingContent = { FoodTypeImage(ingredient.type) }
                )
            }
        }
    }

    if (showDialog) {
        AddIngredientDialog(
            onDismiss = { showDialog = false },
            onAdd = { name, type -> viewModel.addIngredient(name, type) }
        )
    }
}

@Composable
fun AddIngredientDialog(onDismiss: () -> Unit, onAdd: (String, FoodType) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(FoodType.vegetable) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    onAdd(name, selectedType)
                    onDismiss()
                }
            }) { Text("Ajouter") }
        },
        title = { Text("Nouvel aliment") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom") })
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    FoodType.values().forEach { type ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { selectedType = type }) {
                            FoodTypeImage(type)
                            RadioButton(selected = selectedType == type, onClick = { selectedType = type })
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun RecipesScreen(viewModel: CookingViewModel) {
    val recipes by viewModel.recipes.collectAsState()
    val allIngredients by viewModel.ingredients.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column {
        Button(onClick = { showAddDialog = true }, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Créer une recette")
        }
        LazyColumn {
            items(recipes) { recipeWithIng ->
                Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(recipeWithIng.recipe.name, style = MaterialTheme.typography.titleLarge)
                        Text("Ingrédients : " + recipeWithIng.ingredients.joinToString { it.name })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddRecipeDialog(allIngredients, onDismiss = { showAddDialog = false }) { name, selected ->
            viewModel.addRecipe(name, selected)
        }
    }
}

@Composable
fun AddRecipeDialog(
    availableIngredients: List<Ingredient>,
    onDismiss: () -> Unit,
    onConfirm: (String, List<Ingredient>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val selectedIngredients = remember { mutableStateListOf<Ingredient>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && selectedIngredients.isNotEmpty()) {
                    onConfirm(name, selectedIngredients)
                    onDismiss()
                }
            }) { Text("Sauvegarder") }
        },
        title = { Text("Nouvelle Recette") },
        text = {
            Column(modifier = Modifier.height(300.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom de la recette") })
                Text("Sélectionnez les ingrédients :", modifier = Modifier.padding(top = 8.dp))
                LazyColumn {
                    items(availableIngredients) { ingredient ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = selectedIngredients.contains(ingredient),
                                onCheckedChange = { isChecked ->
                                    if (isChecked) selectedIngredients.add(ingredient) else selectedIngredients.remove(ingredient)
                                }
                            )
                            Text(ingredient.name)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun SearchScreen(viewModel: CookingViewModel) {
    var query by remember { mutableStateOf("") }
    val results by viewModel.searchResults.collectAsState()
    var searchMode by remember { mutableStateOf(true) }
    val allIngredients by viewModel.ingredients.collectAsState()
    val selectedIngsForSearch = remember { mutableStateListOf<Ingredient>() }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = { searchMode = true }) { Text("Par Nom") }
            TextButton(onClick = { searchMode = false }) { Text("Par Ingrédients") }
        }

        if (searchMode) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.searchRecipes(it)
                },
                label = { Text("Rechercher une recette...") },
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
        } else {
            Button(onClick = { viewModel.searchRecipesByIngredients(selectedIngsForSearch) }, modifier = Modifier.padding(8.dp)) {
                Text("Lancer la recherche")
            }
            Text("Ingrédients requis :", modifier = Modifier.padding(8.dp))
            LazyColumn(modifier = Modifier.height(150.dp)) {
                items(allIngredients) { ingredient ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = selectedIngsForSearch.contains(ingredient),
                            onCheckedChange = { isChecked ->
                                if (isChecked) selectedIngsForSearch.add(ingredient) else selectedIngsForSearch.remove(ingredient)
                            }
                        )
                        Text(ingredient.name)
                    }
                }
            }
        }

        Divider()
        Text("Résultats : ${results.size}", modifier = Modifier.padding(8.dp))
        LazyColumn {
            items(results) { recipe ->
                ListItem(
                    headlineContent = { Text(recipe.recipe.name) },
                    supportingContent = { Text(recipe.ingredients.joinToString { it.name }) }
                )
            }
        }
    }
}

@Composable
fun OnlineScreen(viewModel: CookingViewModel) {
    val onlineList by viewModel.onlineIngredients.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchOnlineIngredients()
    }

    Column {
        Text("Ingrédients de la communauté", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))

        LazyColumn {
            items(onlineList) { ingredient ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FoodTypeImage(ingredient.type)
                        Spacer(Modifier.width(8.dp))
                        Text(ingredient.name)
                    }
                    Button(onClick = { viewModel.addIngredient(ingredient.name, ingredient.type) }) {
                        Text("Ajouter")
                    }
                }
            }
        }
    }
}