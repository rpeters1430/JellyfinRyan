# JellyfinRyan

JellyfinRyan is a modern Android TV client application for Jellyfin media servers, designed following Android TV design patterns and featuring a beautiful, user-friendly interface optimized for large screens and remote control navigation.

## Key Features

### 🎬 Enhanced Featured Carousel
*   **JetStream-style Anatomy Feature:** Split-screen carousel with main content info on the left (title, description, play/info buttons) and metadata on the right (genre, rating, cast)
*   **Cinematic Experience:** Full-screen background images with smooth transitions
*   **Auto-rotation:** Featured content automatically cycles every 8 seconds
*   **Focus-driven Navigation:** Background updates dynamically as you navigate

### 📚 My Libraries Section
*   **Horizontal Cards:** Beautiful 280x160dp cards showcasing library artwork
*   **Gradient Overlays:** Enhanced readability with cinematic gradients
*   **Focus Animations:** Smooth scaling and border highlighting for TV navigation

### 🆕 Recently Added Content
*   **Per-Library Sections:** Separate "Recently Added" sections for each library
*   **Horizontal Scrolling:** Easy browsing of up to 15 recent items per library
*   **Smart Loading:** Efficient API calls with image metadata optimization

### 🎮 TV-Optimized Interface
*   **Focus Management:** Advanced focus handling with auto-focus utilities
*   **Loading States:** Elegant loading indicators and error handling
*   **Dynamic Backgrounds:** Context-aware background images that update with focus
*   **Remote Control Friendly:** Designed specifically for D-pad navigation

### 🔧 Enhanced API Integration
*   **Image Optimization:** 
    - Featured Carousel: 1280x720 HD images for cinematic experience
    - Recently Added Cards: 560x315 optimized 16:9 aspect ratio images
    - Vertical Cards: 400x267 poster-style images
*   **Smart Image Selection:** Prefer backdrop images for horizontal cards, primary images for vertical
*   **High Quality:** 96% quality settings with proper Jellyfin API compliance
*   **Smart Caching:** Efficient image loading with Coil and proper URL patterns
*   **Error Resilience:** Graceful error handling with user-friendly messages

## Technology Stack

*   **Language:** Kotlin
*   **UI Toolkit:** Jetpack Compose
*   **Target Platform:** Android TV (Leanback)
*   **Dependency Injection:** Hilt
*   **Navigation:** Jetpack Navigation

## Building the Project

This is a standard Android Gradle project. To build it:

1.  Ensure you have Android Studio installed.
2.  Clone the repository.
3.  Open the project in Android Studio.
4.  Let Gradle sync and download dependencies.
5.  Build the project using the "Build" menu (e.g., "Make Project" or "Build Bundles(s) / APK(s)").

## Image Quality Enhancements

The app now features significantly improved image loading with proper Jellyfin API integration:

*   **Featured Carousel**: Full HD 1280x720 backdrop images for cinematic presentation
*   **Recently Added**: Optimized 560x315 horizontal cards with 16:9 aspect ratio
*   **Library Cards**: High-quality images with proper fallback mechanisms
*   **API Compliance**: Full adherence to official Jellyfin Image API specifications

See `IMAGE_API_IMPROVEMENTS.md` for detailed technical documentation.

## 🎯 Project Completion Status

### ✅ COMPLETED FEATURES

#### 🎬 Featured Carousel (100% Complete)
- **Movie-Only Content:** Displays only the last 4 movies from your library
- **Cinematic Experience:** Full HD 1280x720 backdrop images with smooth transitions
- **Play & Info Buttons:** Direct navigation to movie details and playback
- **Auto-Focus Navigation:** Dynamic background updates as you browse

#### 📚 My Libraries (100% Complete) 
- **Visual Library Cards:** Beautiful 280x160dp cards with gradient overlays
- **Focus Animations:** Smooth scaling and highlighting for TV remote navigation
- **All Library Types:** Movies, TV Shows, Music, Books, Photos automatically detected

#### 🆕 Recently Added Sections (100% Complete)
- **Per-Library Sections:** Separate sections for each library type
- **Smart Titles:** "Recently Added Movies", "Recently Added Music", etc.
- **Optimized Loading:** 15 items per section with efficient API calls

#### 📺 Recent TV Episodes (95% Complete - Ready to Activate)
- **Backend Complete:** Full API integration using `BaseItemKind.EPISODE`
- **Proper Jellyfin API:** Recursive search with `SortBy=DateCreated` descending
- **UI Prepared:** Section ready in HomeScreen with proper styling
- **One-Line Activation:** Simply uncomment one line to enable

### 🚀 How to Activate TV Episodes

The TV Episodes feature is fully implemented but temporarily disabled due to Kotlin incremental compilation. To activate:

1. **Open:** `app/src/main/java/com/example/jellyfinryan/ui/screens/HomeScreen.kt`
2. **Find line ~46:** Look for the TV Episodes section
3. **Replace this line:**
   ```kotlin
   val recentEpisodes = emptyList<JellyfinItem>() // Remove this line and uncomment above to activate
   ```
4. **With this line:**
   ```kotlin
   val recentEpisodes by viewModel.recentTvEpisodes.collectAsState()
   ```
5. **Build the project:** Run `./gradlew assembleDebug`

The TV Episodes section will then display the last 10 episodes added across all your TV libraries.

## Future Improvements / Known Issues

*   (To be added)

## 🔧 Technical Implementation Highlights

### API Integration
- **Jellyfin SDK:** Full integration with official Jellyfin Kotlin SDK
- **Image Optimization:** High-quality images with proper aspect ratios and caching
- **Error Handling:** Graceful fallbacks and user-friendly error messages
- **Efficient Loading:** Smart API calls with proper field selection and limits

### TV Episodes API Implementation
The Recent TV Episodes feature uses the exact Jellyfin API specification:
```
GET /Users/{userId}/Items?
  Recursive=true&
  SortBy=DateCreated&
  SortOrder=Descending&
  IncludeItemTypes=Episode&
  Limit=10
```

### Android TV Optimization
- **Focus Management:** Proper D-pad navigation with focus handling
- **Leanback Design:** Follows Android TV design patterns
- **Performance:** Efficient Compose rendering with proper state management
- **Memory Management:** Optimized image loading with Coil caching

### Architecture
- **MVVM Pattern:** Clean separation with ViewModels and Repository pattern
- **Dependency Injection:** Hilt for clean dependency management
- **Jetpack Compose:** Modern UI toolkit with TV Material3 components
- **Coroutines & Flow:** Reactive programming for smooth UI updates
