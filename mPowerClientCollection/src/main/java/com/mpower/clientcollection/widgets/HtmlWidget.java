package com.mpower.clientcollection.widgets;

import android.content.Context;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.mpower.clientcollection.application.ClientCollection;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

/**
 * Created by ratna on 4/12/16.
 */
public class HtmlWidget extends QuestionWidget{
    final Context mContex;

    public HtmlWidget(Context context, FormEntryPrompt p) {
        super(context, p);
        mPrompt = p;
        mContex = context;
        WebView mWebView = new WebView(context);
        WebSettings mWebSettings = mWebView.getSettings();

        mWebSettings.setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
               // mContex.setProgress(progress * 1000);//TODO
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(mContex, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });

        mWebView.loadUrl("file:///android_asset/www/index.html");
        addView(mWebView);
    }

    @Override
    public IAnswerData getAnswer() {
        return null;
    }

    @Override
    public void clearAnswer() {

    }

    @Override
    public void setFocus(Context context) {

    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {

    }
}
