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
