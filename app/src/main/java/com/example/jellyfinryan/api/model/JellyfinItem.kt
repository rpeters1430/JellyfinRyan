package com.example.jellyfinryan.api.model

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
) {    fun getImageUrl(serverUrl: String): String? {
        // Try PrimaryImageTag first
        PrimaryImageTag?.let { tag ->
            return "$serverUrl/Items/$Id/Images/Primary?tag=$tag"
        }

        // Try ImageTags map for Primary
        ImageTags?.get("Primary")?.let { tag ->
            return "$serverUrl/Items/$Id/Images/Primary?tag=$tag"
        }

        // Fallback to primary image without tag (Jellyfin will still serve it)
        return "$serverUrl/Items/$Id/Images/Primary"
    }

    fun getRunTimeMinutes(): Int? {
        return RunTimeTicks?.let { (it / 600000000).toInt() }
    }

    fun getHorizontalImageUrl(serverUrl: String): String? {
        // For horizontal cards, prefer backdrop images for movies/series/episodes
        BackdropImageTags?.firstOrNull()?.let { backdropTag ->
            return "$serverUrl/Items/$Id/Images/Backdrop/0?tag=$backdropTag"
        }
        
        // Try ImageTags for Backdrop
        ImageTags?.get("Backdrop")?.let { backdropTag ->
            return "$serverUrl/Items/$Id/Images/Backdrop?tag=$backdropTag"
        }

        // For series/episodes, try to get parent backdrop
        if (Type in listOf("Series", "Season", "Episode") && ParentId != null) {
            // Fallback to primary image but request it in a more horizontal format
            return getImageUrl(serverUrl)?.let { url ->
                "$url&maxWidth=480&maxHeight=270" // 16:9 aspect ratio
            }
        }

        // Fallback to primary image
        return getImageUrl(serverUrl)
    }
}
// ✅ Fixed: Include ImageTags in JellyfinLibrary too
data class JellyfinLibrary(
    val Id: String,
    val Name: String,
    val CollectionType: String?,
    val PrimaryImageItemId: String?,
    val PrimaryImageTag: String?,
    val ImageTags: Map<String, String>? // <-- add this field
) {
    fun getImageUrl(serverUrl: String): String? {
        PrimaryImageTag?.let {
            return "$serverUrl/Items/$Id/Images/Primary?tag=$it"
        }

        ImageTags?.get("Primary")?.let {
            return "$serverUrl/Items/$Id/Images/Primary?tag=$it"
        }

        return null
    }
}


