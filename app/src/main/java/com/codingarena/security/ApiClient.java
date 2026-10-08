package com.codingarena.security;

import android.os.Build;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiClient {

    private final String baseUrl;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    public JSONObject registerDevice() throws Exception {
        JSONObject body = new JSONObject();

        body.put("deviceKey", Build.MANUFACTURER + "_" + Build.MODEL + "_" + Build.ID);
        body.put("deviceName", Build.MODEL);
        body.put("androidVersion", Build.VERSION.RELEASE);
        body.put("manufacturer", Build.MANUFACTURER);
        body.put("model", Build.MODEL);
        body.put("appVersion", "1.0");

        return post("/api/devices/register", body);
    }

    public JSONObject sendScan(long deviceId, JSONObject report) throws Exception {
        JSONObject body = new JSONObject();

        body.put("riskScore", 0);
        body.put("riskLevel", "SAFE");
        body.put("reportJson", report.toString());

        return post("/api/devices/" + deviceId + "/scan", body);
    }

    public JSONObject getScans(long deviceId) throws Exception {
        return get("/api/devices/" + deviceId + "/scan");
    }

    private JSONObject post(String path, JSONObject body) throws Exception {
        URL url = new URL(baseUrl + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setConnectTimeout(10000);
        con.setReadTimeout(10000);
        con.setDoOutput(true);

        OutputStream out = con.getOutputStream();
        out.write(body.toString().getBytes("UTF-8"));
        out.close();

        return readResponse(con);
    }

    private JSONObject get(String path) throws Exception {
        URL url = new URL(baseUrl + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");
        con.setConnectTimeout(10000);
        con.setReadTimeout(10000);

        return readResponse(con);
    }

    private JSONObject readResponse(HttpURLConnection con) throws Exception {
        int code = con.getResponseCode();

        BufferedReader reader;

        if (code >= 200 && code < 300) {
            reader = new BufferedReader(
                    new InputStreamReader(con.getInputStream())
            );
        } else {
            reader = new BufferedReader(
                    new InputStreamReader(con.getErrorStream())
            );
        }

        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        con.disconnect();

        if (code < 200 || code >= 300) {
            throw new Exception("HTTP " + code + ": " + response);
        }

        return new JSONObject(response.toString());
    }
}
