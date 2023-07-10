package com.example.ulkelerinyemektarifleriapiuygulamasi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> mealsList;
    private ArrayAdapter<String> adapter;
    private String selectedCountry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.listele);
        mealsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mealsList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedMeal = mealsList.get(position);
                showMealDetails(selectedMeal);
            }
        });

        showCountrySelectionDialog();
    }

    private void showCountrySelectionDialog() {
        final String[] countries = {"Turkish", "Italian","american","american","dutch","Moroccan"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ülke Seçimi")
                .setItems(countries, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCountry = countries[which];
                        fetchMeals();
                    }
                })
                .show();
    }

    private void fetchMeals() {
        String apiUrl = "https://www.themealdb.com/api/json/v1/1/filter.php?a=" + selectedCountry.toLowerCase();
        FetchMealsTask fetchMealsTask = new FetchMealsTask();
        fetchMealsTask.execute(apiUrl);
    }

    private class FetchMealsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String apiUrl = params[0];
            StringBuilder response = new StringBuilder();

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();
                } else {
                    Log.e("API Request", "API isteği başarısız oldu. Response Code: " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response.toString();
        }

        @Override
        protected void onPostExecute(String response) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray mealsArray = jsonObject.getJSONArray("meals");

                mealsList.clear();

                for (int i = 0; i < mealsArray.length(); i++) {
                    JSONObject mealObject = mealsArray.getJSONObject(i);
                    String mealName = mealObject.getString("strMeal");
                    mealsList.add(mealName);
                }

                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showMealDetails(String mealName) {
        String apiUrl = "https://www.themealdb.com/api/json/v1/1/search.php?s=" + mealName;

        FetchMealDetailsTask fetchMealDetailsTask = new FetchMealDetailsTask();
        fetchMealDetailsTask.execute(apiUrl);
    }

    private class FetchMealDetailsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String apiUrl = params[0];
            StringBuilder response = new StringBuilder();

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();
                } else {
                    Log.e("API Request", "API isteği başarısız oldu. Response Code: " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response.toString();
        }

        @Override
        protected void onPostExecute(String response) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray mealsArray = jsonObject.getJSONArray("meals");

                if (mealsArray != null && mealsArray.length() > 0) {
                    JSONObject mealObject = mealsArray.getJSONObject(0);
                    String mealInstructions = mealObject.getString("strInstructions");

                    showMealInstructions(mealInstructions);
                } else {
                    showErrorMessage();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showErrorMessage();
            }
        }
    }

    private void showMealInstructions(String instructions) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Yemek Tarifi")
                .setMessage(instructions)
                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showErrorMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hata")
                .setMessage("Yemek tarifi bulunamadı.")
                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}