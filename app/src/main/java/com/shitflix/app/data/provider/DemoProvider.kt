package com.shitflix.app.data.provider

import com.shitflix.app.data.model.*

/**
 * Sample provider serving public-domain / open-license content so the app
 * is usable out of the box. Replace / add real providers by implementing
 * [ProviderApi] and registering with [com.shitflix.app.data.repo.ProviderRegistry].
 */
class DemoProvider : ProviderApi {
    override val id = "demo"
    override val name = "Demo (Open Content)"

    private val bigBuck = Movie(
        providerId = id, id = "big-buck-bunny",
        title = "Big Buck Bunny",
        posterUrl = "https://peach.blender.org/wp-content/uploads/bbb-splash.png",
        backdropUrl = "https://peach.blender.org/wp-content/uploads/bbb-splash.png",
        year = 2008, rating = 7.8,
    )
    private val sintel = Movie(
        providerId = id, id = "sintel",
        title = "Sintel",
        posterUrl = "https://durian.blender.org/wp-content/uploads/2010/05/sintel_poster.jpg",
        backdropUrl = "https://durian.blender.org/wp-content/uploads/2010/05/sintel_poster.jpg",
        year = 2010, rating = 8.0,
    )
    private val tears = Movie(
        providerId = id, id = "tears-of-steel",
        title = "Tears of Steel",
        posterUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/04_TOS_Celia_Drone.jpg",
        backdropUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/04_TOS_Celia_Drone.jpg",
        year = 2012, rating = 7.2,
    )
    private val elephants = Movie(
        providerId = id, id = "elephants-dream",
        title = "Elephants Dream",
        posterUrl = "https://orange.blender.org/wp-content/themes/orange/images/poster.jpg",
        backdropUrl = "https://orange.blender.org/wp-content/themes/orange/images/poster.jpg",
        year = 2006, rating = 7.0,
    )

    private val catalog = listOf(bigBuck, sintel, tears, elephants)

    override suspend fun home(): List<HomeRow> = listOf(
        HomeRow("Trending Now", catalog),
        HomeRow("Open Cinema", listOf(sintel, tears, elephants, bigBuck)),
        HomeRow("Animated Picks", listOf(bigBuck, elephants, sintel)),
    )

    override suspend fun search(query: String): List<Movie> =
        catalog.filter { it.title.contains(query, ignoreCase = true) }

    override suspend fun details(id: String): MovieDetails {
        val m = catalog.first { it.id == id }
        return MovieDetails(
            movie = m,
            overview = when (id) {
                "big-buck-bunny" -> "A large rabbit deals with three bullies in this open-license animated short."
                "sintel" -> "A girl searches for a dragon she once befriended — a Blender Open Movie."
                "tears-of-steel" -> "A sci-fi short combining live action with CG, made entirely with Blender."
                "elephants-dream" -> "The world's first open-source animated movie."
                else -> ""
            },
            genres = listOf("Animation", "Short"),
            runtimeMin = 12,
        )
    }

    override suspend fun load(id: String, episodeId: String?): List<StreamLink> {
        val base = when (id) {
            "big-buck-bunny" -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            "sintel" -> "https://download.blender.org/durian/trailer/sintel_trailer-480p.mp4"
            "tears-of-steel" -> "https://download.blender.org/demo/movies/ToS/tears_of_steel_720p.mov"
            "elephants-dream" -> "https://archive.org/download/ElephantsDream/ed_1024_512kb.mp4"
            else -> return emptyList()
        }
        return listOf(
            StreamLink(
                name = "Auto",
                url = base,
                qualityLabel = "720p",
                subtitles = listOf(
                    Subtitle(lang = "English", url = "https://gist.githubusercontent.com/samdutton/ca37f3adaf4e23679957b8083e061177/raw/e19399fbccbc069a2af4266e5120ae6bad62699a/sample.vtt"),
                ),
            ),
        )
    }
}
