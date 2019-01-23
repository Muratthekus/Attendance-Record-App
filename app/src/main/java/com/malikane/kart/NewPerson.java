package com.malikane.kart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;
import java.util.TreeSet;

public class NewPerson extends AppCompatActivity {
    TextView isim,LinearLayoutText;
    EditText input;
    Button ekle,ara,update;
    TableRow rowOne,rowTwo;
    static String DATE= Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"/"+(Calendar.getInstance().get(Calendar.MONTH)+1)+"/"+Calendar.getInstance().get(Calendar.YEAR);
    LinearLayout linearLayout;
    String tarih="";
    Set<String> TodayPerson = new TreeSet<>();
    String PATH;

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
    static String validRecordFile="",validSheet="";
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
        linearLayout=findViewById(R.id.LinerLayout);
        update=findViewById(R.id.update);
        LinearLayoutText=findViewById(R.id.textView2);

        TodayPerson=data.getStringSet("TodayPerson",TodayPerson);
        tarih=data.getString("date",tarih);
        fullScreen();
        PATH=getApplicationContext().getExternalFilesDir(null).getAbsolutePath();
        RecordDataShow();
        fullScreen();

        ekle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!input.getText().toString().equals("") && !validSheet.equals("") && !validSheet.equals("")) {
                    addNewPersonAlertMessage(input.getText().toString());
                }
                else{
                    Toast.makeText(getApplicationContext(), "Geçersiz Girdi", Toast.LENGTH_SHORT).show();
                }
            }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TodayPerson.isEmpty()){
                    Toast.makeText(getApplicationContext(),"YOKLAMA KAYDI YOK",Toast.LENGTH_SHORT).show();
                }
                else{
                    if(!validSheet.equals("") && !validSheet.equals("")) {
                        try {
                            UpdateRecord();
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), "IO ERROR", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else Toast.makeText(getApplicationContext(), "KAYIT DOSYASI SEÇİNİZ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        ara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!input.getText().toString().equals("") && !validSheet.equals("") && !validSheet.equals("")) {
                    try {
                        SearchPerson(input.getText().toString());
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "IO ERROR", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
    private void UpdateRecord() throws IOException{
        HSSFWorkbook myWorkBook=ClassicExcelStuff();
        HSSFSheet mySheet=myWorkBook.getSheet(validSheet);
        Cell c ,dateCell;
        //Cell style for header row
        CellStyle cs = myWorkBook.createCellStyle();
        cs.setFillForegroundColor(HSSFColor.LIME.index);
        cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        Row row, dateRow=mySheet.getRow(0);

        //Check Date
        int recordIndex=dateRow.getLastCellNum()-1;
        if(!dateRow.getCell(dateRow.getLastCellNum()-1).toString().equals(tarih)){
            recordIndex=dateRow.getLastCellNum();
            mySheet.setColumnWidth(recordIndex,(15 * 300));
            dateCell=dateRow.createCell(recordIndex);
            dateCell.setCellValue(tarih);
        }

        //check person is exist
        for(int i=0; i<=mySheet.getLastRowNum(); i++){
            row=mySheet.getRow(i);
            c=row.getCell(0);
            for(Object name:TodayPerson.toArray()){
                if(c.toString().equals(name.toString())){
                    c=row.createCell(recordIndex);
                    c.setCellValue("    +     ");
                }
            }
        }
        TodayPerson.clear();
        editor.putStringSet("TodayPerson",TodayPerson);

        File SaveFile=new File(getApplicationContext().getExternalFilesDir(null),validRecordFile);
        FileOutputStream os=null;
        try {
            os = new FileOutputStream(SaveFile);
            myWorkBook.write(os);
            Log.w("FileUtils", "Writing file" + SaveFile);
        } catch (IOException e) {
            Log.w("FileUtils", "Error writing " + SaveFile, e);
        } catch (Exception e) {
            Log.w("FileUtils", "Failed to save file", e);
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
            }
        }

    }
    //Show user to all record data and then user choose the valid record data
    @SuppressLint("SetTextI18n")
    private void RecordDataShow(){
        linearLayout.removeAllViews();
        File file=new File(PATH);
        //No DATA
        if(file.listFiles().length==0){
            Button button=new Button(this);
            button.setText("NO DATA");
            button.setClickable(false);
            button.setVisibility(View.VISIBLE);
            button.setLayoutParams (new LinearLayout.LayoutParams(300, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(button);
        }
        else{
            File [] files=file.listFiles();
            for(File f:files){
                final Button button=new Button(this);
                button.setVisibility(View.VISIBLE);
                button.setLayoutParams (new LinearLayout.LayoutParams(300, LinearLayout.LayoutParams.WRAP_CONTENT));
                button.setText(f.getName());
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        validRecordFile=button.getText().toString();
                        Toast.makeText(getApplicationContext(),"Gecerli Kayıt Dosyası: "+validRecordFile,Toast.LENGTH_SHORT).show();
                        try {
                            RecordDataSheetShow();
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(),"IO ERROR",Toast.LENGTH_LONG).show();
                        }
                    }
                });
                linearLayout.addView(button);
            }
        }
    }

    private void RecordDataSheetShow() throws IOException{
        linearLayout.removeAllViews();
        LinearLayoutText.setText("GEÇERLİ SHEET'İ SEÇİN");
        File file=new File(PATH,validRecordFile);
        FileInputStream myInput = new FileInputStream(file);

        // Create a POIFSFileSystem object
        POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

        // Create a workbook using the File System
        HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

        for(int i=0; i<myWorkBook.getNumberOfSheets(); i++){
            final Button button=new Button(this);
            button.setText(myWorkBook.getSheetAt(i).getSheetName());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    validSheet=button.getText().toString();
                    Toast.makeText(getApplicationContext(),"SHEET SEÇİLDİ",Toast.LENGTH_SHORT).show();
                }
            });
            linearLayout.addView(button);
        }
    }
    //-----------------------------------------------------------------------

    private void SearchPerson(String searchPerson) throws IOException{
        HSSFWorkbook myWorkBook = ClassicExcelStuff();

        Cell c = null;
        //Cell style for header row
        CellStyle cs = myWorkBook.createCellStyle();
        cs.setFillForegroundColor(HSSFColor.LIME.index);
        cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        HSSFSheet mySheet =myWorkBook.getSheet(validSheet);

        boolean isExist=false;
        Row row;
        //check person is exist
        for(int i=0; i<=mySheet.getLastRowNum(); i++){
            row=mySheet.getRow(i);
            c=row.getCell(0);
            if(searchPerson.equals(c.toString())){
                Row calendarRow=mySheet.getRow(0);
                isim.setText(searchPerson);
                if(calendarRow.getLastCellNum()!=0){
                    for(int j=1; j<=calendarRow.getLastCellNum()-1; j++){
                        TextView date=new TextView(this);
                        TextView plus=new TextView(this);
                        date.setText(calendarRow.getCell(j).toString());
                        plus.setText(row.getCell(j).toString());
                        rowOne.addView(date);
                        rowTwo.addView(plus);
                    }
                }
                isExist=true;
            }
        }
        if(!isExist) Toast.makeText(getApplicationContext(),"ARANAN KİSİ YOK",Toast.LENGTH_LONG).show();

    }

    private HSSFWorkbook ClassicExcelStuff() throws IOException{
        File file=new File(PATH,validRecordFile);
        FileInputStream myInput = new FileInputStream(file);
        // Create a POIFSFileSystem object
        POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);
        // Create a workbook using the File System
        return new HSSFWorkbook(myFileSystem);
    }
    //---------------------------------------------------------------------
    private void addNewPersonAlertMessage(final String newPerson){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("DEMO");
        builder.setMessage("KİŞİ EKLENSİN Mİ?");
        builder.setPositiveButton("EVET", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    addPersonToExcel(newPerson);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //If User Choose Negative Button Then Nothing Will Happen
        builder.setNegativeButton("HAYIR", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    private void addPersonToExcel(String newPerson) throws IOException{

        HSSFWorkbook myWorkBook = ClassicExcelStuff();

        Cell c = null;

        //Cell style for header row
        CellStyle cs = myWorkBook.createCellStyle();
        cs.setFillForegroundColor(HSSFColor.LIME.index);
        cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        HSSFSheet mySheet =myWorkBook.getSheet(validSheet);
        boolean isExist=false;
        Row row;
        //check person is exist
        for(int i=0; i<=mySheet.getLastRowNum(); i++){
            row=mySheet.getRow(i);
            c=row.getCell(0);
            if(newPerson.equals(c.toString())) isExist=true;
        }

        if(!isExist){
            row=mySheet.createRow(mySheet.getLastRowNum()+1);
            c=row.createCell(0);
            c.setCellValue(newPerson);
        }
        else Toast.makeText(getApplicationContext(),"KİŞİ ZATEN VAR",Toast.LENGTH_LONG).show();

        File SaveFile=new File(getApplicationContext().getExternalFilesDir(null),validRecordFile);
        FileOutputStream os=null;
        try {
            os = new FileOutputStream(SaveFile);
            myWorkBook.write(os);
            Log.w("FileUtils", "Writing file" + SaveFile);
        } catch (IOException e) {
            Log.w("FileUtils", "Error writing " + SaveFile, e);
        } catch (Exception e) {
            Log.w("FileUtils", "Failed to save file", e);
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
            }
        }
    }
    //----------------------------------------------------------------------
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
