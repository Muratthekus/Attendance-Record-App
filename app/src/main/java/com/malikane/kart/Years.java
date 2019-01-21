package com.malikane.kart;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Years extends AppCompatActivity {
    Button create,delete;
    EditText input;
    TableLayout table;
    TableRow row1,row2;
    String PATH;
    static SharedPreferences data;
    static SharedPreferences.Editor editor;

    @Override
    protected void onResume() {
        super.onResume();
        fullScreen();
    }

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_years);
        create=findViewById(R.id.ekle);
        delete=findViewById(R.id.delete);
        table=findViewById(R.id.tableLayout);
        row1=findViewById(R.id.row1);
        row2=findViewById(R.id.row2);
        data= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = data.edit();
        //Exact file saving location
        PATH=getApplicationContext().getExternalFilesDir(null).getAbsolutePath();
        input=findViewById(R.id.EditText);
        ResourceDataShow();

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!input.getText().toString().equals("")) AlertMessage(true,input.getText().toString());
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!input.getText().toString().equals("")) AlertMessage(false,input.getText().toString());
            }
        });
    }
    //This method for show xls file in TableLayout
    private void ResourceDataShow(){
        File file=new File(PATH);
        //Clear view objects for any redrawing situation
        row1.removeAllViews();
        row2.removeAllViews();
        //---------------------------
        if(file.listFiles().length!=0){
            File[]files=file.listFiles();
            int fileLength=files.length;
            if (fileLength > 4) {
                for(int j=0; j<4; j++) setTableRowContent(row1, row2, files[j].getName());//For Show First 4 file
                TableRow tableRow1=null,tableRow2=null;
                for (int i = 4; i < files.length; i++){
                    if(i%4==0) {
                        tableRow1 = new TableRow(this);//for show file logo
                        tableRow2 = new TableRow(this);//for show file name
                    }
                    assert tableRow1!=null;
                    setTableRowContent(tableRow1, tableRow2, files[i].getName());
                    table.addView(tableRow1);
                    table.addView(tableRow2);

                }
            }
            else {
                for (File file1 : files) setTableRowContent(row1, row2, file1.getName());
            }

        }
    }

    private void setTableRowContent(TableRow row1, TableRow row2,String fileName){
        ImageView image; //this will use for show file logo
        TextView text; //this will use for show file name
        image=new ImageView(this);
        image=setFileLogo(image);
        text=new TextView(this);
        text.setText(fileName);//fletch and put name
        text.setTextIsSelectable(true);
        row1.addView(image);
        row2.addView(text);
    }

    private ImageView setFileLogo(ImageView image){
        image.setImageResource(R.drawable.excel);
        return image;
    }

    //If Excel file is not exist which user want to create, then create Excel file from beginning
    private boolean createExcelFile(String fileName,String sheetName){
        // check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Toast.makeText(this,"Storage not available or read only",Toast.LENGTH_LONG).show();
            return false;
        }
        boolean success = false;

        //New Workbook
        Workbook wb = new HSSFWorkbook();

        Cell c = null;

        //Cell style for header row
        CellStyle cs = wb.createCellStyle();
        cs.setFillForegroundColor(HSSFColor.LIME.index);
        cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        //New Sheet
        Sheet sheet1 = null;
        sheet1 = wb.createSheet(sheetName);

        // Generate column headings
        Row row = sheet1.createRow(0);

        c = row.createCell(0);
        c.setCellValue("İsim/Gün");
        c.setCellStyle(cs);

        sheet1.setColumnWidth(0, (15 * 500));

        // Create a path where we will place our List of objects on external storage
        File file = new File(getApplicationContext().getExternalFilesDir(null), fileName+".xlsx");
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            wb.write(os);
            Log.w("FileUtils", "Writing file" + file);
            success = true;
        } catch (IOException e) {
            Log.w("FileUtils", "Error writing " + file, e);
        } catch (Exception e) {
            Log.w("FileUtils", "Failed to save file", e);
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
            }
        }
        return success;
    }

    //If Excel file is already exist and user want to add new sheet to that file
    private void addSheetExcel(String fileName,String sheetName) throws IOException {
        // check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Toast.makeText(this,"Storage not available or read only",Toast.LENGTH_LONG).show();
            return;
        }

        File file = new File(getApplicationContext().getExternalFilesDir(null), fileName+".xlsx");
        FileInputStream myInput = new FileInputStream(file);

        // Create a POIFSFileSystem object
        POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

        // Create a workbook using the File System
        HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

        Cell c = null;

        //Cell style for header row
        CellStyle cs = myWorkBook.createCellStyle();
        cs.setFillForegroundColor(HSSFColor.LIME.index);
        cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        HSSFSheet mySheet =myWorkBook.createSheet(sheetName);
        Row row = mySheet.createRow(0);
        c=row.createCell(0);
        c.setCellValue("İsim/Gün");
        c.setCellStyle(cs);
        mySheet.setColumnWidth(0, (15 * 500));

        File SaveFile=new File(getApplicationContext().getExternalFilesDir(null),fileName+".xlsx");
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

    //Sheet is Exist, So we ask the user, Does He/She want to add new sheet or not
    private void SheetExistAlertMessage(final String filename){
        final EditText sheetnameInput=new EditText(this);
        sheetnameInput.setInputType(InputType.TYPE_CLASS_TEXT);//Getting the sheet name
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("DEMO");
        builder.setMessage("Dosya Var, Eklenecek Sheet Ismini Giriniz");
        builder.setView(sheetnameInput);
        builder.setPositiveButton("TAMAM", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    addSheetExcel(filename,sheetnameInput.getText().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //If User Choose Negative Button Then Nothing Will Happen
        builder.setNegativeButton("İPTAL", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    //For take sheet name from user, we create an alert message
    private void createNewExcelAlertMessage(final String filename){
        final EditText sheetnameInput=new EditText(this);
        sheetnameInput.setInputType(InputType.TYPE_CLASS_TEXT);//Getting the sheet name
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("DEMO");
        builder.setMessage("Sheet Ismı Giriniz");
        builder.setView(sheetnameInput);

        builder.setPositiveButton("TAMAM", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                createExcelFile(filename,sheetnameInput.getText().toString());
            }
        });
        //If User Choose Negative Button Then Nothing Will Happen
        builder.setNegativeButton("İPTAL", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    private void AlertMessage(final boolean bool, final String fileName){
        //if bool=true, user want to create a new Excel File
        // else user want to delete an Excel File
        final File Resourcefile=new File(PATH);
        String message;
        if(bool) message="Yeni Dönem Yaratılacak";
        else message="Seçilen Dönem Silinecek";

        boolean check=false;
        //Check file is exit
        for(File f:Resourcefile.listFiles()){
            if(f.getName().equals(fileName+".xlsx")) check=true;
        }
        final boolean isExist=check;
        //Alert Dialog Part
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("DEMO");
        builder.setMessage(message);

        builder.setPositiveButton("TAMAM", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //File Create
                if(bool){
                    if(isExist){//File is Already Exist
                        SheetExistAlertMessage(fileName);
                    }
                    else{
                        createNewExcelAlertMessage(fileName);
                    }
                }
                //File delete
                else{
                    if(isExist){
                        for(File f:Resourcefile.listFiles()){
                            if(f.getName().equals(fileName+".xlsx")) f.delete();
                        }
                    }
                    else Toast.makeText(getApplicationContext(),"Silmek İstediginiz Dosya Yok",Toast.LENGTH_LONG).show();

                }
            }
        });
        //If User Choose Negative Button Then Nothing Will Happen
        builder.setNegativeButton("İPTAL", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.show();
        ResourceDataShow();
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
    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }
    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }
}
