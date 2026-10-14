# Ledgerly — Smart Expense Tracker

A compact full-stack expense tracker built with React, Java Spring Boot and MySQL. The interface uses a calm editorial dashboard rather than a generic admin template. It includes accounts, income/expense transactions, monthly budgets, category/month charts, spending insights and month-to-month reports.

## Features
- Register and sign in with BCrypt password hashing and JWT sessions.
- Each transaction belongs to the signed-in user.
- Record income or expense, category, date, note and amount.
- Monthly budget and dashboard totals.
- Category breakdown and six-month trend charts.
- Insights generated from the user's real transactions (transparent rule-based analysis; no paid AI key required).
- Monthly comparison report.

## Requirements
- Java 21+
- Node.js 20+
- MySQL 8+

## Run locally
1. Create a MySQL database: `CREATE DATABASE ledgerly;`
2. Set environment variables before starting the API:
   - `DB_URL=jdbc:mysql://localhost:3306/ledgerly`
   - `DB_USERNAME=your_mysql_user`
   - `DB_PASSWORD=your_mysql_password`
   - `JWT_SECRET=replace-with-a-random-secret-at-least-32-characters-long`
3. From `backend`, run `mvn spring-boot:run`.
4. In another terminal, from `frontend`, run `npm install` then `npm run dev`.
5. Open the Vite URL, normally `http://localhost:5173`.

The backend creates the tables automatically for local development. Do not use `ddl-auto=update` for a production financial application without reviewing migrations and backups.

## Deploy
### Frontend on Vercel
- Import the repository in Vercel.
- Set **Root Directory** to `frontend`.
- Add environment variable `VITE_API_URL` with the public backend URL, no trailing slash, e.g. `https://your-api.example.com/api`.
- Deploy. `frontend/vercel.json` handles SPA routing.

### Backend and database
Vercel does not run a persistent Java Spring Boot server as a normal Vite frontend deployment. Deploy `backend` to a Java-capable host (or a Docker host), create a managed MySQL database, then set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, and `APP_CORS_ORIGINS` on the backend service. Set `APP_CORS_ORIGINS` to your exact Vercel domain, e.g. `https://ledgerly-yourname.vercel.app`.

## API overview
- `POST /api/auth/register` — `{ "name", "email", "password" }`
- `POST /api/auth/login` — `{ "email", "password" }`
- `GET /api/transactions` — list signed-in user's transactions
- `POST /api/transactions` — create transaction
- `DELETE /api/transactions/{id}` — delete own transaction
- `GET /api/dashboard?month=YYYY-MM` — totals, category totals and six-month trend
- `GET /api/insights?month=YYYY-MM` — spending suggestions
- `GET /api/reports?month=YYYY-MM` — selected month vs previous month
- `PUT /api/budget` — `{ "month": "YYYY-MM", "amount": 20000 }`

## Notes
Insights are explainable statistical/rule-based recommendations, not a claim that a large language model is running. This keeps the project free to run and protects transaction data from being sent to a third party. An LLM provider can be added later behind the backend if desired. For production, add rate limiting, email verification, password reset, database migrations, HTTPS, monitoring and a security review.
