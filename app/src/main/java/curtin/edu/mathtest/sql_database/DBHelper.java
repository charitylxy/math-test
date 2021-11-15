package curtin.edu.mathtest.sql_database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import curtin.edu.mathtest.sql_database.DBSchema.StudentTable;
import curtin.edu.mathtest.sql_database.DBSchema.ResultTable;

public class DBHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "mathTest.db";

    public DBHelper (Context context){
        super(context, DATABASE_NAME, null, VERSION);
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " + StudentTable.NAME + "(" +
                StudentTable.Cols.ID + " TEXT PRIMARY KEY, " +
                StudentTable.Cols.FIRSTNAME + " TEXT, " +
                StudentTable.Cols.LASTNAME + " TEXT, " +
                StudentTable.Cols.PHONE + " TEXT, " +
                StudentTable.Cols.EMAIL + " INTEGER)");

        db.execSQL("CREATE TABLE " + ResultTable.NAME + "(" +
                ResultTable.Cols.ID + " TEXT PRIMARY KEY, " +
                ResultTable.Cols.STU_ID + " TEXT, " +
                ResultTable.Cols.STARTTIME + " TEXT, " +
                ResultTable.Cols.DURATION + " TEXT, " +
                ResultTable.Cols.SCORE + " DOUBLE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        throw new UnsupportedOperationException("Sorry");
    }
}
