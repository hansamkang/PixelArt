package com.example.pixelart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class LoadActivity extends AppCompatActivity {

    static private String SHARE_NAME = "SHARE-PREF";
    static SharedPreferences sharedPref = null;
    static SharedPreferences.Editor editor = null;
    Gson gson = new Gson();
    int dataMount;
    ArrayList<String> flist = new ArrayList<String>();
    ArrayList<String> clist = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        sharedPref = getSharedPreferences(SHARE_NAME, MODE_PRIVATE);
        editor = sharedPref.edit();

        dataMount = sharedPref.getInt("dataMount",0);
        int dataindex=1;


        for(int i=0 ; i<dataMount; i++){
            while(true){
                String fname = "name" + Integer.toString(dataindex);
                if(sharedPref.contains(fname)){
                    String canvasName = sharedPref.getString(fname, "");

                    flist.add(fname);
                    clist.add(canvasName);
                    Log.d("hansam", "로드 들어옴" + fname + " : " + canvasName);
                    dataindex++;
                    break;
                }
                dataindex++;
            }
        }
        ListView listView = (ListView)findViewById(R.id.loadListView);
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.loadLinearView);

        if(!clist.isEmpty()){
            linearLayout.setVisibility(View.INVISIBLE);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, clist);
        listView.setAdapter(adapter); // 자바안

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                String selected = flist.get(i);
                Intent intent = new Intent();
                intent.putExtra("refname", selected);
                setResult(RESULT_OK, intent);

                Log.d("hansam", "넘겨는 줌"+ selected +"l");
                finish();

            }
        });



    }

}
