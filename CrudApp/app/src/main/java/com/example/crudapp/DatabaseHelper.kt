package com.example.crudapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "students.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE Students(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, course TEXT, grade TEXT, email TEXT, phone TEXT UNIQUE, created_at DATETIME DEFAULT CURRENT_TIMESTAMP, updated_at DATETIME DEFAULT CURRENT_TIMESTAMP)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Students")
        onCreate(db)
    }

    fun insertItem(name: String, course: String, grade: String, email: String, phone: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("course", course)
            put("grade", grade)
            put("email", email)
            put("phone", phone)
        }
        db.insert("Students", null, values)
    }

    fun getAllItems(): List<String> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT name FROM Students", null) // Fetching 'name' instead of 'id'
        val items = mutableListOf<String>()
        while (cursor.moveToNext()) {
            items.add(cursor.getString(0)) // Now retrieving 'name'
        }
        cursor.close()
        return items
    }


    fun deleteItem(name: String) {
        val db = writableDatabase
        db.delete("Students", "name=?", arrayOf(name))
    }

    fun updateItem(oldPhone: String, name: String, course: String, grade: String, email: String, phone: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("course", course)
            put("grade", grade)
            put("email", email)
            put("phone", phone)
            put("updated_at", "CURRENT_TIMESTAMP")
        }
        db.update("Students", values, "phone=?", arrayOf(oldPhone)) // Update using phone number
    }

}

class ItemViewModel(private val dbHelper: DatabaseHelper) : ViewModel() {
    var items by mutableStateOf(listOf<String>())
        private set

    init { fetchItems() }

    fun fetchItems() {
        items = dbHelper.getAllItems()
    }

    fun addItem(name: String, course: String, grade: String, email: String, phone: String) {
        dbHelper.insertItem(name, course, grade, email, phone)
        fetchItems()
    }

    fun removeItem(name: String) {
        dbHelper.deleteItem(name)
        fetchItems()
    }

    fun updateItem(oldPhone: String, name: String, course: String, grade: String, email: String, phone: String) {
        dbHelper.updateItem(oldPhone, name, course, grade, email, phone)
        fetchItems() // Refresh UI
    }

}

@Composable
fun ItemScreen(viewModel: ItemViewModel) {
    var name by remember { mutableStateOf(TextFieldValue()) }
    var course by remember { mutableStateOf(TextFieldValue()) }
    var grade by remember { mutableStateOf(TextFieldValue()) }
    var email by remember { mutableStateOf(TextFieldValue()) }
    var phone by remember { mutableStateOf(TextFieldValue()) }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = course,
            onValueChange = { course = it },
            label = { Text("Course") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = grade,
            onValueChange = { grade = it },
            label = { Text("Grade") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (name.text.isNotBlank() && course.text.isNotBlank() && grade.text.isNotBlank()
                    && email.text.isNotBlank() && phone.text.isNotBlank()
                ) {
                    viewModel.addItem(name.text, course.text, grade.text, email.text, phone.text)
                    name = TextFieldValue()
                    course = TextFieldValue()
                    grade = TextFieldValue()
                    email = TextFieldValue()
                    phone = TextFieldValue()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Student")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(viewModel.items) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item, modifier = Modifier.weight(1f))
                    Button(onClick = { viewModel.removeItem(item) }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun PreviewItemScreen() {
    val context = LocalContext.current
    val viewModel: ItemViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ItemViewModel(DatabaseHelper(context)) as T
        }
    })
    ItemScreen(viewModel)
}