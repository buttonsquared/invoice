package com.invoice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.invoice.TestDataBuilder
import com.invoice.dto.CreateInvoiceRequest
import com.invoice.dto.UpdateInvoiceStatusRequest
import com.invoice.entity.InvoiceStatus
import com.invoice.service.InvoiceService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc
class InvoiceControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var invoiceService: InvoiceService

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun invoiceService(): InvoiceService = mockk()
    }

    @Test
    @WithMockUser
    fun `createInvoice should return 201 with created invoice`() {
        // Given
        val request = TestDataBuilder.createInvoiceRequest(
            freelancerEmail = "freelancer@test.com",
            clientName = "Test Client",
            amount = BigDecimal("100.00"),
            description = "Test invoice"
        )
        
        val responseDto = com.invoice.dto.InvoiceResponse(
            id = 1,
            freelancerEmail = request.freelancerEmail,
            clientName = request.clientName,
            clientEmail = request.clientEmail,
            amount = request.amount,
            description = request.description,
            walletAddress = "0x1234567890abcdef1234567890abcdef12345678",
            status = InvoiceStatus.PENDING,
            createdAt = LocalDateTime.now(),
            paidAt = null,
            expiresAt = LocalDateTime.now().plusDays(30),
            paymentUrl = "http://localhost:3000/invoice/${UUID.randomUUID()}"
        )

        every { invoiceService.createInvoice(any()) } returns responseDto

        // When & Then
        mockMvc.perform(
            post("/api/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andDo(MockMvcResultHandlers.print())
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.freelancerEmail").value(request.freelancerEmail))
            .andExpect(jsonPath("$.clientName").value(request.clientName))
            .andExpect(jsonPath("$.amount").value("100.0"))
            .andExpect(jsonPath("$.description").value(request.description))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.walletAddress").exists())
            .andExpect(jsonPath("$.paymentUrl").exists())

        verify { invoiceService.createInvoice(any()) }
    }

    @Test
    @WithMockUser
    fun `createInvoice should return 400 for invalid request`() {
        // Given
        val invalidRequest = CreateInvoiceRequest(
            freelancerEmail = "invalid-email", // Invalid email
            clientName = "", // Empty name
            clientEmail = null,
            amount = BigDecimal("-10"), // Negative amount
            description = ""
        )

        // When & Then
        mockMvc.perform(
            post("/api/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser
    fun `getInvoice should return 200 with invoice when found`() {
        // Given
        val invoiceId = 1L
        val responseDto = com.invoice.dto.InvoiceResponse(
            id = invoiceId,
            freelancerEmail = "freelancer@test.com",
            clientName = "Test Client",
            clientEmail = "client@test.com",
            amount = BigDecimal("100.00"),
            description = "Test invoice",
            walletAddress = "0x1234567890abcdef1234567890abcdef12345678",
            status = InvoiceStatus.PENDING,
            createdAt = LocalDateTime.now(),
            paidAt = null,
            expiresAt = LocalDateTime.now().plusDays(30),
            paymentUrl = "http://localhost:3000/invoice/$invoiceId"
        )

        every { invoiceService.getInvoiceById(invoiceId) } returns responseDto

        // When & Then
        mockMvc.perform(get("/api/invoices/$invoiceId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(invoiceId.toString()))
            .andExpect(jsonPath("$.freelancerEmail").value("freelancer@test.com"))
            .andExpect(jsonPath("$.status").value("PENDING"))

        verify { invoiceService.getInvoiceById(invoiceId) }
    }

    @Test
    @WithMockUser
    fun `getInvoice should return 404 when not found`() {
        // Given
        val invoiceId = 1L
        every { invoiceService.getInvoiceById(invoiceId) } returns null

        // When & Then
        mockMvc.perform(get("/api/invoices/$invoiceId"))
            .andExpect(status().isNotFound)

        verify { invoiceService.getInvoiceById(invoiceId) }
    }

    @Test
    @WithMockUser
    fun `getInvoicesByFreelancer should return 200 with invoices list`() {
        // Given
        val freelancerEmail = "freelancer@test.com"
        val invoice1 = com.invoice.dto.InvoiceResponse(
            id = 1L,
            freelancerEmail = freelancerEmail,
            clientName = "Client 1",
            clientEmail = "client1@test.com",
            amount = BigDecimal("100.00"),
            description = "Invoice 1",
            walletAddress = "0x1234567890abcdef1234567890abcdef12345678",
            status = InvoiceStatus.PENDING,
            createdAt = LocalDateTime.now(),
            paidAt = null,
            expiresAt = LocalDateTime.now().plusDays(30),
            paymentUrl = "http://localhost:3000/invoice/${UUID.randomUUID()}"
        )
        val invoice2 = invoice1.copy(
            id = 1L,
            clientName = "Client 2",
            status = InvoiceStatus.PAID,
            paidAt = LocalDateTime.now()
        )

        every { invoiceService.getInvoicesByFreelancer(freelancerEmail) } returns listOf(invoice1, invoice2)

        // When & Then
        mockMvc.perform(
            get("/api/invoices")
                .param("freelancerEmail", freelancerEmail)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].clientName").value("Client 1"))
            .andExpect(jsonPath("$[1].clientName").value("Client 2"))

        verify { invoiceService.getInvoicesByFreelancer(freelancerEmail) }
    }

    @Test
    @WithMockUser
    fun `updateInvoiceStatus should return 200 with updated invoice`() {
        // Given
        val invoiceId = 1L
        val request = UpdateInvoiceStatusRequest(status = InvoiceStatus.PAID)
        val responseDto = com.invoice.dto.InvoiceResponse(
            id = invoiceId,
            freelancerEmail = "freelancer@test.com",
            clientName = "Test Client",
            clientEmail = "client@test.com",
            amount = BigDecimal("100.00"),
            description = "Test invoice",
            walletAddress = "0x1234567890abcdef1234567890abcdef12345678",
            status = InvoiceStatus.PAID,
            createdAt = LocalDateTime.now().minusHours(1),
            paidAt = LocalDateTime.now(),
            expiresAt = LocalDateTime.now().plusDays(30),
            paymentUrl = "http://localhost:3000/invoice/$invoiceId"
        )

        every { invoiceService.updateInvoiceStatus(invoiceId, InvoiceStatus.PAID) } returns responseDto

        // When & Then
        mockMvc.perform(
            put("/api/invoices/$invoiceId/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("PAID"))
            .andExpect(jsonPath("$.paidAt").exists())

        verify { invoiceService.updateInvoiceStatus(invoiceId, InvoiceStatus.PAID) }
    }

    @Test
    fun `updateInvoiceStatus should return 404 when invoice not found`() {
        // Given
        val invoiceId = 1L
        val request = UpdateInvoiceStatusRequest(status = InvoiceStatus.PAID)

        every { invoiceService.updateInvoiceStatus(invoiceId, InvoiceStatus.PAID) } returns null

        // When & Then
        val result = mockMvc.perform(

            put("/api/invoices/$invoiceId/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(user(User("test", "N/A", listOf<GrantedAuthority>())))
        ).andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNotFound)
            .andReturn()


        verify { invoiceService.updateInvoiceStatus(invoiceId, InvoiceStatus.PAID) }
    }
}