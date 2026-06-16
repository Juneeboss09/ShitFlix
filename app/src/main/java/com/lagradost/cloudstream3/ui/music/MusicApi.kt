package com.lagradost.cloudstream3.ui.music

import com.lagradost.api.Log
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.mvvm.logError
import kotlinx.serialization.json.*

private const val TAG = "MusicApi"
private const val INNERTUBE_API_KEY = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"
private const val BASE_URL = "https://music.youtube.com/youtubei/v1"
private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

data class MusicSong(
    val videoId: String,
    val title: String,
    val artist: String,
    val album: String?,
    val thumbnail: String,
    val duration: Long,
    val browseId: String? = null
)

data class MusicSection(
    val title: String,
    val songs: List<MusicSong>
)

object MusicApi {
    private fun buildBody(extra: JsonObject.() -> Unit): JsonObject = buildJsonObject {
        putJsonObject("context") {
            putJsonObject("client") {
                put("clientName", "WEB_REMIX")
                put("clientVersion", "1.20250204.01.00")
                put("gl", "US")
                put("hl", "en")
            }
        }
        extra()
    }

    private suspend fun innerTubeRequest(endpoint: String, body: JsonObject): Result<JsonElement> {
        return try {
            val url = "$BASE_URL/$endpoint?key=$INNERTUBE_API_KEY"
            val bodyStr = body.toString()
            val mediaType = okhttp3.MediaType.parse("application/json; charset=utf-8")!!
            val requestBody = okhttp3.RequestBody.create(mediaType, bodyStr)
            val request = okhttp3.Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Content-Type", "application/json")
                .addHeader("Origin", "https://music.youtube.com")
                .build()
            val response = app.baseClient.newCall(request).execute()
            val text = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return Result.failure(Exception("HTTP ${response.code}: $text"))
            }
            val json = Json.parseToJsonElement(text)
            Result.success(json)
        } catch (e: Exception) {
            logError(e)
            Result.failure(e)
        }
    }

    private fun extractSongsFromSearch(json: JsonElement): List<MusicSong> {
        val songs = mutableListOf<MusicSong>()
        try {
            val contents = json.jsonObject["contents"]
                ?.jsonObject?.get("tabbedSearchResultsRenderer")
                ?.jsonObject?.get("tabs")
                ?.jsonArray?.get(0)?.jsonObject?.get("tabRenderer")
                ?.jsonObject?.get("content")?.jsonObject?.get("sectionListRenderer")
                ?.jsonObject?.get("contents")?.jsonArray

            contents?.forEach { section ->
                val items = section.jsonObject?.get("musicShelfRenderer")
                    ?.jsonObject?.get("contents")?.jsonArray ?: return@forEach

                items.forEach { item ->
                    val renderer = item.jsonObject?.get("musicResponsiveListItemRenderer")?.jsonObject ?: return@forEach
                    val song = parseMusicListItem(renderer)
                    if (song != null) songs.add(song)
                }
            }
        } catch (e: Exception) {
            logError(e)
        }
        return songs
    }

    private fun extractSongsFromBrowse(json: JsonElement): List<MusicSection> {
        val sections = mutableListOf<MusicSection>()
        try {
            val contents = json.jsonObject["contents"]
                ?.jsonObject?.get("singleColumnBrowseResultsRenderer")
                ?.jsonObject?.get("tabs")
                ?.jsonArray?.get(0)?.jsonObject?.get("tabRenderer")
                ?.jsonObject?.get("content")?.jsonObject?.get("sectionListRenderer")
                ?.jsonObject?.get("contents")?.jsonArray

            contents?.forEach { section ->
                val shelf = section.jsonObject?.get("musicShelfRenderer") ?: return@forEach
                val title = shelf.jsonObject["title"]?.jsonObject?.get("runs")
                    ?.jsonArray?.joinToString("") { run ->
                        run.jsonObject["text"]?.jsonPrimitive?.content ?: ""
                    } ?: return@forEach

                val items = shelf.jsonObject["contents"]?.jsonArray ?: return@forEach
                val songs = items.mapNotNull { item ->
                    val renderer = item.jsonObject?.get("musicResponsiveListItemRenderer")?.jsonObject ?: return@mapNotNull null
                    parseMusicListItem(renderer)
                }
                if (songs.isNotEmpty()) {
                    sections.add(MusicSection(title = title, songs = songs))
                }
            }
        } catch (e: Exception) {
            logError(e)
        }
        return sections
    }

    private fun parseMusicListItem(renderer: JsonObject): MusicSong? {
        return try {
            val videoId = renderer["overlay"]?.jsonObject
                ?.get("musicItemThumbnailOverlayRenderer")?.jsonObject
                ?.get("content")?.jsonObject?.get("musicPlayButtonRenderer")?.jsonObject
                ?.get("playNavigationEndpoint")?.jsonObject?.get("watchEndpoint")?.jsonObject
                ?.get("videoId")?.jsonPrimitive?.content ?: return null

            val flexColumns = renderer["flexColumns"]?.jsonArray ?: return null

            val title = flexColumns.getOrNull(0)?.jsonObject
                ?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
                ?.get("text")?.jsonObject?.get("runs")?.jsonArray
                ?.joinToString("") { it.jsonObject["text"]?.jsonPrimitive?.content ?: "" } ?: "Unknown"

            val artist = flexColumns.getOrNull(1)?.jsonObject
                ?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
                ?.get("text")?.jsonObject?.get("runs")?.jsonArray
                ?.joinToString("") { it.jsonObject["text"]?.jsonPrimitive?.content ?: "" } ?: ""

            val thumbnails = renderer["thumbnail"]?.jsonObject
                ?.get("musicThumbnailRenderer")?.jsonObject
                ?.get("thumbnail")?.jsonObject?.get("thumbnails")?.jsonArray

            val thumbnail = thumbnails?.lastOrNull()?.jsonObject
                ?.get("url")?.jsonPrimitive?.content ?: ""

            val durationText = flexColumns.getOrNull(1)?.jsonObject
                ?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
                ?.get("text")?.jsonObject?.get("runs")?.jsonArray
                ?.lastOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.content ?: ""

            val duration = parseDuration(durationText)

            MusicSong(
                videoId = videoId,
                title = title,
                artist = artist,
                album = null,
                thumbnail = thumbnail,
                duration = duration
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseDuration(text: String): Long {
        val parts = text.trim().split(":")
        return when (parts.size) {
            2 -> parts[0].toLongOrNull()?.times(60)?.plus(parts[1].toLongOrNull() ?: 0) ?: 0
            3 -> parts[0].toLongOrNull()?.times(3600)?.plus(parts[1].toLongOrNull()?.times(60) ?: 0)?.plus(parts[2].toLongOrNull() ?: 0) ?: 0
            else -> 0
        }
    }

    suspend fun search(query: String): Result<List<MusicSong>> {
        val body = buildBody {
            put("query", query)
        }
        return try {
            val json = innerTubeRequest("search", body).getOrThrow()
            Result.success(extractSongsFromSearch(json))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHomeSections(): Result<List<MusicSection>> {
        val body = buildBody {
            put("browseId", "FEmusic_home")
        }
        return try {
            val json = innerTubeRequest("browse", body).getOrThrow()
            Result.success(extractSongsFromBrowse(json))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStreamUrl(videoId: String): Result<String?> {
        val body = buildBody {
            put("videoId", videoId)
        }
        return try {
            val json = innerTubeRequest("player", body).getOrThrow()
            val url = try {
                val formats = json.jsonObject["streamingData"]?.jsonObject
                    ?.get("adaptiveFormats")?.jsonArray

                val audioFormat = formats?.filter { format ->
                    val mime = format.jsonObject["mimeType"]?.jsonPrimitive?.content ?: ""
                    mime.startsWith("audio/")
                }?.maxByOrNull { format ->
                    format.jsonObject["bitrate"]?.jsonPrimitive?.longOrNull ?: 0
                }

                audioFormat?.jsonObject?.get("url")?.jsonPrimitive?.content
                    ?: audioFormat?.jsonObject?.get("signatureCipher")?.jsonPrimitive?.content
            } catch (e: Exception) {
                logError(e)
                null
            }
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
