package com.shitflix.app.data.provider

import com.shitflix.app.data.model.*

/**
 * CloudStream-inspired provider contract. A provider is anything that can
 * return a home feed, run a search, and resolve playable stream links for a
 * given title. Real CloudStream extensions are loadable DEX modules; this
 * interface is the in-app equivalent so built-in and (future) hot-loaded
 * providers share the same surface.
 */
interface ProviderApi {
    val id: String
    val name: String
    val language: String get() = "en"

    suspend fun home(): List<HomeRow>
    suspend fun search(query: String): List<Movie>
    suspend fun details(id: String): MovieDetails
    /** Resolve playable stream links (multiple qualities/mirrors). */
    suspend fun load(id: String, episodeId: String? = null): List<StreamLink>
}
