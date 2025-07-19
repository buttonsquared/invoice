package com.invoice

import com.invoice.dto.CreateInvoiceRequest
import com.invoice.dto.InvoiceResponse
import com.invoice.entity.Invoice
import com.invoice.entity.InvoiceStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

object TestDataBuilder {
    
    fun createInvoiceRequest(
        freelancerEmail: String = "freelancer@example.com",
        clientName: String = "Test Client",
        clientEmail: String? = "client@example.com",
        amount: BigDecimal = BigDecimal("100.50"),
        description: String = "Test invoice description"
    ) = CreateInvoiceRequest(
        freelancerEmail = freelancerEmail,
        clientName = clientName,
        clientEmail = clientEmail,
        amount = amount,
        description = description
    )
    
    fun invoice(
        freelancerEmail: String = "freelancer@example.com",
        clientName: String = "Test Client",
        clientEmail: String? = "client@example.com",
        amount: BigDecimal = BigDecimal("100.50"),
        description: String = "Test invoice description",
        walletAddress: String = "0x" + UUID.randomUUID().toString().replace("-", "").take(40),
        status: InvoiceStatus = InvoiceStatus.PENDING,
        createdAt: LocalDateTime = LocalDateTime.now(),
        paidAt: LocalDateTime? = null,
        expiresAt: LocalDateTime? = LocalDateTime.now().plusDays(30)
    ) = Invoice().apply {
        this.freelancerEmail = freelancerEmail
        this.clientName = clientName
        this.clientEmail = clientEmail
        this.amount = amount
        this.description = description
        this.walletAddress = walletAddress
        this.status = status
        this.createdAt = createdAt
        this.paidAt = paidAt
        this.expiresAt = expiresAt
    }
    
    fun invoiceResponse(
        id: Long? = 1L,
        freelancerEmail: String = "freelancer@example.com",
        clientName: String = "Test Client",
        clientEmail: String? = "client@example.com",
        amount: BigDecimal = BigDecimal("100.50"),
        description: String = "Test invoice description",
        walletAddress: String = "0x" + UUID.randomUUID().toString().replace("-", "").take(40),
        status: InvoiceStatus = InvoiceStatus.PENDING,
        createdAt: LocalDateTime = LocalDateTime.now(),
        paidAt: LocalDateTime? = null,
        expiresAt: LocalDateTime? = LocalDateTime.now().plusDays(30),
        paymentUrl: String = "http://localhost:3000/invoice/$id"
    ) = InvoiceResponse(
        id = id,
        freelancerEmail = freelancerEmail,
        clientName = clientName,
        clientEmail = clientEmail,
        amount = amount,
        description = description,
        walletAddress = walletAddress,
        status = status,
        createdAt = createdAt,
        paidAt = paidAt,
        expiresAt = expiresAt,
        paymentUrl = paymentUrl
    )
}