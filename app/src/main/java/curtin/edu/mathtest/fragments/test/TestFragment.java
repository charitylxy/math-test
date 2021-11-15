package curtin.edu.mathtest.fragments.test;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import curtin.edu.mathtest.R;
import curtin.edu.mathtest.classes.Student;
import curtin.edu.mathtest.fragments.students.StudentViewFragment;
import curtin.edu.mathtest.fragments.students.StudentsFragment;
import curtin.edu.mathtest.sql_database.StudentDBModel;

public class TestFragment extends Fragment {
    private StudentDBModel studentDBModel;
    private Student selectedStudent = null;
    private List<Student> students;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private TextView txtNoStudent;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate((R.layout.fragment_test), container, false);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        //load database
        studentDBModel = new StudentDBModel();
        studentDBModel.load(getActivity().getApplicationContext());

        //get students list
        students = new ArrayList<>();
        students = studentDBModel.getAllStudents();

        txtNoStudent = (TextView) view.findViewById(R.id.txtNoStudentTest);
        if (students.isEmpty()){
            txtNoStudent.setVisibility(View.VISIBLE);
        }
        else {
            txtNoStudent.setVisibility(View.INVISIBLE);
        }

        //students recycler view
        recyclerView = (RecyclerView) view.findViewById(R.id.studentTestListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false));
        adapter = new StudentAdapter();
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void startTest(){
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedStudent", selectedStudent);
        Fragment fragmentStartTest = new StartTestFragment();
        fragmentStartTest.setArguments(bundle);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack("TestFragment");
        transaction.setReorderingAllowed(true);
        transaction.replace(R.id.fragmentContainer,fragmentStartTest , null)
                .commit();
    }

    //STUDENT ADAPTER
    private class StudentViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgAccount;
        private TextView txtName;
        private Student student = null;

        public StudentViewHolder(LayoutInflater inflater, ViewGroup view) {
            super(inflater.inflate(R.layout.list_students, view, false));

            imgAccount = (ImageView) itemView.findViewById(R.id.imgStu);
            txtName = (TextView) itemView.findViewById(R.id.txtStuName);

            itemView.setOnClickListener(view1 -> {
                selectedStudent = student;
                startTest();
            });

        }

        public void bind(Student student) {
            this.student = student;
            //set student image
            txtName.setText(student.getFirstName() + " " + student.getLastName());

            String photoFileName = student.getId();
            File photoFile = new File(getActivity().getFilesDir(),photoFileName+".png");
            if (photoFile.exists()){
                Bitmap photo = BitmapFactory.decodeFile(photoFile.toString());
                imgAccount.setImageBitmap(photo);
            }
            else {
                imgAccount.setImageDrawable(getResources().getDrawable(R.drawable.default_profile, getActivity().getTheme()));
            }
        }
    }

    private class StudentAdapter extends RecyclerView.Adapter<StudentViewHolder> {

        @NonNull
        @Override
        public StudentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new StudentViewHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
            holder.bind(students.get(position));
        }

        @Override
        public int getItemCount() {
            return students.size();
        }
    }
}
