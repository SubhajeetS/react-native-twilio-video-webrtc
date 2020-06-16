package com.twiliorn.library;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class TwilioModule extends ReactContextBaseJavaModule {
    private static final int REQUEST_MEDIA_PROJECTION = 100;
    private Promise permissionPromise;
    private ScreenCapturerManager screenCapturerManager;

    TwilioModule(ReactApplicationContext context) {
        super(context);
        context.addActivityEventListener(mActivityEventListener);
    }

    @Override
    public String getName() {
        return "TwilioModule";
    }

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
            if(requestCode == REQUEST_MEDIA_PROJECTION && permissionPromise != null) {
                if(resultCode != AppCompatActivity.RESULT_OK) {
                    permissionPromise.reject("E_PERMISSION_DENIED", "Permission was denied") ;
                    return;
                }
                CustomTwilioVideoView.startScreenCapture(resultCode, data);
                permissionPromise.resolve(true);
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @ReactMethod
    public void requestPermission(final Promise promise) {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            promise.reject("E_ACTIVITY_DOES_NOT_EXIST", "Activity doesn't exist");
            return;
        }
        // Store the promise to resolve/reject when picker returns data
        permissionPromise = promise;
        try {
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                    currentActivity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);

            currentActivity.startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
        } catch (Exception e) {
            permissionPromise.reject("E_FAILED_TO_SHOW_PICKER", e);
            permissionPromise = null;
        }
    }

}