package com.admin.medease.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.admin.medease.databinding.ActivityLoginStatusBinding

class LoginStatusActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginStatusBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnAdmin.setOnClickListener {
            startActivity(Intent(this,AdminLoginActivity::class.java).putExtra("Status","0"))
        }

        binding.btnDoctor.setOnClickListener {
            startActivity(Intent(this,SplashActivity::class.java).putExtra("Status","1"))
        }

    }
}