package hoang.example.logbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Camera extends AppCompatActivity implements ImageAnalysis.Analyzer {
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;

    private Executor executor = Executors.newSingleThreadExecutor();
    private final int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[] {"android.permission.CAMERA"
            ,"android.permission.RECORD_AUDIO"};
    ImageView imageView;
    EditText inputPictureUri;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);
        dbHelper =  new DatabaseHelper(getApplicationContext());

        imageView = findViewById(R.id.imageView);
        inputPictureUri = findViewById(R.id.inputUri);
        previewView = findViewById(R.id.previewView);
        Button btn = findViewById(R.id.button);
        if(!allPermissionsGranted()){
            ActivityCompat.requestPermissions(this,REQUIRED_PERMISSIONS,REQUEST_CODE_PERMISSIONS);
        }
        startCamera();
        initSaveImage();

        btn.setOnClickListener(view -> {
            final String[]  options = {"Take photo","Choose from library","View a picture from Uri"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setItems(options,(dialog,item)->{
                if (options[item]=="Take photo"){
                    takePicture();
                }else if(options[item]=="Choose from library"){
                    Toast.makeText(this,"Choose from library",Toast.LENGTH_SHORT).show();
                }else if(options[item]=="View a picture from Uri"){
                    String uri = inputPictureUri.getText().toString();
                    File imgFile = new  File(uri);
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imageView.setImageBitmap(myBitmap);
                    dialog.dismiss();
                }
            });
            builder.show();
        });

    }

    private void initSaveImage(){
        findViewById(R.id.btnSave).setOnClickListener(v -> {
            dbHelper.insertImage(inputPictureUri.getText().toString());
            Toast.makeText(Camera.this, "Save Image", Toast.LENGTH_SHORT).show();
        });
    }
    // /data/user/0/com.example.cameraxdemo/files/1664772838768
    private void takePicture() {
        long timestamp = System.currentTimeMillis();
        File output = new File(getApplicationContext().getFilesDir(), "Image_"+ timestamp + ".png");
        ImageCapture.OutputFileOptions option1 = new ImageCapture.OutputFileOptions
                .Builder(output).build();
        imageCapture.takePicture(
                option1,
                executor,
                new ImageCapture.OnImageSavedCallback(){
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        runOnUiThread(()->{
                                inputPictureUri.setText(output.getAbsolutePath());
                                Glide.with(imageView).load(output.getAbsolutePath()).into(imageView);

                        });
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                    }
                }
        );
    }

    private boolean allPermissionsGranted(){
        for(String permission: REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){
                return  false;
            }
        }
        return  true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permission[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permission, grantResults);
        switch (requestCode){
            case REQUEST_CODE_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(Camera.this , "Permissions granted", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(Camera.this , "Permissions denied", Toast.LENGTH_SHORT).show();
                }
        }
    }
    private void startCamera(){
        final ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(()->{
            try {
                ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        //image capture usecase
        imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();
        videoCapture = new VideoCapture.Builder().setVideoFrameRate(30).build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        imageAnalysis.setAnalyzer(executor,this);
        cameraProvider.bindToLifecycle((LifecycleOwner) this,cameraSelector,preview,imageCapture,videoCapture);

    }
    @Override
    public void analyze(@NonNull ImageProxy image){
        image.close();
    }
}
