package curtin.edu.mathtest.sql_database;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.graphics.Bitmap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import curtin.edu.mathtest.classes.Student;
import curtin.edu.mathtest.classes.Result;
import curtin.edu.mathtest.sql_database.DBSchema.StudentTable;
import curtin.edu.mathtest.sql_database.DBSchema.ResultTable;

public class DBCursor extends CursorWrapper {
    public DBCursor(Cursor cursor){
        super (cursor);
    }

    public Student getStudent(){
        String id = getString (getColumnIndex(DBSchema.StudentTable.Cols.ID));
        String firstname = getString (getColumnIndex(StudentTable.Cols.FIRSTNAME));
        String lastname = getString (getColumnIndex(StudentTable.Cols.LASTNAME));

        String sPhoneNum = getString (getColumnIndex(StudentTable.Cols.PHONE));
        String arrPhone [] = sPhoneNum.split(";;");
        List<String> phone = new ArrayList<>();
        if (!sPhoneNum.equals("")) {
            phone = Arrays.asList(arrPhone);
        }

        String sEmails = getString(getColumnIndex(StudentTable.Cols.EMAIL));
        String arrEmail[] = sEmails.split(";;");
        List<String> email = new ArrayList<>();
        if (!sEmails.equals("")) {
            email = Arrays.asList(arrEmail);
        }

        return new Student(id,firstname,lastname,phone,email);
    }

    public Result getResult(){
        String id = getString (getColumnIndex(DBSchema.ResultTable.Cols.ID));
        String studentId = getString (getColumnIndex(ResultTable.Cols.STU_ID));
        String starttime = getString (getColumnIndex(ResultTable.Cols.STARTTIME));
        String duration = getString (getColumnIndex(ResultTable.Cols.DURATION));
        Double score = getDouble(getColumnIndex(ResultTable.Cols.SCORE));

        return new Result(id,studentId,starttime,duration,score);
    }
}
