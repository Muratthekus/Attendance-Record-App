package com.malikane.kart;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;
import java.util.TreeSet;

public class NewPerson extends AppCompatActivity {
    TextView isim;
    EditText input;
    Button ekle,ara;
    TableRow rowOne,rowTwo;
    static String DATE= Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"/"+(Calendar.getInstance().get(Calendar.MONTH)+1)+"/"+Calendar.getInstance().get(Calendar.YEAR);

    Set<String>kisiler =new TreeSet<>();
    Set<String> tarih = new TreeSet<>();
    Set<String> TodayPerson = new TreeSet<>();

    @Override
    protected void onRestart() {
        super.onRestart();
        editor.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor.commit();
    }

    static SharedPreferences data;
    static SharedPreferences.Editor editor;
    //veri saklama
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_person);
        data= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = data.edit();
        input=findViewById(R.id.IncomeKıisi);
        ekle=findViewById(R.id.button);
        isim=findViewById(R.id.textView34);
        ara=findViewById(R.id.button2);
        rowOne=findViewById(R.id.rowOne);
        rowTwo=findViewById(R.id.rowTwo);
        tarih=data.getStringSet("LastCheckDaY",tarih);
        //Today Person Sıfırlanması Lazım, Yoklama islendikten sonra
        TodayPerson=data.getStringSet("TodayPerson",TodayPerson);
        kisiler=data.getStringSet("kisiler",kisiler);
        fullScreen();
        //Alert Dialog Eklenecek
        ekle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!input.getText().toString().equals("")) {
                    kisiler.add(input.getText().toString());
                    update(kisiler);
                    Toast.makeText(getApplicationContext(), input.getText().toString() + " eklendi", Toast.LENGTH_SHORT).show();
                }
            }
        });
        ara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar();
            }
        });

    }
    private void Calendar(){
        // | dd/mm/yyyy // Date View
        // |      +     //  Check List View
        for(Object tarih:tarih.toArray()){
            TextView text=new TextView(this);
            text.setText(tarih.toString());
            rowOne.addView(text);
        }
    }
    private void update(Set<String>input){
        editor.putStringSet("kisiler",input);
        editor.commit();
    }
    private void CheckProcess(){
        for(Object todaypeople:TodayPerson.toArray()){
            for(Object members:kisiler.toArray()){
                if(todaypeople.toString().equals(members.toString() )){

                }
            }
        }
        //Kümeyi bir sonraki yoklama için sıfırlıyor
        TodayPerson.clear();
        editor.putStringSet("TodayPerson",TodayPerson);
        editor.commit();
    }
    private void fullScreen(){
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
