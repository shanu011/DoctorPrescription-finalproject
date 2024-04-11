package com.admin.medease.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.admin.medease.R
import com.admin.medease.clickInterface.ClickInterface
import com.admin.medease.clickInterface.ClickType
import com.admin.medease.databinding.DoctorListItemBinding
import com.admin.medease.models.DoctorsModel

class DoctorsAdapter (var context: Context, var arrayList: ArrayList<DoctorsModel>, var clicklistener: ClickInterface
//,var imgset:imageSetting
): RecyclerView.Adapter<DoctorsAdapter.ViewHolder>() {

    class ViewHolder(var binding: DoctorListItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun bindData(
            doctorsModel: DoctorsModel,
            position: Int,
            clicklistener: ClickInterface,
            imageView: ImageView
        ){
            binding.doctorModel=doctorsModel
            binding.position=position
            binding.clickListener=clicklistener
            binding.llItemview.setOnClickListener {
                clicklistener.onClick(position, ClickType.ViewClick,imageView)
            }
            binding.tvupdate.setOnClickListener {
                clicklistener.onClick(position, ClickType.update,imageView)
            }
            binding.imgDelete.setOnClickListener {
                clicklistener.onClick(position, ClickType.Delete,imageView)
            }


        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DoctorsAdapter.ViewHolder {
        val binding= DoctorListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            binding.tvname.setText(arrayList[position].docName)
            binding.tvQualification.setText(arrayList[position].docQualificatrion)
            binding.tvExperience.setText("${ arrayList[position].docExperience } yrs Experience")
            binding.tvSpecialization.setText(arrayList[position].docSpecialization)
            Glide
                .with(context)
                .load(arrayList[position].docImage)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(binding.imgProfile)
//            binding.imgCandle.setImageURI(Uri.parse(arrayList[position].categoryImgUri))
            bindData(arrayList[position],position,clicklistener,binding.imgProfile)
//            imgset.setImage(position,binding.imgCandle)
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

//    interface imageSetting {
//        fun setImage(position: Int,imageView: ImageView)
//    }
}