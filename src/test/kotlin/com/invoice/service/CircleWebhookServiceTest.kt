package com.invoice.service

import com.invoice.TestDataBuilder
import com.invoice.dto.*
import com.invoice.entity.InvoiceStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import java.math.BigDecimal

class CircleWebhookServiceTest {

    private lateinit var invoiceService: InvoiceService
    private lateinit var circleWebhookService: CircleWebhookService

    @BeforeEach
    fun setUp() {
        invoiceService = mockk()
        circleWebhookService = CircleWebhookService(invoiceService)
        ReflectionTestUtils.setField(circleWebhookService, "webhookSecret", "test-secret")
    }

    @Test
    fun `processWebhook should mark invoice as paid for successful transfer`() {
        // Given
        val walletAddress = "0x1234567890abcdef1234567890abcdef12345678"
        val invoice = TestDataBuilder.invoice(
            walletAddress = walletAddress,
            amount = BigDecimal("50.00"),
            status = InvoiceStatus.PENDING
        ).copy(id = 1L)

        val payload = CircleWebhookPayload(
            type = "transfer",
            data = WebhookData(
                id = "transfer-123",
                source = TransferSource(type = "wallet", id = "source-wallet"),
                destination = TransferDestination(type = "blockchain", address = walletAddress),
                amount = WebhookAmount(amount = "50.00", currency = "USD"),
                status = "complete",
                createDate = "2024-01-01T12:00:00Z"
            )
        )

        every { invoiceService.findInvoiceByWalletAddress(walletAddress) } returns invoice
        every { invoiceService.updateInvoiceStatus(1L, InvoiceStatus.PAID) } returns TestDataBuilder.invoiceResponse()

        // When
        circleWebhookService.processWebhook(payload, "valid-signature")

        // Then
        verify { invoiceService.findInvoiceByWalletAddress(walletAddress) }
        verify { invoiceService.updateInvoiceStatus(1L, InvoiceStatus.PAID) }
    }

    @Test
    fun `processWebhook should not process incomplete transfers`() {
        // Given
        val payload = CircleWebhookPayload(
            type = "transfer",
            data = WebhookData(
                id = "transfer-123",
                source = TransferSource(type = "wallet"),
                destination = TransferDestination(type = "blockchain", address = "0x1234"),
                amount = WebhookAmount(amount = "50.00", currency = "USD"),
                status = "pending",
                createDate = "2024-01-01T12:00:00Z"
            )
        )

        // When
        circleWebhookService.processWebhook(payload, "valid-signature")

        // Then
        verify(exactly = 0) { invoiceService.findInvoiceByWalletAddress(any()) }
        verify(exactly = 0) { invoiceService.updateInvoiceStatus(any(), any()) }
    }

    @Test
    fun `processWebhook should not process payments with insufficient amount`() {
        // Given
        val walletAddress = "0x1234567890abcdef1234567890abcdef12345678"
        val invoice = TestDataBuilder.invoice(
            walletAddress = walletAddress,
            amount = BigDecimal("100.00"),
            status = InvoiceStatus.PENDING
        ).copy(id = 1L)

        val payload = CircleWebhookPayload(
            type = "transfer",
            data = WebhookData(
                id = "transfer-123",
                source = TransferSource(type = "wallet"),
                destination = TransferDestination(type = "blockchain", address = walletAddress),
                amount = WebhookAmount(amount = "50.00", currency = "USD"),
                status = "complete",
                createDate = "2024-01-01T12:00:00Z"
            )
        )

        every { invoiceService.findInvoiceByWalletAddress(walletAddress) } returns invoice

        // When
        circleWebhookService.processWebhook(payload, "valid-signature")

        // Then
        verify { invoiceService.findInvoiceByWalletAddress(walletAddress) }
        verify(exactly = 0) { invoiceService.updateInvoiceStatus(any(), any()) }
    }
}