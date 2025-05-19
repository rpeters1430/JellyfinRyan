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
    val RunTimeTicks: Long?
) {
    fun getImageUrl(serverUrl: String): String? {
        return if (PrimaryImageTag != null) {
            "$serverUrl/Items/$Id/Images/Primary?tag=$PrimaryImageTag"
        } else null
    }

    fun getRunTimeMinutes(): Int? {
        return RunTimeTicks?.let { (it / 600000000).toInt() }
    }
}