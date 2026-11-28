package com.example.habisin.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AiApi {
    @POST("ai/chat")
    suspend fun chat(
        @Body body: ChatRequest,
    ): ChatResponse

    @POST("ai/saran-harga")
    suspend fun suggestPrice(
        @Body body: PriceAiRequest,
    ): PriceAiResponse

    @POST("ai/deskripsi-produk")
    suspend fun productDescription(
        @Body body: DescriptionAiRequest,
    ): DescriptionAiResponse

    @GET("ai/ringkasan-penjualan")
    suspend fun salesSummary(): SalesSummaryResponse

    @POST("ai/cari")
    suspend fun smartSearch(
        @Body body: SmartSearchRequest,
    ): SmartSearchResponse

    @GET("ai/dampakku")
    suspend fun myImpact(): ImpactResponse
