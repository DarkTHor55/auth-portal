# 🔐 Auth Portal - Quarkus User Authentication System

A secure user authentication portal built using **Quarkus** + **PostgreSQL**, supporting:
- User Signup with Email Verification
- Login with JWT Token
- Logout with Session Management
- Email Sending via Gmail SMTP

---

## 🛠 Tech Stack

- ⚙️ Quarkus (Java 17)
- 🗄️ PostgreSQL
- ✉️ Gmail SMTP
- 🔐 JWT Authentication
- 🧪 REST APIs

---

## 📁 Project Structure

auth-portal/
├── src/main/java/com/authportal/
│ ├── controller/
│ ├── service/
│ ├── repository/
│ └── model/
├── src/main/resources/
│ ├── application.properties
│ ├── privateKey.pem
│ └── publicKey.pem
├── pom.xml
└── README.md



## 🔧 Configuration
# Native Build
quarkus.native.enabled=false
quarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-native-image:22.3-java17

# Database
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=your_db_username
quarkus.datasource.password=your_db_password
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/your_database_name

quarkus.hibernate-orm.database.generation=update

# HTTP Port
quarkus.http.port=8088

# Password Hashing
security.password.salt=your_password_salt_here

# JWT Config
smallrye.jwt.sign.key.location=classpath:your_private_key.pem
mp.jwt.verify.publickey.location=classpath:your_public_key.pem
mp.jwt.verify.issuer=your_jwt_issuer
mp.jwt.verify.algorithm=RS256
jwt.expiration=3600

# Gmail SMTP
quarkus.mailer.host=smtp.gmail.com
quarkus.mailer.port=587
quarkus.mailer.username=your_email@gmail.com
quarkus.mailer.password=your_gmail_app_password
quarkus.mailer.from=your_email@gmail.com
quarkus.mailer.auth=true
quarkus.mailer.auth-methods=PLAIN LOGIN
quarkus.mailer.start-tls=REQUIRED
quarkus.mailer.ssl=false
