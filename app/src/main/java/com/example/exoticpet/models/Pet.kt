package com.example.exoticpet.models

data class Pet(
    var id: Int = 1,
    var name: String = "橙橙",
    var species: String = "鬃狮蜥",
    var gender: String = "♂",
    var birthDate: String = "2025-09-01",
    var length: Double = 28.0,
    var weight: Double = 125.0,
    var specialMark: String = "无",
    var enclosureSize: String = "60*45*45cm",
    var stapleFood: String = "杜比亚蟑螂",
    var healthScore: Int = 92,
    var lastCheckup: String = "无异常"
)