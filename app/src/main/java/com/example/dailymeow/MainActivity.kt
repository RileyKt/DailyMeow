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
    var catImageUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val likesRepository = remember { LikesRepository(context) }
    val favoritesRepository = remember { FavoritesRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    val likes = catImageUrl?.let { likesRepository.getLikes(it).collectAsState(initial = 0).value } ?: 0
    val isLiked = catImageUrl?.let { likesRepository.isLiked(it).collectAsState(initial = false).value } ?: false
    val isFavorite = catImageUrl?.let {
        favoritesRepository.favorites.collectAsState(initial = emptySet()).value.contains(it)
    } ?: false

    // Fetch a random cat image
    val fetchRandomCatImage: () -> Unit = {
        coroutineScope.launch {
            try {
                val response = RetrofitInstance.api.getRandomCat()
                if (response.isNotEmpty()) {
                    catImageUrl = response[0].url
                    likesRepository.ensureLikesInitialized(response[0].url)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchRandomCatImage()
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
                        contentDescription = "Cat Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Likes: $likes")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = {
                            coroutineScope.launch {
                                likesRepository.toggleLike(catImageUrl!!)
                            }
                        }) {
                            Text(if (isLiked) "Unlike ($likes)" else "Like ($likes)")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(onClick = {
                            coroutineScope.launch {
                                if (isFavorite) {
                                    favoritesRepository.removeFavorite(catImageUrl!!)
                                } else {
                                    favoritesRepository.addFavorite(catImageUrl!!)
                                }
                            }
                        }) {
                            Text(if (isFavorite) "Unfavorite" else "Favorite")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { fetchRandomCatImage() }) {
                        Text("Next")
                    }
                } else {
                    CircularProgressIndicator()
                }
            }
        }
    )
}

@Composable
fun DetailScreen(navController: NavController, imageUrl: String) {
    val context = LocalContext.current
    val likesRepository = remember { LikesRepository(context) }
    val favoritesRepository = remember { FavoritesRepository(context) }
    val isFavorite = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Initialize likes immediately
    LaunchedEffect(imageUrl) {
        coroutineScope.launch {
            likesRepository.ensureLikesInitialized(imageUrl)
        }
    }

    val likes = likesRepository.getLikes(imageUrl).collectAsState(initial = 0).value
    val isLiked = likesRepository.isLiked(imageUrl).collectAsState(initial = false).value

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
                Text("Likes: $likes")
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = {
                        coroutineScope.launch {
                            favoritesRepository.toggleFavorite(imageUrl)
                            isFavorite.value = !isFavorite.value
                        }
                    }) {
                        Text(if (isFavorite.value) "Unfavorite" else "Favorite")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        coroutineScope.launch {
                            likesRepository.toggleLike(imageUrl)
                        }
                    }) {
                        Text(if (isLiked) "Unlike ($likes)" else "Like ($likes)")
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
    val likesRepository = remember { LikesRepository(context) }
    val favoritesRepository = remember { FavoritesRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    // Get likes and favorite state
    val likes = catImageUrl?.let { likesRepository.getLikes(it).collectAsState(initial = 0).value } ?: 0
    val isLiked = catImageUrl?.let { likesRepository.isLiked(it).collectAsState(initial = false).value } ?: false
    val isFavorite = catImageUrl?.let {
        favoritesRepository.favorites.collectAsState(initial = emptySet()).value.contains(it)
    } ?: false

    // Ensure likes are initialized when the image URL changes
    LaunchedEffect(catImageUrl) {
        coroutineScope.launch {
            catImageUrl?.let { likesRepository.ensureLikesInitialized(it) }
        }
    }

    // Fetch a random cat image from the API
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
                    Text("Likes: $likes")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = {
                            coroutineScope.launch {
                                likesRepository.toggleLike(catImageUrl!!)
                            }
                        }) {
                            Text(if (isLiked) "Unlike ($likes)" else "Like ($likes)")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(onClick = {
                            coroutineScope.launch {
                                if (isFavorite) {
                                    favoritesRepository.removeFavorite(catImageUrl!!)
                                } else {
                                    favoritesRepository.addFavorite(catImageUrl!!)
                                }
                            }
                        }) {
                            Text(if (isFavorite) "Unfavorite" else "Favorite")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(onClick = {
                            navController.navigate("browse")
                        }) {
                            Text("Browse Cats")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(onClick = {
                            navController.navigate("favorites")
                        }) {
                            Text("Go to Favorites")
                        }
                    }
                } else {
                    CircularProgressIndicator()
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