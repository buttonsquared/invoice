package com.invoice.dto

import jakarta.validation.constraints.*
import java.math.BigDecimal

data class CreateInvoiceRequest(
    @field:Email(message = "Freelancer email must be valid")
    @field:NotBlank(message = "Freelancer email is required")
    val freelancerEmail: String,

    @field:NotBlank(message = "Client name is required")
    @field:Size(max = 100, message = "Client name must be less than 100 characters")
    val clientName: String,

    @field:Email(message = "Client email must be valid")
    val clientEmail: String?,

    @field:DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @field:Digits(integer = 12, fraction = 6, message = "Amount can have maximum 12 digits and 6 decimal places")
    val amount: BigDecimal,

    @field:NotBlank(message = "Description is required")
    @field:Size(max = 1000, message = "Description must be less than 1000 characters")
    val description: String
)