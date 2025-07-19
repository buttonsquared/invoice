package com.invoice.repository

import com.invoice.entity.Invoice
import com.invoice.entity.InvoiceStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface InvoiceRepository : JpaRepository<Invoice, Long> {
    
    fun findByFreelancerEmailOrderByCreatedAtDesc(freelancerEmail: String): List<Invoice>
    
    fun findByWalletAddress(walletAddress: String): Invoice?
    
    fun findByStatus(status: InvoiceStatus): List<Invoice>
    
    fun findByFreelancerEmailAndStatus(freelancerEmail: String, status: InvoiceStatus): List<Invoice>
}