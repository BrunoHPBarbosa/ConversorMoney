package com.example.conversormoney.view.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.conversormoney.model.ConversorResponse
import com.example.conversormoney.model.Country
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
):ViewModel() {

    private val _ammount =
        MutableStateFlow<ResourceState<ConversorResponse>>(ResourceState.Loading())
    val ammount: StateFlow<ResourceState<ConversorResponse>> = _ammount


    fun fetchAmmount(fromCurrency: String, toCurrency: String, amount: Double) {
        viewModelScope.launch {
            try {
                val response = repository.getConversor(fromCurrency, toCurrency, amount)
                Log.d("MainViewModel", "Resposta da API: $response")
                _ammount.value = handleResponseSeries(response)
            } catch (t: Throwable) {
                _ammount.value = ResourceState.Error("Erro ao buscar dados: ${t.message}")
                Log.e("MainViewModel", "Erro ao buscar dados: ${t.message}", t)
            }
        }
    }

    // Função para tratar a resposta da série
    private fun handleResponseSeries(response: Response<ConversorResponse>): ResourceState<ConversorResponse> {
        if (response.isSuccessful) {
            response.body()?.let {
                Log.d("MainViewModel", "Corpo da resposta: $it") // Adicione este log
                if (it.result != null) {
                    return ResourceState.Success(it)
                } else {
                    return ResourceState.Error("Resultado da conversão não disponível")
                }
            } ?: return ResourceState.Error("Corpo da resposta vazio")
        } else {
            Log.e("MainViewModel", "Erro na resposta: ${response.code()} ${response.message()}")
            return ResourceState.Error("Erro na resposta da API: ${response.message()}")
        }
    }
}

