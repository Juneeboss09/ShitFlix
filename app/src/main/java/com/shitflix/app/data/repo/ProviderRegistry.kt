package com.shitflix.app.data.repo

import com.shitflix.app.data.model.HomeRow
import com.shitflix.app.data.model.Movie
import com.shitflix.app.data.provider.DemoProvider
import com.shitflix.app.data.provider.ProviderApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * In-memory provider registry. The Extensions screen can enable/disable
 * providers; aggregated calls fan out across enabled ones.
 */
object ProviderRegistry {
    private val all: MutableList<ProviderApi> = mutableListOf(DemoProvider())
    private val enabled: MutableSet<String> = all.map { it.id }.toMutableSet()

    fun list(): List<ProviderApi> = all.toList()
    fun get(id: String): ProviderApi? = all.firstOrNull { it.id == id }

    fun isEnabled(id: String): Boolean = id in enabled
    fun setEnabled(id: String, on: Boolean) {
        if (on) enabled.add(id) else enabled.remove(id)
    }

    fun register(p: ProviderApi) {
        if (all.none { it.id == p.id }) {
            all += p
            enabled += p.id
        }
    }

    private fun enabledProviders(): List<ProviderApi> = all.filter { it.id in enabled }

    suspend fun aggregateHome(): List<HomeRow> = coroutineScope {
        enabledProviders().map { p ->
            async(Dispatchers.IO) {
                runCatching { p.home() }.getOrDefault(emptyList())
            }
        }.flatMap { it.await() }
    }

    suspend fun aggregateSearch(query: String): List<Movie> = coroutineScope {
        enabledProviders().map { p ->
            async(Dispatchers.IO) {
                runCatching { p.search(query) }.getOrDefault(emptyList())
            }
        }.flatMap { it.await() }
    }
}
