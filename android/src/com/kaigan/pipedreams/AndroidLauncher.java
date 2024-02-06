package com.kaigan.pipedreams;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowManager;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidGraphics;
import com.badlogic.gdx.backends.android.AndroidZipFileHandle;
import com.badlogic.gdx.files.FileHandle;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LevelEndEvent;
import com.crashlytics.android.answers.LevelStartEvent;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.InternalOfferwallListener;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;

import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;
import game31.Game;
import game31.Globals;
import game31.VoiceProfile;
import io.fabric.sdk.android.Fabric;
import sengine.Sys;
import sengine.audio.Audio;
import sengine.graphics2d.Fonts;
import sengine.graphics2d.texturefile.TextureLoader;
import sengine.mass.MassFile;

public class AndroidLauncher extends AndroidApplication implements Game.PlatformInterface, BillingProcessor.IBillingHandler {
	private static final String TAG = "AndroidLauncher";

	private static String resolveAchievementID(Globals.Achievement achievement) {
		switch (achievement) {
            case INSTALLED_FB:
                return "CgkIjr_FtfoFEAIQAQ";
            case FIRST_FB_WIN:
                return "CgkIjr_FtfoFEAIQAg";
            case SECOND_FB_WIN:
                return "CgkIjr_FtfoFEAIQAw";
            case SHOWDOWN_WIN:
                return "CgkIjr_FtfoFEAIQBA";
            case SHOWDOWN_GIVEUP:
                return "CgkIjr_FtfoFEAIQBQ";
            case TEDDY_VLOGS:
                return "CgkIjr_FtfoFEAIQBg";
            case IRIS_ADS:
                return "CgkIjr_FtfoFEAIQBw";
            case TEDDY_WEBCAM_VIDEOS:
                return "CgkIjr_FtfoFEAIQCA";
            case ARG_PHONECALL:
                return "CgkIjr_FtfoFEAIQCQ";
            case ARG_FRONT_WEBSITE:
                return "CgkIjr_FtfoFEAIQCg";
            case ARG_GATEWAY_WEBSITE:
                return "CgkIjr_FtfoFEAIQCw";
            case SHARED_FB_TO_ALL:
                return "CgkIjr_FtfoFEAIQDA";
            case GAMEMAKER:
                return "CgkIjr_FtfoFEAIQDQ";
            case DIE_IN_FB:
                return "CgkIjr_FtfoFEAIQDg";
            case SCORED_IN_FB:
                return "CgkIjr_FtfoFEAIQDw";
            case BAD_HEALTH:
                return "CgkIjr_FtfoFEAIQEA";
            case ABSTAIN_FROM_CHOICE:
                return "CgkIjr_FtfoFEAIQEQ";
            case LIFEHOURS_DRAINED:
                return "CgkIjr_FtfoFEAIQEg";
            case SECRET_DEMON_CALLS:
                return "CgkIjr_FtfoFEAIQEw";
            case CHEATED_FB:
                return "CgkIjr_FtfoFEAIQFA";
            case NAME_TEDDY:
                return "CgkIjr_FtfoFEAIQFQ";
            case NAME_EXPLETIVE:
                return "CgkIjr_FtfoFEAIQFg";
            case NAME_YOUTUBER:
                return "CgkIjr_FtfoFEAIQFw";
            case REVIVE_IN_FB:
                return "CgkIjr_FtfoFEAIQGA";
            case JETSTREAM_IN_FB:
                return "CgkIjr_FtfoFEAIQGQ";
            case ENDING_BOTH_DIE:
                return "CgkIjr_FtfoFEAIQGg";
            case ENDING_TEDDY_DIES:
                return "CgkIjr_FtfoFEAIQGw";
            case ENDING_PLAYER_DIES:
                return "CgkIjr_FtfoFEAIQHA";
            case ENDING_BOTH_SURVIVE:
                return "CgkIjr_FtfoFEAIQHQ";
			default:
				return null;
		}
	}

	static final int GAME_DATA_VERSION = 44;
	static final long GAME_DATA_CRC = 0x2e026681L; // 0x3b42264L; // version 44

	public static int hdpiHeightThreshold = 1500;

	// Google Play Activity Request Codes
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_ACHIEVEMENT_UI = 9002;

    private static final String GOOGLE_IAP_REMOVE_ADS = "remove_ads";

    // Google game center
	private GoogleSignInClient mGoogleSignInClient;
	private SnapshotsClient mSnapshotsClient;
	private AchievementsClient mAchievementsClient;
    // The currently signed in account, used to check the account has changed outside of this activity when resuming.
    private GoogleSignInAccount mSignedInAccount = null;

	private long saveGameTime = 0;
	private long saveGameTimeStarted = 0;

	private SweetAlertDialog updateRequiredDialog = null;

	// Billing
    private BillingProcessor billingProcessor;
    private boolean hasRemovedAds = false;


    private void startSignInIntent() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    private void signInSilently() {
        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful())
                            onConnected(task.getResult());
                        else
                            onDisconnected();
                    }
                });
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        mSignedInAccount = googleSignInAccount;
        mSnapshotsClient = Games.getSnapshotsClient(this, googleSignInAccount);
        mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount);
        reportLog(TAG, "Google login done, opening save game");
        final FileHandle saveFile = Gdx.files.local(Globals.SAVE_FILENAME);
        mSnapshotsClient.open(Globals.SAVE_FILENAME, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        reportLogError(TAG, "Error opening snapshot: " + Globals.SAVE_FILENAME, e);
                        informLoginDone(true);      // not sure should be true or not, but has logged in
                    }
                })
                .continueWith(new Continuation<SnapshotsClient.DataOrConflict<Snapshot>, byte[]>() {
                    @Override
                    public byte[] then(@NonNull Task<SnapshotsClient.DataOrConflict<Snapshot>> task) {
                        Snapshot snapshot = task.getResult().getData();

                        // Opening the snapshot was a success and any conflicts have been resolved.
                        try {
                            // Extract the raw data from the snapshot.
                            return snapshot.getSnapshotContents().readFully();
                        } catch (IOException e) {
                            reportLogError(TAG, "Error reading snapshot: " + Globals.SAVE_FILENAME, e);
                        }
                        return null;
                    }
                }).addOnCompleteListener(new OnCompleteListener<byte[]>() {
            @Override
            public void onComplete(@NonNull Task<byte[]> task) {
                try {
                    byte[] data = task.getResult();
                    if (data != null && data.length > 0 && !saveFile.exists()) {
                        reportLog(TAG, "Using cloud save");
                        saveFile.writeBytes(data, false);            // Only overwrite if there is no local save game
                    }
                } catch (Throwable e) {
                    reportLogError(TAG, "Failed to load cloud save", e);
                }
                informLoginDone(true);      // Done login
            }
        });
    }

    private void onDisconnected() {
        mSnapshotsClient = null;
        mSignedInAccount = null;
        mAchievementsClient = null;
        informLoginDone(false);      // Login failed
    }

    private boolean isSignedIn() {
        return mSnapshotsClient != null;
    }


    private void initializeAds() {
        // Prepare callbacks
        IronSource.setRewardedVideoListener(new RewardedVideoListener() {
            @Override
            public void onRewardedVideoAdOpened() {
                // ignored
            }

            @Override
            public void onRewardedVideoAdClosed() {
                // Show reward
                Globals.grid.postMessage(new Runnable() {
                    @Override
                    public void run() {
                        if (Globals.grid.flapeeBirdApp != null) {
                            if (Globals.grid.flapeeBirdApp.queuedReward() > 0)
                                Globals.grid.flapeeBirdApp.showRewardMenu();
                            else {
                                Sys.error(TAG, "No reward found, showing fake ad");
                                Globals.grid.flapeeBirdApp.adScreen.show(false);
                                Globals.grid.flapeeBirdApp.adScreen.open(false);
                                Globals.grid.flapeeBirdApp.queueReward(30);
                            }
                        }
                    }
                });
            }

            @Override
            public void onRewardedVideoAvailabilityChanged(boolean b) {
                // ignored
            }

            @Override
            public void onRewardedVideoAdStarted() {
                // ignored
            }

            @Override
            public void onRewardedVideoAdEnded() {
                // ignored
            }

            @Override
            public void onRewardedVideoAdRewarded(Placement placement) {
                try {
                    final int credits = placement.getRewardAmount();
                    reportLog(TAG, "Received rewarded video credits: " + credits);
                    if(credits > 0) {
                        Globals.grid.postMessage(new Runnable() {
                            @Override
                            public void run() {
                                Globals.grid.flapeeBirdApp.queueReward(credits);
                            }
                        });
                    }
                } catch (Throwable e) {
                    reportLogError(TAG, "Failed to process rewarded video credits", e);
                }
            }

            @Override
            public void onRewardedVideoAdShowFailed(IronSourceError ironSourceError) {
                reportLogError(TAG, "onRewardedVideoAdShowFailed" + ironSourceError);
                onRewardedVideoAdClosed();      // TODO: not sure if this is needed
            }

            @Override
            public void onRewardedVideoAdClicked(Placement placement) {
                // ignored
            }
        });
        IronSource.setInterstitialListener(new InterstitialListener() {
            @Override
            public void onInterstitialAdReady() {
                // ignored
            }

            @Override
            public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
                reportLogError(TAG, "onInterstitialAdLoadFailed: " + ironSourceError);
            }

            @Override
            public void onInterstitialAdOpened() {
                // ignored
            }

            @Override
            public void onInterstitialAdClosed() {
                // Load another
                IronSource.loadInterstitial();

                // Show menu
                Globals.grid.postMessage(new Runnable() {
                    @Override
                    public void run() {
                        if (Globals.grid.flapeeBirdApp != null)
                            Globals.grid.flapeeBirdApp.showMenu(true);
                    }
                });
            }

            @Override
            public void onInterstitialAdShowSucceeded() {
                // ignored
            }

            @Override
            public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {
                reportLogError(TAG, "onInterstitialAdShowFailed: " + ironSourceError);
                onInterstitialAdClosed();      // TODO: not sure if this is needed
            }

            @Override
            public void onInterstitialAdClicked() {
                // ignored
            }
        });
        IronSource.setOfferwallListener(new InternalOfferwallListener() {
            @Override
            public void onOfferwallAvailable(boolean b, IronSourceError ironSourceError) {
                // ignored
            }

            @Override
            public void onOfferwallAvailable(boolean b) {
                // ignored
            }

            @Override
            public void onOfferwallOpened() {
                // ignored
            }

            @Override
            public void onOfferwallShowFailed(IronSourceError ironSourceError) {
                reportLogError(TAG, "onOfferwallShowFailed: " + ironSourceError);
                onOfferwallAdCredited(0, 0, true);      // TODO: not sure if this is needed
            }

            @Override
            public boolean onOfferwallAdCredited(int credits, int totalCredits, boolean totalCreditsFlag) {
                if(totalCreditsFlag)
                    credits = 0;            // ignore credits as it can't be determined if these are from a previous installation
                final int creditsFinal = credits;

                Globals.grid.postMessage(new Runnable() {
                    @Override
                    public void run() {
                        if (Globals.grid.flapeeBirdApp != null) {
                            if (creditsFinal > 0) {
                                Globals.grid.flapeeBirdApp.queueReward(creditsFinal);
                                Globals.grid.flapeeBirdApp.showRewardMenu();
                            }
                            else
                                Globals.grid.flapeeBirdApp.showMenu(true);
                        }
                    }
                });

                // Absorb any
                return true;
            }

            @Override
            public void onGetOfferwallCreditsFailed(IronSourceError ironSourceError) {
                reportLogError(TAG, "onGetOfferwallCreditsFailed: " + ironSourceError);
                onOfferwallAdCredited(0, 0, true);      // TODO: not sure if this is needed
            }

            @Override
            public void onOfferwallClosed() {
                // Process offerwall credits
                IronSource.getOfferwallCredits();
            }
        });

        // Initialize
        IronSource.init(this, "7ca3255d", IronSource.AD_UNIT.REWARDED_VIDEO, IronSource.AD_UNIT.OFFERWALL, IronSource.AD_UNIT.INTERSTITIAL);
        IronSource.loadInterstitial();
    }

    @Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Fabric.with(this, new Answers(), new Crashlytics());            // TODO: move to application

		// Prepare sign in client
		mGoogleSignInClient = GoogleSignIn.getClient(this,
				new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
					.requestScopes(Drive.SCOPE_APPFOLDER, Games.SCOPE_GAMES_LITE)		// For saved games
					.build()
		);

		// Ads
		initializeAds();

		// Initialize android
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useImmersiveMode = true;

		// Check for 4K fonts
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		if(displayMetrics.heightPixels >= hdpiHeightThreshold)
			Fonts.resolutionMultiplier = 2;         // For 4K

		// Request to overlap with display cutout
		if (Build.VERSION.SDK_INT >= 28) {
			try {
				//
				WindowManager.LayoutParams params = getWindow().getAttributes();
				params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
				getWindow().setAttributes(params);
			} catch (Throwable e) {
                reportLogError(TAG, "Unable to set display cutout mode", e);
			}
		}

		// Platform
        VideoMaterialProvider.init();

		// Create game
		Game game = new Game(this);

		// Start
		initialize(game.applicationListener, config);

		// Link expansion files
        files.setAPKExpansion(GAME_DATA_VERSION, -1);			// TODO Monitor the result

        // Compatibility fixes
        // This is necessary to check if the expansion file contains the file first, fix for motorola and older samsung devices
        sengine.File.customFileSource = new sengine.File.CustomFileSource() {
            @Override
            public FileHandle open(String path) {
                AndroidZipFileHandle zipFileHandle = new AndroidZipFileHandle(path);
                if(zipFileHandle.isDirectory())
                    return null;        // only intended for files
                return zipFileHandle;
            }
        };

        // Special cases
        String device = android.os.Build.MODEL + " " + android.os.Build.MANUFACTURER + " " + android.os.Build.PRODUCT;
        if(device.toLowerCase().contains("motorola")) {
            // Synchronized IO... for crashing motorola devices
            TextureLoader.synchronizedIO = true;
            Audio.synchronizedIO = true;
        }

		// Screen dimming
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Billing Processor
        billingProcessor = new BillingProcessor(this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqNvd1mGwyfLNYrtP0CDxoRoH+tc7/UbmvjSA506U+x7FIB/Y2RxouvWF4hQeSJJwLhVYGFfHm1znPYYD0xv9BcT3pDOYa3URigFSmlsevKZ9EJTOfv0/Vie+Ihh0aKwzMr+o1nkSa1+9Jp695iQiXW+cdfhSs1DVJzBM81yXgUxIkRaez/orlOSIbOXgoB1/AkGS/Oks/YttD4FhMjty//h+8YFUw1BKpBh36ytvtTgbfh1tqD/pFxO/F+xS5UKWppSCjYQHsrJdGjzhnPrylL7kOH3Y85AeeeTVzWfKrQbLoX+8c1Cc+TpTd5kFw9lKvViEfQipWC7WzSiG8pgKEwIDAQAB", this);
        billingProcessor.initialize();
	}

	@Override
	protected void onResume() {
		super.onResume();

		saveGameTimeStarted = System.currentTimeMillis();

		// Sign in silently if possible
        signInSilently();

        // Ads
        IronSource.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		saveGameTime += System.currentTimeMillis() - saveGameTimeStarted;

		// Ads
        IronSource.onPause(this);
	}

    @Override
    protected void onDestroy() {
        billingProcessor.release();

        super.onDestroy();
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(billingProcessor.handleActivityResult(requestCode, resultCode, data))
            return;

        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                onConnected(account);
            } catch (ApiException apiException) {
                reportLogError(TAG, "Failed to sign in", apiException);

                onDisconnected();
            }
        }

		super.onActivityResult(requestCode, resultCode, data);
	}

	// Crashlytics
	@Override
	public void reportLog(String source, String text) {
		Crashlytics.log(source + ": " + text);
		Log.i(source, text);
	}

	@Override
	public void reportLog(String source, String text, Throwable exception) {
		Crashlytics.log(source + ": " + text);
		Crashlytics.logException(exception);
		Log.i(source, text, exception);
	}

	@Override
	public void reportLogDebug(String source, String text) {
		Crashlytics.log(source + ": " + text);
		Log.d(source, text);
	}

	@Override
	public void reportLogDebug(String source, String text, Throwable exception) {
		Crashlytics.log(source + ": " + text);
		Crashlytics.logException(exception);
		Log.d(source, text, exception);
	}

	@Override
	public void reportLogError(String source, String text) {
		Crashlytics.log(source + ": " + text);
		Log.e(source, text);
	}

	@Override
	public void reportLogError(String source, String text, Throwable exception) {
		Crashlytics.log(source + ": " + text);
		Crashlytics.logException(exception);
		Log.e(source, text, exception);
	}

	@Override
	public VoiceProfile createTextVoice(String path) {
		throw new RuntimeException("Unsupported on Android, desktop version only!");
	}

	@SuppressLint("NewApi")
	@Override
	public void linkGameData() {
		if (Build.VERSION.SDK_INT >= 28) {
			try {
				// Requested display cutouts to overlap, request metrics
				View view = ((AndroidGraphics) Gdx.graphics).getView();
				DisplayCutout cutout = view.getRootWindowInsets().getDisplayCutout();
				if (cutout != null) {
					Globals.topSafeAreaInset = cutout.getSafeInsetTop();
					Globals.bottomSafeAreaInset = cutout.getSafeInsetBottom();
					reportLog(TAG, "Using safe area insets " + Globals.topSafeAreaInset + ", " + Globals.bottomSafeAreaInset);
				}
			} catch (Throwable e) {
                reportLogError(TAG, "Unable to determine safe area insets", e);
				Globals.topSafeAreaInset = 0;
				Globals.bottomSafeAreaInset = 0;
			}
		}

		// nothing
	}


	// Cloud saves and achievements
	@Override
	public void prepareSaveGame() {
		informLoginDone(true);						// No need to sync with server, trust local for speed
	}

	@Override
	public boolean existsSaveGame() {
		return Gdx.files.local(Globals.SAVE_FILENAME).exists();
	}

	@Override
	public void writeSaveGame(MassFile save) {
		final FileHandle saveFile = Gdx.files.local(Globals.SAVE_FILENAME);
		save.save(saveFile);
		if(isSignedIn()) {
            mSnapshotsClient.open(Globals.SAVE_FILENAME, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            reportLogError(TAG, "Error opening snapshot for saving: " + Globals.SAVE_FILENAME, e);
                        }
                    })
                    .continueWithTask(new Continuation<SnapshotsClient.DataOrConflict<Snapshot>, Task<SnapshotMetadata>>() {
                        @Override
                        public Task<SnapshotMetadata> then(@NonNull Task<SnapshotsClient.DataOrConflict<Snapshot>> task) {
                            Snapshot snapshot = task.getResult().getData();
                            snapshot.getSnapshotContents().writeBytes(saveFile.readBytes());

                            long playedTime = snapshot.getMetadata().getPlayedTime();
                            long currentTimeMillis = System.currentTimeMillis();
                            saveGameTime += currentTimeMillis - saveGameTimeStarted;
                            saveGameTimeStarted = currentTimeMillis;
                            playedTime += saveGameTime;
                            saveGameTime = 0;

                            SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                                    .setDescription("Last save game for Pipe Dreams")
                                    .setPlayedTimeMillis(playedTime)
                                    .build();


                            return mSnapshotsClient.commitAndClose(snapshot, metadataChange);
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<SnapshotMetadata>() {
                        @Override
                        public void onComplete(@NonNull Task<SnapshotMetadata> task) {
                            try {
                                reportLog(TAG, "Saved to cloud: " + task.getResult(Throwable.class).getLastModifiedTimestamp());
                            } catch (Throwable e) {
                                reportLogError(TAG, "Failed to save to cloud", e);
                            }
                        }
                    });
		}
	}

	@Override
	public MassFile readSaveGame() {
		FileHandle saveFile = Gdx.files.local(Globals.SAVE_FILENAME);
		if(saveFile.exists()) {
			MassFile save = new MassFile();
			save.load(saveFile);
			return save;
		}
		return null;
	}

	@Override
	public void deleteSaveGame() {
		// Delete save game
		Gdx.files.local(Globals.SAVE_FILENAME).delete();

        if(isSignedIn()) {
            mSnapshotsClient.open(Globals.SAVE_FILENAME, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            reportLogError(TAG, "Error opening snapshot for saving: " + Globals.SAVE_FILENAME, e);
                        }
                    })
                    .continueWithTask(new Continuation<SnapshotsClient.DataOrConflict<Snapshot>, Task<String>>() {
                        @Override
                        public Task<String> then(@NonNull Task<SnapshotsClient.DataOrConflict<Snapshot>> task) {
                            Snapshot snapshot = task.getResult().getData();
                            return mSnapshotsClient.delete(snapshot.getMetadata());
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            try {
                                reportLog(TAG, "Deleted cloud save: " + task.getResult(Throwable.class));
                            } catch (Throwable e) {
                                reportLogError(TAG, "Failed to delete cloud save", e);
                            }
                        }
                    });
        }

        // Reset game time
		saveGameTime = 0;
		saveGameTimeStarted = System.currentTimeMillis();
	}

	@Override
	public boolean showGameCenter() {
		return true;			// Always show
	}

	@Override
	public boolean promptGameCenterLogin() {
		return !isSignedIn();
	}

	@Override
	public void openGameCenter() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// If connected, show achievements screen
                if(isSignedIn()) {
                    Games.getAchievementsClient(AndroidLauncher.this, mSignedInAccount)
                            .getAchievementsIntent()
                            .addOnSuccessListener(new OnSuccessListener<Intent>() {
                                @Override
                                public void onSuccess(Intent intent) {
                                    startActivityForResult(intent, RC_ACHIEVEMENT_UI);
                                }
                            });
                }
			}
		});
	}

	@Override
	public void loginGameCenter() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Start login as not yet logged in
                startSignInIntent();
			}
		});
	}

	@Override
	public void unlockAchievement(final Globals.Achievement achievement) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
			    if(isSignedIn()) {
			        // Unlock achievement
                    String id = resolveAchievementID(achievement);
                    if (id == null) {
                        Sys.error(TAG, "Unable to resolve achievement ID: " + achievement.name());
                        return;
                    }
                    mAchievementsClient.unlock(id);
                }
			}
		});
	}

	@Override
	public void processCallbacks() {
		// nothing
	}

	// Ads
    @Override
    public boolean showRewardedVideoAd() {
        if(IronSource.isRewardedVideoAvailable()) {
            reportLog(TAG, "Showing Rewarded Video Ad");
            IronSource.showRewardedVideo();
            return true;
        }
        else if(IronSource.isOfferwallAvailable()) {
            reportLog(TAG, "Showing Offerwall Ads");
            IronSource.showOfferwall();
            return true;
        }
        else
            return false;
    }

    @Override
    public boolean showInterstitialAd() {
        if(IronSource.isInterstitialReady()) {
            reportLog(TAG, "Showing Interstitial Ads");
            IronSource.showInterstitial();
            return true;
        }
        else
            return false;
    }


    // Analytics
    @Override
    public void analyticsStartLevel(String name) {
        try {
            Answers.getInstance().logLevelStart(new LevelStartEvent()
                    .putLevelName(name)
            );
        } catch (Throwable e) {
            reportLogError(TAG, "analyticsStartLevel failed: " + name, e);
        }
    }

    @Override
    public void analyticsEndLevel(String name, int score, boolean success) {
        try {
            LevelEndEvent event = new LevelEndEvent()
                    .putLevelName(name);
            if(score != -1) {
                event.putScore(score);
                event.putSuccess(success);
            }

            Answers.getInstance().logLevelEnd(new LevelEndEvent());
        } catch (Throwable e) {
            reportLogError(TAG, "analyticsEndLevel failed: " + name + " " + score + " " + success, e);
        }
    }

    @Override
    public void analyticsEvent(String name) {
        try {
            Answers.getInstance().logCustom(new CustomEvent(name));
        } catch (Throwable e) {
            reportLogError(TAG, "analyticsEvent failed: " + name, e);
        }
    }

    @Override
    public void analyticsView(String name, String type, String id) {
        try {
            Answers.getInstance().logContentView(new ContentViewEvent()
                    .putContentName(name)
                    .putContentType(type)
                    .putContentId(id)
            );
        } catch (Throwable e) {
            reportLogError(TAG, "analyticsView failed: " + name + " " + type + " " + id, e);
        }
    }

    @Override
    public void analyticsValue(String name, String field, float value) {
        try {
            CustomEvent event = new CustomEvent(name)
                    .putCustomAttribute(field, value);
            Answers.getInstance().logCustom(event);
        } catch (Throwable e) {
            reportLogError(TAG, "analyticsValue failed: " + name + " " + field + " " + value, e);
        }
    }

    @Override
    public void analyticsString(String name, String field, String value) {
        try {
            CustomEvent event = new CustomEvent(name)
                    .putCustomAttribute(field, value);
            Answers.getInstance().logCustom(event);
        } catch (Throwable e) {
            reportLogError(TAG, "analyticsString failed: " + name + " " + field + " " + value, e);
        }
    }

    // Promotion
    @Override
    public void openSimulacraAppPage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.kaigan.simulacra")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.kaigan.simulacra")));
                }
            }
        });
    }

	@Override
	public void openReviewPage() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
				try {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
				} catch (android.content.ActivityNotFoundException anfe) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
				}
			}
		});
	}

    @Override
    public void exitGame() {
        // not used
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                moveTaskToBack(true);
            }
        });
    }

    @Override
	public void destroyed() {
		// nothing
	}

	@Override
	public void setWindowed() {
		// nothing
	}


    private void informLoginDone(final boolean success) {
		if(Globals.grid == null)
			return;		// Failed to login before game starts, ignore as we will inform done login later anyway

//        if(System.currentTimeMillis() > 1550275199000L) {
//            // TODO Friday, February 15, 2019 11:59:59 PM
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    // Expiry check
//                    if(updateRequiredDialog != null)
//                        updateRequiredDialog.dismiss();
//                    updateRequiredDialog = new SweetAlertDialog(AndroidLauncher.this, SweetAlertDialog.ERROR_TYPE)
//                            .setTitleText("Update Required")
//                            .setContentText("Unable to check files, please update the app. Open Google Play?")
//                            .setConfirmText("Yes")
//                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                                @Override
//                                public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                    sweetAlertDialog.cancel();
//                                    updateRequiredDialog = null;
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
//                                    updateRequiredDialog = null;
//                                    // Back to home
//                                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
//                                    homeIntent.addCategory(Intent.CATEGORY_HOME);
//                                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    startActivity(homeIntent);
//                                }
//                            });
//                    updateRequiredDialog.setCancelable(false);
//                    updateRequiredDialog.show();
//                }
//            });
//            return;
//        }


		Globals.grid.postMessage(new Runnable() {
			@Override
			public void run() {
				// Inform done login
				Globals.grid.mainMenu.doneLogin(success, existsSaveGame());		// Inform done with login
			}
		});
	}

	// Billing

    @Override
    public void removeAds() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                billingProcessor.purchase(AndroidLauncher.this, GOOGLE_IAP_REMOVE_ADS);
            }
        });
    }

    @Override
    public void checkRemovedAds() {
        if(hasRemovedAds) {
            Globals.grid.postMessage(new Runnable() {
                @Override
                public void run() {
                    Sys.info(TAG, "Purchased removed ads");
                    Globals.grid.mainMenu.informHasRemovedAds();
                }
            });
        }

    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        if(productId.equals(GOOGLE_IAP_REMOVE_ADS)) {
            hasRemovedAds = true;
            checkRemovedAds();
        }
    }

    @Override
    public void onPurchaseHistoryRestored() {
        Log.d(TAG, "onPurchaseHistoryRestored");
        for(String sku : billingProcessor.listOwnedProducts())
            Log.d(TAG, "onPurchaseHistoryRestored owned: " + sku);
        if(billingProcessor.listOwnedProducts().contains(GOOGLE_IAP_REMOVE_ADS))
            hasRemovedAds = true;
    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        Log.e(TAG, "onBillingError " + errorCode, error);
    }

    @Override
    public void onBillingInitialized() {
        Log.d(TAG, "onBillingInitialized");
        // Restore purchased
        billingProcessor.loadOwnedPurchasesFromGoogle();
        onPurchaseHistoryRestored();
    }
}
