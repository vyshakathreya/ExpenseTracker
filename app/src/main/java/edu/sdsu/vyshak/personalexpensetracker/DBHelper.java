package edu.sdsu.vyshak.personalexpensetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by vysha on 3/31/2017.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "name.db";
    private static final int DATABASE_VERSION = 1;
    private static final String FRIENDS_COLUMN_NAME = "nickname";
    private static final String FRIENDS_COLUMN_COUNTRY = "country";
    private static final String FRIENDS_COLUMN_STATE = "state";
    private static final String FRIENDS_COLUMN_CITY = "city";
    private static final String FRIENDS_COLUMN_EMAIL = "email";
    private static final String FRIENDS_COLUMN_LATITUDE = "latitude";
    private static final String FRIENDS_COLUMN_LONGITUDE = "longitude";
    private static final String FRIENDS_COLUMN_YEAR = "year";
    private static final String FRIENDS_COLUMN_ID = "id";
    private static final String TABLE_NAME = "users";
    private static final String ACCOUNTS_TABLE = "userAccounts";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE userAccounts" +
                "(accountNickname text primary key, accountType text, displayAccount text)"
        );

        db.execSQL("CREATE TABLE IF NOT EXISTS userTransactions" +
                "(transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, account text, amount int,currency text," +
                "transactionType text, category text, date text, description text)"
        );
        //paymentChosen,expense,currencyChosen,transactionType,categoryChosen,spentDate
    }


    /*public boolean insertUser (int id, String nickname, String email, String city, String state, String country,double latitude,
                                  double longitude,int year) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id",id);
        contentValues.put("nickname", nickname);
        contentValues.put("email", email);
        contentValues.put("city", city);
        contentValues.put("state",state);
        contentValues.put("country", country);
        contentValues.put("latitude", latitude);
        contentValues.put("longitude", longitude);
        contentValues.put("year", year);
        db.insert("users", null, contentValues);
        return true;
    }*/

    public boolean insertAccount(String accountNickname, String accountType, String displayName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("accountNickname", accountNickname);
        contentValues.put("accountType", accountType);
        contentValues.put("displayAccount", displayName);
        db.insert("userAccounts", null, contentValues);
        return true;
    }

    /*public ArrayList<User> getUsers(String query) {
        ArrayList<User> array_list = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( query, null );
        res.moveToFirst();
        while(res.isAfterLast() == false){
           *//* User user = new User();
            user.setNickname(res.getString(res.getColumnIndex(FRIENDS_COLUMN_NAME)));
            user.setYear(res.getString(res.getColumnIndex(FRIENDS_COLUMN_YEAR)));
            user.setState(res.getString(res.getColumnIndex(FRIENDS_COLUMN_STATE)));
            user.setCountry(res.getString(res.getColumnIndex(FRIENDS_COLUMN_COUNTRY)));
            user.setLatitude(res.getDouble(res.getColumnIndex(FRIENDS_COLUMN_LATITUDE)));
            user.setLongitude(res.getDouble(res.getColumnIndex(FRIENDS_COLUMN_LONGITUDE)));*//*
            //array_list.add(user);
            res.moveToNext();
        }
        res.close();
        return array_list;
    }*/


    /*public int getUserCount(String query) {
        //String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    public int getMaxid() {
        String idQuery = "SELECT MAX(id) FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(idQuery, null);
        cursor.moveToFirst();
        int maxId = cursor.getInt(0);
        cursor.close();
        return maxId;
    }*/

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /*public int getminUserCount() {
        String idQuery = "SELECT MIN(id) FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(idQuery, null);
        cursor.moveToFirst();
        int minId = cursor.getInt(0);
        cursor.close();
        return minId;
    }

    public int getminUserNum() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }*/

    public ArrayList<String> getAllAccounts() {
        ArrayList<String> array_list = new ArrayList<>();

        String query = "SELECT displayAccount FROM userAccounts";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery(query, null);
        res.moveToFirst();
        while (res.isAfterLast() == false) {
            array_list.add(res.getString(res.getColumnIndex("displayAccount")));
            res.moveToNext();
        }
        res.close();
        return array_list;

    }

    public void insertTransaction(String account, int amount, String currency, String transtype, String category, String date, String desc) {
        //paymentChosen,expense,currencyChosen,transactionType,categoryChosen,spentDate
/*        .execSQL("CREATE TABLE IF NOT EXISTS userTransactions" +
                        "(transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, account text, amount int,currency text" +
                        "transactionType text, category text, date text, description text)"*/
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("account", account);
        contentValues.put("transactionType", transtype);
        contentValues.put("currency", currency);
        contentValues.put("amount", amount);
        contentValues.put("category", category);
        contentValues.put("date", date);
        contentValues.put("description", desc);
        db.insert("userTransactions", null, contentValues);
    }

    public ArrayList<Expenses> gettransactions( String query) {
        ArrayList<Expenses> array_list = new ArrayList<>();
        //String query="Select * from transaction";
        SQLiteDatabase dbw = getWritableDatabase();
        dbw.execSQL("CREATE TABLE IF NOT EXISTS userTransactions" +
                "(transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, account text, amount int,currency text," +
                "transactionType text, category text, date text, description text)"
        );
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery(query, null);
        res.moveToFirst();
        while (res.isAfterLast() == false) { //account text, amount int,currency text" +
            //"transactionType text, category text, date text, description text
            Expenses expenses = new Expenses();
            expenses.setAccount(res.getString(res.getColumnIndex("account")));
            expenses.setAmount(res.getInt(res.getColumnIndex("amount")));
            expenses.setCategory(res.getString(res.getColumnIndex("category")));
            expenses.setCurrency(res.getString(res.getColumnIndex("currency")));
            expenses.setDate(res.getString(res.getColumnIndex("date")));
            expenses.setDesc(res.getString(res.getColumnIndex("description")));
            array_list.add(expenses);
            res.moveToNext();
        }
        res.close();
        return array_list;
    }
}
