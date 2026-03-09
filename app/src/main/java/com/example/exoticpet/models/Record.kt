package com.example.exoticpet.models

data class Record(
    var id: Int = 0,
    var date: String = "",
    var time: String = "",
    var type: String = "",
    var description: String = "",
    var suggestion: String = ""
)