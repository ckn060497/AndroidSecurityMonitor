package com.codingarena.security;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.content.pm.*;
import android.net.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityManager;
import org.json.*;
import java.util.*;
import java.io.*;
import java.net.*;
import android.widget.Toast;
import org.json.JSONObject;

public class MainActivity extends Activity {
    WebView web;
    long deviceId=-1;
    Set<String> knownRemote = new HashSet<>(Arrays.asList(
        "com.anydesk.anydeskandroid", "com.teamviewer.quicksupport.market",
        "com.teamviewer.host.market", "com.google.chromeremotedesktop",
        "com.microsoft.rdc.androidx", "com.splashtop.remote.sos"
    ));

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(android.graphics.Color.rgb(24,28,27));
        getWindow().setNavigationBarColor(android.graphics.Color.rgb(24,28,27));
        web = new WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.setWebViewClient(new WebViewClient());
        web.addJavascriptInterface(new SecurityBridge(), "AndroidSecurity");
        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
        registerDevice();
    }
 private void registerDevice() {
    new Thread(() -> {
        try {
            String deviceKey = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );

            if (deviceKey == null || deviceKey.isEmpty()) {
                deviceKey = Build.MANUFACTURER + "_" + Build.MODEL;
            }

            String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
            String androidVersion = Build.VERSION.RELEASE;
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            String appVersion = "1.0";

            JSONObject data = new JSONObject();
            data.put("deviceKey", deviceKey);
            data.put("deviceName", deviceName);
            data.put("androidVersion", androidVersion);
            data.put("manufacturer", manufacturer);
            data.put("model", model);
            data.put("appVersion", appVersion);

            URL url = new URL(
                    "http://androidsecuritymonitor.us-east-1.elasticbeanstalk.com/api/devices/register"
            );

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(data.toString().getBytes("UTF-8"));
            }

            int code = conn.getResponseCode();

            InputStream stream = code >= 200 && code < 300
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(stream)
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            br.close();

            String result = response.toString();


            runOnUiThread(() -> {
    try {
        JSONObject obj = new JSONObject(result);
        deviceId = obj.getLong("id");

        web.evaluateJavascript(
    "window.setDeviceId("+deviceId+");",
    null
);
        
        Toast.makeText(
                MainActivity.this,
                "Registration successful\nDevice ID: " + deviceId,
                Toast.LENGTH_LONG
        ).show();

    } catch (Exception e) {
        Toast.makeText(
                MainActivity.this,
                "Registration response error\nHTTP: " + code,
                Toast.LENGTH_LONG
        ).show();
    }
});
           /* runOnUiThread(() -> {
                android.util.Log.d(
                        "DEVICE_REGISTER",
                        "HTTP " + code + ": " + result
                );

                try {
                    JSONObject obj = new JSONObject(result);

                    deviceId = obj.getLong("id");

                    android.util.Log.d(
                            "DEVICE_REGISTER",
                            "Registered deviceId = " + deviceId
                    );

                } catch (Exception e) {
                    android.util.Log.e(
                            "DEVICE_REGISTER",
                            "Invalid response: " + e.getMessage()
                    );
                }
            });
     */

            conn.disconnect();

        } catch (Exception e) {
            android.util.Log.e(
                    "DEVICE_REGISTER",
                    "Registration failed: " + e.getMessage(),
                    e
            );
        }
    }).start();
}

    class SecurityBridge {
        @JavascriptInterface public void open(String action) { runOnUiThread(() -> openSettings(action)); }
        @JavascriptInterface public void uninstall(String pkg) {
            if (pkg == null || pkg.contains("com.codingarena.security")) return;
            Intent i = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + pkg));
            startActivity(i);
        }
        @JavascriptInterface public String scan() { return buildReport().toString(); }
    }

    void openSettings(String action) {
        Intent i;
        try {
            switch(action) {
                case "accessibility": i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS); break;
                case "admin": i = new Intent(Settings.ACTION_SECURITY_SETTINGS); break;
                case "overlay": i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION); break;
                case "playprotect": i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gms")); break;
                case "updates": i = new Intent(Settings.ACTION_SECURITY_SETTINGS); break;
                default: i = new Intent(Settings.ACTION_SECURITY_SETTINGS);
            }
            startActivity(i);
        } catch(Exception e) { Toast.makeText(this, "Open Android Settings manually", Toast.LENGTH_LONG).show(); }
    }

    JSONObject buildReport() {
        JSONObject rep = new JSONObject();
        try {
            JSONArray remote = new JSONArray(), powerful = new JSONArray(), apps = new JSONArray();
            PackageManager pm = getPackageManager();
            List<PackageInfo> installed = pm.getInstalledPackages(0);
            for (PackageInfo p : installed) {
                String label = pm.getApplicationLabel(p.applicationInfo).toString();
                String low = label.toLowerCase(Locale.US);
                boolean candidate = knownRemote.contains(p.packageName) || low.contains("anydesk") || low.contains("teamviewer") || low.contains("remote support") || low.contains("remote control");
                JSONObject a = new JSONObject(); a.put("label", label); a.put("package", p.packageName); a.put("remote", candidate); apps.put(a);
                if(candidate) { JSONObject x = new JSONObject(); x.put("label", label); x.put("package", p.packageName); remote.put(x); }
            }
            AccessibilityManager am = (AccessibilityManager)getSystemService(ACCESSIBILITY_SERVICE);
            for(AccessibilityServiceInfo s : am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
                JSONObject x = new JSONObject(); x.put("type","Accessibility service"); x.put("package",s.getResolveInfo().serviceInfo.packageName); powerful.put(x);
            }
            DevicePolicyManager d = (DevicePolicyManager)getSystemService(DEVICE_POLICY_SERVICE);
            List<ComponentName> admins = d.getActiveAdmins();
            if(admins != null) for(ComponentName c : admins) { JSONObject x=new JSONObject(); x.put("type","Device administrator"); x.put("package",c.getPackageName()); powerful.put(x); }
            ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            Network n=cm.getActiveNetwork(); NetworkCapabilities nc=n==null?null:cm.getNetworkCapabilities(n);
            rep.put("remoteAccess",remote); rep.put("powerfulAccess",powerful); rep.put("apps",apps);
            rep.put("vpn",nc!=null && nc.hasTransport(NetworkCapabilities.TRANSPORT_VPN));
            rep.put("scannedAt",System.currentTimeMillis());
        } catch(Exception e) { try { rep.put("error",e.toString()); } catch(Exception ignored){} }
        return rep;
    }

    @Override public void onResume() {
        super.onResume();
        if(web != null) web.postDelayed(() -> web.evaluateJavascript("window.setSecurityReport(" + buildReport().toString() + ");", null), 250);
    }
    @Override public void onBackPressed() { if(web != null && web.canGoBack()) web.goBack(); else super.onBackPressed(); }
}
