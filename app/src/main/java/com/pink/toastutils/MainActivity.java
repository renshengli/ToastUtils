package com.pink.toastutils;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toastBtn:
                ToastUtils.makeText(this, "This is toastDialog", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
