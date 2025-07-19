package com.invoice.repository

import com.invoice.TestDataBuilder
import com.invoice.entity.InvoiceStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import java.math.BigDecimal
import java.time.LocalDateTime

@DataJpaTest
class InvoiceRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var invoiceRepository: InvoiceRepository

    @Test
    fun `findByFreelancerEmailOrderByCreatedAtDesc should return invoices sorted by creation date`() {
        // Given
        val freelancerEmail = "freelancer@test.com"
        val invoice1 = TestDataBuilder.invoice(
            freelancerEmail = freelancerEmail,
            clientName = "Client 1",
            createdAt = LocalDateTime.now().minusDays(2),
            walletAddress = "wallet1"
        )
        val invoice2 = TestDataBuilder.invoice(
            freelancerEmail = freelancerEmail,
            clientName = "Client 2", 
            createdAt = LocalDateTime.now().minusDays(1),
            walletAddress = "wallet2"
        )
        val invoice3 = TestDataBuilder.invoice(
            freelancerEmail = "other@test.com", // Different freelancer
            clientName = "Client 3",
            createdAt = LocalDateTime.now(),
            walletAddress = "wallet3"
        )

        invoiceRepository.save(invoice1)
        invoiceRepository.save(invoice2)
        invoiceRepository.save(invoice3)

        // When
        val result = invoiceRepository.findByFreelancerEmailOrderByCreatedAtDesc(freelancerEmail)

        // Then
        assertEquals(2, result.size)
        assertEquals("Client 2", result[0].clientName) // Most recent first
        assertEquals("Client 1", result[1].clientName)
    }

    @Test
    fun `findByWalletAddress should return invoice when exists`() {
        // Given
        val walletAddress = "0x1234567890abcdef1234567890abcdef12345678"
        val invoice = TestDataBuilder.invoice(
            walletAddress = walletAddress,
            clientName = "Test Client"
        )

        invoiceRepository.save(invoice)

        // When
        val result = invoiceRepository.findByWalletAddress(walletAddress)

        // Then
        assertNotNull(result)
        assertEquals(walletAddress, result?.walletAddress)
        assertEquals("Test Client", result?.clientName)
    }

    @Test
    fun `findByWalletAddress should return null when not exists`() {
        // Given
        val walletAddress = "0xnonexistent"

        // When
        val result = invoiceRepository.findByWalletAddress(walletAddress)

        // Then
        assertNull(result)
    }

    @Test
    fun `findByStatus should return invoices with specified status`() {
        // Given
        val pendingInvoice1 = TestDataBuilder.invoice(
            status = InvoiceStatus.PENDING,
            clientName = "Pending Client 1",
            walletAddress = "pending1"
        )
        val pendingInvoice2 = TestDataBuilder.invoice(
            status = InvoiceStatus.PENDING,
            clientName = "Pending Client 2",
            walletAddress = "pending2"
        )
        val paidInvoice = TestDataBuilder.invoice(
            status = InvoiceStatus.PAID,
            clientName = "Paid Client",
            walletAddress = "paid1"
        )

        invoiceRepository.save(pendingInvoice1)
        invoiceRepository.save(pendingInvoice2)
        invoiceRepository.save(paidInvoice)

        // When
        val pendingResults = invoiceRepository.findByStatus(InvoiceStatus.PENDING)
        val paidResults = invoiceRepository.findByStatus(InvoiceStatus.PAID)

        // Then
        assertEquals(2, pendingResults.size)
        assertEquals(1, paidResults.size)
        assertTrue(pendingResults.all { it.status == InvoiceStatus.PENDING })
        assertTrue(paidResults.all { it.status == InvoiceStatus.PAID })
    }

    @Test
    fun `findByFreelancerEmailAndStatus should return filtered invoices`() {
        // Given
        val freelancerEmail = "freelancer@test.com"
        val pendingInvoice = TestDataBuilder.invoice(
            freelancerEmail = freelancerEmail,
            status = InvoiceStatus.PENDING,
            clientName = "Pending Client",
            walletAddress = "pending"
        )
        val paidInvoice = TestDataBuilder.invoice(
            freelancerEmail = freelancerEmail,
            status = InvoiceStatus.PAID,
            clientName = "Paid Client",
            walletAddress = "paid"
        )
        val otherFreelancerInvoice = TestDataBuilder.invoice(
            freelancerEmail = "other@test.com",
            status = InvoiceStatus.PENDING,
            clientName = "Other Freelancer",
            walletAddress = "other"
        )

        invoiceRepository.save(pendingInvoice)
        invoiceRepository.save(paidInvoice)
        invoiceRepository.save(otherFreelancerInvoice)

        // When
        val result = invoiceRepository.findByFreelancerEmailAndStatus(freelancerEmail, InvoiceStatus.PENDING)

        // Then
        assertEquals(1, result.size)
        assertEquals("Pending Client", result[0].clientName)
        assertEquals(freelancerEmail, result[0].freelancerEmail)
        assertEquals(InvoiceStatus.PENDING, result[0].status)
    }

    @Test
    fun `should save and retrieve invoice with all fields`() {
        // Given
        val invoice = TestDataBuilder.invoice(
            freelancerEmail = "freelancer@test.com",
            clientName = "Test Client",
            clientEmail = "client@test.com",
            amount = BigDecimal("123.45"),
            description = "Test invoice description",
            walletAddress = "1234567890abcdef1234567890abcdef12345678",
            status = InvoiceStatus.PENDING
        )

        // When
        val savedInvoice = invoiceRepository.save(invoice)

        val retrievedInvoice = invoiceRepository.findById(savedInvoice.id!!).orElse(null)

        // Then
        assertNotNull(retrievedInvoice)
        assertEquals(invoice.freelancerEmail, retrievedInvoice.freelancerEmail)
        assertEquals(invoice.clientName, retrievedInvoice.clientName)
        assertEquals(invoice.clientEmail, retrievedInvoice.clientEmail)
        assertEquals(invoice.amount, retrievedInvoice.amount)
        assertEquals(invoice.description, retrievedInvoice.description)
        assertEquals(invoice.walletAddress, retrievedInvoice.walletAddress)
        assertEquals(invoice.status, retrievedInvoice.status)
        assertNotNull(retrievedInvoice.createdAt)
    }

    @Test
    fun `should enforce unique wallet address constraint`() {
        // Given
        val walletAddress = "1234567890abcdef1234567890abcdef12345678"
        val invoice1 = TestDataBuilder.invoice(
            walletAddress = walletAddress,
            freelancerEmail = "freelancer1@test.com"
        )
        val invoice2 = TestDataBuilder.invoice(
            walletAddress = walletAddress,
            freelancerEmail = "freelancer2@test.com"
        )

        // When & Then
        invoiceRepository.save(invoice1)
        
        assertThrows(Exception::class.java) {
            invoiceRepository.save(invoice2)
            invoiceRepository.flush()
        }
    }
}