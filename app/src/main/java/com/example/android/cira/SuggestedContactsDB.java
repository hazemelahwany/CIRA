package com.example.android.cira;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class SuggestedContactsDB extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "MyDBName.db";
    public static final String Contacts_TABLE_NAME = "SuggestedContacts";
    public static final String Contacts_COLUMN_ID = "id";
    public static final String Contacts_COLUMN_Contact_Json = "contact";

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "create table suggestedContacts " +
                        "(id integer primary key , contact text)"
        );
    }

    public SuggestedContactsDB(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS suggestedContacts");
        onCreate(sqLiteDatabase);
    }
    public boolean insertContact (String id, String contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("contact", contact);
        db.insert("suggestedContacts", null, contentValues);
        return true;
    }
    public String getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from suggestedContacts where id="+id+"", null );
        if (res != null && res.moveToFirst()) {
            // data?
            System.out.println(res.getString(res.getColumnIndex("contact")));
            return res.getString(res.getColumnIndex("contact"));
        }

        return null;
    }
    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, Contacts_TABLE_NAME);
        return numRows;
    }

    public boolean updateContact (Integer id, String contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("contact", contact);
        db.update("suggestedContacts", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteContact (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("suggestedContacts",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<String> getAllContacts() {
        ArrayList<String> array_list = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from suggestedContacts", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(Contacts_COLUMN_Contact_Json)));
            res.moveToNext();
        }
        return array_list;
    }
}

