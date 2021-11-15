package curtin.edu.mathtest.sql_database;

public class DBSchema {
    public static class StudentTable {
        public static final String  NAME = "student";
        public static class Cols {
            public static final String ID = "student_id";
            public static final String FIRSTNAME = "student_firstname";
            public static final String LASTNAME = "student_lastname";
            public static final String PHONE = "student_phone";
            public static final String EMAIL = "student_email";
        }
    }

    public static class ResultTable {
        public static final String NAME = "result";
        public static class Cols{
            public static final String ID = "result_id";
            public static final String STU_ID = "result_studentID";
            public static final String STARTTIME = "result_starttime";
            public static final String DURATION = "result_duration";
            public static final String SCORE = "result_score";
        }
    }
}
