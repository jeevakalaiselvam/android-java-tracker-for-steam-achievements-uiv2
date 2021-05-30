package com.gamerguide.android.trackerforsteamachivementss.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gamerguide.android.trackerforsteamachivementss.Object.Achievement;
import com.gamerguide.android.trackerforsteamachivementss.Object.Game;
import com.gamerguide.android.trackerforsteamachivementss.Fragment.FragmentFlowAchievement;
import com.gamerguide.android.trackerforsteamachivementss.Fragment.FragmentGlobalAchievements;
import com.gamerguide.android.trackerforsteamachivementss.Fragment.FragmentLockedAchievement;
import com.gamerguide.android.trackerforsteamachivementss.Fragment.FragmentPlannerAchievement;
import com.gamerguide.android.trackerforsteamachivementss.Fragment.FragmentUnlockedAchievement;
import com.gamerguide.android.trackerforsteamachivementss.Helper.BlurTransformation;
import com.gamerguide.android.trackerforsteamachivementss.Helper.ZUtils;
import com.gamerguide.android.trackerforsteamachivementss.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class GameActivity extends AppCompatActivity implements Serializable {

    private int UNLOCKED_OR_NOT_STATUS_GAMES_SET = 0;

    private TextView global, locked, unlocked, planner, flow;
    private ProgressBar progressBar;
    private TextView status, title, sub;
    private ViewGroup main;
    private ImageView home, recent;
    private ViewGroup frame, completionFrame;
    private EditText search;
    private ImageView background,icon;
    private TextView filter,name,desc,percentage;
    private ViewGroup minimalInclude;

    private HashMap<String,String> achievementProgress;

    private FragmentManager mFragmentManager;
    private Fragment mFragment;
    private FragmentTransaction mFragmentTransaction;

    private ZUtils zUtils;
    public Game game;
    private String gameId;
    public String searchKey = "";

    private int GAME_SCHEMA_DOWNLOADED = 0;
    public static int ACHIEVEMENT_PERCENTAGE_DOWNLOADED = 0;
    private static final int NUM_PAGES = 3;

    @Override
    protected void onResume() {
        super.onResume();

        zUtils = new ZUtils(this);

        if (zUtils.getSharedPreferenceBoolean(GameActivity.this, "came_back_activity", false)) {

            setupAchievementAdapter();
            zUtils.insertSharedPreferenceBoolean(GameActivity.this, "came_back_activity", false);
        }


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game_new);



        mFragmentManager = getSupportFragmentManager();
        background = findViewById(R.id.background);
        //setRandomGradientDrawable();
        main = findViewById(R.id.main);
        filter = findViewById(R.id.filter);
        title = findViewById(R.id.title);
        progressBar = findViewById(R.id.progress);
        name = findViewById(R.id.name);
        desc = findViewById(R.id.desc);
        percentage = findViewById(R.id.percentage);
        icon = findViewById(R.id.icon);
        minimalInclude = findViewById(R.id.minimal_include);
        achievementProgress = new HashMap<>();


        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        game = gson.fromJson(getIntent().getStringExtra("game"), Game.class);

        title.setText(game.getName());

        zUtils = new ZUtils(this);

        zUtils.insertSharedPreferenceString(GameActivity.this, "gamebackground_id", game.getId());


        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(GameActivity.this, FilterActivity.class);
                startActivityForResult(i, 1200);

            }
        });


        setupBackgroundBlur();




    }



    private void setRandomGradientDrawable() {


        Random rnd = new Random();
        int color1 = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        int color2 = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{color1, color2});
        gd.setCornerRadius(0f);

        background.setBackgroundDrawable(gd);
    }

    public void setupViewItems() {

        getAllAchievementsAndData();
    }


    public void setupBackgroundBlur() {
        showRefreshingAchievements();
        hideMinimalInclude();
        Picasso.get()
                .load(zUtils.getGameImageURL(game.getId()))
                .transform(new BlurTransformation(GameActivity.this))
                .into(background);

        setAlphaAnimation(background);

        setupViewItems();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1200) {
            if (resultCode == Activity.RESULT_OK) {

                setupDefaultFragment();

            }
        }

    }

    private void getAllAchievementsAndData() {


        getGamesAndAssociatedData();


    }

    private void getGamesAndAssociatedData() {

        getGameSchema(game);
    }

    private void getGameSchema(final Game game) {

        RequestQueue queue = Volley.newRequestQueue(GameActivity.this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, ZUtils.getGameSchemaURL(game.getId()),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        zUtils.logError("JEEVA_URL: " + ZUtils.getGameSchemaURL(game.getId()));
                        parseGameSchemaJSON(response, game);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


            }
        });

        queue.add(stringRequest);
    }

    public void showRefreshingAchievements(){

        Toast toast = Toast.makeText(GameActivity.this, "Getting Achievement List...", Toast.LENGTH_SHORT);
        LinearLayout toastLayout = (LinearLayout) toast.getView();
        TextView toastTV = (TextView) toastLayout.getChildAt(0);
        toastTV.setTextSize(14);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/dinprom.ttf");
        toastTV.setTypeface(face);
        toast.show();


    }

    public void showNoAchievementMessage() {

        Toast toast = Toast.makeText(GameActivity.this, "Game has no Steam Achievements...", Toast.LENGTH_SHORT);
        LinearLayout toastLayout = (LinearLayout) toast.getView();
        TextView toastTV = (TextView) toastLayout.getChildAt(0);
        toastTV.setTextSize(14);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/dinprom.ttf");
        toastTV.setTypeface(face);
        toast.show();
    }

    public void showAllAchievementsUnlocked() {

        Toast toast = Toast.makeText(GameActivity.this, "All Achievements Unlocked...", Toast.LENGTH_SHORT);
        LinearLayout toastLayout = (LinearLayout) toast.getView();
        TextView toastTV = (TextView) toastLayout.getChildAt(0);
        toastTV.setTextSize(14);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/dinprom.ttf");
        toastTV.setTypeface(face);
        toast.show();
    }

    private void parseGameSchemaJSON(String response, Game game) {


        try {

            game.getAchievements().clear();

            JSONObject obj = new JSONObject(response);
            JSONObject gameI = obj.getJSONObject("game");
            JSONObject stats = gameI.getJSONObject("availableGameStats");

            JSONArray achievements = stats.getJSONArray("achievements");

            if (achievements.length() != 0) {

                for (int i = 0; i < achievements.length(); i++) {


                    JSONObject innerObj = new JSONObject(String.valueOf(achievements.get(i)));
                    Achievement achievement = new Achievement();
                    achievement.setId(innerObj.getString("name").trim());
                    achievement.setName(innerObj.getString("displayName").trim());
                    achievement.setIcon(innerObj.getString("icon").trim());
                    achievement.setHidden(innerObj.getString("hidden").trim());
                    achievement.setIconLocked(innerObj.getString("icongray").trim());

                    if (innerObj.getString("hidden").equalsIgnoreCase("0")) {
                        achievement.setDesc(innerObj.getString("description"));
                    } else {
                        achievement.setDesc(zUtils.getSharedPreferenceString(GameActivity.this, game.getId()
                                + achievement.getId(), "Hidden Achievement"));
                    }

                    game.addAchievement(achievement);
                    zUtils.logError("JEEVA_INSERT:" + game.getName() + " { " + achievement.getName() + " " + achievement.getIcon() + " " + achievement.getUnlocked());

                }
                setupGamesInList();
            }


        } catch (JSONException e) {
            showNoAchievementMessage();
        }

    }


    private void setupGamesInList() {

        getAllPlayerAchievement(game);

    }

    private void getAllPlayerAchievement(final Game game) {

        RequestQueue queue = Volley.newRequestQueue(GameActivity.this);

        zUtils.logError("JEEVA_PERCENTAGE:" + ZUtils.getPlayerAchievements(game.getId()));
        StringRequest stringRequest = new StringRequest(Request.Method.GET, ZUtils.getPlayerAchievements(game.getId()),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        parseAllPlayerAchievementJSON(response, game);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                zUtils.logError("JEEVA_ERROR:" + error.getLocalizedMessage());
            }
        });

        queue.add(stringRequest);
    }

    private void parseAllPlayerAchievementJSON(String response, Game game) {

        try {

            JSONObject obj = new JSONObject(response);

            JSONObject stats = obj.getJSONObject("playerstats");

            JSONArray achievements = stats.getJSONArray("achievements");


            if (achievements.length() != 0) {

                for (int i = 0; i < achievements.length(); i++) {

                    JSONObject innerObj = new JSONObject(String.valueOf(achievements.get(i)));

                    String achievementID = innerObj.getString("apiname").trim();
                    String achievementUnlocked = innerObj.getString("achieved").trim();
                    String achievementUnlockedTime = innerObj.getString("unlocktime").trim();

                    for (Achievement achievement : game.getAchievements()) {
                        if (achievement.getId().equalsIgnoreCase(achievementID)) {
                            achievement.setUnlocked(achievementUnlocked);
                            achievement.setUnlockTime(achievementUnlockedTime);
                        }
                    }

                    zUtils.logError("JEEVA_INSERT:" + achievementID + " { " + achievementUnlocked + " " + achievementUnlockedTime + " ");

                }


                UNLOCKED_OR_NOT_STATUS_GAMES_SET += 1;

                setupDownloadForAchievementPercentage(game);

            }

        } catch (JSONException e) {

        }


    }


    private void setupDownloadForAchievementPercentage(final Game game) {

        RequestQueue queue = Volley.newRequestQueue(GameActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, ZUtils.getGameGlobalPercentage(game.getId()),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        parseAllPercentageAchievementJSON(response, game);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(stringRequest);
    }

    private void parseAllPercentageAchievementJSON(String response, Game game) {

        try {

            JSONObject obj = new JSONObject(response);
            JSONObject gameI = obj.getJSONObject("achievementpercentages");
            JSONArray achievements = gameI.getJSONArray("achievements");
            if (achievements.length() != 0) {

                for (int i = 0; i < achievements.length(); i++) {

                    JSONObject innerObj = new JSONObject(String.valueOf(achievements.get(i)));
                    for (Achievement achievement : game.getAchievements()) {
                        if (innerObj.getString("name").equalsIgnoreCase(achievement.getId())) {
                            achievement.setPercentage(innerObj.getString("percent"));
                            ACHIEVEMENT_PERCENTAGE_DOWNLOADED++;
                        }
                    }
                    setupAchievementAdapter();
                }

                //setupAProgress();

            }


        } catch (JSONException e) {

        }
    }

    private void setupAProgress() {

        RequestQueue queue = Volley.newRequestQueue(GameActivity.this);

        zUtils.logError("Data found URL: " + ZUtils.getAchievmentProgress(game.getId()));
        StringRequest stringRequest = new StringRequest(Request.Method.GET, ZUtils.getAchievmentProgress(game.getId()),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        parseGameProgress(response, game);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(stringRequest);

    }

    private void parseGameProgress(String response, Game game) {

        try {
            achievementProgress = new HashMap<>();
            Document doc = Jsoup.parse(response);

            Elements achievement_row = doc.getElementsByClass("achieveRow");
            for (Element achievement : achievement_row) {

                Elements achievement_text = achievement.getElementsByClass("achieveTxtHolder");

                for (Element achievementInner : achievement_text) {

                    String achievementName = "";
                    String achievementProgress = "";

                    Elements achievement_name = achievementInner.getElementsByTag("h3");

                    for (Element achievement_name_data : achievement_name) {
                        zUtils.logError("Data found: " + achievement_name_data.data());
                        achievementName = achievement_name_data.data();

                    }

                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        setupAchievementAdapter();
    }

    private void setupAchievementAdapter() {
        setupDefaultFragment();
        //getAchievementDescription();
    }

    int pointer = -1;


    private void parseHTMLHidden(String response, Achievement achievement) {

        Document doc = Jsoup.parse(response);
        try {


            Elements tr = doc.getElementsByTag("tr");


            for (int i = 1; i < tr.size(); i++) {

                String nameAll = tr.get(i).text().substring(0, tr.get(i).text().indexOf("%") - 4);

                if (nameAll.contains(achievement.getName())) {
                    achievement.setDesc(nameAll.replaceFirst(achievement.getName(), ""));
                }
            }
            ACHIEVEMENT_PERCENTAGE_DOWNLOADED++;
            setupDefaultFragment();


        } catch (Exception e) {
            e.printStackTrace();
            zUtils.logError("JEEVA_HTMLERROR: " + e.getMessage());
        }
    }

    private void setupDefaultFragment() {

        int tag = zUtils.getSharedPreferenceInt(GameActivity.this, "game_default", 0);


        switch (tag) {
            case 0:
                setupGlobalFragment();
                break;
            case 1:
                setupUnlockedFragment();
                break;
            case 2:
                setupLockedFragment();
                break;
            default:
                setupGlobalFragment();
                break;
        }

    }

    private void setupGlobalFragment() {

        setHomeSelected();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragment = new FragmentGlobalAchievements();
        mFragmentTransaction.replace(R.id.fragment_container_main, mFragment).commit();
        progressBar.setVisibility(View.GONE);
        //setupCounts();
    }

    private void setupUnlockedFragment() {

        setHomeSelected();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragment = new FragmentUnlockedAchievement();
        mFragmentTransaction.replace(R.id.fragment_container_main, mFragment).commit();
        progressBar.setVisibility(View.GONE);
        //setupCounts();
    }

    private void setupLockedFragment() {

        int notlocked = 0;

        for (Achievement achievement: game.getAchievements()){
            if (achievement.getUnlocked().equalsIgnoreCase("0"))
                notlocked++;
        }

        if(notlocked == 0){

            showAllAchievementsUnlocked();

        }else{

            setHomeSelected();
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragment = new FragmentLockedAchievement();
            mFragmentTransaction.replace(R.id.fragment_container_main, mFragment).commit();
            progressBar.setVisibility(View.GONE);
            //setupCounts();
        }

    }

    private void setHomeSelected() {



    }

    private void setupPlannerFragment() {

        setHomeSelected();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragment = new FragmentPlannerAchievement();
        mFragmentTransaction.replace(R.id.fragment_container_main, mFragment).commit();
        progressBar.setVisibility(View.GONE);
        //setupCounts();
    }

    private void setupFlowFragment() {

        //setFlowSelected();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragment = new FragmentFlowAchievement();
        mFragmentTransaction.replace(R.id.fragment_container_main, mFragment).commit();
        //setupCounts();
    }


    public void hideLoadingUI() {
    }

    public void setupCounts() {
    }

    public Game getGame() {

        return game;
    }

    public void updateStatusSubText(String filtered_by_percentage) {
    }

    public void updateStatusText(String s) {
    }


    private void setAlphaAnimation(View v) {


        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(v, "alpha", 1f, 0f);
        fadeOut.setDuration(2000);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(v, "alpha", 0f, 1f);
        fadeIn.setDuration(2000);

        final AnimatorSet mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeIn);

        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        mAnimationSet.start();
    }

    public void setupAchievementData(final Achievement achievement){


        DecimalFormat form = new DecimalFormat("0.0");
        final float trophyPercentage = Float.parseFloat(achievement.getPercentage());

        final String presentData = zUtils.getSharedPreferenceString(GameActivity.this, "custom_note" + game.getId() +achievement.getId(), "");


        if (name != null)
            name.setText(achievement.getName().trim());

        if (name != null)
            desc.setText(achievement.getDesc().trim());

        Picasso.get().load(achievement.getIcon()).resize(64, 64).into(icon);


        percentage.setText(String.valueOf(form.format(trophyPercentage)) + " %");

        if(trophyPercentage < 10.0f){

            percentage.setTextColor(getResources().getColor(R.color.gold));
            percentage.setShadowLayer(20f, 1, 1, getResources().getColor(R.color.gold));

        }else{
            percentage.setTextColor(getResources().getColor(R.color.colorTextWHITE));
            percentage.setShadowLayer(10f, 1, 1, getResources().getColor(R.color.colorTextWHITE));
        }

        if (achievement.getDesc().equalsIgnoreCase("Hidden Achievement") && achievement.getHidden().equalsIgnoreCase("1")) {

            getAchievementDescription(game.getId(), achievement, desc);

        } else {
            desc.setText(achievement.getDesc());
        }

        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.google.com/search?q=" + achievement.getName() + " achievement trophy "  + game.getName()  ;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

    }

    private void getAchievementDescription(final String id, final Achievement achievement, final TextView desc) {

        RequestQueue queue = Volley.newRequestQueue(GameActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, ZUtils.getSteamDBHiddden(id),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        zUtils.logError("JEEVA_HIDDENURL: " + ZUtils.getSteamDBHiddden(id));
                        parseHTMLHidden(response, achievement, desc);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                zUtils.logError("JEEVA_HIDDENERROR: " + error.getLocalizedMessage());
            }
        });

        queue.add(stringRequest);
    }

    private void parseHTMLHidden(String response, Achievement achievement, TextView desc) {

        Document doc = Jsoup.parse(response);
        try {


            Elements tr = doc.getElementsByTag("tr");


            for (int i = 1; i < tr.size(); i++) {

                String nameAll = tr.get(i).text().substring(0, tr.get(i).text().indexOf("%") - 4);

                if (nameAll.contains(achievement.getName())) {
                    achievement.setDesc(nameAll.replaceFirst(achievement.getName(), "").trim());
                    zUtils.insertSharedPreferenceString(GameActivity.this, game.getId() + achievement.getId(), nameAll.replaceFirst(achievement.getName(), "").trim());
                }
            }

            if (desc != null) {

                desc.setText(String.valueOf(achievement.getDesc().trim()));
            }


        } catch (Exception e) {
            e.printStackTrace();
            zUtils.logError("JEEVA_HTMLERROR: " + e.getMessage());
        }
    }

    public void hideMinimalInclude(){
        minimalInclude.setVisibility(View.GONE);
    }

    public void showMinimalInclude(){
        setAlphaAnimation(minimalInclude);
        minimalInclude.setVisibility(View.VISIBLE);
    }
}
