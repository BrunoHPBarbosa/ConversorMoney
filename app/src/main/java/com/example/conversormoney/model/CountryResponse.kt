package com.example.conversormoney.model

data class Country(
    val name: Name,
    val currencies: Map<String, Currency>,
    val flags: Flags
)

data class Name(
    val common: String,
    val official: String
)

data class Currency(
    val name: String,
    val symbol: String
)

data class Flags(
    val png: String,
    val svg: String
)
