package curtin.edu.mathtest.fragments.students;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import curtin.edu.mathtest.classes.Student;
import curtin.edu.mathtest.sql_database.StudentDBModel;
import de.hdodenhof.circleimageview.CircleImageView;

public class StudentAddFragment extends Fragment {

    public static final String TAG = "StudentAddFragment";
    private StudentDBModel studentDBModel;

    private EditText tFirstName , tLastName;
    private FloatingActionButton phoneFAB, emailFAB, photoFAB;
    private Button btnAddStudent;
    private CircleImageView photoView;
    private RecyclerView phoneRV, emailRV;
    private RecyclerView.Adapter phoneAdapter, emailAdapter;

    private List<String> phoneList ;
    private List<String>  emailList ;
    private int contactId;
    private String firstname, lastname;
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

        //load database
        studentDBModel = new StudentDBModel();
        studentDBModel.load(getActivity().getApplicationContext());

        //initialization of textbox and button
        tFirstName = (EditText)view.findViewById(R.id.txtFirstName);
        tLastName = (EditText) view.findViewById(R.id.txtLastName);
        phoneFAB = (FloatingActionButton) view.findViewById(R.id.btnAddPhone);
        emailFAB = (FloatingActionButton) view.findViewById(R.id.btnAddEmail);
        photoFAB = (FloatingActionButton) view.findViewById(R.id.btnAddPic);
        photoView = (CircleImageView) view.findViewById(R.id.imgProfile) ;

        //phone and email arraylist
        phoneList = new ArrayList<>();
        emailList = new ArrayList<>();

        //phone recycler view
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
        //add phone number
        phoneFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (phoneList.size() < 10) {
                    phoneList.add("");
                    phoneAdapter.notifyItemInserted(phoneList.size() - 1);
                    Log.d(TAG, "Add Phone: " + Integer.toString(phoneList.size() - 1));
                }
                else {
                    Toast.makeText(getActivity(),"Only 10 Phone Numbers can be stored!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //add email
        emailFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emailList.size() < 10) {
                    emailList.add("");
                    emailAdapter.notifyItemInserted(emailList.size() - 1);
                    Log.d(TAG, "Add Email: " + Integer.toString(emailList.size() - 1));
                }
                else {
                    Toast.makeText(getActivity(),"Only 10 Emails can be stored!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //add student
        btnAddStudent = (Button) view.findViewById(R.id.btnConfirmAD);
        btnAddStudent.setText("Add");
        btnAddStudent.setBackground(getResources().getDrawable(R.drawable.button_style, getActivity().getTheme()));
        btnAddStudent.setTextColor(getResources().getColor(R.color.white, getActivity().getTheme()));
        btnAddStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addStudent();

            }
        });

        return view;
    }
    //STUDENT PHOTO OPTIONS
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

    //ADD STUDENT
    private void addStudent (){
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
            Student student = new Student(firstname,lastname,phoneList, emailList);
            studentDBModel.add(student);

            try {
                if (photo!=null) {
                    getFileDirectory(student.getId());
                    OutputStream fOut = new FileOutputStream(photoFile);
                    photo.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                }
            }
            catch (FileNotFoundException e){
                e.printStackTrace();
            }
            Log.d(TAG, "Student Added.");
            Toast.makeText(getActivity(),"New student added.",Toast.LENGTH_SHORT).show();
            returnFragment();
        }
    }

    private void returnFragment(){
        Fragment fragmentReturn = new StudentsFragment();
        getActivity().getSupportFragmentManager().popBackStack();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.replace(R.id.fragmentContainer,fragmentReturn , null)
                .commit();
    }

    //IMPORT CONTACT
    private void importContactDetails(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent,MainActivity.REQUEST_CONTACT);
    }

    private void showName(){
        Uri dataUri = ContactsContract.Data.CONTENT_URI;
        String[] queryFields = new String[] {
                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME
        };

        String whereClause = ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID
                + "=? and " + ContactsContract.CommonDataKinds.StructuredName.MIMETYPE +"=?";
        String [] whereValues = new String[]{
                String.valueOf(this.contactId),
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
        };
        Cursor c = getActivity().getContentResolver().query(
                dataUri, queryFields, whereClause,whereValues, null);
        try{
            c.moveToFirst();
            do{
                firstname = c.getString(0);
                lastname = c.getString(1);

            }
            while (c.moveToNext());

        }
        finally {
            c.close();
        }

        tFirstName.setText(firstname);
        tLastName.setText(lastname);
    }

    private void showEmail(){
        Uri emailUri = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        String[] queryFields = new String[] {
                ContactsContract.CommonDataKinds.Email.ADDRESS
        };

        String whereClause = ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?";
        String [] whereValues = new String[]{
                String.valueOf(this.contactId)
        };
        Cursor c = getActivity().getContentResolver().query(
                emailUri, queryFields, whereClause,whereValues, null);

        try{
            c.moveToFirst();
            while (!c.isAfterLast()){
                String emailAddress = c.getString(0);
                emailList.add(emailAddress);
                c.moveToNext();
            }

        }
        finally {
            c.close();
        }
        emailAdapter.notifyDataSetChanged();
    }

    private void showContact(){
        Uri contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] queryFields = new String[] {
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        String whereClause = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
        String [] whereValues = new String[]{
                String.valueOf(this.contactId)
        };
        Cursor c = getActivity().getContentResolver().query(
                contactUri, queryFields, whereClause,whereValues, null);
        try{
            c.moveToFirst();
            while (!c.isAfterLast()){
                String contactNum = c.getString(0);
                phoneList.add(contactNum);
                c.moveToNext();
            }
        }
        finally {
            c.close();
        }

        phoneAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode==MainActivity.REQUEST_READ_CONTACT_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Contact Reading Permission Granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MainActivity.REQUEST_CONTACT && resultCode == Activity.RESULT_OK){

            Uri contactUri = data.getData();
            String[] queryFields = new String[] {
                    ContactsContract.Contacts._ID,
            };
            Cursor c = getActivity().getContentResolver().query(
                    contactUri, queryFields, null, null, null);
            try {
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    this.contactId = c.getInt(0);
                }
            }
            finally {
                c.close();
            }
            showName();
            showEmail();
            showContact();
        }

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


    //PHONE ADAPTER
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


    //EMAIL ADAPTER
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

    //MENU BAR
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.import_contact_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.ic_import:
                //request permission first (if not yet)
                if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS},
                            MainActivity.REQUEST_READ_CONTACT_PERMISSION);
                }

                importContactDetails();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
