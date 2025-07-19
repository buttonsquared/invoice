# ✅ Stablecoin Invoicing App – Task Breakdown

## 🧱 1. Project Setup

- [ ] Create `spring-boot` Kotlin backend project (Gradle or Maven)
- [ ] Set up PostgreSQL connection with `application.yml`
- [ ] Create `invoice` table model with fields:
    - `id`, `userId`, `clientName`, `amount`, `currency`, `walletAddress`, `description`, `status`, `createdAt`

## 📦 2. Backend Features

- [ ] Implement REST API endpoints:
    - `POST /api/invoices` – create invoice
    - `GET /api/invoices/{id}` – get invoice by ID
    - `GET /api/invoices?userId=` – list invoices for a user
    - `POST /api/invoices/{id}/mark-paid` – mark invoice as paid
- [ ] Integrate Swagger (Springdoc OpenAPI) for API testing
- [ ] Add `InvoiceRepository` and `InvoiceService`

## 💸 3. Circle API Integration

- [ ] Add service to call Circle API to generate USDC deposit address per invoice
- [ ] Store address in `walletAddress` field
- [ ] Create webhook handler at `POST /api/webhook/circle`
    - Match wallet address to invoice
    - Update status to `PAID`

## 💻 4. Frontend – React App

- [ ] Set up React + TypeScript + Tailwind CSS
- [ ] Create pages:
    - `/create` → CreateInvoice page with form
    - `/invoice/:id` → InvoiceDetail page (client-facing)
    - `/` → Dashboard page listing invoices for user

## 🔁 5. Real-Time Payment Updates

- [ ] Add polling to `InvoiceDetail` to check status every 10s
- [ ] Display QR code using `qrcode.react`
- [ ] Show payment status visually (e.g., ✅ Paid, ⏳ Waiting)

## 🌍 6. Deployment

- [ ] Deploy backend to Render (Java Web Service + PostgreSQL)
- [ ] Deploy frontend to Vercel (React app)
- [ ] Set `REACT_APP_API_URL` environment variable in Vercel

## 💡 Optional Enhancements

- [ ] Add branding to invoice page (logo, colors)
- [ ] Add copy-to-clipboard for wallet address
- [ ] Add email notification upon payment received
- [ ] Add admin dashboard for managing invoices

