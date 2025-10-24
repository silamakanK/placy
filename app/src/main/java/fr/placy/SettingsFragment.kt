package fr.placy

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import android.widget.Toast

class SettingsFragment : Fragment() {

    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        listView = view.findViewById(R.id.settingsListView)

        // Liste d’options
        val options = listOf(
            "Se connecter",
            "S'inscrire"
        )

        // Adapter simple pour la liste
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            options
        )

        listView.adapter = adapter

        // Gestion des clics sur les éléments
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> { // Se connecter
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    startActivity(intent)
                }
                1 -> { // S'inscrire
                    val intent = Intent(requireContext(), SignupActivity::class.java)
                    startActivity(intent)
                }
                else -> {
                    Toast.makeText(requireContext(), "Option inconnue", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }
}
