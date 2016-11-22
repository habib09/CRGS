package com.mpower.clientcollection.activities;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.net.Uri;
import android.os.Environment;

import android.util.Log;

import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import com.mpower.clientcollection.R;

public class HtmlTestActivty extends Activity {

    private static final String TAG = MainActivity.class.getCanonicalName();

    /**MEMBERS**/

    //private WebView mWebView;

    private ProgressBar mLoading;
    public static final String APPLICATION_ROOT = Environment.getExternalStorageDirectory()
            + File.separator + "snl";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        //mWebView = (WebView)findViewById(R.id.wvPortal);

        //mLoading = (ProgressBar) findViewById(R.id.pbLoading);

        //getWindow().requestFeature(Window.FEATURE_PROGRESS);

        Log.w(MainActivity.class.getSimpleName(), "APPLICATION_ROOT = " + APPLICATION_ROOT);
        WebView mWebView = new WebView(this);
        setContentView(mWebView);

        WebSettings mWebSettings = mWebView.getSettings();

        mWebSettings.setJavaScriptEnabled(true);
        // mWebView.setWebChromeClient(new WebChromeClient());
        //mWebView.setWebChromeClient(new BridgeWCClient());

        final Activity activity = this;
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
                activity.setProgress(progress * 1000);
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });
        /*String path = "android.resource://" + getPackageName() + "/raw/test_video" ;
        mWebView.loadUrl(path);
*/
        mWebView.loadUrl("file:///android_asset/www/index.html");
        //mWebView.loadUrl("http://developer.android.com/");

    }
}