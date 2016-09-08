package me.firmannizammudin.trysimplefacedetection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Button btnProcess;
    private ImageView imgProcess;

    private Bitmap imgBitmap = null;

    private final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        imgProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessImage processImage = new ProcessImage();
                processImage.execute();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    imgBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    imgProcess.setImageBitmap(imgBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    ProgressDialog progressDialog;

    private class ProcessImage extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this, "Please Wait", "Finding Face(s)", true);
            progressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            if (imgBitmap == null)
                imgBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.img1, options);

            Paint rect = new Paint();
            rect.setStrokeWidth(5);
            rect.setColor(Color.GREEN);
            rect.setStyle(Paint.Style.STROKE);

            Bitmap tempBitmap = Bitmap.createBitmap(imgBitmap.getWidth(), imgBitmap.getHeight(), Bitmap.Config.RGB_565);
            Canvas tempCanvas = new Canvas(tempBitmap);
            tempCanvas.drawBitmap(imgBitmap, 0, 0, null);

            FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext())
                    .setTrackingEnabled(false)
                    .build();
            if (!faceDetector.isOperational()) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Could not set up the face detector!")
                        .show();
            }

            Frame frame = new Frame.Builder()
                    .setBitmap(imgBitmap)
                    .build();
            SparseArray<Face> faceSparseArray = faceDetector.detect(frame);

            for (int i = 0; i < faceSparseArray.size(); i++) {
                Face thisface = faceSparseArray.valueAt(i);
                float x1 = thisface.getPosition().x;
                float y1 = thisface.getPosition().y;
                float x2 = x1 + thisface.getWidth();
                float y2 = y1 + thisface.getHeight();
                tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, rect);
            }
            return tempBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            imgProcess.setImageDrawable(new BitmapDrawable(getResources(), result));
            progressDialog.dismiss();
        }
    }

    private void initView() {
        btnProcess = (Button) findViewById(R.id.main_btn_process);
        imgProcess = (ImageView) findViewById(R.id.main_iv_imgproc);
    }
}
