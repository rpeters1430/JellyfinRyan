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

## Future Improvements / Known Issues

*   (To be added)
