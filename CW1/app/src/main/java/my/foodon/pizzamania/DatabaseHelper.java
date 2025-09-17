package my.foodon.pizzamania;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "PizzaManiaDB.db";
    private static final int DATABASE_VERSION = 1;

    // Table name and columns
    private static final String TABLE_ABOUT = "about_info";
    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_IMAGE_URL = "image_url";
    private static final String COL_PHONE = "phone";
    private static final String COL_EMAIL = "email";
    private static final String COL_ADDRESS = "address";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_ABOUT + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_IMAGE_URL + " TEXT, " +
                COL_PHONE + " TEXT, " +
                COL_EMAIL + " TEXT, " +
                COL_ADDRESS + " TEXT)";
        db.execSQL(createTable);

        // Insert default data
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, "Pizza Mania");
        values.put(COL_DESCRIPTION, "Welcome to Pizza Mania! We serve the most delicious pizzas in town with fresh ingredients and authentic flavors. Our passion for great food and excellent service makes us your favorite pizza destination.");
        values.put(COL_IMAGE_URL, "https://example.com/pizza-logo.jpg");
        values.put(COL_PHONE, "+1-234-567-8900");
        values.put(COL_EMAIL, "info@pizzamania.com");
        values.put(COL_ADDRESS, "123 Pizza Street, Food City, FC 12345");
        db.insert(TABLE_ABOUT, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ABOUT);
        onCreate(db);
    }

    // CRUD Operations
    public boolean updateAboutInfo(String title, String description, String imageUrl,
                                   String phone, String email, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_IMAGE_URL, imageUrl);
        values.put(COL_PHONE, phone);
        values.put(COL_EMAIL, email);
        values.put(COL_ADDRESS, address);

        int result = db.update(TABLE_ABOUT, values, COL_ID + "=?", new String[]{"1"});
        return result > 0;
    }

    public Cursor getAboutInfo() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ABOUT, null, null, null, null, null, null);
    }

    public boolean deleteAboutInfo() {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_ABOUT, COL_ID + "=?", new String[]{"1"});
        return result > 0;
    }

    public boolean insertAboutInfo(String title, String description, String imageUrl,
                                   String phone, String email, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_IMAGE_URL, imageUrl);
        values.put(COL_PHONE, phone);
        values.put(COL_EMAIL, email);
        values.put(COL_ADDRESS, address);

        long result = db.insert(TABLE_ABOUT, null, values);
        return result != -1;
    }
}