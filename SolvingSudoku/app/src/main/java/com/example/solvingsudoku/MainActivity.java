package com.example.solvingsudoku;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.UnicodeSetSpanner;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.lang.reflect.Array;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView imageView;
    private Bitmap bitmap,train,bitmapinit;
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
//        TextView tv = findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());

        getWindow().setBackgroundDrawableResource(R.drawable.bgc);
        findViewById(R.id.button).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        imageView = (ImageView)findViewById(R.id.imageView);
        bitmap = null;
        train = BitmapFactory.decodeResource(getResources(),R.drawable.train);
        bitmapinit = BitmapFactory.decodeResource(getResources(),R.drawable.bitmapinit);
        imageView.setImageBitmap(bitmapinit);

        findViewById(R.id.playSudoku).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button:
                Intent intent_gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(intent_gallery, 1);
                break;
            case R.id.button2:
                if(bitmap == null){
                    Toast.makeText(this,"请先选择有方框的数独图片哦！",Toast.LENGTH_SHORT).show();
                }else{
                    Solv();
                }
                break;
            case R.id.playSudoku:
                Intent intent = new Intent(this,SudokuActivity.class);
                startActivity(intent);
                break;

        }
    }

    public void Solv(){

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int tw = train.getWidth();
        int th = train.getHeight();

        int[] piexls = new  int[w*h];

        int[] tpiexls = new int[tw*th];

        bitmap.getPixels(piexls, 0, w,0,0, w, h);
        train.getPixels(tpiexls,0,tw,0,0,tw,th);

        if(Vali(piexls,w,h)){
            int[] resultData = Solvmain(piexls,w,h,tpiexls,tw,th);

            Log.i("abc","ABC");
            Bitmap resultImage = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
            resultImage.setPixels(resultData,0,w,0,0,w,h);

            imageView.setImageBitmap(resultImage);
            Toast.makeText(this,"求解成功！",Toast.LENGTH_SHORT).show();
            bitmap = null;
        }else {
            Toast.makeText(this,"您选择的图片不符合要求！！",Toast.LENGTH_SHORT).show();
            imageView.setImageBitmap(bitmapinit);
        }


    }

    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == 1&&resultCode == RESULT_OK&&data!=null){
            Uri uri = data.getData();
            ContentResolver cr = this.getContentResolver();
            try {
                bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(MainActivity.this,"请选择一张数独图片!",Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
//    public native String stringFromJNI();

    public native int[] Solvmain(int[] pixels,int w,int h,int[] tpixels,int tw,int th);
    public native boolean Vali(int[] pixels,int w,int h);
}
