package io.github.nfdz.findoordemoapp.common.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.nfdz.findoordemoapp.R;


public class PermissionsHelper {

    public interface Callback {
        void onPermissionsGranted();
        void onPermissionsDenied();
    }

    private static final int INSIST_LIMIT = 10;
    private static final short REQUEST_CODE = 6412;

    private final Activity activity;
    private final List<String> permissionsToRequest;
    private final Callback callback;
    private int askCounter = 0;

    public PermissionsHelper(Activity activity, List<String> permissionsToRequest, Callback callback) {
        this.activity = activity;
        this.permissionsToRequest = permissionsToRequest;
        this.callback = callback;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            askPermissions();
        }
    }

    public void askPermissions() {
        if (++askCounter > INSIST_LIMIT) {
            callback.onPermissionsDenied();
            return;
        }

        List<String> permissions = new ArrayList<>();
        for (String permission : permissionsToRequest) {
            boolean hasPermission = checkPermission(permission);
            if (!hasPermission && shouldShowRequestPermissionRationale(permission)) {
                showExplanatoryDialog(permission);
                return;
            }
            if (!hasPermission) permissions.add(permission);
        }

        if (permissions.isEmpty()) {
            callback.onPermissionsGranted();
        } else {
            askPermissions(permissions);
        }
    }

    private boolean checkPermission(String permission) {
        return ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean shouldShowRequestPermissionRationale(String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    private void askPermissions(List<String> permissions) {
        ActivityCompat.requestPermissions(activity, permissions.toArray(new String[]{}), REQUEST_CODE);
    }

    private void showExplanatoryDialog(final String permission) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        askPermissions(Collections.singletonList(permission));
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        callback.onPermissionsDenied();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(R.string.explain_permissions_title)
                .setMessage(R.string.explain_permissions_msg)
                .setPositiveButton(android.R.string.ok, dialogClickListener)
                .setNegativeButton(android.R.string.cancel, dialogClickListener)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        callback.onPermissionsDenied();
                    }
                })
                .show();
    }
}