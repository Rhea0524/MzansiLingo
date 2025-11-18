# 🌍 Mzansi Lingo

Mzansi Lingo is an Android-based language learning app designed to help South Africans improve their *language vocabulary, phrases, and pronunciation through interactive lessons, daily goals, AI chat support, and progress tracking.

---

## 📑 Table of Contents

- [Overview](#overview)  
- [Technologies Used](#technologies-used)  
- [Latest Features (2025 Update)](#latest-features-2025-update)  
- [System Functionalities & User Roles](#system-functionalities--user-roles)  
- [Setup Instructions](#setup-instructions)  
- [Architecture](#architecture)  
- [Screenshots & YouTube Demo](#screenshots--youtube-demo)  
- [Code Attribution](#code-attribution)  
- [Contact](#contact)  

---

## Overview

**Mzansi Lingo** provides an immersive platform for learning **Afrikaans**. Users can learn words, phrases, and quotes, track progress, and practice pronunciation.  

Key features include:

- **Daily Goals** – Complete daily word and phrase goals with gamified pop-ups for motivation.  
- **Audio-Based Learning** – Hear correct pronunciation for words, phrases, and quotes.  
- **AI Chat Support** – Ask a chatbot how to say or pronounce words and phrases.  
- **Progress Tracking** – Earn points, monitor learning progress, and share progress screenshots.  
- **Leaderboards** – Compare progress with other learners.  
- **Custom Goals** – Set personalized daily targets for vocabulary learning.  

The app emphasizes **consistency, pronunciation accuracy, and interactive learning** for individual learners.

---

## Technologies Used

- **Kotlin** – Main app development language  
- **Android Studio** – IDE  
- **XML** – UI layout design  
- **REST API (Railway)** – AI chatbot integration  
- **Retrofit** – API communication  
- **BiometricPrompt API** – Biometric authentication  
- **Firebase Cloud Messaging (FCM)** – Real-time notifications  
- **Room Database / Local Caching** – Offline mode support  
- **Kotlin Coroutines** – Asynchronous tasks  
- **Git & GitHub** – Version control  

---

## Latest Features (2025 Update)

- Biometric login and authentication  
- Offline mode with local caching  
- Real-time notifications via FCM  
- Enhanced AI chatbot interactions  
- Leaderboards and gamification improvements  
- Multi-language support  
- Progress screenshot capture feature  

---

## System Functionalities & User Roles

### Learners Can:

- Study words, phrases, and quotes  
- Listen to audio pronunciation  
- Track progress, points, and streaks  
- Complete daily goals  
- Use AI chatbot for translations and pronunciation  
- Capture progress screenshots  
- Enable biometric login  
- Operate offline  
- Select preferred language  
- View leaderboards  

### Core App Features:

- Gamification (points, pop-ups, progress rewards)  
- Audio-based pronunciation  
- Real-time notifications  
- Offline access to saved content  
- Biometric login  
- Multi-language interface  
- AI chatbot for translations and pronunciation  
- Leaderboards and progress tracking  

---

## Setup Instructions

### Prerequisites

- [Android Studio](https://developer.android.com/studio)  
- Java Development Kit (JDK) 11+  
- Git  

### Installation

1. **Clone the Repository**  
```bash
git clone https://github.com/Rhea0524/MzansiLingo.git

## Setup Instructions

### Open in Android Studio
1. Launch Android Studio  
2. Select **Open an existing project** and choose the cloned folder  

### Configure API Key
- Add your AI chatbot API key in `ApiKeyManager.kt` (or use an environment variable)  

### Run the Application
1. Connect an Android device or emulator  
2. Click **Run** (green play button) in Android Studio  

---

## Architecture

### App Structure

| Component                | Description                                      |
|--------------------------|--------------------------------------------------|
| HomeActivity             | Displays daily goals, progress, and navigation  |
| WordsActivity            | List of Afrikaans words                          |
| PhrasesActivity          | List of Afrikaans phrases                        |
| AiChatActivity           | AI chatbot interactions                          |
| LeaderboardActivity      | User rankings                                   |
| SettingsActivity         | Language selection, notifications, preferences  |
| ProfileActivity          | Displays progress and user details              |
| BiometricAuthActivity    | Manages biometric authentication                |
| OfflineManager           | Local content caching                            |
| NotificationService      | Sends reminders and updates                     |

### Data Flow
- User actions update the **Room database** and UI in real time  
- AI chatbot communication via **Retrofit** to REST API  
- Offline content stored locally, syncing when online  
- Notifications sent through **Firebase Cloud Messaging**  
- **BiometricPrompt** handles secure user authentication  

---

## Screenshots & YouTube Demo

### Home Screen
![Home Screen](Images/home.png)

### AI Chat
![AI Chat](Images/ai_chat.png)

### Leaderboard
![Leaderboard](Images/leaderboard.png)

**Full Demo**: [Watch on YouTube](https://youtu.be/your-demo-link)  

---

## Code Attribution / References

- [Android Developer Documentation](https://developer.android.com/docs)  
- [Kotlin Language Reference](https://kotlinlang.org/docs/reference/)  
- [Material Design Guidelines](https://material.io/design)  
- [Retrofit Library](https://square.github.io/retrofit/)  
- [Firebase Documentation](https://firebase.google.com/docs)  
- [OpenAI / AI Chatbot Integration](https://platform.openai.com/docs)  
- [RecyclerView Guide](https://developer.android.com/guide/topics/ui/layout/recyclerview)  
- [Glide Image Loading Library](https://github.com/bumptech/glide)  
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-overview.html)  
- Stack Overflow and YouTube tutorials for Android development  

---

## Contact

For questions, feedback, or collaboration:  

**Mzansi Lingo**  
📧 Email: [mzansilingo@gmail.com](mailto:mzansilingo@gmail.com)  ration opportunities, you can reach out to:

**Mzansi Lingo**  
Email: [mzansilingo@gmail.com](mailto:mzansilingo@gmail.com)  

