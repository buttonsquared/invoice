package com.invoice.controller

import com.invoice.dto.CreateInvoiceRequest
import com.invoice.dto.InvoiceResponse
import com.invoice.dto.UpdateInvoiceStatusRequest
import com.invoice.service.InvoiceService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = ["http://localhost:3000"]) // For React frontend
class InvoiceController(
    private val invoiceService: InvoiceService
) {

    @PostMapping
    fun createInvoice(@Valid @RequestBody request: CreateInvoiceRequest): ResponseEntity<InvoiceResponse> {
        val invoice = invoiceService.createInvoice(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice)
    }

    @GetMapping("/{id}")
    fun getInvoice(@PathVariable id: Long): ResponseEntity<InvoiceResponse> {
        val invoice = invoiceService.getInvoiceById(id)
        return if (invoice != null) {
            ResponseEntity.ok(invoice)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping
    fun getInvoicesByFreelancer(@RequestParam freelancerEmail: String): ResponseEntity<List<InvoiceResponse>> {
        val invoices = invoiceService.getInvoicesByFreelancer(freelancerEmail)
        return ResponseEntity.ok(invoices)
    }

    @PutMapping("/{id}/status")
    fun updateInvoiceStatus(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateInvoiceStatusRequest
    ): ResponseEntity<InvoiceResponse> {
        val updatedInvoice = invoiceService.updateInvoiceStatus(id, request.status)
        return if (updatedInvoice != null) {
            ResponseEntity.ok(updatedInvoice)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}