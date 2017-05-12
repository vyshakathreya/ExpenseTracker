package edu.sdsu.vyshak.personalexpensetracker;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

/**
 * Created by vysha on 3/31/2017.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "name.db";
    private static final int DATABASE_VERSION = 1;

    private static final String ACCOUNTS_TABLE = "accounts";
    private static final String ACCOUNTS_COLUMN_ACCOUNT_NAME = "accountNickname";
    private static final String ACCOUNTS_COLUMN_ACCOUNT_TYPE = "accountType";
    private static final String ACCOUNTS_COLUMN_ACCOUNT_USER = "uid";
    private static final String ACCOUNTS_COLUMN_DISPLAY_NAME = "displayAccount";

    private static final String TRANSACTIONS_NAME = "transactions";
    private static final String EXPENSES_COLUMN_USER = "uid";
    private static final String EXPENSES_COLUMN_CATEGORY = "category";
    private static final String EXPENSES_COLUMN_ACCOUNT = "account";
    private static final String EXPENSES_COLUMN_AMOUNT = "amount";
    private static final String EXPENSES_COLUMN_CURRENCY = "currency";
    private static final String EXPENSES_COLUMN_TYPE = "transactionType";
    private static final String EXPENSES_COLUMN_DATE = "date";
    private static final String EXPENSES_COLUMN_DESCRIPTION = "description";

    private static final String TAG="DBHelper";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS accounts" +
                "(uid INTEGER NOT NULL, accountNickname text, accountType text, displayAccount text PRIMARY KEY)"
        );

        db.execSQL("CREATE TABLE IF NOT EXISTS transactionsSummary" +
                "(transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, uid text, account text, amount real,currency text," +
                "transactionType text, category text, date date, description text)"
        );

        db.execSQL("CREATE TABLE IF NOT EXISTS userTable(name text , email text primary key, phone text, currency text)");
        db.execSQL("CREATE TABLE IF NOT EXISTS alertsTbl(duedate date , duebill text primary key, repeatCycle text)");

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean insertAccount(String uid, String accountNickname, String accountType, String displayName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ACCOUNTS_COLUMN_ACCOUNT_USER,uid);
        contentValues.put(ACCOUNTS_COLUMN_ACCOUNT_NAME, accountNickname);
        contentValues.put(ACCOUNTS_COLUMN_ACCOUNT_TYPE, accountType);
        contentValues.put(ACCOUNTS_COLUMN_DISPLAY_NAME, displayName);
        db.insert("accounts", null, contentValues);
        return true;
    }

    public ArrayList<String> getAllAccounts(String userid) {
        ArrayList<String> array_list = new ArrayList<>();
        SQLiteDatabase dbw = getWritableDatabase();
        dbw.execSQL("CREATE TABLE IF NOT EXISTS accounts" +
                "(uid INTEGER NOT NULL, accountNickname text, accountType text, displayAccount text PRIMARY KEY)"
        );
        String query = "SELECT displayAccount FROM accounts where uid like "+"\'"+userid+"\'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery(query, null);
        res.moveToFirst();
        while(res.isAfterLast() == false) {
            array_list.add(res.getString(res.getColumnIndex("displayAccount")));
            res.moveToNext();
        }
        res.close();
        return array_list;

    }

    public void insertTransaction(String userID, String account,float amount, String currency, String transtype, String category, String date, String desc) {
        SQLiteDatabase dbw = getWritableDatabase();
        dbw.execSQL("CREATE TABLE IF NOT EXISTS transactionsSummary" +
                "(transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, uid text, account text, amount real,currency text," +
                "transactionType text, category text, date date, description text)"
        );
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("account", account);
        contentValues.put("transactionType", transtype);
        contentValues.put("currency", currency);
        contentValues.put("amount", amount);
        contentValues.put("category", category);
        contentValues.put("date", date);// .getTime()
        contentValues.put("description", desc);
        contentValues.put("uid",userID);
        db.insert("transactionsSummary", null, contentValues);
    }

    public ArrayList<Expenses> gettransactions(String query) {
        ArrayList<Expenses> array_list = new ArrayList<>();
        SQLiteDatabase dbw = getWritableDatabase();
        dbw.execSQL("CREATE TABLE IF NOT EXISTS transactionsSummary" +
                "(transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, uid text, account text, amount real,currency text," +
                "transactionType text, category text, date date, description text)"
        );
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery(query, null);
        res.moveToFirst();
        while (res.isAfterLast() == false) { //account text, amount int,currency text" +
            //"transactionType text, category text, date text, description text
            Expenses expenses = new Expenses();
            expenses.setAccount(res.getString(res.getColumnIndex(EXPENSES_COLUMN_ACCOUNT)));
            expenses.setAmount(res.getFloat(res.getColumnIndex(EXPENSES_COLUMN_AMOUNT)));
            expenses.setCategory(res.getString(res.getColumnIndex(EXPENSES_COLUMN_CATEGORY)));
            expenses.setCurrency(res.getString(res.getColumnIndex(EXPENSES_COLUMN_CURRENCY)));
            expenses.setDesc(res.getString(res.getColumnIndex(EXPENSES_COLUMN_DESCRIPTION)));
            expenses.setDate(res.getString(res.getColumnIndex(EXPENSES_COLUMN_DATE)));
            expenses.setTranstype(res.getString(res.getColumnIndex(EXPENSES_COLUMN_TYPE)));
            expenses.setUserId(res.getString(res.getColumnIndex(EXPENSES_COLUMN_USER)));
            array_list.add(expenses);
            res.moveToNext();
        }
        res.close();
        return array_list;
    }

    public void removeAccount(String toRemove, String user) {
        //uid INTEGER NOT NULL, accountNickname text, accountType text, displayAccount text PRIMARY KEY
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d(TAG,"removing account"+toRemove+user );
        db.execSQL("Delete FROM accounts where displayAccount like"+"\'"+toRemove+"\'" + "and uid like "+"\'"+user+"\'");
        Log.d(TAG,getAllAccounts(user).toString());
    }

    public void removeTransaction(String query) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
    }

    public float totalBalance(String user){
        SQLiteDatabase dbw = getWritableDatabase();
        dbw.execSQL("CREATE TABLE IF NOT EXISTS transactionsSummary" +
                        "(transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, uid text, account text, amount real,currency text," +
                        "transactionType text, category text, date date, description text)");
        String query = "select sum(amount) from transactionsSummary where uid like"+"\'"+user+"\'"+"and transactionType like" +"\'"+"Income"+"\'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        float countIncome = cursor.getFloat(0);
        Log.d("DBincome",String.valueOf(countIncome));// getInt(cursor.getColumnIndex(EXPENSES_COLUMN_AMOUNT));
        cursor.close();
        String queryExpense = "select sum(amount) from transactionsSummary where uid like"+"\'"+user+"\'"+"and transactionType like" +"\'"+"Expense"+"\'";
        cursor = db.rawQuery(queryExpense, null);
        cursor.moveToFirst();
        float countExpense = cursor.getFloat(0); //getCount();
        Log.d("DBexpense",String.valueOf(countExpense));// getInt(cursor.getColumnIndex(EXPENSES_COLUMN_AMOUNT));
        cursor.close();
        return countIncome-countExpense;
    }

    public int expensePerCategory(String user){
        String query = "select sum(amount) from transactionsSummary where uid like"+"\'"+user+"\'"+"and transactionType like" +"\'"+"Income"+"\'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int countIncome = cursor.getInt(0);
        Log.d("DBincome",String.valueOf(countIncome));// getInt(cursor.getColumnIndex(EXPENSES_COLUMN_AMOUNT));
        cursor.close();
        String queryExpense = "select sum(amount) from transactionsSummary where uid like"+"\'"+user+"\'"+"and transactionType like" +"\'"+"Expense"+"\'";
        cursor = db.rawQuery(queryExpense, null);
        cursor.moveToFirst();
        int countExpense = cursor.getInt(0); //getCount();
        Log.d("DBexpense",String.valueOf(countExpense));// getInt(cursor.getColumnIndex(EXPENSES_COLUMN_AMOUNT));
        cursor.close();
        return countIncome-countExpense;
    }

    public void saveShoppingItems(String itemName) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS shoppingItems(item text)");
        ContentValues contentValues = new ContentValues();
        contentValues.put("item",itemName);
        db.insert("shoppingItems", null, contentValues);
    }

    public ArrayList<ShopItems> getShoppingItems() {
        ArrayList<ShopItems> array_list = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS shoppingItems(item text)");
        String query="SELECT * FROM shoppingItems";
        Cursor res = db.rawQuery(query, null);
        res.moveToFirst();
        while (res.isAfterLast() == false) {
            ShopItems shopItems = new ShopItems();
            shopItems.setItemName(res.getString(res.getColumnIndex("item")));
            array_list.add(shopItems);
            res.moveToNext();
        }
        res.close();
        return array_list;
    }


    public void removeShoppingList(String shopItems) {

        String query = "Delete FROM shoppingItems where item like"+"\'"+shopItems+"\'";
        Log.d(TAG,query);
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
    }

    public void storeUsers(String name, String email, String currency, String phone) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS userTable(name text , email text primary key, phone text, currency text)");
        Log.d(TAG,name+email+currency);
        ContentValues contentValues = new ContentValues();
        contentValues.put("name",name);
        contentValues.put("email",email);
        contentValues.put("phone",phone);
        contentValues.put("currency",currency);
        db.insert("userTable", null, contentValues);
    }

    public String getuserinfo(String what, String wherelike){
        String query = "Select "+what+" from userTable where email like "+"\'"+wherelike+"\'";
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS userTable(name text , email text primary key, phone text, currency text)");
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        if(cursor == null || !cursor.moveToFirst() )
            return null;
        String countExpense = cursor.getString(0); //getCount();
        Log.d("DBexpense",String.valueOf(countExpense));// getInt(cursor.getColumnIndex(EXPENSES_COLUMN_AMOUNT));
        cursor.close();
        return countExpense;
    }

    public ArrayList<Disp> getAllUserAccounts(String uid) {
        ArrayList<Disp> array_list = new ArrayList<>();
        SQLiteDatabase dbw = getWritableDatabase();
        dbw.execSQL("CREATE TABLE IF NOT EXISTS accounts" +
                "(uid INTEGER NOT NULL, accountNickname text, accountType text, displayAccount text PRIMARY KEY)"
        );
        String query = "SELECT displayAccount FROM accounts where uid like "+"\'"+uid+"\'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery(query, null);
        res.moveToFirst();
        while(!res.isAfterLast()) {
            Disp disp = new Disp();
            disp.setDisplayname(res.getString(0));
            array_list.add(disp);
            res.moveToNext();
        }
        res.close();
        return array_list;
    }

    public void savelimits(String chosenBudgetCategory, float amountLimit, String limitCycle) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS budget" +
                "(category text PRIMARY KEY, amountlimit real, limitcycle text)"
        );
        ContentValues contentValues = new ContentValues();
        contentValues.put("category",chosenBudgetCategory);
        contentValues.put("amountlimit",amountLimit);
        contentValues.put("limitcycle",limitCycle);
        db.insert("budget", null, contentValues);
    }

    public ArrayList<Budget> getBudgetLimits(){
        SQLiteDatabase dbl=getWritableDatabase();
        dbl.execSQL("CREATE TABLE IF NOT EXISTS budget" +
                "(category text PRIMARY KEY, amountlimit real, limitcycle text)"
        );
        ArrayList<Budget> budgetArray = new ArrayList<>();
        String query="select * from budget";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery(query, null);
        res.moveToFirst();
        while (res.isAfterLast() == false) {
            Budget budget = new Budget();
            budget.setCategory(res.getString(res.getColumnIndex("category")));
            budget.setAmount(res.getDouble(res.getColumnIndex("amountlimit")));
            budget.setCycle(res.getString(res.getColumnIndex("limitcycle")));
            budgetArray.add(budget);
            res.moveToNext();
        }
        res.close();
        return budgetArray;
    }

    public void removeBudget(String shopItems) {

        String query = "Delete FROM budget where category like"+"\'"+shopItems+"\'";
        Log.d(TAG,query);
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
    }

    public double getexpense(String budget, String fromdate) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();

        String query = "Select sum(amount) from transactionsSummary where transactionType like 'Expense' and date > "+ fromdate+" and category like "+"\'"+ budget+"%\'";
        Log.d("expense from to ",query);
        SQLiteDatabase db = getReadableDatabase();
        Cursor res = db.rawQuery(query, null);
        res.moveToFirst();
        double ans=res.getDouble(0);
        res.close();
        return  ans;
    }

    public void setAlerts(String duedate, String duebill, String repeatCycle) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS alertsTbl(duedate date, duebill text primary key, repeatCycle text)");
        Log.d(TAG,duebill+duedate+repeatCycle);
        ContentValues contentValues = new ContentValues();
        contentValues.put("duedate",duedate);
        contentValues.put("duebill",duebill);
        contentValues.put("repeatCycle",repeatCycle);
        db.insert("alertsTbl", null, contentValues);
    }

    public ArrayList<UserAlerts> getAlerts( String fromdate) {
        SQLiteDatabase dbw = getWritableDatabase();
        dbw.execSQL("CREATE TABLE IF NOT EXISTS alertsTbl(duedate date, duebill text primary key, repeatCycle text)");
        ArrayList<UserAlerts> array_list = new ArrayList<>();
        String query = "Select * from alertsTbl where duedate > "+ fromdate;
        Log.d("expense from to ",query);
        SQLiteDatabase db = getReadableDatabase();
        Cursor res = db.rawQuery(query, null);
        res.moveToFirst();
        while (res.isAfterLast() == false) { //account text, amount int,currency text" +
            UserAlerts userAlerts = new UserAlerts();
            userAlerts.setDuebill(res.getString(res.getColumnIndex("duebill")));
            userAlerts.setDuedate(res.getString(res.getColumnIndex("duedate")));
            array_list.add(userAlerts);
            res.moveToNext();
        }
        res.close();
        return array_list;
    }


}
