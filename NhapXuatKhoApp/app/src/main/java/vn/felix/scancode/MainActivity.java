package vn.felix.scancode;

/**
 * Created by VietRuyn on 15/08/2016.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import vn.felix.scancode.util.SharedPreferenceKeyType;


public class MainActivity extends Activity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private LinearLayout llStart;
    private RelativeLayout rlLogin, llInputOutputWarehouse;
    private SharedPreferences preferences;
    private TextView tvUserName, tvPassWord;
    private Button btnLogin;
    private RadioButton rdTamp, rdSerialImei, rdTampAndImei;
    private RadioGroup radioGroup;


    private int type = -1; //type=0: tamp, type=1: Serial/IMEI, type=2: Tamp%Serial/IMEI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        llStart = (LinearLayout) findViewById(R.id.llStart);
        llInputOutputWarehouse = (RelativeLayout) findViewById(R.id.llInputOutputWarehouse);
        rlLogin = (RelativeLayout) findViewById(R.id.rlLogin);
        tvUserName = (TextView) findViewById(R.id.tvUserName);
        tvPassWord = (TextView) findViewById(R.id.tvPassWord);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        rdTamp = (RadioButton) findViewById(R.id.rdTamp);
        rdSerialImei = (RadioButton) findViewById(R.id.rdSerialImei);
        rdTampAndImei = (RadioButton) findViewById(R.id.rdTampAndImei);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        btnLogin.setOnClickListener(this);
        llStart.setOnClickListener(this);
        radioGroup.setOnCheckedChangeListener(this);

        preferences = getSharedPreferences(SharedPreferenceKeyType.DATA_INFO.toString(), MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean firstTime = preferences.getBoolean(SharedPreferenceKeyType.CHECK_LOGIN.toString(), false);
        if (!firstTime) {
            rlLogin.setVisibility(View.VISIBLE);
            llInputOutputWarehouse.setVisibility(View.GONE);
            preferences.edit().putInt(SharedPreferenceKeyType.NUMBER_RECORE_XLC.toString(), 1).commit();
        } else {
            rlLogin.setVisibility(View.GONE);
            llInputOutputWarehouse.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
                if (tvPassWord.getText().toString().trim().isEmpty() || tvUserName.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "Tên đăng nhập và mật khẩu không được để trống", Toast.LENGTH_SHORT);
                } else {
                    preferences.edit().putBoolean(SharedPreferenceKeyType.CHECK_LOGIN.toString(), true).commit();
                    rlLogin.setVisibility(View.GONE);
                    llInputOutputWarehouse.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.llStart:
                if (type >= 0 && type < 3) {
                    Intent intent = new Intent(this, BarcodeScanner.class);
                    intent.putExtra("type", type);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Vui lòng chọn loại mã quét", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i) {
            case R.id.rdTamp:
                type = 0;
                break;
            case R.id.rdSerialImei:
                type = 1;
                break;
            case R.id.rdTampAndImei:
                type = 2;
                break;
        }
    }
}