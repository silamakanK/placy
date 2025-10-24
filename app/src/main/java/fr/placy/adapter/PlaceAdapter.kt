package fr.placy.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.placy.R
import fr.placy.model.Place

class PlaceAdapter(
    private val places: List<Place>,
    private val onClick: ((Place) -> Unit)? = null
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    inner class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameView: TextView = view.findViewById(R.id.placeName)
        val cityView: TextView = view.findViewById(R.id.placeCity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.nameView.text = place.name
        holder.cityView.text = place.city ?: place.category ?: "Inconnu"

        holder.itemView.setOnClickListener {
            onClick?.invoke(place)
        }
    }

    override fun getItemCount(): Int = places.size
}
