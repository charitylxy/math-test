package curtin.edu.mathtest.sql_database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import curtin.edu.mathtest.classes.Result;
import curtin.edu.mathtest.classes.Student;
import curtin.edu.mathtest.sql_database.DBSchema.ResultTable;

public class ResultDBModel {
    private List<Result> results = new ArrayList<>();
    private SQLiteDatabase db;

    public ResultDBModel(){}

    public void load (Context context){
        this.db = new DBHelper(context).getWritableDatabase();
    }

    public int size(){
        return results.size();
    }

    public Result get(int i){
        return results.get(i);
    }

    public void add (Result newResult){
        results.add(newResult);
        db.insert(ResultTable.NAME, null, cv(newResult));
    }

    public void remove (Result rmResult){
        results.remove(rmResult);
        // remove faction from database
        String[] whereValues = {String.valueOf(rmResult.getId())};
        db.delete(ResultTable.NAME,
                ResultTable.Cols.ID + "=?",
                whereValues);
    }

    public ContentValues cv (Result newResult){
        ContentValues cv = new ContentValues();
        cv.put (ResultTable.Cols.ID, newResult.getId());
        cv.put (ResultTable.Cols.STU_ID, newResult.getStudent_id());
        cv.put (ResultTable.Cols.STARTTIME, newResult.getStart_time());
        cv.put (ResultTable.Cols.DURATION, newResult.getDuration());
        cv.put (ResultTable.Cols.SCORE, newResult.getScore());
        return cv;
    }

    public List<Result> getAllResults(){
        List <Result> resultList = new ArrayList<>();
        Cursor cursor = db.query(ResultTable.NAME,
                null,null,null,
                null,null,null);
        DBCursor resultDBCursor = new DBCursor(cursor);

        try{
            resultDBCursor.moveToFirst();
            while (!resultDBCursor.isAfterLast()){
                resultList.add(resultDBCursor.getResult());
                resultDBCursor.moveToNext();
            }
        }
        finally{
            cursor.close();
        }
        return resultList;
    }

    public List<Result> getStudentResult(String student){
        List <Result> resultList = new ArrayList<>();

        //store student username that added by instructor
        String selectQuery = "SELECT  * FROM " + ResultTable.NAME + " WHERE "
                + ResultTable.Cols.STU_ID + " = '" + student + "'";

        Cursor cursor = db.rawQuery(selectQuery,
                null);
        DBCursor resultDBCursor = new DBCursor(cursor);

        try{
            resultDBCursor.moveToFirst();
            while (!resultDBCursor.isAfterLast()){
                resultList.add(resultDBCursor.getResult());
                resultDBCursor.moveToNext();
            }
        }
        finally{
            cursor.close();
        }
        return resultList;
    }
}
