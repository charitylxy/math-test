package curtin.edu.mathtest.sql_database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import curtin.edu.mathtest.classes.Result;
import curtin.edu.mathtest.classes.Student;

import curtin.edu.mathtest.sql_database.DBSchema.StudentTable;
import curtin.edu.mathtest.sql_database.DBSchema.ResultTable;

public class StudentDBModel {
    private List<Student> students = new ArrayList<>();
    private SQLiteDatabase db;

    public StudentDBModel(){}

    public void load (Context context){
        this.db = new DBHelper(context).getWritableDatabase();
    }

    public int size(){
        return students.size();
    }

    public Student get(int i){
        return students.get(i);
    }

    public void add (Student newStudent){
        students.add(newStudent);
        db.insert(StudentTable.NAME, null, cv(newStudent));
    }

    public void edit (Student newStudent){
        String[] whereValues = {String.valueOf(newStudent.getId())};
        db.update(StudentTable.NAME, cv(newStudent),
                StudentTable.Cols.ID + "=?", whereValues);
    }

    public void remove (Student rmStudent){
        students.remove(rmStudent);
        // remove faction from database
        String[] whereValues = {String.valueOf(rmStudent.getId())};
        db.delete(StudentTable.NAME,
                StudentTable.Cols.ID + "=?",
                whereValues);
    }

    public ContentValues cv (Student newStudent){
        ContentValues cv = new ContentValues();
        cv.put (StudentTable.Cols.ID, newStudent.getId());
        cv.put (StudentTable.Cols.FIRSTNAME, newStudent.getFirstName());
        cv.put (StudentTable.Cols.LASTNAME, newStudent.getLastName());

        String phone ="";
        List<String> arrPhone = newStudent.getPhone();
        for (int i =0; i< arrPhone.size() ; i++){
            phone += arrPhone.get(i);

            if (i < arrPhone.size() -1){
                phone += ";;";
            }
        }
        cv.put (StudentTable.Cols.PHONE, phone);

        String email ="";
        List<String> arrEmail = newStudent.getEmail();
        for (int i =0; i< arrEmail.size() ; i++){
            email += arrEmail.get(i);

            if (i < arrEmail.size() -1){
                email += ";;";
            }
        }
        cv.put (StudentTable.Cols.EMAIL, email);

        return cv;
    }

    public List<Student> getAllStudents(){
        List <Student> studentList = new ArrayList<>();
        Cursor cursor = db.query(StudentTable.NAME,
                null,null,null,
                null,null,null);
        DBCursor studentDBCursor = new DBCursor(cursor);

        try{
            studentDBCursor.moveToFirst();
            while (!studentDBCursor.isAfterLast()){
                studentList.add(studentDBCursor.getStudent());
                studentDBCursor.moveToNext();
            }
        }
        finally{
            cursor.close();
        }
        return studentList;
    }

    public Student getStudent(String studentId){
        Student student = null;

        //store student username that added by instructor
        String selectQuery = "SELECT  * FROM " + StudentTable.NAME + " WHERE "
                + StudentTable.Cols.ID + " = '" + studentId + "'";

        Cursor cursor = db.rawQuery(selectQuery,
                null);
        DBCursor studentDBCursor = new DBCursor(cursor);

        try{
            studentDBCursor.moveToFirst();
            while (!studentDBCursor.isAfterLast()){
                student = studentDBCursor.getStudent();
                studentDBCursor.moveToNext();
            }
        }
        finally{
            cursor.close();
        }
        return student;
    }
}
