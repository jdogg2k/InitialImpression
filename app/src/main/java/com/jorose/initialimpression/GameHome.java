package com.jorose.initialimpression;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
                        personTrue = true;
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
                adapter.add(newGuess);
                uText.setText("");


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

        return initials;
    }
}
