package com.example.imagetotext;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;

public class TranslateActivity extends AppCompatActivity {

    private TextView mSourceLang;
    private EditText mSourceText;
    private Button mTranslateBtn;
    private TextView mTranslatedText;
    private String sourceText;

    private ArrayList<CountryItem> mCountryList;
    private CountryAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.translate_main);
        mSourceLang = findViewById(R.id.sourceLang);
        mSourceText = findViewById(R.id.sourceText);
        mTranslateBtn = findViewById(R.id.translate);
        mTranslatedText = findViewById(R.id.translatedText);

        initList();
        Spinner spinnerCountries = findViewById(R.id.spinnerTlInto);
        mAdapter = new CountryAdapter(this,mCountryList);
        spinnerCountries.setAdapter(mAdapter);

        spinnerCountries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CountryItem clickedItem = (CountryItem) parent.getItemAtPosition(position);
                String clickedCountryName = clickedItem.getCountryName();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        Intent intent = getIntent();
        final String text = intent.getStringExtra("EXTRA_TEXT");
        mSourceText.setText(text);

        mTranslateBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                identifyLanguage(text);
            }
        });


    }

    private void initList() {
        mCountryList = new ArrayList<>();
        mCountryList.add(new CountryItem("English", R.drawable.flag_united_kingdom));
        mCountryList.add(new CountryItem("Russian", R.drawable.flag_russian));
        mCountryList.add(new CountryItem("Japanese", R.drawable.flag_japan));
        mCountryList.add(new CountryItem("Lithuanian", R.drawable.flag_lithuania));
    }

    private void identifyLanguage(String text) {

        sourceText = mSourceText.getText().toString();
        FirebaseLanguageIdentification identifier = FirebaseNaturalLanguage.getInstance().getLanguageIdentification();

        mSourceLang.setText("Detecting...");

        identifier.identifyLanguage(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                if(s.equals("und")){
                    Toast.makeText(getApplicationContext(), "Language not identified", Toast.LENGTH_SHORT).show();
                }
                else {
                    getLanguageCode(s);
                }
            }
        });
    }

    private void getLanguageCode(String language) {
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
        translateText(langCode);
    }

    private void translateText(int langCode) {
        mTranslatedText.setText("Translating...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder().setSourceLanguage(langCode)
                .setTargetLanguage(FirebaseTranslateLanguage.EN).build();
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
}
