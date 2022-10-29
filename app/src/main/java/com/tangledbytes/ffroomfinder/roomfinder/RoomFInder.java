package com.tangledbytes.ffroomfinder.roomfinder;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.EditText;

import com.tangledbytes.ffroomfinder.R;
import com.tangledbytes.ffroomfinder.utils.AppConstants;
import com.tangledbytes.ffroomfinder.utils.Utils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class RoomFInder extends BaseRoomFinder {
    private static final String TAG = "RoomFInder";

//    private static final String PACKAGE_NAME = "com.dts.freefireth";
    private static final String PACKAGE_NAME = "com.dts.freefiremax";

    /////////////////////////////////////////////
    //             Thread Runnable             //
    /////////////////////////////////////////////
    private final Runnable mAutomatorThreadRunnable = new Runnable() {
		private long mLastRefreshTime = -1;
		private int mCountPasswordNotEntered = 0;
		private boolean mHasChangedPlayerPosition = false;

        private void onStart() {
			mLastRefreshTime = -1;
			mCountPasswordNotEntered = 0;
			mHasChangedPlayerPosition = false;
			
            Log.i(TAG, "Thread: Started");
            mIsThreadRunning = true;
        }

        private void onStop() {
            mIsThreadRunning = false;
            removeOverlay();
            onAutomatorThreadStopped();
            Log.i(TAG, "Thread: Finished!");
        }

        @Override
        public void run() {
            onStart();
			
            while (continueThread()) {
				if (System.currentTimeMillis() - mLastRefreshTime >= 30_000)
					refreshRooms();
				int[] passwords = {1};

				for (int password : passwords) {
					if (!continueThread())
						break;
					enterPassword(password);
				}

				// Scroll a little down
				performGesture(600, 300, 600, 460, 550);
            }
            onStop();
        }

		private void enterPassword(int password) {
			// Open
            clickAt(500, 500, 200);
            // Join
            clickAt(600, 530, 200);
            // Click on enter password
            clickAt(830, 340, 200);

            // Enter password
			boolean hasEnteredPassword = false;
			for (AccessibilityNodeInfo node : getWidget(getRootInActiveWindow(), EditText.class)) {
				Bundle arguments = new Bundle();
				arguments.putString(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
									String.valueOf(password));
				node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
				hasEnteredPassword = true;
				mHasChangedPlayerPosition = false;
			}
			if (hasEnteredPassword) mCountPasswordNotEntered = 0;
			else mCountPasswordNotEntered++;

			if (mCountPasswordNotEntered >= 4) {
				if (!mHasChangedPlayerPosition) {
					changePlayersPosition();
					mHasChangedPlayerPosition = true;
				}
				partialSleep(10);
				mLastRefreshTime = System.currentTimeMillis() - 30_000;
			}

			// Close edittext
			sleep(200);
			for (AccessibilityNodeInfo node : getWidget(getRootInActiveWindow(), Button.class))
				node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
			sleep(150);
			// Confirm
			clickAt(830, 530, 500);
			// Ok (password incorrect)
			clickAt(950, 460, 200);
		}

		private void changePlayersPosition() {
			// Click at -> slot 1, 4
			clickAt(965, 295, 400);

			// confirm
			clickAt(810, 520, 400);

			// Click at -> slot 2, 4
			clickAt(1350, 290, 400);

			// confirm
			clickAt(810, 520, 200);
		}

		private void refreshRooms() {
			// Refresh rooms
			clickAt(1060, 100, 200);
			mLastRefreshTime = System.currentTimeMillis();

			for (int i = 0; i < 13 && continueThread(); i++)
				performGesture(600, 550, 600, 150, 450);
			sleep(150);
		}

		public List<AccessibilityNodeInfo> getWidget(AccessibilityNodeInfo fromNode, Class<?> widget) {
			if (fromNode == null)
				return null;

			List<AccessibilityNodeInfo> editTextNodes = new ArrayList<>();
			for (int i = 0; i < fromNode.getChildCount(); i++) {
				AccessibilityNodeInfo node = fromNode.getChild(i);
				if (node == null)
					continue;
				if (node.getClassName().equals(widget.getCanonicalName()))
					editTextNodes.add(node);
				else if (node.getChildCount() > 0)
					editTextNodes.addAll(getWidget(node, widget));
			}
			return editTextNodes;
		}

        public boolean continueThread() {
            return mContinueThread;
        }

        /**
         * Handy method for waiting.
         * @param millis Waiting time
         */
        private void sleep(long millis) {
            try {
                synchronized (this) {
                    wait(millis);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "sleep(Object, long): Failed to wait", e);
            }
        }

        /**
         * This method is very useful if you want to wait for a few seconds but also want
         * to check whether we are still in game and user had not asked us to stop the automation.
         *
         * @param secs Number of seconds you want to wait
         */
        private void partialSleep(int secs) {
            try {
                synchronized (this) {
                    while (mContinueThread && secs > 0) {
                        wait(1000);
                        secs--;
                    }
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "partialSleep(int): Failed to wait, secs=" + secs, e);
            }	
        }
    };
    /////////////////////////////////////////////

    private AccessibilityEvent mLastEvent;
    private RoomFinderUtils mAutomatorUtils;
    private Button mBtnStartRoomFinder;
	private Button mBtnPauseRoomFinder;
    private Button mBtnStopRoomFinder;
    private Handler mHandler;
    private String mCurrentActivity;
    private Thread mAutomatorThread = null;
    private View mLayout;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private boolean mContinueThread = false;
    private boolean mIsThreadRunning = false;

    public RoomFInder(AccessibilityService service) {
        super(service);
        mHandler = new Handler(Looper.getMainLooper());
        mAutomatorUtils = new RoomFinderUtils(this);
    }

    public RoomFInder(Context context) {
        super(context);
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onStart() {
        if (!hasOverlay()) {
            launchApp(getPackageName());
            createOverlay();
        }
    }

    @Override
    public void onEvent(AccessibilityEvent event) {
        mLastEvent = event;
        RoomFinderUtils.debugClick(mLastEvent);
        String currentActivity = mAutomatorUtils.getCurrentActivity(event);
        if (currentActivity != null)
            mCurrentActivity = currentActivity;
    }

    @Override
    public String getPackageName() {
        return PACKAGE_NAME;
    }

    private void startAutomatorThread() {
        if (!mIsThreadRunning) {
            mContinueThread = true;
            mAutomatorThread = new Thread(mAutomatorThreadRunnable);
            mAutomatorThread.setPriority(Thread.MAX_PRIORITY);
            mAutomatorThread.start();
            showPauseBtn();
        } else {
            showToast("Automator has already been started!");
        }
    }

	private void pauseAutomatorThread() {
		stopAutomatorThread();
		new Thread(new Runnable() {
				@Override
				public void run() {
					while (mIsThreadRunning) sleep(100);
					final Intent intent = new Intent();
					intent.setAction(AppConstants.AUTOMATOR_START);
					getService().sendBroadcast(intent);
				}
			}).start();
	}

    private void stopAutomatorThread() {
        if (mIsThreadRunning) {
            if (mContinueThread) {
                mContinueThread = false;
                disableStopPauseBtns();
            }
        } else {
			onAutomatorThreadStopped();
		}
    }

    /**
     * Called when thread is fianlly stopped.
     */
    private void onAutomatorThreadStopped() {
        removeOverlay();
        showToast("Stopped!");
    }

    /**
     * Shows 'stop and pause' buttons and hides 'start' button.
     *
     */
    public void showPauseBtn() {
        if (mBtnStartRoomFinder != null && mBtnPauseRoomFinder != null) {
            mBtnStartRoomFinder.setVisibility(View.GONE);
            mBtnPauseRoomFinder.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Makes 'stop' button (that is on overlay) disabled.
     */
    public void disableStopPauseBtns() {
        if (mBtnStopRoomFinder != null)
            mBtnStopRoomFinder.setEnabled(false);
		if (mBtnPauseRoomFinder != null)
			mBtnPauseRoomFinder.setEnabled(false);
    }

    /**
     * Checks whether we have added overlay or not.
     */
    private boolean hasOverlay() {
        return (mLayout != null && mLayout.getParent() != null);
    }

    /**
     * Creates overlay over screen.
     */
    private void createOverlay() {
        if (mLayout == null || !hasOverlay()) {
            mHandler.post(() -> {
                mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                mLayoutParams = new WindowManager.LayoutParams();
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                mLayoutParams.format = PixelFormat.TRANSPARENT;
                mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                mLayoutParams.gravity = Gravity.TOP | Gravity.CENTER;
                final LayoutInflater inflater = LayoutInflater.from(getContext());
                mLayout  = inflater.inflate(R.layout.layout_overlay, null);
                mWindowManager.addView(mLayout, mLayoutParams);
                mBtnStartRoomFinder = mLayout.findViewById(R.id.start_room_finder);
                mBtnPauseRoomFinder = mLayout.findViewById(R.id.pause_room_finder);
                mBtnStopRoomFinder = mLayout.findViewById(R.id.stop_room_finder);
                mBtnStartRoomFinder.setOnClickListener(view -> startAutomatorThread());
                mBtnPauseRoomFinder.setOnClickListener(view -> pauseAutomatorThread());
                mBtnStopRoomFinder.setOnClickListener(view -> stopAutomatorThread());
            });
        }
    }

    /**
     * Removes overlay from screen.
     */
    private void removeOverlay() {
        if (mLayout != null && hasOverlay()) {
            mHandler.post(() -> mWindowManager.removeView(mLayout));
        }
    }

    /**
     * Handy method for showing toasts, even from another thread.
     */
    private void showToast(final String msg) {
        mHandler.post(() -> Utils.showToast(getService(), msg));
    }
}
