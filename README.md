# 📚 EduTrack - Smart Student Management System

<div align="center">

![EduTrack Logo](app/src/main/res/drawable/logo.png)

**A comprehensive Android application for managing academic life**

[![Android](https://img.shields.io/badge/Android-7.0%2B-green.svg)](https://www.android.com/)
[![Firebase](https://img.shields.io/badge/Firebase-Realtime%20DB-orange.svg)](https://firebase.google.com/)
[![Material Design](https://img.shields.io/badge/Material%20Design-3-blue.svg)](https://m3.material.io/)

</div>

---

## 🎯 Overview

EduTrack is a modern Android application designed to help students manage their academic journey efficiently. Built with **Material Design 3** principles, the app offers a beautiful and intuitive interface while providing powerful features for attendance tracking, task management, and focus sessions.

---

## ✨ Key Features

### 📊 **Attendance Management**
- **Real-time Attendance Tracking**: Mark attendance for each subject with Present/Absent
- **Overall Percentage Calculation**: Automatic calculation with color-coded status:
  - 🟢 Green: ≥75% (Safe)
  - 🟡 Yellow: 65-74% (Warning)
  - 🔴 Red: <65% (Critical)
- **75% Prediction Algorithm**: Smart calculations showing:
  - How many classes you can skip while maintaining 75%
  - How many classes you need to attend to reach 75%
- **Subject-wise Statistics**: Individual attendance percentage for each subject
- **Visual Progress Indicators**: Easy-to-understand attendance status

### ✅ **Task Manager**
- **Comprehensive Task Creation**: Title, description, due date/time, and priority levels (Low, Medium, High)
- **Smart Reminders**: 
  - 🔔 Alarm notification **5 minutes before** due time
  - 📧 Email reminder **1 minute after** due time (if incomplete)
- **Task Organization**:
  - Mark tasks as complete with checkbox
  - Auto-hide completed tasks (with toggle to show/hide)
  - Task details dialog on click
  - Delete confirmation for safety
- **Statistics Dashboard**: Real-time counters showing "X pending • Y completed"
- **Color-coded Priorities**: Visual distinction for Low/Medium/High priority tasks

### 🎯 **Focus Mode (Pomodoro Timer)**
- **Classic Pomodoro Technique**:
  - 🔴 25-minute focus sessions
  - ☕ 5-minute short breaks
  - 🌴 15-minute long breaks (after 4 pomodoros)
- **Visual Timer**: Circular progress indicator with countdown
- **Session Tracking**: Automatically saves all focus sessions to database
- **Task Integration**: Link focus sessions to specific tasks for better tracking
- **Statistics**:
  - Today's total focus time
  - All-time focus hours
  - Session count tracker
- **Session History**: Track all past focus sessions with timestamps

### 👤 **Student Profile**
- **Personal Information Management**:
  - Student name
  - Roll number
  - Branch/Department
- **Cloud Sync**: All profile data synced with Firebase Realtime Database
- **Statistics Dashboard** showing:
  - Overall attendance percentage
  - Total subjects enrolled
  - Tasks completed vs total
- **Quick Edit**: Update profile details anytime

### 🔐 **Authentication**
- **Firebase Authentication**: Secure email/password authentication
- **Auto-login**: Remembers logged-in users
- **Registration with Student Details**: Capture all student information during signup
- **Logout Functionality**: Secure sign-out from dashboard

### ☁️ **Cloud Synchronization**
- **Firebase Realtime Database Integration**:
  - Tasks synced across devices
  - Profile data accessible from any device
  - Real-time updates
- **Cross-device Access**: Login from any device and access your data
- **Offline Support**: Works offline with local Room database, syncs when online

### 🔔 **Smart Notifications**
- **Alarm System**:
  - MediaPlayer for audible alarm sound
  - Wake lock to ensure device wakes up
  - Vibration support
- **Email Reminders**: Opens email client with pre-filled message to user's registered email
- **Notification Channels**: Organized notifications for better management

---

## 🏗️ Technical Architecture

### **Technology Stack**

| Component | Technology |
|-----------|-----------|
| **Language** | Java |
| **UI Framework** | Material Design 3 |
| **Database** | Room (SQLite) |
| **Cloud Backend** | Firebase (Authentication + Realtime Database) |
| **Build System** | Gradle 8.6 |
| **Min SDK** | Android 7.0 (API 24) |
| **Target SDK** | Android 12+ (API 31+) |

### **Database Schema**

#### **Subject Table**
```java
- id: Integer (Primary Key, Auto-increment)
- name: String
- total: Integer (total classes)
- attended: Integer (attended classes)
```

#### **Task Table**
```java
- id: Integer (Primary Key, Auto-increment)
- title: String
- description: String
- time: Long (creation timestamp)
- dueDate: String
- priority: String (Low/Medium/High)
- isCompleted: Boolean
- hasAlarm: Boolean
```

#### **FocusSession Table**
```java
- id: Integer (Primary Key, Auto-increment)
- startTime: Long
- endTime: Long
- durationMinutes: Integer
- sessionType: String (focus/short_break/long_break)
- taskId: Integer (linked task, -1 if none)
- date: String (YYYY-MM-DD)
```

### **Firebase Structure**
```
students/
  └── {userId}/
      ├── email: String
      ├── name: String
      ├── rollNumber: String
      ├── branch: String
      └── tasks/
          └── {taskId}/
              ├── id: Integer
              ├── title: String
              ├── description: String
              ├── dueDate: String
              ├── priority: String
              ├── time: Long
              ├── isCompleted: Boolean
              └── hasAlarm: Boolean
```

---

## 📱 App Screens

### **Main Dashboard**
- Profile card with quick access to student details
- Attendance card for managing class attendance
- Task Manager card for assignments and todos
- Focus Mode card for Pomodoro timer
- Logout button in header

### **Attendance Screen**
- Add new subjects
- List of all subjects with attendance status
- Mark attendance (Present/Absent buttons)
- Overall percentage with color indicator
- 75% prediction calculations
- Individual subject percentages

### **Task List Screen**
- List of all tasks (RecyclerView)
- Toggle switch to show/hide completed tasks
- Task statistics (pending/completed count)
- FAB button to add new tasks
- Task details dialog on click
- Delete with confirmation
- Back navigation

### **Add Task Screen**
- Task title input
- Description text area
- Date picker for due date
- Time picker for due time
- Priority spinner (Low/Medium/High)
- Save button with Firebase sync

### **Focus Mode Screen**
- Large circular timer display
- Session type indicator (Focus/Break)
- Pomodoro counter
- Start/Pause button
- Reset button
- Task selection for linking
- Today's focus time
- Total focus hours
- Automatic session recording

### **Profile Screen**
- Editable fields for name, roll, branch
- Statistics dashboard:
  - Overall attendance
  - Total subjects
  - Task completion ratio
- Save button with cloud sync

### **Login/Register Screens**
- Logo and app branding
- Email/password fields
- Firebase authentication
- Registration includes student details
- Material Design inputs

---

## 🎨 Design Features

### **Color Scheme**
- **Primary**: Blue (#1E88E5)
- **Primary Dark**: #1565C0
- **Primary Light**: #42A5F5
- **Accent**: Orange (#FF6F00)
- **Success**: Green (#4CAF50)
- **Error**: Red (#F44336)
- **Background**: Light Gray (#F5F5F5)

### **UI Components**
- Gradient headers with logo
- Rounded card views (24dp corner radius)
- Elevated cards with shadows
- Material buttons with ripple effects
- Custom drawables and backgrounds
- Smooth animations and transitions
- Professional typography

---

## ⏰ Alarm & Reminder System

### **Timing Configuration**

| Event | Timing | Action |
|-------|--------|--------|
| **Initial Alarm** | 5 minutes before due time | 🔔 Notification with sound + vibration |
| **Email Reminder** | 1 minute after due time | 📧 Opens email client if task incomplete |

### **How It Works**
1. When creating a task, two alarms are scheduled:
   - **Pre-reminder**: 5 min before → Notification
   - **Post-reminder**: 1 min after → Email
2. AlarmManager uses `setExactAndAllowWhileIdle()` for reliability
3. PowerManager wake lock ensures device wakes up
4. MediaPlayer plays alarm ringtone (TYPE_ALARM)
5. Email opens with pre-filled subject and body
6. Reminders only trigger if task is still incomplete

---

## 📥 Installation

### **Prerequisites**
- Android Studio Arctic Fox or later
- JDK 11 or higher
- Android device/emulator with Android 7.0+
- Firebase project setup

### **Setup Steps**

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/edutrack.git
   cd edutrack
   ```

2. **Firebase Configuration**
   - Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
   - Enable Email/Password authentication
   - Enable Realtime Database
   - Download `google-services.json`
   - Place it in `app/` directory

3. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the project folder
   - Wait for Gradle sync

4. **Build and Run**
   - Connect Android device or start emulator
   - Click "Run" button (or Shift+F10)
   - Grant required permissions when prompted

### **Permissions Required**
- `SCHEDULE_EXACT_ALARM` - For precise alarm scheduling
- `POST_NOTIFICATIONS` - For alarm notifications
- `VIBRATE` - For notification vibration
- `INTERNET` - For Firebase sync
- `WAKE_LOCK` - To wake device for alarms
- `RECEIVE_BOOT_COMPLETED` - To restore alarms after reboot

---

## 🚀 Usage Guide

### **First Time Setup**
1. Launch the app
2. Click "Register" on login screen
3. Enter your details:
   - Name
   - Roll Number
   - Branch
   - Email (for login)
   - Password (min 6 characters)
4. Login with your credentials
5. You're ready to go!

### **Managing Attendance**
1. Go to "Attendance" from dashboard
2. Add subjects using "Add Subject" button
3. For each class:
   - Click "Present" if attended
   - Click "Absent" if missed
4. Check overall percentage in header
5. View 75% prediction calculations

### **Creating Tasks**
1. Go to "Task Manager" from dashboard
2. Click FAB (+) button
3. Fill in task details:
   - Title (required)
   - Description (optional)
   - Due date and time (required)
   - Priority level
4. Click "Save Task"
5. Alarms automatically scheduled!

### **Using Focus Mode**
1. Go to "Focus Mode" from dashboard
2. Optionally link to a task
3. Click "START" to begin 25-min focus session
4. Work without distractions
5. Timer automatically starts break when done
6. Repeat for productivity!

### **Checking Statistics**
1. Open Profile from dashboard
2. View statistics card:
   - Overall attendance %
   - Total subjects
   - Task completion rate
3. Focus Mode shows today's and total focus time

---

## 🔧 Advanced Features

### **Task Auto-hide**
- Completed tasks disappear after 500ms (smooth fade)
- Toggle switch to show/hide completed tasks
- Prevents clutter while maintaining history

### **Firebase Sync**
- Tasks automatically sync on create/update/delete
- Load tasks from Firebase on login
- Merge with local database for offline support
- Cross-device accessibility

### **Focus Session Analytics**
- Query sessions by date
- Query sessions by linked task
- Calculate total focus time
- Track session types separately
- Date-wise breakdown available

### **Smart Attendance Prediction**
- Algorithm calculates exact number of skips allowed
- Shows "Attend next X classes" if below 75%
- Real-time updates as attendance changes
- Color-coded warnings for quick status check

---

## 📝 Code Structure

```
app/src/main/java/com/example/edutrack/
├── activities/
│   ├── LoginActivity.java
│   ├── RegisterActivity.java
│   ├── MainActivity.java
│   ├── ProfileActivity.java
│   ├── AttendanceActivity.java
│   ├── AddSubjectActivity.java
│   ├── TaskListActivity.java
│   ├── AddTaskActivity.java
│   ├── FocusModeActivity.java
│   └── AlarmReceiver.java (BroadcastReceiver)
├── adapters/
│   ├── SubjectAdapter.java
│   └── TaskAdapter.java
└── db/
    ├── AppDatabase.java
    ├── Subject.java
    ├── SubjectDao.java
    ├── Task.java
    ├── TaskDao.java
    ├── FocusSession.java
    └── FocusSessionDao.java
```

---

## 🐛 Known Issues & Limitations

1. **Email Feature**: Opens email client, doesn't send automatically (requires user to click send)
2. **Alarm Sound**: May not work in Doze mode on some devices (wake lock mitigates this)
3. **Database Migration**: Upgrade from v2 to v3 required for Focus Mode feature
4. **Offline Sync**: Tasks created offline don't sync until internet is available

---

## 🔮 Future Enhancements

- [ ] Dark mode support
- [ ] GPA/CGPA calculator
- [ ] Timetable/schedule manager
- [ ] Notes and study materials storage
- [ ] Export attendance as PDF
- [ ] Widget support for home screen
- [ ] Recurring tasks
- [ ] Task categories/tags
- [ ] Calendar view for tasks
- [ ] Subject-wise focus time analytics

---

## 👨‍💻 Developer

**Name**: [Your Name]  
**Email**: subrahmanyam310308@gmail.com  
**GitHub**: [Your GitHub Profile]  
**LinkedIn**: [Your LinkedIn Profile]  

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 🙏 Acknowledgments

- **Material Design 3** for beautiful UI components
- **Firebase** for cloud backend infrastructure
- **Room Database** for local data persistence
- **Android Jetpack** for modern Android development

---

## 📞 Support

For issues, questions, or suggestions:
- Open an issue on GitHub
- Email: subrahmanyam310308@gmail.com

---

<div align="center">

**Made with ❤️ for students, by a student**

⭐ Star this repo if you found it helpful!

</div>
