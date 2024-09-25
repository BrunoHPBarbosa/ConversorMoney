package com.example.conversormoney.repository

import com.example.conversormoney.model.remote.ServiceApi
import javax.inject.Inject
import javax.inject.Named

class FlagsRepository@Inject constructor(
    @Named("flagsServiceApi") private val flagsApi: ServiceApi
) {
    suspend fun getCountry()= flagsApi.getCountries()
}
