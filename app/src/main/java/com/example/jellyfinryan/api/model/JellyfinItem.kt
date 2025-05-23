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
    fun getImageUrl(serverUrl: String): String? {
        val imageUrl = when {
            PrimaryImageTag != null -> {
                "$serverUrl/Items/$Id/Images/Primary?tag=$PrimaryImageTag"
            }
            ImageTags?.get("Primary") != null -> {
                "$serverUrl/Items/$Id/Images/Primary?tag=${ImageTags["Primary"]}"
            }
            else -> null
        }

        Log.d("JellyfinItem", "Image URL for '$Name': $imageUrl")
        return imageUrl
    }

    fun getRunTimeMinutes(): Int? {
        return RunTimeTicks?.let { (it / 600000000).toInt() }
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
            else -> null
        }

        Log.d("JellyfinLibrary", "Image URL for '$Name': $imageUrl")
        return imageUrl
    }
}


