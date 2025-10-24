   package fr.placy

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.DialogTitle
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import fr.placy.repository.ProfileRepository
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.UUID

   class ProfileFragment : Fragment() {

   private lateinit var fullNameView: TextView
   private lateinit var usernameView: TextView
   private lateinit var emailView: TextView
   private lateinit var phoneView: TextView
   private lateinit var bioView: TextView
   private lateinit var statusView: TextView
   private lateinit var logoutBtn: LinearLayout
   private lateinit var avatarImage: ImageView
   private lateinit var textInitial: TextView
   private lateinit var editAvatarBtn: ImageButton
   private lateinit var textFullNameTitle: TextView
   private val PICK_IMAGE_REQUEST = 1001
   private lateinit var uploadLoader: View



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
      avatarImage = view.findViewById(R.id.imageAvatar)
      textInitial = view.findViewById(R.id.textInitial)
      editAvatarBtn = view.findViewById(R.id.btnEditAvatar)
      uploadLoader = view.findViewById(R.id.uploadLoaderContainer)
      textFullNameTitle = view.findViewById(R.id.textFullNameTitle)


         //Gestion du bonton de logout
      logoutBtn.setOnClickListener {
         val builder = AlertDialog.Builder(requireContext())
         builder.setTitle("Déconnexion")
         builder.setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")

         builder.setPositiveButton("Déconnexion") { dialog, _ ->
            setupLogout()
            dialog.dismiss()
         }

         builder.setNegativeButton("Annuler") { dialog, _ ->
            dialog.dismiss()
         }

         builder.show()
      }

      // Gestion du clic sur l’avatar (changement d’image)
      editAvatarBtn.setOnClickListener {
         val intent = Intent(Intent.ACTION_PICK)
         intent.type = "image/*"
         startActivityForResult(intent, PICK_IMAGE_REQUEST)
      }

      loadUserProfile()

      return view
   }

   private fun loadUserProfile() {
      viewLifecycleOwner.lifecycleScope.launch {
         val profile = ProfileRepository.getCurrentUserProfile()
         if (profile != null) {
            fullNameView.text = profile.full_name ?: "Nom non défini"
            emailView.text = profile.id ?: "Non renseigné"
            phoneView.text = "+33 7 58 40 21 29"
            usernameView.text = profile.username ?: "username"
            bioView.text = profile.bio ?: "Aucune bio disponible"
            statusView.text = profile.status_message ?: "Aucun statut"
            textFullNameTitle.text = profile.full_name ?: "Nom non défini"

            val fullName = profile.full_name ?: "Utilisateur"
            val avatarUrl = profile.avatar_url ?: ""

            if (avatarUrl.isNotEmpty()) {
               // Si avatar existant → on charge l’image
               textInitial.visibility = View.GONE
               avatarImage.visibility = View.VISIBLE

               Glide.with(requireContext())
                  .load(avatarUrl)
                  .placeholder(R.drawable.ic_profile_foreground)
                  .circleCrop()
                  .into(avatarImage)
            } else {
               // Sinon on affiche l’initiale
               val initial = fullName.firstOrNull()?.uppercaseChar() ?: '?'
               textInitial.text = initial.toString()
               textInitial.visibility = View.VISIBLE
               avatarImage.visibility = View.GONE
            }
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

   // Sélection d’une nouvelle image
   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
      super.onActivityResult(requestCode, resultCode, data)
      if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
         val imageUri: Uri? = data.data
         if (imageUri != null) {
            uploadAvatarToSupabase(imageUri)
         }
      }
   }

   private fun uploadAvatarToSupabase(imageUri: Uri) {
      viewLifecycleOwner.lifecycleScope.launch {
         try {
            uploadLoader.visibility = View.VISIBLE // ⏳ afficher le loader

            val supabase = fr.placy.SupabaseManager.supabase
            val storage = supabase.storage.from("avatars")

            val inputStream: InputStream? =
               requireContext().contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()

            if (bytes != null) {
               val fileName = "avatar_${UUID.randomUUID()}.jpg"

               // 1️⃣ Upload dans Supabase Storage
               storage.upload(fileName, bytes)

               // 2️⃣ Récupération de l'URL publique
               val publicUrl = storage.publicUrl(fileName)

               // 3️⃣ Mise à jour du profil
               ProfileRepository.updateAvatarUrl(publicUrl)

               // 4️⃣ Mise à jour de l’affichage
               withContext(Dispatchers.Main) {
                  Glide.with(requireContext())
                     .load(publicUrl)
                     .circleCrop()
                     .into(avatarImage)
                  textInitial.visibility = View.GONE
                  avatarImage.visibility = View.VISIBLE
               }
            }

         } catch (e: Exception) {
            Log.e("SupabaseUpload", "Erreur upload avatar: ${e.message}")
         } finally {
            uploadLoader.visibility = View.GONE // ✅ cacher le loader
         }
      }
   }
}
