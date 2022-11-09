package hoang.example.logbook;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;

public class DownloadImageTask extends AsyncTask {

    ProgressDialog mProgressDialog;
    Context context;
    ImageView image;

    public DownloadImageTask(ProgressDialog mProgressDialog, Context context, ImageView image) {
        this.mProgressDialog = mProgressDialog;
        this.context = context;
        this.image = image;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        String imageURL = (String) objects[0];
        Bitmap bitmap = null;
        try {
            // Download Image from URL
            InputStream input = new java.net.URL(imageURL).openStream();
            // Decode Bitmap
            bitmap = BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle("Loading...");
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    @Override
    protected void onPostExecute(Object o) {
        image.setImageBitmap((Bitmap) o);
        // Close progressdialog
        mProgressDialog.hide();
    }
}