package com.example.contentproviders


import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private val CONTENT_URI: Uri = Uri.parse("content://com.example.crudapp.provider/students")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val students = remember { mutableStateListOf<String>() }
            fetchStudents(students)

            StudentListScreen(students)
        }
    }

    private fun fetchStudents(studentList: MutableList<String>) {
        val cursor: Cursor? = contentResolver.query(CONTENT_URI, arrayOf("name"), null, null, null)
        studentList.clear()

        cursor?.use {
            while (it.moveToNext()) {
                studentList.add(it.getString(0)) // Fetch student name
            }
        }
    }
}

@Composable
fun StudentListScreen(students: List<String>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Student List", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(students) { student ->
                Text(text = student, modifier = Modifier.padding(8.dp))
            }
        }
    }
}
