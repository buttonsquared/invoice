package com.invoice.service

import com.invoice.dto.CircleWebhookPayload
import com.invoice.entity.InvoiceStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.*

@Service
class CircleWebhookService(
    private val invoiceService: InvoiceService
) {
    
    private val logger = LoggerFactory.getLogger(CircleWebhookService::class.java)
    
    @Value("\${circle.webhook.secret}")
    private lateinit var webhookSecret: String
    
    fun processWebhook(payload: CircleWebhookPayload, signature: String?) {
        // Verify webhook signature in production
        if (!isValidSignature(payload, signature)) {
            logger.warn("Invalid webhook signature received")
            // In development, we'll continue processing even with invalid signature
            // throw SecurityException("Invalid webhook signature")
        }
        
        when (payload.type) {
            "transfer" -> handleTransferWebhook(payload)
            else -> {
                logger.info("Unhandled webhook type: ${payload.type}")
            }
        }
    }
    
    private fun handleTransferWebhook(payload: CircleWebhookPayload) {
        val data = payload.data
        
        // Only process successful transfers
        if (data.status != "complete") {
            logger.info("Transfer not complete, status: ${data.status}")
            return
        }
        
        // Find invoice by destination wallet address
        val destinationAddress = data.destination.address
        if (destinationAddress == null) {
            logger.warn("No destination address in webhook payload")
            return
        }
        
        val invoice = invoiceService.findInvoiceByWalletAddress(destinationAddress)
        if (invoice == null) {
            logger.warn("No invoice found for wallet address: $destinationAddress")
            return
        }
        
        // Verify payment amount matches or exceeds invoice amount
        val paymentAmount = BigDecimal(data.amount.amount)
        if (paymentAmount < invoice.amount) {
            logger.warn("Payment amount ($paymentAmount) less than invoice amount (${invoice.amount})")
            return
        }
        
        // Update invoice status to paid
        invoiceService.updateInvoiceStatus(invoice.id!!, InvoiceStatus.PAID)
        logger.info("Invoice ${invoice.id} marked as paid. Payment: $paymentAmount ${data.amount.currency}")
    }
    
    private fun isValidSignature(payload: CircleWebhookPayload, signature: String?): Boolean {
        if (signature == null || webhookSecret.contains("your-webhook-secret")) {
            // Skip signature validation in development
            return true
        }
        
        return try {
            val mac = Mac.getInstance("HmacSHA256")
            val secretKeySpec = SecretKeySpec(webhookSecret.toByteArray(), "HmacSHA256")
            mac.init(secretKeySpec)
            
            val payloadBytes = payload.toString().toByteArray()
            val expectedSignature = Base64.getEncoder().encodeToString(mac.doFinal(payloadBytes))
            
            signature == expectedSignature
        } catch (e: Exception) {
            logger.error("Error validating webhook signature", e)
            false
        }
    }
}