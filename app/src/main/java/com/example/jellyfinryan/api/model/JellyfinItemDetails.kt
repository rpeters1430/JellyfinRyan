package com.example.jellyfinryan.api.model

import com.google.gson.annotations.SerializedName

// Based on typical Jellyfin API responses for an item. Adjust fields as needed.
data class JellyfinItemDetails(
    @SerializedName("Id") val id: String,
    @SerializedName("Name") val name: String?,
    @SerializedName("OriginalTitle") val originalTitle: String?,
    @SerializedName("ServerId") val serverId: String?,
    @SerializedName("Etag") val etag: String?,
    @SerializedName("Type") val type: String, // e.g., "Series", "Movie", "Episode"
    @SerializedName("PremiereDate") val premiereDate: String?, // Format: "YYYY-MM-DDTHH:mm:ss.SSSSSSSZ"
    @SerializedName("ProductionYear") val productionYear: Int?,
    @SerializedName("Overview") val overview: String?,
    @SerializedName("CommunityRating") val communityRating: Float?,
    @SerializedName("OfficialRating") val officialRating: String?, // e.g., "TV-MA", "PG-13"
    @SerializedName("RunTimeTicks") val runTimeTicks: Long?, // Convert to duration: ticks / 10,000,000 = seconds
    @SerializedName("ImageTags") val imageTags: Map<String, String>?, // e.g., {"Primary": "tag1", "Thumb": "tag2", "Backdrop": "tag3"}
    @SerializedName("BackdropImageTags") val backdropImageTags: List<String>?, // List of backdrop tags
    @SerializedName("UserData") val userData: ItemUserData?,
    @SerializedName("Studios") val studios: List<NameIdPair>?,
    @SerializedName("Genres") val genres: List<String>?,
    @SerializedName("People") val people: List<PersonInfo>?,
    // For Series type
    @SerializedName("SeasonCount") val seasonCount: Int?,
    @SerializedName("Status") val status: String?, // e.g., "Continuing", "Ended"
    // You might also get fields like 'SeriesName', 'SeriesId', 'SeasonName' if it's an episode or season itself.
    // For Movies
    // ... (any movie specific fields not covered)
    // For Episodes
    @SerializedName("IndexNumber") val indexNumber: Int?, // Episode number
    @SerializedName("ParentIndexNumber") val parentIndexNumber: Int?, // Season number for an episode
    @SerializedName("SeriesName") val seriesName: String?,
    @SerializedName("SeriesId") val seriesId: String?,
    @SerializedName("SeasonId") val seasonId: String?,
    @SerializedName("SeasonName") val seasonName: String?
)

data class ItemUserData(
    @SerializedName("PlaybackPositionTicks") val playbackPositionTicks: Long,
    @SerializedName("PlayCount") val playCount: Int,
    @SerializedName("IsFavorite") val isFavorite: Boolean,
    @SerializedName("Played") val played: Boolean,
    @SerializedName("LastPlayedDate") val lastPlayedDate: String? // Format: "YYYY-MM-DDTHH:mm:ss.SSSSSSSZ"
)

data class NameIdPair( // Used for Studios, etc.
    @SerializedName("Name") val name: String?,
    @SerializedName("Id") val id: String?
)

data class PersonInfo(
    @SerializedName("Name") val name: String?,
    @SerializedName("Id") val id: String?,
    @SerializedName("Role") val role: String?,
    @SerializedName("Type") val type: String?, // e.g., "Actor", "Director", "Writer"
    @SerializedName("PrimaryImageTag") val primaryImageTag: String?
)

// Data class for a Show Season
data class ShowSeason(
    @SerializedName("Id") val id: String,
    @SerializedName("Name") val name: String?,
    @SerializedName("IndexNumber") val indexNumber: Int?, // Season number (e.g., 0 for Specials, 1 for Season 1)
    @SerializedName("Type") val type: String = "Season", // Typically "Season"
    @SerializedName("ImageTags") val imageTags: Map<String, String>?, // e.g., {"Primary": "someTag"}
    @SerializedName("UserData") val userData: ItemUserData?,
    @SerializedName("LocationType") val locationType: String?,
    @SerializedName("ProductionYear") val productionYear: Int?, // Year the season was produced/aired
    @SerializedName("ChildCount") val childCount: Int? // Number of episodes in this season
    // Add any other relevant fields from the API
)