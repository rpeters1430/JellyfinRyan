# Jellyfin Image API Improvements

## Overview
This document outlines the comprehensive improvements made to the Jellyfin image fetching system for the Android TV client, specifically targeting the Featured Carousel and Recently Added sections.

## 🎯 Issues Addressed

### 1. Featured Carousel Image Quality
- **Problem**: Carousel was using lower quality images not optimized for large TV displays
- **Solution**: Implemented `getFeaturedCarouselImageUrl()` method with 1280x720 resolution and 96% quality

### 2. Recently Added Horizontal Cards
- **Problem**: Cards were not properly displaying backdrop images for optimal horizontal layout
- **Solution**: Enhanced `getHorizontalImageUrl()` with proper 16:9 aspect ratio (560x315) images

### 3. API Image Metadata
- **Problem**: API calls weren't requesting sufficient image metadata fields
- **Solution**: Enhanced API endpoints to include comprehensive image fields

## 🔧 Technical Improvements

### JellyfinItem.kt Enhancements

#### 1. Primary Image URLs (Vertical Cards)
```kotlin
fun getImageUrl(serverUrl: String): String?
```
- **Quality**: 96% (up from 90%)
- **Dimensions**: 400x267 (optimal for vertical poster cards)
- **Fallback Chain**: PrimaryImageTag → ImageTags["Primary"] → Parent images → Untagged fallback

#### 2. Horizontal Image URLs (Recently Added Cards)
```kotlin
fun getHorizontalImageUrl(serverUrl: String): String?
```
- **Quality**: 96%
- **Dimensions**: 560x315 (16:9 aspect ratio for TV)
- **Prioritization**:
  - Movies/Series: Backdrop images preferred
  - Episodes: Thumb → Screenshot → Series backdrop
  - Seasons: Season backdrop → Series backdrop → Primary with horizontal sizing

#### 3. Featured Carousel Images (Largest Quality)
```kotlin
fun getFeaturedCarouselImageUrl(serverUrl: String): String?
```
- **Quality**: 96%
- **Dimensions**: 1280x720 (full HD for carousel backgrounds)
- **Prioritization**: Backdrop images → Primary images with large sizing

#### 4. Backdrop Images (Specific Use)
```kotlin
fun getBackdropImageUrl(serverUrl: String): String?
```
- **Quality**: 96%
- **Dimensions**: 1280x720 (HD resolution)
- **Purpose**: Dedicated backdrop fetching for specific contexts

### API Service Enhancements

#### 1. Enhanced Latest Items Endpoint
```kotlin
@GET("Users/{userId}/Items/Latest")
suspend fun getLatestItems(...)
```
**Improvements**:
- `ImageTypeLimit`: Increased to 3 (from 1)
- `EnableImageTypes`: Added Screenshot, Logo
- `Fields`: Added comprehensive image metadata fields

#### 2. Enhanced Items with Images Endpoint
```kotlin
@GET("Users/{userId}/Items")
suspend fun getItemsWithImages(...)
```
**Improvements**:
- `ImageTypeLimit`: Increased to 3
- `Fields`: Added ParentBackdropImageTags, ParentPrimaryImageTag, ParentThumbImageTag
- `EnableImageTypes`: Comprehensive image type support

#### 3. Recently Added Items Endpoint
```kotlin
@GET("Users/{userId}/Items")
suspend fun getRecentlyAddedItems(...)
```
**New Features**:
- `SortBy`: "DateAdded" for true recently added sorting
- `Recursive`: true for deep library scanning
- Optimized image fields for horizontal card display

### Repository Improvements

#### Recently Added Items Fetching
- Switched to dedicated `getRecentlyAddedItems()` endpoint
- Enhanced debug logging for image URL generation
- Better error handling and fallback mechanisms

## 📱 UI Component Updates

### Featured Carousel
- Updated to use `getFeaturedCarouselImageUrl()` for highest quality images
- Optimized for 1280x720 full HD carousel backgrounds
- Better image selection prioritizing backdrop images

### Horizontal Media Cards
- Maintained use of `getHorizontalImageUrl()` for optimal 16:9 aspect ratio
- Enhanced fallback chain for different content types
- Debug logging for troubleshooting image loading issues

## 🎨 Image Sizing Strategy

### Size Categories
1. **Vertical Cards (Posters)**: 400x267 (3:2 aspect ratio)
2. **Horizontal Cards**: 560x315 (16:9 aspect ratio)
3. **Featured Carousel**: 1280x720 (HD 16:9)
4. **Library Cards**: 560x315 (horizontal layout)

### Quality Settings
- **Standard Quality**: 96% (increased from 90%)
- **Image Format**: Jellyfin's optimized format with fill parameters
- **Caching**: Handled by Coil image loader

## 🔗 Jellyfin API Compliance

All image URLs follow the official Jellyfin API specification:
```
/Items/{itemId}/Images/{imageType}?tag={imageTag}&quality=96&fillHeight={height}&fillWidth={width}
```

### Supported Image Types
- **Primary**: Main poster/thumbnail images
- **Backdrop**: Widescreen background images
- **Thumb**: Episode thumbnails
- **Screenshot**: Episode screenshots
- **Logo**: Series/movie logos
- **Banner**: Horizontal banner images

## 🚀 Performance Benefits

1. **Optimized Bandwidth**: Proper image sizing reduces unnecessary data transfer
2. **Better Caching**: Consistent URL patterns improve cache efficiency
3. **Enhanced UX**: Higher quality images improve visual appeal
4. **Fallback Reliability**: Multiple fallback options ensure images always display

## 🧪 Testing Recommendations

1. **Network Conditions**: Test on various network speeds
2. **Content Types**: Verify images for Movies, Series, Episodes, Seasons
3. **Image Availability**: Test with content having different image combinations
4. **Cache Behavior**: Verify image caching and reload scenarios
5. **TV Displays**: Test on actual Android TV devices with various screen sizes

## 📋 Future Enhancements

1. **Dynamic Quality**: Adapt image quality based on network speed
2. **Progressive Loading**: Implement low-quality placeholders
3. **Image Prefetching**: Pre-load images for smoother navigation
4. **Custom Aspect Ratios**: Support for different TV screen ratios
