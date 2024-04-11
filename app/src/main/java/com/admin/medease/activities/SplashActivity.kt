package com.admin.medease.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Toast
import com.admin.medease.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {
    lateinit var binding: ActivitySplashBinding
    var mAuth = Firebase.auth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getSupportActionBar()?.hide()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        Handler(Looper.getMainLooper()).postDelayed(
            {
                mAuth = FirebaseAuth.getInstance()
                // Check if a user is currently logged in
//                val currentUser = mAuth.currentUser
//                if (currentUser != null) {
//                    // User is logged in, you can access the user details
//                    val userId = currentUser.uid
//                    val userEmail = currentUser.email
//                    // ... other user details
//                    startActivity(Intent(this,DoctorPrescriptionActivity::class.java))
//                    finish()
//
//                    // Example: Display a welcome message
//                    Toast.makeText(
//                        this,
//                        "Welcome, $userEmail!",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                } else {
                    // No user is currently logged in, redirect to the login screen
                    startActivity(Intent(this, DoctorLoginActivity::class.java))
                    finish() // Close MainActivity to prevent going back to it after login

//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//            finish()
            }, 3000)
    }


}
