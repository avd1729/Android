package com.example.crudapp.provider

import android.content.*
import android.database.Cursor
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import com.example.crudapp.DatabaseHelper

class StudentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.crudapp.provider"
        val CONTENT_URI: Uri = Uri.parse("content://com.example.crudapp.provider/students")

        private const val STUDENTS = 1
        private const val STUDENT_ID = 2

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "students", STUDENTS)
            addURI(AUTHORITY, "students/#", STUDENT_ID)
        }
    }

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(): Boolean {
        // Initialize dbHelper only if context is not null
        context?.let {
            dbHelper = DatabaseHelper(it)
        } ?: run {
            throw IllegalStateException("Context is null. Cannot initialize dbHelper.")
        }
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        val db = dbHelper.readableDatabase
        val queryBuilder = SQLiteQueryBuilder().apply { tables = "Students" }

        val cursor = when (uriMatcher.match(uri)) {
            STUDENTS -> queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder)
            STUDENT_ID -> {
                queryBuilder.appendWhere("id = ${uri.lastPathSegment}")
                queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder)
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }

        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = dbHelper.writableDatabase
        val id = db.insert("Students", null, values)
        context?.contentResolver?.notifyChange(uri, null)
        return Uri.withAppendedPath(CONTENT_URI, id.toString())
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val db = dbHelper.writableDatabase
        val count = db.update("Students", values, selection, selectionArgs)
        context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val db = dbHelper.writableDatabase
        val count = db.delete("Students", selection, selectionArgs)
        context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            STUDENTS -> "vnd.android.cursor.dir/vnd.$AUTHORITY.students"
            STUDENT_ID -> "vnd.android.cursor.item/vnd.$AUTHORITY.students"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }
}
