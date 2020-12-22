package com.example.pixelart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.DiscretePathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    final int resolution = 520;
    final int pixelWidth = 26;
    final int metrixXY = resolution/pixelWidth;
    final int pixelCenter = pixelWidth/2;

    // 저장 관련
    String slot = "";
    static private String SHARE_NAME = "SHARE-PREF";
    static SharedPreferences sharedPref = null;
    static SharedPreferences.Editor editor = null;
    Gson gson = new Gson();

    //설정 가능한것들
    float cursorOffset = 2.6f;
    int queueSize = 20;

    // 그림 그린 로그 큐
    LinkedList<DrawLog> drawLogQueue = new LinkedList<DrawLog>();
    // 돌아가기 로그, 앞으로 되돌릴떄 써먹음
    LinkedList<DrawLog> goBackLogQueue = new LinkedList<DrawLog>();

    int selectedPallete;
    int cursorX, cursorY;
    float mX, mY;
    String selectedColor;
    String palletColors[] = { "#ff1100","#fcba03","#03fc28","#00ccff","#0b03fc" ,"#ff00dd"};
    String canvasMetrix[][];
    boolean tempFlagMetrix[][];
    ImageView mainImageView, gridImageView, cursorImageView, colorExtractionButton;
    Button pressButton;
    ImageButton gridButton, goBackButton, goFrontButton, fillColorButton, eraseButton;
    ImageButton pallete [];
    AlertDialog newDialog, saveDialog;

    Canvas canvasMain, canvasGrid, canvasCursor;
    Bitmap bitmapMain, bitmapGrid, bitmapCursor;

    Bitmap cursorImage;


    String testArray[][] = new String[metrixXY][metrixXY];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // 여기서 부터 내 코드
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 공유저장 초기화
        sharedPref = getSharedPreferences(SHARE_NAME, MODE_PRIVATE);
        editor = sharedPref.edit();

        if(!sharedPref.contains("dataMount")){
            editor.putInt("dataMount",0);
            editor.commit();
        }


        // 위젯 초기화

        mainImageView = (ImageView)findViewById(R.id.canvasMain);
        gridImageView = (ImageView)findViewById(R.id.gridView);
        cursorImageView = (ImageView)findViewById(R.id.cursorView);

        // 버튼 초기화
        pallete  = new ImageButton [6];
        pallete[0] = (ImageButton)findViewById(R.id.palletButton0);
        pallete[1] = (ImageButton)findViewById(R.id.palletButton1);
        pallete[2] = (ImageButton)findViewById(R.id.palletButton2);
        pallete[3] = (ImageButton)findViewById(R.id.palletButton3);
        pallete[4] = (ImageButton)findViewById(R.id.palletButton4);
        pallete[5] = (ImageButton)findViewById(R.id.palletButton5);
        pressButton = (Button)findViewById(R.id.pressButton);

        gridButton = (ImageButton)findViewById(R.id.gridButton) ;
        goBackButton = (ImageButton)findViewById(R.id.goBackButton);
        goFrontButton = (ImageButton)findViewById(R.id.goFrontReturnBotton);
        colorExtractionButton = (ImageButton)findViewById(R.id.colorExtractionButton);
        fillColorButton = (ImageButton)findViewById(R.id.fillButton);
        eraseButton = (ImageButton)findViewById(R.id.eraseButton);

        // 캔버스 초기화
        bitmapMain = Bitmap.createBitmap(resolution,resolution, Bitmap.Config.ARGB_8888);
        bitmapGrid = Bitmap.createBitmap(resolution+1,resolution+1, Bitmap.Config.ARGB_8888);
        bitmapCursor = Bitmap.createBitmap(resolution,resolution, Bitmap.Config.ARGB_8888);

        // ImageView와 Canvas들의 bitmap 설정
        mainImageView.setImageBitmap(bitmapMain);
        gridImageView.setImageBitmap(bitmapGrid);
        cursorImageView.setImageBitmap(bitmapCursor);
        canvasMain = new Canvas(bitmapMain);
        canvasGrid = new Canvas(bitmapGrid);
        canvasCursor = new Canvas(bitmapCursor);


        // 이외에 초기화
        cursorX = cursorY = resolution/2;
        selectedPallete = 0;
        selectedColor = "#ff1100";
        canvasMetrix= new String[metrixXY][metrixXY];

        pallete[0].setColorFilter(Color.parseColor("#ff0000"), PorterDuff.Mode.MULTIPLY);
        pallete[1].setColorFilter(Color.parseColor("#ff9900"), PorterDuff.Mode.MULTIPLY);
        pallete[2].setColorFilter(Color.parseColor("#00ff00"), PorterDuff.Mode.MULTIPLY);
        pallete[3].setColorFilter(Color.parseColor("#0000ff"), PorterDuff.Mode.MULTIPLY);
        pallete[4].setColorFilter(Color.parseColor("#dd00ff"), PorterDuff.Mode.MULTIPLY);
        pallete[5].setColorFilter(Color.parseColor("#363636"), PorterDuff.Mode.MULTIPLY);

        clearCanvas();
        drawGrid();
        createCursor();
        changeCursorColor();

        // 강제 종료시 백업
        if(savedInstanceState != null){
            Log.d("hansam", "이거 왜 실행 안되냐");
            String tempCanvas[][] = (String [][])savedInstanceState.getSerializable("savedCanvasMetrix");
            reDrawCanvas(tempCanvas);
        }
        // 저장된게 있다면
        update();
        Log.d("hansam", "슬롯 :" + slot);
        cursorImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float rawX = motionEvent.getRawX();
                float rawY = motionEvent.getRawY();

                switch(motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        mX = rawX;
                        mY = rawY;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = rawX - mX;
                        float dy = rawY - mY;
                        mX = rawX;
                        mY = rawY;
                        translateCursor(dx, dy);
                        invalidateOptionsMenu();
                        break;
                    case MotionEvent.ACTION_UP:
                }
                return true;
            }
        });

        eraseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor= "#ffffff";
                changeCursorColor();
            }
        });

        // GridButton 동작 구현, HIDE or SHOW
        gridButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gridImageView.getVisibility() == View.VISIBLE)
                {
                    gridImageView.setVisibility(View.INVISIBLE);
                }
                else{
                    gridImageView.setVisibility(View.VISIBLE);
                }
            }
        });

        // 뒤로가기 버튼
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBackReturn();
            }
        });

        // 앞으로 가기
        goFrontButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goFrontReturn();
            }
        });

        // PressButton 동작 구현
        pressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawPixel(selectedColor);
            }
        });

        //색상 추출
        colorExtractionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorExtraction();
            }
        });

        //색 채우기
        fillColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int x, y;

                x = cursorX/pixelWidth;
                y = cursorY/pixelWidth;
                fillColor(x, y, canvasMetrix[x][y],selectedColor,true);
                invalidateOptionsMenu();
            }
        });

        // 파레트
        pallete[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPallete= 0;
                selectedColor = palletColors[0];
                changeCursorColor();
            }
        });
        pallete[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPallete= 1;
                selectedColor = palletColors[1];
                changeCursorColor();
            }
        });
        pallete[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPallete= 2;
                selectedColor = palletColors[2];
                changeCursorColor();
            }
        });
        pallete[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPallete= 3;
                selectedColor = palletColors[3];
                changeCursorColor();
            }
        });
        pallete[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPallete= 4;
                selectedColor = palletColors[4];
                changeCursorColor();
            }
        });
       pallete[5].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPallete= 5;
                selectedColor = palletColors[5];
                changeCursorColor();
            }
        });

       // 파레트 롱클릭 이벤트
       pallete[0].setOnLongClickListener(new View.OnLongClickListener() {
           @Override
           public boolean onLongClick(View view) {
               selectedPallete =0;
               openColorPicker();
               return false;
           }
       });
        pallete[1].setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                selectedPallete =1;
                openColorPicker();
                return false;
            }
        });
        pallete[2].setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                selectedPallete =2;
                openColorPicker();
                return false;
            }
        });
        pallete[3].setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                selectedPallete =3;
                openColorPicker();
                return false;
            }
        });
        pallete[4].setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                selectedPallete =4;
                openColorPicker();
                return false;
            }
        });
        pallete[5].setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                selectedPallete =5;
                openColorPicker();
                return false;
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_new) { // 새로 만들기
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton("확인", listener);
            builder.setNegativeButton("취소", listener);
            builder.setTitle("새로운 캔버스를 만드시겠습니까?");
            newDialog = builder.create();
            newDialog.show();
        } else if (id == R.id.nav_save) { // 저장
            final EditText input = new EditText(MainActivity.this);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if(slot.equals("")){
                builder.setTitle("저장할 캔버스 이름을 입력하세요");
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String temp = input.getText().toString();
                        input.setText("");
                        saveData(temp);
                    }
                });
            }
            else{
                builder.setTitle("저장 되었습니다");
                saveData();
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
            }

            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder.show();
        } else if (id == R.id.nav_load) { // 불러오기
            Intent intent = new Intent(this, LoadActivity.class);
            startActivityForResult(intent, 2);
        } else if (id == R.id.nav_saveAsJPG) { // JPG로 만들기
            saveToJPG();
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, Settings.class);
            intent.putExtra("cursorDPI", cursorOffset);
            intent.putExtra("queueSize", queueSize);
            startActivityForResult(intent, 4);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 4 && resultCode == RESULT_OK) {
            cursorOffset = data.getFloatExtra("reCursorDpi",0f);
            queueSize = data.getIntExtra("reQueueSize",0);

            saveSettings();
            Log.d("hansam", "리퀘스트 4 : " + cursorOffset);
        }

        if(requestCode == 2 && resultCode == RESULT_OK){
            String fname = data.getStringExtra("refname");
            Log.d("hansam", "데이터 받음 = " + fname);
            slot = fname;
            loadData(fname);
        }
        update();
    }

    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            if(dialogInterface == newDialog && i == DialogInterface.BUTTON_POSITIVE){
                clearCanvas();
                slot = "";
            }
        }
    };


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("savedCanvasMetrix", canvasMetrix);
        Log.d("hansam", "on SaveInstance 저장됐습니다.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("hansam", "디스트로이 됏음");
    }

    // Grid 그리는 함수
    public void drawGrid(){
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1f);
        paint.setColor(Color.BLACK);

        // 가로줄 그리기
        for(int i =0 ; i<=resolution; i+=pixelWidth)
        {
            float temp = Float.intBitsToFloat(i);
            canvasGrid.drawLine(0, i, resolution, i, paint);
        }

        // 세로줄 그리기
        for(int i =0; i<=resolution; i+=pixelWidth)
        {
            float temp = Float.intBitsToFloat(i);
            canvasGrid.drawLine(i, 0, i , resolution, paint);
        }
    }

    // 점 그리기
    public void drawPixel(String colorString){
        int tempX, tempY,metrixX, metrixY;

        metrixX = cursorX/pixelWidth;
        metrixY = cursorY/pixelWidth;

        tempX = cursorX%pixelWidth;
        tempY = cursorY%pixelWidth;
        tempX = cursorX-tempX+pixelCenter;
        tempY = cursorY-tempY+pixelCenter;

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(pixelWidth);
        paint.setColor(Color.parseColor(colorString));

        if(canvasMetrix[metrixX][metrixY].equals(colorString)) {
            Log.d("hansam","그냥 리턴됨");
            return;
        }
        if(drawLogQueue.size()>queueSize){ drawLogQueue.remove(); }
        if(!goBackLogQueue.isEmpty()){ goBackLogQueue.clear();}
        drawLogQueue.add(new DrawLog(metrixX, metrixY, canvasMetrix[metrixX][metrixY],null));

        canvasMetrix[metrixX][metrixY] = colorString;


        canvasMain.drawPoint(tempX,tempY,paint);
        invalidateOptionsMenu();
    }

    // 되돌리기용 drawPixel
    public void drawPixel(int x, int y, String colorString){
        int tempX, tempY;
        tempX = pixelCenter+(pixelWidth*x);
        tempY = pixelCenter+(pixelWidth*y);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(pixelWidth);
        paint.setColor(Color.parseColor(colorString));

        canvasMetrix[x][y] = colorString;
        canvasMain.drawPoint(tempX,tempY,paint);
}

    // 커서 생성
    public void createCursor(){
        Resources r = getResources();
        cursorImage = BitmapFactory.decodeResource(r,R.drawable.cursor);;
        canvasCursor.drawBitmap(cursorImage, resolution/2, resolution/2, null);
    }

    public void translateCursor(float dx, float dy){
        int tempX= (int)(dx/cursorOffset);
        int tempY= (int)(dy/cursorOffset);
        cursorX+=tempX;
        cursorY+=tempY;

        if(cursorX >resolution-10) cursorX = resolution-10;
        if(cursorY >resolution-10) cursorY = resolution-10;
        if(cursorX <0) cursorX =0;
        if(cursorY <0) cursorY =0;

        canvasCursor.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvasCursor.drawBitmap(cursorImage, cursorX, cursorY, null);
    }

    // 현재 선택되어있는 색으로 커서 색 변경
    public void changeCursorColor(){
        cursorImageView.setColorFilter(Color.parseColor(selectedColor), PorterDuff.Mode.MULTIPLY);
    }

    // ColorPicker
    public void openColorPicker() {
        int tColor = Color.parseColor(palletColors[selectedPallete]);
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, tColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                selectedColor = String.format("#%06X", 0xFFFFFF & color);
                palletColors[selectedPallete] = selectedColor;
                pallete[selectedPallete].setColorFilter(Color.parseColor(selectedColor), PorterDuff.Mode.MULTIPLY);
                changeCursorColor();
            }
        });
        colorPicker.show();
    }

    // 색 추출
    public void colorExtraction(){
        int tMetrixX, tMetrixY;
        tMetrixX = cursorX/pixelWidth;
        tMetrixY = cursorY/pixelWidth;

        selectedColor = canvasMetrix[tMetrixX][tMetrixY];

        changeCursorColor();
        palletColors[selectedPallete] =selectedColor;
        pallete[selectedPallete].setColorFilter(Color.parseColor(selectedColor), PorterDuff.Mode.MULTIPLY);
    }

    public void goBackReturn(){
        if(drawLogQueue.isEmpty()) return;
        int x, y;
        DrawLog tempLog = drawLogQueue.pollLast();
        x = tempLog.getX(); y = tempLog.getY();

        if(tempLog.isEmty()){// 일반
            goBackLogQueue.add(new DrawLog(x, y, canvasMetrix[x][y], null));
            drawPixel(x, y, tempLog.getColor());
        }else{// 채우기
            goBackLogQueue.add(new DrawLog(x, y, canvasMetrix[x][y], tempLog.getFlag()));
            fillColorFlag(tempLog.getColor(), tempLog.getFlag());
        }

        invalidateOptionsMenu();
        Log.d("hansam","BACK ="+ tempLog.getX() + " : " + tempLog.getY() + " :" + tempLog.getColor() );
    }

    public void goFrontReturn(){
        if(goBackLogQueue.isEmpty()) return;
        int x, y;
        DrawLog tempLog = goBackLogQueue.pollLast();
        x = tempLog.getX(); y = tempLog.getY();

        if(tempLog.isEmty()){// 일반
            drawLogQueue.add(new DrawLog(x,y, canvasMetrix[x][y],null));
            drawPixel(x, y, tempLog.getColor());
        }else{// 채우기
            drawLogQueue.add(new DrawLog(x, y, canvasMetrix[x][y], tempLog.getFlag()));
            fillColorFlag(tempLog.getColor(), tempLog.getFlag());
        }

        invalidateOptionsMenu();

        Log.d("hansam","FRONT + "+ tempLog.getX() + " : " + tempLog.getY() + " :" + tempLog.getColor() );
    }

    // 색 채우기 함수, dfs 알고리즘 , int는 초기시작시 0, 뒤로감기등등으로 할떈 -1
    public void fillColor(int x, int y, String preColor, String postColor, boolean start){
        if(preColor.equals(postColor)) return;
        if(start){
            tempFlagMetrix = new boolean[metrixXY][metrixXY];
            for(boolean[] row: tempFlagMetrix)
                Arrays.fill(row, false);
        }

        if(canvasMetrix[x][y].equals(preColor)){
            drawPixel(x,y,postColor); // darwPixel에서 canvasMetrix 해당위치의 color값 변경해줌
            tempFlagMetrix[x][y] = true;
        }else{
            return;
        }

        if(x-1>=0 && tempFlagMetrix[x-1][y] == false){fillColor(x-1, y,preColor,postColor,false);}
        if(y-1>=0 && tempFlagMetrix[x][y-1] == false){fillColor(x,y-1,preColor,postColor, false);}
        if(x+1<=metrixXY-1 && tempFlagMetrix[x+1][y] == false){fillColor(x+1,y,preColor,postColor,false);}
        if(y+1<=metrixXY-1 && tempFlagMetrix[x][y+1] == false){fillColor(x,y+1,preColor,postColor,false);}

        if(start){
            if(drawLogQueue.size()>queueSize){ drawLogQueue.remove();}
            drawLogQueue.add(new DrawLog(x,y,preColor, tempFlagMetrix));
            tempFlagMetrix = null;
        }
    }

    // 플래그로 그리기
    public void fillColorFlag(String color, boolean arr[][]){
        for(int i= 0 ; i<metrixXY; i++){
            for(int j =0 ; j<metrixXY; j++){
                if(arr[i][j] == true) {
                    drawPixel(i, j, color);
                }
            }
        }
    }

    // 캔버스 배열로 다시 그리기
    public void reDrawCanvas(String canvas[][]){
        for(int i= 0 ; i<metrixXY; i++){
            for(int j =0 ; j<metrixXY; j++){
                if(canvas[i][j] != null) {
                    drawPixel(i, j, canvas[i][j]);
                }
                else{
                    drawPixel(i,j, "#ffffff");
                }
            }
        }
    }

    void clearCanvas(){
        canvasMain.drawColor(Color.WHITE);
        canvasMetrix= new String[metrixXY][metrixXY];
        for(String[] row: canvasMetrix)
            Arrays.fill(row, "#ffffff");
    }

    public void saveData(){
        JsonData data = new JsonData(canvasMetrix);
        String json = gson.toJson(data);

        String dataName = "data"+slot.substring(4);
        Log.d("hansam","save"+ slot);
        editor.putString(dataName, json);
        editor.commit();
        Log.d("hansam", "저장 완료");
    }

    // 새 캔버스
    public void saveData(String canvasName){
        JsonData data = new JsonData(canvasMetrix);
        String json = gson.toJson(data);
        String dataName;
        String cname;

        int dataMount = sharedPref.getInt("dataMount",0);
        dataMount++;

        for(int i=1 ; ;i++){
            dataName = "data" + Integer.toString(i);
            cname = "name" + Integer.toString(i);
            if(!sharedPref.contains(dataName)){
                break;
            }
        }
        editor.putInt("dataMount", dataMount);
        editor.putString(cname, canvasName);
        editor.putString(dataName, json);
        editor.commit();
        slot = dataName;
        Log.d("hansam","dataName ="+dataName+"  cname = " + cname  );
        Log.d("hansam", "새 캔버스 저장 완료");
    }

    public void loadData(String fName){
        String dataName = "data"+fName.substring(4);
        if(sharedPref.contains(dataName) == false) return;

        String json =  sharedPref.getString(dataName, "");
        Log.d("hansam", "실행 완료!!!!!!!!! " );
        JsonData data = gson.fromJson(json, JsonData.class);

        reDrawCanvas(data.getCanvas());
        goBackLogQueue.clear();
        drawLogQueue.clear();

        Log.d("metrixCheck", selectedColor);
    }

    public void update(){
        Log.d("hansam", "업데이트 들어왔음");
        if(sharedPref.contains("cursorOffset")){
            cursorOffset = sharedPref.getFloat("cursorOffset", 0f);
            Log.d("hansam", "커서 업데이트" + cursorOffset);
        }
        if(sharedPref.contains("queueSize"))
        {
            int temp = sharedPref.getInt("queueSize", 0);
            if(queueSize == temp){ return; }
            queueSize = temp;
            while(drawLogQueue.size()>queueSize) drawLogQueue.remove();
            while(goBackLogQueue.size()>queueSize) goBackLogQueue.remove();

            Log.d("hansam", "큐사이즈 업데이트");
        }
    }

    public void saveSettings(){
        editor.putFloat("cursorOffset",cursorOffset);
        editor.putInt("queueSize", queueSize);
        editor.commit();
        Log.d("hansam", "설정 저장 완료");
    }

    public void saveToJPG(){
        File folder = new File(Environment.getExternalStorageDirectory().toString()+"/PixelArt");
        boolean success = false;

        if (!folder.exists())
        {
            Log.d("hansam", "폴더 만듬");
            success = folder.mkdirs();
        }

        System.out.println(success+"folder");
        Log.d("hansam", Environment.getExternalStorageDirectory().toString());

        File file;

        if(slot.equals(""))
        {
            file = new File(Environment.getExternalStorageDirectory().toString() + "/PixelArt/sample.png");
        }
        else{
            file = new File(Environment.getExternalStorageDirectory().toString() + "/PixelArt/"+slot+".png");
        }


        if ( !file.exists() )
        {
            try {
                success = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println(success+"file");

        FileOutputStream ostream = null;
        try
        {
            ostream = new FileOutputStream(file);

            System.out.println(ostream);

            bitmapMain.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            Toast.makeText(MainActivity.this,"사진을 변환 성공!",Toast.LENGTH_SHORT).show();
        }catch (NullPointerException e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Null error", Toast.LENGTH_SHORT).show();
        }

        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "File error", Toast.LENGTH_SHORT).show();
        }

        catch (IOException e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "IO error", Toast.LENGTH_SHORT).show();
        }
    }
    public void loadSettings(){
        cursorOffset = sharedPref.getFloat("cusorOffset", 0f);
        queueSize = sharedPref.getInt("queueSize", queueSize);
    }

}
