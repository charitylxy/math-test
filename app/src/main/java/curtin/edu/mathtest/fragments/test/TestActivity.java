package curtin.edu.mathtest.fragments.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import curtin.edu.mathtest.R;
import curtin.edu.mathtest.classes.Question;
import curtin.edu.mathtest.classes.Result;
import curtin.edu.mathtest.classes.Student;
import curtin.edu.mathtest.fragments.test.options.Option2Fragment;
import curtin.edu.mathtest.fragments.test.options.Option3Fragment;
import curtin.edu.mathtest.fragments.test.options.Option4Fragment;
import curtin.edu.mathtest.sql_database.ResultDBModel;

import static android.text.format.DateUtils.formatElapsedTime;

public class TestActivity extends AppCompatActivity implements PassDataInterface {

    private static final String TAG = "TestActivity";
    public static final String BASE_URL = "https://10.0.2.2:8000/random/question";
    public static final int CORRECT = 10;
    public static final int WRONG = -5;

    private Result result;
    private ResultDBModel resultDBModel;
    private Student selectedStudent;
    private Question questionData;

    private Button btnNext, btnPass;
    public EditText  txtScore, txtAnswer;
    private Chronometer txtDuration;
    private TextView txtTimer, ttlQuestion, txtQuestion, ttlScore, ttlDuration;
    private ProgressBar timerProgress, quesStatus;
    private ViewPager optionPager;
    private OptionsAdapter optionsAdapter;
    private LinearLayout indicatorLayout;
    private TextView[] indicator;


    PassDataInterface passDataInterface;
    private Double score;
    private String startTime, duration;
    private long timer , timeWhenStopped;
    private int quesCount, optionSize;
    private CountDownTimer timeToSolve;
    private String answer;

    List<Fragment> fragmentList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        if(!getResources().getBoolean(R.bool.isTablet)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        //load database
        resultDBModel = new ResultDBModel();
        resultDBModel.load(getApplicationContext());

        //get data
        Intent data = getIntent();
        selectedStudent = (Student) data.getSerializableExtra("selectedStudent") ;


        ttlQuestion = (TextView) findViewById(R.id.ttlQuestion);
        txtQuestion= (TextView) findViewById(R.id.txtQuestion);
        txtTimer= (TextView) findViewById(R.id.timerText);
        ttlDuration = (TextView) findViewById(R.id.ttlTotalTimePass);
        txtDuration = (Chronometer) findViewById(R.id.txtTotalTimePass);
        ttlScore = (TextView) findViewById(R.id.ttlTotalScore);
        txtScore =(EditText) findViewById(R.id.txtTotalScore);
        txtAnswer = (EditText) findViewById(R.id.txtAnswer);
        timerProgress = (ProgressBar) findViewById(R.id.timerProgress) ;
        btnPass = (Button) findViewById(R.id.btnPassQues);
        btnNext = (Button) findViewById(R.id.btnNextQues);
        optionPager = (ViewPager) findViewById(R.id.optionsView);
        indicatorLayout = (LinearLayout) findViewById(R.id.indicator_container) ;
        quesStatus =(ProgressBar) findViewById(R.id.quesStatus);


        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",
                Locale.getDefault());
        startTime = sdf.format(new Date());
        duration = "0.0";
        score = 0.0;
        quesCount = 1;
        answer ="";

        hideQuestion();

        txtDuration.start();
        //stop timer when downloading question
        timeWhenStopped = 0;
        timeWhenStopped = txtDuration.getBase() - SystemClock.elapsedRealtime();
        txtDuration.stop();
        new DownloaderTask().execute();

    }

    private void hideQuestion(){
        //hide test questions
        txtQuestion.setVisibility(View.INVISIBLE);
        ttlQuestion.setVisibility(View.INVISIBLE);
        txtTimer.setVisibility(View.INVISIBLE);
        btnPass.setVisibility(View.INVISIBLE);
        btnNext.setVisibility(View.INVISIBLE);
        timerProgress.setVisibility(View.INVISIBLE);
        txtAnswer.setVisibility(View.INVISIBLE);
        optionPager.setVisibility(View.INVISIBLE);
        ttlScore.setVisibility(View.INVISIBLE);
        txtScore.setVisibility(View.INVISIBLE);
        ttlDuration.setVisibility(View.INVISIBLE);
        txtDuration.setVisibility(View.INVISIBLE);
        quesStatus.setVisibility(View.VISIBLE);
    }

    private void showQuestion (){
        //make question visible
        txtQuestion.setVisibility(View.VISIBLE);
        ttlQuestion.setVisibility(View.VISIBLE);
        txtTimer.setVisibility(View.VISIBLE);
        btnPass.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);
        timerProgress.setVisibility(View.VISIBLE);
        ttlScore.setVisibility(View.VISIBLE);
        txtScore.setVisibility(View.VISIBLE);
        ttlDuration.setVisibility(View.VISIBLE);
        txtDuration.setVisibility(View.VISIBLE);
        quesStatus.setVisibility(View.INVISIBLE);
    }

    //DOWNLOAD QUESTION FROM SERVER
    private class DownloaderTask extends AsyncTask<Void,Integer, Question> {

        @Override
        protected Question doInBackground(Void... voids) {
            try{
                String urlString = Uri.parse (BASE_URL)
                        .buildUpon()
                        .appendQueryParameter("method", "thedata.getit")
                        .appendQueryParameter("format", "json")
                        .appendQueryParameter("api_key", "01189998819991197253")
                        .build().toString();

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                DownloadUtils.addCertificate(TestActivity.this, (HttpsURLConnection)conn);

                try {
                    Log.d(TAG, "Connecting");
                    int responseCode = conn.getResponseCode();
                    String responseMSG = conn.getResponseMessage();
                    if (responseCode!= HttpsURLConnection.HTTP_OK){
                        String msg = String.format(
                                "Errir from Server: %d - %s",
                                responseCode, responseMSG
                        );

                        Log.d(TAG, msg);
                        throw new IOException(msg);
                    }
                    InputStream is = conn.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    int totalByteRead = 0;
                    int byteRead = 0;
                    byte[] buffer = new byte[1024];
                    while ((byteRead = is.read(buffer))> 0){
                        baos.write(buffer,0,byteRead);
                        totalByteRead+= byteRead;
                        publishProgress(totalByteRead);
                    }

                    // Parsing JSON - get questions and store them
                    try{
                        questionData = new Question();
                        List<Integer> options = new ArrayList<>();
                        JSONObject jQuestion = new JSONObject(new String(baos.toByteArray()));

                        questionData.setQuestion(jQuestion.getString("question") + ": ");
                        JSONArray jOptions = jQuestion.getJSONArray("options");
                        for (int o = 0; o< jOptions.length(); o++){
                            options.add(jOptions.getInt(o));
                            Log.d("Options:", String.format("%d: %d",o, jOptions.getInt(o)));
                        }
                        questionData.setOptions(options);
                        questionData.setResult(jQuestion.getString("result"));
                        questionData.setTimeToSolve(jQuestion.getLong("timetosolve"));

                    }
                    catch(JSONException e){
                        Log.e(TAG, "JSON Exception");
                    }
                    baos.close();
                }
                finally {
                    Log.d(TAG, "Disconnect");
                }
            }
            catch (GeneralSecurityException |IOException e){
                Log.e(TAG, e.getMessage());
            }

            return questionData;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
        }

        @Override
        protected void onPostExecute(Question question) {
            //continue timer once downloaded
            if (question != null) {
                txtDuration.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                txtDuration.start();


                showQuestion();

                //OPTIONS
                List<Integer> options = new ArrayList<>();
                options.addAll(question.getOptions());
                optionSize = options.size();

                //no option or 1 option
                if (optionSize<=1){
                    txtAnswer.setVisibility(View.VISIBLE);
                    optionPager.setVisibility(View.INVISIBLE);
                }

                //multiple options
                else {
                    txtAnswer.setVisibility(View.INVISIBLE);
                    optionPager.setVisibility(View.VISIBLE);

                    optionSize = options.size();
                    Log.d(TAG, Integer.toString(optionSize));

                    //get fragments based on number of options
                    fragmentList = new ArrayList<>();
                    int currentOptionCount = 0;

                    // 4 options fragment
                    while (optionSize-4 >= 0 && optionSize != 5){
                        ArrayList<Integer>fragmentOption = new ArrayList<>();
                        for (int i = currentOptionCount; i<(currentOptionCount+4); i++){
                            fragmentOption.add(options.get(i));
                        }
                        currentOptionCount+= 4;

                        Bundle bundle = new Bundle();
                        bundle.putIntegerArrayList("options", fragmentOption);
                        bundle.putString("currentAnswer", answer);
                        Fragment fragmentOption4 = new Option4Fragment();
                        fragmentOption4.setArguments(bundle);

                        optionSize-= 4;
                        fragmentList.add(fragmentOption4);
                    }

                    //3 options fragment
                    while (optionSize-3 >= 0){
                        ArrayList<Integer>fragmentOption = new ArrayList<>();
                        for (int i = currentOptionCount; i<(currentOptionCount+3); i++){
                            fragmentOption.add(options.get(i));
                        }
                        currentOptionCount+= 3;

                        Bundle bundle = new Bundle();
                        bundle.putIntegerArrayList("options", fragmentOption);
                        bundle.putString("currentAnswer", answer);
                        Fragment fragmentOption3 = new Option3Fragment();
                        fragmentOption3.setArguments(bundle);

                        optionSize-= 3;
                        fragmentList.add(fragmentOption3);
                    }

                    //2 options fragment
                    while (optionSize-2 >= 0){
                        ArrayList<Integer>fragmentOption = new ArrayList<>();
                        for (int i = currentOptionCount; i<(currentOptionCount+2); i++){
                            fragmentOption.add(options.get(i));
                        }
                        currentOptionCount+= 2;

                        Bundle bundle = new Bundle();
                        bundle.clear();
                        bundle.putIntegerArrayList("options", fragmentOption);
                        bundle.putString("currentAnswer", answer);
                        Fragment fragmentOption2 = new Option2Fragment();
                        fragmentOption2.setArguments(bundle);

                        optionSize-= 2;
                        fragmentList.add(fragmentOption2);
                    }

                    //set viewpager adapter
                    optionsAdapter = new OptionsAdapter(getSupportFragmentManager(), fragmentList);
                    optionPager.setAdapter(optionsAdapter);

                    //set up indicators
                    indicator = new TextView[fragmentList.size()];
                    setIndicators();
                    selectedDots(0);

                    //set current page indicator
                    optionPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int i, float v, int i1) {

                        }

                        @Override
                        public void onPageSelected(int i) {
                            selectedDots(i);
                        }

                        @Override
                        public void onPageScrollStateChanged(int i) {
                        }
                    });
                }

                //QUESTIONS
                ttlQuestion.setText(String.format("Question %d", quesCount));
                txtQuestion.setText(question.getQuestion());

                //TIMER
                timerProgress.setMin(0);
                timerProgress.setMax(question.getTimeToSolve().intValue());
                timer = question.getTimeToSolve() * 1000;  //get time from server
                timeToSolve = new CountDownTimer(timer, 1000) {
                    int progress = question.getTimeToSolve().intValue();
                    public void onTick(long millisUntilFinished) {
                        txtTimer.setText(formatElapsedTime(millisUntilFinished / 1000));
                        timerProgress.setProgress(progress--);
                    }

                    public void onFinish() {
                        quesCount++;
                        answer ="";
                        timeWhenStopped = txtDuration.getBase() - SystemClock.elapsedRealtime();
                        txtDuration.stop();
                        indicatorLayout.removeAllViews();
                        new DownloaderTask().execute();
                    }
                }.start();

                //PASS QUES BUTTON
                btnPass.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        quesCount++;
                        answer ="";
                        txtAnswer.setText("");
                        indicatorLayout.removeAllViews();

                        timeToSolve.cancel();
                        timeWhenStopped = txtDuration.getBase() - SystemClock.elapsedRealtime();
                        txtDuration.stop();

                        hideQuestion();
                        new DownloaderTask().execute();
                    }
                });

                //NEXT QUES BUTTON
                btnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        quesCount++;

                        //show edit text if theres no option or 1 option
                        if (question.getOptions().size() <=1){
                            answer = txtAnswer.getText().toString();
                        }

                        //check answer & go next question
                        if (!answer.equals("")) {
                            Log.d(TAG, "User answer: " +answer);
                            if (answer.equals(question.getResult())) {
                                score += CORRECT;
                            } else {
                                score += WRONG;
                            }
                            timeToSolve.cancel();

                            answer = "";
                            txtScore.setText(Double.toString(score));
                            txtAnswer.setText("");
                            indicatorLayout.removeAllViews();

                            timeWhenStopped = txtDuration.getBase() - SystemClock.elapsedRealtime();
                            txtDuration.stop();
                            hideQuestion();
                            new DownloaderTask().execute();
                        }

                        else {
                            Toast.makeText(TestActivity.this, "No answer was given", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            else {
                cancelTest();
                quesStatus.setVisibility(View.INVISIBLE);
            }
        }

    }

    //ANSWER OPTION VIEWPAGER
    private class OptionsAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> fragments;
        public OptionsAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    //set indicator color
    private void selectedDots(int position) {
        for (int i = 0; i < indicator.length; i++) {
            if (i == position) {
                indicator[i].setTextColor(getResources().getColor(R.color.dark_grey));
            } else {
                indicator[i].setTextColor(getResources().getColor(R.color.grey));
            }
        }
    }

    //create indicator dots
    private void setIndicators() {
        for (int i = 0; i < indicator.length; i++) {
            indicator[i] = new TextView(this);
            indicator[i].setText(Html.fromHtml("&#9679;"));
            indicator[i].setTextSize(18);
            indicatorLayout.addView(indicator[i]);
        }

    }

    //get answer from fragments
    @Override
    public void onDataReceived(String data) {
        answer=data;
        optionsAdapter.notifyDataSetChanged();
    }

    private void cancelTest(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(TestActivity.this);
        dialog.setTitle("Failed to connect to server");
        dialog.setMessage("We will cancel the test. Please retry later on.");
        dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                endTest();
            }
        });
        dialog.show();
    }

    //END TEST
    private void endTestConfirmation (){
        AlertDialog.Builder dialog = new AlertDialog.Builder(TestActivity.this);
        dialog.setTitle("End Test?");
        dialog.setMessage("Do you want to submit your test now? After submitting, your result will be final and not editable. ");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                storeResult();
                endTest();
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

    private void storeResult(){
        score = score / ((quesCount-1)*10) *100;
        txtDuration.stop();
        duration = formatElapsedTime((SystemClock.elapsedRealtime() - txtDuration.getBase())/1000);
        result = new Result(selectedStudent.getId(), startTime, duration, score );
        resultDBModel.add(result);
    }

    private void endTest(){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    //END TEST MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.end_test_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ic_end_test:
                endTestConfirmation();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //DISABLE BACKPRESSED
    @Override
    public void onBackPressed() {

    }





}