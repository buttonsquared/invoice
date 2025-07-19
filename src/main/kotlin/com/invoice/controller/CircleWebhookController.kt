package com.invoice.controller

import com.invoice.dto.CircleWebhookPayload
import com.invoice.service.CircleWebhookService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/webhook")
class CircleWebhookController(
    private val circleWebhookService: CircleWebhookService
) {
    
    private val logger = LoggerFactory.getLogger(CircleWebhookController::class.java)
    
    @PostMapping("/circle")
    fun handleCircleWebhook(
        @RequestBody payload: CircleWebhookPayload,
        @RequestHeader("X-Circle-Signature", required = false) signature: String?
    ): ResponseEntity<String> {
        
        logger.info("Received Circle webhook: type=${payload.type}")
        
        return try {
            circleWebhookService.processWebhook(payload, signature)
            ResponseEntity.ok("Webhook processed successfully")
        } catch (e: Exception) {
            logger.error("Failed to process Circle webhook", e)
            ResponseEntity.badRequest().body("Failed to process webhook: ${e.message}")
        }
    }
}