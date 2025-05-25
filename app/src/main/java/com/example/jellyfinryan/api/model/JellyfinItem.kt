package com.example.jellyfinryan.api.model

import com.google.gson.annotations.SerializedName

data class JellyfinItem(
    @SerializedName("Id") val Id: String, // Capitalized to match JSON
    @SerializedName("Name") val Name: String, // Capitalized to match JSON
    @SerializedName("Type") val Type: String, // Capitalized to match JSON
    @SerializedName("PrimaryImageTag") val PrimaryImageTag: String?,
    @SerializedName("Overview") val Overview: String?,
    @SerializedName("PremiereDate") val PremiereDate: String?,
    @SerializedName("CommunityRating") val CommunityRating: Float?,
    @SerializedName("OfficialRating") val OfficialRating: String?,
    @SerializedName("RunTimeTicks") val RunTimeTicks: Long?,
    @SerializedName("ImageTags") val ImageTags: Map<String, String>?,
    @SerializedName("ParentId") val ParentId: String?,
    @SerializedName("BackdropImageTags") val BackdropImageTags: List<String>?,
    @SerializedName("SeriesPrimaryImageTag") val SeriesPrimaryImageTag: String?,
    @SerializedName("ParentPrimaryImageTag") val ParentPrimaryImageTag: String?,
    @SerializedName("ParentBackdropImageTags") val ParentBackdropImageTags: List<String>?,
    @SerializedName("ThumbImageTags") val ThumbImageTags: List<String>?,
    @SerializedName("ScreenshotImageTags") val ScreenshotImageTags: List<String>?,
    @SerializedName("ProductionYear") val ProductionYear: Int?,
    @SerializedName("ParentThumbImageTag") val ParentThumbImageTag: String?,
    @SerializedName("SeriesThumbImageTag") val SeriesThumbImageTag: String?,
    @SerializedName("SeriesName") val SeriesName: String? = null // Default to null for compatibility
) {
    /**
     * Get the primary image URL for vertical cards (posters)
     * Now supports both SDK and manual URL construction
     */
    fun getImageUrl(serverUrl: String, sdkRepository: com.example.jellyfinryan.api.JellyfinSdkRepository? = null, preferVertical: Boolean = false): String? { // Added preferVertical
        // Try SDK first if available - this is the proper way that working Jellyfin clients use
        sdkRepository?.let { sdk ->
            if (sdk.isAvailable()) {
                // Adjust size based on preference
                val width = if (preferVertical) 267 else 560
                val height = if (preferVertical) 400 else 315
                sdk.getPrimaryImageUrl(Id, width, height)?.let { return it }
            }
        }
        
        // Fallback to manual construction
        val width = if (preferVertical) 267 else 560
        val height = if (preferVertical) 400 else 315

        PrimaryImageTag?.let {
            return "$serverUrl/Items/$Id/Images/Primary?tag=$it&quality=96&fillHeight=$height&fillWidth=$width"
        }
        ImageTags?.get("Primary")?.let {
            return "$serverUrl/Items/$Id/Images/Primary?tag=$it&quality=96&fillHeight=$height&fillWidth=$width"
        }

        if (Type in listOf("Episode", "Season") && ParentId != null) {
            ParentPrimaryImageTag?.let {
                return "$serverUrl/Items/$ParentId/Images/Primary?tag=$it&quality=96&fillHeight=$height&fillWidth=$width"
            }
            SeriesPrimaryImageTag?.let {
                return "$serverUrl/Items/$ParentId/Images/Primary?tag=$it&quality=96&fillHeight=$height&fillWidth=$width"
            }
        }
        return "$serverUrl/Items/$Id/Images/Primary?quality=96&fillHeight=$height&fillWidth=$width"
    }

    /**
     * Get horizontal image URL for landscape cards (backdrops)
     * Now supports both SDK and manual URL construction
     */
    fun getHorizontalImageUrl(serverUrl: String, sdkRepository: com.example.jellyfinryan.api.JellyfinSdkRepository? = null): String? {
        // Try SDK first if available - this is the proper way that working Jellyfin clients use
        sdkRepository?.let { sdk ->
            if (sdk.isAvailable()) {
                sdk.getHorizontalImageUrl(Id, 560, 315)?.let { return it }
            }
        }
        
        // Fallback to manual construction for backwards compatibility
        // For movies and series, prefer backdrop images (16:9 aspect ratio)
        if (Type in listOf("Movie", "Series")) {
            BackdropImageTags?.firstOrNull()?.let { backdropTag ->
                return "$serverUrl/Items/$Id/Images/Backdrop/0?tag=$backdropTag&quality=96&fillHeight=315&fillWidth=560"
            }

            ImageTags?.get("Backdrop")?.let { backdropTag ->
                return "$serverUrl/Items/$Id/Images/Backdrop?tag=$backdropTag&quality=96&fillHeight=315&fillWidth=560"
            }
        }

        // For episodes, try episode thumb first, then series backdrop
        if (Type == "Episode") {
            ThumbImageTags?.firstOrNull()?.let { thumbTag ->
                return "$serverUrl/Items/$Id/Images/Thumb/0?tag=$thumbTag&quality=96&fillHeight=315&fillWidth=560"
            }
            ScreenshotImageTags?.firstOrNull()?.let { screenshotTag ->
                return "$serverUrl/Items/$Id/Images/Screenshot/0?tag=$screenshotTag&quality=96&fillHeight=315&fillWidth=560"
            }
            ImageTags?.get("Thumb")?.let { thumbTag ->
                return "$serverUrl/Items/$Id/Images/Thumb?tag=$thumbTag&quality=96&fillHeight=315&fillWidth=560"
            }
            if (ParentId != null) {
                ParentBackdropImageTags?.firstOrNull()?.let { backdropTag ->
                    return "$serverUrl/Items/$ParentId/Images/Backdrop/0?tag=$backdropTag&quality=96&fillHeight=315&fillWidth=560"
                }
            }
        }

        if (Type == "Season") {
            BackdropImageTags?.firstOrNull()?.let { backdropTag ->
                return "$serverUrl/Items/$Id/Images/Backdrop/0?tag=$backdropTag&quality=96&fillHeight=315&fillWidth=560"
            }
            if (ParentId != null) {
                ParentBackdropImageTags?.firstOrNull()?.let { backdropTag ->
                    return "$serverUrl/Items/$ParentId/Images/Backdrop/0?tag=$backdropTag&quality=96&fillHeight=315&fillWidth=560"
                }
            }
            PrimaryImageTag?.let { tag ->
                return "$serverUrl/Items/$Id/Images/Primary?tag=$tag&quality=96&fillHeight=315&fillWidth=560"
            }
        }
        return getImageUrl(serverUrl, sdkRepository, preferVertical = false)?.replace("fillHeight=400&fillWidth=267", "fillHeight=315&fillWidth=560")
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
    }    /**
     * Get the best image URL for Featured Carousel (prioritizes largest backdrops)
     * Now supports both SDK and manual URL construction
     */
    fun getFeaturedCarouselImageUrl(serverUrl: String, sdkRepository: com.example.jellyfinryan.api.JellyfinSdkRepository? = null): String? {
        // Try SDK first if available - this is the proper way that working Jellyfin clients use
        sdkRepository?.let { sdk ->
            if (sdk.isAvailable()) {
                sdk.getFeaturedCarouselImageUrl(Id)?.let { return it }
            }
        }
        
        // Fallback to manual construction for backwards compatibility
        BackdropImageTags?.firstOrNull()?.let { backdropTag ->
            return "$serverUrl/Items/$Id/Images/Backdrop/0?tag=$backdropTag&quality=96&fillHeight=720&fillWidth=1280"
        }
        ImageTags?.get("Backdrop")?.let { backdropTag ->
            return "$serverUrl/Items/$Id/Images/Backdrop?tag=$backdropTag&quality=96&fillHeight=720&fillWidth=1280"
        }
        if (Type == "Episode" && ParentId != null) {
            ParentBackdropImageTags?.firstOrNull()?.let { backdropTag ->
                return "$serverUrl/Items/$ParentId/Images/Backdrop/0?tag=$backdropTag&quality=96&fillHeight=720&fillWidth=1280"
            }
        }
        PrimaryImageTag?.let { tag ->
            return "$serverUrl/Items/$Id/Images/Primary?tag=$tag&quality=96&fillHeight=720&fillWidth=1280"
        }
        ImageTags?.get("Primary")?.let { tag ->
            return "$serverUrl/Items/$Id/Images/Primary?tag=$tag&quality=96&fillHeight=720&fillWidth=1280"
        }
        return null // Explicitly return null if no suitable image is found
    }/**
     * Get backdrop image specifically for Featured Carousel (largest size)
     * Uses optimal sizing for fullscreen carousel backgrounds
     */
    fun getBackdropImageUrl(serverUrl: String): String? {
        BackdropImageTags?.firstOrNull()?.let { backdropTag ->
            return "$serverUrl/Items/$Id/Images/Backdrop/0?tag=$backdropTag&quality=96&fillHeight=720&fillWidth=1280"
        }
        ImageTags?.get("Backdrop")?.let { backdropTag ->
            return "$serverUrl/Items/$Id/Images/Backdrop?tag=$backdropTag&quality=96&fillHeight=720&fillWidth=1280"
        }
        if (Type == "Episode" && ParentId != null) {
            ParentBackdropImageTags?.firstOrNull()?.let { backdropTag ->
                return "$serverUrl/Items/$ParentId/Images/Backdrop/0?tag=$backdropTag&quality=96&fillHeight=720&fillWidth=1280"
            }
        }
        // Fallback for items that might not have specific backdrop tags but have a primary image that can be used as a backdrop
        PrimaryImageTag?.let {
            return "$serverUrl/Items/$Id/Images/Primary?tag=$it&quality=96&fillHeight=720&fillWidth=1280"
        }
        ImageTags?.get("Primary")?.let {
            return "$serverUrl/Items/$Id/Images/Primary?tag=$it&quality=96&fillHeight=720&fillWidth=1280"
        }
        return null // Explicitly return null if no suitable image is found
    }

    /**
     * Get the URL for a vertical card image (e.g., poster for movies/series, episode thumbnail if vertical).
     * Prioritizes specific vertical image types, then primary image, then backdrop.
     */
    fun getVerticalCardImageUrl(serverUrl: String): String? {
        // Attempt to get a specifically tagged "Primary" image first, assuming it's poster-like.
        PrimaryImageTag?.let {
            return "$serverUrl/Items/$Id/Images/Primary?tag=$it&quality=90&fillHeight=280&fillWidth=200"
        }
        ImageTags?.get("Primary")?.let {
            return "$serverUrl/Items/$Id/Images/Primary?tag=$it&quality=90&fillHeight=280&fillWidth=200"
        }

        // For Episodes, try to get the Series' Primary image (poster) if the episode itself doesn't have one.
        if (Type == "Episode" && ParentId != null) {
            SeriesPrimaryImageTag?.let { // This should be the series poster
                return "$serverUrl/Items/$ParentId/Images/Primary?tag=$it&quality=90&fillHeight=280&fillWidth=200"
            }
            // Fallback to ParentPrimaryImageTag if SeriesPrimaryImageTag is not available for some reason
            ParentPrimaryImageTag?.let {
                 return "$serverUrl/Items/$ParentId/Images/Primary?tag=$it&quality=90&fillHeight=280&fillWidth=200"
            }
        }

        // If no primary, try a backdrop image, resized to fit the vertical card dimensions.
        // This is a fallback and might not look ideal, but it's better than no image.
        BackdropImageTags?.firstOrNull()?.let { backdropTag ->
            return "$serverUrl/Items/$Id/Images/Backdrop/0?tag=$backdropTag&quality=90&fillHeight=280&fillWidth=200&crop=true" // Added crop=true
        }
        ImageTags?.get("Backdrop")?.let { backdropTag ->
            return "$serverUrl/Items/$Id/Images/Backdrop?tag=$backdropTag&quality=90&fillHeight=280&fillWidth=200&crop=true" // Added crop=true
        }
        
        // Generic fallback to item's primary image without specific tag, sized for vertical card
        return "$serverUrl/Items/$Id/Images/Primary?quality=90&fillHeight=280&fillWidth=200"
    }

    /**
     * Get the URL for a horizontal card image (e.g., 16:9 thumbnail for episodes or generic items).
     * Prioritizes specific horizontal image types (Thumb, Screenshot, Backdrop), then primary image.
     */
    fun getHorizontalCardImageUrl(serverUrl: String): String? {
        // For episodes, prioritize Thumb or Screenshot images.
        if (Type == "Episode") {
            ThumbImageTags?.firstOrNull()?.let {
                return "$serverUrl/Items/$Id/Images/Thumb/0?tag=$it&quality=90&fillHeight=180&fillWidth=320"
            }
            ImageTags?.get("Thumb")?.let {
                return "$serverUrl/Items/$Id/Images/Thumb?tag=$it&quality=90&fillHeight=180&fillWidth=320"
            }
            ScreenshotImageTags?.firstOrNull()?.let {
                return "$serverUrl/Items/$Id/Images/Screenshot/0?tag=$it&quality=90&fillHeight=180&fillWidth=320"
            }
            ImageTags?.get("Screenshot")?.let {
                return "$serverUrl/Items/$Id/Images/Screenshot?tag=$it&quality=90&fillHeight=180&fillWidth=320"
            }
            // Fallback to Series Backdrop for episodes if no specific episode image is found.
            if (ParentId != null) {
                ParentBackdropImageTags?.firstOrNull()?.let { backdropTag ->
                    return "$serverUrl/Items/$ParentId/Images/Backdrop/0?tag=$backdropTag&quality=90&fillHeight=180&fillWidth=320"
                }
            }
        }

        // For other types, or as a fallback for episodes, try Backdrop images.
        BackdropImageTags?.firstOrNull()?.let { backdropTag ->
            return "$serverUrl/Items/$Id/Images/Backdrop/0?tag=$backdropTag&quality=90&fillHeight=180&fillWidth=320"
        }
        ImageTags?.get("Backdrop")?.let { backdropTag ->
            return "$serverUrl/Items/$Id/Images/Backdrop?tag=$backdropTag&quality=90&fillHeight=180&fillWidth=320"
        }

        // Fallback to the primary image, resized for horizontal card.
        // This might crop or not look ideal if the primary image is a portrait poster.
        PrimaryImageTag?.let {
            return "$serverUrl/Items/$Id/Images/Primary?tag=$it&quality=90&fillHeight=180&fillWidth=320&crop=true" // Added crop=true
        }
        ImageTags?.get("Primary")?.let {
            return "$serverUrl/Items/$Id/Images/Primary?tag=$it&quality=90&fillHeight=180&fillWidth=320&crop=true" // Added crop=true
        }
        
        // Generic fallback to item's primary image without specific tag, sized for horizontal card
        return "$serverUrl/Items/$Id/Images/Primary?quality=90&fillHeight=180&fillWidth=320&crop=true" // Added crop=true
    }

    /**
     * Returns the runtime in minutes, or null if not available.
     */
    fun getRunTimeMinutes(): Int? {
        return RunTimeTicks?.let { (it / 600000000L).toInt() }
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
            return "$serverUrl/Items/$Id/Images/Primary?tag=$it&quality=96&fillHeight=315&fillWidth=560"
        }

        ImageTags?.get("Primary")?.let {
            return "$serverUrl/Items/$Id/Images/Primary?tag=$it&quality=96&fillHeight=315&fillWidth=560"
        }

        return "$serverUrl/Items/$Id/Images/Primary?quality=96&fillHeight=315&fillWidth=560"
    }
}

