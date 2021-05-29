package helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLException;

public class StoreDatabase {

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_PRICE = "price";
    public static final String KEY_STATUS = "status";

    private static final String TAG = "ShopDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "mediShop";
    private static final String SHOP_TABLE = "shop";
    private static final int DATABASE_VERSION = 1;

    private final Context mContext;

    private static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + SHOP_TABLE + " (" +
            KEY_ID + " INTEGER PRIMARY KEY," +
            KEY_NAME + " TEXT,"+
            KEY_DESCRIPTION + " TEXT," +
            KEY_PRICE + " INTEGER," +
            KEY_STATUS + " TEXT" +");";

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            Log.w(TAG, DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            db.execSQL("DROP TABLE IF EXISTS "+SHOP_TABLE);
            onCreate(db);
        }
    }

    public StoreDatabase(Context context){
        this.mContext = context;
    }

    public StoreDatabase open() throws SQLException {
        mDbHelper = new DatabaseHelper(mContext);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        if (mDbHelper != null){
            mDbHelper.close();
        }
    }

    public long createItem(String id,String name, String description, double price, String status) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_ID, id);
        initialValues.put(KEY_DESCRIPTION, description);
        initialValues.put(KEY_PRICE, price);
        initialValues.put(KEY_STATUS, status);

        return mDb.insert(SHOP_TABLE, null, initialValues);
    }



    public Integer deleteContact (String id) {

        return mDb.delete(SHOP_TABLE,
                "_id = ? ",
                new String[] { Integer.toString(Integer.parseInt(id)) });
    }

    public boolean deleteAllItems() {

        int doneDelete = 0;
        doneDelete = mDb.delete(SHOP_TABLE, null , null);
        Log.w(TAG, Integer.toString(doneDelete));
        return doneDelete > 0;
    }

    public int getCartItemsRowCount(boolean type){
        mDb = mDbHelper.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(mDb, SHOP_TABLE, null, null);
    }

    public boolean addToCart (Integer id, String val){
        mDb = mDbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_STATUS, val);
        mDb.update(SHOP_TABLE, contentValues, "_id= ? ", new String[]{Integer.toString(id)});
        mDb.close();
        return true;
    }

    public int getTotalItemsCount() {
        String countQuery = "SELECT  * FROM " + SHOP_TABLE;
        mDb = mDbHelper.getReadableDatabase();
        Cursor cursor = mDb.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    public int getAmount() {
        mDb = mDbHelper.getReadableDatabase();
        Cursor cursor = mDb.rawQuery("SELECT SUM(" + KEY_PRICE + ") FROM " + SHOP_TABLE +"", null);
        int total = 0;
        if(cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        return total;
    }

    public Cursor fetchAllItems() {

        Cursor mCursor = mDb.query(SHOP_TABLE, new String[] {KEY_ID, KEY_NAME, KEY_DESCRIPTION, KEY_PRICE, KEY_STATUS},
                null,null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }


}
