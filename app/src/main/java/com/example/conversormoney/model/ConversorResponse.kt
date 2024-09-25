package com.example.conversormoney.model

data class ConversorResponse(
    val base: String,
    val amount: Double,
    val result: ConversionResult,
    val ms: Double
)

data class ConversionResult(
    val rate: Double,
    val converted: Map<String, Double>
)

