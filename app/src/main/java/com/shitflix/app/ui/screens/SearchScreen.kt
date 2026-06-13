package com.shitflix.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shitflix.app.data.model.Movie
import com.shitflix.app.data.repo.ProviderRegistry
import com.shitflix.app.ui.components.MovieCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(nav: NavController) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Movie>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(query) {
        if (query.isBlank()) {
            results = emptyList()
            return@LaunchedEffect
        }
        delay(250)
        scope.launch {
            results = ProviderRegistry.aggregateSearch(query)
        }
    }

    Column(Modifier.fillMaxSize().padding(top = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search movies, shows...") },
                singleLine = true,
                modifier = Modifier.weight(1f).padding(end = 12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE50914),
                    unfocusedBorderColor = Color.DarkGray,
                ),
            )
        }
        Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(results, key = { it.providerId + "/" + it.id }) { m ->
                MovieCard(m) { nav.navigate("details/${m.providerId}/${m.id}") }
            }
        }
    }
}
