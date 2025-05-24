# Social Feed App - Project Structure

## 🏗️ Architecture Overview

This project implements **MVVM (Model-View-ViewModel)** with **Clean Architecture** principles, using **Jetpack Compose** and **Firebase** for a scalable social media application.

## 📁 Project Structure

```
app/src/main/java/com/kashi/democalai/
├── data/
│   ├── model/
│   │   └── Post.kt                    # Firestore data model with Timestamp support
│   └── repository/
│       ├── AuthRepository.kt          # Firebase Auth operations & state management
│       └── PostsRepository.kt         # Firestore CRUD, real-time listeners & pagination
├── presentation/
│   ├── screen/
│   │   ├── LoginScreen.kt            # Google Sign-In UI with error handling
│   │   └── HomeScreen.kt             # Main feed with post creation, filtering & animations
│   └── viewmodel/
│       ├── AuthViewModel.kt          # Auth state management with reactive flows
│       └── HomeViewModel.kt          # Feed state, post creation & analytics tracking
├── ui/theme/                         # Material Design 3 theming & colors
├── utils/
│   └── AnalyticsHelper.kt            # Post view tracking and analytics
├── MainActivity.kt                   # Navigation setup with Hilt injection
└── SocialFeedApplication.kt          # Application class with @HiltAndroidApp
```
