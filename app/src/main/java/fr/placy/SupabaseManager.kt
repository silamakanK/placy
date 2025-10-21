package fr.placy

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseManager {
    val supabase: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://wfgoloaagkncifkmdgde.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndmZ29sb2FhZ2tuY2lma21kZ2RlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjEwMDcyNTUsImV4cCI6MjA3NjU4MzI1NX0.1-N-vFqbsnQQr3UCRdA098bmikn4j9V2i4G4dQhFI6Q"
    ) {
        install(Auth)
        install(Postgrest)
    }
}