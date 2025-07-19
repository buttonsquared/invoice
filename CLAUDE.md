# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a stablecoin invoicing application built with:
- **Backend**: Kotlin + Spring Boot + PostgreSQL 
- **Frontend**: React + TypeScript + Tailwind CSS (to be implemented)
- **Payments**: Circle USDC API integration
- **Deployment**: Vercel (Frontend) + Render (Backend + DB)

The app allows freelancers and businesses to create and share USDC-based invoices with real-time payment detection via Circle webhooks.

## Development Commands

### Backend (Kotlin/Spring Boot)
- **Run application**: `./gradlew bootRun`
- **Run tests**: `./gradlew test` 
- **Build**: `./gradlew build`
- **Clean build**: `./gradlew clean build`

The application runs on the default Spring Boot port (8080) unless configured otherwise.

## Architecture

### Current Structure
- **Main application**: `src/main/kotlin/com/invoice/InvoiceApplication.kt` - Standard Spring Boot application entry point
- **Tests**: `src/test/kotlin/com/invoice/InvoiceApplicationTests.kt` - Basic context loading test
- **Configuration**: `src/main/resources/application.yml` - Spring application configuration

### Planned Architecture (from tasks.md)
The application is designed around these core components:

1. **Invoice Entity**: Database model with fields for id, userId, clientName, amount, currency, walletAddress, description, status, createdAt
2. **REST API Endpoints**:
   - `POST /api/invoices` - create invoice
   - `GET /api/invoices/{id}` - get invoice by ID  
   - `GET /api/invoices?userId=` - list invoices for user
   - `POST /api/invoices/{id}/mark-paid` - mark invoice as paid
   - `POST /api/webhook/circle` - Circle API webhook handler
3. **Services**: InvoiceRepository and InvoiceService for business logic
4. **Circle API Integration**: Service to generate USDC deposit addresses and handle payment webhooks

### Dependencies
Key Spring Boot starters in use:
- `spring-boot-starter-data-jpa` - Database integration
- `spring-boot-starter-security` - Security framework
- `spring-boot-starter-web` - REST API support
- Kotlin support with reflection and Jackson module

## Key Implementation Notes

- Uses Java 21 as the target JVM version
- JPA entities configured with allOpen plugin for proper Kotlin integration
- Database will be PostgreSQL (not yet configured in application.yml)
- Swagger/Springdoc OpenAPI planned for API documentation
- Real-time payment updates via 10-second polling on frontend
- QR code generation planned using qrcode.react library