package com.invoice.service

import com.invoice.dto.CreateInvoiceRequest
import com.invoice.dto.InvoiceResponse
import com.invoice.entity.Invoice
import com.invoice.entity.InvoiceStatus
import com.invoice.repository.InvoiceRepository
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class InvoiceService(
    private val invoiceRepository: InvoiceRepository,
    private val circleApiService: CircleApiService
) {
    
    @Value("\${app.frontend.base-url:http://localhost:3000}")
    private lateinit var frontendBaseUrl: String

    fun createInvoice(request: CreateInvoiceRequest): InvoiceResponse {
        // Generate wallet address using Circle API
        val walletAddress = runBlocking {
            circleApiService.createWalletAddress("Invoice for ${request.clientName}")
        }
        
        val invoice = Invoice().apply {
            freelancerEmail = request.freelancerEmail
            clientName = request.clientName
            clientEmail = request.clientEmail
            amount = request.amount
            description = request.description
            this.walletAddress = walletAddress
            status = InvoiceStatus.PENDING
            createdAt = LocalDateTime.now()
            expiresAt = LocalDateTime.now().plusDays(30) // 30 day expiration
        }
        
        val savedInvoice = invoiceRepository.save(invoice)
        return mapToResponse(savedInvoice)
    }

    fun getInvoiceById(id: Long): InvoiceResponse? {
        return invoiceRepository.findById(id)
            .map { mapToResponse(it) }
            .orElse(null)
    }

    fun getInvoicesByFreelancer(freelancerEmail: String): List<InvoiceResponse> {
        return invoiceRepository.findByFreelancerEmailOrderByCreatedAtDesc(freelancerEmail)
            .map { mapToResponse(it) }
    }

    fun updateInvoiceStatus(id: Long, status: InvoiceStatus): InvoiceResponse? {
        val invoice = invoiceRepository.findById(id).orElse(null) ?: return null
        
        invoice.status = status
        if (status == InvoiceStatus.PAID) {
            invoice.paidAt = LocalDateTime.now()
        }
        
        val savedInvoice = invoiceRepository.save(invoice)
        return mapToResponse(savedInvoice)
    }

    fun findInvoiceByWalletAddress(walletAddress: String): Invoice? {
        return invoiceRepository.findByWalletAddress(walletAddress)
    }

    private fun mapToResponse(invoice: Invoice): InvoiceResponse {
        return InvoiceResponse(
            id = invoice.id,
            freelancerEmail = invoice.freelancerEmail,
            clientName = invoice.clientName,
            clientEmail = invoice.clientEmail,
            amount = invoice.amount,
            description = invoice.description,
            walletAddress = invoice.walletAddress,
            status = invoice.status,
            createdAt = invoice.createdAt,
            paidAt = invoice.paidAt,
            expiresAt = invoice.expiresAt,
            paymentUrl = "$frontendBaseUrl/invoice/${invoice.id}"
        )
    }

}