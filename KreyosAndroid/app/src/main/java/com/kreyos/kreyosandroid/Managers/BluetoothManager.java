package com.kreyos.kreyosandroid.managers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Message;
import android.os.Handler;
import android.util.Log;

import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.activities.MainActivity;
import com.kreyos.kreyosandroid.bluetooth.BluetoothAgent;
import com.kreyos.kreyosandroid.bluetooth.KreyosService;
import com.kreyos.kreyosandroid.bluetooth.StackActivitiesService;
import com.kreyos.kreyosandroid.bluetooth.Protocol;
import com.kreyos.kreyosandroid.dataobjects.ActivityData;
import com.kreyos.kreyosandroid.listeners.IBluetoothListener;
import com.kreyos.kreyosandroid.objectdata.TodayActivity;
import com.kreyos.kreyosandroid.utilities.Constants;
import com.kreyos.kreyosandroid.utilities.KreyosUtility;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by emman on 7/28/14.
 */

// TODO: checkWatchConnections() missing cases

public class BluetoothManager {

//--------------------------------------------------------------------------------------------------- Variables
    private static BluetoothManager     mInstance       = null;

    private Constants.WATCH_STATE       mWatchState     = Constants.WATCH_STATE.DISCONNECTED;

    public BluetoothAdapter             mAdapter        = BluetoothAdapter.getDefaultAdapter();
    private ArrayList<BluetoothDevice>  mDevices        = new ArrayList <BluetoothDevice>();
    private BluetoothDevice             mSelectedDevice;
    public static BluetoothProfile      mBluetoothProfile;

    private Timer                       mPairTime;
    private TimerTask                   mPairTask;

    private Activity                    mActivity       = null;
    private IBluetoothListener          mListener       = null;

    private DialogManager               mDialog;

    // simplified
    private BTMessageHandler            mBTMessageHandler   = null;
    private static KreyosService        mService            = null;

    private Timer                       mGetDataTimer;
    private TimerTask                   mGetDataTask;
    private long                        mGetDataDelay       = 10000;
    private boolean                     mBGettingData       = false;
    private boolean                     mBConnectionSaved   = false;

    private String                      mActiveDeviceName   = "";
    private Protocol                    mProtocol           = null;



//--------------------------------------------------------------------------------------------------- Service Connection
    protected ServiceConnection mServiceConnection      = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - On Service Disconnected from mServiceConnection");
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - On Service Connected from mServiceConnection");
            mService = (((KreyosService.KreyosServiceBinder) binder).getService());
            mService.initServiceTasks();
        }
    };

    protected ServiceConnection mStackServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {}

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {}
    };

    private ServiceListener mHeadsetBluetoothListener   = new ServiceListener() {

        @Override
        public void onServiceDisconnected(int profile) {}

        @Override
        public void onServiceConnected(int pProfile, BluetoothProfile pBluetoothProfile) {
            Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - On Service Connected from mHeadsetBluetoothListener");

            try {
                Method connect = BluetoothHeadset.class.getDeclaredMethod( "connect", BluetoothDevice.class);

                try {
                    // how to get the paired device?
                    // available resources ? bluetooth name
                    String deviceName                   = PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_BT_DEVICE_NAME );
                    BluetoothDevice selectedDevice      = null;
                    Set<BluetoothDevice> bondedDevices  = mAdapter.getBondedDevices();

                    for ( BluetoothDevice device : bondedDevices ) {
                        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - onServiceConnected: " + device.getName() + " || " + deviceName);

                        if ( device.getName().equalsIgnoreCase(deviceName) ) {
                            Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - onServiceConnected: selected device = " + device.getName() + " / " + deviceName );
                            selectedDevice = device;
                            break;
                        }
                    }

                    if (selectedDevice == null) {
                        return;
                    }

                    mBluetoothProfile = pBluetoothProfile;

                    connect.setAccessible(true);
                    connect.invoke( mBluetoothProfile, mSelectedDevice);

                    mDialog.cancelProgressDialog();

                    onBluetoothHeadsetConnected();

                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    };


    private void onBluetoothHeadsetConnected() {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - BT Headset Connected!");

        // Change the watch state to connected
        PreferencesManager.getInstance().saveDataInt( Constants.PREFKEY_USER_WATCHED_STATE,
                                                      Constants.WATCH_STATE.CONNECTED.ordinal() );

        if ( mSelectedDevice != null) {
            PreferencesManager.getInstance().saveDataString( Constants.PREFKEY_BT_DEVICE_NAME,
                                                             mSelectedDevice.getName() );
        }

        mWatchState = Constants.WATCH_STATE.CONNECTED;

        // change header color
        mListener.onBluetoothHeadsetConnected();

        try {
            mDialog.cancelProgressDialog();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void restartBTAgentSession() {
        /******/
        // supposed to be located on the activity

        if ( !mBConnectionSaved ) {
            mActiveDeviceName = mSelectedDevice.getName();

            PreferencesManager.getInstance().saveDataString( Constants.PREFKEY_BT_DEVICE_NAME, mActiveDeviceName );

            BluetoothAgent agent = BluetoothAgent.getInstance( mBTMessageHandler );
            agent.bindDevice( mActiveDeviceName );
            agent.restartSession();

            // Log devicename
            Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - BT Headset Connected! device name = " + mActiveDeviceName);


            mService.initBluetooth(mActiveDeviceName);
            Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - BT Headset Connected! Service init!!! device name = " + mActiveDeviceName);
        }

        if ( mBGettingData ) {
            return;
        }

        onStartGetActivityTimer();
        /******/
    }

    //----------------------------------------------------------------------------------------------- BT Data Handler
    // problem with message handler tranfer: cannot be handled using BT Manager (instance needed5)
    private static class BTMessageHandler extends Handler {

        private final WeakReference<MainActivity> mActivity;

        public BTMessageHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(Constants.TAG_DEBUG, "( MANAGER:BTMessageHandler ) - handle message");
            MainActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleBluetoothMessage(msg);
            }
        }
    }


//--------------------------------------------------------------------------------------------------- Constructor
    protected BluetoothManager() {

    }

    public static BluetoothManager getInstance() {
        if(mInstance == null) {
            mInstance = new BluetoothManager();
        }
        return mInstance;
    }

    public void setListener(IBluetoothListener pListener) {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - set listener");
        mListener = pListener;
    }

//--------------------------------------------------------------------------------------------------- Setup
    /** 1 */
    public void init(Activity pActivity) {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - init");
        mActivity = pActivity;
        mDialog = new DialogManager(mActivity);

        // TODO: make the protocol public (problem that might occur: null/BT Message Handler)
//        mProtocol = new Protocol( BluetoothAgent.getInstance(mBTMessageHandler), mActivity.getApplicationContext() );
    }




//--------------------------------------------------------------------------------------------------- Initial connection
    /** 2 */
    public boolean turnOn() {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - turn on");

        // Check bluetooth connection
        // CONNECTED? check watch connections
        // NOT CONNECTED? show intent

        if (mAdapter.isEnabled()) {
            checkWatchConnections();
            return true;
        }

        // prompt: Bluetooth permission request
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        mActivity.startActivityForResult(enableBtIntent, Constants.RC_BLUETOOTH_ENABLE);
        return false;
    }

    // called by MainActivity (Bluetooth permission request)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Activity Result");

        if ( requestCode != Constants.RC_BLUETOOTH_ENABLE ) {
            return;
        }

        if (resultCode == 1) {
            checkWatchConnections();
            return;
        }

        // show prompt again
        turnOn();
    }

    /** 3 */
    private void checkWatchConnections() {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Check watch connections");

        boolean bSavedDeviceName    = PreferencesManager.getInstance().containsKey( Constants.PREFKEY_BT_DEVICE_NAME );
        String strSavedDeviceName   = PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_BT_DEVICE_NAME );

        // No watch connected
        if (    !BluetoothManager.getInstance().isConnectedToADevice()
            ||  !bSavedDeviceName
            ||  strSavedDeviceName.equals("")) {
            if ( mWatchState == Constants.WATCH_STATE.DISCONNECTED ) {
                Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Check watch connections: Start connection");

                PreferencesManager.getInstance().deleteDataForKey( Constants.PREFKEY_BT_DEVICE_NAME );
                initBluetoothAgent();
                getAvailableDevices();
                return;
            }

            // User canceled pairing
            // Enable user to browse even without no watch paired
            BluetoothManager.getInstance().pairCancelled();
            return;
        }

        // Newly open application with saved device
        mBConnectionSaved = true;

        mBTMessageHandler = new BTMessageHandler( (MainActivity) mActivity );
        if (BluetoothAgent.getInstance(null) == null) {
            Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Check watch connections: Fresh connection");

            BluetoothAgent.initBluetoothAgent( mActivity );
            BluetoothAgent.getInstance( mBTMessageHandler ).initialize();
            setupConnection();
            return;
        }


        // Switched from another activity
        setupConnection();
        if (mWatchState == Constants.WATCH_STATE.CONNECTED) { // Register message handler
            Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Check watch connections: Register message handler");
            BluetoothAgent.getInstance( mBTMessageHandler );
        }

        setActivityData();
    }


    public boolean isConnectedToADevice() {

        Set<BluetoothDevice> bondedDevices = mAdapter.getBondedDevices();

        for ( BluetoothDevice i : bondedDevices ) {
            if (    i.getName().startsWith("Meteor")
                ||  i.getName().startsWith("Kreyos")) {
                Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - is Connected To Watch = TRUE");
                return true;
            }
        }

        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - is Connected To Watch = FALSE");
        return false;
    }

    public void getAvailableDevices() {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Getting Devices");

        // Make it sure that no watch connected when searching
        try {
            disconnectWatch();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Watch Disconnection error");
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        mActivity.registerReceiver(mDeviceReceiver, filter);

        mDevices.clear();
        mAdapter.startDiscovery();

        // Show progressdialog
        mDialog.showProgressDialog("Please Wait", "Searching Meteor...");
    }


    /* RESTART CONNECTION TO A WATCH - also used by Active watch */
    private void initBluetoothAgent() {

        if (mActivity == null)
            return;

        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - init bluetooth agents");

        BluetoothAgent.initBluetoothAgent( mActivity );
        BluetoothAgent.getInstance(null).initialize();

        setupKreyosService();

        mBTMessageHandler = new BTMessageHandler( (MainActivity)mActivity );
    }

    private void setupKreyosService() {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Setup Kreyos Service");

        // These are all setup of kreyosService class
        Intent intent = new Intent( mActivity, KreyosService.class);

        // init cloud sync
        intent.putExtra("cloud_id",             PreferencesManager.getInstance().retrieveDataForString( "cloud_id" ));
        intent.putExtra("access_token",         PreferencesManager.getInstance().retrieveDataForString( "access_token" ));
        intent.putExtra("access_expires",       PreferencesManager.getInstance().retrieveDataForLong( "access_expires", 0 ));
        intent.putExtra("twitter_access_token", PreferencesManager.getInstance().retrieveDataForString( "twitter_access_token" ));
        intent.putExtra("twitter_secret_token", PreferencesManager.getInstance().retrieveDataForString( "twitter_secret_token" ));

        // init notifs
        intent.putExtra("notification.facebook",       PreferencesManager.getInstance().retrieveDataForBoolean( Constants.PREFKEY_NOTIF_FACEBOOK, false ));
        intent.putExtra("notification.weather",        PreferencesManager.getInstance().retrieveDataForBoolean( Constants.PREFKEY_NOTIF_WEATHER, false ));
        intent.putExtra("notification.twitter",        PreferencesManager.getInstance().retrieveDataForBoolean( Constants.PREFKEY_NOTIF_TWITTER, false ));
        intent.putExtra("notification.reminder",       PreferencesManager.getInstance().retrieveDataForBoolean( Constants.PREFKEY_NOTIF_REMINDER, false ));
        intent.putExtra("notification.sms",            PreferencesManager.getInstance().retrieveDataForBoolean( Constants.PREFKEY_NOTIF_SMS, false ));
        intent.putExtra("notification.call",           PreferencesManager.getInstance().retrieveDataForBoolean( Constants.PREFKEY_NOTIF_CALL, false ));
        intent.putExtra("notification.low_battery",    PreferencesManager.getInstance().retrieveDataForBoolean( Constants.PREFKEY_NOTIF_LOW_BATTERY, false ));
        intent.putExtra("notification.bt_outof_range", PreferencesManager.getInstance().retrieveDataForBoolean( Constants.PREFKEY_NOTIF_OUT_OF_RANGE, false ));

        // bluetooth agent
        intent.putExtra("bluetooth.device_name",        PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_BT_DEVICE_NAME ));

        // Start and bind the service class
        mActivity.bindService(intent,	mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void resetBluetoothConnection() {
        // FOR ACTIVE WATCH
        if (mActivity == null)
            return;

        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Reset Bluetooth Connection");

        mBTMessageHandler = null;

        initBluetoothAgent();

        getAvailableDevices();
    }

    public void displayProgressBarUpdate( TodayActivity pActivity ) {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Display progress bar update");

        String steps    = KreyosUtility.setDataForDisplay((double) pActivity.steps);
        String distance = KreyosUtility.setDataForDisplay((double)(pActivity.distance/1000));
        String calories = KreyosUtility.setDataForDisplay((double)(pActivity.calories /1000));
        ActivityData stats = new ActivityData(null, steps, distance, calories);

        sendStatToAdapter(stats);
    }

    public void setActivityData() {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Set Activity Data");

        ActivityData stats = new ActivityData(null, "0.0", "0.0", "0.0");
        sendStatToAdapter(stats);
    }

    // Send data on the adapter
    private void sendStatToAdapter(ActivityData pStats) {

        if (pStats == null) {
            return;
        }

        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Send stats to adapter");

        float maxSteps  = PreferencesManager.getInstance().retrieveDataForInt( Constants.PREFKEY_SPORTS_GOAL_STEPS, 1000 );
        float steps     = Float.parseFloat(pStats.mSteps);
        float distance  = Float.parseFloat(pStats.mDistance);
        float calories  = Float.parseFloat(pStats.mCalories);

        mListener.updateProgressBar(maxSteps,
                                    steps,
                                    distance,
                                    calories);
    }

    private BroadcastReceiver mDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            try {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - mDeviceReceiver: End of Search!");

                    mDialog.cancelProgressDialog();
                    mDialog.showPairingDialog(mDevices);
                    mActivity.unregisterReceiver(mDeviceReceiver);
                    return;
                }

                if (device == null) {
                    return;
                }
                if (mDevices.contains(device)) {
                    return;
                }
                if (device.getName().contains("MeteorLE")) {
                    return;
                }
                if (device.getName().contains("KreyosLE")) {
                    return;
                }

                // TODO: "Kreyos Iphone" can be detected
                if (device.getName().startsWith("Meteor") ||
                        device.getName().startsWith("Kreyos")) {
                    Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - mDeviceReceiver: Device added: " + device.getName());
                    mDevices.add(device);
                    return;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    public void pairCancelled() {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Pair Cancelled");

        mWatchState = Constants.WATCH_STATE.WAITING;
        PreferencesManager.getInstance().saveDataInt( Constants.PREFKEY_USER_WATCHED_STATE, Constants.WATCH_STATE.WAITING.ordinal() );

        setActivityData();
    }

    public void pairDevice(BluetoothDevice pDevice) {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Pair Device");

        mSelectedDevice = pDevice;

        try {
            mSelectedDevice.getClass().getMethod("createBond", (Class[]) null).invoke(mSelectedDevice,  (Object[]) null);

        } catch (IllegalAccessException e) {
            e.printStackTrace();

        } catch (IllegalArgumentException e) {
            e.printStackTrace();

        } catch (InvocationTargetException e) {
            e.printStackTrace();

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        PreferencesManager.getInstance().saveDataString(    Constants.PREFKEY_BT_DEVICE_NAME,
                                                            mSelectedDevice.getName());

        mPairTime = new Timer();
        mPairTask = new TimerTask() {
            @Override
            public void run() {
                if ( mSelectedDevice.getBondState() == BluetoothDevice.BOND_BONDED ) {
                    Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Pair Device: bonded!");

                    mPairTime.cancel();
                    mDialog.cancelProgressDialog();
                    connectToHeadset();
                }
            }
        };
        mPairTime.schedule(mPairTask, 0, 1000);
        mDialog.showProgressDialog("Please wait", "Pairing with Meteor");
    }

    public void disconnectWatch() {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Disconnect Watch");

        // Clear all connected kreyos watch
        clearConnectedWatch();

        if ( mWatchState == Constants.WATCH_STATE.CONNECTED) {

            String deviceName = PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_BT_DEVICE_NAME );

            if( mSelectedDevice == null ) {

                mSelectedDevice = null;
                Set<BluetoothDevice> bondedDevices = mAdapter.getBondedDevices();
                ArrayList<BluetoothDevice> bondedWatches = new ArrayList<BluetoothDevice>();
                for (BluetoothDevice i : bondedDevices) {
                    if (i.getName().startsWith("Meteor") || i.getName().startsWith("Kreyos")){
                        bondedWatches.add(i);
                    }
                }
                for (BluetoothDevice device : bondedWatches) {
                    if (device.getName().equals(deviceName)) {
                        mSelectedDevice = device;
                        break;
                    }
                }
            }

            if ( mSelectedDevice != null) {
                unpairDevice(mSelectedDevice);
                BluetoothAgent.getInstance(null).forceStopSession();
            }
        }

        mWatchState = Constants.WATCH_STATE.DISCONNECTED;
        PreferencesManager.getInstance().deleteDataForKey( Constants.PREFKEY_BT_DEVICE_NAME );
        PreferencesManager.getInstance().deleteDataForKey( Constants.PREFKEY_USER_WATCHED_STATE );
    }

    public void unpairDevice(BluetoothDevice pDevice) {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Unpair Device");

        mSelectedDevice = pDevice;

        try {
            mSelectedDevice.getClass().getMethod("removeBond", (Class[]) null)
                    .invoke(mSelectedDevice, (Object[]) null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
		/*
		 * try { Method m = device.getClass() .getMethod("removeBond", (Class[])
		 * null); m.invoke(device, (Object[]) null); } catch (Exception e) {
		 * Log.e("Unpairing", e.getMessage()); }
		 */
    }

    public void clearConnectedWatch() {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Clear Paired Device");

        Set<BluetoothDevice> bondDevices = mAdapter.getBondedDevices();

        for (BluetoothDevice device : bondDevices) {
            if (device.getName().startsWith("Meteor") || device.getName().startsWith("Kreyos")) {
                unpairDevice(device);
            }
        }
    }

    public void connectToHeadset() {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Connect to Headset");

        final Context context = mActivity.getApplicationContext();
        Timer delayTimer = new Timer();
        TimerTask delayTask = new TimerTask() {

            @Override
            public void run() {
                 mAdapter.getProfileProxy(  context,
                                            mHeadsetBluetoothListener,
                                            BluetoothProfile.HEADSET );

                Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - connect to headset (Timer)");
            }
        };
        delayTimer.schedule(delayTask, 1000);
    }

    // called by a successful bluetooth headset connection
    private void onStartGetActivityTimer() {
        mGetDataTimer = new Timer();
        mGetDataTask = new TimerTask() {
            @Override
            public void run() {
                if ( mWatchState != Constants.WATCH_STATE.CONNECTED ) {
                    return;
                }

                Log.d("ActivityData", "Getting ActivityData");
                Protocol p = new Protocol(BluetoothAgent.getInstance(mBTMessageHandler), mActivity.getApplicationContext());
                p.sendDailyActivityRequest();
            }
        };
        mGetDataTimer.schedule(mGetDataTask, 0, mGetDataDelay);
        mBGettingData = true;
    }

    private void setupConnection() {

        // Checking of watch state
        Constants.WATCH_STATE _watchState = (Constants.WATCH_STATE.values()) [PreferencesManager.getInstance().retrieveDataForInt( Constants.PREFKEY_USER_WATCHED_STATE, 0 )];
        mWatchState = _watchState;

        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Setup connection: PREFKEY_USER_WATCHED_STATE=\"" + mWatchState.ordinal() + mWatchState.name() + "\" ");

        if (    mWatchState == Constants.WATCH_STATE.WAITING
            ||  mWatchState == Constants.WATCH_STATE.DISCONNECTED   ) {
            return;
        }

        if (    mAdapter != null
            &&  mAdapter.isEnabled() ) {
            Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Setup connection: Started!");

            setupKreyosService();

            // Stack Service
            Intent stackIntent = new Intent(mActivity,
                                            StackActivitiesService.class);
            mActivity.bindService(stackIntent,
                    mStackServiceConnection,
                    Context.BIND_AUTO_CREATE);

            // bluetooth headset
            try {
                connectBluetoothHeadset();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void connectBluetoothHeadset() {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Connect bluetooth headset");

        final Context _context = mActivity.getApplicationContext();

        final Timer _delayTimer = new Timer();
        TimerTask _delayTask    = new TimerTask() {
            @Override
            public void run() {
                Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Connect bluetooth headset: connecting...");

                mAdapter.getProfileProxy(_context,
                                         mHeadsetBluetoothListener,
                                         BluetoothProfile.HEADSET);
                _delayTimer.cancel();
            }
        };

        _delayTimer.schedule( _delayTask, 1000);
    }

    public void unlockDevice() {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Unlock Device");

        BluetoothAgent agent = BluetoothAgent.getInstance(null);
        Protocol p = new Protocol( agent, mActivity.getApplicationContext() );
        p.unlockWatch();
    }

    // Setter and Getter
    public String getSelectedDeviceName() {
        return mSelectedDevice.getName();
    }

    public void checkConnectionAndGetData() {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Check Connection & Get Data");

        try {
            if( mWatchState == Constants.WATCH_STATE.CONNECTED ) {
                Protocol p = new Protocol( BluetoothAgent.getInstance( mBTMessageHandler ), mActivity );
                p.getActivityData();
            }
        }
        catch(NullPointerException e) {
            // Error! either no connection or not connected to watch
            Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Overall activities No connection!" );
        }
    }

    // unbind (called by Main Activity)
    public void unbindService() {

        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Unbind Service");

        if ( mActivity == null )
            return;

        if (mServiceConnection != null) {
            try {
                mActivity.unbindService(mServiceConnection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mStackServiceConnection != null) {
            try {
                mActivity.unbindService(mStackServiceConnection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            BluetoothAdapter.getDefaultAdapter().closeProfileProxy(
                    BluetoothProfile.HEADSET, mBluetoothProfile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (mDialog == null) {
            return;
        }

        mDialog.cancelProgressDialog();

        mBTMessageHandler = null;
    }

    public void changeDeviceConnectionState ( Constants.WATCH_STATE pState ) {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Change Watch Connection: "+ pState);

        mWatchState = pState;
    }

    public void syncDeviceConfig(final String[] worldClocks,
        final int[] worldClockOffset, final boolean isDigitalClock,
        final int digitalClock, final int analogClock,
        final int sportsGrid, final int[] sportsGrids, final int[] goals,
        final int weight, final int height, final boolean enableGesture,
        final boolean isLeftHandGesture, final int[] gestureActionsTable,
        final boolean isUkUnit) {

        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Sync Config");

        Protocol p = new Protocol(BluetoothAgent.getInstance(null), mActivity.getApplicationContext());
        p.syncWatchConfig(worldClocks, worldClockOffset, isDigitalClock, digitalClock,
                analogClock, sportsGrid, sportsGrids, goals, weight, height,
                enableGesture, isLeftHandGesture, gestureActionsTable, isUkUnit);
    }

    public void handleTimeSync() {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Handle Time Sync");

        Protocol p = new Protocol(BluetoothAgent.getInstance(mBTMessageHandler), mActivity.getApplicationContext());

        BluetoothAgent.getInstance(mBTMessageHandler).bindMessageHandler(mBTMessageHandler);
        p.getSportsGrid();

    }

    public boolean isDeviceDisconnected() {

        if ( mWatchState == Constants.WATCH_STATE.DISCONNECTED) {
            KreyosUtility.showErrorMessage( mActivity,
                                            mActivity.getString(R.string.dialog_title_device_not_found),
                                            mActivity.getString(R.string.dialog_msg_connect_watch));
            return true;
        }

        return false;
    }

//--------------------------------------------------------------------------------------------------- SETTINGS (involves Service only)
    public void notifStartCallReceiver() {
        mService.startCallReceiver();
    }

    public void notifStartSMSReceiver() {
        mService.startSMSReceiver();
    }

    public void notifStartWeather() {
        mService.startWeatherNotification();
    }

    public void notifStartFacebook() {
        mService.startFacebookNotification();
    }

    public void notifStartTwitter() {
        mService.startTwitterNotification();
    }

    public void notifStartReminder() {
        mService.startReminderListener();
    }

    public void notifStartLowBattery() {
        mService.startLowBatteryNotification();
    }


    public void notifStopCallReceiver() {
        mService.stopCallReceiver();
    }

    public void notifStopSMSReceiver() {
        mService.stopSMSReceiver();
    }

    public void notifStopWeather() {
        mService.stopWeatherNotification();
    }

    public void notifStopFacebook() {
        mService.stopFacebookNotification();
    }

    public void notifStopTwitter() {
        mService.stopTwitterNotification();
    }

    public void notifStopReminder() {
        mService.stopReminderListener();
    }

    public void notifStopLowBattery() {
        mService.stopLowBatteryNotification();
    }

//--------------------------------------------------------------------------------------------------- ACTIVE WATCH
    public String getConnectedDeviceName() {

        String deviceName = PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_BT_DEVICE_NAME );

        if (   mWatchState == Constants.WATCH_STATE.CONNECTED
            && !(deviceName.equals("NO VALUE")) ) {
            return deviceName;
        }

        return "";
    }

    public void unpairConnectedDevice() {
        disconnectWatch();

        // Show progressdialog
        mDialog.showProgressDialog( mActivity.getString(R.string.dialog_title_disconnect_watch),
                                    mActivity.getString(R.string.dialog_msg_please_wait));

        // Delay on disconnection
        Handler delay = new Handler();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {

                if ( mDialog != null )
                    mDialog.cancelProgressDialog();

                mListener.onUnpairConnectedDevice();
            }
        };
        delay.postDelayed(runnable, 2000); // 2 seconds
    }

    public Constants.WATCH_STATE getDeviceState() {
        return mWatchState;
    }

//--------------------------------------------------------------------------------------------------- DATE & TIME
    public void syncTime() {
        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Sync Time");

        Protocol p = new Protocol(BluetoothAgent.getInstance(null), mActivity);
        p.syncTime();
    }

//--------------------------------------------------------------------------------------------------- SILENT ALARMS
    public void setWatchAlarm(final int index, final int mode,
                              final int monthday, final int weekday,
                              final int hour, final int minute) {

        Log.d(Constants.TAG_DEBUG, "( MANAGER:bluetooth ) - Set Alarm Watch");

        Protocol p = new Protocol(BluetoothAgent.getInstance(null), mActivity);

        p.setWatchAlarm(index, mode,
                        monthday, weekday,
                        hour, minute);
    }


}
