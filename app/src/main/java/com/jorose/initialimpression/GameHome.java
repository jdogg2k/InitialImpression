package com.jorose.initialimpression;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class GameHome extends Activity {

    public static Properties properties = new Properties();
    PersonSearch pSearch;
    boolean personTrue = false;
    char initial1;
    char initial2;
    private ListView list;
    private ArrayList<Guess> arrayOfGuesses;
    private GuessesAdapter adapter;
    private MediaPlayer clockSound;
    private MediaPlayer correctSound;
    private MediaPlayer wrongSound;
    private MediaPlayer endSound;
    private MediaPlayer highSound;
    private int scoreNum;
    private int curScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_home);

        arrayOfGuesses = new ArrayList<Guess>();
        // Create the adapter to convert the array to views
        adapter = new GuessesAdapter(this, arrayOfGuesses);

        list=(ListView)this.findViewById(R.id.mainListView);
        list.setAdapter(adapter);

        Button search = (Button) findViewById(R.id.searchButton);
        search.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EditText uText = (EditText) findViewById(R.id.searchName);
                pSearch = new PersonSearch();
                pSearch.execute(uText.getText().toString());
            }
        });

        Button generator = (Button) findViewById(R.id.initialGenerator);
        generator.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                adapter.clear();
                EditText uInput = (EditText) findViewById(R.id.searchName);
                uInput.setText("");
                TextView uText = (TextView) findViewById(R.id.textInitials);
                uText.setText(getRandomLetters());
            }
        });

        clockSound = MediaPlayer.create(this, R.raw.ticktock);
        clockSound.setLooping(true);

        correctSound = MediaPlayer.create(this, R.raw.right);
        wrongSound = MediaPlayer.create(this, R.raw.wrong);
        endSound = MediaPlayer.create(this, R.raw.end);
        highSound = MediaPlayer.create(this, R.raw.triumph);

        //get high score
        //getting preferences
        SharedPreferences prefs = this.getSharedPreferences("highScore", Context.MODE_PRIVATE);
        scoreNum = prefs.getInt("score", 0); //0 is the default value
        TextView hText = (TextView) findViewById(R.id.highScore);
        hText.setText("Current High Score: " + String.valueOf(scoreNum));

    }

    private class PersonSearch extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub

            try {
                //check the proper initials

                String[] names = params[0].split("\\s+");

                if ((names[0].toString().charAt(0) == initial1) && (names[1].toString().charAt(0) == initial2)) {

                    //properties.load(new FileInputStream("freebase.properties"));
                    HttpTransport httpTransport = new NetHttpTransport();
                    HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
                    JSONParser parser = new JSONParser();
                    GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/search");
                    url.put("query", params[0]);
                    url.put("filter", "(all type:/people/person)");
                    url.put("limit", "3");
                    url.put("indent", "true");
                    //url.put("key", properties.get("API_KEY"));
                    //url.put("key", "AIzaSyAM-kTxjXEgfk_bze6jGZ5V93LVWknUFac");
                    HttpRequest request = requestFactory.buildGetRequest(url);
                    HttpResponse httpResponse = request.execute();
                    JSONObject response = (JSONObject) parser.parse(httpResponse.parseAsString());
                    JSONArray results = (JSONArray) response.get("result");

                    if (results.size() > 0) {
                        personTrue = false;
                        for (int j = 0; j < results.size(); j++) {
                            JSONObject obj = (JSONObject) results.get(j);
                            String name = (String) obj.get("name");
                            if (name.equals(params[0])) {
                                personTrue = true;
                            }
                        }
                    } else {
                        personTrue = false;
                    }
                } else {
                    personTrue = false;
                }

                //for (Object result : results) {
                    //resultText = resultText + JsonPath.read(result,"$.name").toString();
                //}
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return "done";
        }

        protected void onPostExecute(String Result){
            try{

                //Toast.makeText(getApplicationContext(), "R E S U L T :"+jsonResult, Toast.LENGTH_LONG).show();

                //System.out.println(resultText);
                //showing result

                EditText uText = (EditText) findViewById(R.id.searchName);
                Guess newGuess = new Guess(uText.getText().toString(), personTrue);
                adapter.insert(newGuess, 0);
                uText.setText("");

                if (personTrue) {
                    correctSound.start();
                    EditText scoreText = (EditText) findViewById(R.id.scoreText);
                    curScore = Integer.parseInt(scoreText.getText().toString());
                    curScore ++;
                    scoreText.setText(String.valueOf(curScore));
                } else {
                    wrongSound.start();
                }




            }catch(Exception E){
                Toast.makeText(getApplicationContext(), "Error:"+E.getMessage(), Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.game_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String formatTime(long millis){

        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

    }

    private void checkHighScore() {
        TextView hText = (TextView) findViewById(R.id.highScore);
        String prefix = "Current ";
        if (curScore > scoreNum){
            SharedPreferences prefs = this.getSharedPreferences("highScore", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("score", curScore);
            editor.commit();
            scoreNum = curScore;
            prefix = "NEW!!! ";
            highSound.start();
        }
        hText.setText(prefix + "High Score: " + String.valueOf(scoreNum));
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private String getRandomLetters() {
        Random rnd = new Random();
        int numLetters = 2;

        String randomLetters = "ABCDEFGHIJKLMNOPRSTWY";
        String initials = "";

        for (int n=0; n<numLetters; n++) {
            char newChar = randomLetters.charAt(rnd.nextInt(randomLetters.length()));
            initials += newChar + ". ";
            if (n == 0){
                initial1 = newChar;
            } else {
                initial2 = newChar;
            }
        }

        EditText scoreText = (EditText) findViewById(R.id.scoreText);
        scoreText.setText("0");


        Button search = (Button) findViewById(R.id.searchButton);
        search.setEnabled(true);

        clockSound.start();

        new CountDownTimer(90000, 1000) {

            TextView tText = (TextView) findViewById(R.id.timeBox);

            public void onTick(long millisUntilFinished) {

                tText.setText(formatTime(millisUntilFinished));
            }

            public void onFinish() {
                tText.setText("Time's Up!");
                Button search = (Button) findViewById(R.id.searchButton);
                search.setEnabled(false);
                clockSound.pause();
                clockSound.seekTo(0);
                endSound.start();
                hideKeyboard();

                //check score
                checkHighScore();
            }
        }.start();

        curScore = 0;
        return initials;
    }
}
