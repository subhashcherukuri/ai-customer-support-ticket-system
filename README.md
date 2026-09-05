# AI Customer Support Ticket Management System

An AI-powered customer support ticket management web application built with Java and Spring Boot. The system provides role-based ticket management for customers, support agents, and administrators, with AI-assisted ticket analysis and response suggestions.

---

## 📌 Project Overview

The application provides a centralized platform for handling customer support requests.

Customers can create and track support tickets and communicate with support agents. Agents can manage their assigned tickets, respond to customers, and update ticket status and priority. Administrators can monitor all tickets, assign tickets to agents, create support agents, and manage the overall support workflow.

The application also integrates an LLM through Spring AI and Groq to analyze support tickets and generate useful information for support teams.

### High-Level Workflow

```text
Customer
    ↓
Creates Support Ticket
    ↓
AI Ticket Analysis
    ↓
Category + Priority + Sentiment
+ Summary + Suggested Reply
    ↓
Ticket Assigned to Agent
    ↓
Customer ↔ Agent Conversation
    ↓
Resolve Ticket
    ↓
Close Ticket
```

---

## ✨ Features

### 👤 Customer

- Customer registration and login
- Create support tickets
- View personal tickets
- View ticket details
- Send messages on tickets
- View conversation history
- View ticket status and priority
- View AI-generated ticket analysis

### 🎧 Agent

- Secure agent login
- View assigned tickets
- View ticket details
- Reply to customers
- Update ticket status
- Update ticket priority
- View ticket conversations
- Perform AI ticket reanalysis

### 👨‍💼 Admin

- Admin dashboard
- View all support tickets
- View ticket details
- Assign tickets to agents
- Update ticket status
- Update ticket priority
- Reply to any ticket
- Perform AI ticket reanalysis
- Create support agents
- View existing agents
- Monitor ticket statistics

---

## 🤖 AI Integration

The application uses Spring AI with an OpenAI-compatible configuration to communicate with Groq's API and an LLM model.

### AI Capabilities

The AI analyzes a support ticket and generates:

- Ticket category
- Ticket priority
- Customer sentiment
- Ticket summary
- Suggested reply

### AI Workflow

```text
Customer creates ticket
        ↓
Ticket information is sent to AI
        ↓
AI analyzes the ticket
        ↓
Category
Priority
Sentiment
Summary
Suggested Reply
        ↓
Analysis is stored with the ticket
        ↓
Agent uses the information while handling the ticket
```

Agents and administrators can also trigger AI reanalysis when required.

---

## 🔐 Authentication & Security

The application uses Spring Security for authentication and authorization.

### Security Features

- Session-based authentication
- BCrypt password hashing
- Role-based access control
- Method-level authorization using `@PreAuthorize`
- Protected web pages and REST endpoints
- Customer-specific ticket access
- Agent-specific ticket access
- Admin access to all tickets
- Secure logout
- Unauthenticated web requests redirected to the login page

### Role-Based Access

| Feature | Customer | Agent | Admin |
|---|:---:|:---:|:---:|
| Create Ticket | ✅ | ❌ | ❌ |
| View Own Tickets | ✅ | ❌ | ❌ |
| View Assigned Tickets | ❌ | ✅ | ✅ |
| View All Tickets | ❌ | ❌ | ✅ |
| Send Message | Own | Assigned | Any |
| Change Status | ❌ | ✅ | ✅ |
| Change Priority | ❌ | ✅ | ✅ |
| Assign Agent | ❌ | ❌ | ✅ |
| Create Agents | ❌ | ❌ | ✅ |
| AI Reanalysis | ❌ | ✅ | ✅ |

Authorization is enforced on the backend rather than relying only on UI restrictions.

---

## 🎫 Ticket Management

Each ticket contains information including:

- Ticket ID
- Title
- Description
- Category
- Priority
- Status
- Sentiment
- AI-generated summary
- AI-generated suggested reply
- Customer
- Assigned agent
- Created timestamp
- Updated timestamp

### Ticket Categories

- `PAYMENT`
- `REFUND`
- `ORDER`
- `DELIVERY`
- `ACCOUNT`
- `TECHNICAL`
- `PRODUCT`
- `OTHER`

### Ticket Priorities

- `LOW`
- `MEDIUM`
- `HIGH`
- `CRITICAL`

### Ticket Statuses

- `OPEN`
- `IN_PROGRESS`
- `RESOLVED`
- `CLOSED`

### Sentiment

- `POSITIVE`
- `NEUTRAL`
- `NEGATIVE`

---

## 🔄 Ticket Status Workflow

Tickets follow a controlled status workflow:

```text
OPEN
  ↓
IN_PROGRESS
  ↓
RESOLVED
  ↓
CLOSED
```

A resolved ticket can be moved back to `IN_PROGRESS` when necessary.

Closed tickets cannot receive new messages or be moved to another status.

---

## 💬 Ticket Conversations

Each ticket supports a conversation between customers and support agents.

```text
Customer
    ↓
Message
    ↓
Agent
    ↓
Reply
    ↓
Customer
    ↓
...
```

Messages are stored separately and linked to the ticket and sender.

Conversation access is controlled according to the user's role.

---

## 👨‍💼 Admin Dashboard

The admin dashboard provides an overview of support operations.

It displays:

- Total tickets
- Open tickets
- In-progress tickets
- Resolved tickets
- Closed tickets

Admins can access:

- Ticket Management
- Agent Management

### Ticket Assignment

Administrators can assign a ticket to a support agent.

```text
Admin
  ↓
Select Ticket
  ↓
Select Agent
  ↓
Assign
  ↓
Ticket assigned to Agent
  ↓
Ticket appears in Agent Dashboard
```

---

## 🖥️ User Interface

The application uses server-side rendering with Thymeleaf.

### Frontend Technologies

- HTML
- CSS
- Thymeleaf
- Bootstrap

The UI is designed to be responsive and professional while keeping the application simple and beginner-friendly.

### UI Pages

#### Authentication

- Login
- Customer Registration

#### Customer

- Dashboard
- Create Ticket
- My Tickets
- Ticket Details

#### Agent

- Dashboard
- Ticket Details

#### Admin

- Dashboard
- All Tickets
- Ticket Details
- Manage Agents

The core application workflow does not require JavaScript.

---

## 🛠️ Technology Stack

### Backend

- Java
- Spring Boot
- Spring MVC
- Spring Data JPA
- Hibernate
- Spring Security
- Spring Validation
- Spring AI
- Lombok

### Frontend

- HTML
- CSS
- Thymeleaf
- Bootstrap

### Database

- MySQL

### AI

- Spring AI
- Groq API
- GPT-OSS-20B

### Development Tools

- IntelliJ IDEA
- Maven
- Postman
- Git
- GitHub

---

## 🏗️ Application Architecture

The project follows a simple layered architecture.

```text
                ┌─────────────────────┐
                │   Thymeleaf UI      │
                │   REST API          │
                └──────────┬──────────┘
                           ↓
                ┌─────────────────────┐
                │    Controllers      │
                └──────────┬──────────┘
                           ↓
                ┌─────────────────────┐
                │      Services       │
                └──────┬─────────┬────┘
                       ↓         ↓
              ┌────────────┐  ┌────────────┐
              │ Repository │  │  AiService │
              └──────┬─────┘  └──────┬─────┘
                     ↓                ↓
              ┌────────────┐    ┌──────────┐
              │   MySQL    │    │   Groq   │
              └────────────┘    └──────────┘
```

### Main Layers

- **Controller** – Handles web and REST requests
- **Service** – Contains application and business logic
- **Repository** – Handles database operations
- **Entity** – Represents database tables
- **DTO** – Transfers request and response data
- **Exception** – Handles application-specific errors
- **Config** – Contains security configuration

---

## 📁 Project Structure

```text
src/
└── main/
    ├── java/
    │   └── com/example/support/
    │       │
    │       ├── config/
    │       │   └── SecurityConfig.java
    │       │
    │       ├── controller/
    │       │   ├── AuthController.java
    │       │   ├── TicketController.java
    │       │   ├── AdminController.java
    │       │   └── PageController.java
    │       │
    │       ├── dto/
    │       │   ├── LoginRequest.java
    │       │   ├── LoginResponse.java
    │       │   ├── RegisterRequest.java
    │       │   ├── UserResponse.java
    │       │   ├── CreateTicketRequest.java
    │       │   ├── TicketResponse.java
    │       │   ├── CreateMessageRequest.java
    │       │   ├── MessageResponse.java
    │       │   ├── CreateAgentRequest.java
    │       │   └── AiTicketAnalysisResponse.java
    │       │
    │       ├── entity/
    │       │   ├── User.java
    │       │   ├── Ticket.java
    │       │   └── TicketMessage.java
    │       │
    │       ├── enums/
    │       │   ├── Role.java
    │       │   ├── TicketStatus.java
    │       │   ├── TicketPriority.java
    │       │   ├── TicketCategory.java
    │       │   └── Sentiment.java
    │       │
    │       ├── exception/
    │       │   ├── ResourceNotFoundException.java
    │       │   ├── DuplicateResourceException.java
    │       │   ├── BadRequestException.java
    │       │   └── AccessDeniedException.java
    │       │
    │       ├── repository/
    │       │   ├── UserRepository.java
    │       │   ├── TicketRepository.java
    │       │   └── TicketMessageRepository.java
    │       │
    │       └── service/
    │           ├── UserService.java
    │           ├── TicketService.java
    │           └── AiService.java
    │
    └── resources/
        │
        ├── templates/
        │   ├── login.html
        │   ├── register.html
        │   ├── index.html
        │   │
        │   ├── customer/
        │   │   ├── dashboard.html
        │   │   ├── create-ticket.html
        │   │   ├── tickets.html
        │   │   └── ticket-details.html
        │   │
        │   ├── agent/
        │   │   ├── dashboard.html
        │   │   └── ticket-details.html
        │   │
        │   ├── admin/
        │   │   ├── dashboard.html
        │   │   ├── tickets.html
        │   │   ├── ticket-details.html
        │   │   └── agents.html
        │   │
        │   └── fragments/
        │       ├── navbar.html
        │       └── alerts.html
        │
        └── static/
            └── css/
                └── style.css
```

---

## 🗄️ Database Design

The application uses MySQL for persistent data storage.

### Database

```text
support_ticket_db
```

### Main Entities

```text
User
Ticket
TicketMessage
```

### Entity Relationships

```text
User
 ├── Customer → Tickets
 ├── Agent → Assigned Tickets
 └── Sender → Ticket Messages

Ticket
 ├── Customer
 ├── Assigned Agent
 └── Ticket Messages
```

### User

Stores customer, agent, and administrator information.

Main fields:

- ID
- Name
- Email
- Password
- Role
- Created timestamp

### Ticket

Stores customer support requests and AI analysis.

Main fields:

- ID
- Title
- Description
- Category
- Priority
- Status
- Sentiment
- Summary
- Suggested reply
- Customer
- Assigned agent
- Created timestamp
- Updated timestamp

### TicketMessage

Stores messages exchanged within a ticket conversation.

Main fields:

- ID
- Message
- Ticket
- Sender
- Created timestamp

---

## 🚀 Getting Started

### Prerequisites

Install the following:

- Java
- Maven
- MySQL
- Git

A Groq API key is required for AI functionality.

---

## ⚙️ Database Configuration

Create the database in MySQL:

```sql
CREATE DATABASE support_ticket_db;
```

Configure the database connection in:

```text
src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/support_ticket_db
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

Do not commit your actual database password to GitHub.

---

## 🤖 AI Configuration

The application uses Groq through Spring AI.

Set the API key as an environment variable.

### Windows

```powershell
$env:GROQ_API_KEY="your_api_key"
```

### Linux / macOS

```bash
export GROQ_API_KEY="your_api_key"
```

The application reads the key using:

```properties
spring.ai.openai.api-key=${GROQ_API_KEY}
```

The application uses Groq's OpenAI-compatible API endpoint.

Never commit the actual API key to GitHub.

---

## ▶️ Running the Application

### 1. Clone the repository

```bash
git clone YOUR_GITHUB_REPOSITORY_URL
```

### 2. Navigate to the project

```bash
cd support-ticket-system
```

### 3. Build the application

```bash
mvn clean install
```

### 4. Run the application

```bash
mvn spring-boot:run
```

### 5. Open the application

```text
http://localhost:8080
```

---

## 🔑 Default Admin Account

For development and testing, the application initializes a default administrator account.

```text
Email: admin@support.com
Password: admin123
```

For production environments, replace the default credentials with secure credentials.

---

## 🧪 Testing

The application can be tested using:

- Browser-based UI testing
- Postman REST API testing
- Authentication testing
- Role-based authorization testing
- Ticket workflow testing
- Ticket conversation testing
- AI analysis testing

Important authorization scenarios include:

- Customer can access only their own tickets
- Agent can access only tickets assigned to them
- Admin can access all tickets
- Only admins can assign tickets
- Only admins can create agents
- Closed tickets reject new messages
- Unauthenticated web users are redirected to login

---

## 📡 REST API Overview

The application exposes REST endpoints for core backend operations.

### Authentication

```text
POST /api/auth/login
GET  /api/auth/me
POST /logout
```

### Customer Tickets

```text
POST /api/tickets
GET  /api/tickets/my
GET  /api/tickets/{ticketId}
```

### Ticket Messages

```text
POST /api/tickets/{ticketId}/messages
GET  /api/tickets/{ticketId}/messages
```

### Agent/Admin Ticket Management

```text
GET  /api/tickets/all
PUT /api/tickets/{ticketId}/status
PUT /api/tickets/{ticketId}/priority
PUT /api/tickets/{ticketId}/assign
POST /api/tickets/{ticketId}/reanalyze
```

### Admin

```text
POST /admin/agents
```

> The REST API paths should be verified against the final controller mappings if the project is modified later.

---

## 📊 Key Project Highlights

- AI-powered support ticket analysis
- Role-based customer, agent, and admin workflows
- Spring Security authentication and authorization
- BCrypt password hashing
- Customer-agent ticket conversations
- Admin ticket assignment
- Controlled ticket status workflow
- AI-generated summaries and suggested replies
- MySQL persistence using JPA/Hibernate
- DTO-based request and response handling
- Global exception handling
- Server-side Thymeleaf UI
- Responsive Bootstrap interface
- REST APIs
- Git/GitHub version control
- Simple layered architecture without unnecessary microservices

---

## 🔮 Future Enhancements

Possible future improvements include:

- Email notifications
- File attachments
- Advanced ticket search and filtering
- Pagination
- Agent performance analytics
- Customer notifications
- Knowledge base integration
- AI-powered knowledge base suggestions
- Ticket escalation rules
- Automated SLA tracking

---

## 📚 Learning Outcomes

This project provided practical experience with:

- Java backend development
- Spring Boot application development
- REST API development
- Spring Data JPA and Hibernate
- MySQL database integration
- Spring Security
- Authentication and authorization
- BCrypt password hashing
- DTO design
- Exception handling
- Thymeleaf server-side rendering
- Bootstrap UI development
- LLM/API integration using Spring AI
- Git and GitHub project management

---

## 👨‍💻 Author

**Subhash Cherukuri**

Computer Science Engineering Student

### Areas of Interest

- Java Development
- Spring Boot
- Data Structures & Algorithms
- Machine Learning
- AI Application Development

---

## ⭐ Project Summary

This project combines traditional Java Spring Boot backend development with modern AI capabilities to create a practical customer support management system.

It demonstrates how authentication, authorization, database management, REST APIs, server-side web development, and LLM integration can be combined into a single real-world application.
