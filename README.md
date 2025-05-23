# Social Feed App - Project Structure

## 🏗️ Architecture Overview

This project implements **MVVM (Model-View-ViewModel)** with **Clean Architecture** principles, using **Jetpack Compose** and **Firebase** for a scalable social media application.

## 📁 Project Structure

```
app/src/main/java/com/kashi/democalai/
├── data/
│   ├── model/
│   │   └── Post.kt                    # Firestore data model with annotations
│   └── repository/
│       ├── AuthRepository.kt          # Authentication operations & state management
│       └── PostsRepository.kt         # Firestore CRUD & real-time listeners
├── presentation/
│   ├── screen/
│   │   ├── LoginScreen.kt            # Google Sign-In UI with state handling
│   │   └── HomeScreen.kt             # Main feed with post creation & display
│   └── viewmodel/
│       ├── AuthViewModel.kt          # Auth state management with StateFlow
│       └── HomeViewModel.kt          # Home screen state & business logic
├── ui/theme/                         # Material Design 3 theming
├── MainActivity.kt                   # Navigation setup with Hilt injection
└── SocialFeedApplication.kt          # Application class with @HiltAndroidApp
```
