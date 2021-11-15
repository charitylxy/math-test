package curtin.edu.mathtest.fragments.test.options;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;

import curtin.edu.mathtest.R;
import curtin.edu.mathtest.fragments.test.PassDataInterface;
import curtin.edu.mathtest.fragments.test.TestActivity;

public class Option4Fragment extends Fragment {
    PassDataInterface passDataInterface;
    private RadioButton option1, option2, option3, option4;
    private RadioGroup rg;
    String ans1, ans2, ans3, ans4, currentAns;
    ArrayList<Integer> options;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate((R.layout.fragment_option4), container, false);

        Bundle bundle = getArguments();
        options = bundle.getIntegerArrayList("options");
        currentAns = bundle.getString("currentAnswer");

        rg = (RadioGroup) view.findViewById(R.id.radiogroup4) ;
        option1 = (RadioButton) view.findViewById(R.id.option1_4);
        option2 = (RadioButton) view.findViewById(R.id.option2_4);
        option3 = (RadioButton) view.findViewById(R.id.option3_4);
        option4 = (RadioButton) view.findViewById(R.id.option4_4);

        ans1 = Integer.toString(options.get(0));
        ans2 = Integer.toString(options.get(1));
        ans3 = Integer.toString(options.get(2));
        ans4 = Integer.toString(options.get(3));
        option1.setText(ans1);
        option2.setText(ans2);
        option3.setText(ans3);
        option4.setText(ans4);

        setRBCheck();

        option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                option1.setChecked(true);
                currentAns = ans1;
                passDataInterface.onDataReceived(currentAns);
                Log.d("Fragment answer", currentAns);
                option2.setChecked(false);
                option3.setChecked(false);
                option4.setChecked(false);
            }
        });

        option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                option2.setChecked(true);
                currentAns = ans2;
                passDataInterface.onDataReceived(currentAns);
                Log.d("Fragment answer", currentAns);
                option1.setChecked(false);
                option3.setChecked(false);
                option4.setChecked(false);
            }
        });

        option3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                option3.setChecked(true);
                currentAns = ans3;
                passDataInterface.onDataReceived(currentAns);
                Log.d("Fragment answer", currentAns);
                option1.setChecked(false);
                option2.setChecked(false);
                option4.setChecked(false);
            }
        });

        option4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                option4.setChecked(true);
                currentAns = ans4;
                passDataInterface.onDataReceived(currentAns);
                Log.d("Fragment answer", currentAns);
                option2.setChecked(false);
                option3.setChecked(false);
                option1.setChecked(false);
            }
        });


        return view;
    }

    private void setRBCheck() {

        if (currentAns.equals(ans1)){
            option1.setChecked(true);
        }

        else if (currentAns.equals(ans2)){
            option2.setChecked(true);
        }

        else if (currentAns.equals(ans3)){
            option3.setChecked(true);
        }

        else if (currentAns.equals(ans4)){
            option4.setChecked(true);
        }

        else {
            option1.setChecked(false);
            option2.setChecked(false);
            option3.setChecked(false);
            option4.setChecked(false);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            passDataInterface = (PassDataInterface) context;
        } catch (ClassCastException e) {

        }
    }
}
