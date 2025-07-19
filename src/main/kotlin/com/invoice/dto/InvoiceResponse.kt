package com.invoice.dto

import com.invoice.entity.InvoiceStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class InvoiceResponse(
    val id: Long?,
    val freelancerEmail: String,
    val clientName: String,
    val clientEmail: String?,
    val amount: BigDecimal,
    val description: String,
    val walletAddress: String,
    val status: InvoiceStatus,
    val createdAt: LocalDateTime,
    val paidAt: LocalDateTime?,
    val expiresAt: LocalDateTime?,
    val paymentUrl: String
)