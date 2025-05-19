package com.nabiilawidya.tehteksi.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nabiilawidya.tehteksi.R
import com.nabiilawidya.tehteksi.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding  // ViewBinding utama

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.layoutLogin.root.visibility = View.VISIBLE
        binding.layoutRegister.root.visibility = View.GONE

        binding.layoutLogin.cirLoginButton.setOnClickListener {
            // TODO: validasi dulu kalau perlu
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.layoutLogin.signupText.setOnClickListener {
            binding.layoutLogin.root.visibility = View.GONE
            binding.layoutRegister.root.visibility = View.VISIBLE
        }

        binding.layoutRegister.signintext.setOnClickListener {
            // TODO: logika pendaftaran di sini
            binding.layoutRegister.root.visibility = View.GONE
            binding.layoutLogin.root.visibility = View.VISIBLE
        }

        binding.layoutRegister.btnRegister.setOnClickListener {
            // TODO: logika pendaftaran di sini
            binding.layoutRegister.root.visibility = View.GONE
            binding.layoutLogin.root.visibility = View.VISIBLE
        }
    }
}
