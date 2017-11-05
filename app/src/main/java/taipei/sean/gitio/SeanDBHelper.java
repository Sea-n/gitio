package taipei.sean.gitio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class SeanDBHelper extends SQLiteOpenHelper {

    SeanDBHelper(Context context, String name,
                 SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE main.history " +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "url TEXT NOT NULL, " +
                "code TEXT NOT NULL," +
                "date INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE main.params " +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "kind TEXT NOT NULL, " +
                "value TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 0:
                try {
                    db.execSQL("CREATE TABLE main.history " +
                            "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "url TEXT NOT NULL, " +
                            "code TEXT NOT NULL," +
                            "date INTEGER NOT NULL)");

                    db.execSQL("CREATE TABLE main.params " +
                            "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "kind TEXT NOT NULL, " +
                            "value TEXT)");
                } catch (SQLiteException e) {
                    Log.e("db", "onUpgrade", e);
                }
        }
    }

    ArrayList<HistoryActivity.Store> getHistory() {
        SQLiteDatabase db = getWritableDatabase();

        ArrayList<HistoryActivity.Store> result = new ArrayList<HistoryActivity.Store>() {
        };

        Cursor cursor = db.query("history", null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            long id = cursor.getLong(0);
            String url = cursor.getString(1);
            String code = cursor.getString(2);
            long date = cursor.getLong(3);
            HistoryActivity.Store item = new HistoryActivity.Store(url, code, id, date);
            result.add(item);
        }

        cursor.close();
        return result;
    }

    String getParam(String kind) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.query("params", null, "kind=?", new String[]{kind}, null, null, null);
        int count = cursor.getCount();
        if (count == 0) {
            cursor.close();
            return null;
        } else {
            cursor.moveToNext();
            String value = cursor.getString(2);
            cursor.close();
            return value;
        }
    }

    void insertHistory(String url, String code) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("url", url);
        values.put("code", code);
        values.put("date", new java.util.Date().getTime());

        Cursor cursor = db.query("history", null, "code=?", new String[]{code}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        if (count == 0)
            db.insert("history", null, values);
    }

    void updateParam(String kind, String value) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("kind", kind);
        values.put("value", value);

        Cursor cursor = db.query("params", null, "kind=?", new String[]{kind}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        if (count == 0) {
            db.insert("params", null, values);
        } else {
            db.update("params", values, "kind = ?", new String[]{kind + ""});
        }
    }

    void deleteHistory(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("history", "_id = ?", new String[]{id + ""});
    }
}
