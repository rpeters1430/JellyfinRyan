package com.example.jellyfinryan.api.model

import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ImageType
import com.example.jellyfinryan.utils.UnsafeKtorClient

/**
 * Wrapper class for Jellyfin SDK BaseItemDto to maintain compatibility
 * while providing proper image URL functionality
 */
data class JellyfinSdkItem(
    private val baseItem: BaseItemDto,
    private val sdkService: com.example.jellyfinryan.api.JellyfinSdkService
) {
    val id: String = baseItem.id.toString()
    val name: String = baseItem.name ?: ""
    val type: String = baseItem.type?.toString() ?: ""
    val overview: String? = baseItem.overview
    val premiereDate: String? = baseItem.premiereDate?.toString()
    val communityRating: Float? = baseItem.communityRating?.toFloat()
    val officialRating: String? = baseItem.officialRating
    val runTimeTicks: Long? = baseItem.runTimeTicks
    val parentId: String? = baseItem.parentId?.toString()
    val productionYear: Int? = baseItem.productionYear
    val seriesName: String? = baseItem.seriesName // <-- Add this line
    
    /**
     * Get the primary image URL for vertical cards (posters)
     * Uses the official Jellyfin SDK for proper URL construction
     */
    fun getImageUrl(): String? {
        return sdkService.getPrimaryImageUrl(
            item = baseItem,
            maxWidth = 267,
            maxHeight = 400,
            quality = 96
        )
    }
    
    /**
     * Get horizontal image URL for landscape cards (backdrops)
     * Perfect for "Recently Added" horizontal cards
     */
    fun getHorizontalImageUrl(): String? {
        // Try backdrop first for movies/series
        when (baseItem.type?.toString()) {
            "Movie", "Series" -> {
                sdkService.getBackdropImageUrl(
                    item = baseItem,
                    maxWidth = 560,
                    maxHeight = 315,
                    quality = 96
                )?.let { return it }
            }
            "Episode" -> {
                // Try thumb first for episodes
                sdkService.getThumbImageUrl(
                    item = baseItem,
                    maxWidth = 560,
                    maxHeight = 315,
                    quality = 96
                )?.let { return it }
                
                // Fallback to backdrop
                sdkService.getBackdropImageUrl(
                    item = baseItem,
                    maxWidth = 560,
                    maxHeight = 315,
                    quality = 96
                )?.let { return it }
            }
        }
        
        // Fallback to primary with horizontal sizing
        return sdkService.getPrimaryImageUrl(
            item = baseItem,
            maxWidth = 560,
            maxHeight = 315,
            quality = 96
        )
    }
    
    /**
     * Get the highest quality image URL for featured carousel
     * Uses HD resolution for maximum visual impact
     */
    fun getFeaturedCarouselImageUrl(): String? {
        // Try backdrop first in HD quality
        sdkService.getBackdropImageUrl(
            item = baseItem,
            maxWidth = 1280,
            maxHeight = 720,
            quality = 96
        )?.let { return it }
        
        // Fallback to primary in HD
        return sdkService.getPrimaryImageUrl(
            item = baseItem,
            maxWidth = 1280,
            maxHeight = 720,
            quality = 96
        )
    }
    
    /**
     * Get backdrop image URL specifically
     */
    fun getBackdropImageUrl(): String? {
        return sdkService.getBackdropImageUrl(
            item = baseItem,
            maxWidth = 1280,
            maxHeight = 720,
            quality = 96
        )
    }
    
    /**
     * Get the best available image URL for any context
     */
    fun getBestImageUrl(): String? {
        return getImageUrl() ?: getHorizontalImageUrl() ?: getFeaturedCarouselImageUrl()
    }
    
    /**
     * Convert to the old JellyfinItem format for backwards compatibility
     */
    fun toJellyfinItem(): com.example.jellyfinryan.api.model.JellyfinItem {
        return com.example.jellyfinryan.api.model.JellyfinItem(
            Id = id,
            Name = name,
            Type = type,
            PrimaryImageTag = null, // Not needed with SDK
            Overview = overview,
            PremiereDate = premiereDate,
            CommunityRating = communityRating,
            OfficialRating = officialRating,
            RunTimeTicks = runTimeTicks,
            ImageTags = null, // Not needed with SDK
            ParentId = parentId,
            BackdropImageTags = null, // Not needed with SDK
            SeriesPrimaryImageTag = null, // Not needed with SDK
            ParentPrimaryImageTag = null, // Not needed with SDK
            ParentBackdropImageTags = null, // Not needed with SDK
            ThumbImageTags = null, // Not needed with SDK
            ScreenshotImageTags = null, // Not needed with SDK
            ProductionYear = productionYear,
            ParentThumbImageTag = null, // Not needed with SDK
            SeriesThumbImageTag = null, // Not needed with SDK
            SeriesName = seriesName // <-- Add this line
        )
    }
}

/**
 * Library item wrapper for SDK compatibility
 */
data class JellyfinSdkLibrary(
    private val baseItem: BaseItemDto,
    private val sdkService: com.example.jellyfinryan.api.JellyfinSdkService
) {
    val id: String = baseItem.id.toString()
    val name: String = baseItem.name ?: ""
    val type: String = baseItem.type?.toString() ?: ""
    
    fun getImageUrl(): String? {
        return sdkService.getPrimaryImageUrl(
            item = baseItem,
            maxWidth = 560,
            maxHeight = 315,
            quality = 96
        )
    }
      /**
     * Convert to the old JellyfinLibrary format for backwards compatibility
     */    fun toJellyfinLibrary(): com.example.jellyfinryan.api.model.JellyfinLibrary {
        return com.example.jellyfinryan.api.model.JellyfinLibrary(
            Id = id,
            Name = name,
            CollectionType = baseItem.collectionType?.toString(),
            PrimaryImageItemId = baseItem.id.toString(),
            PrimaryImageTag = null, // Not used with SDK
            ImageTags = null // Not used with SDK
        )
    }
}
