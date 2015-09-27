package com.example.raghav.myola;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.view.View;
import android.util.*;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
public class EnterActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        final String redirect_uri = "http://localhost/team38";
        Button btn = (Button)findViewById(R.id.loginBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final WebView webview = (WebView) findViewById(R.id.oathwebview);
                webview.setVisibility(View.VISIBLE);
                // set up webview for OAuth2 login
                webview.setWebViewClient(new WebViewClient() {
                                             @Override
                                             public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                                 if (url.startsWith(redirect_uri)) {

                                                     // extract OAuth2 access_token appended in url
                                                     if (url.indexOf("access_token=") != -1) {
                                                         webview.setVisibility(View.GONE);
                                                         //Toast.makeText(getApplicationContext()," URL "+url,Toast.LENGTH_LONG).show();
                                                         System.out.println("URL"+url);

                                                         int firstAmpersand=0;
                                                         while(url.charAt(firstAmpersand)!='&'){
                                                             firstAmpersand++;
                                                         }
                                                         String access_token=url.substring((url.indexOf("access_token=")+13),firstAmpersand);
                                                         System.out.println("FIRST AMPERSAND " + firstAmpersand);
                                                         Toast.makeText(getApplicationContext(),"access token "+ access_token,Toast.LENGTH_LONG).show();
                                                         Log.e("Access token - ", url.valueOf("access_token=") + "");
                                                         SharedPreferences pref = getApplicationContext().getSharedPreferences("currentuser", 0); // 0 - for private mode
                                                         Editor editor = pref.edit();
                                                         editor.putString("access_token", access_token);
                                                         editor.commit();
                                                         Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                         startActivity(intent);

                                                     }

                                                     return true;
                                                 }
                                                 return super.shouldOverrideUrlLoading(view, url);
                                             }
                                             public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
                                                 handler.proceed() ;
                                             }
                                         }
                );
                webview.loadUrl("http://sandbox-t.olacabs.com/oauth2/authorize?response_type=token&client_id=Mzc3OTlmNmMtMWQ1Mi00MjE5LWJlZDQtOGMyMDUzMzQxYzc0&redirect_uri=http://localhost/team38&scope=profile%20booking&state=state123");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_enter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
