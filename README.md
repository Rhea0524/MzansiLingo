🌍 Mzansi Lingo
Mzansi Lingo is an Android-based language learning app designed to help South Africans improve their language vocabulary, phrases, and pronunciation through interactive lessons, daily goals, AI chat support, and progress tracking.
GitHub link: https://github.com/Rhea0524/MzansiLingo 
Full Demo: https://youtu.be/85hXIaZRtzs 


📑 Table of Contents
•	Overview
•	Technologies Used
•	Latest Features (2025 Update)
•	System Functionalities & User Roles
•	Installation & Setup Instructions
•	Architecture
•	Screenshots & YouTube Demo
•	Code Attribution
•	Application Screenshots
•	Contact

Overview
Mzansi Lingo provides an immersive platform for learning Afrikaans. Users can learn words, phrases, and quotes, track progress, and practice pronunciation.
Key features: 
- Daily Goals: Complete daily word and phrase goals with gamified pop-ups for motivation. 
- Audio-Based Learning: Hear correct pronunciation for words, phrases, and quotes. 
- AI Chat Support: Ask a chatbot how to say or pronounce words and phrases. 
- Progress Tracking: Earn points, monitor learning progress, and share progress screenshots.
 - Leaderboards: Compare progress with other learners.
 - Custom Goals: Set personalized daily targets for vocabulary learning.
The app emphasizes consistency, pronunciation accuracy, and interactive learning for individual learners.

Technologies Used
•	Kotlin – Main app development language
•	Android Studio – IDE
•	XML – UI layout design
•	REST API (Railway) – AI chatbot integration
•	Retrofit – API communication
•	BiometricPrompt API – Biometric authentication
•	Firebase Cloud Messaging (FCM) – Real-time notifications
•	Room Database / Local Caching – Offline mode support
•	Kotlin Coroutines – Asynchronous tasks
•	Git & GitHub – Version control

Latest Features 
•	Biometric login and authentication
•	Offline mode with local caching
•	Real-time notifications via FCM
•	Enhanced AI chatbot interactions
•	Leaderboards and gamification improvements
•	Multi-language support
•	Progress screenshot capture feature

System Functionalities & User Roles
Learners Can: - Study words, phrases, and quotes - Listen to audio pronunciation - Track progress, points, and streaks - Complete daily goals - Use AI chatbot for translations and pronunciation - Capture progress screenshots - Enable biometric login - Operate offline - Select preferred language - View leaderboards
Core App Features: - Gamification (points, pop-ups, progress rewards) - Audio-based pronunciation - Real-time notifications - Offline access to saved content - Biometric login - Multi-language interface - AI chatbot for translations and pronunciation - Leaderboards and progress tracking

Installation & Setup Instructions
Prerequisites
•	Android Studio – Recommended version: Arctic Fox or later
•	Java Development Kit (JDK) 11+
•	Git
•	Android device or emulator (API level 26+)
Steps to Install and Run the App
1.	Clone the Repository
git clone https://github.com/Rhea0524/MzansiLingo.git
2.	Open the Project in Android Studio
•	Launch Android Studio
•	Select Open an existing project
•	Navigate to the cloned MzansiLingo folder and click Open
•	Wait for Gradle to build and sync the project
3.	Configure API Key
•	Open app/src/main/java/.../ApiKeyManager.kt
•	Replace the placeholder with your AI API key:

object ApiKeyManager {
    const val AI_API_KEY = "YOUR_API_KEY_HERE"
}
•	Optionally, use an environment variable or local.properties for better security
4.	Run the Application
•	Connect an Android device or launch an emulator
•	Click Run (green play button) in Android Studio
•	The app should start on your device/emulator
5.	Optional: Build APK
•	Go to Build > Build Bundle(s) / APK(s) > Build APK(s)
•	Generated APK is in app/build/outputs/apk/
Troubleshooting Tips: - Gradle sync errors: Ensure Android Studio and Gradle versions are compatible - API Key issues: Check API key validity and internet connection - Emulator issues: Use API level 26+ with Google Play Services

Architecture
App Structure: | Component | Description | |———–|————-| | HomeActivity | Displays daily goals, progress, and navigation | | WordsActivity | List of Afrikaans words | | PhrasesActivity | List of Afrikaans phrases | | AiChatActivity | AI chatbot interactions | | LeaderboardActivity | User rankings | | SettingsActivity | Language selection, notifications, preferences | | ProfileActivity | Displays progress and user details | | BiometricAuthActivity | Manages biometric authentication | | OfflineManager | Local content caching | | NotificationService | Sends reminders and updates |
Data Flow: - User actions update the Room database and UI in real time - AI chatbot communication via Retrofit to REST API - Offline content stored locally, syncing when online - Notifications sent through Firebase Cloud Messaging - BiometricPrompt handles secure user authentication

Code Attribution / References
•	Android Developer Documentation
•	Kotlin Language Reference
•	Material Design Guidelines
•	Retrofit Library
•	Firebase Documentation
•	OpenAI / AI Chatbot Integration
•	RecyclerView Guide
•	Glide Image Loading Library
•	Kotlin Coroutines Guide
•	Stack Overflow and YouTube tutorials for Android development

## Application Screenshots

### Login & Authentication
![Login Screen](./images/image1.png)
![Biometric Authentication](./images/image2.png)

### Offline Quiz Feature
![Offline Quiz](./images/image3.png)
![Offline Quiz 2](./images/image4.png)

### Multi-Language Support
![Multi-Language Feature](./images/image5.png)

### AI Chatbot
![AI Chatbot](./images/image6.png)

### Leaderboard
![Leaderboard](./images/image7.png)


Contact
For questions, feedback, or collaboration:
Mzansi Lingo Email: mzansilingo@gmail.com

