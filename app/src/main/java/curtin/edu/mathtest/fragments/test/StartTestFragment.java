package curtin.edu.mathtest.fragments.test;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import curtin.edu.mathtest.MainActivity;
import curtin.edu.mathtest.R;
import curtin.edu.mathtest.classes.Result;
import curtin.edu.mathtest.classes.Student;
import curtin.edu.mathtest.fragments.students.StudentViewFragment;
import curtin.edu.mathtest.fragments.students.StudentsFragment;

public class StartTestFragment extends Fragment {

    private Student selectedStudent;
    private EditText tStudentName;
    private Button btnStartTest;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate((R.layout.fragment_test_start), container, false);

        if(!getResources().getBoolean(R.bool.isTablet)){
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        //get data
        Bundle bundle = this.getArguments();
        selectedStudent = (Student) bundle.getSerializable("selectedStudent");

        //set selected student name
        tStudentName = (EditText) view.findViewById(R.id.txtStudentName);
        tStudentName.setText(selectedStudent.getFirstName() + " " + selectedStudent.getLastName());

        //start test button
        btnStartTest = (Button) view.findViewById(R.id.btnStartTest);
        btnStartTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startTestIntent = new Intent(getActivity(), TestActivity.class);
                startTestIntent.putExtra("selectedStudent", selectedStudent);
                startActivityForResult(startTestIntent, MainActivity.LAUNCH_TEST);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.LAUNCH_TEST && resultCode == getActivity().RESULT_OK) {

            ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_Students);
            Fragment fragmentStudent = new StudentsFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setReorderingAllowed(true);
            transaction.replace(R.id.fragmentContainer, fragmentStudent, null)
                    .commit();
        }
    }
}
