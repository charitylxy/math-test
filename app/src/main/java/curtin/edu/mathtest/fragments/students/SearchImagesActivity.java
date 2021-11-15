package curtin.edu.mathtest.fragments.students;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import curtin.edu.mathtest.MainActivity;
import curtin.edu.mathtest.R;

public class SearchImagesActivity extends AppCompatActivity {
    public static final String TAG = "Import Pixabay Images";
    public static final String BASE_URL = "https://pixabay.com/api/";
    public static final String API_KEY = "23319229-94b52a4727158e1dc3fd5f2db";

    private ProgressBar progressBar;
    private TextView progressTxt;
    private SearchView searchView;
    private RecyclerView imageRV;
    private RecyclerView.Adapter imageAdapter;
    private List<Bitmap> imageList;

    private String searchValues;
    private int progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_image);
        getSupportActionBar().setTitle(R.string.title_Search_online);

        progressBar = findViewById(R.id.progressBarImg);
        progressTxt = findViewById(R.id.txtLoading);
        searchView = findViewById(R.id.searchBar);

        progressBar.setVisibility(View.INVISIBLE);
        progressTxt.setVisibility(View.INVISIBLE);

        //images recycler view
        imageList = new ArrayList<>();
        imageRV = (RecyclerView) findViewById(R.id.imageRV);
        imageRV.setLayoutManager(new GridLayoutManager(
                getApplicationContext(), 3));
        imageAdapter = new ImageAdapter();
        imageRV.setAdapter(imageAdapter);

        //search image
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Searching..");
                imageList.clear();
                imageAdapter.notifyDataSetChanged();
                searchValues = searchView.getQuery().toString();
                progressTxt.setText("Loading Images... ");
                progressTxt.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                new DownloaderTask().execute();
            }
        });
    }

    //IMAGE ADAPTER
    public class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        Bitmap selectedImage;

        public ImageViewHolder(LayoutInflater inflater, ViewGroup view) {
            super(inflater.inflate(R.layout.list_image, view, false));

            imageView = (ImageView) itemView.findViewById(R.id.picureId);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {
                        Log.d(TAG, "Selected Image");
                        //Write file
                        String filename = "import_pixabay.png";
                        FileOutputStream stream = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
                        selectedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);

                        //Cleanup
                        stream.close();
                        selectedImage.recycle();

                        //Pop intent
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("selectedPhoto", filename);
                        setResult(Activity.RESULT_OK,returnIntent);
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public void bind(Bitmap image) {
            this.selectedImage = image;
            imageView.setImageBitmap(image);
        }
    }

    public class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder> {
        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(SearchImagesActivity.this);
            return new ImageViewHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            holder.bind(imageList.get(position));
        }

        @Override
        public int getItemCount() {
            return imageList.size();
        }
    }


    //DOWNLOAD FROM PIXABAY (ASYNCTASK)
    private class DownloaderTask extends AsyncTask<Void,Integer,List<Bitmap>> {
        @Override
        protected List<Bitmap> doInBackground(Void... voids) {
            List<Bitmap> searchedImgs= new ArrayList<>();
            String imgData = null;

            try {
                //searchRemoteAPI
                String urlString = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter("key",API_KEY)
                        .appendQueryParameter("q",searchValues)
                        .appendQueryParameter("per_page", String.valueOf(50))
                        .build().toString();

                Log.d(TAG, "pictureRetrievalTask: "+urlString);
                HttpURLConnection connection = openConnection(urlString);

                // check connection
                if (connection == null){
                    Log.e("No connection", "Check your connection");
                }

                else if (isConnectionOkay(connection)== false) {
                    Log.e("No connection", "Problem with downloading");
                }

                else {
                    //download to String
                    InputStream inputStream = connection.getInputStream();
                    byte[] byteData = IOUtils.toByteArray(inputStream);
                    imgData = new String(byteData, StandardCharsets.UTF_8);
                    Log.d(TAG, "Image Data :" + imgData);

                    if (imgData!= null){
                        //get Image Large URL
                        List <String> imgUrls = getImageUrl(imgData);

                        for (String imgUrl : imgUrls) {
                            Log.d(TAG, "Image Large URL: " + imgUrl);

                            if (imgUrl != null) {
                                //get image from url
                                String imgUrlString = Uri.parse(imgUrl).buildUpon()
                                        .build().toString();
                                Log.d(TAG, "ImageUrlString: " + imgUrlString);

                                HttpURLConnection imgConnection = openConnection(imgUrlString);
                                //check connection
                                if (imgConnection == null) {
                                    Log.e("No connection", "Check your connection");
                                }

                                else if (isConnectionOkay(imgConnection) == false) {
                                    Log.e("No connection", "Problem with downloading");
                                }

                                else {
                                    //download to Bitmap
                                    InputStream imgInputStream = imgConnection.getInputStream();
                                    byte[] imgByteData = getByteArrayFromInputStream(imgInputStream);
                                    Log.d(TAG, String.valueOf(imgByteData.length));
                                    Bitmap searchedImg = BitmapFactory.decodeByteArray(imgByteData, 0, imgByteData.length);
                                    searchedImgs.add(searchedImg);
                                    imgConnection.disconnect();
                                }
                            }
                        }
                    }

                    else {
                        Log.d("Hello", "Nothing returned");
                    }
                    connection.disconnect();
                    Log.d(TAG, "Disconnected");
                }

            }
            catch ( IOException e){
                Log.e(TAG, e.getMessage());
            }
            return searchedImgs;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressTxt.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(List<Bitmap> imgs) {
            if (imgs.isEmpty()){
                progressTxt.setText("No images found");
                progressBar.setVisibility(View.INVISIBLE);
            }
            else {
                imageList.addAll(imgs);
                imageAdapter.notifyDataSetChanged();
                progressTxt.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }
        }

    }


    // get images url - with JSON
    private List <String> getImageUrl (String data){
        List <String> imgUrls = new ArrayList<>();
        String imgUrl = null;
        int length;
        try {
            JSONObject jBase = new JSONObject(data);
            JSONArray jHits = jBase.getJSONArray("hits");
            Log.d(TAG, "hits: " + Integer.toString(jHits.length()));

            if(jHits.length()>0){
                for (int i = 0; i < jHits.length(); i++){
                    JSONObject jHitsItem = jHits.getJSONObject(i);
                    imgUrl = jHitsItem.getString("previewURL");
                    imgUrls.add(imgUrl);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return imgUrls;
    }

    private byte[] getByteArrayFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
            progress += nRead;
        }
        buffer.close();
        return buffer.toByteArray();
    }

    //check connections
    private HttpURLConnection openConnection(String urlString)  {

        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
        } catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return conn;
    }

    private boolean isConnectionOkay(HttpURLConnection conn){
        try {
            if(conn.getResponseCode()==HttpURLConnection.HTTP_OK){
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }




}