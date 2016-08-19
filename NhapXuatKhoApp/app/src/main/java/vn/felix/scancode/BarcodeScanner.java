package vn.felix.scancode;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import vn.felix.scancode.adapter.ListCodeAdapter;
import vn.felix.scancode.data.DbHelper;
import vn.felix.scancode.object.InfoCode;
import vn.felix.scancode.util.SharedPreferenceKeyType;

/**
 * Created by kvprasad on 10/3/2015.
 */
public class BarcodeScanner extends Activity {

    private static final String TAG = "excelFile";
    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;

    private LinearLayout llHoanThanh, type1, type0, type2;
    private ImageScanner scanner;
    private ListView lvListCode;
    private ArrayList<InfoCode> infoCodes;
    private ListCodeAdapter listCodeAdapter;

    private boolean barcodeScanned = false;
    private boolean previewing = true;

    private DbHelper mHelper;
    private SQLiteDatabase dataBase;

    private int type = -1; //type=0: tamp, type=1: Serial/IMEI, type=2: Tamp%Serial/IMEI

    private int record = 0;
    private Boolean isSave = false;
    private String temp = "";
    private InfoCode infoCode = null;
    private SharedPreferences preferences;

    //Excel file
    HSSFWorkbook workbook;
    HSSFSheet sheet;
    HSSFRow rowhead;

    static {
        System.loadLibrary("iconv");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_scanner);

        initControls();
    }

    private void initControls() {

        type = getIntent().getExtras().getInt("type");


        workbook = new HSSFWorkbook();
        sheet = workbook.createSheet("FirstSheet");
        rowhead = sheet.createRow((short) 0);
        rowhead.createCell((short) 0).setCellValue("No.");
        rowhead.createCell((short) 1).setCellValue("stampCode");
        rowhead.createCell((short) 2).setCellValue("serialCode");
        rowhead.createCell((short) 3).setCellValue("dateScan");
        rowhead.createCell((short) 4).setCellValue("plotsCode");
        rowhead.createCell((short) 5).setCellValue("nameProduce");


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        autoFocusHandler = new Handler();
        infoCodes = new ArrayList<>();
        mHelper = new DbHelper(this);

        mCamera = getCameraInstance();

        // Instance barcode scanner
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        mPreview = new CameraPreview(BarcodeScanner.this, mCamera, previewCb,
                autoFocusCB);
        FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
        preview.addView(mPreview);

        llHoanThanh = (LinearLayout) findViewById(R.id.llHoanThanh);
        type0 = (LinearLayout) findViewById(R.id.type0);
        type1 = (LinearLayout) findViewById(R.id.type1);
        type2 = (LinearLayout) findViewById(R.id.type2);
        if (type == 0) {
            type0.setVisibility(View.VISIBLE);
            type1.setVisibility(View.GONE);
            type2.setVisibility(View.GONE);
        } else if (type == 1) {
            type0.setVisibility(View.GONE);
            type1.setVisibility(View.VISIBLE);
            type2.setVisibility(View.GONE);
        } else {
            type0.setVisibility(View.GONE);
            type1.setVisibility(View.GONE);
            type2.setVisibility(View.VISIBLE);
        }

        lvListCode = (ListView) findViewById(R.id.lvListCode);
        listCodeAdapter = new ListCodeAdapter(BarcodeScanner.this, R.layout.custom_item_list, infoCodes);
        lvListCode.setAdapter(listCodeAdapter);

        llHoanThanh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (barcodeScanned) {
                    resetScan();
                }
                preferences = getSharedPreferences(SharedPreferenceKeyType.DATA_INFO.toString(), Context.MODE_PRIVATE);
                int recode = preferences.getInt(SharedPreferenceKeyType.NUMBER_RECORE_XLC.toString(), 0);
                dataBase = mHelper.getWritableDatabase();
                Cursor mCursor = dataBase.rawQuery("SELECT * FROM "
                        + DbHelper.TABLE_NAME, null);
                if (mCursor.moveToFirst()) {
                    do {
                        HSSFRow row = sheet.createRow((short) recode);
                        row.createCell((short) 0).setCellValue(recode + "");
                        row.createCell((short) 1).setCellValue(mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_STAMPCODE)));
                        row.createCell((short) 2).setCellValue(mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_SERIALCODE)));
                        row.createCell((short) 3).setCellValue(mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_DATESCAN)));
                        row.createCell((short) 3).setCellValue(mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_NAME)));
                        row.createCell((short) 3).setCellValue(mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_PLOTSCODE)));
                        recode += 1;
                        // Create a path where we will place our List of objects on external storage
                        File file = new File(getExternalFilesDir(null), "myExcel.xls");
                        FileOutputStream os = null;

                        try {
                            os = new FileOutputStream(file);
                            workbook.write(os);
                            Log.w("FileUtils", "Writing file" + file);
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
                    } while (mCursor.moveToNext());

                    preferences.edit().putInt(SharedPreferenceKeyType.NUMBER_RECORE_XLC.toString(), recode).commit();
                }

            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            releaseCamera();
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
        }
        return c;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                mCamera.autoFocus(autoFocusCB);
        }
    };

    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

            int result = scanner.scanImage(barcode);

            if (result != 0) {
                previewing = false;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();

                SymbolSet syms = scanner.getResults();
                for (Symbol sym : syms) {

                    Log.i("<<<<<<Asset Code>>>>> ",
                            "<<<<Bar Code>>> " + sym.getData());
                    String scanResult = sym.getData().trim();


                    showAlertDialog(scanResult);


                  /*  Toast.makeText(BarcodeScanner.this, scanResult,
                            Toast.LENGTH_SHORT).show();*/

                    barcodeScanned = true;

                    break;
                }
            }
        }
    };

    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };


    private void showAlertDialog(final String message) {

        isSave = false;
        new AlertDialog.Builder(this)
                .setTitle("Save")
                .setCancelable(false)
                .setMessage(message)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (barcodeScanned) {
                            resetScan();
                        }
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (barcodeScanned) {
                            resetScan();
                            switch (type) {
                                case 2:
                                    if (record % 2 == 0) {
                                        temp = message;
                                        infoCode = new InfoCode();
                                        infoCode.setStampCode(message);
                                        infoCode.setSerialCode("");
                                        infoCode.setPlotsCode("");
                                        infoCode.setNameProduce("");
                                        infoCode.setDateScan("");
                                        infoCodes.add(infoCode);
                                        listCodeAdapter.notifyDataSetChanged();
                                        record += 1;
                                    } else {
                                        infoCode.setStampCode(temp);
                                        infoCode.setSerialCode(message);
                                        infoCode.setPlotsCode("");
                                        infoCode.setNameProduce("");
                                        infoCode.setDateScan("");
                                        infoCodes.set(infoCodes.size() - 1, infoCode);
                                        listCodeAdapter.notifyDataSetChanged();
                                        saveData(temp, message, "", "", "");
                                        record = 0;

                                    }
                            }
                        }
                    }
                })
                .show();
    }

    private void resetScan() {
        barcodeScanned = false;
        mCamera.setPreviewCallback(previewCb);
        mCamera.startPreview();
        previewing = true;
        mCamera.autoFocus(autoFocusCB);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (infoCodes.size() == 0) {
            displayData();
        }

    }

    private void displayData() {
        dataBase = mHelper.getWritableDatabase();
        Cursor mCursor = dataBase.rawQuery("SELECT * FROM "
                + DbHelper.TABLE_NAME, null);

        if (mCursor.moveToFirst()) {
            do {
                InfoCode infoCode = new InfoCode();
                infoCode.setStampCode(mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_STAMPCODE)));
                infoCode.setSerialCode(mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_SERIALCODE)));
                infoCode.setDateScan(mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_DATESCAN)));
                infoCode.setNameProduce(mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_NAME)));
                infoCode.setPlotsCode(mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_PLOTSCODE)));
                infoCodes.add(infoCode);

            } while (mCursor.moveToNext());
            listCodeAdapter.notifyDataSetChanged();
        }
        mCursor.close();
    }

    private void saveData(String stampCode, String serialCode, String dateScan, String plotsCode, String nameProduce) {
        dataBase = mHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DbHelper.KEY_DATESCAN, dateScan);
        values.put(DbHelper.KEY_NAME, nameProduce);
        values.put(DbHelper.KEY_PLOTSCODE, plotsCode);
        values.put(DbHelper.KEY_SERIALCODE, serialCode);
        values.put(DbHelper.KEY_STAMPCODE, stampCode);

        System.out.println("");
        //insert data into database
        dataBase.insert(DbHelper.TABLE_NAME, null, values);
        //close database
        dataBase.close();
    }

    private void deleteData(String key) {
        dataBase.delete(
                DbHelper.TABLE_NAME,
                DbHelper.KEY_STAMPCODE + "="
                        + key, null);
        displayData();
    }

}