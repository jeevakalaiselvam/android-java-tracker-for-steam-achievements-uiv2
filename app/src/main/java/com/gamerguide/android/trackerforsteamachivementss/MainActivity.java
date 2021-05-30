package com.gamerguide.android.trackerforsteamachivementss;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gamerguide.android.trackerforsteamachivementss.Activity.GameActivity;
import com.gamerguide.android.trackerforsteamachivementss.Activity.HelpActivity;
import com.gamerguide.android.trackerforsteamachivementss.Helper.BlurTransformation;
import com.gamerguide.android.trackerforsteamachivementss.Helper.ZUtils;
import com.gamerguide.android.trackerforsteamachivementss.Object.Achievement;
import com.gamerguide.android.trackerforsteamachivementss.Object.Game;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class MainActivity extends AppCompatActivity {

    private ZUtils zUtils;

    private String SEARCH_QUERY = "";

    public static ArrayList<Game> games;
    private int viewType = 0;
    private RecyclerView gamesList;
    private ProgressBar progressBar;
    private ImageView setting, refresh;
    private TextView status;
    private EditText search;
    private ImageView background;
    private String backgroundGameID;

    private int TOTAL_GAMES = 0;
    private int OBTAINED_GAMES = 0;
    private int POSITION_POINTER = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main_new);
        background = findViewById(R.id.background);

        setAlphaAnimation(background);

        zUtils = new ZUtils(MainActivity.this);
        //Get default game id to setup background image from
        backgroundGameID = zUtils.getSharedPreferenceString(this, "gamebackground_id", "7000");


        gamesList = findViewById(R.id.games_list);
        progressBar = findViewById(R.id.progress);
        setting = findViewById(R.id.setting);
        status = findViewById(R.id.status);
        refresh = findViewById(R.id.refresh);
        search = findViewById(R.id.search);


        //Show progress bar when app is launched
        progressBar.setVisibility(View.VISIBLE);

        //Show help information when the user launches the app for the first time
        if (zUtils.getSharedPreferenceBoolean(this, "is_first_time", true)) {

            Intent i = new Intent(MainActivity.this, HelpActivity.class);
            startActivityForResult(i, 999);

        } else {
            //Get all games from steam
            setupGamesFromSteam();
        }

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showSettingsPanel();
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search.setText("");
            }
        });

        //When user searches for a game top, Find the game and show it in the list below.
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SEARCH_QUERY = String.valueOf(s);
                //Reset the adapter based on the search query for games
                setupAdapterForList();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        //Check Firebase for new update data
        checkUpdateForApp();

    }

    //Show sharing information to user
    private void showSharingPage() {

        int sharingCounterDone = zUtils.getSharedPreferenceInt(MainActivity.this, "sharing_counter_done", 0);
        int sharingCounter = zUtils.getSharedPreferenceInt(MainActivity.this, "sharing_counter", 0);

        if (sharingCounterDone == 0) {
            sharingCounter = sharingCounter + 1;
            zUtils.insertSharedPreferenceInt(MainActivity.this, "sharing_counter", sharingCounter);
            if ((sharingCounter % 10) == 0) {

                showSharingAlert(sharingCounter);
            }
        }
    }

    //Show rating information to user
    private void showRatingPage() {

        int ratingCounterDone = zUtils.getSharedPreferenceInt(MainActivity.this, "rating_counter_done", 0);
        int ratingCounter = zUtils.getSharedPreferenceInt(MainActivity.this, "rating_counter", 0);

        if (ratingCounterDone == 0) {
            ratingCounter = ratingCounter + 1;
            zUtils.insertSharedPreferenceInt(MainActivity.this, "rating_counter", ratingCounter);
            if ((ratingCounter % 7) == 0) {

                showRatingAlert(ratingCounter);
            }
        }
    }

    //Ger user feedback for rating
    private void showRatingAlert(final int ratingCounter) {

        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this, android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_rating, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("RATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                zUtils.insertSharedPreferenceInt(MainActivity.this, "rating_counter_done", 1);
                checkRating();

            }
        });


        dialogBuilder.setNegativeButton("LATER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        });


        android.app.AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

    }

    private void showSharingAlert(final int sharingCounter) {

        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this, android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_sharing, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("SHARE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                checkSharing();
                zUtils.insertSharedPreferenceInt(MainActivity.this, "sharing_counter_done", 1);

            }
        });


        dialogBuilder.setNegativeButton("LATER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        });


        android.app.AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

    }

    //Start a dialog when user decides to share the data
    private void checkSharing() {

        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Steam Achievement Tracker");
            String shareMessage = "\nLet me recommend you this application to help you track your achievement progress in your Steam games. Its pretty useful, Check it out.\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + "com.gamerguide.android.trackerforsteamachivements" + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
            //e.toString();
        }

    }

    //Help user navigate to the play store page when user decides to rate the app
    private void checkRating() {

        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.gamerguide.android.r6tabpro")));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.gamerguide.android.r6tabpro")));
        }

    }

    //Check firebase data for any recent update information and show that to user
    private void checkUpdateForApp() {
/*
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference listRef = storage.getReference().child("files/");

        listRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {

                        for (StorageReference item : listResult.getItems()) {

                            checkUpdateAndShow(String.valueOf(item.getName()).trim());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        zUtils.logError("JEEVA_FIREBASE: " + e.getMessage());
                    }
                });

                */

    }

    //Use current version number against data in Firebase to check if app is recent version
    private void checkUpdateAndShow(String version) {

        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);

            if (Integer.parseInt(version.trim()) > pi.versionCode) {

                showUpdateAlert();
            } else {
                //Toast.makeText(MainActivity.this,String.valueOf("No Update found.").trim(),Toast.LENGTH_SHORT).show();
            }

        } catch (Exception ex) {

        }
    }

    //Show update information to user that a new update is available
    private void showUpdateAlert() {

        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this, android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                checkUpdate();

            }
        });


        dialogBuilder.setNegativeButton("LATER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {


            }
        });


        android.app.AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    //When user decides to update the app, Take them to the play store
    private void checkUpdate() {

        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.gamerguide.android.trackerforsteamachivements")));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.gamerguide.android.trackerforsteamachivements")));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //When the user launches the app for the first time, Show them the settings page for initial setup help
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        showSettingsPanel();

    }

    //Open the settings panel and ask them information for app configuration
    private void showSettingsPanel() {

        final AlertDialog alertDialog;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.option_main_settings, null);
        //final TextView steam_api = dialogView.findViewById(R.id.steam_api);
        final TextView player_key = dialogView.findViewById(R.id.player_key);
        //steam_api.setText(zUtils.getSharedPreferenceString(MainActivity.this, "STEAM_KEY", ""));
        player_key.setText(zUtils.getSharedPreferenceString(MainActivity.this, "PLAYER_USERNAME", ""));
        Button save = dialogView.findViewById(R.id.save);
        Button report = dialogView.findViewById(R.id.report);
        TextView help = dialogView.findViewById(R.id.help);
        dialogBuilder.setView(dialogView);
        alertDialog = dialogBuilder.create();
        alertDialog.show();
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSteamIdFromWeb(alertDialog, String.valueOf(player_key.getText()).trim().toLowerCase(), MainActivity.this);
            }
        });


        //Start helper activity to give user more information on how to setup the app correctly
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(i);
            }
        });

        //Provide user a button to report any bugs in the application and for any queries from users
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, "kalaiselvamg1995@gmail.com");
                intent.putExtra(Intent.EXTRA_SUBJECT, "");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }

    //Get Steam ID from web, When the user provides their profile URL in settings page, Use the profile data to fetch their
    //Steam ID from a website.
    private void getSteamIdFromWeb(final AlertDialog alertDialog, final String username, final Activity activity) {

        Toast.makeText(activity, "Please wait..", Toast.LENGTH_LONG).show();

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, ZUtils.getSteamPlayerID(username),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        zUtils.logError("JEEVA_URL: " + ZUtils.getSteamPlayerID(username));

                        parseHTMLHidden(response, username, alertDialog, activity);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                zUtils.logError("JEEVA_HTMLERROR: " + error.getLocalizedMessage());
                Toast.makeText(activity, "Kindly enter valid profile URL or if your profile is private, Make it public.", Toast.LENGTH_LONG).show();
            }
        });

        queue.add(stringRequest);

    }

    //Parse the HTTP response from the Steam ID finder website
    private void parseHTMLHidden(String response, String username, AlertDialog dialog, Activity activity) {

        String steamID64 = "";

        Document doc = Jsoup.parse(response);
        try {


            Elements tr = doc.getElementsByTag("input");

            //Get the SteamID64 from helper website
            steamID64 = tr.get(5).val();

            zUtils.logError("JEEVA_ID: " + steamID64);


        } catch (Exception e) {
            e.printStackTrace();
            zUtils.logError("JEEVA_HTMLERROR: " + e.getMessage());
            Toast.makeText(this, "Error finding username..", Toast.LENGTH_SHORT).show();
        }

        //When userID64 is present, Store that information to local storage for later use.
        Toast.makeText(MainActivity.this, "Settings updated.", Toast.LENGTH_SHORT).show();
        zUtils.insertSharedPreferenceString(MainActivity.this, "PLAYER_ID", String.valueOf(steamID64));
        zUtils.insertSharedPreferenceString(MainActivity.this, "PLAYER_USERNAME", String.valueOf(username));
        zUtils = new ZUtils(MainActivity.this);
        dialog.hide();
        zUtils.insertSharedPreferenceBoolean(activity, "is_first_time", false);

        //Obtain games list from Steam API
        setupGamesFromSteam();
    }


    private void setupGamesFromSteam() {

        status.setText("Downloading data from Steam..");

        games = new ArrayList<>();

        //Create a HTTP request and get all games list for user from Steam API
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        zUtils.logError("GAME: " + zUtils.gamesOwnedSteamURL());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, zUtils.gamesOwnedSteamURL(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        status.setText("Fetching data for all owned games..");
                        zUtils.logError(zUtils.gamesOwnedSteamURL());
                        parseGamesFromSteamStorage(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                status.setText("Error Fetching from Steam, Make sure you setup app properly. Click Settings icon on top left and click green help icon..");
                zUtils.logError("Error: " + error.getLocalizedMessage());
            }
        });

        queue.add(stringRequest);
    }


    //Setup games data adapter for recycle view lising all games in user library
    private void setupAdapterForList() {


        if (games == null) {
            setupGamesFromSteam();
        }

        int sort = zUtils.getSharedPreferenceInt(MainActivity.this, "maingames_sort", 0);


        Collections.sort(games, new RecentComparator());

        ArrayList<Game> searchContainedGames = new ArrayList<>();
        for (Game game : games) {
            if (game.getName().toLowerCase().trim().contains(SEARCH_QUERY)) {
                searchContainedGames.add(game);
            }


        }

        //Based on user preference for view type, Show the games as list as columns or rows
        if (viewType == 0) {
            MainGamesAdapter mainGamesAdapter = new MainGamesAdapter(searchContainedGames, viewType);
            GridLayoutManager mainGamesManager = new GridLayoutManager(MainActivity.this, 2);
            gamesList.setLayoutManager(mainGamesManager);
            gamesList.setAdapter(mainGamesAdapter);
            setAlphaAnimation(gamesList);
        } else {
            MainGamesAdapter mainGamesAdapter = new MainGamesAdapter(searchContainedGames, viewType);
            GridLayoutManager mainGamesManager = new GridLayoutManager(MainActivity.this, 1);
            gamesList.setLayoutManager(mainGamesManager);
            gamesList.setAdapter(mainGamesAdapter);
            setAlphaAnimation(gamesList);
        }


    }

    //Make the image background blur using the blur library
    public void setupImageBlurBackground() {

        String id = zUtils.getSharedPreferenceString(MainActivity.this, "gamebackground_id", "1091500");

        Collections.sort(games, new RecentComparator());

        Picasso.get()
                .load(zUtils.getGameImageURL(id))
                .transform(new BlurTransformation(MainActivity.this))
                .into(background);

        setAlphaAnimation(background);

        setupAdapterForList();
    }


    //Setup the animation during the blur to provide a cool effect to user
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

    ///////////////////////////////////////////////////////////
    //ADAPTERS
    ///////////////////////////////////////////////////////////

    //This adapter provides data to the main recycleview which will show all the games in user library
    class MainGamesAdapter extends RecyclerView.Adapter<MyViewHolder1> {

        ArrayList<Game> games;
        int mainViewType;


        public MainGamesAdapter(ArrayList<Game> games, int mainViewType) {

            this.mainViewType = mainViewType;
            progressBar.setVisibility(View.GONE);
            status.setVisibility(View.GONE);
            search.setVisibility(View.VISIBLE);

            this.games = games;

            for (Game game : games) {

                ArrayList<Achievement> onlyUnlockedAchievements = new ArrayList<>();

                for (Achievement achievement : game.getAchievements()) {

                    if (achievement.getUnlocked().equalsIgnoreCase("1")) {
                        onlyUnlockedAchievements.add(achievement);
                    }
                }

                float completionPercentage = (((float) onlyUnlockedAchievements.size() / (float) game.getAchievements().size()) * 100f);
                game.setCompletion(completionPercentage);

            }

        }

        @Override
        public MyViewHolder1 onCreateViewHolder(ViewGroup parent, int viewType) {

            View v;

            v = (View) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_main_game_image, parent, false);

            MyViewHolder1 vh = new MyViewHolder1(v);
            return vh;
        }


        @Override
        public void onBindViewHolder(final MyViewHolder1 holder, final int position) {

            final ViewGroup frame = holder.mView.findViewById(R.id.frame);
            ImageView image = holder.mView.findViewById(R.id.image);
            TextView name = holder.mView.findViewById(R.id.name);
            name.setText(games.get(position).getName());

            Picasso.get().load(ZUtils.getGameImageURL(games.get(position).getId())).resize(460, 215).into(image);

            //CHECKING AND SETTING RECENT SIX ACHIEVEMENTS

            //When the game is clicked, Take them to the Achievements activity for the respective game
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    zUtils.insertSharedPreferenceString(MainActivity.this, "gamebackground_id", games.get(position).getId());
                    zUtils.insertSharedPreferenceLong(MainActivity.this, games.get(position).getId() + "_recent", System.currentTimeMillis());
                    games.get(position).setSearchTime(zUtils.getSharedPreferenceLong(MainActivity.this, games.get(position).getId() + "_recent", 0));
                    Intent i = new Intent(MainActivity.this, GameActivity.class);
                    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
                    String gameObjectJSON = gson.toJson(games.get(position));
                    i.putExtra("game", gameObjectJSON);
                    startActivity(i);
                }
            });


        }


        @Override
        public int getItemCount() {

            return games.size();
        }


    }


    class MyViewHolder1 extends RecyclerView.ViewHolder {

        public View mView;

        public MyViewHolder1(View v) {
            super(v);
            mView = v;
        }

    }

    ///////////////////////////////////////////////////////////
    //COMPARATORS
    ///////////////////////////////////////////////////////////

    //All the Comparators are helpers function to define model when different sorts are applied in settings

    public class RecentComparator implements Comparator<Game> {
        @Override
        public int compare(Game o1, Game o2) {
            return smaller(o1.getSearchTime(), o2.getSearchTime());
        }
    }

    public class PlaytimeComparator implements Comparator<Game> {
        @Override
        public int compare(Game o1, Game o2) {
            return smaller(Integer.parseInt(o1.getPlaytime()), Integer.parseInt(o2.getPlaytime()));
        }
    }
    public class UnlockTimeComparator implements Comparator<Achievement> {
        @Override
        public int compare(Achievement a1, Achievement a2) {
            return smaller(Long.parseLong(a1.getUnlockTime()), Long.parseLong(a2.getUnlockTime()));
        }
    }

    public class CompletionComparator implements Comparator<Game> {
        @Override
        public int compare(Game o1, Game o2) {
            return smaller(o1.getCompletion(), o2.getCompletion());
        }
    }


    public class NameComparator implements Comparator<Game> {
        @Override
        public int compare(Game o1, Game o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    private int smaller(int arg1, int arg2) {

        if (arg1 < arg2)
            return 1;
        else if (arg1 == arg2)
            return 0;
        else
            return -1;
    }

    private int smaller(float arg1, float arg2) {

        if (arg1 < arg2)
            return 1;
        else if (arg1 == arg2)
            return 0;
        else
            return -1;
    }


    ///////////////////////////////////////////////////////
    //ACTIVITY ACCESS HELPERS
    ///////////////////////////////////////////////////////


    public static Game getGameById(String gameId) {

        for (Game game : games) {
            if (game.getId().equalsIgnoreCase(gameId)) {
                return game;
            } else {
                return null;
            }
        }

        return null;
    }


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



    //Parse all games from local storage if any instead of getting information from Steam API
    private void parseGamesFromSteamStorage(String response) {

        try {

            TOTAL_GAMES = 0;
            OBTAINED_GAMES = 0;

            JSONObject obj = new JSONObject(response);

            JSONObject totalGames = obj.getJSONObject("response");
            JSONArray allGames = totalGames.getJSONArray("games");

            if (allGames != null) {

                for (int i = 0; i < allGames.length(); i++) {

                    JSONObject innerObj = new JSONObject(String.valueOf(allGames.get(i)));

                    status.setText("Saving owned game data..");
                    Game game = new Game(innerObj.getString("appid"),
                            innerObj.getString("playtime_forever").trim(),
                            innerObj.getString("name"),
                            zUtils.getSharedPreferenceLong(MainActivity.this, innerObj.getString("appid") + "_recent", 0));

                    games.add(game);
                    zUtils.logError("\nJEEVA_GAMES: Game { " + " Name: " + game.getName() +
                            " Id: " + game.getId() +
                            " Playtime: " + game.getPlaytime() +
                            " }");
                    zUtils.logError("\nJEEVA_GAMES_ID: Game { " + game.getId() +
                            " }");

                    TOTAL_GAMES += 1;

                }



                zUtils.logError("\nJEEVA_TOTAL_GAMES: " + TOTAL_GAMES);
                getAllAchievementsAndData();



            }

        } catch (JSONException e) {
            zUtils.logError("JEEVA_ERROR: " + e.getMessage());
        }

        viewType = zUtils.getSharedPreferenceInt(MainActivity.this, "maingames_view", 0);

        setupImageBlurBackground();
    }

    //GETTING ALL ACHIEVEMENTS

    private void getAllAchievementsAndData() {

        POSITION_POINTER = 0;
        //getGamesAndAssociatedData();


    }

    private void getGamesAndAssociatedData() {

        for (Game game: games){

            getGameSchema(game);
        }
    }



    //Get all game information for particular game from Steam API
    private void getGameSchema(final Game game) {

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

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


    //Parse particular game information received from Steam API requested for a game using its gameID
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
                        achievement.setDesc(zUtils.getSharedPreferenceString(MainActivity.this, game.getId()
                                + achievement.getId(), "Hidden Achievement"));
                    }

                    game.addAchievement(achievement);
                    zUtils.logError("JEEVA_INSERT:" + game.getName() + " { " + achievement.getName() + " " + achievement.getIcon() + " " + achievement.getUnlocked());

                }
                setupGamesInList(game);
            }


        } catch (JSONException e) {

            OBTAINED_GAMES += 1;


        }

    }


    private void setupGamesInList(Game game) {


        for (Achievement achievement: game.getAchievements()){

            zUtils.logAchievement(achievement,"BEFORE GETTING ANYTHING");
        }

        getAllPlayerAchievement(game);

    }

    //For a game, Request STEAM API for all player achievement data for that particular game.
    private void getAllPlayerAchievement(final Game game) {

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

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

    //Parse a game achivement data returned from Steam API requested for a gameID and userID
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


                //UNLOCKED_OR_NOT_STATUS_GAMES_SET += 1;

                for (Achievement achievement: game.getAchievements()){

                    zUtils.logAchievement(achievement , "AFTER GETTING UNLOCKED");
                }

                setupDownloadForAchievementPercentage(game);



            }

        } catch (JSONException e) {

        }


    }

    //For a gameID, Request Steam API for all information that is globally available, Unlock percentages global information from Steam Database
    private void setupDownloadForAchievementPercentage(final Game game) {

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
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

    //Parse all global achievement percentage for a requested gameID from Steam.
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
                            //ACHIEVEMENT_PERCENTAGE_DOWNLOADED++;
                        }
                    }

                }


                for (Achievement achievement: game.getAchievements()){

                    zUtils.logAchievement(achievement,"AFTER GETTING PERCENTAGE");
                }

            }

            zUtils.logError("GAME_OBTAINED: " + OBTAINED_GAMES++);



        } catch (JSONException e) {

        }
    }
}
