# Drip - AI-Powered Android App

A modern Android application built with Kotlin that leverages Google's Gemini AI to deliver intelligent features and seamless user experiences.

## 📱 About

Drip is a demonstration project showcasing mobile development expertise with integration of cutting-edge AI capabilities. The app demonstrates best practices in Android development, API integration, and modern architecture patterns.

## 🎯 Features

- **AI-Powered Functionality**: Integrated with Google Gemini API for intelligent capabilities
- **Modern Android Stack**: Built with Kotlin and modern Android development practices
- **Clean Architecture**: Well-structured codebase following Android best practices
- **Environment Configuration**: Secure API key management via `.env` file

## 🚀 Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (Latest version recommended)
- Android SDK 21+
- Gradle 7.0+

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/Hexa08/Drip.git
   cd Drip
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select **File** → **Open** and choose the project directory
   - Allow Android Studio to sync and resolve any dependencies

3. **Configure API Key**
   - Create a `.env` file in the project root directory
   - Add your Gemini API key:
     ```
     GEMINI_API_KEY=your_api_key_here
     ```
   - See `.env.example` for reference

4. **Build Configuration**
   - Remove the following line from `app/build.gradle.kts`:
     ```kotlin
     signingConfig = signingConfigs.getByName("debugConfig")
     ```

5. **Run the App**
   - Connect an Android device or start an emulator
   - Click **Run** (or press Shift + F10)
   - Select your target device

## 📦 Build Release APK

To build a release APK for distribution:

```bash
./gradlew assembleRelease
```

The APK will be generated in: `app/build/outputs/apk/release/`

## 🛠 Tech Stack

- **Language**: Kotlin
- **AI Integration**: Google Gemini API
- **Architecture**: MVVM (recommended pattern)
- **Build Tool**: Gradle

## 📋 Project Structure

```
Drip/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/    # Kotlin source files
│   │   │   └── res/     # Android resources
│   │   └── androidTest/ # Instrumented tests
│   └── build.gradle.kts
├── .env.example
├── build.gradle.kts
└── README.md
```

## 📝 License

This project is provided as a portfolio demonstration.

## 🔗 Links

- [View on AI Studio](https://ai.studio/apps/9d28700f-4e65-470a-b957-ef7aa259b6bf)
- [Android Developer Documentation](https://developer.android.com)
- [Google Gemini API](https://ai.google.dev)

## 👨‍💻 Author

**Hexa08** - Android Developer

---

**Note**: This is a portfolio project demonstrating Android development capabilities and AI integration.
