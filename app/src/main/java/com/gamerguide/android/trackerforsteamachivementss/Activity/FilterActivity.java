package com.gamerguide.android.trackerforsteamachivementss.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.gamerguide.android.trackerforsteamachivementss.Helper.ZUtils;
import com.gamerguide.android.trackerforsteamachivementss.R;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class FilterActivity extends AppCompatActivity {


    TextView global,locked,unlocked,name,percentage,recent,hidden,detail,minimal;
    Button apply,reset;
    ZUtils zUtils;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_filter);

        zUtils = new ZUtils(this);
        global = findViewById(R.id.global);
        locked = findViewById(R.id.locked);
        unlocked = findViewById(R.id.unlocked);
        reset = findViewById(R.id.reset);
        apply = findViewById(R.id.apply);


        name = findViewById(R.id.name);
        percentage = findViewById(R.id.percentage);
        hidden = findViewById(R.id.hidden);
        recent = findViewById(R.id.recent);
        detail = findViewById(R.id.detail);
        minimal = findViewById(R.id.minimal);


        setupListeners();


    }

    private void setupListeners() {

        global.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                zUtils.insertSharedPreferenceInt(FilterActivity.this,"game_default",0);
                zUtils.insertSharedPreferenceInt(FilterActivity.this,"filter_activity_one",0);
                setGlobalSelected();
            }
        });

        unlocked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zUtils.insertSharedPreferenceInt(FilterActivity.this,"game_default",1);
                zUtils.insertSharedPreferenceInt(FilterActivity.this,"filter_activity_one",1);
                setUnlockedSelected();

            }
        });

        locked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zUtils.insertSharedPreferenceInt(FilterActivity.this,"game_default",2);
                zUtils.insertSharedPreferenceInt(FilterActivity.this,"filter_activity_one",2);
                setLockedSelected();

            }
        });

        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zUtils.insertSharedPreferenceInt(FilterActivity.this,"filter_activity_two",0);
                zUtils.insertSharedPreferenceInt(FilterActivity.this,"achievement_sort_order",0);
                setNameSelected();

            }
        });

        percentage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zUtils.insertSharedPreferenceInt(FilterActivity.this,"filter_activity_two",1);
                zUtils.insertSharedPreferenceInt(FilterActivity.this,"achievement_sort_order",1);
                setPercentageSelected();

            }
        });

        hidden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zUtils.insertSharedPreferenceInt(FilterActivity.this,"filter_activity_two",2);
                zUtils.insertSharedPreferenceInt(FilterActivity.this,"achievement_sort_order",2);
                setHiddenSelected();

            }
        });

        recent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zUtils.insertSharedPreferenceInt(FilterActivity.this,"filter_activity_two",3);
                zUtils.insertSharedPreferenceInt(FilterActivity.this,"achievement_sort_order",3);
                setRecentSelected();

            }
        });

        detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zUtils.insertSharedPreferenceInt(FilterActivity.this,"filter_activity_three",0);
                zUtils.insertSharedPreferenceInt(FilterActivity.this,"viewtype_preference",0);
                setDetailedSelected();

            }
        });

        minimal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zUtils.insertSharedPreferenceInt(FilterActivity.this,"filter_activity_three",1);
                zUtils.insertSharedPreferenceInt(FilterActivity.this,"viewtype_preference",1);
                setMinimalSelected();

            }
        });

        checkandSetupPreviousSelections();

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishFilterActivity();
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                global.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
                unlocked.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
                locked.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
                name.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
                percentage.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
                hidden.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
                recent.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
            }
        });
    }

    private void setGlobalSelected() {
        global.setBackground(getResources().getDrawable(R.drawable.color_button2bgdark));
        unlocked.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
        locked.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
    }


    private void setUnlockedSelected() {
        global.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
        unlocked.setBackground(getResources().getDrawable(R.drawable.color_button2bgdark));
        locked.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
    }


    private void setLockedSelected() {
        global.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
        unlocked.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
        locked.setBackground(getResources().getDrawable(R.drawable.color_button2bgdark));
    }

    private void setNameSelected() {
        name.setBackground(getResources().getDrawable(R.drawable.color_button2bgdark));
        percentage.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
        hidden.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
        recent.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
    }

    private void setPercentageSelected() {
        name.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
        percentage.setBackground(getResources().getDrawable(R.drawable.color_button2bgdark));
        hidden.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
        recent.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
    }

    private void setHiddenSelected() {
        name.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
        percentage.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
        hidden.setBackground(getResources().getDrawable(R.drawable.color_button2bgdark));
        recent.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
    }

    private void setRecentSelected() {
        name.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
        percentage.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
        hidden.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
        recent.setBackground(getResources().getDrawable(R.drawable.color_button2bgdark));
    }

    private void setDetailedSelected() {
        detail.setBackground(getResources().getDrawable(R.drawable.color_button2bgdark));
        minimal.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
    }

    private void setMinimalSelected() {
        detail.setBackground(getResources().getDrawable(R.drawable.color_button2bg));
        minimal.setBackground(getResources().getDrawable(R.drawable.color_button2bgdark));
    }

    private void checkandSetupPreviousSelections() {

        int one = zUtils.getSharedPreferenceInt(FilterActivity.this,"filter_activity_one",0);
        int two = zUtils.getSharedPreferenceInt(FilterActivity.this,"filter_activity_two",0);
        int three = zUtils.getSharedPreferenceInt(FilterActivity.this,"filter_activity_three",0);

        switch (one){
            case 0:
                setGlobalSelected();
                break;
            case 1:
                setUnlockedSelected();
                break;
            case 2:
                setLockedSelected();
                break;
            default:
        }

        switch (two){
            case 0:
                setNameSelected();
                break;
            case 1:
                setPercentageSelected();
                break;
            case 2:
                setHiddenSelected();
                break;
            case 3:
                setRecentSelected();
                break;
            default:
        }

        switch (three){
            case 0:
                setDetailedSelected();
                break;
            case 1:
                setMinimalSelected();
                break;
            default:
        }

    }


    public void finishFilterActivity(){

        Intent output = new Intent();
        output.putExtra("filter", 0);
        setResult(RESULT_OK, output);
        finish();
    }


}
