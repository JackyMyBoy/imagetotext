package com.example.imagetotext;

public class CountryItem {
    private String mCountryName;
    private String mCountryCode;
    private int mFlagImage;

    public CountryItem(String countryName, int flagImage, String countryCode){
        mCountryName = countryName;
        mFlagImage = flagImage;
        mCountryCode = countryCode;
    }

    public String getCountryName(){
        return mCountryName;
    }
    public String getCountryCode(){
        return mCountryCode;
    }

    public int getFlagImage(){
        return mFlagImage;
    }
}
