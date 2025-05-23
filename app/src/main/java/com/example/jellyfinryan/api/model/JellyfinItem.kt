package com.example.jellyfinryan.api.model

import android.util.Log

data class JellyfinItem(
    val Id: String,
    val Name: String,
    val Type: String,
    val PrimaryImageTag: String?,
    val Overview: String?,
    val PremiereDate: String?,
    val CommunityRating: Float?,
    val OfficialRating: String?,
    val RunTimeTicks: Long?,
    val ImageTags: Map<String, String>?,
    val ParentId: String?,
    val BackdropImageTags: List<String>?
) {
    /**
     * Gets a typed image URL for the item.
     * Common types: "Primary", "Banner", "Logo", "Thumb", "Art", "Disc", "Menu"
     */
    fun getImageUrl(serverUrl: String, type: String = "Primary"): String? {
        val imageTag = ImageTags?.get(type)
            ?: if (type == "Primary") PrimaryImageTag else null

        val url = imageTag?.let { tag ->
            "$serverUrl/Items/$Id/Images/$type?tag=$tag"
        }
        return url
    }

    /**
     * Gets the primary image URL using the most reliable fields.
     */
    fun getPrimaryImageUrl(serverUrl: String): String? {
        val tag = ImageTags?.get("Primary") ?: PrimaryImageTag
        val url = tag?.let { "$serverUrl/Items/$Id/Images/Primary?tag=$it" }
        return url
    }

    /**
     * Gets a backdrop image URL.
     * @param index The index of the backdrop image (usually 0 for the first one).
     */
    fun getBackdropImageUrl(serverUrl: String, index: Int = 0): String? {
        return BackdropImageTags?.getOrNull(index)?.let { tag ->
            "$serverUrl/Items/$Id/Images/Backdrop/$index?tag=$tag"
        }
    }

    /**
     * Gets the best horizontal image available for TV layout.
     * Prioritizes Banner > Thumb > Backdrop > Primary
     */
    fun getBestHorizontalImageUrl(serverUrl: String): String? {
        return getImageUrl(serverUrl, "Banner")
            ?: getImageUrl(serverUrl, "Thumb")
            ?: getBackdropImageUrl(serverUrl)
            ?: getPrimaryImageUrl(serverUrl)
    }

    /**
     * Gets the best image for featured/hero content.
     * Prioritizes Backdrop > Banner > Thumb > Primary
     */
    fun getBestFeaturedImageUrl(serverUrl: String): String? {
        return getBackdropImageUrl(serverUrl)
            ?: getImageUrl(serverUrl, "Banner")
            ?: getImageUrl(serverUrl, "Thumb")
            ?: getPrimaryImageUrl(serverUrl)
    }

    /**
     * Converts RunTimeTicks to minutes.
     * RunTimeTicks represents time in 100-nanosecond intervals
     */
    fun getRunTimeMinutes(): Int? {
        return RunTimeTicks?.let { ticks ->
            // Convert from 100-nanosecond intervals to minutes
            // 1 second = 10,000,000 ticks
            // 1 minute = 600,000,000 ticks
            (ticks / 600000000L).toInt()
        }
    }

    /**
     * Extracts the year from PremiereDate.
     */
    val year: String?
        get() = PremiereDate?.takeIf { it.length >= 4 }?.substring(0, 4)

    /**
     * Gets formatted runtime string (e.g., "1h 30m" or "45m")
     */
    fun getFormattedRuntime(): String? {
        return getRunTimeMinutes()?.let { minutes ->
            when {
                minutes >= 60 -> {
                    val hours = minutes / 60
                    val remainingMinutes = minutes % 60
                    if (remainingMinutes > 0) {
                        "${hours}h ${remainingMinutes}m"
                    } else {
                        "${hours}h"
                    }
                }
                else -> "${minutes}m"
            }
        }
    }

    /**
     * Gets a display-friendly type name
     */
    fun getDisplayType(): String {
        return when (Type) {
            "Movie" -> "Movie"
            "Series" -> "TV Series"
            "Season" -> "Season"
            "Episode" -> "Episode"
            "MusicArtist" -> "Artist"
            "MusicAlbum" -> "Album"
            "Audio" -> "Song"
            "CollectionFolder" -> "Library"
            else -> Type
        }
    }

    /**
     * Checks if this item is a video type that can be played
     */
    fun isPlayable(): Boolean {
        return Type in listOf("Movie", "Episode", "Video")
    }

    /**
     * Checks if this item is a container that can be browsed
     */
    fun isBrowsable(): Boolean {
        return Type in listOf("Series", "Season", "CollectionFolder", "Folder", "MusicArtist", "MusicAlbum")
    }
}

data class JellyfinLibrary(
    val Id: String,
    val Name: String,
    val CollectionType: String?,
    val PrimaryImageItemId: String?,
    val PrimaryImageTag: String?,
    val ImageTags: Map<String, String>?
) {
    fun getImageUrl(serverUrl: String): String? {
        val imageUrl = when {
            PrimaryImageTag != null -> {
                "$serverUrl/Items/$Id/Images/Primary?tag=$PrimaryImageTag"
            }
            ImageTags?.get("Primary") != null -> {
                "$serverUrl/Items/$Id/Images/Primary?tag=${ImageTags["Primary"]}"
            }
            else -> {
                // Fallback - try banner for libraries
                "$serverUrl/Items/$Id/Images/Banner"
            }
        }
        Log.d("JellyfinLibrary", "Image URL for library '$Name': $imageUrl")
        return imageUrl
    }

    fun getBannerUrl(serverUrl: String): String {
        return "$serverUrl/Items/$Id/Images/Banner"
    }
}


