package curtin.edu.mathtest.fragments.students;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
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
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import curtin.edu.mathtest.MainActivity;
import curtin.edu.mathtest.R;
import curtin.edu.mathtest.classes.Result;
import curtin.edu.mathtest.classes.Student;
import curtin.edu.mathtest.sql_database.ResultDBModel;
import curtin.edu.mathtest.sql_database.StudentDBModel;
import de.hdodenhof.circleimageview.CircleImageView;

public class StudentEditFragment extends Fragment {
    public static final String TAG = "StudentEditFragment";

    private StudentDBModel studentDBModel;
    private Student selectedStudent;
    private ResultDBModel resultDBModel;

    private EditText tFirstName , tLastName;
    private FloatingActionButton phoneFAB, emailFAB , photoFAB;
    private Button btnDeleteStudent;
    private CircleImageView photoView;
    private RecyclerView phoneRV, emailRV;
    private RecyclerView.Adapter phoneAdapter, emailAdapter;

    private List<String> phoneList ;
    private List<String>  emailList ;
    private String firstname, lastname, photoFileName;
    private File photoFile;
    private Bitmap photo;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate((R.layout.fragment_student_aed), container, false);
        setHasOptionsMenu(true);

        //get data
        Bundle bundle = this.getArguments();
        selectedStudent = (Student) bundle.getSerializable("selectedStudent");

        //load database
        studentDBModel = new StudentDBModel();
        studentDBModel.load(getActivity().getApplicationContext());
        resultDBModel = new ResultDBModel();
        resultDBModel.load(getActivity().getApplicationContext());

        //initialization of textbox and buttons
        tFirstName = (EditText)view.findViewById(R.id.txtFirstName);
        tLastName = (EditText) view.findViewById(R.id.txtLastName);
        phoneFAB = (FloatingActionButton) view.findViewById(R.id.btnAddPhone);
        emailFAB = (FloatingActionButton) view.findViewById(R.id.btnAddEmail);
        photoFAB = (FloatingActionButton) view.findViewById(R.id.btnAddPic);
        photoView = (CircleImageView) view.findViewById(R.id.imgProfile) ;

        // set photo
        photoFileName = selectedStudent.getId();
        photoFile = new File(getActivity().getFilesDir(),photoFileName+".png");
        if (photoFile.exists()){
            photo = BitmapFactory.decodeFile(photoFile.toString());
            photoView.setImageBitmap(photo);
        }
        else {
            photoView.setImageDrawable(getResources().getDrawable(R.drawable.default_profile, getActivity().getTheme()));
        }

        //set textboxes
        firstname  = selectedStudent.getFirstName();
        tFirstName.setText(firstname);

        lastname = selectedStudent.getLastName();
        tLastName.setText(lastname);

        //set phone and email lists
        phoneList = new ArrayList<>();
        phoneList.addAll(selectedStudent.getPhone());

        emailList = new ArrayList<>();
        emailList.addAll(selectedStudent.getEmail());

        // phone recycler view
        phoneRV = (RecyclerView) view.findViewById(R.id.phoneRV);
        phoneRV.setLayoutManager(new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false));
        phoneAdapter = new PhoneAdapter();
        phoneRV.setAdapter(phoneAdapter);

        // email recycler view
        emailRV = (RecyclerView) view.findViewById(R.id.emailRV);
        emailRV.setLayoutManager(new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false));
        emailAdapter = new EmailAdapter();
        emailRV.setAdapter(emailAdapter);

        //add phone numbers
        phoneFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (phoneList.size() < 10) {
                    phoneList.add("");
                    phoneAdapter.notifyItemInserted(phoneList.size() - 1);
                    Log.d(TAG, "Add Phone: " + Integer.toString(phoneList.size() - 1));
                } else {
                    Toast.makeText(getActivity(), "Only 10 Phone Numbers can be stored!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //add emails
        emailFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emailList.size() < 10) {
                    emailList.add("");
                    emailAdapter.notifyItemInserted(emailList.size() - 1);
                    Log.d(TAG, "Add Email: " + Integer.toString(emailList.size() - 1));
                } else {
                    Toast.makeText(getActivity(), "Only 10 Emails can be stored!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //add photo
        photoFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(getActivity(), view);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.photo_live:
                                takePhoto();
                                return true;

                            case R.id.photo_gallery:
                                selectGallery();
                                return true;

                            case R.id.photo_internet:
                                searchOnline();
                                return true;

                            default:
                                return false;
                        }
                    }
                });
                popup.inflate(R.menu.photo_option_menu);
                popup.show();
            }
        });
        
        //delete student
        btnDeleteStudent = (Button) view.findViewById(R.id.btnConfirmAD);
        btnDeleteStudent.setText("Delete Student");
        btnDeleteStudent.setBackground(getResources().getDrawable(R.drawable.button_delete, getActivity().getTheme()));
        btnDeleteStudent.setTextColor(getResources().getColor(R.color.red,getActivity().getTheme()));
        btnDeleteStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteConfirmation();
            }
        });

        return view;
    }

    //STUDENT PHOTO OPTION
    public void getFileDirectory(String photoFileName){
        photoFile = new File(getActivity().getFilesDir(),photoFileName+".png");
    }

    public void takePhoto(){
        Intent photoIntent = new Intent();
        photoIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(photoIntent, MainActivity.REQUEST_PHOTO);
    }

    public void selectGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, MainActivity.REQUEST_GALLERY);
    }

    public void searchOnline(){
        Intent searchIntent = new Intent(getActivity(), SearchImagesActivity.class);
        startActivityForResult(searchIntent, MainActivity.LAUNCH_SEARCH_IMAGES);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MainActivity.REQUEST_PHOTO && resultCode == getActivity().RESULT_OK){
             photo = (Bitmap) data.getExtras().get("data");
             if (photo!=null){
                 photoView.setImageBitmap(photo);
             }
        }

        if(requestCode == MainActivity.REQUEST_GALLERY && resultCode == getActivity().RESULT_OK){
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);

                photo = BitmapFactory.decodeStream(imageStream);
                photoView.setImageBitmap(photo);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == MainActivity.LAUNCH_SEARCH_IMAGES && resultCode == getActivity().RESULT_OK){

            String filename = data.getStringExtra("selectedPhoto");
            try {
                FileInputStream is = getActivity().getApplicationContext().openFileInput(filename);
                photo = BitmapFactory.decodeStream(is);
                photoView.setImageBitmap(photo);
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //EDIT AND DELETE STUDENT
    private void returnFragment(){
        Fragment fragmentReturn = new StudentsFragment();
        int count = getActivity().getSupportFragmentManager().getBackStackEntryCount();
        for (int i = 0; i < count; i++){
            getActivity().getSupportFragmentManager().popBackStack();
        }
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.replace(R.id.fragmentContainer,fragmentReturn , null)
                .commit();
    }

    private void editDetails(){
        Boolean check = true;
        firstname = tFirstName.getText().toString();
        lastname = tLastName.getText().toString();

        if (TextUtils.isEmpty(firstname)){
            tFirstName.setError("First name cannot be empty");
            check = false;
        }
        if (TextUtils.isEmpty(lastname)){
            tLastName.setError( ("Last Name cannot be empty"));
            check = false;
        }

        if (check == true){

            // remove empty items
            for (int i = 0 ; i < phoneList.size(); i++){
                if (phoneList.get(i).equals("")){
                    phoneList.remove(i);
                }
            }
            for (int i = 0 ; i < emailList.size(); i++){
                if (emailList.get(i).equals("")){
                    emailList.remove(i);
                }
            }

            try {
                if (photo!=null) {
                    getFileDirectory(selectedStudent.getId());
                    OutputStream fOut = new FileOutputStream(photoFile);
                    photo.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                }
            }
            catch (FileNotFoundException e){
                e.printStackTrace();
            }

            selectedStudent.setFirstName(firstname);
            selectedStudent.setLastName(lastname);
            selectedStudent.setPhone(phoneList);
            selectedStudent.setEmail(emailList);
            studentDBModel.edit(selectedStudent);
            Log.d(TAG, "Student edit saved");
            Toast.makeText(getActivity(),"Edit saved.",Toast.LENGTH_SHORT).show();
            returnFragment();
        }
    }

    public void deleteConfirmation (){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Delete Confirmation");
        dialog.setMessage("Are you sure you want to delete this student? ");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               deleteStudent();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void deleteStudent(){
        List<Result> studentResult = new ArrayList<>();
        studentResult = resultDBModel.getStudentResult(selectedStudent.getId());
        for (Result r : studentResult){
            resultDBModel.remove(r);
        }

        getFileDirectory(selectedStudent.getId());
        if (photoFile.exists()){
            photoFile.delete();
        }
        studentDBModel.remove(selectedStudent);
        Log.d(TAG, "Student Deleted.");
        Toast.makeText(getActivity(),"student deleted.",Toast.LENGTH_SHORT).show();
        returnFragment();
    }


    // PHONE ADAPTER
    public class PhoneViewHolder extends RecyclerView.ViewHolder {
        private EditText tPhone;
        private FloatingActionButton deletePhone;

        public PhoneViewHolder(LayoutInflater inflater, ViewGroup view) {
            super(inflater.inflate(R.layout.list_phone, view, false));

            tPhone = (EditText) itemView.findViewById(R.id.txtPhone);
            deletePhone = (FloatingActionButton) itemView.findViewById(R.id.btnDelPhone);


            tPhone.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    phoneList.set(getAdapterPosition(),tPhone.getText().toString());
                    Log.d(TAG,"Update Phone: " + Integer.toString(getAdapterPosition())
                            + ": " + tPhone.getText().toString());
                }
            });

            deletePhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.d(TAG,"Remove Phone: " + Integer.toString(getAdapterPosition()) +
                            ": " + phoneList.get(getAdapterPosition()));
                    phoneList.remove(getAdapterPosition());
                    phoneAdapter.notifyDataSetChanged();
                    phoneAdapter.notifyItemRemoved(getAdapterPosition());
                }
            });
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
            super(inflater.inflate(R.layout.list_email, view, false));

            tEmail = (EditText) itemView.findViewById(R.id.txtEmail);
            deleteEmail = (FloatingActionButton) itemView.findViewById(R.id.btnDelEmail);

            tEmail.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    emailList.set(getAdapterPosition(),tEmail.getText().toString());
                    Log.d(TAG,"Update Email: " + Integer.toString(getAdapterPosition())
                            + ": " + tEmail.getText().toString());
                }
            });

            deleteEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG,"Remove Phone: " + Integer.toString(getAdapterPosition()) +
                            ": " + emailList.get(getAdapterPosition()));
                    emailList.remove(getAdapterPosition());
                    emailAdapter.notifyDataSetChanged();
                    emailAdapter.notifyItemRemoved(getAdapterPosition());
                }
            });


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
        inflater.inflate(R.menu.save_student_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.ic_save:
                editDetails();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }




}
