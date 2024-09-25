package com.example.conversormoney.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.conversormoney.R
import com.example.conversormoney.model.Country
import com.squareup.picasso.Picasso

class CurrencyAdapter(
    context: Context,
    private val countries: List<Country>
) : ArrayAdapter<Country>(context, 0, countries) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)

        val country = countries[position]
        val flagImageView = view.findViewById<ImageView>(R.id.imgFlag1) // Imagem da bandeira
        val nameTextView = view.findViewById<TextView>(R.id.txtNameFlag1) // Nome da moeda ou país

        // Obter a bandeira
        val flagUrl = country.flags.png // Usar o URL da bandeira PNG
        Picasso.get()
            .load(flagUrl)
            .into(flagImageView)

        // Obter a primeira moeda disponível
        val currency = country.currencies.values.firstOrNull()
        nameTextView.text = " ${currency?.symbol ?: ""} (${country.name.common})" // Exibir nome do país e símbolo da moeda

        return view
    }
}
