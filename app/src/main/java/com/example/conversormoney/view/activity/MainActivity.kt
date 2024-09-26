package com.example.conversormoney.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.conversormoney.databinding.ActivityMainBinding
import com.example.conversormoney.model.Country
import com.example.conversormoney.resource.ResourceState
import com.example.conversormoney.view.adapter.CurrencyAdapter
import com.example.conversormoney.view.viewmodel.CountryViewModel
import com.example.conversormoney.view.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val viewModel: MainViewModel by viewModels()
    private val viewModelCountry: CountryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Configura os listeners para os spinners
        setupSpinnerListeners()

        // Observa os estados da API
        observeViewModel()

        // Observa o campo de texto do valor
        setupTextWatcher()

        // Chama a função para buscar países
        viewModelCountry.fetchCountries()
        observeCountries()
    }

    private fun setupSpinnerListeners() {
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCountry = parent.getItemAtPosition(position) as Country
                val fromCurrency = selectedCountry.currencies.keys.firstOrNull() ?: ""
                viewModel.updateFromCurrency(fromCurrency)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCountry = parent.getItemAtPosition(position) as Country
                val toCurrency = selectedCountry.currencies.keys.firstOrNull() ?: ""
                viewModel.updateToCurrency(toCurrency)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupTextWatcher() {
        binding.edtAmmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setAmount(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.amount.collect { resource ->
                when (resource) {
                    is ResourceState.Success -> {
                        val conversionResult = resource.data?.result
                        if (conversionResult?.rate != null) {
                            val amount = binding.edtAmmount.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
                            val convertedValue = amount * conversionResult.rate
                            binding.txtTotalConverted.text = String.format("%.2f", convertedValue).replace(".", ",")
                        }
                    }

                    is ResourceState.Error -> {
                        Toast.makeText(this@MainActivity, resource.message, Toast.LENGTH_SHORT).show()
                    }

                    is ResourceState.Loading -> TODO()


                    is ResourceState.Empty -> {
                        binding.txtTotalConverted.text = ""
                    }
                }
            }
        }
    }

    private fun observeCountries() {
        lifecycleScope.launch {
            viewModelCountry.countries.collect { resource ->
                when (resource) {
                    is ResourceState.Success -> {
                        val countries = resource.data ?: emptyList()
                        val adapter = CurrencyAdapter(this@MainActivity, countries)
                        binding.spinner.adapter = adapter
                        binding.spinner2.adapter = adapter
                    }

                    is ResourceState.Error -> {
                        Toast.makeText(this@MainActivity, resource.message, Toast.LENGTH_SHORT).show()
                    }

                    is ResourceState.Loading -> TODO()



                    is ResourceState.Empty -> TODO()
                }
            }
        }
    }
}
