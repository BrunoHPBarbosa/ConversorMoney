package com.example.conversormoney.view.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.conversormoney.model.ConversorResponse
import com.example.conversormoney.repository.ConversorRepository
import com.example.conversormoney.resource.ResourceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ConversorRepository
) : ViewModel() {

    private val _amount = MutableStateFlow<ResourceState<ConversorResponse>>(ResourceState.Empty())
    val amount: StateFlow<ResourceState<ConversorResponse>> = _amount

    private var fromCurrency: String = ""
    private var toCurrency: String = ""

    fun updateFromCurrency(currency: String) {
        fromCurrency = currency
        validateAndFetchAmount()
    }

    fun updateToCurrency(currency: String) {
        toCurrency = currency
        validateAndFetchAmount()
    }

    fun setAmount(amount: String) {
        val cleanString = amount.replace(",", ".").toDoubleOrNull() ?: 0.0
        if (cleanString > 0 && fromCurrency.isNotEmpty() && toCurrency.isNotEmpty()) {
            fetchAmount(fromCurrency, toCurrency, cleanString)
        } else {
            _amount.value = ResourceState.Empty()
        }
    }

    private fun validateAndFetchAmount() {
        // Verifica se todas as condições são atendidas para realizar a conversão
        if (fromCurrency.isNotEmpty() && toCurrency.isNotEmpty()) {
            fetchAmount(fromCurrency, toCurrency, 1.0)
        }
    }

    private fun fetchAmount(fromCurrency: String, toCurrency: String, amount: Double) {
        viewModelScope.launch {
            try {
                val response = repository.getConversor(fromCurrency, toCurrency, amount)
                _amount.value = handleResponseConversor(response)
            } catch (t: Throwable) {
                _amount.value = ResourceState.Error("Erro ao buscar dados: ${t.message}")
            }
        }
    }

    private fun handleResponseConversor(response: Response<ConversorResponse>): ResourceState<ConversorResponse> {
        if (response.isSuccessful) {
            response.body()?.let {
                return ResourceState.Success(it)
            } ?: return ResourceState.Error("Erro: Resposta vazia")
        }
        return ResourceState.Error("Erro na resposta: ${response.message()}")
    }
}
