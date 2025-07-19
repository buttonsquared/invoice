package com.invoice.dto

import com.invoice.entity.InvoiceStatus
import jakarta.validation.constraints.NotNull

data class UpdateInvoiceStatusRequest(
    @field:NotNull(message = "Status is required")
    val status: InvoiceStatus
)