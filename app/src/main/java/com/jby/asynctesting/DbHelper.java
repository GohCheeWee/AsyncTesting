package com.jby.asynctesting;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DbHelper extends SQLiteOpenHelper {
    private Context context;

    public DbHelper(Context context) {
        super(context, DbContact.DATABASE_NAME, null, DbContact.DATABASE_VERSION);
        this.context = context;
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DbContact.CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DbContact.TB_USER);
        onCreate(sqLiteDatabase);
    }

    public void saveContact(String username, String status, SQLiteDatabase sqLiteDatabase)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("username", username);
        contentValues.put("status", status);
        sqLiteDatabase.insert(DbContact.TB_USER,null,contentValues);
    }

    public Cursor getContact(SQLiteDatabase sqLiteDatabase)
    {
        String[] projection = {"username", "status"};
        return(sqLiteDatabase.query(DbContact.TB_USER, projection, null, null, null, null, null));
    }

    public void updateLocalDatabase(String name, String status, SQLiteDatabase sqLiteDatabase)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", status);

        String selection ="username LIKE ?";
        String[] selection_args = {name};
        sqLiteDatabase.update(DbContact.TB_USER, contentValues, selection, selection_args);
    }

    public Cursor fetchAll(SQLiteDatabase sqLiteDatabase) {
//        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT username, status FROM " +DbContact.TB_USER;
        return sqLiteDatabase.rawQuery(sql, null);
    }
}
