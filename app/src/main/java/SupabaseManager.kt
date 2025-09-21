package com.example.essence

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

object SupabaseManager {

    val supabase: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = "https://zpwdakbyvqudpyaujkbe.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inpwd2Rha2J5dnF1ZHB5YXVqa2JlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTc5OTE3MTcsImV4cCI6MjA3MzU2NzcxN30.hg1ISfcCVFq7yu3uPajH7bL3FyGi64mj4CV7P76jsB0"
        ) {
            install(Storage)
        }
    }
}

