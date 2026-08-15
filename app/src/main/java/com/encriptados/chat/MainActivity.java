package com.encriptados.chat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
public class MainActivity extends AppCompatActivity {
  private static final String HOME = "https://chat.getencriptados.com/";
  private static final int REQ_MIC = 101;
  private static final int REQ_FILE = 102;
  private WebView web;
  private ValueCallback<Uri[]> fileCallback;
  private PermissionRequest pendingPerm;
  @SuppressLint("SetJavaScriptEnabled")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    web = new WebView(this);
    setContentView(web);
    web.setBackgroundColor(0xFF04120C);
    WebSettings s = web.getSettings();
    s.setJavaScriptEnabled(true);
    s.setDomStorageEnabled(true);
    s.setMediaPlaybackRequiresUserGesture(false);
    s.setSupportMultipleWindows(false);
    s.setAllowFileAccess(false);
    s.setAllowContentAccess(false);
    web.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String u = request.getUrl().toString();
        if (u.contains("getencriptados.com")) return false;
        try { startActivity(new Intent(Intent.ACTION_VIEW, request.getUrl())); } catch (Exception ignored) {}
        return true;
      }
    });
    web.setWebChromeClient(new WebChromeClient() {
      @Override
      public void onPermissionRequest(final PermissionRequest request) {
        for (String res : request.getResources()) {
          if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(res)) {
            pendingPerm = request;
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
              request.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
              pendingPerm = null;
            } else {
              ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQ_MIC);
            }
            return;
          }
        }
        request.deny();
      }
      @Override
      public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        fileCallback = filePathCallback;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        try {
          startActivityForResult(Intent.createChooser(i, "Elegir foto"), REQ_FILE);
        } catch (Exception e) {
          fileCallback = null;
          return false;
        }
        return true;
      }
    });
    getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        if (web.canGoBack()) web.goBack();
        else finish();
      }
    });
    if (savedInstanceState == null) web.loadUrl(HOME);
  }
  @Override
  protected void onSaveInstanceState(@NonNull Bundle out) {
    super.onSaveInstanceState(out);
    web.saveState(out);
  }
  @Override
  protected void onRestoreInstanceState(@NonNull Bundle in) {
    super.onRestoreInstanceState(in);
    web.restoreState(in);
  }
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQ_MIC && pendingPerm != null) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        pendingPerm.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
      else
        pendingPerm.deny();
      pendingPerm = null;
    }
  }
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQ_FILE) {
      if (fileCallback == null) return;
      Uri[] result = null;
      if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
        result = new Uri[]{ data.getData() };
      }
      fileCallback.onReceiveValue(result);
      fileCallback = null;
    }
  }
}
