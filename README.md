# BankingApp - API

Banking API built with Spring Boot that supports core financial operations such as user subscriptions, auto investment, deposits, withdrawals, transfers, and auto debit.

## 🚀 Features

- ✅ User subscription and PIN validation
- 💸 Deposit, withdrawal, and transfer endpoints
- 📅 Automatic debit scheduling
- 📈 Auto investment system
- 🔒 Secured with token-based authentication

## 🛠️ Technologies

- Java 21
- Spring Boot 3.3.1
- Spring Web, Spring Data JPA, Spring Security
- MySQL
- Docker
- Lombok

## 🧪 Run Locally

### Prerequisites

- Java 21
- Maven
- Docker

### Clone & Build

```bash
git clone https://github.com/Yairqh21/api-bankingapp.git
cd bankingapp
```
Run with Docker
```bash

docker build -t bankingapp .
docker run -p 3000:3000 bankingapp
```


📬 Main Endpoints

| Endpoint                               | Method | Auth | Description                    |
| -------------------------------------- | ------ | ---- | ------------------------------ |
| `/api/users/register`                  | POST   | ❌    | Register new user              |
| `/api/users/login`                     | POST   | ❌    | Login and receive token        |
| `/api/dashboard/user`                  | GET    | ✅    | Get user info                  |
| `/api/account/deposit`                 | POST   | ✅    | Deposit money                  |
| `/api/account/withdraw`                | POST   | ✅    | Withdraw funds                 |
| `/api/account/fund-transfer`           | POST   | ✅    | Transfer to another account    |
| `/api/account/transactions`            | GET    | ✅    | View transaction history       |
| `/api/account/buy-asset`               | POST   | ✅    | Buy asset                      |
| `/api/account/sell-asset`              | POST   | ✅    | Sell asset                     |
| `/market/prices`                       | GET    | ❌    | View all asset prices          |
| `/market/prices/{symbol}`              | GET    | ❌    | View specific asset price      |
| `/api/user-actions/subscribe`          | POST   | ✅    | Subscribe to periodic payments |
| `/api/user-actions/enable-auto-invest` | POST   | ✅    | Enable auto investment feature |
| `/api/users/logout`                    | GET    | ✅    | Logout and invalidate token    |

🔐 Auth = Requires JWT token in Authorization: Bearer <token>