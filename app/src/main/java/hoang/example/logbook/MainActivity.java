package hoang.example.logbook;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;

import Entities.Image;
import com.squareup.picasso.Picasso;
public class MainActivity extends AppCompatActivity  {

    ProgressDialog mProgressDialog;
    private static int curr_id = -1;
    private static boolean is_imageUrl = false;

    public static boolean IsValidUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            return URLUtil.isValidUrl(urlString) && Patterns.WEB_URL.matcher(urlString).matches();
        } catch (MalformedURLException ignored) {
        }
        return false;
    }
//    String url = "https://image-us.24h.com.vn/upload/3-2022/images/2022-08-30/2022-08-30_19-51-38-1661863909-392-width740height555.jpg";
//    ImageView imgView = (ImageView) findViewById(R.id.imgView);
//    Picasso.with(context).load(url).into(imgView);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get buttons by id
        Button btnNext = findViewById(R.id.btnNext);
        Button btnPrev = findViewById(R.id.btnPrev);
        Button btnAddLink = findViewById(R.id.btnAddLink);
        Button btnCamera = findViewById(R.id.btnCamera);

        btnCamera.setOnClickListener(view ->{
            Intent intent = new Intent(MainActivity.this, Camera.class);
            startActivity(intent);
        });

        // Add new link image by URL
        btnAddLink.setOnClickListener(view -> {
            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
            EditText txtURL = findViewById(R.id.txtUrl);
            String str_txtURL = txtURL.getText().toString();
            is_imageUrl = str_txtURL.startsWith("data:image/");
            if(IsValidUrl(str_txtURL)){
                dbHelper.insertImage(str_txtURL);
                Toast.makeText(this, "Added new link", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(this, "Not valid URL! Please enter again", Toast.LENGTH_LONG).show();
            }
        });

        // Show image
        Button btnShow = findViewById(R.id.btnShow);
        btnShow.setOnClickListener(view -> {
            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
            ArrayList<Image> arrayImages = dbHelper.getAllImages();
            int len = arrayImages.size();
            if(len == 0){
                Toast.makeText(this, "There is no image in database", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Size: " + String.valueOf(len), Toast.LENGTH_SHORT).show();
                int random_id = new Random().nextInt(len);
                curr_id = random_id;
                Toast.makeText(this, "Current ID: " + String.valueOf(curr_id), Toast.LENGTH_SHORT).show();
                Image img = arrayImages.get(curr_id);
                String url = img.getUrl();
                ImageView imgView = findViewById(R.id.imgView);
                DownloadImageTask task = new DownloadImageTask(mProgressDialog,this,imgView);
                task.execute(url);
            }
        });

        // Next image
        btnNext.setOnClickListener(view ->{
            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
            ArrayList<Image> arrayImages = dbHelper.getAllImages();
            int len = arrayImages.size();
            if(len == 0){
                Toast.makeText(this, "There is no image in database", Toast.LENGTH_SHORT).show();
            }else{
                if(curr_id == len-1){
                    Toast.makeText(this, "This is the last image", Toast.LENGTH_SHORT).show();
                }else{
                    curr_id += 1;
                    Toast.makeText(this, String.valueOf(curr_id), Toast.LENGTH_SHORT).show();
                    Image img = arrayImages.get(curr_id);
                    String url = img.getUrl();
                    ImageView imgView = findViewById(R.id.imgView);
                    DownloadImageTask task = new DownloadImageTask(mProgressDialog,this,imgView);
                    task.execute(url);
                }
            }
        });
        btnPrev.setOnClickListener(view -> {
            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
            ArrayList<Image> arrayImages = dbHelper.getAllImages();
            int len = arrayImages.size();
            if(len == 0){
                Toast.makeText(this, "There is no image in database", Toast.LENGTH_SHORT).show();
            }else{
                if(curr_id == 0){
                    Toast.makeText(this, "This is the first image", Toast.LENGTH_SHORT).show();
                }else{
                    curr_id -= 1;
                    Toast.makeText(this, String.valueOf(curr_id), Toast.LENGTH_SHORT).show();
                    Image img = arrayImages.get(curr_id);
                    String url = img.getUrl();
                    ImageView imgView = findViewById(R.id.imgView);
                    DownloadImageTask task = new DownloadImageTask(mProgressDialog,this,imgView);
                    task.execute(url);
                }
            }
        });
    }
}