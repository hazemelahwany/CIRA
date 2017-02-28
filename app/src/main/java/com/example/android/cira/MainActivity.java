package com.example.android.cira;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            WelcomeFragement welcomeFragement = new WelcomeFragement();
            getFragmentManager().beginTransaction().add(R.id.main_container,
                    welcomeFragement).commit();
        }
    }
}
