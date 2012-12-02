package com.example.reboxed;

import android.content.Context;
import android.webkit.WebView;

public class GifWebView extends WebView {

    public GifWebView(Context context, String path) {
        super(context);            
        setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);  
        loadUrl(path);
    }   
}
