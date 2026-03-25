package com.example.exoticpet

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.exoticpet.models.Pet
import com.example.exoticpet.models.Record

class PetDatabase(context: Context) : SQLiteOpenHelper(context, "PetDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        // 创建宠物表
        db.execSQL("""
            CREATE TABLE pets (
                id INTEGER PRIMARY KEY,
                name TEXT,
                species TEXT,
                gender TEXT,
                birthDate TEXT,
                length REAL,
                weight REAL,
                specialMark TEXT,
                enclosureSize TEXT,
                stapleFood TEXT,
                healthScore INTEGER,
                lastCheckup TEXT
            )
        """)

        // 创建记录表
        db.execSQL("""
            CREATE TABLE records (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT,
                time TEXT,
                type TEXT,
                description TEXT,
                suggestion TEXT
            )
        """)

        // 插入默认数据
        insertDefaultPet(db)
        insertSampleRecords(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS pets")
        db.execSQL("DROP TABLE IF EXISTS records")
        onCreate(db)
    }

    private fun insertDefaultPet(db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put("id", 1)
            put("name", "橙橙")
            put("species", "鬃狮蜥")
            put("gender", "♂")
            put("birthDate", "2025-09-01")
            put("length", 28.0)
            put("weight", 125.0)
            put("specialMark", "无")
            put("enclosureSize", "60*45*45cm")
            put("stapleFood", "杜比亚蟑螂")
            put("healthScore", 92)
            put("lastCheckup", "无异常")
        }
        db.insert("pets", null, values)
    }

    private fun insertSampleRecords(db: SQLiteDatabase) {
        val records = listOf(
            ContentValues().apply {
                put("date", "2026-03-01")
                put("time", "14:30")
                put("type", "蜕皮")
                put("description", "完成一次完整蜕皮，腹部残留少量甲片")
                put("suggestion", "保持湿度50%以上")
            },
            ContentValues().apply {
                put("date", "2026-02-15")
                put("time", "09:20")
                put("type", "异常")
                put("description", "拒食2天，精神萎靡")
                put("suggestion", "升温至30℃，补充钙粉")
            }
        )

        records.forEach { record ->
            db.insert("records", null, record)
        }
    }

    // 获取宠物信息
    fun getPet(): Pet {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM pets WHERE id = 1", null)
        val pet = Pet()

        if (cursor.moveToFirst()) {
            pet.name = cursor.getString(1)
            pet.species = cursor.getString(2)
            pet.gender = cursor.getString(3)
            pet.birthDate = cursor.getString(4)
            pet.length = cursor.getDouble(5)
            pet.weight = cursor.getDouble(6)
            pet.specialMark = cursor.getString(7)
            pet.enclosureSize = cursor.getString(8)
            pet.stapleFood = cursor.getString(9)
            pet.healthScore = cursor.getInt(10)
            pet.lastCheckup = cursor.getString(11)
        }
        cursor.close()
        return pet
    }

    // 更新宠物信息
    fun updatePet(pet: Pet): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", pet.name)
            put("species", pet.species)
            put("gender", pet.gender)
            put("birthDate", pet.birthDate)
            put("length", pet.length)
            put("weight", pet.weight)
            put("specialMark", pet.specialMark)
            put("enclosureSize", pet.enclosureSize)
            put("stapleFood", pet.stapleFood)
            put("healthScore", pet.healthScore)
            put("lastCheckup", pet.lastCheckup)
        }
        return db.update("pets", values, "id = 1", null) > 0
    }

    // 添加记录
    fun addRecord(record: Record): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("date", record.date)
            put("time", record.time)
            put("type", record.type)
            put("description", record.description)
            put("suggestion", record.suggestion)
        }
        return db.insert("records", null, values)
    }

    // 获取所有记录
    fun getAllRecords(): List<Record> {
        val records = mutableListOf<Record>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM records ORDER BY date DESC, time DESC", null)

        while (cursor.moveToNext()) {
            val record = Record(
                id = cursor.getInt(0),
                date = cursor.getString(1),
                time = cursor.getString(2),
                type = cursor.getString(3),
                description = cursor.getString(4),
                suggestion = cursor.getString(5)
            )
            records.add(record)
        }
        cursor.close()
        return records
    }

    // 按类型筛选记录
    fun getRecordsByType(type: String): List<Record> {
        if (type == "全部") return getAllRecords()

        val records = mutableListOf<Record>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM records WHERE type = ? ORDER BY date DESC, time DESC",
            arrayOf(type)
        )

        while (cursor.moveToNext()) {
            val record = Record(
                id = cursor.getInt(0),
                date = cursor.getString(1),
                time = cursor.getString(2),
                type = cursor.getString(3),
                description = cursor.getString(4),
                suggestion = cursor.getString(5)
            )
            records.add(record)
        }
        cursor.close()
        return records
    }
}