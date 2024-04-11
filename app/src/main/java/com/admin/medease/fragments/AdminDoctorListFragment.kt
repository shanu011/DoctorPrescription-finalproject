package com.admin.medease.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
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
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.admin.medease.Constants
import com.admin.medease.R
import com.admin.medease.activities.MainActivity
import com.admin.medease.adapters.DoctorsAdapter
import com.admin.medease.clickInterface.ClickInterface
import com.admin.medease.clickInterface.ClickType
import com.admin.medease.databinding.AddDoctorsDialogBinding
import com.admin.medease.databinding.FragmentAdminDoctorListBinding
import com.admin.medease.models.DoctorRegisterModel
import com.admin.medease.models.DoctorsModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar


class AdminDoctorListFragment : Fragment() {
    lateinit var binding: FragmentAdminDoctorListBinding
     val db = Firebase.firestore
    var collectionName = Constants.Doctors
    lateinit var dialogBinding: AddDoctorsDialogBinding
    private var storageRef = FirebaseStorage.getInstance()
    var doctorsList= arrayListOf<DoctorsModel>()
    var uriContent : Uri?= null
    var downloadUri: Uri?=null
    lateinit var doctorsAdapter: DoctorsAdapter
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var mainActivity: MainActivity
    var imgProfile: ImageView?=null
    var uriFilePath : String ?= null
    var speId=""
    var doctorAuthId=""
    var progressBar: ProgressBar?=null

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
            CropImageContractOptions(uri = null,
                cropImageOptions = CropImageOptions(
                    imageSourceIncludeCamera = false,
                    imageSourceIncludeGallery = true,
                ),)
        )
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // Use the returned uri.
            uriContent = result.uriContent
            uriFilePath = result.getUriFilePath(requireContext()) // optional usage
            dialogBinding.imgAddProfile.setImageURI(uriContent)
//            imgCandle?.setImageURI(uriContent)
        } else {
            // An error occurred.
            val exception = result.error
        }
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressBar = ProgressBar(requireContext(), null, android.R.attr.progressBarStyleSmall)

        mainActivity = activity as MainActivity
        dialogBinding = AddDoctorsDialogBinding.inflate(layoutInflater)

        arguments?.let {
            speId = it.getString(Constants.specializationId,"") ?:""
        }
        Log.e("categoryId"," ${speId}")
        Log.e("doctorList", "onCreate:$doctorsList", )

        db.collection(collectionName).whereEqualTo("doctorSpecId",speId).
        addSnapshotListener { snapshots, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            for (snapshot in snapshots!!.documentChanges) {
                val userModel = convertObject(snapshot.document)

                when (snapshot.type) {
                    DocumentChange.Type.ADDED -> {
                        userModel?.let { doctorsList.add(it) }
                        Log.e("", "userModelList ${doctorsList}")
                    }

                    DocumentChange.Type.MODIFIED -> {
                        userModel?.let {
                            var index = getIndex(userModel)
                            if (index > -1)
                                doctorsList.set(index, it)
                        }
                    }

                    DocumentChange.Type.REMOVED -> {
                        userModel?.let {
                            var index = getIndex(userModel)
                            if (index > -1)
                                doctorsList.removeAt(index)
                        }
                    }
                }
            }
            doctorsAdapter.notifyDataSetChanged()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentAdminDoctorListBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar=binding.pbar


        binding.fabAdd.setOnClickListener {
            showAddDoctorDialog()
        }
        doctorsAdapter= DoctorsAdapter(requireContext(),doctorsList,object : ClickInterface {
            override fun onClick(position: Int, clickType: ClickType?, imageView: ImageView): Boolean {
                when (clickType) {
                    ClickType.update->{
                        showAddDoctorDialog(position)
                    }
//                    ClickType.REGISTER->{
//                        mainActivity.navController.navigate(R.id.registerFragment, bundleOf(Constants.id to doctorsList[position].doctorId ))
//                    }
                    ClickType.Delete -> {
                        AlertDialog.Builder(requireContext()).apply {
                            setTitle(resources.getString(R.string.delete_alert))
                            setPositiveButton("Yes") { _, _ ->
                                //deleting the particular collection from firestore
                                db.collection(collectionName)
                                    .document(doctorsList[position].doctorId ?: "").delete()
                            }
                            setNegativeButton("No") { _, _ -> }
                            show()
                        }
                    }


                    else -> {}
                }
                return true
            }

            override fun view(position: Int,imageView: ImageView) {
                imgProfile=imageView
            }


        })
        layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCategory.layoutManager = layoutManager
        binding.recyclerCategory.adapter = doctorsAdapter

    }

    fun showAddDoctorDialog(position: Int =-1) {

        dialogBinding = AddDoctorsDialogBinding.inflate(layoutInflater)
        var dialog = Dialog(requireContext()).apply {
            setContentView(dialogBinding.root)
            dialogBinding.imgAddProfile.setOnClickListener {
                checkPermissions()
            }
            if(position>-1){
                dialogBinding.tvaddtitle.setText("Update")
                Glide
                    .with(context)
                    .load(doctorsList[position].docImage)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(dialogBinding.imgAddProfile)
            }else{
                dialogBinding.tvaddtitle.setText("Add")
            }
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            show()
        }

        dialogBinding.edtname.doOnTextChanged{ text, _, _, _ ->
            var textLength = text?.length ?: 0
            if (textLength > 0) {
                dialogBinding.tilName.isErrorEnabled = false
            } else {
                dialogBinding.tilName.isErrorEnabled = true
                dialogBinding.tilName.error = "Enter Name"
            }
        }
        dialogBinding.edtQualification.doOnTextChanged { text, _, _, _ ->
            var textLength = text?.length ?: 0
            if (textLength > 0) {
                dialogBinding.tilQualification.isErrorEnabled = false
            } else {
                dialogBinding.tilQualification.isErrorEnabled = true
                dialogBinding.tilQualification.error = "Enter Qualification"
            }
        }
        dialogBinding.edtExperience.doOnTextChanged { text, _, _, _ ->
            var textLength = text?.length ?: 0
            if (textLength > 0) {
                dialogBinding.tilExperience.isErrorEnabled = false
            } else {
                dialogBinding.tilExperience.isErrorEnabled = true
                dialogBinding.tilExperience.error = "Enter Experience"
            }
        }
        dialogBinding.edtSpecialization.doOnTextChanged { text, _, _, _ ->
            var textLength = text?.length ?: 0
            if (textLength > 0) {
                dialogBinding.tilSpecialization.isErrorEnabled = false
            } else {
                dialogBinding.tilSpecialization.isErrorEnabled = true
                dialogBinding.tilSpecialization.error = "Enter Specialization"
            }
        }
//        else{
//
//            updateData(position, downloadUri.toString(), dialog)
//        }
        dialogBinding.position=position
        if (position > -1) {
            dialogBinding.doctorModel = doctorsList[position]
        } else {
            dialogBinding.doctorModel = DoctorsModel()
        }

        dialogBinding.btnsave.setOnClickListener {
            progressBar?.visibility=View.VISIBLE

            if (dialogBinding.edtname.text.toString().isNullOrEmpty()) {
                dialogBinding.tilName.isErrorEnabled = true
                dialogBinding.tilName.error = "Enter Name"
            }
            else if (dialogBinding.edtQualification.text.toString().isNullOrEmpty()) {
                dialogBinding.tilQualification.isErrorEnabled = true
                dialogBinding.tilQualification.error = "Enter Qualification"
            }
            else if (dialogBinding.edtExperience.text.toString().isNullOrEmpty()) {
                dialogBinding.tilExperience.isErrorEnabled = true
                dialogBinding.tilExperience.error = "Enter Experience"
            }
            else if (dialogBinding.edtSpecialization.text.toString().isNullOrEmpty()) {
                dialogBinding.tilSpecialization.isErrorEnabled = true
                dialogBinding.tilSpecialization.error = "Enter punjabi Name"
            }
            else if(uriContent != null) {
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
                        System.out.println("position $position")
                        val mAuht = Firebase.auth

                        if(doctorAuthId!=""){
                            updateData(position, downloadUri.toString(), dialog,doctorAuthId)

                        }else{
                            mAuht.createUserWithEmailAndPassword( dialogBinding.edtEmail.text.toString(), dialogBinding.edtPassword.text.toString())
                                .addOnCompleteListener(requireActivity()) { task ->
                                    if (task.isSuccessful) {
                                        // Registration successful
                                        val user = mAuht.currentUser
                                        val registerModel = DoctorRegisterModel()
                                        registerModel.username = dialogBinding.edtname.text.toString()
                                        registerModel.useremail =dialogBinding.edtEmail.text.toString()
                                        registerModel.userauthId = user?.uid
                                        doctorAuthId= registerModel.userauthId.toString()
                                        registerModel.doctorSpecId=speId
                                        // Save user details to Firestore database
                                        db.collection("users").add(registerModel)
                                            .addOnCompleteListener { registrationTask ->
                                                if (registrationTask.isSuccessful) {
                                                    // Registration and data save successful
                                                    updateData(position, downloadUri.toString(), dialog,doctorAuthId)
                                                    Toast.makeText(requireContext(), "Registration Successful", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(requireContext(), "Registration error", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    } else {
                                        progressBar?.visibility=View.GONE

                                        // Registration failed
                                        // Handle error appropriately
                                    }
                                }
                        }

                        if(position >-1){
                            dialogBinding.imgAddProfile?.let { it1 ->
                                Glide
                                    .with(this)
                                    .load(doctorsList[position].docImage)
                                    .centerCrop()
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .into(it1)
                            }
                        }

                    }
                }
            }else{
                updateData(position, downloadUri.toString(), dialog,doctorAuthId)
            }


        }
    }

    fun updateData(position:Int = -1,docImage:String?="", dialog: Dialog,docAuthid:String?=""){
        var doctorModel = DoctorsModel (
            docName =   dialogBinding.edtname.text.toString(),
            docAuthId = docAuthid,
            docEmail = dialogBinding.edtEmail.text.toString(),
            docQualificatrion = dialogBinding.edtQualification.text.toString(),
            docExperience = dialogBinding.edtExperience.text.toString(),
            docSpecialization = dialogBinding.edtSpecialization.text.toString(),
            docImage = docImage,
            doctorSpecId = speId,


            )
        dialog.dismiss()
        if (position > -1) {
            db.collection(collectionName).document(doctorsList[position].doctorId ?: "").set(
                DoctorsModel(
                    docName =   dialogBinding.edtname.text.toString(),
                    docAuthId = docAuthid,
                    docEmail = dialogBinding.edtEmail.text.toString(),
                    docQualificatrion =   dialogBinding.edtQualification.text.toString(),
                    docExperience =   dialogBinding.edtExperience.text.toString(),
                    docSpecialization =   dialogBinding.edtSpecialization.text.toString(),
                    docImage = downloadUri.toString(),
                    doctorSpecId = speId
                    )
            )
            dialogBinding.imgAddProfile.setImageURI(downloadUri)
            doctorModel.doctorId = doctorsList[position].doctorId ?: ""
            db.collection(collectionName)
                .document(doctorsList[position].doctorId ?: "").set(
                    doctorModel
                ).addOnSuccessListener {
                    progressBar?.visibility=View.GONE
                }.addOnFailureListener {
                    Log.e("TAG", "Error $it")
                }
            dialog.dismiss()
        } else {
            //add in firestore

            db.collection(collectionName).add(
                doctorModel
            )
            dialog.dismiss()
        }
    }

    fun convertObject(snapshot: QueryDocumentSnapshot) : DoctorsModel?{
        val doctorModel:DoctorsModel? =
            snapshot.toObject(DoctorsModel::class.java)
        doctorModel?.doctorId = snapshot.id ?: ""
        return doctorModel
    }

    fun getIndex(doctorModel: DoctorsModel) : Int{
        var index = -1
        index = doctorsList.indexOfFirst { element ->
            element.doctorId?.equals(doctorModel.doctorId) == true
        }
        return index
    }

}