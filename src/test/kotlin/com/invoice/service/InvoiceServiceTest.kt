package com.invoice.service

import com.invoice.TestDataBuilder
import com.invoice.entity.InvoiceStatus
import com.invoice.repository.InvoiceRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class InvoiceServiceTest {

    private lateinit var invoiceRepository: InvoiceRepository
    private lateinit var circleApiService: CircleApiService
    private lateinit var invoiceService: InvoiceService

    @BeforeEach
    fun setUp() {
        invoiceRepository = mockk()
        circleApiService = mockk()
        invoiceService = InvoiceService(invoiceRepository, circleApiService)
        ReflectionTestUtils.setField(invoiceService, "frontendBaseUrl", "http://localhost:3000")
    }

    @Test
    fun `createInvoice should save invoice and return response`() {
        // Given
        val request = TestDataBuilder.createInvoiceRequest(
            freelancerEmail = "freelancer@test.com",
            clientName = "Test Client",
            amount = BigDecimal("50.00"),
            description = "Test description"
        )
        
        val savedInvoiceSlot = slot<com.invoice.entity.Invoice>()
        val mockSavedInvoice = TestDataBuilder.invoice(
            freelancerEmail = request.freelancerEmail,
            clientName = request.clientName,
            amount = request.amount,
            description = request.description,
            walletAddress = "1234567890abcdef1234567890abcdef12345678"
        )

        every { invoiceRepository.save(capture(savedInvoiceSlot)) } returns mockSavedInvoice
        coEvery { circleApiService.createWalletAddress(any()) } returns "1234567890abcdef1234567890abcdef12345678"

        // When
        val result = invoiceService.createInvoice(request)

        // Then
        verify { invoiceRepository.save(any()) }
        
        val capturedInvoice = savedInvoiceSlot.captured
        assertEquals(request.freelancerEmail, capturedInvoice.freelancerEmail)
        assertEquals(request.clientName, capturedInvoice.clientName)
        assertEquals(request.amount, capturedInvoice.amount)
        assertEquals(request.description, capturedInvoice.description)
        assertEquals(InvoiceStatus.PENDING, capturedInvoice.status)
        assertNotNull(capturedInvoice.walletAddress)
        assertNotNull(capturedInvoice.expiresAt)

        assertEquals(mockSavedInvoice.id, result.id)
        assertEquals(request.freelancerEmail, result.freelancerEmail)
        assertEquals("http://localhost:3000/invoice/${mockSavedInvoice.id}", result.paymentUrl)
    }

    @Test
    fun `getInvoiceById should return invoice when found`() {
        // Given
        val invoiceId = 1L
        val invoice = TestDataBuilder.invoice().copy(id = invoiceId)
        
        every { invoiceRepository.findById(invoiceId) } returns Optional.of(invoice)

        // When
        val result = invoiceService.getInvoiceById(invoiceId)

        // Then
        assertNotNull(result)
        assertEquals(invoiceId, result?.id)
        assertEquals(invoice.freelancerEmail, result?.freelancerEmail)
    }

    @Test
    fun `getInvoiceById should return null when not found`() {
        // Given
        val invoiceId = 1L
        every { invoiceRepository.findById(invoiceId) } returns Optional.empty()

        // When
        val result = invoiceService.getInvoiceById(invoiceId)

        // Then
        assertNull(result)
    }

    @Test
    fun `getInvoicesByFreelancer should return sorted invoices`() {
        // Given
        val freelancerEmail = "freelancer@test.com"
        val invoice1 = TestDataBuilder.invoice(
            freelancerEmail = freelancerEmail,
            createdAt = LocalDateTime.now().minusDays(1)
        )
        val invoice2 = TestDataBuilder.invoice(
            freelancerEmail = freelancerEmail,
            createdAt = LocalDateTime.now()
        )
        
        every { invoiceRepository.findByFreelancerEmailOrderByCreatedAtDesc(freelancerEmail) } returns listOf(invoice2, invoice1)

        // When
        val result = invoiceService.getInvoicesByFreelancer(freelancerEmail)

        // Then
        assertEquals(2, result.size)
        assertEquals(invoice2.id, result[0].id)
        assertEquals(invoice1.id, result[1].id)
    }

    @Test
    fun `updateInvoiceStatus should update status and set paidAt when PAID`() {
        // Given
        val invoiceId = 1L
        val originalInvoice = TestDataBuilder.invoice(
            status = InvoiceStatus.PENDING
        ).copy(id = invoiceId)
        val updatedInvoice = originalInvoice.copy(
            status = InvoiceStatus.PAID,
            paidAt = LocalDateTime.now()
        )
        
        every { invoiceRepository.findById(invoiceId) } returns Optional.of(originalInvoice)
        every { invoiceRepository.save(any()) } returns updatedInvoice

        // When
        val result = invoiceService.updateInvoiceStatus(invoiceId, InvoiceStatus.PAID)

        // Then
        assertNotNull(result)
        assertEquals(InvoiceStatus.PAID, result?.status)
        assertNotNull(result?.paidAt)
    }

    @Test
    fun `updateInvoiceStatus should return null when invoice not found`() {
        // Given
        val invoiceId = 1L
        every { invoiceRepository.findById(invoiceId) } returns Optional.empty()

        // When
        val result = invoiceService.updateInvoiceStatus(invoiceId, InvoiceStatus.PAID)

        // Then
        assertNull(result)
        verify(exactly = 0) { invoiceRepository.save(any()) }
    }

    @Test
    fun `findInvoiceByWalletAddress should return invoice when found`() {
        // Given
        val walletAddress = "1234567890abcdef1234567890abcdef12345678"
        val invoice = TestDataBuilder.invoice(walletAddress = "1234567890abcdef1234567890abcdef12345678")
        
        every { invoiceRepository.findByWalletAddress(walletAddress) } returns invoice

        // When
        val result = invoiceService.findInvoiceByWalletAddress(walletAddress)

        // Then
        assertNotNull(result)
        assertEquals(walletAddress, result?.walletAddress)
    }
}