package com.admin.medease.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.admin.medease.Constants
import com.admin.medease.R
import com.admin.medease.activities.DoctorPrescriptionActivity
import com.admin.medease.adapters.CustomerRequestsAdapter
import com.admin.medease.clickInterface.ClickInterface
import com.admin.medease.clickInterface.ClickType
import com.admin.medease.databinding.FragmentCustomerRequestBinding
import com.admin.medease.models.PrescriptionModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CustomerRequestFragment : Fragment() {
    lateinit var binding: FragmentCustomerRequestBinding
    lateinit var doctorPrescriptionActivity:DoctorPrescriptionActivity
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var categoriesAdapter: CustomerRequestsAdapter
    var prescriptionModelList= arrayListOf<PrescriptionModel>()
    val db = Firebase.firestore
    var mAuth = Firebase.auth
    var doctorAuthId = ""

    var collectionName = Constants.prescription

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        doctorPrescriptionActivity = activity as DoctorPrescriptionActivity

        val currentUser = mAuth.currentUser
        doctorAuthId = currentUser?.uid.toString()
        db.collection(Constants.prescription).whereEqualTo("doctorAuthId",doctorAuthId).addSnapshotListener{ snapshot, e->

            if (e != null){
                println("SnapShotListener Error: ${e.message}")
                return@addSnapshotListener
            }
            for (snapshot in snapshot!!.documentChanges) {

                val userModel = convertObject( snapshot.document)
                println("SnapShotListener IN LOOP")
                when (snapshot.type) {
                    DocumentChange.Type.ADDED -> {
                        userModel?.let { prescriptionModelList.add(it) }
                        Log.e("", "userModelList ${prescriptionModelList}")
                        Log.e("", "userModelListadded ${userModel}")
                        categoriesAdapter.notifyDataSetChanged()
                    }
                    DocumentChange.Type.MODIFIED -> {
                        userModel?.let {
                            var index = getIndex(userModel)
                            if (index > -1)
                                prescriptionModelList.set(index, it)
                            categoriesAdapter.notifyDataSetChanged()
                        }
                    }
                    DocumentChange.Type.REMOVED -> {
                        userModel?.let {
                            var index = getIndex(userModel)
                            if (index > -1)
                                prescriptionModelList.removeAt(index)
                            categoriesAdapter.notifyDataSetChanged()
                        }
                    }
                }

            }
        }


    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentCustomerRequestBinding.inflate(layoutInflater)
        return (binding.root)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoriesAdapter= CustomerRequestsAdapter(requireContext(),prescriptionModelList,object :
            ClickInterface {
            override fun onClick(
                position: Int,
                clickType: ClickType?,
                imageView: ImageView
            ): Boolean {
                when (clickType) {
                    ClickType.update->{
                        doctorPrescriptionActivity.docnavController.navigate(R.id.requestUpdateFragment, bundleOf(Constants.CustomerAuthId to prescriptionModelList[position].customerAuthId,Constants.presId to prescriptionModelList[position].prescriptionId ))

                    }
                    ClickType.ViewClick->{
                        doctorPrescriptionActivity.docnavController.navigate(R.id.requestDetailsFragment2, bundleOf(Constants.CustomerAuthId to prescriptionModelList[position].customerAuthId,Constants.presId to prescriptionModelList[position].prescriptionId ))

//                        showAddCategoryDialog(position)
                    }
                    else -> {}
                }
                return true
            }

            override fun view(position:Int,imageView: ImageView) {
                imageView?.let { it1 ->
                    Glide
                        .with(requireContext())
                        .load(Uri.parse(prescriptionModelList[position].customerImage))
                        .centerCrop()
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(it1)
                }
            }


        })
//        layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCategory.layoutManager = GridLayoutManager(context,2)
        binding.recyclerCategory.adapter = categoriesAdapter
        Log.e("categoriesList", "onCreate: ${PrescriptionModel()}", )

    }
    fun convertObject(snapshot: QueryDocumentSnapshot) : PrescriptionModel?{
        val prescriptionModel:PrescriptionModel? =
            snapshot.toObject(PrescriptionModel::class.java)
        prescriptionModel?.prescriptionId = snapshot.id ?: ""
        return prescriptionModel
    }

    fun getIndex(prescriptionModel:PrescriptionModel) : Int{
        var index = -1
        index = prescriptionModelList.indexOfFirst { element ->
            element.prescriptionId?.equals(prescriptionModel.prescriptionId) == true
        }
        return index
    }
}