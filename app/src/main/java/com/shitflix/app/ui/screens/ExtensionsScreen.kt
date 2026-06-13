package com.shitflix.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shitflix.app.data.repo.ProviderRegistry
import com.shitflix.app.extensions.ExtensionInstaller

@Composable
fun ExtensionsScreen(nav: NavController) {
    var providers by remember { mutableStateOf(ProviderRegistry.list()) }
    var repos by remember { mutableStateOf(ExtensionInstaller.repos()) }
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    // tick triggers recomposition when toggle state changes
    var tick by remember { mutableStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Extensions & Providers") },
            navigationIcon = {
                IconButton(onClick = { nav.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black, titleContentColor = Color.White, navigationIconContentColor = Color.White,
            ),
        )

        Text("Providers", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp), color = Color.White)
        LazyColumn(Modifier.weight(1f, fill = false)) {
            items(providers, key = { it.id }) { p ->
                key(tick, p.id) {
                    ListItem(
                        headlineContent = { Text(p.name, color = Color.White) },
                        supportingContent = { Text("id: ${p.id}", color = Color.Gray) },
                        trailingContent = {
                            Switch(
                                checked = ProviderRegistry.isEnabled(p.id),
                                onCheckedChange = {
                                    ProviderRegistry.setEnabled(p.id, it)
                                    tick++
                                },
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Black),
                    )
                    Divider(color = Color(0xFF222222))
                }
            }
        }

        Divider(color = Color(0xFF222222))
        Text("Add extension repo", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp), color = Color.White)
        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("Name (optional)") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = url, onValueChange = { url = it; error = null },
            label = { Text("Repo URL (e.g. https://.../repo.json)") },
            isError = error != null,
            supportingText = { error?.let { Text(it, color = Color.Red) } },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                val r = ExtensionInstaller.addRepo(name, url)
                r.fold(
                    onSuccess = {
                        name = ""; url = ""; error = null
                        repos = ExtensionInstaller.repos()
                    },
                    onFailure = { error = it.message ?: "Invalid URL" },
                )
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) { Text("Add repo") }

        Spacer(Modifier.height(12.dp))
        LazyColumn(Modifier.fillMaxWidth()) {
            items(repos, key = { it.url }) { r ->
                ListItem(
                    headlineContent = { Text(r.name, color = Color.White) },
                    supportingContent = { Text(r.url, color = Color.Gray) },
                    trailingContent = {
                        IconButton(onClick = {
                            ExtensionInstaller.removeRepo(r.url)
                            repos = ExtensionInstaller.repos()
                        }) { Icon(Icons.Filled.Delete, "Remove", tint = Color.White) }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Black),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
