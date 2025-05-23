package com.example.jellyfinryan.api.model

import android.util.Log

data class JellyfinItem(
    val Id: String,
    val Name: String,
    val Type: String,
    val PrimaryImageTag: String?, // Can be null, especially for newer servers relying on ImageTags
    val Overview: String?,
    val PremiereDate: String?, // Format typically "YYYY-MM-DDTHH:mm:ss.SSSSSSSZ"
    val CommunityRating: Float?,
    val OfficialRating: String?,
    val RunTimeTicks: Long?, // Runtime in ticks (10,000 ticks = 1 millisecond)
    val ImageTags: Map<String, String>?, // Preferred source for image tags like Primary, Banner, Logo
    val ParentId: String?,
    val BackdropImageTags: List<String>? // List of tags for backdrop images
) {
    /**
     * Gets a typed image URL for the item.
     * Common types: "Primary", "Banner", "Logo", "Thumb".
     */
    fun getImageUrl(serverUrl: String, type: String = "Primary"): String? {
        val imageTag = ImageTags?.get(type)
        // Fallback to PrimaryImageTag if type is Primary and no specific tag found in ImageTags
            ?: if (type == "Primary") PrimaryImageTag else null

        val url = imageTag?.let { tag ->
            "$serverUrl/Items/$Id/Images/$type?tag=$tag"
        }
        // Log.d("JellyfinItem", "Image URL for '$Name' (type $type): $url")
        return url
    }

    /**
     * Gets the primary image URL using the most reliable fields.
     */
    fun getPrimaryImageUrl(serverUrl: String): String? {
        // Prefer ImageTags["Primary"], then PrimaryImageTag
        val tag = ImageTags?.get("Primary") ?: PrimaryImageTag
        val url = tag?.let { "$serverUrl/Items/$Id/Images/Primary?tag=$it" }
        if (tag == null) Log.d("JellyfinItem", "No Primary image tag found for '$Name'")
        Log.d("JellyfinItem", "Primary Image URL for '$Name': $url")
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
     * Converts RunTimeTicks to minutes.
     */
    fun getRunTimeMinutes(): Int? {
        return RunTimeTicks?.let { (it / (10000L * 1000L * 60L)).toInt() } // Ticks to minutes
    }

    /**
     * Extracts the year from PremiereDate.
     */
    val year: String?
        get() = PremiereDate?.takeIf { it.length >= 4 }?.substring(0, 4)
}

// The JellyfinLibrary data class can remain the same for now.
// If you also want typed images for libraries, it could be similarly updated.
data class JellyfinLibrary(
    val Id: String,
    val Name: String,
    val CollectionType: String?,
    // These fields might not always be directly available for a library itself.
    // Library images are often managed by its DisplayPreferences or folder image.
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
                // Fallback for libraries if they don't have direct PrimaryImageTag or ImageTags.
                // This might point to a generic folder icon or require a different API endpoint.
                // For example, for a library, you might construct a banner URL:
                // "$serverUrl/Items/$Id/Images/Banner"
                // Or rely on the client to show a default based on CollectionType.
                null
            }
        }
        Log.d("JellyfinLibrary", "Image URL for library '$Name': $imageUrl")
        return imageUrl
    }
}


