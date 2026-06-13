package com.shitflix.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shitflix.app.ui.screens.*
import com.shitflix.app.ui.theme.ShitFlixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShitFlixTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "home") {
        composable("home") { HomeScreen(nav) }
        composable("search") { SearchScreen(nav) }
        composable("extensions") { ExtensionsScreen(nav) }
        composable(
            "details/{providerId}/{movieId}",
            arguments = listOf(
                navArgument("providerId") { type = NavType.StringType },
                navArgument("movieId") { type = NavType.StringType },
            )
        ) {
            val p = it.arguments?.getString("providerId").orEmpty()
            val m = it.arguments?.getString("movieId").orEmpty()
            DetailsScreen(nav, p, m)
        }
        composable(
            "player/{providerId}/{movieId}",
            arguments = listOf(
                navArgument("providerId") { type = NavType.StringType },
                navArgument("movieId") { type = NavType.StringType },
            )
        ) {
            val p = it.arguments?.getString("providerId").orEmpty()
            val m = it.arguments?.getString("movieId").orEmpty()
            PlayerScreen(nav, p, m)
        }
    }
}
