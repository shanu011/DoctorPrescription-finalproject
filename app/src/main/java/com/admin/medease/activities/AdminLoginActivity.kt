package com.admin.medease.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.admin.medease.databinding.ActivityAdminLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AdminLoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityAdminLoginBinding
    var mAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAdminLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnsave.setOnClickListener {
            if (binding.edtemail.text.isNullOrEmpty()) {
                binding.tilemail.isErrorEnabled = true
                binding.tilemail.error = "Enter Email"
            } else  if (!isEmailValid(binding.edtemail.text.toString())) {
                binding.tilemail.isErrorEnabled = true
                binding.tilemail.error = "Enter Valid Email"
            } else
                if (binding.edtPassword.text.isNullOrEmpty()) {
                binding.tilPassword.isErrorEnabled = true
                binding.tilPassword.error = "Enter password"
            } else {
                binding.tilPassword.isErrorEnabled = false
                binding.tilemail.isErrorEnabled = false
                mAuth.signInWithEmailAndPassword(
                    binding.edtemail.text.toString(),
                    binding.edtPassword.text.toString()
                ).addOnCompleteListener { loginTask ->
                    if (loginTask.isSuccessful) {
                        // Login successful
//                binding.llProgressBar.visibility = View.GONE
                        Snackbar.make(
                            binding.edtemail,
                            "Login Successfully",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        this.finish()
                        println("Login Successfully")
                    } else {
                        // Login failed
                        Snackbar.make(
                            binding.edtemail,
                            "Login Error",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        println("Login Error: ${loginTask.exception}")
                    }
                }
            }
        }
        binding.tvForgetPassword.setOnClickListener {
            if(binding.edtemail.text.isNullOrEmpty()){
                binding.tilemail.error = "Enter Email"
            }else  if (!isEmailValid(binding.edtemail.text.toString())) {
                binding.tilemail.isErrorEnabled = true
                binding.tilemail.error = "Enter Valid Email"
            }else{
                mAuth.sendPasswordResetEmail(binding.edtemail.text.toString()).addOnCompleteListener {
                    if(it.isSuccessful){
                        Toast.makeText(this,"Mail Sent", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this,"NetWork Issue", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun isEmailValid(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})")
        return emailRegex.matches(email)
    }
}