# 🎓 Portal Monitoria

**Portal Monitoria** is a comprehensive academic monitoring platform designed to bridge the gap between students and monitors. The system streamlines the process of finding academic support, managing monitoring schedules, and facilitating real-time communication through forums and instant messaging.

## 🚀 Features

### 👥 Role-Based Access Control
- **Student**: Search for monitors, post questions in forums, schedule meetings, and chat with monitors.
- **Monitor**: Manage available time slots, answer forum questions, and provide real-time support to students.
- **Admin**: Oversee the entire system, manage users, disciplines, and monitor assignments.

### 📚 Academic Support
- **Disciplines Mapping**: Organized structure of courses and their respective monitoring teams.
- **Monitoring Schedule**: Interactive management of availability slots to avoid scheduling conflicts.

### 💬 Communication Suite
- **Forum**: A structured Q&A system for academic doubts, allowing for asynchronous knowledge sharing.
- **Real-time Chat**: Instant messaging powered by WebSockets for immediate resolution of queries.

---

## 🛠 Technologies

### Backend
- **Language**: Java 17+
- **Framework**: Spring Boot
- **Data Access**: Spring Data JPA
- **Build Tool**: Maven
- **Communication**: Spring WebSocket (STOMP/SockJS)
- **Database**: Relational Database (H2/PostgreSQL/MySQL)

### Frontend
- **Library**: React
- **Language**: TypeScript
- **Build Tool**: Vite
- **Styling**: CSS / Modern CSS modules

---

## 📂 Project Structure

```text
portal-monitoria-new/
├── backend/                 # Spring Boot Application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/unifor/monitoria/
│   │   │   │   ├── config/     # WebSocket & Security configurations
│   │   │   │   ├── controller/  # REST Endpoints
│   │   │   │   ├── model/       # JPA Entities
│   │   │   │   ├── repository/   # Spring Data Interfaces
│   │   │   │   └── service/     # Business Logic
│   │   │   └── resources/     # application.properties & static assets
│   └── pom.xml              # Maven dependencies
└── frontend_new/            # React Application
    ├── src/
    │   ├── pages/            # Page-level components (Auth, Forum, Chat)
    │   ├── services/         # API integration (Axios/WebSocket)
    │   └── styles/           # CSS stylesheets
    ├── package.json          # NPM dependencies
    └── vite.config.js        # Vite configuration
```

---

## ⚙️ Installation Guide

### Backend
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Configure your database settings in `src/main/resources/application.properties`.
3. Build and run the application:
   ```bash
   mvn spring-boot:run
   ```

### Frontend
1. Navigate to the frontend directory:
   ```bash
   cd frontend_new
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```

---

## 🗄 Database Schema

### Main Entities & Relationships
- **User**: Stores credentials and roles (`ADMIN`, `MONITOR`, `STUDENT`).
- **Discipline**: Represents a course (e.g., "Calculus I").
- **Matricula**: Links students to disciplines.
- **Duvida**: Contains the question, author (`User`), and linked `Discipline`.
- **RespostaDuvida**: Links to a `Duvida` and an author (`User`).
- **Schedule**: Represents a specific date/time window assigned to a `Monitor`.
- **Conversation/Message**: Stores history of communication between users.

**Relationships:**
- `User` ↔ `Matricula` (One-to-Many)
- `Discipline` ↔ `Matricula` (One-to-Many)
- `Discipline` ↔ `Duvida` (One-to-Many)
- `Duvida` ↔ `RespostaDuvida` (One-to-Many)

---

## 🔌 API Overview

### Auth Controller
- `POST /api/auth/login` - Authenticates user and returns JWT/Session.
- `POST /api/auth/register` - Creates a new user account.

### Forum Controller
- `GET /api/forum/posts` - Lists all academic questions.
- `POST /api/forum/posts` - Creates a new question.
- `POST /api/forum/answers/{postId}` - Adds a response to a post.

### Schedule Controller
- `GET /api/schedule/{monitorId}` - Retrieves availability for a specific monitor.
- `POST /api/schedule/book` - Requests a monitoring slot.

### Discipline Controller
- `GET /api/disciplines` - Lists all available courses with monitoring.
- `GET /api/disciplines/{id}/monitors` - Lists monitors for a specific course.

### Chat (WebSocket)
- `/ws` - WebSocket endpoint for real-time messaging.
- `/app/chat.sendMessage` - Send a message to a specific user or room.
- `/topic/messages` - Subscription point for receiving messages.
