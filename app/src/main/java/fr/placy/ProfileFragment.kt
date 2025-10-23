   package fr.placy

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import fr.placy.repository.ProfileRepository
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

   private lateinit var fullNameView: TextView
   private lateinit var usernameView: TextView
   private lateinit var emailView: TextView
   private lateinit var phoneView: TextView
   private lateinit var bioView: TextView
   private lateinit var statusView: TextView
   private lateinit var logoutBtn: LinearLayout

   override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
   ): View? {
      val view = inflater.inflate(R.layout.fragment_profile, container, false)

      // Récupération des vues
      fullNameView = view.findViewById(R.id.textFullName)
      usernameView = view.findViewById(R.id.textUsername)
      emailView = view.findViewById(R.id.textEmail)
      phoneView = view.findViewById(R.id.textPhone)
      bioView = view.findViewById(R.id.textBio)
      statusView = view.findViewById(R.id.textStatus)
      logoutBtn = view.findViewById(R.id.btnLogout)

      loadUserProfile()
      setupLogout()

      return view
   }

   private fun loadUserProfile() {
      viewLifecycleOwner.lifecycleScope.launch {
         val profile = ProfileRepository.getCurrentUserProfile()
         if (profile != null) {
            fullNameView.text = profile.full_name ?: "Nom non défini"
            usernameView.text = "@${profile.username ?: "anonyme"}"
            emailView.text = profile.id ?: "Non renseigné"
            phoneView.text = "+33 7 58 40 21 29" // exemple statique
            bioView.text = profile.bio ?: "Aucune bio disponible"
            statusView.text = profile.status_message ?: "Aucun statut"
         } else {
            fullNameView.text = "Utilisateur introuvable"
         }
      }
   }

   private fun setupLogout() {
      logoutBtn.setOnClickListener {
         viewLifecycleOwner.lifecycleScope.launch {
            fr.placy.SupabaseManager.supabase.auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
         }
      }
   }
}
