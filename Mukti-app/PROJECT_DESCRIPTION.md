# MUKTI APP - Project Overview & Documentation

## 🌟 Vision

**Mukti App** is a community-driven platform designed to eliminate food waste and fight hunger. It bridges the gap between those with surplus food (donors) and those experiencing food insecurity (receivers), creating a sustainable ecosystem of sharing and mutual support.

---

## 🎨 Modern Design System

The application features a cutting-edge **Glassmorphism Design System** that provides a premium, "living" user interface.

### Key Visual Language

* **Aesthetic**: High-blur surfaces, subtle gradients, and glowing ambient elements.
* **Typography**: Optimized for clarity using the **Outfit** and **Inter** font family, with **Hind Siliguri** for native language branding.
* **Therapeutic UI**: A custom **Mouse-following Ambient Light Effect** (250px radius core) tracks user movement across the entire platform, creating a soothing and responsive environment.
* **Modern UX Patterns**: Includes dynamic entrance animations, live stats incrementing, and a context-aware navigation bar that adapts to vertical scrolling.

---

## 🛠️ Core Features

### 1. Advanced Donation Management (`/food/donate`)

Donors can easily list surplus food by providing:

* **Real-time Details**: Food name, quantity (servings), and location.
* **Freshness Tracking**: Specific "Cook Time" and "Expiry Time" to ensure food safety.
* **Quick-Tags**: Pre-defined category badges (Biryani, Rice, Curry, etc.) for rapid listing.

### 2. Intelligent Discovery (`/food/receive-page`)

Receivers can find support through:

* **Localized Search**: A real-time filtering system to find donations nearby.
* **Safety Transparency**: View donor profiles, cook times, and expire times before requesting.
* **One-Click Receipt**: Simplified request process with immediate feedback and instructions.

### 3. Integrated Real-time Chat (`/chat`)

A seamless peer-to-peer messaging system to coordinate food collection:

* **Unified Inbox**: Manage all communication between donors and receivers in one place.
* **Secure Exchange**: Direct coordination for pickup locations and timing without exposing sensitive data.

### 4. Impact Tracking & Leaderboard (`/food/leaderboard`)

Gamifying community service to encourage participation:

* **Donor Hall of Fame**: A visual leaderboard celebrating top contributors.
* **Personal Dashboard**: Track individual metrics like "Total Donations Saved," "People Fed," and "Lives Impacted."
* **History Logs**: Detailed record of all past activities with status tracking (Available, Processing, Received, Cancelled).

---

## 🏗️ Technical Architecture

* **Backend**: Spring Boot (Java) - providing a robust, enterprise-grade foundation.
* **Frontend**: Thymeleaf with Modern Vanilla JS/CSS (Atomic CSS tokens).
* **Security**: Integrated User Authentication with Role-based Access (Donor/Receiver).
* **Design Library**: FontAwesome for iconography, Google Fonts for dynamic typography.

### Database Configuration

* **JDBC URL**: `jdbc:h2:file:./data/mukti-db;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE`
* **Console**: `http://localhost:8080/h2-console`
* **Credentials**: User: `sa`, Password: *(blank)*

---

## 🚀 Impact Metrics

* **2.5+ Tons** of food saved from landfills.
* **8,500+** meals shared with the community.
* **425+** verified active donors.

---

## 💡 How it Works

1. **Register**: Create a profile as a donor or receiver in under 60 seconds.
2. **Post**: Donors upload details of surplus food with photos and freshness windows.
3. **Connect**: Receivers request items and connect instantly via the built-in chat.
4. **Exchange**: Safe pick-up coordination and shared meals.
5. **Impact**: Points are awarded to donors, contributing to the Hall of Fame.

---

*© 2026 MUKTI APP. Crafted for a Zero-Waste, Zero-Hunger Community.*
