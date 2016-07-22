package com.lecongphuc.sampleproject;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SubActivity1 extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub1);
        Button btn=(Button) findViewById(R.id.btntrove);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

    }
}
