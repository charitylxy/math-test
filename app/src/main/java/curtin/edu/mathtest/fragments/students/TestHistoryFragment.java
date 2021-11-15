package curtin.edu.mathtest.fragments.students;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import curtin.edu.mathtest.R;
import curtin.edu.mathtest.classes.Result;
import curtin.edu.mathtest.classes.Student;
import curtin.edu.mathtest.fragments.results.ResultsFragment;
import curtin.edu.mathtest.sql_database.ResultDBModel;

public class TestHistoryFragment extends Fragment {
    private Student selectedStudent;
    private ResultDBModel resultDBModel;
    private List<Result> studentResults;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private TextView ttlHistory;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate((R.layout.fragment_test_history), container, false);

        //get data
        Bundle bundle = this.getArguments();
        selectedStudent = (Student) bundle.getSerializable("selectedStudent");

        //load database
        resultDBModel = new ResultDBModel();
        resultDBModel.load(getActivity().getApplicationContext());

        //get student result
        studentResults = resultDBModel.getStudentResult(selectedStudent.getId());

        //hide title if empty
        ttlHistory = (TextView) view.findViewById(R.id.ttlTestHistory) ;
        if (studentResults.isEmpty()){
            ttlHistory.setVisibility(View.INVISIBLE);
        }
        else {
            ttlHistory.setVisibility(View.VISIBLE);
        }

        //students recycler view
        recyclerView = (RecyclerView) view.findViewById(R.id.testHistoryListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false));
        adapter = new ResultAdapter();
        recyclerView.setAdapter(adapter);

        return view;
    }


    //RESULT ADAPTER
    private class ResultViewHolder extends RecyclerView.ViewHolder {

        private TextView  txtStarttime, txtDuration, txtScore;
        private Result result = null;

        public ResultViewHolder(LayoutInflater inflater, ViewGroup view) {
            super(inflater.inflate(R.layout.list_test_history, view, false));

            txtStarttime = (TextView) itemView.findViewById(R.id.testStartTime);
            txtDuration = (TextView) itemView.findViewById(R.id.testDuration);
            txtScore = (TextView) itemView.findViewById(R.id.testScore);

            //delete result (for testing purpose)
//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    studentResults.remove(result);
//                    resultDBModel.remove(result);
//                    adapter.notifyItemRemoved(getAdapterPosition());
//                    return false;
//                }
//            });


        }
        public void bind(Result result) {
            this.result = result;

            //set result deets
            txtStarttime.setText (result.getStart_time());
            txtDuration.setText("Total Time Taken : " + result.getDuration());
            txtScore.setText(new DecimalFormat("#").format(result.getScore())  + "%");
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
            holder.bind(studentResults.get(position));
        }

        @Override
        public int getItemCount() {
            return studentResults.size();
        }
    }

}
