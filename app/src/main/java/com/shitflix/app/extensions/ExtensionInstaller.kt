package com.shitflix.app.extensions

import com.shitflix.app.data.repo.ProviderRegistry

/**
 * Stub for a CloudStream-style extension installer.
 *
 * A real implementation would:
 *  1. Download a `.cs3` (DEX) extension from a repo URL.
 *  2. Verify signature / hash.
 *  3. Load the DEX via [dalvik.system.DexClassLoader].
 *  4. Instantiate the `ProviderApi` implementation and register it.
 *
 * That requires careful sandboxing and is intentionally not wired up here.
 * This stub records "installed" repos so the UI flow works end-to-end.
 */
object ExtensionInstaller {
    data class Repo(val name: String, val url: String)

    private val repos = mutableListOf<Repo>()

    fun repos(): List<Repo> = repos.toList()

    fun addRepo(name: String, url: String): Result<Repo> {
        if (url.isBlank()) return Result.failure(IllegalArgumentException("URL required"))
        val r = Repo(name.ifBlank { url }, url)
        repos += r
        // In a real impl: fetch + parse manifest, then for each entry register a provider.
        // ProviderRegistry.register(loadedProvider)
        return Result.success(r)
    }

    fun removeRepo(url: String) {
        repos.removeAll { it.url == url }
    }

    @Suppress("unused")
    private fun touch() = ProviderRegistry // keep reference for future wiring
}
