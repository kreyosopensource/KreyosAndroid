package com.kreyos.kreyosandroid.listeners;

import org.json.JSONObject;


public interface IAsyncTaskLoginListener {
    public void onLoginResult(boolean pResult, String pResponse, JSONObject pJsonObject);
    public void onCreateEmailResult(boolean pResult, String pResponse, String pEmailText, String pPasswordText, String pPasswordConf);
    public void onCreateUserInformationResult(boolean pResult, String pResponse, JSONObject pJsonObject);
}
