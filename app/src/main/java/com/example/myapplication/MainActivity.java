package com.example.myapplication;


import android.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        this.getWindow().setFlags(1024,                       //屏蔽通知栏
                1024);                  //WindowManager.LayoutParams.FLAG_FULLSCREEN =1024
        //actionBar = getActionBar();
        //actionBar.hide();
        WebView OneWebView = (WebView)findViewById(R.id.OneWebView);
        WebSettings settings = OneWebView.getSettings();
        OneWebView.loadUrl("http://vue.ydcloud.xyz/LightMerit/#/032185");

        settings.setSupportZoom(false);
        settings.setLoadsImagesAutomatically(true);//自动加载图片
        settings.setBuiltInZoomControls(true);
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        OneWebView.setWebViewClient(new WebViewClient(){

        });
    }
}