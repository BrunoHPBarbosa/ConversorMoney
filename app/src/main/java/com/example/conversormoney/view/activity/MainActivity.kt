package com.example.conversormoney.view.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.conversormoney.R
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

    private var fromCurrency: String = ""
    private var toCurrency: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Log.d("MainActivity", "onCreate: Inicializando...")

        // Configura os listeners para os spinners
        binding.spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCountry = parent.getItemAtPosition(position) as Country
                fromCurrency = selectedCountry.currencies.keys.firstOrNull() ?: ""
                Log.d("MainActivity", "onItemSelected: fromCurrency = $fromCurrency")

                // Chama a função de conversão após selecionar uma nova moeda
                convertCurrency()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Log.d("MainActivity", "onNothingSelected: Nenhum país selecionado no spinner 1.")
            }
        })

        binding.spinner2.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCountry = parent.getItemAtPosition(position) as Country
                toCurrency = selectedCountry.currencies.keys.firstOrNull() ?: ""
                Log.d("MainActivity", "onItemSelected: toCurrency = $toCurrency")

                // Chama a função de conversão após selecionar uma nova moeda
                convertCurrency()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Log.d("MainActivity", "onNothingSelected: Nenhum país selecionado no spinner 2.")
            }
        })


        // Chama a função para buscar países
        viewModelCountry.fetchCountries()
        observeCountries()

        binding.edtAmmount.addTextChangedListener(object : TextWatcher {
            private var isFormatting: Boolean = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isFormatting) return

                isFormatting = true

                // Remove todos os caracteres não numéricos, exceto o ponto
                val cleanString = s.toString().replace("[^\\d]".toRegex(), "")

                // Converte o valor limpo para um número
                val parsedValue = cleanString.toDoubleOrNull() ?: 0.0

                // Formata o número com vírgula como separador decimal
                val formatted = String.format("%.2f", parsedValue / 100).replace(".", ",")

                // Atualiza o EditText com o valor formatado
                binding.edtAmmount.setText(formatted)
                binding.edtAmmount.setSelection(formatted.length) // Move o cursor para o final

                isFormatting = false

                // Chama a função de conversão aqui para atualizar sempre que o valor muda
                if (parsedValue > 0 && fromCurrency.isNotEmpty() && toCurrency.isNotEmpty()) {
                    Log.d("MainActivity", "Fetching amount from: $fromCurrency to: $toCurrency")
                    viewModel.fetchAmmount(
                        fromCurrency,
                        toCurrency,
                        parsedValue / 100
                    ) // Passa o valor dividido por 100
                } else {
                    binding.txtTotalConverted.text = ""
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

// No observer do valor convertido
        lifecycleScope.launch {
            viewModel.ammount.collect { resource ->
                when (resource) {
                    is ResourceState.Success -> {
                        Log.d("MainActivity", "Success: ${resource.data}")
                        val conversionResult = resource.data?.result
                        val rate = conversionResult?.rate // Obtenha a taxa de conversão
                        if (rate != null) {
                            // Obtém o valor a partir do EditText
                            val amount = binding.edtAmmount.text.toString().replace(",", ".")
                                .toDoubleOrNull() ?: 0.0
                            // Calcule o valor convertido
                            val convertedValue = amount * rate // Cálculo do valor convertido

                            // Arredonda o valor convertido para duas casas decimais
                            val roundedValue =
                                String.format("%.2f", convertedValue).replace(".", ",")
                            binding.txtTotalConverted.text = roundedValue
                        } else {
                            binding.txtTotalConverted.text = "Conversão não disponível"
                            Toast.makeText(
                                this@MainActivity,
                                "Conversão não disponível para as moedas selecionadas.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    is ResourceState.Error -> {
                        Log.e("MainActivity", "Error: ${resource.message}")
                        Toast.makeText(this@MainActivity, resource.message, Toast.LENGTH_SHORT)
                            .show()
                    }

                    is ResourceState.Loading -> {
                        Log.d("MainActivity", "Loading...")
                    }

                    is ResourceState.Empty -> {
                        Log.d("MainActivity", "Empty: Nenhum resultado encontrado.")
                    }
                }
            }
        }
    }

    // Função para realizar a conversão
    private fun convertCurrency() {
        // Obtém o valor do EditText e o formata
        val amount =
            binding.edtAmmount.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0

        // Chama a função de conversão se o valor e as moedas forem válidos
        if (amount > 0 && fromCurrency.isNotEmpty() && toCurrency.isNotEmpty()) {
            Log.d("MainActivity", "Converting amount from: $fromCurrency to: $toCurrency")
            viewModel.fetchAmmount(fromCurrency, toCurrency, amount) // Passa o valor correto
        } else {
            binding.txtTotalConverted.text = ""
        }
    }

    // Observa a lista de países
        private fun observeCountries() {
            lifecycleScope.launch {
                viewModelCountry.countries.collect { resource ->
                    when (resource) {
                        is ResourceState.Success -> {
                            Log.d("MainActivity", "Success: Países recebidos")
                            val countries = resource.data ?: emptyList()
                            val adapter = CurrencyAdapter(this@MainActivity, countries)
                            binding.spinner.adapter = adapter
                            binding.spinner2.adapter = adapter
                        }

                        is ResourceState.Error -> {
                            Log.e("MainActivity", "Error: ${resource.message}")
                            Toast.makeText(this@MainActivity, resource.message, Toast.LENGTH_SHORT)
                                .show()
                        }

                        is ResourceState.Empty -> {
                            Log.d("MainActivity", "Empty: Nenhuma moeda encontrada.")
                            Toast.makeText(
                                this@MainActivity,
                                "Nenhuma moeda encontrada.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is ResourceState.Loading -> {
                            Log.d("MainActivity", "Loading... Carregando lista de países.")
                        }
                    }
                }
            }
        }
    }

