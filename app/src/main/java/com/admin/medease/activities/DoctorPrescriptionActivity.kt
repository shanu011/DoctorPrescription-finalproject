package com.admin.medease.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.admin.medease.R
import com.admin.medease.databinding.ActivityDoctorPrescriptionBinding
import com.google.firebase.auth.FirebaseAuth

class DoctorPrescriptionActivity : AppCompatActivity(){
lateinit var binding: ActivityDoctorPrescriptionBinding
lateinit var docnavController: NavController
    lateinit var mainmenu: Unit
    private lateinit var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityDoctorPrescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        docnavController = findNavController(R.id.fragment)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        return super.onCreateOptionsMenu(menu)
        mainmenu=menuInflater.inflate(R.menu.menu_main, menu)
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.logout->{
                mAuth.signOut()
                // Redirect to LoginActivity
                startActivity(Intent(this, DoctorLoginActivity::class.java))
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun logout() {
        mAuth.signOut()
        // Redirect to LoginActivity
        startActivity(Intent(this, DoctorLoginActivity::class.java))
        finish() // Close MainActivity
        // Clear any saved user authentication state


    }
}