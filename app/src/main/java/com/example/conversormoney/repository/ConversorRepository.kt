package com.example.conversormoney.repository

import com.example.conversormoney.model.remote.ServiceApi
import com.example.conversormoney.util.Constants
import javax.inject.Inject
import javax.inject.Named

class ConversorRepository @Inject constructor(
    @Named("mainServiceApi") private val api: ServiceApi

) {
    suspend fun getConversor(fromCurrency: String, toCurrency: String, amount: Double, date: String? = null) = api.getConversor(
        accessKey = Constants.API_KEY,
        from = fromCurrency,
        to = toCurrency,
        amount = amount,
      //  date = date
    )

}
