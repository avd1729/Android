package com.example.crudapp

import android.content.*
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri

class StudentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.crudapp.provider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/students")

        const val TABLE_NAME = "Students"
        const val STUDENTS = 1
        const val STUDENT_ID = 2

        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "students", STUDENTS)
            addURI(AUTHORITY, "students/#", STUDENT_ID)
        }
    }

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(): Boolean {
        dbHelper = DatabaseHelper(context!!)
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        return when (uriMatcher.match(uri)) {
            STUDENTS -> db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder)
            STUDENT_ID -> {
                val id = uri.lastPathSegment
                db.query(TABLE_NAME, projection, "id=?", arrayOf(id), null, null, sortOrder)
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val id = db.insert(TABLE_NAME, null, values)
        context?.contentResolver?.notifyChange(uri, null)
        return Uri.withAppendedPath(CONTENT_URI, id.toString())
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val count = db.update(TABLE_NAME, values, selection, selectionArgs)
        context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val count = db.delete(TABLE_NAME, selection, selectionArgs)
        context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun getType(uri: Uri): String? = when (uriMatcher.match(uri)) {
        STUDENTS -> "vnd.android.cursor.dir/$AUTHORITY.$TABLE_NAME"
        STUDENT_ID -> "vnd.android.cursor.item/$AUTHORITY.$TABLE_NAME"
        else -> throw IllegalArgumentException("Unknown URI: $uri")
    }
}
