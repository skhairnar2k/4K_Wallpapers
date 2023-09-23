package com.siddhesh.a4kwallpapers;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class MainActivity extends AppCompatActivity implements CategoryRVAdapter.CategoryClickInterface{

    private EditText searchEdit;
    private ImageView searchIV;
    private RecyclerView categoryRV, wallpaperRV;
    private ProgressBar loadingPB;
    private ArrayList<String> wallpaperArrayList;
    private ArrayList<CategoryRVModel> categoryRVModelArrayList;
    private CategoryRVAdapter categoryRVAdapter;
    private WallpaperRVAdapter wallpaperRVAdapter;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    public ImageButton gameBtn,quizBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        InterstitialAd.load(this,"ca-app-pub-1373457972994236/6416329647", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });

        gameBtn = findViewById(R.id.idGameBtn);
        quizBtn = findViewById(R.id.idQuizBtn);

        searchEdit = findViewById(R.id.idEditSearch);
        searchIV = findViewById(R.id.idIVSearch);
        categoryRV = findViewById(R.id.idRVCategory);
        wallpaperRV = findViewById(R.id.idRVWallpapers);
        loadingPB = findViewById(R.id.idPBLoading);
        wallpaperArrayList = new ArrayList<>();
        categoryRVModelArrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this,RecyclerView.HORIZONTAL,false);
        categoryRV.setLayoutManager(linearLayoutManager);
        categoryRVAdapter = new CategoryRVAdapter(categoryRVModelArrayList,this,this::onCategoryClick);
        categoryRV.setAdapter(categoryRVAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);
        wallpaperRV.setLayoutManager((gridLayoutManager));
        wallpaperRVAdapter = new WallpaperRVAdapter(wallpaperArrayList,this);
        wallpaperRV.setAdapter(wallpaperRVAdapter);

        getCategories();
        getWallpapers();

        searchIV.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(MainActivity.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }

                String searchStr = searchEdit.getText().toString();
                if(searchStr.isEmpty()){
                    Toast.makeText(MainActivity.this,"Please enter Search Query",Toast.LENGTH_SHORT).show();
                }  else {
                    getWallpapersByCategory(searchStr);
                }
            }
        });
        gameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(MainActivity.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }
                String url = "https://7559.play.gamezop.com/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        quizBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(MainActivity.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }
                String url = "https://7560.play.quizzop.com/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }

    private void getWallpapersByCategory(String category){

        wallpaperArrayList.clear();
        loadingPB.setVisibility(View.VISIBLE);
        String url = "https://api.pexels.com/v1/search?query="+category+"&per_page=3000&page=1";
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONArray photoArray = null;
                loadingPB.setVisibility(View.GONE);
                try {
                    photoArray = response.getJSONArray("photos");
                    for(int i=0; i<photoArray.length(); i++){
                        JSONObject photoObj = photoArray.getJSONObject(i);
                        String imgUrl = photoObj.getJSONObject("src").getString("portrait");
                        wallpaperArrayList.add(imgUrl);
                    }
                    wallpaperRVAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Fail to load Wallpapers", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String>headers = new HashMap<>();
                headers.put("Authorization","RvozWyS4kt2GofJ8zyGd01Ol0vsXcD3xxXOb6rUSscTJLwsUxGHDteoN");
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

private void getWallpapers(){
        wallpaperArrayList.clear();
        loadingPB.setVisibility(View.VISIBLE);
        String url = "https://api.pexels.com/v1/curated?per_page=3000&page=1";
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                try {
                    JSONArray photoArray = response.getJSONArray("photos");
                    for (int i=0; i<photoArray.length();i++){
                        JSONObject photoObj = photoArray.getJSONObject(i);
                        String imgUrl = photoObj.getJSONObject("src").getString("portrait");
                        wallpaperArrayList.add(imgUrl);
                    }
                    wallpaperRVAdapter.notifyDataSetChanged();
                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Fail to load wallpapers...",Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> headers = new HashMap<>();
                headers.put("Authorization","RvozWyS4kt2GofJ8zyGd01Ol0vsXcD3xxXOb6rUSscTJLwsUxGHDteoN");
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }
private void getCategories(){
    categoryRVModelArrayList.add(new CategoryRVModel("Technology","https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?ixid=MnwxMjA3fDB8MHxzZWFyY2h8MTJ8fHRlY2hub2xvZ3l8ZW58MHx8MHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60\n"));
    categoryRVModelArrayList.add(new CategoryRVModel("Programming","https://images.unsplash.com/photo-1587620962725-abab7fe55159?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8cHJvZ3JhbW1pbmd8ZW58MHx8MHx8fDA%3D&w=1000&q=80"));
    categoryRVModelArrayList.add(new CategoryRVModel("Nature","data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBxAQDxUPEhAVFRUPFQ0VDxUQFRUVEBUPFRUWFhUVFRUYHSggGBolGxUVITEhJSkrLi4uFx8zODMsNygtLisBCgoKDg0OFxAQGi0dHh0tLS0tLS0rLSstLS0tLS0tLTUtNy0tKy0tKy0tLSstLS0tLS8tLS0tLS0uLSstLS0tK//AABEIAR4AsAMBIgACEQEDEQH/xAAbAAACAwEBAQAAAAAAAAAAAAAAAgEDBAUGB//EAEEQAAEDAQMHCAgFAwQDAAAAAAEAAhEDBCFRBRIxQWGBkQYTQlJxobHBFBUiMmLR4fAHU3KS8UOCohYjstIkM2P/xAAaAQABBQEAAAAAAAAAAAAAAAAAAQIDBAUG/8QALREAAgIBAgUDAwQDAQAAAAAAAAECEQMEEgUTITFBFCJRUmFxMoGRoRVC8Ab/2gAMAwEAAhEDEQA/APl0IhTCIWsZZEIhTCIQBEIhTCIRQCwiE0IhKAsKYUwiEUBEIhTCIQBEKITQiEARCiE0IhAEQiFMIhAEQohNCIQAsKYUwiEAMoTQiEtCWQoTQiEUFkKE0IhFBZChNCIRQWKhNCIRQWQhTCIRQWQhTCIRQWQhTCIRQWQhTCIRQWQhTCIRQWQoTQiEUFjQiFKIThpEIhTCIQBEIhTCEBZEIhShAEQiFMIhAEQiFKEARCIUwhAEQiFKEARCIUohAEQiFMIhAWRCIUwiEAMhTCmEtCCoTIRQCoTQhACohShAEQiFKEARCIUoQBCEyhAEITQiEALCE0IQAqEyhAEQiE0IhADQoTwohKJYqE8IhAWIiE0IAQFiqCE+ajNSBYqiE+ajNQFiQiFZCIQFiQgBPCAECWJCE5CIShYiE8IhAWIhNCmEC2JCkhTCIQA0IhOiEo2xYRCaEQgSxYRmp4RCBLFzUZqeEZqAsSFOanhTCAsqhEK2EQkEsqhEK2FEJRbK4RCeEQgLEhQQnIRCBbEhEJoRCAEzUQnhEJaAcBEJ4RCBliQiE8IzUAKgBOGpg2UNiWVwnZSJ0Bei5P8AJStaYdGYzW52jcNZX0LJPJuzWYDNYHOHSeATOwaAsfW8Yw6d7V7pfCNHTcOy5er9qPmuTuS1qrwW0iAek64cSu/Zfw8effqtGwAkr6GoXPZuPamf6aijWx8LwR7qzxrPw9o66ztzR80P/D2jqrO3tB817JEKp/ldZ9bLHosH0o+f2n8PHdCs07HAt+a4GUeSlqoiTSJA1t9ody+voVrDx3VQfuqSIMnDMEuyr8HwV9IjSIVcL7TlXk9ZrSDnsAcemwQ7fjvXz/lByRq2aXt9un1m6v1DUuh0XGcOoai/bL7mTqOG5MSuPuR5WEQrXMi6EpC2bM6yuEQnLUQgLEhEJ4UZqAsshRCshRCBliwiE6AEBZDWTcvdckeSOcBXrj2bixmt204DxVHIfk6Kp9Iqj2Gn2Qek4eQX0Vc1xfibjeHE+vlm9w3QJpZcn7IhrQAABAFwAuAGxShC5VxbN0lEKEJNoEoUIRtAlChCNoEqCJuOvT2KFKVRA8Pys5IiDXs7dEl7BhrLdmxfP3sgwV94XgOXXJ0N/wDJpD2Sf9wDQ1x19hXUcI4nJtYcr/DMPiWgVc3GvyeEhQQrCFELpzAsSEQnhEICyzNUZqshCBlleauhkXJzrRXbSb0iJOA1ngsRXvvw5yfDH2gi8+wzs0u8lU12fk4ZS8+C5ocHOzKPjuz19ms7aTG02CGsAA+8VYhQuHlFt2zskklSJUKM5KXJuwdRYhVZ6jPSbAoulEqgvTMejaLRahQCplLsGgpUKUbABJWpNe0scJa4EOGIKdCdGLTsR9T49yhyWbNXdSOgGWnFpvBXLhfSPxDyeH0W1wL6ZzXfpOjvnivnULttBqOdhUn38nHa/ByczS7PqhIRCcBTCulKxA9OHrKKgTc6E3cLtNQvK+u8n7OKVkpM+EE9rr/NfHKD5cBtC+uttBDQBqDRwELF4xK4xidBwLDbnL8HWlRK5rbQ5WNrrA2nRctmtxVbknPqp9cJNoqiWOeq3VCqnWgJedBRsHJDOqlSyo5KHN1lWNqsCTYKXUyVe0lZvSmqPS24pdgxo15+1KawxWN9qZiqHWxmKXYGw6Ta4xVram1cgWpuoqxtc6ijaLyzXleiKtnqU+sx0doEjwXx14gkL62yq43Yr5DbM4VHDAu8Vu8HlSlE5zjuGlCX5GhEKkZytaFu2c21Rx+dKYVSt3qoqRk0hQ0yxzIGOi85wv1he+9McL89eRp2G9fSLDkuk6kxxAOc1p0a4WbxCP6WzoeAzUt6X2OL61cOl3oZl0/cL0JyNR1tHAJDkWjgsyofB0VM5TMvDXO9M7LrMQuh6lo4BN6oo9UcEm2AUzjvy0zW4d6QZdpjpjvXRrZAonQBwWGryZGot4JyhAa1IX/UdPrBI/lA09IBV1OTDtRCVnJqpsTuXjG+8b1xPSUjKE9LvVrOTrhgm/0877KTZAd7ioWxvXC00rU3rIpcnMStlPILRpJTXGI5WNZ7WztXSo2ieistLJjW6FsY0t2Jjgh1Gqm74V8nyi6azzHSf4lfTLRbWsa5xcPZDjpwC+XV7S0uJkXrU4ZDa5M5v/0L9sIihOGlUG1tGtKbcNq17OX2Sfg7JCRwVhKQlR2OeMphe05N2zOoBs30yRuN4XjSuhkW283Uibn3Ht1KDU4+ZCvg1uDZVg1CvtLoe0NZIa65zrSqjaVlrEdxR1DX2pTXXKNp2pTaUvJCjqOtEKBahiuQbV2pTa9iXkiUjs+kbVPpC4vpaV1qdq8EckKR3PSVBtwGK8xVttWYDv8AEfNLztU6ah/aE70420ekdldoVQy4JuB3ArzNUVPzP8VnfWrt0O4R805adDJZK8HshlIu1P4fVJUGfpJHa0rx3rWuNNU7h9VdTyraDoe7h9UemoZ6iPwdLLtnDaR9t0uuAiLta8k6y9q6FvttoqGC6c3HHWs7TV6sq/gx7I9TkeLajnZ/b2XQyeiHBKbIequiDU6gUzUw7lP0My5Gk1goz1EjBTDcE20TpWI56qL3fcLRzTOqEzbMzqhI5okjibfQto5RqERIkYlMba/FveinZKfVHBbKNipnohV5SgvBvYM+ocUrMXprsR3qPTDiu3RyZRP9McFrp5HoflN4KCWpxx8F2PqH5R5n03ao9MXr25Fs/wCSzgrBkOzfks4KJ67EvDH7NR8o8Z6btQLbtXthkGzfks/aEeorL+SzgEnr8Xww2an6keKFs2qRbV7F2RLN+SzgFW7I9n/JZwCVa7F8MNmo+UeVFtU+ljAL0FTJdAf0mcFjrWGkP6beCkjqcb8MbL1C8o5htLdQbvAWa0Wp4HsxfhIW6rZmDohYqtFvVCsQnFlHPlz7WrSOYec0yUprVR9hbXUm4BUvYMFYU0zBnia8lTLZUGkhaWWuekOH1WfMbgEwhOtEVNFocPv+EwePv+FkawdXuanDB95vyTGLE2NermOWFrRj3N/6q1jtvh8lHIt4zo03LdQK5lKpslbKNXYRxKqZGbGnR16BW+kVyaFcbf2n5LdSqjEeCzcrNfGjotcrWuWNlQYq5pOHCFSnIm2mrOUOcqM44HuUl5wUe8No7lRUKC8/Y+qpqPT4zF2lNdy59daqxWGsSrmKRFkRjrFYapWusYWKq5aeJmRqUZ3qh5VlQrPUVyJjZRXFQSkckJ7VKinIp50dUb/5Vjag2d48FDTQxPerWto48fqkY6PcZlW/3hvv8lfTq/EP8klPmes3eYV7MzU5u6/zUUi3jNFF3xNOyBK3URsHeFhY5vWbxHgVsovAFz234QR4qnlRr6eRspN+HhK2U3XaY7Sblio1RPv8G/MLo0a7dZ8FnZjXxFtInrN3QStDWk9IjcQswqN63n4BMbQwdIbvkqE+5YNgpP1P8SiHdadx+awC3xobO9p7gZU+sD1Y7Qo9rFNbnvw70j6j+r4LKbY44AbVmrW3/wCh/tv8FJGLYnRGi0OcfouXaHN0E370tavJ94/3B/zWeqR28YV7DFruV8sk10Fqvb9ysznj+U9Q6szuPkszy7CN31WnjMjOLUjFZahCarU2E+CzOqn7CtxRkZWM4pS44ql1Y48AkLjtUyKUjNmH+UCidisGacfJOKY1JGSIrbZz1o7FcyzbZ7D5SnaRoI4EKzNb9z5KNksSGWc6p4geJW+nzrR7kxrJA75VFmq02n2r/FdWzOpOFznj9II8Aq2aVGppcd+aM9O11cI2FodwIK20rSXASXt/Zm92jerqdja67Pqby68b1dTye0dJxjGo4eAWfklFmtihNebM0vF+kfG4Ce4eK1U6dMiYj9JzxxzlspWQAe8+/FxcOJV4YwY8XFUMmReC9CHyc51nwc47ObMfe9NzdUaIHa35uhdGTi4bj5GVU6mw6dfW19gcouZ8j9pzKpB994dGpjCeMOMcFRzId7tMR+oZ3foXWfSoi8l37oHAFZqltoN1uP6QTG9oViE34RFKC8sz+jkX5hH90+NyqdZSTJu2ucPIhO/KAJOa0ja4xHEhY6xDry89jTPCblZx7vPQr5HDx1Grmm27PBIwa4+aw1K+BP7Y+avqzFwcBrznNHgqC58acdR8Qr+NUjMzu2ZX52J4KpxxPGVdUq6r9eiR3rOajMT4q5FmRliKag2bkhf2JzmnWd8hK6kMVMinJECsTcXO3CPNSDfdd2uWEO2wpzx1uB+SayRI6P8AuY9w81Y0g3Egk7r+K5jXD+Vrp5p1xiAD4Qo2T42jo02P0BjuJ0dsJ2NtPRzh+0/8isbA1uis0cR3K1tto9NwdPVLmkbgFXnf5NHFXnp+5eBawbiWnFzmNH7fqm5y1R/7gY2geKWjXspiHDD23kXdpHzWg0HASytAwFcxGy6FWlL5X9FyEfKd/uOw2mA7nSZ0jOjzv3LVSqV9daoBsu7yTPFc30hzfeqO2EVQf8gVoYWvvIOr2i97m91/eq04/wDUWoTX3/k6dB1Q6LTVdGq4jjmjxWoUjpcHEa/bdncBd3ri1bHV0jMI1Ec54l0LO+ztB9qo9hMQWy1vFpPgq/LT8k/McV1j/Z6MU6fUeOwk8UOdTAvcR23HuXm84gQ205w1y5/jHig+0M3nCRhJDd2KfHT/ACxHqPsegqupHpm74i0br1neacey7i5x81wPVtNxMPDiNIBB44dyofksE6TAwaI4hWYYYryVcmon9J2qtJpvmdsHzJWGpRHxbb4++K5jsnOF4J2RM8B9FDLLW/MffqCuwgl5M7Lkk/8AU3OpR1h3nxWd7XG4Tv8AsrHVqvaYJ0YuMzxVFSqTrKsxRn5JGwtxI8PAqh9X4lmzhqgnb/KUl2CkXQryVlRhLnHUFDRN8qxrdiYSdhJdq7kHOmCTxTTNw71JouxSCoto1I0t3kT3hbmW5uiXf2xG4OXMNGBOntUh3wjcAmSRLCbR2mWhr/6w7C1rTxAjxV1mj3c2m/DOYCRsBhcB2bfdEaSAJ3ShhA0AXYj6qGWOy1DUNO6PRVKJIuzW7Aw+QIPFZqmSp9ptTbIu8hCqsPPPuD2gHVmMj/itR5xpzQ4NiCSwC/uCgalHyW04TVtGelQqgzTrunXmVASO1utaPSbTT/rZ06jLT3tSWlr4BLyZ0EQHd4McVzn2TOGdJMk++ZSqKl3r+BkpOH6b/k6PrSrrY06ffqOJ7yRwhZ61pcRJc5uwXA9hzDPFYH2P2c5zoA1NE+MKkMLbw4jAg+SkUIrsQyzZH3NRtBOl7tmgjgAFa3KDh0if3RwMhZTWqAgEgzx3mFOa117tugDVwhSUiHe12ZqqZRc/pHYGuzT3ATvVZrk6Xv7M8uKwODNQO9QC4fU/RPVIjcm31NwztRgbRKnm36SJ2wsXPnAcE7XzpnipEyCSL3fpduQ1wxdvgqn0mBcEG1GNCcmiNxZ//9k="));
    categoryRVModelArrayList.add(new CategoryRVModel("Travel","https://wallpapers.com/images/hd/compass-travel-hand-focus-sucrs3gyylhk9xi6.jpg"));
    categoryRVModelArrayList.add(new CategoryRVModel("Architecture","https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTVB4RWMHMI1c6HL8wIcmqZaIsArN13Z9GPKA&usqp=CAU"));
    categoryRVModelArrayList.add(new CategoryRVModel("Arts","data:image/jpeg;base64,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"));
    categoryRVModelArrayList.add(new CategoryRVModel("Music","https://cdn.wallpapersafari.com/25/21/3znvB5.jpg"));
    categoryRVModelArrayList.add(new CategoryRVModel("Abstract","https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR0GWYW3gYZe8EJYHALGFIEoakvcosn-6_SmA&usqp=CAU"));
    categoryRVModelArrayList.add(new CategoryRVModel("Cars","https://cdn.wallpapersafari.com/36/37/O5cokz.jpg"));
    categoryRVModelArrayList.add(new CategoryRVModel("Flowers","https://images.pexels.com/photos/56866/garden-rose-red-pink-56866.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500"));
    categoryRVAdapter.notifyDataSetChanged();
}
    @Override
    public void onCategoryClick(int position) {
        String category = categoryRVModelArrayList.get(position).getCategory();
        getWallpapersByCategory(category);
        
    }
}