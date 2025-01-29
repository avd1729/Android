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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "items.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE Items(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Items")
        onCreate(db)
    }

    fun insertItem(name: String) {
        val db = writableDatabase
        val values = ContentValues().apply { put("name", name) }
        db.insert("Items", null, values)
    }

    fun getAllItems(): List<String> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT name FROM Items", null)
        val items = mutableListOf<String>()
        while (cursor.moveToNext()) {
            items.add(cursor.getString(0))
        }
        cursor.close()
        return items
    }

    fun deleteItem(name: String) {
        val db = writableDatabase
        db.delete("Items", "name=?", arrayOf(name))
    }
}

class ItemViewModel(private val dbHelper: DatabaseHelper) : ViewModel() {
    var items by mutableStateOf(listOf<String>())
        private set

    init { fetchItems() }

    fun fetchItems() {
        items = dbHelper.getAllItems()
    }

    fun addItem(name: String) {
        dbHelper.insertItem(name)
        fetchItems()
    }

    fun removeItem(name: String) {
        dbHelper.deleteItem(name)
        fetchItems()
    }
}

@Composable
fun ItemScreen(viewModel: ItemViewModel) {
    var textState by remember { mutableStateOf(TextFieldValue()) }
    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = textState,
            onValueChange = { textState = it },
            label = { Text("Enter item") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (textState.text.isNotBlank()) {
                viewModel.addItem(textState.text)
                textState = TextFieldValue()
            }
        }) {
            Text("Add Item")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(viewModel.items) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item, modifier = Modifier.weight(1f))
                    Button(onClick = { viewModel.removeItem(item) }) {
                        Text("Delete")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Preview
@Composable
fun PreviewItemScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: ItemViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ItemViewModel(DatabaseHelper(context)) as T
        }
    })
    ItemScreen(viewModel)
}
