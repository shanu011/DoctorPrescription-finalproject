package com.admin.medease.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.admin.medease.Constants
import com.admin.medease.databinding.FragmentRegisterBinding
import com.admin.medease.models.DoctorRegisterModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class RegisterFragment : Fragment() {
   lateinit var binding: FragmentRegisterBinding
    val db = Firebase.firestore
    var docid=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            docid=it.getString(Constants.id,"")
        }
        Log.e("doctorid", "onCreate: $docid", )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentRegisterBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return(binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mAuht = Firebase.auth
        binding.btnsave.setOnClickListener {
            mAuht.createUserWithEmailAndPassword( binding.edtemail.text.toString(), binding.edtPassword.text.toString())
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Registration successful
                        val user = mAuht.currentUser
                        val registerModel = DoctorRegisterModel()
                        registerModel.username = binding.edtitems.text.toString()
                        registerModel.useremail = binding.edtemail.text.toString()
                        registerModel.userauthId = user?.uid
                        registerModel.doctorSpecId=docid
                        // Save user details to Firestore database
                        db.collection("users").add(registerModel)
                            .addOnCompleteListener { registrationTask ->
                                if (registrationTask.isSuccessful) {
                                    // Registration and data save successful
                                    Toast.makeText(requireContext(), "Registration Successful", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(requireContext(), "Registration error", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        // Registration failed
                        // Handle error appropriately
                    }
                }
        }
    }
}