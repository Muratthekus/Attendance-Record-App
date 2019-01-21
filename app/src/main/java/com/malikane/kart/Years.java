package com.malikane.kart;

import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Years extends AppCompatActivity {
    Button create,delete;
    EditText input;
    TableLayout table;
    TableRow row1,row2;
    String PATH;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_years);
        create=findViewById(R.id.ekle);
        delete=findViewById(R.id.delete);
        table=findViewById(R.id.tableLayout);
        row1=findViewById(R.id.row1);
        row2=findViewById(R.id.row2);

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
            final int numberCreatedRow=fileLength/4-1;//if value=0 we don't have to create dynamic TableRow
            for (int i = 0; i < numberCreatedRow; i++) {
                for (File file1 : files) {
                    if (fileLength > 4) {
                        if (i == 0) setTableRowContent(row1, row2, file1.getName());
                        else {
                            TableRow tableRow1 = new TableRow(this);//for show file logo
                            TableRow tableRow2 = new TableRow(this);//for show file name
                            setTableRowContent(tableRow1, tableRow2, file1.getName());
                            table.addView(tableRow1);
                            table.addView(tableRow2);
                        }
                    } else {
                        setTableRowContent(row1, row2, file1.getName());
                    }
                }

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
    private boolean createExcelFile(String fileName){
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
        sheet1 = wb.createSheet("DEMO18/19");

        // Generate column headings
        Row row = sheet1.createRow(0);

        c = row.createCell(0);
        c.setCellValue("Item Number");
        c.setCellStyle(cs);

        c = row.createCell(1);
        c.setCellValue("Quantity");
        c.setCellStyle(cs);

        c = row.createCell(2);
        c.setCellValue("Price");
        c.setCellStyle(cs);

        sheet1.setColumnWidth(0, (15 * 500));
        sheet1.setColumnWidth(1, (15 * 500));
        sheet1.setColumnWidth(2, (15 * 500));

        // Create a path where we will place our List of objects on external storage
        File file = new File(PATH, fileName);
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
    private void AlertMessage(final boolean bool, final String fileName){
        //if bool=true, user want to create a new year
        // else user want to delete a year
        File Resourcefile=new File(PATH);
        String message;
        if(bool) message="Yeni Dönem Yaratılacak";
        else message="Seçilen Dönem Silinecek";

        boolean check=false;
        //Check file is exit
        for(File f:Resourcefile.listFiles()){
            if(f.getName().equals(fileName)) check=true;
        }

        final boolean isExist=check;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("DEMO");
        builder.setMessage(message);
        builder.setPositiveButton("TAMAM", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(bool){
                    if(isExist) Toast.makeText(getApplicationContext(),"File Is Already Exist",Toast.LENGTH_LONG).show();
                    else createExcelFile(fileName);
                }
                else{
                    //DELETE EXCEL FILE STUFF
                }

            }
        });
        //If User Choose Negative Button Then Nothing Will Happen
        builder.setNegativeButton("İPTAL", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        builder.show();
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
