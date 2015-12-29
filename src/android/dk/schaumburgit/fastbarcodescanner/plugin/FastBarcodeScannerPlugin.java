package dk.schaumburgit.fastbarcodescanner.plugin;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.provider.Settings;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Override;

import dk.schaumburgit.fastbarcodescanner.FastBarcodeScanner;
import dk.schaumburgit.stillsequencecamera.camera2.StillSequenceCamera2;

public class FastBarcodeScannerPlugin
        extends CordovaPlugin
        implements FastBarcodeScanner.BarcodeDetectedListener, FastBarcodeScanner.MultipleBarcodesDetectedListener
{

	public static final String TAG = "Fast Barcode Scanner Plugin";

    private static final String ACTION_SHOW_TOAST = "showToast";
	private static final String ACTION_START_SCANNING = "startScanning";
	private static final String ACTION_STOP_SCANNING = "stopScanning";
    //private static final String ACTION_REQUEST_PERMISSION = "requestPermission";
	//private static final String ACTION_WRITE_HEX = "writeSerialHex";
	//private static final String ACTION_CLOSE = "closeSerial";
	//private static final String ACTION_READ_CALLBACK = "registerReadCallback";

	/**
	 * Constructor.
	 */
	public FastBarcodeScannerPlugin() {
	}

    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.d(TAG, "initialize");
    }

	/**
	 * Overridden execute method
	 * @param action the string representation of the action to execute
	 * @param args
	 * @param callbackContext the cordova {@link CallbackContext}
	 * @return true if the action exists, false otherwise
	 * @throws JSONException if the args parsing fails
	 */
	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		Log.d(TAG, "Action: " + action);
		JSONObject arg_object = args.optJSONObject(0);

        // show a toast
        if (ACTION_SHOW_TOAST.equals(action)) {
            String data = arg_object.getString("data");
            showToast(data);
            return true;
        }
		// start scanning
		else if (ACTION_START_SCANNING.equals(action)) {
			startScanning(callbackContext);
			return true;
		}
		// stop scanning
		else if (ACTION_STOP_SCANNING.equals(action)) {
            stopScanning(callbackContext);
			return true;
		}
		// the action doesn't exist
		return false;
	}

    private void showToast(final String text)
    {
        final int duration = Toast.LENGTH_SHORT;
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(cordova.getActivity().getApplicationContext(), text, duration);
                toast.show();
            }
        });
    }

    private FastBarcodeScanner mScanner = null;

    // callback that will be used to send back data to the cordova app
    private CallbackContext mScanCallback;
    private HandlerThread mScanCallbackThread;
    private Handler mScanCallbackHandler;
    private void startScanning(final CallbackContext callbackContext) {
        Log.d(TAG, "Start scanning");

        if (mScanner == null) {
            boolean hasCameraPermission = requestCameraPermission(CALL_START_WHEN_DONE, callbackContext);
            if (!hasCameraPermission) {
                Log.d(TAG, "Postpone scanning");
                return;
            }

            mScanner = new FastBarcodeScanner(cordova.getActivity(), 1024 * 768);
        }

        mScanCallback = callbackContext;

        cordova.getThreadPool().execute(new Runnable() {
            //cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Log.d(TAG, "Registering scan Callback");
                // Keep the callback for later use:
                mScanCallback = callbackContext;
                // Use a dedicated thread for handling all the incoming images
                mScanCallbackThread = new HandlerThread("FastBarcodeScanner plugin callback");
                mScanCallbackThread.start();
                mScanCallbackHandler = new Handler(mScanCallbackThread.getLooper());
                // Start listening for callbacks
                try {
                    mScanner.StartScan(false, FastBarcodeScannerPlugin.this, mScanCallbackHandler);
                    // Create an OK result:
                    JSONObject returnObj = new JSONObject();
                    addProperty(returnObj, "startScanning", "true");
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, returnObj);
                    pluginResult.setKeepCallback(true); // make sure the callback is kept open
                    // Return:
                    callbackContext.sendPluginResult(pluginResult);
                } catch (Exception exc) {
                    Log.e(TAG, "StartScan failed", exc);
                    // Create an ERROR result:
                    JSONObject returnObj = new JSONObject();
                    addProperty(returnObj, "startScanning", exc.getMessage());
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, returnObj);
                    // make sure the callback is closed:
                    pluginResult.setKeepCallback(false);
                    mScanCallback = null;
                    // Clean-up and close callback thread and handler:
                    if (mScanCallbackHandler != null) {
                        mScanCallbackHandler.removeCallbacksAndMessages(null);
                        mScanCallbackHandler = null;
                    }
                    if (mScanCallbackThread != null) {
                        try {
                            mScanCallbackThread.quitSafely();
                            mScanCallbackThread.join();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mScanCallbackThread = null;
                    }
                    // Return the error:
                    callbackContext.sendPluginResult(pluginResult);
                }
            }
        });
    }

    private void stopScanning(final CallbackContext callbackContext) {
        Log.v(TAG, "Stop scanning");

        mScanCallback = null;

        if (mScanner == null) {
            return;
        }

        mScanner.StopScan();

        if (mScanCallback != null) {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "stopScanning called");
            result.setKeepCallback(false);
            mScanCallback.sendPluginResult(result);
            mScanCallback = null;
        }

        Log.i(TAG, "Killing scan callback");
        try {
            if (mScanCallbackHandler != null) {
                mScanCallbackHandler.removeCallbacksAndMessages(null);
                mScanCallbackHandler = null;
            }
            if (mScanCallbackThread != null) {
                mScanCallbackThread.quitSafely();
                mScanCallbackThread.join();
                mScanCallbackThread = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        callbackContext.sendPluginResult(
                new PluginResult(PluginResult.Status.OK, (String) null)
        );
    }

    @Override
    public void onSingleBarcodeAvailable(FastBarcodeScanner.BarcodeInfo barcodeInfo, byte[] image, int format, int width, int height) {
        Log.d(TAG, "Start barcode");
        String barcode = null;
        if (barcodeInfo != null)
            barcode = barcodeInfo.barcode;
        Log.d(TAG, "Barcode: " + barcode);

        if (mScanCallback != null) {
            Log.d(TAG, "Callback");
            JSONObject returnObj = new JSONObject();
            addProperty(returnObj, "barcode", barcode);
            addProperty(returnObj, "format", format);
            PluginResult result = new PluginResult(PluginResult.Status.OK, returnObj);
            result.setKeepCallback(true);
            mScanCallback.sendPluginResult(result);
        }
    }

    @Override
    public void onMultipleBarcodeAvailable(FastBarcodeScanner.BarcodeInfo[] barcodes, byte[] image, int format, int width, int height){
        String barcode = null;
        if (barcodes != null && barcodes.length > 0)
            barcode = barcodes[0].barcode;

        if( mScanCallback != null ) {
            JSONObject returnObj = new JSONObject();
            addProperty(returnObj, "barcodes", barcodes);
            addProperty(returnObj, "format", format);
            PluginResult result = new PluginResult(PluginResult.Status.OK, returnObj);
            result.setKeepCallback(true);
            mScanCallback.sendPluginResult(result);
        }
    }

    @Override
    public void onError(Exception error){
        if( mScanCallback != null ) {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, error.getMessage());
            result.setKeepCallback(true);
            mScanCallback.sendPluginResult(result);
        }
    }

    /**
     * Paused activity handler
     * @see org.apache.cordova.CordovaPlugin#onPause(boolean)
     */
    @Override
    public void onPause(boolean multitasking) {
        Log.d(TAG, "Pause");


        if (mScanner == null) {
            return;
        }

        mScanner.Pause();
    }


    /**
     * Resumed activity handler
     * @see org.apache.cordova.CordovaPlugin#onResume(boolean)
     */
    @Override
    public void onResume(boolean multitasking) {
        Log.d(TAG, "Resume");

        if (mScanner == null) {
            return;
        }

        mScanner.Resume();
    }

    /**
     * Destroy activity handler
     * @see org.apache.cordova.CordovaPlugin#onDestroy()
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroy");
    }

    /**
     * Utility method to add some properties to a {@link JSONObject}
     * @param obj the json object where to add the new property
     * @param key property key
     * @param value value of the property
     */
    private void addProperty(JSONObject obj, String key, Object value) {
        try {
            obj.put(key, value);
        }
        catch (JSONException e){}
    }

    private static final int CALL_START_WHEN_DONE = 2;

    private CallbackContext mPermissionCallbackContext;
    private boolean requestCameraPermission(int whatNext, CallbackContext callbackContext)
    {
        Log.d(TAG, "Check camera permission");

        if (mPermissionCallbackContext != null) {
            Log.e(TAG, "RACE CONDITION: two overlapping permission requests");
            callbackContext.error("Application error requesting permissions - see the log for details");
            PluginResult r = new PluginResult(PluginResult.Status.ERROR);
            callbackContext.sendPluginResult(r);
            return false;
        }

        if (cordova.hasPermission(Manifest.permission.CAMERA))
        {
            Log.d(TAG, "...already have camera permission - life is good");
            return true;
        }

        Log.d(TAG, "...dont have camera permission - will have to ask the user");
        mPermissionCallbackContext = callbackContext;
        cordova.requestPermission(this, whatNext, Manifest.permission.CAMERA);

        return false;
    }

    //public void onRequestPermissionResult(int requestCode, String[] permissions,
    //                                      int[] grantResults) throws JSONException
    /**
     * Called by the system when the user grants permissions
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException
    {
        if (mPermissionCallbackContext == null) {
            Log.e(TAG, "No context - got a permission result we didnt ask for...??? ");
            return;
        }

        for(int r:grantResults)
        {
            if(r == PackageManager.PERMISSION_DENIED)
            {
                Log.d(TAG, "User refused us access to  the camera - there is nothing we can do");
                return;
            }
        }

        int whatNext = requestCode;
        CallbackContext ctx = mPermissionCallbackContext;
        mPermissionCallbackContext = null; // if there's a race-condition, let's make life hard for it...
        switch (whatNext) {
            case CALL_START_WHEN_DONE:
                startScanning(ctx);
                break;
            default:
                Log.e(TAG, "Unexpected requestCode - got a permission result we didnt ask for...???");
                ctx.error("Application error requesting permissions - see the log for details");
                PluginResult r = new PluginResult(PluginResult.Status.ERROR);
                ctx.sendPluginResult(r);
                break;
        }
    }
}
