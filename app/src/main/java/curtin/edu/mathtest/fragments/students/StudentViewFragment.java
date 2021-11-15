package curtin.edu.mathtest.fragments.students;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import curtin.edu.mathtest.R;
import curtin.edu.mathtest.classes.Student;
import curtin.edu.mathtest.sql_database.ResultDBModel;
import curtin.edu.mathtest.sql_database.StudentDBModel;
import de.hdodenhof.circleimageview.CircleImageView;

public class StudentViewFragment extends Fragment {
    public static final String TAG = "StudentEditFragment";

    private ResultDBModel resultDBModel;
    private Student selectedStudent;

    private EditText tFirstName , tLastName;
    private TextView ttlPhone, ttlEmail;
    private CircleImageView photoView;
    private FloatingActionButton btnTestHistory;
    private RecyclerView phoneRV, emailRV;
    private RecyclerView.Adapter phoneAdapter, emailAdapter;

    private List<String> phoneList ;
    private List<String>  emailList ;
    private String firstname, lastname;

    Boolean isTablet;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate((R.layout.fragment_student_view), container, false);

        //get data
        Bundle bundle = this.getArguments();
        selectedStudent = (Student) bundle.getSerializable("selectedStudent");

        resultDBModel = new ResultDBModel();
        resultDBModel.load(getActivity().getApplicationContext());

        //initialization of textbox and buttons
        tFirstName = (EditText)view.findViewById(R.id.txtFirstNameV);
        tLastName = (EditText) view.findViewById(R.id.txtLastNameV);

        // set student photo
        photoView = (CircleImageView) view.findViewById(R.id.imgProfileV);
        String photoFileName = selectedStudent.getId();
        File photoFile = new File(getActivity().getFilesDir(),photoFileName+".png");
        if (photoFile.exists()){
            Bitmap photo = BitmapFactory.decodeFile(photoFile.toString());
            photoView.setImageBitmap(photo);
        }
        else {
            photoView.setImageDrawable(getResources().getDrawable(R.drawable.default_profile, getActivity().getTheme()));
        }

        //set student name
        firstname  = selectedStudent.getFirstName();
        tFirstName.setText(firstname);

        lastname = selectedStudent.getLastName();
        tLastName.setText(lastname);

        //set phone and email lists
        ttlPhone = (TextView) view.findViewById(R.id.ttlPhoneV);
        phoneList = new ArrayList<>();
        phoneList.addAll(selectedStudent.getPhone());
        if (phoneList.isEmpty()){
            ttlPhone.setVisibility(View.INVISIBLE);
        }

        ttlEmail = (TextView) view.findViewById(R.id.ttlEmailV);
        emailList = new ArrayList<>();
        emailList.addAll(selectedStudent.getEmail());
        if (emailList.isEmpty()){
            ttlEmail.setVisibility(View.INVISIBLE);
        }

        // phone recycler view
        phoneRV = (RecyclerView) view.findViewById(R.id.phoneRVV);
        phoneRV.setLayoutManager(new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false));
        phoneAdapter = new PhoneAdapter();
        phoneRV.setAdapter(phoneAdapter);

        // email recycler view
        emailRV = (RecyclerView) view.findViewById(R.id.emailRVV);
        emailRV.setLayoutManager(new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false));
        emailAdapter = new EmailAdapter();
        emailRV.setAdapter(emailAdapter);

        //get device type (tablet or phone)
        isTablet = getResources().getBoolean(R.bool.isTablet);
        if(isTablet){
            Bundle bundle2 = new Bundle();
            bundle2.putSerializable("selectedStudent",selectedStudent);
            Fragment fragmentTestHistory = new TestHistoryFragment();
            fragmentTestHistory.setArguments(bundle2);

            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.setReorderingAllowed(true);
            transaction.replace(R.id.testHistoryContainer,fragmentTestHistory , null)
                    .commit();
        }

        // test history button
        btnTestHistory = (FloatingActionButton) view.findViewById(R.id.btnTestHistory);
        if (isTablet || resultDBModel.getStudentResult(selectedStudent.getId()).isEmpty()){
            btnTestHistory.hide();
        }
        else {
            btnTestHistory.show();
        }

        btnTestHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testHistory();
            }
        });

        return view;
    }

    private void testHistory(){
        Bundle bundle = new Bundle();

        bundle.putSerializable("selectedStudent", selectedStudent);
        Fragment fragmentTestHistory = new TestHistoryFragment();
        fragmentTestHistory.setArguments(bundle);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack("TestHistoryState");
        transaction.setReorderingAllowed(true);
        transaction.replace(R.id.fragmentContainer,fragmentTestHistory , null)
                .commit();
    }

    private void editFragment(){
        Bundle bundle = new Bundle();

        bundle.putSerializable("selectedStudent", selectedStudent);
        Fragment fragmentEditStudent = new StudentEditFragment();
        fragmentEditStudent.setArguments(bundle);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack("StudentViewState");
        transaction.setReorderingAllowed(true);
        transaction.replace(R.id.fragmentContainer,fragmentEditStudent , null)
                .commit();
    }


    // PHONE ADAPTER
    public class PhoneViewHolder extends RecyclerView.ViewHolder {
        private EditText tPhone;

        public PhoneViewHolder(LayoutInflater inflater, ViewGroup view) {
            super(inflater.inflate(R.layout.list_phone_view, view, false));

            tPhone = (EditText) itemView.findViewById(R.id.txtPhone);
        }

        public void bind(String phoneNum) {
            tPhone.setText(phoneNum);
        }
    }

    public class PhoneAdapter extends RecyclerView.Adapter<PhoneViewHolder> {
        @NonNull
        @Override
        public PhoneViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new PhoneViewHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(PhoneViewHolder holder, int position) {
            holder.bind(phoneList.get(position));
        }

        @Override
        public int getItemCount() {
            return phoneList.size();
        }
    }

    // EMAIL ADAPTER
    private class EmailViewHolder extends RecyclerView.ViewHolder {
        private EditText tEmail;
        private FloatingActionButton deleteEmail;

        public EmailViewHolder(LayoutInflater inflater, ViewGroup view) {
            super(inflater.inflate(R.layout.list_email_view, view, false));

            tEmail = (EditText) itemView.findViewById(R.id.txtEmail);
        }

        public void bind(String email) {
            tEmail.setText(email);
        }
    }

    public class EmailAdapter extends RecyclerView.Adapter<EmailViewHolder> {

        @NonNull
        @Override
        public EmailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());;
            return new EmailViewHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(EmailViewHolder holder, int position) {
            holder.bind(emailList.get(position));
        }

        @Override
        public int getItemCount() {
            return emailList.size();
        }
    }

   // MENU BAR
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_student_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.ic_edit:
                editFragment();
                Log.d(TAG, "Student editable");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
