package com.example.jellyfinryan.api.model

import com.google.gson.annotations.SerializedName

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
    val BackdropImageTags: List<String>?,
    // Additional image fields for better image support
    val SeriesPrimaryImageTag: String?,
    val ParentPrimaryImageTag: String?,
    val ParentBackdropImageTags: List<String>?,
    val ThumbImageTags: List<String>?,
    val ScreenshotImageTags: List<String>?,
    @SerializedName("ProductionYear") val ProductionYear: Int?,
    val ParentThumbImageTag: String?,
    val SeriesThumbImageTag: String?
) {
    /**
     * Get the primary image URL for vertical cards (posters)
     */
    fun getImageUrl(serverUrl: String): String? {
        // Try PrimaryImageTag first
        PrimaryImageTag?.let { tag ->
            return "$serverUrl/Items/$Id/Images/Primary?tag=$tag&quality=90"
        }

        // Try ImageTags map for Primary
        ImageTags?.get("Primary")?.let { tag ->
            return "$serverUrl/Items/$Id/Images/Primary?tag=$tag&quality=90"
        }

        // For episodes/seasons, try parent primary image
        if (Type in listOf("Episode", "Season") && ParentId != null) {
            ParentPrimaryImageTag?.let { tag ->
                return "$serverUrl/Items/$ParentId/Images/Primary?tag=$tag&quality=90"
            }

            SeriesPrimaryImageTag?.let { tag ->
                return "$serverUrl/Items/$ParentId/Images/Primary?tag=$tag&quality=90"
            }
        }

        // Fallback to primary image without tag
        return "$serverUrl/Items/$Id/Images/Primary?quality=90"
    }

    /**
     * Get horizontal image URL for landscape cards (backdrops)
     * This is specifically for the "Recently Added" horizontal cards
     */
    fun getHorizontalImageUrl(serverUrl: String): String? {
        // For movies and series, prefer backdrop images
        if (Type in listOf("Movie", "Series")) {
            BackdropImageTags?.firstOrNull()?.let { backdropTag ->
                return "$serverUrl/Items/$Id/Images/Backdrop/0?tag=$backdropTag&quality=90&maxWidth=500&maxHeight=280"
            }

            ImageTags?.get("Backdrop")?.let { backdropTag ->
                return "$serverUrl/Items/$Id/Images/Backdrop?tag=$backdropTag&quality=90&maxWidth=500&maxHeight=280"
            }
        }

        // For episodes, try episode thumb first, then series backdrop
        if (Type == "Episode") {
            // Try episode thumbnail/screenshot
            ThumbImageTags?.firstOrNull()?.let { thumbTag ->
                return "$serverUrl/Items/$Id/Images/Thumb/0?tag=$thumbTag&quality=90&maxWidth=500&maxHeight=280"
            }

            ScreenshotImageTags?.firstOrNull()?.let { screenshotTag ->
                return "$serverUrl/Items/$Id/Images/Screenshot/0?tag=$screenshotTag&quality=90&maxWidth=500&maxHeight=280"
            }

            ImageTags?.get("Thumb")?.let { thumbTag ->
                return "$serverUrl/Items/$Id/Images/Thumb?tag=$thumbTag&quality=90&maxWidth=500&maxHeight=280"
            }

            // Fallback to parent series backdrop
            if (ParentId != null) {
                ParentBackdropImageTags?.firstOrNull()?.let { backdropTag ->
                    return "$serverUrl/Items/$ParentId/Images/Backdrop/0?tag=$backdropTag&quality=90&maxWidth=500&maxHeight=280"
                }
            }
        }

        // For seasons, try season primary with horizontal aspect
        if (Type == "Season") {
            PrimaryImageTag?.let { tag ->
                return "$serverUrl/Items/$Id/Images/Primary?tag=$tag&quality=90&maxWidth=500&maxHeight=280"
            }

            // Fallback to parent series backdrop
            if (ParentId != null) {
                ParentBackdropImageTags?.firstOrNull()?.let { backdropTag ->
                    return "$serverUrl/Items/$ParentId/Images/Backdrop/0?tag=$backdropTag&quality=90&maxWidth=500&maxHeight=280"
                }
            }
        }

        // Generic fallback - try primary image with horizontal sizing
        return getImageUrl(serverUrl)?.let { url ->
            "$url&maxWidth=500&maxHeight=280"
        }
    }

    /**
     * Get the best available image URL for any context
     */
    fun getBestImageUrl(serverUrl: String, preferHorizontal: Boolean = false): String? {
        return if (preferHorizontal) {
            getHorizontalImageUrl(serverUrl) ?: getImageUrl(serverUrl)
        } else {
            getImageUrl(serverUrl)
        }
    }

    /**
     * Get backdrop image specifically
     */
    fun getBackdropImageUrl(serverUrl: String): String? {
        BackdropImageTags?.firstOrNull()?.let { backdropTag ->
            return "$serverUrl/Items/$Id/Images/Backdrop/0?tag=$backdropTag&quality=90"
        }

        ImageTags?.get("Backdrop")?.let { backdropTag ->
            return "$serverUrl/Items/$Id/Images/Backdrop?tag=$backdropTag&quality=90"
        }

        // For episodes, try parent backdrop
        if (Type == "Episode" && ParentId != null) {
            ParentBackdropImageTags?.firstOrNull()?.let { backdropTag ->
                return "$serverUrl/Items/$ParentId/Images/Backdrop/0?tag=$backdropTag&quality=90"
            }
        }

        return null
    }

    fun getRunTimeMinutes(): Int? {
        return RunTimeTicks?.let { (it / 600000000).toInt() }
    }

    /**
     * Check if this item has any images available
     */
    fun hasImages(): Boolean {
        return PrimaryImageTag != null ||
                BackdropImageTags?.isNotEmpty() == true ||
                ImageTags?.isNotEmpty() == true ||
                ThumbImageTags?.isNotEmpty() == true ||
                ParentPrimaryImageTag != null
    }
}

// Keep existing JellyfinLibrary data class
data class JellyfinLibrary(
    val Id: String,
    val Name: String,
    val CollectionType: String?,
    val PrimaryImageItemId: String?,
    val PrimaryImageTag: String?,
    val ImageTags: Map<String, String>?
) {
    fun getImageUrl(serverUrl: String): String? {
        PrimaryImageTag?.let {
            return "$serverUrl/Items/$Id/Images/Primary?tag=$it&quality=90"
        }

        ImageTags?.get("Primary")?.let {
            return "$serverUrl/Items/$Id/Images/Primary?tag=$it&quality=90"
        }

        return "$serverUrl/Items/$Id/Images/Primary?quality=90"
    }
}

