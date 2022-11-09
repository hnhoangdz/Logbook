package hoang.example.logbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import Entities.Image;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database name
    private static final String DB_NAME = "ImageVisualization";

    // Database instance
    private final SQLiteDatabase database;

    // Image Table
    private static final String TABLE_IMAGE = "Image";
    private static final String IMAGE_ID = "id";
    private static final String IMAGE_URL = "url";

    // Create Image Table
    private static final String TABLE_IMAGE_CREATE = String.format(
            "CREATE TABLE %s (" +
                    "   %s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "   %s TEXT)",
            TABLE_IMAGE, IMAGE_ID, IMAGE_URL);

    public DatabaseHelper(Context context){
        super(context, DB_NAME, null, 1);
        database = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_IMAGE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGE);

        Log.v(this.getClass().getName(), TABLE_IMAGE + " database upgrade to version " +
                newVersion + " - old data lost");
        onCreate(db);
    }

    // Insert an image
    public long insertImage(String url){
        ContentValues rowValues = new ContentValues();
        rowValues.put(IMAGE_URL, url);
        return database.insertOrThrow(TABLE_IMAGE, null, rowValues);
    }

    // Get all images
    public ArrayList<Image> getAllImages(){
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_IMAGE, null);
        ArrayList<Image> results = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            int image_id = cursor.getInt(0);
            String image_url = cursor.getString(1);

            Image img = new Image();
            img.setId(image_id);
            img.setUrl(image_url);

            results.add(img);
            cursor.moveToNext();
        }
        return results;
    }

    // Get an image by id
    public Image getImageById(int image_id){
        String query = "SELECT * FROM " + TABLE_IMAGE + " WHERE " + IMAGE_ID + " =?;";
        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(image_id)});
        Image img = new Image();
        img.setId(cursor.getInt(0));
        img.setUrl(cursor.getString(1));
        return img;
    }
}
