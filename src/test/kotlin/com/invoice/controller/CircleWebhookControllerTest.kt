package com.invoice.controller

import com.invoice.dto.*
import com.invoice.service.CircleWebhookService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity

class CircleWebhookControllerTest {

    private lateinit var circleWebhookService: CircleWebhookService
    private lateinit var circleWebhookController: CircleWebhookController

    @BeforeEach
    fun setUp() {
        circleWebhookService = mockk()
        circleWebhookController = CircleWebhookController(circleWebhookService)
    }

    @Test
    fun `should process valid Circle webhook`() {
        val payload = CircleWebhookPayload(
            type = "transfer",
            data = WebhookData(
                id = "transfer-123",
                source = TransferSource(type = "wallet"),
                destination = TransferDestination(type = "blockchain", address = "0x1234"),
                amount = WebhookAmount(amount = "50.00", currency = "USD"),
                status = "complete",
                createDate = "2024-01-01T12:00:00Z"
            )
        )

        every { circleWebhookService.processWebhook(any(), any()) } returns Unit

        val result = circleWebhookController.handleCircleWebhook(payload, "valid-signature")

        assertEquals(ResponseEntity.ok("Webhook processed successfully"), result)
        verify { circleWebhookService.processWebhook(payload, "valid-signature") }
    }

    @Test
    fun `should handle webhook processing errors`() {
        val payload = CircleWebhookPayload(
            type = "transfer",
            data = WebhookData(
                id = "transfer-123",
                source = TransferSource(type = "wallet"),
                destination = TransferDestination(type = "blockchain", address = "0x1234"),
                amount = WebhookAmount(amount = "50.00", currency = "USD"),
                status = "complete",
                createDate = "2024-01-01T12:00:00Z"
            )
        )

        every { circleWebhookService.processWebhook(any(), any()) } throws RuntimeException("Processing failed")

        val result = circleWebhookController.handleCircleWebhook(payload, null)

        assertEquals(400, result.statusCode.value())
        assertEquals("Failed to process webhook: Processing failed", result.body)
    }
}