package com.gamerguide.android.trackerforsteamachivementss.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.gamerguide.android.trackerforsteamachivementss.Helper.ZUtils;
import com.gamerguide.android.trackerforsteamachivementss.R;

public class HelpActivity extends AppCompatActivity {

    ImageView icon;
    private ImageView back, option;
    private Button done;
    ZUtils zUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_help);
        back = findViewById(R.id.back);;
        done = findViewById(R.id.done);

        icon = findViewById(R.id.option);
        icon.setVisibility(View.INVISIBLE);



        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });




    }
}
