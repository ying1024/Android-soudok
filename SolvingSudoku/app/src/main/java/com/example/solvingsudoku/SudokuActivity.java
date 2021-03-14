package com.example.solvingsudoku;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.solvingsudoku.databinding.ActivitySudokuBinding;

public class SudokuActivity extends AppCompatActivity {

    private ActivitySudokuBinding bingding;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bingding = DataBindingUtil.setContentView(this, R.layout.activity_sudoku);
        final Sudoku sudoku = new Sudoku();
        bingding.setSudoku(sudoku);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("正确提示")
                .setMessage("你成功了，是否要重新生成")
                .setCancelable(true)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sudoku.createRandomSudoku();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("取消点击：", "0");
                    }
                });

        for (int i=0;i<10;i++){
            String sId = "button" + String.valueOf(i);
            findViewById(getResources().getIdentifier(sId,"id","com.example.solvingsudoku"))
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (sudoku.isFocus() && (! sudoku.isFixed(sudoku.getFocusLocation()))){
                                Button b = (Button) v;
                                CharSequence text = b.getText();
                                if (text.toString().equals("x")){
                                    sudoku.setSudokuData(sudoku.getFocusLocation(),0);
                                } else {
                                    sudoku.setSudokuData(sudoku.getFocusLocation(),Integer.valueOf(text.toString()));
                                }
                                if (sudoku.validate()){
                                    Log.i("info","ok");
                                    dialog.show();
                                }else {
                                    Log.i("info","no");
                                }
                            }
                        }
                    });

            findViewById(R.id.sudokuCal).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if(sudoku.calSudoku()){
                            Toast.makeText(SudokuActivity.this,"计算完毕",Toast.LENGTH_LONG);
                        }else {
                            Toast.makeText( SudokuActivity.this,"无解,请更改屏幕上的数字",Toast.LENGTH_LONG).show();
                        }

                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
            });

            findViewById(R.id.sudokuClear).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sudoku.clear();
                }
            });
        }
//        SurfaceView sv = new SurfaceView(this);
//        Canvas canvas = sv.getHolder().lockCanvas();
//        Path path = new Path();
//        path.moveTo(0, 0);
//        path.lineTo(10f, 20f);
//        canvas.drawPath(path, new Paint());
//        sv.getHolder().unlockCanvasAndPost(canvas);
//        LinearLayout sd = findViewById(R.id.sudokuData);
//        sd.addView(sv);

//        getResources().getIdentifier("sudokuCeil_9","id","com.example.hellocv")
//        LinearLayout sr1 = (LinearLayout) findViewById(R.id.sudokuRow_1);
//        TextView sc9 = new TextView(this);
//        sc9.setId(getResources().getIdentifier("sudokuCeil_9","id","com.example.hellocv"));
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        lp.gravity = Gravity.CENTER;
//        lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
//        lp.height = LinearLayout.LayoutParams.MATCH_PARENT;
//        lp.weight = 1.0f;
//        sc9.setGravity(Gravity.CENTER);
//        sc9.setTextSize(20.0f);
//        sc9.setLayoutParams(lp);

//        TextView tv72 = bingding.getRoot().findViewById(R.id.sudokuCeli72);
//        tv72.setText("@{ sudoku.getSudokuData(0) !=0 ? String.valueOf(sudoku.getSudokuData(0)) : `  `}");



//        findViewById(getResources().getIdentifier("sudokuCeli0", "id", "com.example.hellocv")).setBackgroundColor(Color.parseColor("#ffcc33"));
//        bingding.setSd(sudoku.getSudokuData());
    }
}

