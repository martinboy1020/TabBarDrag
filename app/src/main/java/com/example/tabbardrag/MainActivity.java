package com.example.tabbardrag;

import android.os.Bundle;
import android.widget.TextView;

import com.example.tabbardraganddrop.BottomMenu;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements BottomMenu.BottomMenuClickCallBack {

    BottomMenu bottomMenu;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        title = findViewById(R.id.title);
        title.setText("現在是: " + "首頁");
        bottomMenu = findViewById(R.id.bottomMenu);
        bottomMenu.init(null, this);
    }

    @Override
    public void onBackPressed() {
        if(bottomMenu.checkIsSelectedMenuOpen()) {
            bottomMenu.dismissTopSelectList();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void BottomMenuClickEvent(String nowSelectedItemText) {
        title.setText("現在是: " + nowSelectedItemText);
    }
}
