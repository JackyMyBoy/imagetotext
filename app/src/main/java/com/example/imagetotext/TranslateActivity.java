package com.example.imagetotext;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
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

    public static final String PREFS_NAME = "MyPrefsFile1";
    public CheckBox dontShowAgain;

    private TextView mSourceLang;
    private EditText mSourceText;
    private Button mDetectLanguageBtn;
    private EditText mTranslatedText;
    private String sourceText;
    private Button mTranslateBtn;

    private ArrayList<CountryItem> mCountryList;
    private CountryAdapter mAdapter;
    private String clickedTlLanguageNameInto;
    private String clickedTlLanguageNameFrom;

    private Spinner spinnerLanguageTlInto;
    private Spinner spinnerLanguageTlFrom;

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
        spinnerLanguageTlInto = findViewById(R.id.spinnerTlInto);
        spinnerLanguageTlFrom = findViewById(R.id.spinnerTlFrom);

        mAdapter = new CountryAdapter(this,mCountryList);
        spinnerLanguageTlInto.setAdapter(mAdapter);
        spinnerLanguageTlFrom.setAdapter(mAdapter);
        clickedTlLanguageNameInto = "en";

        spinnerLanguageTlInto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CountryItem clickedItem = (CountryItem) parent.getItemAtPosition(position);
                clickedTlLanguageNameInto = clickedItem.getCountryCode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerLanguageTlFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CountryItem clickedItem = (CountryItem) parent.getItemAtPosition(position);
                clickedTlLanguageNameFrom = clickedItem.getCountryCode();
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

    @Override
    protected void onResume() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        LayoutInflater adbInflater = LayoutInflater.from(this);
        View eulaLayout = adbInflater.inflate(R.layout.checkbox,null);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        String skipMessage = settings.getString("skipMessage", "NOT checked");

        dontShowAgain = (CheckBox) eulaLayout.findViewById(R.id.skip);
        adb.setView(eulaLayout);
        adb.setTitle("Attention");
        adb.setMessage("For every new language use, please turn on Wi-Fi, so the language pack could be downloaded.");

        adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String checkBoxResult = "NOT checked";

                if (dontShowAgain.isChecked()) {
                    checkBoxResult = "checked";
                }

                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();

                editor.putString("skipMessage", checkBoxResult);
                editor.commit();

                // Do what you want to do on "OK" action

                return;
            }
        });

        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String checkBoxResult = "NOT checked";

                if (dontShowAgain.isChecked()) {
                    checkBoxResult = "checked";
                }

                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();

                editor.putString("skipMessage", checkBoxResult);
                editor.commit();

                // Do what you want to do on "CANCEL" action

                return;
            }
        });

        if (!skipMessage.equals("checked")) {
            adb.show();
        }
        super.onResume();
    }

    private void initList() {
        mCountryList = new ArrayList<>();
        mCountryList.add(new CountryItem("English", R.drawable.flag_united_kingdom, "en"));
        mCountryList.add(new CountryItem("Russian", R.drawable.flag_russian, "ru"));
        mCountryList.add(new CountryItem("Japanese", R.drawable.flag_japan, "ja"));
        mCountryList.add(new CountryItem("Lithuanian", R.drawable.flag_lithuania, "lt"));
        mCountryList.add(new CountryItem("Azerbaijani", R.drawable.flag_azerbaijan, "az"));
        mCountryList.add(new CountryItem("Armenian", R.drawable.flag_armenia, "hy"));
        mCountryList.add(new CountryItem("Belarus", R.drawable.flag_belarus, "be"));
        mCountryList.add(new CountryItem("Bosnian", R.drawable.flag_bosnia, "bs"));
        mCountryList.add(new CountryItem("Bulgarian", R.drawable.flag_bulgaria, "bg"));
        mCountryList.add(new CountryItem("Chinese", R.drawable.flag_lithuania, "zh"));
        mCountryList.add(new CountryItem("Croatian", R.drawable.flag_croatia, "hr"));
        mCountryList.add(new CountryItem("Czech", R.drawable.flag_czechia, "cs"));
        mCountryList.add(new CountryItem("Danish", R.drawable.flag_denmark, "da"));
        mCountryList.add(new CountryItem("Estonian", R.drawable.flag_estonia, "et"));
        mCountryList.add(new CountryItem("Finnish", R.drawable.flag_finland, "fi"));
        mCountryList.add(new CountryItem("French", R.drawable.flag_france, "fr"));
        mCountryList.add(new CountryItem("Georgian", R.drawable.flag_georgia, "ka"));
        mCountryList.add(new CountryItem("German", R.drawable.flag_germany, "de"));
        mCountryList.add(new CountryItem("Greek", R.drawable.flag_greece, "el"));
        mCountryList.add(new CountryItem("Hungarian", R.drawable.flag_hungary, "hu"));
        mCountryList.add(new CountryItem("Indonesian", R.drawable.flag_indonesia, "id"));
        mCountryList.add(new CountryItem("Irish", R.drawable.flag_ireland, "ga"));
        mCountryList.add(new CountryItem("Latvian", R.drawable.flag_latvia, "lv"));
        mCountryList.add(new CountryItem("Dutch", R.drawable.flag_netherlands, "nl"));
        mCountryList.add(new CountryItem("Norwegian", R.drawable.flag_norway, "no"));
        mCountryList.add(new CountryItem("Romanian", R.drawable.flag_romania, "ro"));
        mCountryList.add(new CountryItem("Serbian", R.drawable.flag_serbia, "cr"));
        mCountryList.add(new CountryItem("Slovak", R.drawable.flag_slovakia, "sk"));
        mCountryList.add(new CountryItem("Slovenian", R.drawable.flag_slovenia, "sl"));
        mCountryList.add(new CountryItem("Korean", R.drawable.flag_south_korea, "ko"));
        mCountryList.add(new CountryItem("Spanish", R.drawable.flag_spain, "es"));
        mCountryList.add(new CountryItem("Swedish", R.drawable.flag_sweden, "sv"));
        mCountryList.add(new CountryItem("Turkish", R.drawable.flag_turkey, "tr"));
        mCountryList.add(new CountryItem("Ukrainian", R.drawable.flag_ukraina, "uk"));
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
            case "be":
                langCode = FirebaseTranslateLanguage.BE;
                mSourceLang.setText("Belarusian");
                break;
            case "bg":
                langCode = FirebaseTranslateLanguage.BG;
                mSourceLang.setText("Bulgarian");
                break;
            case "zh":
                langCode = FirebaseTranslateLanguage.ZH;
                mSourceLang.setText("Chinese");
                break;
            case "hr":
                langCode = FirebaseTranslateLanguage.HR;
                mSourceLang.setText("Croatian");
                break;
            case "cs":
                langCode = FirebaseTranslateLanguage.CS;
                mSourceLang.setText("Czech");
                break;
            case "da":
                langCode = FirebaseTranslateLanguage.DA;
                mSourceLang.setText("Danish");
                break;
            case "et":
                langCode = FirebaseTranslateLanguage.ET;
                mSourceLang.setText("Estonian");
                break;
            case "fi":
                langCode = FirebaseTranslateLanguage.FI;
                mSourceLang.setText("Finnish");
                break;
            case "fr":
                langCode = FirebaseTranslateLanguage.FR;
                mSourceLang.setText("French");
                break;
            case "ka":
                langCode = FirebaseTranslateLanguage.KA;
                mSourceLang.setText("Georgian");
                break;
            case "de":
                langCode = FirebaseTranslateLanguage.DE;
                mSourceLang.setText("German");
                break;
            case "el":
                langCode = FirebaseTranslateLanguage.EL;
                mSourceLang.setText("Greek");
                break;
            case "hu":
                langCode = FirebaseTranslateLanguage.HU;
                mSourceLang.setText("Hungarian");
                break;
            case "id":
                langCode = FirebaseTranslateLanguage.ID;
                mSourceLang.setText("Indonesian");
                break;
            case "ga":
                langCode = FirebaseTranslateLanguage.GA;
                mSourceLang.setText("Irish");
                break;
            case "lv":
                langCode = FirebaseTranslateLanguage.LV;
                mSourceLang.setText("Latvian");
                break;
            case "nl":
                langCode = FirebaseTranslateLanguage.NL;
                mSourceLang.setText("Dutch");
                break;
            case "no":
                langCode = FirebaseTranslateLanguage.NO;
                mSourceLang.setText("Norwegian");
                break;
            case "ro":
                langCode = FirebaseTranslateLanguage.RO;
                mSourceLang.setText("Romanian");
                break;
            case "sk":
                langCode = FirebaseTranslateLanguage.SK;
                mSourceLang.setText("Slovak");
                break;
            case "sl":
                langCode = FirebaseTranslateLanguage.SL;
                mSourceLang.setText("Slovenian");
                break;
            case "ko":
                langCode = FirebaseTranslateLanguage.KO;
                mSourceLang.setText("Korean");
                break;
            case "es":
                langCode = FirebaseTranslateLanguage.ES;
                mSourceLang.setText("Spanish");
                break;
            case "sv":
                langCode = FirebaseTranslateLanguage.SV;
                mSourceLang.setText("Swedish");
                break;
            case "tr":
                langCode = FirebaseTranslateLanguage.TR;
                mSourceLang.setText("Turkish");
                break;
            case "uk":
                langCode = FirebaseTranslateLanguage.UK;
                mSourceLang.setText("Ukrainian");
                break;
            default:
                langCode = 0;
        }
        return langCode;
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
                        int count = 0;
                        for(CountryItem country : mCountryList){
                            if(country.getCountryCode().equals(s)) {
                                spinnerLanguageTlFrom.setSelection(count);
                            }
                            count++;
                        }
                        translateText(langCodeFrom, langCodeInto);
                    }
                }
            }
        });
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
