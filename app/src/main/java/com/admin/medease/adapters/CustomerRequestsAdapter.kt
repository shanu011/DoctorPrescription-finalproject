package com.admin.medease.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.admin.medease.R
import com.admin.medease.clickInterface.ClickInterface
import com.admin.medease.clickInterface.ClickType
import com.admin.medease.databinding.RequestListItemBinding
import com.admin.medease.models.PrescriptionModel


class CustomerRequestsAdapter(var context: Context, var arrayList: ArrayList<PrescriptionModel>, var clicklistener: ClickInterface):RecyclerView.Adapter<CustomerRequestsAdapter.ViewHolder>() {

    class ViewHolder(var binding: RequestListItemBinding):RecyclerView.ViewHolder(binding.root) {

//        fun bindData(categoriesModel:CategoriesModel,position: Int,clicklistener: ClickInterface,imageView: ImageView){
//            binding.categoriesModel=categoriesModel
//            binding.position=position
//            binding.clickListener=clicklistener
//
//
////            binding.imgDelete.setOnClickListener {
////                clicklistener.onClick(position, ClickType.Delete)
////            }
////            binding.imgCandle.setOnClickListener {
////                clicklistener.onClick(position,ClickType.img)
////            }
////            binding.tvsubcat.setOnClickListener {
////                clicklistener.onClick(position,ClickType.AddSub)
////            }
//        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CustomerRequestsAdapter.ViewHolder {
        val binding=RequestListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }



    override fun onBindViewHolder(holder: CustomerRequestsAdapter.ViewHolder, position: Int) {
        holder.apply {
            binding.tvcategory.setText(arrayList[position].customerProblems)
            println("CustomerRequest: ${arrayList[position]}")
            if (arrayList[position].doctorSolution==""){
                binding.tvStatus.setText("Pending")
            }
            else{
                binding.tvStatus.setText("Responded" )
                binding.tvStatus.setBackgroundColor(0xFF00FF00.toInt())
            }
            Glide
                .with(context)
                .load(arrayList[position].customerImage)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(binding.imgCustomerProblem)

            binding.llcatItemView.setOnClickListener {
                clicklistener.onClick(position, ClickType.ViewClick,binding.imgCustomerProblem)
            }

            binding.tvUpdate.setOnClickListener {
                clicklistener.onClick(position,ClickType.update,binding.imgUpdate)
            }
//            binding.imgCandle.setImageURI(Uri.parse(arrayList[position].categoryImgUri))
//            bindData(arrayList[position],position,clicklistener,binding.imgCandle)
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