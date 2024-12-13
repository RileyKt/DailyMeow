@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.dailymeow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dailymeow.ui.theme.DailyMeowTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.first
import android.net.Uri



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DailyMeowApp()
        }
    }
}

@Composable
fun DailyMeowApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("browse") { BrowseScreen(navController) }
        composable("favorites") { FavoritesScreen(navController) }
        composable("detail/{imageUrl}") { backStackEntry ->
            val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
            DetailScreen(navController, imageUrl)
        }
    }


}

@Composable
fun BrowseScreen(navController: NavController) {
    var catImages by remember { mutableStateOf<List<String>?>(null) }
    val context = LocalContext.current
    val favoritesRepository = remember { FavoritesRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.api.getRandomCat() // Call the Cat API multiple times for browsing
            catImages = response.map { it.url }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browse Cats") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            if (catImages == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(catImages!!) { imageUrl ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            elevation = CardDefaults.cardElevation()
                        ) {
                            Column {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Cat Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                        .padding(8.dp)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Button(onClick = {
                                        coroutineScope.launch {
                                            favoritesRepository.addFavorite(imageUrl)
                                        }
                                    }) {
                                        Text("Favorite")
                                    }
                                    Button(onClick = {
                                        navController.navigate("detail/${Uri.encode(imageUrl)}")
                                    }) {
                                        Text("View Details")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}


@Composable
fun DetailScreen(navController: NavController, imageUrl: String) {
    val context = LocalContext.current
    val favoritesRepository = remember { FavoritesRepository(context) }
    val likesRepository = remember { LikesRepository(context) }
    val isFavorite = remember { mutableStateOf(false) }
    val likesFlow = likesRepository.getLikes(imageUrl).collectAsState(initial = 0)
    val likes = likesFlow.value
    val coroutineScope = rememberCoroutineScope()

    if (imageUrl.isEmpty()) {
        Text("Invalid image URL")
        return
    }

    LaunchedEffect(Unit) {
        val favorites = favoritesRepository.favorites.first()
        isFavorite.value = imageUrl in favorites
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cat Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Cat Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = {
                        coroutineScope.launch {
                            if (isFavorite.value) {
                                favoritesRepository.removeFavorite(imageUrl)
                            } else {
                                favoritesRepository.addFavorite(imageUrl)
                            }
                            isFavorite.value = !isFavorite.value
                        }
                    }) {
                        Text(if (isFavorite.value) "Unfavorite" else "Favorite")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        coroutineScope.launch {
                            likesRepository.incrementLikes(imageUrl)
                        }
                    }) {
                        Text("Like ($likes)")
                    }
                }
            }
        }
    )
}


@Composable
fun HomeScreen(navController: NavController) {
    var catImageUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val favoritesRepository = remember { FavoritesRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    Button(onClick = { navController.navigate("browse") }) {
        Text("Browse Cats")
    }

    // Fetch random cat image
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.api.getRandomCat()
            if (response.isNotEmpty()) {
                catImageUrl = response[0].url
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("DailyMeow") })
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (catImageUrl != null) {
                    AsyncImage(
                        model = catImageUrl,
                        contentDescription = "Random Cat",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        catImageUrl?.let { url ->
                            coroutineScope.launch {
                                favoritesRepository.addFavorite(url)
                            }
                        }
                    }) {
                        Text("Favorite")
                    }
                } else {
                    CircularProgressIndicator()
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("favorites") }) {
                    Text("Go to Favorites")
                }
            }
        }
    )
}
@Composable
fun FavoritesScreen(navController: NavController) {
    val context = LocalContext.current
    val favoritesRepository = remember { FavoritesRepository(context) }
    val favoritesFlow = favoritesRepository.favorites.collectAsState(initial = emptySet())
    val favorites = favoritesFlow.value.toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            if (favorites.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No favorite cats yet! Add some from the Home screen.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(favorites) { imageUrl ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    // Navigate to DetailScreen with encoded URL
                                    navController.navigate("detail/${Uri.encode(imageUrl)}")
                                },
                            elevation = CardDefaults.cardElevation()
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Favorite Cat",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DailyMeowTheme {
    }
}