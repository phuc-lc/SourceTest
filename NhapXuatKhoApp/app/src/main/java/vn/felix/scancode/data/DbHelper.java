package vn.felix.scancode.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * sqlite database helper to create table into SQLite database
 *
 * @author ketan(Visit my <a
 *href="http://androidsolution4u.blogspot.in/">blog</a>)
 */
public class DbHelper extends SQLiteOpenHelper {
    static String DATABASE_NAME = "userdata";
    public static final String TABLE_NAME = "code";
    public static final String KEY_STAMPCODE = "stampCode";
    public static final String KEY_SERIALCODE = "serialCode";
    public static final String KEY_DATESCAN = "dateScan";
    public static final String KEY_PLOTSCODE = "plotsCode";
    public static final String KEY_NAME = "nameProduce";
    public static final String KEY_ID = "id";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                KEY_ID + " INTEGER PRIMARY KEY, "
                + KEY_STAMPCODE + " TEXT, "
                + KEY_SERIALCODE + " TEXT, "
                + KEY_DATESCAN + " TEXT, "
                + KEY_PLOTSCODE + " TEXT, "
                + KEY_NAME + " TEXT)";
        db.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }

}
