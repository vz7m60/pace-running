package com.gm.pacerunning;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.io.File;
import java.io.IOException;

public class MainActivity extends Activity {
    private static final String START_URL = "https://vz7m60.github.io/pace-running/";
    private static final String APP_HOST = "vz7m60.github.io";
    private static final String APP_PATH = "/pace-running";
    private static final int REQUEST_LOCATION = 4101;
    private static final int REQUEST_IMAGE = 4102;

    private WebView webView;
    private ValueCallback<Uri[]> imageCallback;
    private Uri cameraOutputUri;
    private GeolocationPermissions.Callback geolocationCallback;
    private String geolocationOrigin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        getWindow().setStatusBarColor(Color.rgb(243, 241, 232));
        getWindow().setNavigationBarColor(Color.rgb(243, 241, 232));
        WindowInsetsControllerCompat systemBars = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        systemBars.setAppearanceLightStatusBars(true);
        systemBars.setAppearanceLightNavigationBars(true);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(243, 241, 232));
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setSupportMultipleWindows(false);
        webView.addJavascriptInterface(new PaceBridge(), "PaceAndroid");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleNavigation(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleNavigation(Uri.parse(url));
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                installWebAppBridges();
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                requestLocationPermission(origin, callback);
            }

            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                openImageChooser(callback);
                return true;
            }
        });
        setContentView(webView);

        if (savedInstanceState != null && webView.restoreState(savedInstanceState) != null) {
            return;
        }
        webView.loadUrl(START_URL);
    }

    private boolean handleNavigation(Uri uri) {
        String scheme = uri.getScheme();
        String host = uri.getHost();
        String path = uri.getPath() == null ? "" : uri.getPath();
        if ("https".equalsIgnoreCase(scheme)
                && APP_HOST.equalsIgnoreCase(host)
                && (path.equals(APP_PATH) || path.startsWith(APP_PATH + "/"))) {
            return false;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception ignored) {
            Toast.makeText(this, R.string.cannot_open_link, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private void requestLocationPermission(String origin, GeolocationPermissions.Callback callback) {
        if (hasLocationPermission()) {
            callback.invoke(origin, true, true);
            return;
        }
        geolocationOrigin = origin;
        geolocationCallback = callback;
        requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, REQUEST_LOCATION);
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_LOCATION || geolocationCallback == null) return;
        boolean granted = hasLocationPermission();
        geolocationCallback.invoke(geolocationOrigin, granted, granted);
        geolocationCallback = null;
        geolocationOrigin = null;
    }

    private void openImageChooser(ValueCallback<Uri[]> callback) {
        if (imageCallback != null) imageCallback.onReceiveValue(null);
        imageCallback = callback;

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.setType("image/*");
        Intent chooser = Intent.createChooser(galleryIntent, getString(R.string.choose_photo));
        Intent cameraIntent = createCameraIntent();
        if (cameraIntent != null) {
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});
        }
        try {
            startActivityForResult(chooser, REQUEST_IMAGE);
        } catch (Exception ignored) {
            imageCallback.onReceiveValue(null);
            imageCallback = null;
            Toast.makeText(this, R.string.cannot_open_camera, Toast.LENGTH_SHORT).show();
        }
    }

    private Intent createCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) == null) return null;
        try {
            File photoDirectory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "capture");
            if (!photoDirectory.exists() && !photoDirectory.mkdirs()) return null;
            File photoFile = File.createTempFile("PACE_", ".jpg", photoDirectory);
            cameraOutputUri = FileProvider.getUriForFile(this, getPackageName() + ".files", photoFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraOutputUri);
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            cameraIntent.setClipData(ClipData.newUri(getContentResolver(), "PACE photo", cameraOutputUri));
            return cameraIntent;
        } catch (IOException | IllegalArgumentException error) {
            cameraOutputUri = null;
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_IMAGE || imageCallback == null) return;
        Uri[] results = null;
        if (resultCode == RESULT_OK) {
            Uri selectedImage = data == null ? null : data.getData();
            if (selectedImage != null) results = new Uri[]{selectedImage};
            else if (cameraOutputUri != null) results = new Uri[]{cameraOutputUri};
        }
        imageCallback.onReceiveValue(results);
        imageCallback = null;
        cameraOutputUri = null;
    }

    private void installWebAppBridges() {
        String script = "(function(){"
                + "if(window.PaceAndroid && typeof navigator.share !== 'function'){"
                + "try{Object.defineProperty(navigator,'share',{configurable:true,value:function(data){"
                + "window.PaceAndroid.share(data.title||'PACE',data.text||'',data.url||location.href);"
                + "return Promise.resolve();}});}catch(e){}}"
                + "var attach=function(){var modal=document.querySelector('.run-modal');"
                + "if(!modal||!window.PaceAndroid)return false;"
                + "var sync=function(){window.PaceAndroid.setScreenOn(modal.dataset.phase==='live');};"
                + "new MutationObserver(sync).observe(modal,{attributes:true,attributeFilter:['data-phase']});"
                + "document.addEventListener('visibilitychange',sync);sync();return true;};"
                + "if(!attach()){var id=setInterval(function(){if(attach())clearInterval(id);},300);"
                + "setTimeout(function(){clearInterval(id);},15000);}})();";
        webView.evaluateJavascript(script, null);
    }

    @Override
    public void onBackPressed() {
        if (webView == null) {
            super.onBackPressed();
            return;
        }
        String script = "(function(){"
                + "var map=document.querySelector('#route-map-shell');"
                + "if(map&&map.classList.contains('map-fullscreen')){"
                + "document.dispatchEvent(new KeyboardEvent('keydown',{key:'Escape',bubbles:true}));return 'handled';}"
                + "var safety=document.querySelector('#safety-dialog');"
                + "if(safety&&safety.open){document.querySelector('#close-safety-info').click();return 'handled';}"
                + "var dialog=document.querySelector('#run-dialog');"
                + "if(dialog&&dialog.open){var phase=document.querySelector('.run-modal').dataset.phase;"
                + "if(phase==='ready'||phase==='countdown')document.querySelector('#close-run').click();"
                + "return 'handled';}return 'exit';})()";
        webView.evaluateJavascript(script, result -> {
            if ("\"exit\"".equals(result)) {
                if (webView.canGoBack()) webView.goBack();
                else finish();
            }
        });
    }

    @Override
    protected void onPause() {
        if (webView != null) webView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onDestroy() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    private final class PaceBridge {
        @JavascriptInterface
        public void setScreenOn(boolean keepScreenOn) {
            runOnUiThread(() -> {
                if (keepScreenOn) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            });
        }

        @JavascriptInterface
        public void share(String title, String text, String url) {
            runOnUiThread(() -> {
                String message = (text == null ? "" : text) + (url == null || url.isEmpty() ? "" : "\n" + url);
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, message.trim());
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, title == null ? getString(R.string.share_run) : title);
                startActivity(Intent.createChooser(sendIntent, getString(R.string.share_run)));
            });
        }
    }
}
