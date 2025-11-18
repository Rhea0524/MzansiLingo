# 🌍 Mzansi Lingo

**Mzansi Lingo** is an Android-based language learning app designed to help **South Africans** improve their **Afrikaans vocabulary, phrases, and pronunciation** through interactive lessons, daily goals, AI chat support, and progress tracking.

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
Open in Android Studio

Launch Android Studio

Select Open an existing project and choose the cloned folder

Configure API Key

Add your AI chatbot API key in ApiKeyManager.kt (or use an environment variable)

Run the Application

Connect an Android device or emulator

Click Run (green play button) in Android Studio

Architecture
App Structure
Component	Description
HomeActivity	Displays daily goals, progress, and navigation
WordsActivity	List of Afrikaans words
PhrasesActivity	List of Afrikaans phrases
AiChatActivity	AI chatbot interactions
LeaderboardActivity	User rankings
SettingsActivity	Language selection, notifications, preferences
ProfileActivity	Displays progress and user details
BiometricAuthActivity	Manages biometric authentication
OfflineManager	Local content caching
NotificationService	Sends reminders and updates

Data Flow
User actions update the Room database and UI in real time

AI chatbot communication via Retrofit to REST API

Offline content stored locally, syncing when online

Notifications sent through Firebase Cloud Messaging

BiometricPrompt handles secure user authentication

Screenshots & YouTube Demo
Home Screen


AI Chat


Leaderboard


Full Demo: Watch on YouTube

Code Attribution / References
Android Developer Documentation

Kotlin Language Reference

Material Design Guidelines

Retrofit Library

Firebase Documentation

OpenAI / AI Chatbot Integration

RecyclerView Guide

Glide Image Loading Library

Kotlin Coroutines Guide

Stack Overflow and YouTube tutorials for Android development

Contact
For questions, feedback, or collaboration:

Mzansi Lingo
📧 Email: mzansilingo@gmail.com

yaml
Copy code
