package com.invoice.entity

import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "invoices", indexes = [Index(name = "walletAddress_idx", columnList = "wallet_address", unique = true)])
class Invoice(
    @Id
    @Column(name = "id", unique = true, nullable = false)
    @SequenceGenerator(name = "invoice_seq", sequenceName = "invoice_id_seq", allocationSize = 1)
    @GeneratedValue(strategy  = GenerationType.SEQUENCE, generator = "invoice_seq")
    var id: Long? = null,

    @Column(nullable = false)
    @Email
    @NotBlank
    var freelancerEmail: String = "",

    @Column(nullable = false)
    @NotBlank
    var clientName: String = "",

    @Email
    var clientEmail: String? = null,

    @Column(nullable = false, precision = 18, scale = 6)
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    var amount: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, length = 1000)
    @NotBlank
    var description: String = "",

    @Column(nullable = false, unique = true)
    @NotBlank
    var walletAddress: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: InvoiceStatus = InvoiceStatus.PENDING,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    var paidAt: LocalDateTime? = null,

    var expiresAt: LocalDateTime? = null
) {
    fun copy(
        id: Long? = this.id,
        freelancerEmail: String = this.freelancerEmail,
        clientName: String = this.clientName,
        clientEmail: String? = this.clientEmail,
        amount: BigDecimal = this.amount,
        description: String = this.description,
        walletAddress: String = this.walletAddress,
        status: InvoiceStatus = this.status,
        createdAt: LocalDateTime = this.createdAt,
        paidAt: LocalDateTime? = this.paidAt,
        expiresAt: LocalDateTime? = this.expiresAt
    ) = Invoice(
        id, freelancerEmail, clientName, clientEmail, amount,
        description, walletAddress, status, createdAt, paidAt, expiresAt
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Invoice) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "Invoice(id=$id, freelancerEmail='$freelancerEmail', clientName='$clientName', amount=$amount, status=$status)"
    }
}