package com.shitflix.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.shitflix.app.data.model.MovieDetails
import com.shitflix.app.data.repo.ProviderRegistry

@Composable
fun DetailsScreen(nav: NavController, providerId: String, movieId: String) {
    var details by remember { mutableStateOf<MovieDetails?>(null) }
    LaunchedEffect(providerId, movieId) {
        details = ProviderRegistry.get(providerId)?.details(movieId)
    }

    val d = details
    if (d == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(360.dp)) {
            AsyncImage(
                model = d.movie.backdropUrl ?: d.movie.posterUrl,
                contentDescription = d.movie.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color(0x80000000), Color.Transparent, Color(0xFF141414))),
                ),
            )
            IconButton(
                onClick = { nav.popBackStack() },
                modifier = Modifier.padding(8.dp),
            ) { Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White) }
        }
        Column(Modifier.padding(16.dp)) {
            Text(d.movie.title, style = MaterialTheme.typography.displayLarge.copy(fontSize = androidx.compose.ui.unit.TextUnit.Unspecified), color = Color.White)
            Spacer(Modifier.height(6.dp))
            Text(
                buildString {
                    d.movie.year?.let { append(it).append("  •  ") }
                    d.runtimeMin?.let { append("${it} min  •  ") }
                    append(d.genres.joinToString(", "))
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { nav.navigate("player/$providerId/$movieId") },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.PlayArrow, null)
                Spacer(Modifier.width(6.dp))
                Text("Play")
            }
            Spacer(Modifier.height(16.dp))
            Text(d.overview, style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Spacer(Modifier.height(40.dp))
        }
    }
}
