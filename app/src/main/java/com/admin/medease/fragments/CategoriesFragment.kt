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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.admin.medease.Constants
import com.admin.medease.R
import com.admin.medease.activities.MainActivity
import com.admin.medease.adapters.SpecializationAdapter
import com.admin.medease.clickInterface.ClickInterface
import com.admin.medease.clickInterface.ClickType
import com.admin.medease.databinding.AddCategoryDialogBinding
import com.admin.medease.databinding.FragmentCategoriesBinding
import com.admin.medease.models.CategoriesModel
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar


class CategoriesFragment : Fragment() {
    lateinit var binding:FragmentCategoriesBinding
    lateinit var mainActivity: MainActivity
    //    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var categoriesAdapter: SpecializationAdapter
    var categoriesList= arrayListOf<CategoriesModel>()
    val db = Firebase.firestore
    private var storageRef = FirebaseStorage.getInstance()
    var collectionName = Constants.categories
    var uriFilePath : String ?= null
    var uriContent : Uri?= null
    var downloadUri: Uri?=null
    var adapterPosition=-1
    var imgCandle: ImageView?=null
    lateinit var dialogBinding : AddCategoryDialogBinding

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
            dialogBinding.imgAddCandle.setImageURI(uriContent)
//            imgCandle?.setImageURI(uriContent)
        } else {
            // An error occurred.
            val exception = result.error
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity=activity as MainActivity
        db.collection(collectionName).addSnapshotListener{snapshots,e->
            if (e != null){
                return@addSnapshotListener
            }
            for (snapshot in snapshots!!.documentChanges) {
                val userModel = convertObject( snapshot.document)

                when (snapshot.type) {
                    DocumentChange.Type.ADDED -> {
                        userModel?.let { categoriesList.add(it) }
                        Log.e("", "userModelList ${categoriesList.size}")
                    }
                    DocumentChange.Type.MODIFIED -> {
                        userModel?.let {
                            var index = getIndex(userModel)
                            if (index > -1)
                                categoriesList.set(index, it)
                        }
                    }
                    DocumentChange.Type.REMOVED -> {
                        userModel?.let {
                            var index = getIndex(userModel)
                            if (index > -1)
                                categoriesList.removeAt(index)
                        }
                    }
                }
            }
            categoriesAdapter.notifyDataSetChanged()
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentCategoriesBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        categoriesAdapter= SpecializationAdapter(requireContext(),categoriesList,object : ClickInterface {
            override fun onClick(
                position: Int,
                clickType: ClickType?,
                imageView: ImageView
            ): Boolean {
                when (clickType) {
                    ClickType.Add->{
                        mainActivity.navController.navigate(R.id.adminDoctorListFragment, bundleOf(Constants.specializationId to categoriesList[position].categoryId ))
                    }
                    ClickType.Delete -> {
                        AlertDialog.Builder(requireContext()).apply {
                            setTitle(resources.getString(R.string.delete_alert))
                            setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                                //deleting the particular collection from firestore
                                db.collection(collectionName)
                                    .document(categoriesList[position].categoryId ?: "").delete()
                            }
                            setNegativeButton(resources.getString(R.string.no)) { _, _ -> }
                            show()
                        }
                    }

                    ClickType.ViewClick->{
                        showAddCategoryDialog(position)
                    }
                    else -> {}
                }
                return true            }

            override fun view(position: Int,imageView: ImageView) {
                imgCandle=imageView

            }

        })
//        layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCategory.layoutManager = GridLayoutManager(context,2)
        binding.recyclerCategory.adapter = categoriesAdapter

        Log.e("categoriesList", " $categoriesList", )


        binding.fabAdd.setOnClickListener {
            showAddCategoryDialog()
        }

    }

    fun showAddCategoryDialog(position: Int =-1) {

        dialogBinding = AddCategoryDialogBinding.inflate(layoutInflater)
        var dialog = Dialog(requireContext()).apply {
            setContentView(dialogBinding.root)
            if(position>-1){
                dialogBinding.tvaddtitle.setText("Update")
            }else{
                dialogBinding.tvaddtitle.setText("Add")
            }
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            show()
        }

        dialogBinding.imgAddCandle.setOnClickListener {
            checkPermissions()
        }

        dialogBinding.edtitems.doOnTextChanged { text, _, _, _ ->
            var textLength = text?.length ?: 0
            if (textLength > 0) {
                dialogBinding.tilitemName.isErrorEnabled = false
            } else {
                dialogBinding.tilitemName.isErrorEnabled = true
                dialogBinding.tilitemName.error = "Enter Category"
            }
        }
        dialogBinding.position=position
        if (position > -1) {
            dialogBinding.categoriesModel = categoriesList[position]
        } else {
            dialogBinding.categoriesModel = CategoriesModel()
        }
        if(position >-1){
            Glide
                .with(this)
                .load(categoriesList[position].categoryImgUri)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(dialogBinding.imgAddCandle)
        }
//        if (position==-1){
//            imgCandle?.let {
//                Glide
//                    .with(this)
//                    .load(categoriesList[position].categoryImgUri)
//                    .centerCrop()
//                    .placeholder(R.drawable.candle)
//                    .into(it)
//            }
//        }
        dialogBinding.btnsave.setOnClickListener {

            if (dialogBinding.edtitems.text.toString().isNullOrEmpty()) {
                dialogBinding.tilitemName.isErrorEnabled = true
                dialogBinding.tilitemName.error = "Enter Name"
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
                    System.out.println("in on complete listener")
                    if (task.isSuccessful){
                        downloadUri = task.result
                        System.out.println("in on complete listener ${downloadUri.toString()}")
                        System.out.println("position $position")
                        updateData(position, downloadUri.toString(), dialog)
                        if(position >-1){
                            imgCandle?.let { it1 ->
                                Glide
                                    .with(this)
                                    .load(categoriesList[position].categoryImgUri)
                                    .centerCrop()
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .into(it1)
                            }
                        }
//                        imgCandle?.setImageURI(downloadUri)

                    }
                }
            }else{
                updateData(position, downloadUri.toString(), dialog)
            }



        }
    }

    fun updateData(position:Int = -1, imageUrl: String = "", dialog: Dialog){

        var categoriesModel =  CategoriesModel(
            dialogBinding.edtitems.text.toString(),
            imageUrl,
        )
        dialog.dismiss()

        if (position > -1) {
            db.collection(collectionName).document(categoriesList[position].categoryId ?: "").set(
                CategoriesModel(
                    categoryName =  dialogBinding.edtitems.text.toString(),
                    categoryImgUri  = downloadUri.toString(),
                    categoryId =categoriesList[position].categoryId ?: ""
                )
            )
            dialogBinding.imgAddCandle.setImageURI(downloadUri)
            categoriesModel.categoryId = categoriesList[position].categoryId ?: ""
            db.collection(collectionName)
                .document(categoriesList[position].categoryId ?: "").set(
                    categoriesModel
                )
            dialog.dismiss()
        } else {
            //add in firestore

            db.collection(collectionName).add(
                categoriesModel
            )
            dialog.dismiss()
        }
    }


    fun convertObject(snapshot: QueryDocumentSnapshot) : CategoriesModel?{
        val categoriesModel:CategoriesModel? =
            snapshot.toObject(CategoriesModel::class.java)
        categoriesModel?.categoryId = snapshot.id ?: ""
        return categoriesModel
    }

    fun getIndex(categoriesModel:CategoriesModel) : Int{
        var index = -1
        index = categoriesList.indexOfFirst { element ->
            element.categoryId?.equals(categoriesModel.categoryId) == true
        }
        return index
    }


}