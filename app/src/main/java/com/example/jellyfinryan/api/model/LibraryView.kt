package com.example.jellyfinryan.api.model

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a user's media library view from the Jellyfin API.
 */
data class LibraryView(
    @SerializedName("Id") val id: String,
    @SerializedName("Name") val name: String,
    @SerializedName("CollectionType") val collectionType: String?,
    @SerializedName("BackdropItemId") val backdropItemId: String?,
    @SerializedName("PrimaryImageTag") val imageTag: String?,
    val serverUrl: String // Injected when parsing or combining with config
) {
    /**
     * Returns the URL to the primary image of the library.
     */
    fun getPrimaryImageUrl(): String? =
        imageTag?.let { "$serverUrl/Items/$id/Images/Primary?tag=$it&fillWidth=1280&fillHeight=720" }

    /**
     * Returns the URL to the banner image of the library.
     */
    fun getBannerUrl(): String? =
        "$serverUrl/Items/$id/Images/Banner"
}


