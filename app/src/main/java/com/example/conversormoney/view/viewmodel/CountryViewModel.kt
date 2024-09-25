package com.example.conversormoney.view.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.conversormoney.model.Country
import com.example.conversormoney.repository.FlagsRepository
import com.example.conversormoney.resource.ResourceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class CountryViewModel @Inject constructor(
    private val flagsRepository: FlagsRepository
): ViewModel() {
    private val _countries = MutableStateFlow<ResourceState<List<Country>>>(ResourceState.Loading())
    val countries: StateFlow<ResourceState<List<Country>>> = _countries

    fun fetchCountries() {
        viewModelScope.launch {
            try {
                val response = flagsRepository.getCountry()
                _countries.value = handleResponseCountries(response)
            } catch (t: Throwable) {
                _countries.value = ResourceState.Error("Erro ao buscar países: ${t.message}")
                Log.e("MainViewModel", "Erro ao buscar países: ${t.message}", t)
            }
        }
    }

    // Função para tratar a resposta da lista de países
    private fun handleResponseCountries(response: Response<List<Country>>): ResourceState<List<Country>> {
        return if (response.isSuccessful) {
            response.body()?.let { ResourceState.Success(it) }
                ?: ResourceState.Error("Corpo da resposta vazio")
        } else {
            ResourceState.Error(response.message())
        }
    }
}
