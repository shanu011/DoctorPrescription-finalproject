package com.admin.medease.models

data class PrescriptionModel (
    var customerProblems:String?="",
    var customerAuthId:String?="",
    var doctorAuthId:String?="",
    var doctorSolution:String?="",
    var prescriptionId:String?="",
    var doctotrImage:String?="",
    var customerImage:String?="",
    var requestDate:String?="",
    var responseDate:String?="",


    )