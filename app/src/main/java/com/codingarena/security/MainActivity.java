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
