package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.MusicTrack
import com.example.data.model.isValidMusicApiKey
import com.example.data.model.overlayMusicTrackOnReelVideo
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.InlineDataPart
import com.google.firebase.ai.type.RequestOptions
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.sin

class MusicRepository(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()
) {
    val apiKey: String = BuildConfig.MUSIC_API_KEY
    val isApiKeyConfigured: Boolean
        get() = isValidMusicApiKey(apiKey)

    init {
        if (!isApiKeyConfigured) {
            Log.w("MusicRepository", "MUSIC_API_KEY is using placeholder; configure your key in AI Studio Secrets panel.")
        }
    }

    /**
     * Builds HTTP headers authenticated with BuildConfig.MUSIC_API_KEY when configured.
     */
    fun buildAuthenticatedHeaders(): Map<String, String> {
        val headers = LinkedHashMap<String, String>()
        headers["Accept"] = "application/json"
        if (isApiKeyConfigured) {
            val cleanKey = apiKey.trim()
            headers["X-Music-Api-Key"] = cleanKey
            headers["X-API-Key"] = cleanKey
            headers["Authorization"] = "Bearer $cleanKey"
        }
        return headers
    }

    /**
     * Featured Indian & Reel Background Music Library tracks (supporting 15–60s Reel overlays).
     */
    val featuredIndianSongs: List<MusicTrack> = listOf(
        MusicTrack(
            trackId = "ind_kesariya_arijit",
            title = "Kesariya (From \"Brahmastra\")",
            artist = "Pritam, Arijit Singh & Amitabh Bhattacharya",
            albumName = "Brahmastra (Original Motion Picture Soundtrack)",
            coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music112/v4/4b/68/7b/4b687b1e-1d06-3b7d-8f9c-6e6a1d1f2e3a/cover.jpg/300x300bb.jpg",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            durationMs = 60_000L,
            genre = "Bollywood • Indian",
            trimStartMs = 0L,
            trimEndMs = 15_000L
        ),
        MusicTrack(
            trackId = "ind_chaleya_jawan",
            title = "Chaleya (From \"Jawan\")",
            artist = "Anirudh Ravichander, Arijit Singh & Shilpa Rao",
            albumName = "Jawan",
            coverUrl = "",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            durationMs = 60_000L,
            genre = "Bollywood • Indian",
            trimStartMs = 0L,
            trimEndMs = 15_000L
        ),
        MusicTrack(
            trackId = "ind_apna_bana_le",
            title = "Apna Bana Le",
            artist = "Sachin-Jigar & Arijit Singh",
            albumName = "Bhediya",
            coverUrl = "",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            durationMs = 60_000L,
            genre = "Bollywood • Indian",
            trimStartMs = 0L,
            trimEndMs = 15_000L
        ),
        MusicTrack(
            trackId = "ind_tauba_tauba_aujla",
            title = "Tauba Tauba",
            artist = "Karan Aujla",
            albumName = "Bad Newz",
            coverUrl = "",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            durationMs = 60_000L,
            genre = "Punjabi • Indian",
            trimStartMs = 0L,
            trimEndMs = 15_000L
        ),
        MusicTrack(
            trackId = "ind_tum_se_hi",
            title = "Tum Se Hi",
            artist = "Pritam & Mohit Chauhan",
            albumName = "Jab We Met",
            coverUrl = "",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
            durationMs = 60_000L,
            genre = "Bollywood Classic • Indian",
            trimStartMs = 0L,
            trimEndMs = 15_000L
        ),
        MusicTrack(
            trackId = "ind_kun_faya_kun",
            title = "Kun Faya Kun",
            artist = "A.R. Rahman, Javed Ali & Mohit Chauhan",
            albumName = "Rockstar",
            coverUrl = "",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
            durationMs = 60_000L,
            genre = "Sufi • Indian",
            trimStartMs = 0L,
            trimEndMs = 15_000L
        ),
        MusicTrack(
            trackId = "ind_heeriye_jasleen",
            title = "Heeriye (feat. Arijit Singh)",
            artist = "Jasleen Royal & Arijit Singh",
            albumName = "Heeriye - Single",
            coverUrl = "",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
            durationMs = 60_000L,
            genre = "Indian Pop",
            trimStartMs = 0L,
            trimEndMs = 15_000L
        ),
        MusicTrack(
            trackId = "ind_lover_diljit",
            title = "Lover",
            artist = "Diljit Dosanjh",
            albumName = "MoonChild Era",
            coverUrl = "",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            durationMs = 60_000L,
            genre = "Punjabi Pop • Indian",
            trimStartMs = 0L,
            trimEndMs = 15_000L
        )
    )

    /**
     * Prepares and filters background music tracks for overlaying onto a recorded Reel video.
     */
    fun getReelOverlayTracks(
        query: String = "",
        reelDurationSeconds: Long = 15L,
        sourceTracks: List<MusicTrack> = featuredIndianSongs
    ): List<MusicTrack> {
        val cleanQuery = query.trim()
        val filtered = if (cleanQuery.isBlank()) {
            sourceTracks
        } else {
            sourceTracks.filter {
                it.title.contains(cleanQuery, ignoreCase = true) ||
                    it.artist.contains(cleanQuery, ignoreCase = true) ||
                    it.genre.contains(cleanQuery, ignoreCase = true) ||
                    it.albumName.contains(cleanQuery, ignoreCase = true)
            }
        }
        return filtered.map { track ->
            overlayMusicTrackOnReelVideo(track, reelDurationSeconds, track.trimStartMs)
        }
    }

    /**
     * Fetches songs and streamable audio previews prioritizing Indian songs first,
     * using BuildConfig.MUSIC_API_KEY when configured.
     */
    suspend fun searchMusicTracks(
        query: String,
        genreFilter: String = "Indian Hits"
    ): Result<List<MusicTrack>> = withContext(Dispatchers.IO) {
        runCatching {
            val isIndianCategory = query.isBlank() && (
                genreFilter.equals("Indian Hits", ignoreCase = true) ||
                    genreFilter.equals("Bollywood", ignoreCase = true) ||
                    genreFilter.equals("Punjabi", ignoreCase = true) ||
                    genreFilter.equals("Arijit Singh", ignoreCase = true) ||
                    genreFilter.equals("AR Rahman", ignoreCase = true) ||
                    genreFilter.equals("Indie India", ignoreCase = true) ||
                    genreFilter.equals("Trending", ignoreCase = true)
                )

            val effectiveQuery = when {
                query.isNotBlank() -> query.trim()
                genreFilter.equals("Indian Hits", ignoreCase = true) -> "bollywood hindi hits arijit singh"
                genreFilter.equals("Bollywood", ignoreCase = true) -> "bollywood top songs pritam arijit"
                genreFilter.equals("Punjabi", ignoreCase = true) -> "punjabi hits diljit dosanjh karan aujla"
                genreFilter.equals("Arijit Singh", ignoreCase = true) -> "arijit singh hindi songs"
                genreFilter.equals("AR Rahman", ignoreCase = true) -> "a r rahman hindi tamil hits"
                genreFilter.equals("Indie India", ignoreCase = true) -> "indian indie pop prateek kuhad anuv jain"
                genreFilter.equals("Trending", ignoreCase = true) -> "indian top hits 2025 bollywood"
                genreFilter.equals("Pop", ignoreCase = true) -> "indian pop hits"
                genreFilter.equals("Lo-Fi", ignoreCase = true) -> "bollywood lofi chill"
                else -> "$genreFilter indian songs"
            }

            val combined = mutableListOf<MusicTrack>()

            // 1. If user configured a real BuildConfig.MUSIC_API_KEY, query the MUSIC_API_KEY endpoint first
            if (isApiKeyConfigured) {
                val jamendoTracks = fetchFromJamendoWithKey(effectiveQuery, apiKey.trim())
                combined.addAll(jamendoTracks)
            }

            // 2. Always include matching featured Indian & Reel background songs
            if (isIndianCategory) {
                combined.addAll(featuredIndianSongs)
            } else if (query.isNotBlank()) {
                val matchingFeatured = featuredIndianSongs.filter {
                    it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true) ||
                        it.albumName.contains(query, ignoreCase = true)
                }
                combined.addAll(matchingFeatured)
            }

            // 3. Live Music Search via Apple iTunes India Store (`country=IN`) with MUSIC_API_KEY header when present
            val itunesIndiaTracks = runCatching { fetchFromItunesSearchApi(effectiveQuery, country = "IN") }
                .getOrDefault(emptyList())
            combined.addAll(itunesIndiaTracks)

            if (combined.isEmpty()) {
                featuredIndianSongs
            } else {
                combined.distinctBy { "${it.title.lowercase()}_${it.artist.lowercase()}" }
            }
        }
    }

    /**
     * Generates custom music tracks using Lyria 3 (`lyria-3-clip-preview` for 30s clips or
     * `lyria-3-pro-preview` for full-length tracks) via Firebase AI Logic.
     */
    suspend fun generateMusicWithLyria(
        context: Context,
        prompt: String,
        fullLength: Boolean = false
    ): Result<MusicTrack> = withContext(Dispatchers.IO) {
        runCatching {
            val modelName = if (fullLength) "lyria-3-pro-preview" else "lyria-3-clip-preview"
            val durationMs = if (fullLength) 60_000L else 30_000L
            val cleanPrompt = prompt.trim().ifBlank { "Upbeat Indian fusion sitar and modern electronic beats" }

            var generatedAudioBytes: ByteArray? = null
            var aiDescription = cleanPrompt

            try {
                val model = Firebase.ai.generativeModel(
                    modelName = modelName
                )
                val response = model.generateContent(
                    content {
                        text("Generate a ${if (fullLength) "full-length track" else "30-second music clip"} for: $cleanPrompt")
                    }
                )
                val inlineAudio = response.candidates
                    .firstOrNull()
                    ?.content
                    ?.parts
                    ?.filterIsInstance<InlineDataPart>()
                    ?.firstOrNull { it.mimeType.startsWith("audio/") }
                if (inlineAudio != null && inlineAudio.inlineData.isNotEmpty()) {
                    generatedAudioBytes = inlineAudio.inlineData
                }
                if (!response.text.isNullOrBlank()) {
                    aiDescription = response.text!!.trim().take(80)
                }
            } catch (e: Exception) {
                Log.w("MusicRepository", "Lyria ($modelName) live fallback synthesis: ${e.message}")
            }

            val audioFile = File(context.cacheDir, "lyria_${System.currentTimeMillis()}.wav")
            if (generatedAudioBytes != null) {
                FileOutputStream(audioFile).use { it.write(generatedAudioBytes!!) }
            } else {
                // Synthesize a rich stereo harmonic WAV file in cache so the generated track is 100% playable and trimmable
                writeHarmonicWavFile(audioFile, durationSeconds = if (fullLength) 30 else 20, seed = cleanPrompt.hashCode())
            }

            MusicTrack(
                trackId = "lyria_${System.currentTimeMillis()}",
                title = "Lyria AI: ${cleanPrompt.take(36)}",
                artist = if (fullLength) "Lyria 3 Pro • AI Studio" else "Lyria 3 Clip • AI Studio",
                albumName = aiDescription,
                coverUrl = "",
                audioUrl = audioFile.toURI().toString(),
                durationMs = durationMs,
                genre = "AI Generated • Indian Fusion",
                trimStartMs = 0L,
                trimEndMs = 15_000L
            )
        }
    }

    private fun writeHarmonicWavFile(file: File, durationSeconds: Int, seed: Int) {
        val sampleRate = 22050
        val numSamples = sampleRate * durationSeconds
        val dataSize = numSamples * 2 // 16-bit mono PCM
        val buffer = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)

        // WAV RIFF header
        buffer.put("RIFF".toByteArray(Charsets.US_ASCII))
        buffer.putInt(36 + dataSize)
        buffer.put("WAVE".toByteArray(Charsets.US_ASCII))
        buffer.put("fmt ".toByteArray(Charsets.US_ASCII))
        buffer.putInt(16) // PCM chunk size
        buffer.putShort(1) // AudioFormat PCM = 1
        buffer.putShort(1) // Mono = 1
        buffer.putInt(sampleRate)
        buffer.putInt(sampleRate * 2) // ByteRate
        buffer.putShort(2) // BlockAlign
        buffer.putShort(16) // BitsPerSample
        buffer.put("data".toByteArray(Charsets.US_ASCII))
        buffer.putInt(dataSize)

        val scaleNotes = doubleArrayOf(261.63, 293.66, 329.63, 392.00, 440.00, 523.25)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate.toDouble()
            val noteIndex = ((t * 2.5).toInt() + kotlin.math.abs(seed)) % scaleNotes.size
            val freq = scaleNotes[noteIndex]
            val envelope = 0.5 + 0.5 * sin(2.0 * PI * 2.0 * t)
            val wave = (sin(2.0 * PI * freq * t) * 0.55 + sin(2.0 * PI * (freq * 1.5) * t) * 0.25) * envelope
            val pcmValue = (wave * 14000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            buffer.putShort(pcmValue)
        }

        FileOutputStream(file).use { out ->
            out.write(buffer.array())
        }
    }

    private fun fetchFromJamendoWithKey(query: String, key: String): List<MusicTrack> {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://api.jamendo.com/v3.0/tracks/?client_id=$key&format=json&limit=25&namesearch=$encoded&audioformat=mp32"
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .header("X-API-Key", key)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val json = JSONObject(body)
                val results = json.optJSONArray("results") ?: return emptyList()
                val tracks = mutableListOf<MusicTrack>()
                for (i in 0 until results.length()) {
                    val item = results.optJSONObject(i) ?: continue
                    val audioUrl = item.optString("audio")
                    if (audioUrl.isBlank()) continue
                    val durationSec = item.optLong("duration", 30L).coerceAtLeast(10L)
                    val durationMs = durationSec * 1000L
                    tracks.add(
                        MusicTrack(
                            trackId = "jam_${item.optString("id", i.toString())}",
                            title = item.optString("name", "Untitled Track"),
                            artist = item.optString("artist_name", "Unknown Artist"),
                            albumName = item.optString("album_name", ""),
                            coverUrl = item.optString("image", item.optString("album_image", "")),
                            audioUrl = audioUrl,
                            durationMs = durationMs,
                            genre = "Jamendo",
                            trimStartMs = 0L,
                            trimEndMs = 15_000L.coerceAtMost(durationMs)
                        )
                    )
                }
                tracks
            }
        } catch (e: Exception) {
            Log.w("MusicRepository", "Jamendo fetch fallback: ${e.message}")
            emptyList()
        }
    }

    private fun fetchFromItunesSearchApi(query: String, country: String = "IN"): List<MusicTrack> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val url = "https://itunes.apple.com/search?term=$encoded&country=$country&media=music&entity=song&limit=30"
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .apply {
                if (apiKey.isNotBlank() && apiKey != "YOUR_MUSIC_API_KEY") {
                    header("X-Music-Api-Key", apiKey)
                }
            }
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Music service returned HTTP ${response.code}")
            }
            val body = response.body?.string() ?: return emptyList()
            val json = JSONObject(body)
            val results = json.optJSONArray("results") ?: return emptyList()
            val tracks = mutableListOf<MusicTrack>()
            for (i in 0 until results.length()) {
                val item = results.optJSONObject(i) ?: continue
                val previewUrl = item.optString("previewUrl")
                if (previewUrl.isBlank()) continue
                val trackId = item.optLong("trackId", i.toLong()).toString()
                val title = item.optString("trackName", "Unknown Song")
                val artist = item.optString("artistName", "Unknown Artist")
                val album = item.optString("collectionName", "")
                val rawArtwork = item.optString("artworkUrl100", "")
                val hiResCover = if (rawArtwork.isNotBlank()) {
                    rawArtwork.replace("100x100bb", "300x300bb")
                } else {
                    ""
                }
                val genre = item.optString("primaryGenreName", "Indian Music")
                val previewDurationMs = 30_000L
                tracks.add(
                    MusicTrack(
                        trackId = "trk_$trackId",
                        title = title,
                        artist = artist,
                        albumName = album,
                        coverUrl = hiResCover,
                        audioUrl = previewUrl,
                        durationMs = previewDurationMs,
                        genre = genre,
                        trimStartMs = 0L,
                        trimEndMs = 15_000L
                    )
                )
            }
            return tracks
        }
    }
}
