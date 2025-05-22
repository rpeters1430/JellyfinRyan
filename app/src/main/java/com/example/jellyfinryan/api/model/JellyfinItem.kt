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

    fun getRunTimeMinutes(): Int? {
        return RunTimeTicks?.let { (it / 600000000).toInt() }
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
