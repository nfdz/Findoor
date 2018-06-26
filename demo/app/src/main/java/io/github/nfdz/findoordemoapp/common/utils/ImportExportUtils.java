package io.github.nfdz.findoordemoapp.common.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.provider.DocumentFile;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import io.github.nfdz.findoordemoapp.R;
import io.github.nfdz.findoordemoapp.common.model.WifiRecord;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import timber.log.Timber;

public class ImportExportUtils {

    private static final int READ_REQUEST_CODE = 911;
    private static final int WRITE_REQUEST_CODE = 824;

    private static final String CHARSET = "UTF-8";
    private static final String MIME_TYPE = "text/*";
    private static final String SUGGESTED_NAME_FORMAT = "location-%s-%s.findoor";

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    public static void importLocation(Activity activity) {
        // choose a file via the system's file browser
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        // show only results that can be "opened", such as a file
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // filter to show only plain text
        intent.setType(MIME_TYPE);

        activity.startActivityForResult(intent, READ_REQUEST_CODE);
    }

    public static boolean onImportActivityResult(int requestCode,
                                                 int resultCode,
                                                 Intent resultData,
                                                 final Context context,
                                                 Realm realm) {
        if (requestCode == READ_REQUEST_CODE) {
            // URI to user document is contained in the return intent
            if (resultCode == Activity.RESULT_OK && resultData != null && resultData.getData() != null) {
                final Uri uri = resultData.getData();
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(@NonNull Realm realm) {
                        InputStream in = null;
                        try {
                            DocumentFile file = DocumentFile.fromSingleUri(context, uri);
                            in = context.getContentResolver().openInputStream(file.getUri());
                            if (in != null && in.available() != 0) {
                                String inputStreamString = new Scanner(in, CHARSET).useDelimiter("\\A").next();
                                try {
                                    Gson gson = new Gson();
                                    WifiRecord[] records = gson.fromJson(inputStreamString, WifiRecord[].class);
                                    realm.copyToRealmOrUpdate(Arrays.asList(records));
                                } catch (JsonSyntaxException e) {
                                    throw new RuntimeException(context.getString(R.string.import_error_serialization), e);
                                }
                            } else {
                                throw new RuntimeException(context.getString(R.string.import_error_empty));
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(context.getString(R.string.import_error_file), e);
                        } finally {
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e) {
                                    // swallow
                                }
                            }
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(context, R.string.import_success, Toast.LENGTH_LONG).show();
                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(@NonNull Throwable error) {
                        Timber.e(error);
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else if (resultCode != Activity.RESULT_CANCELED) {
                Toast.makeText(context, R.string.import_error, Toast.LENGTH_LONG).show();
            }
            return true;
        } else {
            return false;
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    public static void exportLocation(Activity activity, int location) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        // show only results that can be "opened", such as a file
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // create a file with plain text MIME type
        intent.setType(MIME_TYPE);
        String timeFormatPattern = activity.getString(R.string.date_format_pattern);
        SimpleDateFormat sdf = new SimpleDateFormat(timeFormatPattern, Locale.getDefault());
        String currentDate = sdf.format(new Date());
        String name = location + PreferencesUtils.getLocationAlias(activity, location);
        String suggestedName = String.format(SUGGESTED_NAME_FORMAT, name, currentDate);
        intent.putExtra(Intent.EXTRA_TITLE, suggestedName);
        activity.startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    public static boolean onExportActivityResult(int requestCode,
                                                 int resultCode,
                                                 Intent resultData,
                                                 final Context context,
                                                 final int location,
                                                 Realm realm) {
        if (requestCode == WRITE_REQUEST_CODE) {
            // URI to user document is contained in the return intent
            if (resultCode == Activity.RESULT_OK && resultData != null && resultData.getData() != null) {
                final Uri uri = resultData.getData();
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(@NonNull Realm realm) {
                        RealmResults<WifiRecord> records = realm.where(WifiRecord.class)
                                .equalTo(WifiRecord.LOCATION_FIELD, location)
                                .sort(WifiRecord.TIMESTAMP_FIELD, Sort.ASCENDING)
                                .findAll();
                        Gson gson = new Gson();
                        String serializedRecords = gson.toJson(records.toArray(new WifiRecord[]{}));
                        OutputStream out = null;
                        try {
                            DocumentFile newFile = DocumentFile.fromSingleUri(context, uri);
                            out = context.getContentResolver().openOutputStream(newFile.getUri());
                            out.write(serializedRecords.getBytes(CHARSET));
                        } catch (Exception e) {
                            throw new RuntimeException(context.getString(R.string.export_error_file), e);
                        } finally {
                            if (out != null) {
                                try {
                                    out.close();
                                } catch (IOException e) {
                                    // swallow
                                }
                            }
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(context, R.string.export_success, Toast.LENGTH_LONG).show();
                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(@NonNull Throwable error) {
                        Timber.e(error);
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else if (resultCode != Activity.RESULT_CANCELED) {
                Toast.makeText(context, R.string.export_error, Toast.LENGTH_LONG).show();
            }
            return true;
        } else {
            return false;
        }
    }

}
