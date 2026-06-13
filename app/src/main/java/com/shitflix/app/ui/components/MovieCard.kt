package com.shitflix.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.shitflix.app.data.model.HomeRow
import com.shitflix.app.data.model.Movie

@Composable
fun MovieCard(movie: Movie, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = movie.posterUrl,
            contentDescription = movie.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            movie.title,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
        )
    }
}

@Composable
fun MovieRow(row: HomeRow, onClick: (Movie) -> Unit) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(
            row.title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(row.items) { m -> MovieCard(m) { onClick(m) } }
        }
    }
}
