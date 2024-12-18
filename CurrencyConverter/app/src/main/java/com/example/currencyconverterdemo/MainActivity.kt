package com.example.currencyconverterdemo

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imgView: ImageView = findViewById(R.id.imgView)
        imgView.setImageResource(R.drawable.exchange);

        val spinnerIn: Spinner = findViewById(R.id.spinnerIn)
        val spinnerOut: Spinner = findViewById(R.id.spinnerOut)
        val textField: EditText = findViewById(R.id.textField)
        val outputField: TextView = findViewById(R.id.outputCurrency)

        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.optionsArr)
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerIn.adapter = adapter
            spinnerOut.adapter = adapter
        }
        spinnerOut.setSelection(1)

        textField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int)
            {
                convert(spinnerIn, spinnerOut, textField, outputField)
            }
            override fun afterTextChanged(editable: Editable?) {}
        })
    }

    private fun convert(spinnerIn: Spinner, spinnerOut: Spinner, textField: EditText, outputField: TextView) {
        val input = spinnerIn.selectedItem.toString()
        val output = spinnerOut.selectedItem.toString()

        if (input == output) {
            alert()
            outputField.text = ""
            return
        }

        val amountText = textField.text.toString()
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            outputField.text = "Ungültige Eingabe"
            return
        }

        val url = "https://api.frankfurter.app/latest?base=$input&symbols=$output"

        val queue = Volley.newRequestQueue(this)

        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val rates = jsonResponse.getJSONObject("rates")
                    if (rates.has(output)) {
                        val rate = rates.getDouble(output)
                        val result = amount * rate
                        outputField.text = String.format("%.2f %s", result, output)
                    } else {
                        outputField.text = "Umrechnung fehlgeschlagen"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    outputField.text = "Umrechnung fehlgeschlagen"
                }
            },
            { error ->
                error.printStackTrace()
                outputField.text = "API-Anfrage fehlgeschlagen"
            })
        queue.add(stringRequest)
    }

    /*
    private fun getRate(input: String, output: String): Double {
        return when ("$input $output") {
            "EUR EUR" -> 1.0
            "EUR USD" -> 1.05
            "EUR GBP" -> 0.83
            "EUR JPY" -> 161.44

            "USD EUR" -> 0.95
            "USD USD" -> 1.0
            "USD GBP" -> 0.79
            "USD JPY" -> 154.09

            "GBP EUR" -> 1.21
            "GBP USD" -> 1.27
            "GBP GBP" -> 1.0
            "GBP JPY" -> 195.09

            "JPY EUR" -> 0.0062
            "JPY USD" -> 0.0065
            "JPY GBP" -> 0.0051
            "JPY JPY" -> 1.0

            else -> 1.0
        }
    }
*/
    private fun alert() {
        val msg = AlertDialog.Builder(this)
        msg.setTitle("Ungültige Währungsauwahl")
        msg.setMessage("Zwei Mal dieselbe Währung ausgewählt.")
        msg.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        msg.show()
    }
}
