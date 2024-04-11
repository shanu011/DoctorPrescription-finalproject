package com.admin.medease.fragments

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.admin.medease.Constants
import com.admin.medease.R
import com.admin.medease.databinding.FragmentRequestUpdateBinding
import com.admin.medease.models.CustomerRegisterModel
import com.admin.medease.models.PrescriptionModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RequestUpdateFragment : Fragment() {
    lateinit var binding:FragmentRequestUpdateBinding
    var collectionName = Constants.customers
    var Doctorimage=""
    val db = Firebase.firestore
    var mAuth = Firebase.auth
//    lateinit var prescriptionAdapter: PrescriptionAdapter
    private var storageRef = FirebaseStorage.getInstance()
    var uriContent : Uri?= null
    var downloadUri: Uri?=null
    var imgProfile: ImageView?=null
    var uriFilePath : String ?= null
    var prescriptionId=""
    var userauthId=""
    var DoctorAuthid=""
    var progressBar: ProgressBar?=null

    var preObjectModel:PrescriptionModel?=null
    private val TAG = FragmentRequestUpdateBinding::class.java.canonicalName
    private var mediaPermission = if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S)
        Manifest.permission.READ_EXTERNAL_STORAGE
    else{
        Manifest.permission.READ_MEDIA_IMAGES
    }

    private var getImagePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if(it)
            launchCropImage()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                mediaPermission
            ) != PackageManager.PERMISSION_GRANTED ) {
            getImagePermission.launch(mediaPermission)
        } else{
            launchCropImage()
        }
    }

    fun launchCropImage(){
        cropImage.launch(
            CropImageContractOptions(
                uri = null,
                cropImageOptions = CropImageOptions(
                    imageSourceIncludeCamera = false,
                    imageSourceIncludeGallery = true,
                ),
            )
        )
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // Use the returned uri.
            uriContent = result.uriContent
            uriFilePath = result.getUriFilePath(requireContext()) // optional usage
            binding.imgdoctor.setImageURI(uriContent)
        } else {
            // An error occurred.
            val exception = result.error
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progressBar = ProgressBar(requireContext(), null, android.R.attr.progressBarStyleSmall)
        progressBar?.visibility=View.GONE

        arguments?.let {
            userauthId= it.getString(Constants.CustomerAuthId,"") ?:""
            prescriptionId= it.getString(Constants.presId,"")?:""
        }
        Log.e("Id","CustomerAuthId: ${userauthId}")
        Log.e("Id","prescriptionId: ${prescriptionId}")

        val currentUser = mAuth.currentUser
        DoctorAuthid = currentUser?.uid.toString()

        db.collection(Constants.customers).whereEqualTo("userauthId",userauthId).addSnapshotListener{snapshots,e->
            if (e != null){
                return@addSnapshotListener
            }
            for (snapshot in snapshots!!.documentChanges) {
                val userModel = convertObject( snapshot.document)
                binding.tvName.setText(userModel?.username)
                binding.tvEmail.setText(userModel?.useremail)

            }

        }


        db.collection(Constants.prescription).document(prescriptionId)
            .addSnapshotListener{snapshots,e->
                if (e != null){
                    return@addSnapshotListener
                }
                var model = snapshots?.toObject(PrescriptionModel::class.java)
                binding.tvproblem.setText(model?.customerProblems)
                Glide
                    .with(requireContext())
                    .load(model?.customerImage)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(binding.imgCustomerProblem)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentRequestUpdateBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar=binding.pbar
        binding.customizechkbox.setOnCheckedChangeListener { buttonView, isChecked ->

            if (binding.customizechkbox.isChecked == false) {
                binding.llPrescription.visibility = View.GONE

            } else {
                binding.llPrescription.visibility = View.VISIBLE
            }
        }

        var cal = Calendar.getInstance()
        binding.tvResponseDate.text = SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(cal.time)
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd-MMM-yyyy" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            binding.tvResponseDate.text = sdf.format(cal.time)

        }
        binding.tvResponseDate.setOnClickListener {
            Toast.makeText(requireContext(), "datepicker clicked", Toast.LENGTH_SHORT).show()
            DatePickerDialog(requireContext(), dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        binding.imgdoctor.setOnClickListener {
            checkPermissions()
        }
        binding.btnAddPrescription.setOnClickListener {
            progressBar?.visibility=View.VISIBLE
            Toast.makeText(context, "requesting", Toast.LENGTH_SHORT).show()
            if (binding.edtSolutiontext.text.isNullOrEmpty()){
                binding.tilsolutionText.error="Enter Problem to request Permissiomn"
            }
            else if(uriContent != null) {
                Log.e(TAG, "onViewCreated: ", )
                val ref = storageRef.reference.child(Calendar.getInstance().timeInMillis.toString())
                var uploadTask = uriContent?.let { it1 -> ref.putFile(it1) }

                uploadTask?.continueWithTask { task ->
                    System.out.println("in task $task")
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    ref.downloadUrl
                }?.addOnCompleteListener { task ->
                    progressBar?.visibility=View.VISIBLE

                    System.out.println("in on complete listener")
                    if (task.isSuccessful){
                        downloadUri = task.result
                        System.out.println("in on complete listener ${downloadUri.toString()}")
                        binding.imgdoctor.setImageURI(downloadUri)
                        progressBar?.visibility = View.VISIBLE
                        updateData(downloadUri.toString())
                    }
                }
            } else{
                progressBar?.visibility = View.VISIBLE
                updateData()
            }
        }



    }
    private fun updateData(downloadUri: String ?= "") {

        Log.e("updateData", "updateData: ", )

        db.collection(Constants.prescription)
            .document(prescriptionId)
            .update("doctorSolution",binding.edtSolutiontext.text.toString(),"doctotrImage",downloadUri.toString(),"responseDate",binding.tvResponseDate.text.toString())
            .addOnCompleteListener { updateTask ->
                if (updateTask.isSuccessful) {
                    // Update successful
                    findNavController().navigate(R.id.customerRequestFragment)
                    Log.e("updateData", "updateData: successful", )

                    progressBar?.visibility = View.GONE
                } else {
                    // Update failed
                    // Handle the failure as needed
                    Log.e("updateData", "updateData: failed", )

                    progressBar?.visibility = View.GONE
                }
            }
    }

    fun convertObject(snapshot: QueryDocumentSnapshot) : CustomerRegisterModel?{
        val categoriesModel:CustomerRegisterModel? =
            snapshot.toObject(CustomerRegisterModel::class.java)
        categoriesModel?.userauthId = snapshot.id ?: ""
        return categoriesModel
    }

    fun convertprescriptionObject(snapshot: QueryDocumentSnapshot) : PrescriptionModel?{
        val prescriptionModel:PrescriptionModel? =
            snapshot.toObject(PrescriptionModel::class.java)
        prescriptionModel?.prescriptionId = snapshot.id ?: ""
        return prescriptionModel
    }
}