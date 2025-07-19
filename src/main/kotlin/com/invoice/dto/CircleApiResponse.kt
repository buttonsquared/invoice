package com.invoice.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class CreateWalletAddressRequest(
    val currency: String = "USD",
    val chain: String = "ETH"
)

data class CreateWalletAddressResponse(
    val data: WalletAddressData
)

data class WalletAddressData(
    val id: String,
    val address: String,
    val currency: String,
    val chain: String,
    val description: String? = null
)

data class CircleWebhookPayload(
    val type: String,
    val data: WebhookData
)

data class WebhookData(
    val id: String,
    val source: TransferSource,
    val destination: TransferDestination,
    val amount: WebhookAmount,
    val status: String,
    @JsonProperty("createDate") val createDate: String
)

data class TransferSource(
    val type: String,
    val id: String? = null
)

data class TransferDestination(
    val type: String,
    val address: String? = null,
    val id: String? = null
)

data class WebhookAmount(
    val amount: String,
    val currency: String
)