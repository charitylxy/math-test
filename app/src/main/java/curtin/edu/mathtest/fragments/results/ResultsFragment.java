package curtin.edu.mathtest.fragments.results;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import curtin.edu.mathtest.MainActivity;
import curtin.edu.mathtest.R;
import curtin.edu.mathtest.classes.Result;
import curtin.edu.mathtest.classes.Student;
import curtin.edu.mathtest.sql_database.ResultDBModel;
import curtin.edu.mathtest.sql_database.StudentDBModel;

public class ResultsFragment extends Fragment {

    ResultDBModel resultDBModel;
    StudentDBModel studentDBModel;
    List<Result> results;
    List<Student> students;
    List<Result> emailResult;

    TextView txtNoResult;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    FloatingActionButton btnEmail, btnCancelEmail;

    Boolean selectResult;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate((R.layout.fragment_results), container, false);
        setHasOptionsMenu(true);

        resultDBModel = new ResultDBModel();
        resultDBModel.load(getActivity().getApplicationContext());

        results = new ArrayList<>();

        studentDBModel = new StudentDBModel();
        studentDBModel.load(getActivity().getApplicationContext());
        students = new ArrayList<>();
        students = studentDBModel.getAllStudents();

        selectResult = false;

        for (Student stu : students){
            List<Result> studentResults = new ArrayList<>();
            studentResults = resultDBModel.getStudentResult(stu.getId());
            if (!studentResults.isEmpty()){
                Result result = studentResults.stream().max(Comparator.comparing(Result::getScore)).get();
                results.add(result);
            }
        }
        Collections.sort(results, new ScoreComparatorHL());
        txtNoResult = (TextView) view.findViewById(R.id.txtNoResult);

        //students recycler view
        recyclerView = (RecyclerView) view.findViewById(R.id.resultListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false));
        adapter = new ResultAdapter();
        recyclerView.setAdapter(adapter);

        btnCancelEmail = (FloatingActionButton) view.findViewById(R.id.btnCancelEmail);
        btnEmail = (FloatingActionButton) view.findViewById(R.id.btnEmail);
        btnCancelEmail.hide();

        if (results.isEmpty()){
            txtNoResult.setVisibility(View.VISIBLE);
            btnEmail.hide();
        }
        else {
            txtNoResult.setVisibility(View.INVISIBLE);
            btnEmail.show();
        }

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectResult == false ) {
                    setHasOptionsMenu(false);
                    emailResult = new ArrayList<>();
                    selectResult = true;
                    adapter.notifyDataSetChanged();
                    btnEmail.setImageResource(R.drawable.ic_send);
                    btnCancelEmail.show();
                }

                else {
                    if (emailResult.isEmpty()){
                        Toast.makeText(getActivity().getApplicationContext(),"No result is selected", Toast.LENGTH_SHORT).show();
                    }
                    //get email content
                    else {
                        String content = "";
                        List<String> emailAdd = new ArrayList<>();
                        for (Result r : emailResult) {
                            Student student = studentDBModel.getStudent(r.getStudent_id());
                            emailAdd.addAll(student.getEmail());
                            content +=  student.getFirstName() + " " + student.getLastName();
                            content += "\nTest start time: " + r.getStart_time();
                            content += "\nTime taken for the test: " + r.getDuration();
                            content += "\nScore: " + new DecimalFormat("#").format(r.getScore()) + "%\n\n";
                        }
                        if (emailAdd.isEmpty()){
                            Toast.makeText(getActivity().getApplicationContext(),"Student(s) email address not provided", Toast.LENGTH_SHORT).show();
                        }
                        //send email
                        else {
                            String[] email = new String[emailAdd.size()];
                            email = emailAdd.toArray(email);

                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                            intent.setData(Uri.parse("mailto:"));
                            if (!emailAdd.isEmpty()) {
                                intent.putExtra(Intent.EXTRA_EMAIL, email);
                            }
                            intent.putExtra(Intent.EXTRA_SUBJECT, "Students Result");
                            intent.putExtra(Intent.EXTRA_TEXT, content);
                            startActivityForResult(Intent.createChooser(intent, "Choose an Email client :"), MainActivity.LAUNCH_EMAIL);
                        }
                    }

                }

            }
        });

        btnCancelEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setHasOptionsMenu(true);
                selectResult = false;
                adapter.notifyDataSetChanged();
                btnEmail.setImageResource(R.drawable.ic_email);
                btnCancelEmail.hide();
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MainActivity.LAUNCH_EMAIL) {
            //Called when returning from your email intent
            setHasOptionsMenu(true);
            selectResult = false;
            adapter.notifyDataSetChanged();
            btnEmail.setImageResource(R.drawable.ic_email);
            btnCancelEmail.hide();
        }
    }

    //STUDENT ADAPTER
    private class ResultViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgAccount;
        private TextView txtName, txtStarttime, txtDuration, txtScore;
        private CheckBox emailCheckBox;
        private Result result = null;
        Student student = null;

        public ResultViewHolder(LayoutInflater inflater, ViewGroup view) {
            super(inflater.inflate(R.layout.list_results, view, false));

            imgAccount = (ImageView) itemView.findViewById(R.id.imgStu);
            txtName = (TextView) itemView.findViewById(R.id.txtStuName);
            txtStarttime = (TextView) itemView.findViewById(R.id.testStartTime);
            txtDuration = (TextView) itemView.findViewById(R.id.testDuration);
            txtScore = (TextView) itemView.findViewById(R.id.testScore);
            emailCheckBox = (CheckBox) itemView.findViewById(R.id.checkBox) ;

            emailCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (emailCheckBox.isChecked()){
                        emailResult.add(results.get(getAdapterPosition()));
                    }
                    else {
                        emailResult.remove(results.get(getAdapterPosition()));
                    }
                }
            });

        }

        public void bind(Result result) {
            this.result = result;
            student = studentDBModel.getStudent(result.getStudent_id());

            //set name
            txtName.setText(student.getFirstName() + " " + student.getLastName());

            //set student image
            String photoFileName = student.getId();
            File photoFile = new File(getActivity().getFilesDir(),photoFileName+".png");
            if (photoFile.exists()){
                Bitmap photo = BitmapFactory.decodeFile(photoFile.toString());
                imgAccount.setImageBitmap(photo);
            }
            else {
                imgAccount.setImageDrawable(getResources().getDrawable(R.drawable.default_profile, getActivity().getTheme()));
            }

            //set result deets
            txtStarttime.setText (result.getStart_time());
            txtDuration.setText("Test Duration : " + result.getDuration());
            txtScore.setText(new DecimalFormat("#").format(result.getScore()) + "%");

            if (selectResult == true){
                emailCheckBox.setVisibility(View.VISIBLE);
                emailCheckBox.setChecked(false);
            }
            else {
                emailCheckBox.setVisibility(View.GONE);
            }
        }
    }

    private class ResultAdapter extends RecyclerView.Adapter<ResultViewHolder> {

        @NonNull
        @Override
        public ResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ResultViewHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
            holder.bind(results.get(position));
        }

        @Override
        public int getItemCount() {
            return results.size();
        }
    }

    // MENU BAR
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sort_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.ic_highlow:
                Collections.sort(results, new ScoreComparatorHL());
                adapter.notifyDataSetChanged();
                return true;

            case R.id.ic_lowhigh:
                Collections.sort(results, new ScoreComparatorLH());
                adapter.notifyDataSetChanged();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
