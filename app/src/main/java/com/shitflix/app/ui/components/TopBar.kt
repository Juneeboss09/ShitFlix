package com.shitflix.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ShitFlixTopBar(onSearch: () -> Unit, onExtensions: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xCC000000))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "SHITFLIX",
            color = Color(0xFFE50914),
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onSearch) {
            Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
        }
        IconButton(onClick = onExtensions) {
            Icon(Icons.Filled.Extension, contentDescription = "Extensions", tint = Color.White)
        }
    }
}
