package com.example.exoticpet

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.exoticpet.models.Pet
import com.example.exoticpet.models.Record

class PetDatabase(context: Context) : SQLiteOpenHelper(context, "PetDB", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS pets")
        db.execSQL("DROP TABLE IF EXISTS records")
        onCreate(db)
    }

    // 获取宠物信息：没有数据时返回空对象，而不是返回演示数据
    fun getPet(): Pet {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM pets WHERE id = 1", null)
        val pet = Pet()

        if (cursor.moveToFirst()) {
            pet.id = cursor.getInt(0)
            pet.name = cursor.getString(1) ?: ""
            pet.species = cursor.getString(2) ?: ""
            pet.gender = cursor.getString(3) ?: ""
            pet.birthDate = cursor.getString(4) ?: ""
            pet.length = cursor.getDouble(5)
            pet.weight = cursor.getDouble(6)
            pet.specialMark = cursor.getString(7) ?: ""
            pet.enclosureSize = cursor.getString(8) ?: ""
            pet.stapleFood = cursor.getString(9) ?: ""
            pet.healthScore = cursor.getInt(10)
            pet.lastCheckup = cursor.getString(11) ?: ""
        }

        cursor.close()
        return pet
    }

    // 更新宠物信息：如果本地没有这条记录，就直接插入
    fun updatePet(pet: Pet): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", 1)
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

        return db.insertWithOnConflict(
            "pets",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        ) != -1L
    }

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