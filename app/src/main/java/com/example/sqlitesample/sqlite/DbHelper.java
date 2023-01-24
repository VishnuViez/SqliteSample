package com.example.sqlitesample.sqlite;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "details.db";
    private final static String tableusers = "users";
    private final static String tableprofession = "professions";
    private final static String TITLE = "title";
    public final static String radiooptions = "radiooptions";
    private final static String locationtable = "location";
    private final static String imagetable = "image";
    private final static String videotable = "video";
    private SQLiteDatabase db;

    public DbHelper(Context context) {
        super(context, "details.db", null, 1);
    }

    //creating tables
    @Override
    public void onCreate(SQLiteDatabase MyDB) {
        this.db = MyDB;
        profession(new Data("Designer"));
        profession(new Data("FrontEnd Developer"));
        profession(new Data("BackEnd Developer"));
        profession(new Data("Manager"));
        profession(new Data("Designer"));

        MyDB.execSQL("create Table " + tableusers + " (username text primary key, password text, name text, phone text)");
        MyDB.execSQL("create Table " + tableprofession + " (" + TITLE + " VARCHAR )");
        MyDB.execSQL("create Table " + locationtable + " (latitude text, longitude text)");
        MyDB.execSQL("create Table " + radiooptions + " (radio text)");
        MyDB.execSQL("create Table " + imagetable + " (image byte)");
        MyDB.execSQL("create Table " + videotable + " (video)");
        //MyDB.execSQL("create Table " +options+" (options)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase MyDB, int i, int i1) {
        MyDB.execSQL("drop Table if exists " + tableusers);
        MyDB.execSQL("drop Table if exists " + tableprofession);
        MyDB.execSQL("drop Table if exists " + radiooptions);
        MyDB.execSQL("drop Table if exists " + imagetable);
        MyDB.execSQL("drop Table if exists " + videotable);
        onCreate(MyDB);
    }


    /**
     * Getting all labels
     * returns list of labels
     *
     * @return
     */

    public void insertProfessionValue(String value) {
        db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("options", value);
        db.insert(tableprofession, null, values);
        db.close();
    }

    public List<String> getProfessionsAsList() {
        db = this.getReadableDatabase();
        List<String> list = new ArrayList<String>();
        String selectQuery = "SELECT  * FROM " + tableprofession;
        Cursor cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {

            @SuppressLint("Range") String val = cursor.getString(cursor.getColumnIndex("options"));
            list.add(val);

        }
        // closing connection
        cursor.close();
        db.close();
        // returning lables
        return list;
    }
    public void insertGender() {
        db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("radio", String.valueOf(values));
        db.insert(radiooptions, null, values);
        db.close();
    }

    public Cursor getGender() {
        db = this.getWritableDatabase();
        String selectQuery = "Select * from " + tableprofession;

        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor;
    }
    public Data profession (Data data){
    ContentValues values = new ContentValues();
    values.put(TITLE,data.title);
    db.insert(tableprofession, null, values);
        return data;
    }

    public Cursor getlocation() {
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from locationtable ", null);
        return cursor;
    }

    public Cursor fetch() {
        db = this.getReadableDatabase();
        //String selectQuery = "select * from " + tableusers + "order by " + colusers + " desc";
        Cursor cursor = db.rawQuery("Select * from users ", null);
        return cursor;
    }

    public Boolean insertData(String username, String password, String name, String phone) {
        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("username", username);
        contentValues.put("password", password);
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        long result = db.insert("users", null, contentValues);
        if (result == -1) return false;
        else
            return true;
    }

    public Boolean checkusername(String username) {
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from users where username = ?", new String[]{username});
        if (cursor.getCount() > 0)
            return true;
        else
            return false;
    }

    public Boolean checkusernamepassword(String username, String password) {
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from users where username = ? and password = ?", new String[]{username, password});
        if (cursor.getCount() > 0)
            return true;
        else
            return false;
    }

    public void insertVideo(String table_name, Object o, ContentValues values) {

    }
}
