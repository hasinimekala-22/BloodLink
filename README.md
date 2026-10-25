# BloodLink — Fairness-Aware Emergency Blood Donor Matching and Prioritization System

## About the Project

**BloodLink** is a Java-based desktop application developed as a B.Tech Computer Science and Engineering project. It helps organize donor records, manage blood requests, and rank potential donors using a simplified compatibility filter and a transparent scoring method.

The application uses Java Swing for its graphical user interface and MySQL for data storage.

## Features

- **Dashboard:** View donor, request, and matching information.
- **Donor Registration:** Add donor details to the database.
- **Donor Directory:** Search donors by name, city, or phone and filter by blood group.
- **Availability Management:** Update donor availability status.
- **Blood Requests:** Create and review blood requests.
- **Donor Matching:** Display ranked candidate donors using simplified rules.
- **Request History:** Review recorded blood requests.
- **Fairness Report:** Inspect candidate selection-frequency information.

## Technologies Used

| Technology | Purpose |
|---|---|
| Java 8 | Application programming language |
| Java Swing | Graphical user interface |
| MySQL | Relational database |
| JDBC | Database connectivity |
| MySQL Connector/J | MySQL JDBC driver |
| Git and GitHub | Version control and project hosting |

## Project Structure

```text
BloodLink/
├── src/
│   ├── BloodLink.java
│   └── Donor.java
├── out/                  # Compiled classes (generated)
├── .gitignore
└── README.md
```

## Prerequisites

Before running the application, install or configure:

- Java Development Kit (JDK) 8
- MySQL Server
- MySQL Connector/J
- The `bloodlink` database and required tables

## Database Setup

Create the database and donor table in MySQL:

```sql
CREATE DATABASE IF NOT EXISTS bloodlink;
USE bloodlink;

CREATE TABLE IF NOT EXISTS donors (
    donor_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT,
    gender VARCHAR(20),
    blood_group VARCHAR(5) NOT NULL,
    phone VARCHAR(15),
    city VARCHAR(100),
    available BOOLEAN DEFAULT TRUE
);
```

The application may create additional tables for blood requests and matching logs when it runs.

## How to Compile and Run on Windows

Open Command Prompt in the project directory.

### 1. Navigate to the project

```bat
cd /d "C:\Users\M_Hasini\Desktop\BloodLink"
```

### 2. Configure the database password

Set the environment variable to your local MySQL password:

```bat
set BLOODLINK_DB_PASSWORD=YOUR_LOCAL_PASSWORD
```

Replace `YOUR_LOCAL_PASSWORD` with your actual password. Never publish your real database password.

### 3. Compile the Java source files

```bat
javac -cp "C:\Users\M_Hasini\javacode\mysql-connector-j-26.7.0.jar" -d out src\BloodLink.java
```

### 4. Launch the application

```bat
java -cp "out;C:\Users\M_Hasini\javacode\mysql-connector-j-26.7.0.jar" BloodLink
```

Adjust the Connector/J path if the JAR is stored elsewhere.

## Matching and Prioritization

The prototype uses simplified blood-group compatibility rules and a candidate score based on prior appearances in the matching log and a city-match bonus.

The illustrative scoring formula is:

`Score = 100 - (10 × prior match-log appearances) + 20 (if city matches)`

This score demonstrates rule-based ranking only. Logged candidate appearances do not necessarily represent completed donations, and the score is not a clinically validated fairness measure.

## Limitations and Safety Notice

**BloodLink is an academic prototype and is not intended for real-world medical or transfusion decisions.**

- Compatibility logic is simplified and requires clinical validation.
- The system does not verify medical eligibility, laboratory results, or actual donation completion.
- Candidate ranking is illustrative and must not replace professional judgment.
- Use fictional donor information in demonstrations and testing.
- Do not commit passwords, credentials, or private donor information to GitHub.

## Future Enhancements

- Secure login and role-based access
- Improved data validation and audit logging
- Better donor availability and donation-history management
- Automated unit and integration testing
- Clinically reviewed compatibility workflows
- Explicitly defined and evaluated fairness metrics

## Author

**Hasini Mekala**  
B.Tech — Computer Science and Engineering

## License

This project was developed for academic and educational purposes. Add a license file if you intend to distribute or reuse the project under specific terms.
