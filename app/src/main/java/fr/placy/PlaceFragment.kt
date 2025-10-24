package fr.placy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.placy.adapter.PlaceAdapter
import fr.placy.repository.PlaceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaceFragment : Fragment() {

    private lateinit var searchInput: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var filterButton: ImageButton
    private lateinit var placesRecycler: RecyclerView
    private lateinit var recommendedRecycler: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_place, container, false)

        searchInput = view.findViewById(R.id.searchPlaceInput)
        searchButton = view.findViewById(R.id.searchButton)
        filterButton = view.findViewById(R.id.filterButton)
        placesRecycler = view.findViewById(R.id.placesRecycler)
        recommendedRecycler = view.findViewById(R.id.recommendedPlacesRecycler)

        placesRecycler.layoutManager = LinearLayoutManager(requireContext())
        recommendedRecycler.layoutManager = LinearLayoutManager(requireContext())

        // Chargement initial
        loadRecommendedPlaces()
        loadLimitedPlaces()

        // Bouton recherche
        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isEmpty()) {
                loadLimitedPlaces()
            } else {
                lifecycleScope.launch {
                    val results = PlaceRepository.searchPlaces(query)
                    withContext(Dispatchers.Main) {
                        placesRecycler.adapter = PlaceAdapter(results)
                    }
                }
            }
        }

        filterButton.setOnClickListener {
            Toast.makeText(requireContext(), "Filtres à venir 👀", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun loadLimitedPlaces() {
        lifecycleScope.launch {
            val places = PlaceRepository.getLimitedPlaces()
            withContext(Dispatchers.Main) {
                placesRecycler.adapter = PlaceAdapter(places)
            }
        }
    }

    private fun loadRecommendedPlaces() {
        lifecycleScope.launch {
            val recommended = PlaceRepository.getRecommendedPlaces()
            withContext(Dispatchers.Main) {
                recommendedRecycler.adapter = PlaceAdapter(recommended)
            }
        }
    }
}
