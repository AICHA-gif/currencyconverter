package com.example.currencyconverter

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.currencyconverter.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // View Binding : accès direct aux vues sans findViewById
    private lateinit var binding: ActivityMainBinding

    // Taux fixes exprimés pour 1 EUR
    private val taux = mapOf(
        "EUR" to 1.0, "USD" to 1.17, "TND" to 3.40,
        "GBP" to 0.87, "JPY" to 172.50, "CAD" to 1.62
    )

    // Longueur maximale du montant saisi
    private val longueurMaxMontant = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Soulignement des TextView montant et résultat
        binding.tvMontant.paintFlags = binding.tvMontant.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.tvResultat.paintFlags = binding.tvResultat.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        configurerSpinners()
        configurerClavier()

        // Clic sur « Convertir »
        binding.btnConvertir.setOnClickListener { lancerConversion() }

        // Clic sur « Inverser »
        binding.btnInverser.setOnClickListener { inverserDevises() }
    }

    /** Remplit les deux Spinner depuis R.array.devises (USD par défaut en source, EUR en cible). */
    private fun configurerSpinners() {
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.devises, R.layout.item_spinner
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spDeviseSource.adapter = adapter
        binding.spDeviseCible.adapter = adapter

        binding.spDeviseSource.setSelection(adapter.getPosition("USD"))
        binding.spDeviseCible.setSelection(adapter.getPosition("EUR"))

        // BONUS : conversion automatique quand une devise change
        val ecouteur = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                // Ne reconvertit que si un résultat est déjà affiché
                if (binding.tvResultat.text.isNotEmpty()) lancerConversion()
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        binding.spDeviseSource.onItemSelectedListener = ecouteur
        binding.spDeviseCible.onItemSelectedListener = ecouteur
    }

    /** Relie chaque touche du clavier à son action. */
    private fun configurerClavier() {
        val touches = mapOf(
            binding.btn0 to "0", binding.btn1 to "1", binding.btn2 to "2",
            binding.btn3 to "3", binding.btn4 to "4", binding.btn5 to "5",
            binding.btn6 to "6", binding.btn7 to "7", binding.btn8 to "8",
            binding.btn9 to "9", binding.btnPoint to "."
        )
        for ((bouton, caractere) in touches) {
            bouton.setOnClickListener { ajouterCaractere(caractere) }
        }
        binding.btnC.setOnClickListener { effacer() }
    }

    /** Ajoute un caractère (chiffre ou point) au montant en respectant les règles de gestion. */
    private fun ajouterCaractere(c: String) {
        val montantActuel = binding.tvMontant.text.toString()

        // Un seul point décimal autorisé
        if (c == "." && montantActuel.contains(".")) return

        // Le montant initial « 0 » est remplacé par le premier chiffre tapé
        val nouveauMontant = if (montantActuel == "0" && c != ".") c else montantActuel + c

        // Limite de 10 caractères
        if (nouveauMontant.length > longueurMaxMontant) return

        binding.tvMontant.text = nouveauMontant
    }

    /** Touche C : remet le montant à « 0 » et vide le résultat. */
    private fun effacer() {
        binding.tvMontant.text = getString(R.string.montant_initial)
        binding.tvResultat.text = ""
    }

    /** Lit le montant et les devises, vérifie la saisie, puis affiche le résultat. */
    private fun lancerConversion() {
        val montant = binding.tvMontant.text.toString().toDoubleOrNull() ?: 0.0

        if (montant == 0.0) {
            Toast.makeText(this, R.string.erreur_montant, Toast.LENGTH_SHORT).show()
            return
        }

        val source = binding.spDeviseSource.selectedItem.toString()
        val cible = binding.spDeviseCible.selectedItem.toString()

        val resultat = convertir(montant, source, cible)
        // Locale.US pour garantir le point décimal quelle que soit la langue du téléphone
        binding.tvResultat.text = String.format(Locale.US, "%.2f", resultat)
    }

    /** Conversion via l'euro (devise pivot). */
    private fun convertir(montant: Double, source: String, cible: String): Double {
        if (source == cible) return montant // même devise : pas de calcul
        val montantEnEuro = montant / taux.getValue(source)
        return montantEnEuro * taux.getValue(cible)
    }

    /** Échange les deux devises puis relance la conversion si un résultat est affiché. */
    private fun inverserDevises() {
        val positionSource = binding.spDeviseSource.selectedItemPosition
        val positionCible = binding.spDeviseCible.selectedItemPosition

        binding.spDeviseSource.setSelection(positionCible)
        binding.spDeviseCible.setSelection(positionSource)

        if (binding.tvResultat.text.isNotEmpty()) lancerConversion()
    }
}