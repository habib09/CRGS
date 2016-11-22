package com.mpower.android.crgs;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mpower.clientcollection.activities.ClientCollectionLoginActivity;
import com.mpower.clientcollection.activities.ClientCollectionMainActivity;
import com.mpower.clientcollection.activities.SplashScreenActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent start = new Intent(this, SplashScreenActivity.class);
        startActivity(start);
        finish();
    }
}
