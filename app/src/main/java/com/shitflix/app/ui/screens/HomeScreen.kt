package com.shitflix.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shitflix.app.data.model.HomeRow
import com.shitflix.app.data.repo.ProviderRegistry
import com.shitflix.app.ui.components.Hero
import com.shitflix.app.ui.components.MovieRow
import com.shitflix.app.ui.components.ShitFlixTopBar

@Composable
fun HomeScreen(nav: NavController) {
    var rows by remember { mutableStateOf<List<HomeRow>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        rows = ProviderRegistry.aggregateHome()
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        ShitFlixTopBar(
            onSearch = { nav.navigate("search") },
            onExtensions = { nav.navigate("extensions") },
        )
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                rows.firstOrNull()?.items?.firstOrNull()?.let { featured ->
                    Hero(
                        movie = featured,
                        onPlay = { nav.navigate("player/${featured.providerId}/${featured.id}") },
                        onInfo = { nav.navigate("details/${featured.providerId}/${featured.id}") },
                    )
                }
                rows.forEach { row ->
                    MovieRow(row) { m ->
                        nav.navigate("details/${m.providerId}/${m.id}")
                    }
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}
