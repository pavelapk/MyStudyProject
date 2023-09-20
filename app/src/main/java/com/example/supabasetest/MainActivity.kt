package com.example.supabasetest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.supabasetest.databinding.ActivityMainBinding
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.handleDeeplinks
import io.github.jan.supabase.gotrue.providers.Google
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val client = createSupabaseClient(
            supabaseUrl = "https://jbtsntmpothujgkizkxr.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpidHNudG1wb3RodWpna2l6a3hyIiwicm9sZSI6ImFub24iLCJpYXQiOjE2OTUxODc1NDMsImV4cCI6MjAxMDc2MzU0M30.t0Y644UqU-_rVATKV_aGv2hrNMHjMIcNfcUDbFLvRh0"
        ) {
            install(GoTrue) {// Auth
                host = "login" // deeplink from manifest
                scheme = "com.example.supabasetest" // deeplink from manifest
            }
        }
        client.handleDeeplinks(intent)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            lifecycleScope.launch {
                login(client.gotrue)
            }
        }

        binding.logoutButton.setOnClickListener {
            lifecycleScope.launch {
                logout(client.gotrue)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                observeSession(client.gotrue)
            }
        }
    }

    private suspend fun login(gotrue: GoTrue) {
        gotrue.loginWith(Google)
    }

    private suspend fun logout(gotrue: GoTrue) {
        gotrue.logout()
    }

    private suspend fun observeSession(gotrue: GoTrue) {
        gotrue.sessionStatus.collect {
            when (it) {
                is SessionStatus.Authenticated -> {
                    binding.textView.text = gotrue.currentSessionOrNull()?.user?.email ?: "no"
                }

                SessionStatus.LoadingFromStorage -> {
                    binding.textView.text = "loading"
                }

                SessionStatus.NetworkError -> {
                    binding.textView.text = "network error"
                }

                SessionStatus.NotAuthenticated -> {
                    binding.textView.text = "no auth"
                }
            }
        }
    }
}
