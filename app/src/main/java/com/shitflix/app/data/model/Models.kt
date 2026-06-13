package com.shitflix.app.data.model

data class Movie(
    val providerId: String,
    val id: String,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String? = null,
    val year: Int? = null,
    val rating: Double? = null,
)

data class HomeRow(
    val title: String,
    val items: List<Movie>,
)

data class MovieDetails(
    val movie: Movie,
    val overview: String,
    val genres: List<String> = emptyList(),
    val runtimeMin: Int? = null,
    val episodes: List<Episode> = emptyList(), // empty => single movie
)

data class Episode(
    val id: String,
    val season: Int,
    val number: Int,
    val title: String,
)

data class Subtitle(
    val lang: String,
    val url: String,
    val mime: String = "text/vtt", // or "application/x-subrip"
)

data class StreamLink(
    val name: String,         // e.g. "1080p", "720p auto"
    val url: String,
    val isHls: Boolean = url.endsWith(".m3u8"),
    val isDash: Boolean = url.endsWith(".mpd"),
    val headers: Map<String, String> = emptyMap(),
    val subtitles: List<Subtitle> = emptyList(),
    val qualityLabel: String? = null,
)
