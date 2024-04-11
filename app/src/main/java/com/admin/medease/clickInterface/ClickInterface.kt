package com.admin.medease.clickInterface

import android.widget.ImageView

interface ClickInterface {
    fun onClick(position: Int, clickType: ClickType ?= ClickType.Delete,imageView: ImageView) :Boolean
    fun view(position: Int,imageView: ImageView)
}

enum class ClickType{
   Delete,ViewClick,REGISTER, update,Add
}