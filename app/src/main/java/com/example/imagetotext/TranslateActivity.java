package com.example.imagetotext;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;

public class TranslateActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextView mSourceLang;
    private EditText mSourceText;
    private Button mDetectLanguageBtn;
    private TextView mTranslatedText;
    private String sourceText;
    private Button mTranslateBtn;

    private ArrayList<CountryItem> mCountryList;
    private CountryAdapter mAdapter;
    private String clickedTlLanguageNameInto;
    private String clickedTlLanguageNameFrom;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.translate_main);
        mSourceLang = findViewById(R.id.sourceLang);
        mSourceText = findViewById(R.id.sourceText);
        mDetectLanguageBtn = findViewById(R.id.detect_language);
        mTranslatedText = findViewById(R.id.translatedText);
        mTranslateBtn = findViewById(R.id.translate);

        initList();
        Spinner spinnerLanguageTlInto = findViewById(R.id.spinnerTlInto);
        Spinner spinnerLanguageTlFrom = findViewById(R.id.spinnerTlFrom);

        mAdapter = new CountryAdapter(this,mCountryList);
        spinnerLanguageTlInto.setAdapter(mAdapter);
        spinnerLanguageTlFrom.setAdapter(mAdapter);
        clickedTlLanguageNameInto = "en";

        spinnerLanguageTlInto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CountryItem clickedItem = (CountryItem) parent.getItemAtPosition(position);
                clickedTlLanguageNameInto = clickedItem.getCountryName();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerLanguageTlFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CountryItem clickedItem = (CountryItem) parent.getItemAtPosition(position);
                clickedTlLanguageNameFrom = clickedItem.getCountryName();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        Intent intent = getIntent();
        final String text = intent.getStringExtra("EXTRA_TEXT");
        mSourceText.setText(text);

        mDetectLanguageBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                identifyLanguage(text);
            }
        });
        mTranslateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                identifyLanguage("");
                int langCodeFrom;
                int langCodeInto;
                langCodeFrom = getLanguageCode(clickedTlLanguageNameFrom);
                langCodeInto = getLanguageCode(clickedTlLanguageNameInto);
                translateText(langCodeFrom, langCodeInto);
            }
        });


        drawerLayout = findViewById(R.id.translatorDrawerLayout);
        navigationView = findViewById(R.id.translatorNavView);

        toolbar = (Toolbar) findViewById(R.id.translatorToolbar);
        setSupportActionBar(toolbar);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initList() {
        mCountryList = new ArrayList<>();
        mCountryList.add(new CountryItem("en", R.drawable.flag_united_kingdom));
        mCountryList.add(new CountryItem("ru", R.drawable.flag_russian));
        mCountryList.add(new CountryItem("ja", R.drawable.flag_japan));
        mCountryList.add(new CountryItem("lt", R.drawable.flag_lithuania));
    }

    private void identifyLanguage(final String text) {

        sourceText = mSourceText.getText().toString();
        FirebaseLanguageIdentification identifier = FirebaseNaturalLanguage.getInstance().getLanguageIdentification();

        mSourceLang.setText("Detecting...");

        identifier.identifyLanguage(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                if(text!="") {
                    if (s.equals("und")) {
                        Toast.makeText(getApplicationContext(), "Language not identified", Toast.LENGTH_SHORT).show();
                    } else {
                        int langCodeFrom;
                        int langCodeInto;
                        langCodeInto = getLanguageCode(clickedTlLanguageNameInto);
                        langCodeFrom = getLanguageCode(s);
                        translateText(langCodeFrom, langCodeInto);
                    }
                }
            }
        });
    }

    private int getLanguageCode(String language) {
        int langCode;
        switch (language){
            case "lt":
                langCode = FirebaseTranslateLanguage.LT;
                mSourceLang.setText("Lithuanian");
                break;
            case "en":
                langCode = FirebaseTranslateLanguage.EN;
                mSourceLang.setText("English");
                break;
            case "ru":
                langCode = FirebaseTranslateLanguage.RU;
                mSourceLang.setText("Russian");
                break;
            case "ja":
                langCode = FirebaseTranslateLanguage.JA;
                mSourceLang.setText("Japanese");
                break;
            default:
                langCode = 0;
        }
        return langCode;
    }

    private void translateText(int langCodeFrom, int langCodeInto) {
        mTranslatedText.setText("Translating...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder().setSourceLanguage(langCodeFrom)
                .setTargetLanguage(langCodeInto).build();
        final FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                translator.translate(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        mTranslatedText.setText(s);
                    }
                });
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.homeNav:
                Intent intent1 = new Intent(TranslateActivity.this, MainActivity.class);
                startActivity(intent1);
                break;
            case R.id.translateNav:
                break;
            case R.id.filesNav:
                Intent intent2 = new Intent(TranslateActivity.this, FileManagerActivity.class);
                startActivity(intent2);
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
