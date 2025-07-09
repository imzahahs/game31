package com.kaigan.pipedreams;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

//import com.google.android.vending.expansion.downloader.DownloadProgressInfo;
//import com.google.android.vending.expansion.downloader.impl.BroadcastDownloaderClient;
//import com.google.android.vending.expansion.downloader.impl.DownloaderProxy;
//import com.google.android.vending.expansion.downloader.impl.DownloaderService;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";


//    private static final String EXP_PATH = "/Android/obb/";
//
//    private static final String VERIFY_SHARED_FILENAME = "GAMEDATA_VERIFY";
//    private static final String VERIFY_VERSIONCODE_KEY = "VERSIONCODE";
//    private static final String VERIFY_FILESIZE_KEY = "FILESIZE";

    private static final String DOWNLOADER_NOTIFICATION_CHANNEL = "downloader-channel";

//    class DownloaderClient extends BroadcastDownloaderClient {
//        @Override
//        public void onDownloadStateChanged(int newState) {
//            Log.d(TAG, "Download state changed: " + newState);
//            if(newState == lastState)
//                return;         // dont know why state changed gets called multiple times, ignore
//
//            lastState = newState;
//
//            // Close previous dialog
//            if(downloadDialog != null) {
//                downloadDialog.cancel();
//                downloadDialog = null;
//            }
//
//            if(progress == null)
//                showProgressUI();
//
//            switch(newState) {
//                case STATE_IDLE:
//                case STATE_PAUSED_BY_REQUEST:
//                    // Unexpected, resume download
//                    service.requestContinueDownload();
//                    break;
//
//                case STATE_FETCHING_URL:
//                case STATE_CONNECTING:
//                    // Inform connecting
//                    progress.setTitleText("Checking files");
//                    break;
//
//                case STATE_DOWNLOADING:
//                    // Inform downloading
//                    progress.setTitleText("Downloading");
//                    service.requestDownloadStatus();
//                    break;
//
//                case STATE_COMPLETED:
//                    progress.setTitleText("Checking files");
//                    verifyGameDataCRC();
//                    break;
//
//                case STATE_PAUSED_WIFI_DISABLED:
//                case STATE_PAUSED_NEED_WIFI:
//                case STATE_PAUSED_NETWORK_UNAVAILABLE: {
//                    progress.setTitleText("Internet required");
//                    downloadDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
//                            .setTitleText("Internet Required")
//                            .setContentText("Please make sure your WIFI or cellular connection is turned on.")
//                            .setConfirmText("Exit")
//                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                                @Override
//                                public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                    sweetAlertDialog.cancel();
//                                    downloadDialog = null;
//                                    // Back to home
//                                    Intent homeIntent= new Intent(Intent.ACTION_MAIN);
//                                    homeIntent.addCategory(Intent.CATEGORY_HOME);
//                                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    startActivity(homeIntent);
//                                }
//                            });
//                    downloadDialog.setCancelable(false);
//                    downloadDialog.show();
//                    break;
//                }
//
//                case STATE_PAUSED_ROAMING:
//                case STATE_PAUSED_WIFI_DISABLED_NEED_CELLULAR_PERMISSION:
//                case STATE_PAUSED_NEED_CELLULAR_PERMISSION: {
//                    progress.setTitleText("No internet");
//                    downloadDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
//                            .setTitleText("Wi-Fi Unavailable")
//                            .setContentText(
//                                    "We need to download an additional 300MB of data and Wi-Fi is not available.\n" +
//                                    "\n" +
//                                    "Do you want to download using your cellular connection instead?"
//                            )
//                            .setConfirmText("Yes")
//                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                                @Override
//                                public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                    sweetAlertDialog.cancel();
//                                    downloadDialog = null;
//                                    // Allow over cellular
//                                    service.setDownloadFlags(DownloaderService.FLAGS_DOWNLOAD_OVER_CELLULAR);
//                                    service.requestContinueDownload();
//                                }
//                            })
//                            .setCancelText("No")
//                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                                @Override
//                                public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                    sweetAlertDialog.cancel();
//                                    downloadDialog = null;
//                                    // Back to home
//                                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
//                                    homeIntent.addCategory(Intent.CATEGORY_HOME);
//                                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    startActivity(homeIntent);
//                                }
//                            });
//                    downloadDialog.setCancelable(false);
//                    downloadDialog.show();
//                    break;
//                }
//
//                case STATE_PAUSED_SDCARD_UNAVAILABLE:
//                case STATE_FAILED_SDCARD_FULL: {
//                    progress.setTitleText("No storage");
//                    downloadDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
//                            .setTitleText("Storage Unavailable")
//                            .setContentText("Please make sure your storage is connected and has at least 300MB of free space.")
//                            .setConfirmText("Exit")
//                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                                @Override
//                                public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                    sweetAlertDialog.cancel();
//                                    downloadDialog = null;
//                                    // Back to home
//                                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
//                                    homeIntent.addCategory(Intent.CATEGORY_HOME);
//                                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    startActivity(homeIntent);
//                                }
//                            });
//                    downloadDialog.setCancelable(false);
//                    downloadDialog.show();
//                    break;
//                }
//
//                default: {
//                    // Errors, no license, network redirection, etc
//                    progress.setTitleText("Error");
//                    downloadDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
//                            .setTitleText("Download Error")
//                            .setContentText("Unable to check files, please download again. Open Google Play?")
//                            .setConfirmText("Yes")
//                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                                @Override
//                                public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                    sweetAlertDialog.cancel();
//                                    downloadDialog = null;
//                                    // Open Google play page
//                                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
//                                    try {
//                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
//                                    } catch (android.content.ActivityNotFoundException anfe) {
//                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
//                                    }
//                                }
//                            })
//                            .setCancelText("No")
//                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                                @Override
//                                public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                    sweetAlertDialog.cancel();
//                                    downloadDialog = null;
//                                    // Back to home
//                                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
//                                    homeIntent.addCategory(Intent.CATEGORY_HOME);
//                                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    startActivity(homeIntent);
//                                }
//                            });
//                    downloadDialog.setCancelable(false);
//                    downloadDialog.show();
//                    break;
//                }
//            }
//        }
//
//        @Override
//        public void onDownloadProgress(DownloadProgressInfo info) {
//            if(progress == null)
//                return;         // ignore
//            if(info.mOverallTotal == 0) {
//                progress.setTitleText("Downloading 0%");
//                return;
//            }
//            int percentage = (int) (info.mOverallProgress * 100 / info.mOverallTotal);
//            progress.setTitleText("Downloading " + percentage + "%");
//        }
//    }


    private SweetAlertDialog progress;
    //    private DownloaderClient client;
//    private DownloaderProxy service;
//    private int lastState = -1;
//    private SweetAlertDialog downloadDialog = null;
//
//    private PermissionToken permissionToken = null;
//    private boolean isRequestingPermissions = false;
//    private boolean hasShownPermissionsRationale = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);


        // Manage notification channels required to check download
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(DOWNLOADER_NOTIFICATION_CHANNEL, "Pipe Dreams Downloader", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Additional downloads for Pipe Dreams");
            notificationManager.createNotificationChannel(channel);
        }

        // Immersive mode
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        if (Build.VERSION.SDK_INT >= 28) {
            try {
                //
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                getWindow().setAttributes(params);
            } catch (Throwable e) {
                Log.e(TAG, "Unable to set display cutout mode", e);
            }
        }
        //Set version number text
        //game31.Globals.version = Build.VERSION.RELEASE;
        game31.Globals.version = BuildConfig.VERSION_NAME;
        startGame();
    }

//    private void showPermissionRationaleDialog() {
//        if(isFinishing()) {
//            if(permissionToken != null) {
//                permissionToken.cancelPermissionRequest();
//                permissionToken = null;
//            }
//            isRequestingPermissions = false;
//            return;
//        }
//        if(hasShownPermissionsRationale) {
//            if(permissionToken != null) {
//                permissionToken.continuePermissionRequest();
//                permissionToken = null;
//            }
//            return;
//        }
//        hasShownPermissionsRationale = true;
//        // Show dialog
//        SweetAlertDialog permissionDialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
//                .setTitleText("Permissions Required")
//                .setContentText("We are about to request access to your device storage. This is required to make sure the download was successful.")
//                .setConfirmText("Okay")
//                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                    @Override
//                    public void onClick(SweetAlertDialog sweetAlertDialog) {
//                        sweetAlertDialog.cancel();
//                        // Request permission again
//                        if(permissionToken != null) {
//                            permissionToken.continuePermissionRequest();
//                            permissionToken = null;
//                        }
//                        else
//                            verifyPermissionsGranted();
//                    }
//                });
//        permissionDialog.setCancelable(false);
//        permissionDialog.show();
//    }

//    private void showPermissionsDeniedDialog(final boolean isPermanent) {
//        if(isFinishing()) {
//            isRequestingPermissions = false;
//            return;
//        }
//        String contentText;
//        if(isPermanent)
//            contentText = "You have permanently denied required permissions. You need to manually enable them in the app settings to be able to start. Open Settings?";
//        else
//            contentText = "You have denied required permissions. We cannot start without them. Try again?";
//
//        // Show dialog
//        SweetAlertDialog permissionDialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
//                .setTitleText("Permissions Required")
//                .setContentText(contentText)
//                .setConfirmText("Yes")
//                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                    @Override
//                    public void onClick(SweetAlertDialog sweetAlertDialog) {
//                        sweetAlertDialog.cancel();
//                        if(isPermanent) {
//                            isRequestingPermissions = false;
//                            Intent intent = new Intent();
//                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                            Uri uri = Uri.fromParts("package", getPackageName(), null);
//                            intent.setData(uri);
//                            startActivity(intent);
//                        }
//                        else {
//                            verifyPermissionsGranted();
//                        }
//                    }
//                })
//                .setCancelText("Exit")
//                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                    @Override
//                    public void onClick(SweetAlertDialog sweetAlertDialog) {
//                        sweetAlertDialog.cancel();
//                        isRequestingPermissions = false;
//                        // Didn't allow, go back to home
//                        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
//                        homeIntent.addCategory(Intent.CATEGORY_HOME);
//                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(homeIntent);
//                    }
//                });
//        permissionDialog.setCancelable(false);
//        permissionDialog.show();
//    }

//    private void verifyPermissionsGranted() {
//        if(isFinishing()) {
//            isRequestingPermissions = false;
//            return;
//        }
//        Log.i(TAG, "Checking permissions");
//        isRequestingPermissions = true;
//
//        MultiplePermissionsListener permissionsListener = new MultiplePermissionsListener() {
//            @Override
//            public void onPermissionsChecked(MultiplePermissionsReport report) {
//                Log.i(TAG, "onPermissionsChecked, all granted: " + report.areAllPermissionsGranted() + " | permanently denied: " + report.isAnyPermissionPermanentlyDenied());
//                if (!report.areAllPermissionsGranted()) {
//                    showPermissionsDeniedDialog(report.isAnyPermissionPermanentlyDenied());
//                }
//                else {
//                    Log.i(TAG, "Permissions ok, checking download data");
//                    isRequestingPermissions = false;
//                    checkDataDownloaded();          // Else all permissions were granted, continue to check download data
//                }
//            }
//
//            @Override
//            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
//                Log.i(TAG, "onPermissionRationaleShouldBeShown");
//                // Remember token
//                permissionToken = token;
//                showPermissionRationaleDialog();
//            }
//        };
//        // Check permissions
//        Dexter.withContext(this)
//                .withPermissions(
//                        //Manifest.permission.READ_PHONE_STATE,
//                        //Manifest.permission.WRITE_EXTERNAL_STORAGE
//                )
//                .withListener(permissionsListener)
//                .check();
//    }

    private void startGame() {
        // Schedule start on next loop
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, AndroidLauncher.class));
                if(Build.VERSION.SDK_INT >= 34)
                {
                    overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN,0,0);
                }
                else
                {
                    overridePendingTransition(0, 0);       // no animation
                }
            }
        }, 500);
    }

//    @SuppressLint("StaticFieldLeak")
//    private void verifyGameDataCRC() {
//        final File file = openGameDataFile();
//        if(file == null) {
//            if(AndroidLauncher.GAME_DATA_CRC == 0) {
//                // All files are packed inside apk (for debug)
//                startGame();
//                return;
//            }
//            Log.e(TAG, "Unable to open game data file, checking again");
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    checkDataDownloaded();
//                }
//            });
//            return;
//        }
//
//        // Get actual file size
//        final long fileSize = file.length();
//
//        final SharedPreferences preferences = getSharedPreferences(VERIFY_SHARED_FILENAME, MODE_PRIVATE);
//        int versionCode = preferences.getInt(VERIFY_VERSIONCODE_KEY, -1);
//        long verifiedFileSize = preferences.getLong(VERIFY_FILESIZE_KEY, -1);
//        if(versionCode == BuildConfig.VERSION_CODE && verifiedFileSize == fileSize) {
//            startGame();            // Already checked and file size is the same, assume the correct file and start game
//            return;
//        }
//
//        // Else start async task to recalculate crc
//        showProgressUI();
//        Log.i(TAG, "Performing game data CRC check");
//        new AsyncTask<Object, Object, Object>() {
//            @Override
//            protected Void doInBackground(Object... params) {
//                // Verify game data
//                long crc = calculateCRC(file);
//                if(crc != AndroidLauncher.GAME_DATA_CRC) {
//                    Log.e(TAG, "Game data CRC mismatch, found: " + Long.toHexString(crc) + " required: " + Long.toHexString(AndroidLauncher.GAME_DATA_CRC));
//                    // Delete file and start again
//                    if(AndroidLauncher.GAME_DATA_CRC != 0) {
//                        file.delete();
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                checkDataDownloaded();
//                            }
//                        });
//                        return null;
//                    }
//                }
//                // Else verification passed
//                preferences.edit()
//                        .putLong(VERIFY_VERSIONCODE_KEY, BuildConfig.VERSION_CODE)
//                        .putLong(VERIFY_FILESIZE_KEY, fileSize)
//                        .apply();       // Remember to skip crc checking next time
//                startGame();
//                return null;
//            }
//        }.execute();
//    }

//    private File openGameDataFile() {
//        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
//            return null;         // not mounted
//
//        // Build the full path to the app's expansion files
//        String packageName = getPackageName();
//        File root = Environment.getExternalStorageDirectory();
//        File expPath = new File(root.toString() + EXP_PATH + packageName);
//
//        if (!expPath.exists() || !expPath.isDirectory())
//            return null;          // expansion path not found
//
//        String path = expPath + "/main." + AndroidLauncher.GAME_DATA_VERSION + "." + packageName + ".obb";
//
//        File file = new File(path);
//        if (!file.exists() || !file.isFile())
//            return null;          // main file not found
//        return file;
//    }

//    private long calculateCRC(File file) {
//        try {
//            // Read file
//            InputStream s = new BufferedInputStream(new FileInputStream(file));
//            byte[] buffer = new byte[8 * 1024];        // 8k buffer
//            int read;
//            int total = 0;
//            CRC32 crc = new CRC32();
//            while((read = s.read(buffer)) != -1) {
//                // Calculate crc for this
//                crc.update(buffer, 0, read);
//                total += read;
//            }
//            // Done
//            long hash = crc.getValue();
//            Log.e(TAG, "Hashed " + total + " bytes with crc " + Long.toHexString(hash));
//            return hash;
//        } catch (Throwable e) {
//            // Error
//            Log.e(TAG, "Failed to calculate crc", e);
//            return -1;          // failed
//        }
//    }

//    private void checkDataDownloaded() {
//        // Check for app expansions
//        final byte[] salt = { -3, 6, 12, 64, -74, 125, 42, -12, 5, 126, 122, -15, 5, -7 };
//        final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqNvd1mGwyfLNYrtP0CDxoRoH+tc7/UbmvjSA506U+x7FIB/Y2RxouvWF4hQeSJJwLhVYGFfHm1znPYYD0xv9BcT3pDOYa3URigFSmlsevKZ9EJTOfv0/Vie+Ihh0aKwzMr+o1nkSa1+9Jp695iQiXW+cdfhSs1DVJzBM81yXgUxIkRaez/orlOSIbOXgoB1/AkGS/Oks/YttD4FhMjty//h+8YFUw1BKpBh36ytvtTgbfh1tqD/pFxO/F+xS5UKWppSCjYQHsrJdGjzhnPrylL7kOH3Y85AeeeTVzWfKrQbLoX+8c1Cc+TpTd5kFw9lKvViEfQipWC7WzSiG8pgKEwIDAQAB";
//
//        try {
//            // Check if data already exists
//            if(openGameDataFile() != null || AndroidLauncher.GAME_DATA_CRC == 0) {
//                verifyGameDataCRC();
//                return;
//            }
//
//            // Prepare intent on complete
//            Intent intent = new Intent(this, MainActivity.class);
//            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            int result = DownloaderService.startDownloadServiceIfRequired(this, DOWNLOADER_NOTIFICATION_CHANNEL, pendingIntent, salt, publicKey);
//
//            // Check result
//            if(result == DownloaderService.NO_DOWNLOAD_REQUIRED) {
//                verifyGameDataCRC();
//                return;         // Done
//            }
//
//            // Else download required, followup
//            showProgressUI();
//
//            // Connect to download service
//            service = new DownloaderProxy(this);
//            service.connect();
//
//            // Register client
//            client = new DownloaderClient();
//            client.register(this);
//
//        } catch (Throwable e) {
//            throw new RuntimeException("Unable to prepare game data", e);
//        }
//    }

//    private void showProgressUI() {
//        if(progress != null)
//            return;
//        progress = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE).setTitleText("Checking files");
//        progress.getProgressHelper().setBarColor(Color.parseColor("#000000"));
//        progress.setCancelable(false);
//        progress.show();
//    }

    @Override
    protected void onStart() {
        super.onStart();

//        hasShownPermissionsRationale = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reset
//        lastState = -1;

        Log.i(TAG, "onResumed");

//        // Start process
//        if(!isRequestingPermissions) {
//            // Only check permission if not already requesting
//            verifyPermissionsGranted();
//        }
//        else
//            Log.e(TAG, "Already checking permissions");
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (progress != null) {
            progress.cancel();
            progress = null;
        }

//        if(client != null) {
//            client.unregister(this);
//            client = null;
//        }
//
//        if(service != null) {
//            service.disconnect();
//            service = null;
//        }
    }
}
