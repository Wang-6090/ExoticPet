package com.example.exoticpet.models

data class Pet(
    var id: Int = 1,
    var name: String = "",
    var species: String = "",
    var gender: String = "",
    var birthDate: String = "",
    var length: Double = 0.0,
    var weight: Double = 0.0,
    var specialMark: String = "",
    var enclosureSize: String = "",
    var stapleFood: String = "",
    var healthScore: Int = 0,
    var lastCheckup: String = ""
)