package hoang.example.logbook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import Entities.Image;

public class MainActivity extends AppCompatActivity {

    private static int curr_id = -1;

    public static boolean IsValidUrl(String urlString) {
        try {
            new URL(urlString);
            return URLUtil.isValidUrl(urlString) && Patterns.WEB_URL.matcher(urlString).matches();
        } catch (MalformedURLException ignored) {
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get buttons by id
        Button btnNext = findViewById(R.id.btnNext);
        Button btnPrev = findViewById(R.id.btnPrev);
        Button btnAddLink = findViewById(R.id.btnAddLink);
        Button btnCamera = findViewById(R.id.btnCamera);

        btnCamera.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Camera.class);
            startActivity(intent);
        });

        // Add new link image by URL
        btnAddLink.setOnClickListener(view -> {
            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
            EditText txtURL = findViewById(R.id.txtUrl);
            String str_txtURL = txtURL.getText().toString();
            if (IsValidUrl(str_txtURL)) {
                dbHelper.insertImage(str_txtURL);
                Toast.makeText(this, "Added new link", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Not valid URL! Please enter again", Toast.LENGTH_LONG).show();
            }
        });

        // Show image
        Button btnShow = findViewById(R.id.btnShow);
        btnShow.setOnClickListener(view -> {
            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
            ArrayList<Image> arrayImages = dbHelper.getAllImages();
            int len = arrayImages.size();
            if (len == 0) {
                Toast.makeText(this, "There is no image in database", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Size: " + len, Toast.LENGTH_SHORT).show();
                curr_id = new Random().nextInt(len);
                Toast.makeText(this, "Current ID: " + curr_id, Toast.LENGTH_SHORT).show();
                Image img = arrayImages.get(curr_id);
                String url = img.getUrl();
                showImage(url);
            }
        });

        // Next image
        btnNext.setOnClickListener(view -> {
            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
            ArrayList<Image> arrayImages = dbHelper.getAllImages();
            int len = arrayImages.size();
            if (len == 0) {
                Toast.makeText(this, "There is no image in database", Toast.LENGTH_SHORT).show();
            } else {
                if (curr_id == len - 1) {
                    Toast.makeText(this, "This is the last image", Toast.LENGTH_SHORT).show();
                } else {
                    curr_id += 1;
                    Toast.makeText(this, String.valueOf(curr_id), Toast.LENGTH_SHORT).show();
                    Image img = arrayImages.get(curr_id);
                    String url = img.getUrl();
                    showImage(url);
                }
            }
        });
        btnPrev.setOnClickListener(view -> {
            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
            ArrayList<Image> arrayImages = dbHelper.getAllImages();
            int len = arrayImages.size();
            if (len == 0) {
                Toast.makeText(this, "There is no image in database", Toast.LENGTH_SHORT).show();
            } else {
                if (curr_id == 0) {
                    Toast.makeText(this, "This is the first image", Toast.LENGTH_SHORT).show();
                } else {
                    curr_id -= 1;
                    Toast.makeText(this, String.valueOf(curr_id), Toast.LENGTH_SHORT).show();
                    Image img = arrayImages.get(curr_id);
                    String url = img.getUrl();
                    showImage(url);
                }
            }
        });
    }

    private void showImage(String uri) {
        ImageView imgView = findViewById(R.id.imgView);
        Glide.with(this).load(uri).into(imgView);
    }
}