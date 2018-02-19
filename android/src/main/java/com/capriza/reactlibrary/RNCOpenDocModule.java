
package com.capriza.reactlibrary;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.io.File;
import java.net.HttpURLConnection;

public class RNCOpenDocModule extends ReactContextBaseJavaModule {
  private static final String LOG_TAG = "RNCOpenDoc";

  private final ReactApplicationContext reactContext;

  public RNCOpenDocModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNCOpenDoc";
  }

  private String getMimeType(String filePath) {
    String ext = "";
    int nameEndIndex = filePath.lastIndexOf('.');
    if (nameEndIndex > 0) {
      ext = filePath.substring(nameEndIndex + 1);
    }
    Log.d(LOG_TAG, ext);
    MimeTypeMap mime = MimeTypeMap.getSingleton();
    String type = mime.getMimeTypeFromExtension(ext.toLowerCase());
    if (type == null) {
      type = HttpURLConnection.guessContentTypeFromName(filePath);
    }

    if (type == null) {
      type = "application/" + ext;
    }
    return type;
  }

  @ReactMethod
  public void open(String path) {
    if (path.startsWith("file://")) {
      path = path.replace("file://", "");
    }

    File file = new File(path);
    if (!file.exists()) {
      Log.e(LOG_TAG, "File does not exist");
      return;
    }

    try {
      Uri uri = FileProvider.getUriForFile(reactContext.getApplicationContext(),reactContext.getApplicationContext().getPackageName() + ".provider", file);

      String type = this.getMimeType(uri.toString());

      Intent intent = new Intent(Intent.ACTION_VIEW, uri);

      if (type != null && uri != null) {
        intent.setDataAndType(uri, type);
      } else if (type != null) {
        intent.setType(type);
      }

      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

      getReactApplicationContext().startActivity(intent);
    } catch(ActivityNotFoundException ex) {
      Log.e(LOG_TAG, "can't open document", ex);
    }
  }

  @ReactMethod
  public void share(String path) {
    if (path.startsWith("file://")) {
      path = path.replace("file://", "");
    }

    File file = new File(path);
    if (!file.exists()) {
      Log.e(LOG_TAG, "File does not exist");
      return;
    }

    try {
      Uri uri = FileProvider.getUriForFile(reactContext.getApplicationContext(),reactContext.getApplicationContext().getPackageName() + ".provider", file);

      String type = this.getMimeType(uri.toString());

      Intent shareIntent = new Intent();
      shareIntent.setAction(Intent.ACTION_SEND);
      shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
      shareIntent.setType(type);
      shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

      Intent i = Intent.createChooser(shareIntent, "Share");
      i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      getReactApplicationContext().startActivity(i);
    } catch(ActivityNotFoundException ex) {
      Log.e(LOG_TAG, "can't share document", ex);
    }
  }
}