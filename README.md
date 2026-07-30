<div align="center">

<!-- Logo / Banner -->
<img src="https://img.shields.io/badge/Code--ioPRO-AI%20Assistant-00D4FF?style=for-the-badge&logo=android&logoColor=white" alt="Code-ioPRO" height="60"/>

# 🧠 Code-ioPRO

**Gelişmiş AI Kod Asistanı · Advanced AI Code Assistant**

[![Build APK](https://github.com/ThT0AltayHR/Code-ioPRO/actions/workflows/main.yml/badge.svg)](https://github.com/ThT0AltayHR/Code-ioPRO/actions/workflows/main.yml)
[![License](https://img.shields.io/github/license/ThT0AltayHR/Code-ioPRO?color=00D4FF&style=flat-square)](LICENSE)
[![Version](https://img.shields.io/badge/version-1.0.0-00FF88?style=flat-square)](https://github.com/ThT0AltayHR/Code-ioPRO/releases)
[![Android](https://img.shields.io/badge/Android-12%2B-3DDC84?style=flat-square&logo=android)](https://android.com)
[![Java](https://img.shields.io/badge/Java-11-ED8B00?style=flat-square&logo=openjdk)](https://java.com)
[![Stars](https://img.shields.io/github/stars/ThT0AltayHR/Code-ioPRO?color=FFD740&style=flat-square)](https://github.com/ThT0AltayHR/Code-ioPRO/stargazers)
[![Forks](https://img.shields.io/github/forks/ThT0AltayHR/Code-ioPRO?color=7C4DFF&style=flat-square)](https://github.com/ThT0AltayHR/Code-ioPRO/network)
[![Issues](https://img.shields.io/github/issues/ThT0AltayHR/Code-ioPRO?color=FF4B4B&style=flat-square)](https://github.com/ThT0AltayHR/Code-ioPRO/issues)
[![AI Models](https://img.shields.io/badge/AI%20Models-10%2B-CC44FF?style=flat-square)](https://github.com/ThT0AltayHR/Code-ioPRO)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen?style=flat-square)](CONTRIBUTING.md)

---

**🇹🇷 Türkçe** · [🇬🇧 English](#english)

Muhammed tarafından geliştirilen **Code-ioPRO**, Android için tam özellikli bir yapay zeka kod asistanıdır. Birden fazla AI sağlayıcısını destekler, gerçek bir terminal ortamı sunar, kod editörü içerir ve güvenli API yönetimi sağlar.

</div>

---

## 🇹🇷 Hızlı Bakış

**Code-ioPRO**, cebinizdeki tam teşekküllü bir geliştirici asistanıdır. OpenAI GPT-4o'dan Anthropic Claude'a, Groq'tan DeepSeek'e kadar 10+ yapay zeka modeli ile çalışır. Gerçek terminal, kod editörü, dosya yöneticisi ve güvenli SECRETS deposu ile Replit'i andıran bir deneyim sunar.

### ✨ Temel Özellikler

| Özellik | Açıklama |
|---------|----------|
| 🤖 **10+ AI Model** | GPT-4o, Claude 3.5, Gemini, Groq, Mistral, DeepSeek ve daha fazlası |
| 💻 **Kod Editörü** | Syntax vurgu, satır numaraları, klavye araç çubuğu |
| 🖥️ **Gerçek Terminal** | Python, Node.js, pip, npm, git, bash komutları |
| 📁 **Dosya Yöneticisi** | Tam klasör ağacı, oluştur/sil/düzenle/paylaş |
| 🔐 **SECRETS** | Biyometrik kilitli güvenli API anahtarı yönetimi |
| 🛒 **AI Marketi** | 10+ sağlayıcı, filtrele, API key ekle, model seç |
| ⬇️ **Dosya İndirme** | Her kod bloğu indirilebilir, gerçek indirme |
| 🌟 **Yıldız Arka Plan** | Chat'te hareketli yıldız animasyonu |
| 🎙️ **Sesli Asistan** | Sistem asistanı olarak kullanım |

---

## 🇬🇧 English {#english}

**Code-ioPRO** is a full-featured AI coding assistant for Android, developed by Muhammed. It supports 10+ AI providers, includes a real terminal, code editor, file manager, and secure API key vault.

### ✨ Key Features

- 🤖 **Multi-AI Support** — GPT-4o, Claude 3.5 Sonnet/Haiku, Gemini 1.5, Groq (free!), Mistral, DeepSeek R1, Perplexity, OpenRouter, Together AI, and Custom APIs
- 💻 **Code Editor** — Syntax highlighting, line numbers, keyboard toolbar, save/run/share
- 🖥️ **Real Terminal** — Execute Python, Node.js, bash, pip, npm, git, and any shell command
- 📁 **File Manager** — Full directory tree, create/delete/rename/edit/share files
- 🔐 **SECRETS Vault** — Biometric-locked secure storage for API keys
- 🛒 **AI Marketplace** — Browse all providers, add API keys, switch models with one tap
- ⬇️ **Real File Downloads** — Every code block can be saved to device storage
- ⬆️ **Blob/Data URL Downloads** — Handles all download types including blobs
- 🌟 **Star Background** — Animated starfield in chat empty state
- ▶️ **Run Code in Shell** — One-tap code execution from any code block
- 📋 **Copy Buttons** — Every code block has a copy button
- 🎙️ **Voice Assistant** — Use as system voice assistant
- 🔄 **Auto Updates** — GitHub Releases update checking
- 👆 **Haptic Feedback** — Satisfying vibration on tab switches

---

## 📦 Installation / Kurulum

### Download APK / APK İndir

➡️ **[Releases sayfasından APK indir](https://github.com/ThT0AltayHR/Code-ioPRO/releases)**

### Build from Source / Kaynak Koddan Derle

```bash
# Clone the repository
git clone https://github.com/ThT0AltayHR/Code-ioPRO.git
cd Code-ioPRO

# Build debug APK
./gradlew assembleDebug

# APK is at: app/build/outputs/apk/debug/app-debug.apk
```

#### Requirements / Gereksinimler
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- Android SDK 35
- Min Android 12 (API 31)

---

## 🏗️ Architecture / Mimari

```
Code-ioPRO/
├── app/src/main/
│   ├── java/com/codeioPRO/app/
│   │   ├── MainActivity.java          # 5-tab navigation
│   │   ├── ChatFragment.java          # AI chat (WebView)
│   │   ├── FilesFragment.java         # File manager
│   │   ├── ShellFragment.java         # Terminal emulator
│   │   ├── AiMarketFragment.java      # AI marketplace
│   │   ├── SecretsFragment.java       # Secure key vault
│   │   ├── EditorActivity.java        # Code editor
│   │   ├── ChatActivity.java          # Bottom drawer
│   │   ├── AskActivity.java           # Share intent handler
│   │   ├── SettingsBridge.java        # Settings bridge
│   │   └── util/
│   │       ├── SecretsManager.java    # Encrypted storage
│   │       └── UpdateChecker.java     # GitHub update check
│   ├── assets/
│   │   ├── terminal.html              # Terminal UI (ANSI support)
│   │   ├── secrets.html               # Secrets vault UI
│   │   ├── settings.html              # Settings panel UI
│   │   ├── ai_market.html             # AI marketplace UI
│   │   └── editor.html                # Code editor UI
│   └── res/                           # Resources (layouts, drawables, colors)
```

---

## 🤖 Supported AI Providers / Desteklenen AI Sağlayıcılar

| Provider | Models | Free Tier | API Key Required |
|----------|--------|-----------|-----------------|
| **OpenAI** | GPT-4o, GPT-4o Mini, o1 | ❌ | ✅ |
| **Anthropic** | Claude 3.5 Sonnet, Haiku, Opus | ❌ | ✅ |
| **Google Gemini** | Gemini 1.5 Pro, Flash | ❌ | ✅ |
| **Groq** ⚡ | Llama 3.3 70B, Mixtral | ✅ Free | ✅ |
| **Mistral** | Mistral Large, Codestral | ❌ | ✅ |
| **DeepSeek** | R1, V3 | ❌ | ✅ |
| **Perplexity** | Sonar Online | ❌ | ✅ |
| **OpenRouter** | 200+ models | ✅ Free models | ✅ |
| **Together AI** | Llama, Qwen, Falcon | ✅ Free tier | ✅ |
| **Cohere** | Command R+ | ✅ Free tier | ✅ |
| **Custom API** | Any OpenAI-compatible | ➖ | Optional |

---

## 🖥️ Shell Commands / Terminal Komutları

```bash
# Python
python3 script.py
pip install requests numpy pandas

# Node.js
node app.js
npm install express
npx create-react-app myapp

# Git
git clone https://github.com/user/repo
git add . && git commit -m "feat: new feature"
git push origin main

# File operations
ls -la
cat file.txt
mkdir project && cd project
grep -r "pattern" .

# And everything else standard shell supports!
```

---

## 🔐 Security / Güvenlik

- **Biometric Lock** on SECRETS tab (fingerprint / PIN)
- **Encrypted Storage** — API keys stored with Android Keystore
- **No Data Leaks** — Keys never logged or transmitted
- **Anti-Jailbreak** — System prompt injection prevention
- **Privacy First** — No telemetry, no analytics

---

## 📋 Permissions / İzinler

| Permission | Reason |
|-----------|--------|
| INTERNET | AI API calls, web browsing |
| CAMERA | Share images to AI |
| RECORD_AUDIO | Voice assistant |
| READ/WRITE_MEDIA | File downloads & uploads |
| USE_BIOMETRIC | SECRETS vault lock |
| VIBRATE | Haptic feedback |
| POST_NOTIFICATIONS | Update alerts |

---

## 🛣️ Roadmap / Yol Haritası

- [ ] 🎨 Custom themes (light mode improvements)
- [ ] 🔌 Plugin system for custom tools
- [ ] 📊 Token usage analytics dashboard
- [ ] 🌍 Multi-language support (EN/TR/DE/ZH)
- [ ] 🔄 Cloud sync for settings
- [ ] 🤝 Multi-agent sub-agent orchestration UI
- [ ] 📱 Tablet/landscape optimizations
- [ ] 🔥 Streaming responses (real-time typing)

---

## 🤝 Contributing / Katkı

Pull requests are welcome! For major changes, please open an issue first.

```bash
# Fork & clone
git fork https://github.com/ThT0AltayHR/Code-ioPRO
git checkout -b feat/amazing-feature
git commit -m 'feat: add amazing feature'
git push origin feat/amazing-feature
# Open a Pull Request
```

---

## 👨‍💻 Developer / Geliştirici

**Muhammed** — Code-ioPRO'nun tek geliştiricisi

[![GitHub](https://img.shields.io/badge/GitHub-ThT0AltayHR-181717?style=flat-square&logo=github)](https://github.com/ThT0AltayHR)

---

## 📄 License / Lisans

This project is licensed under the GPL-3.0 License — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Made with ❤️ by Muhammed**

⭐ Bu projeyi beğendiyseniz star vermeyi unutmayın! / Star this repo if you find it useful!

[![Star History Chart](https://api.star-history.com/svg?repos=ThT0AltayHR/Code-ioPRO&type=Date)](https://star-history.com/#ThT0AltayHR/Code-ioPRO&Date)

</div>
