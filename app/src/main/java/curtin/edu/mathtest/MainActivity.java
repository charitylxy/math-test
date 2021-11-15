package curtin.edu.mathtest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import curtin.edu.mathtest.fragments.results.ResultsFragment;
import curtin.edu.mathtest.fragments.students.StudentsFragment;
import curtin.edu.mathtest.fragments.test.TestFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout navDrawerLayout;
    private ActionBarDrawerToggle navToggle;

    public static final int REQUEST_CONTACT = 2;
    public static final int REQUEST_READ_CONTACT_PERMISSION = 3;
    public static final int REQUEST_PHOTO = 4;
    public static final int REQUEST_GALLERY = 5;
    public static final int LAUNCH_SEARCH_IMAGES = 6;
    public static final int LAUNCH_TEST = 7;
    public static final int LAUNCH_EMAIL = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navDrawerLayout = (DrawerLayout) findViewById(R.id.drawLayout);
        navToggle = new ActionBarDrawerToggle(this, navDrawerLayout,
                R.string.open, R.string.close);

        navDrawerLayout.addDrawerListener(navToggle);
        navToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle(R.string.title_Students);
        FragmentManager fm = getSupportFragmentManager();
        Fragment frag = (Fragment) fm.findFragmentById(R.id.fragmentContainer);
        if(frag == null)
        {
            frag = new StudentsFragment();
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, frag)
                    .commit();
        }

        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected( MenuItem menuItem) {
        Fragment selectedFragment = null;
            switch (menuItem.getItemId()){
                case R.id.ic_students:
                    getSupportActionBar().setTitle(R.string.title_Students);
                    selectedFragment = new StudentsFragment();
                    break;

                case R.id.ic_test:
                    getSupportActionBar().setTitle(R.string.title_Test);
                    selectedFragment = new TestFragment();
                    break;

                case R.id.ic_results:
                    getSupportActionBar().setTitle(R.string.title_Results);
                    selectedFragment = new ResultsFragment();
                    break;
            }
            goToFragment(selectedFragment);
            navDrawerLayout.closeDrawer(GravityCompat.START,true);
            return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (navToggle.onOptionsItemSelected(item)) {
            return true;
        }
        else{
            return false;
        }
    }



    private void goToFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer,fragment)
                .commit();
    }

    public void exitConfirmation (){
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Exit application");
        dialog.setMessage("Are you sure you want to exit?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.super.onBackPressed();
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

    @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            exitConfirmation();
        } else {
            getSupportFragmentManager().popBackStack();
        }

    }


}