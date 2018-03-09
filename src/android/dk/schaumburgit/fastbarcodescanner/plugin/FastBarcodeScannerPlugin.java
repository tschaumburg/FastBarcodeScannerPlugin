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
import android.widget.RelativeLayout;
import android.view.WindowManager;
import android.view.TextureView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.media.Image;
import android.graphics.ImageFormat;
import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Override;

import dk.schaumburgit.fastbarcodescanner.IBarcodeScanner;
import dk.schaumburgit.fastbarcodescanner.BarcodeScannerFactory;
//import dk.schaumburgit.stillsequencecamera.camera2.Facing;
import dk.schaumburgit.stillsequencecamera.camera2.StillSequenceCamera2;
import dk.schaumburgit.fastbarcodescanner.imageutils.ImageDecoder;
import dk.schaumburgit.fastbarcodescanner.EventConflation;
import dk.schaumburgit.trackingbarcodescanner.ScanOptions;
import dk.schaumburgit.trackingbarcodescanner.TrackingOptions;
import dk.schaumburgit.fastbarcodescanner.callbackmanagers.CallBackOptions;
import dk.schaumburgit.stillsequencecamera.camera2.StillSequenceCamera2Options;

public class FastBarcodeScannerPlugin
        extends CordovaPlugin
        implements IBarcodeScanner.BarcodeDetectedListener, IBarcodeScanner.MultipleBarcodesDetectedListener
{

	public static final String TAG = "Fast Barcode Scanner Plugin";

    private static final String ACTION_SHOW_TOAST = "showToast";
	private static final String ACTION_START_SCANNING = "startScanning";
	private static final String ACTION_STOP_SCANNING = "stopScanning";
	private static final String ACTION_SHOW_PREVIEW = "showPreview";
	private static final String ACTION_HIDE_PREVIEW = "hidePreview";
	private static final String ACTION_SIMULATE = "simulate";
	

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
		initPreview();
    }

	private StillSequenceCamera2Options convertCameraOptions(JSONObject json) throws JSONException
	{
		boolean enablePreview = json.getBoolean("enablePreview");
		TextureView preview = enablePreview ? mPreview: null;
		StillSequenceCamera2Options.Facing facing = decodeFacing(json.getInt("facing"));
		int minPixels = json.getInt("minPixels");
		StillSequenceCamera2Options res = new StillSequenceCamera2Options(preview, minPixels, facing);
		return res;
	}

	private ScanOptions convertScanOptions(JSONObject json) throws JSONException
	{
		String emptyMarker = json.getString("emptyMarker");
		String beginsWith = json.getString("beginsWith");
		ScanOptions res = new ScanOptions(emptyMarker, beginsWith);
		return res;
	}

	private TrackingOptions convertTrackingOptions(JSONObject json) throws JSONException
	{
		double margin = json.getDouble("trackingMargin");
		int patience = json.getInt("trackingPatience");
		TrackingOptions res = new TrackingOptions(margin, patience);
		return res;
	}

	private EventConflation decodeEventConflation(int val)
	{
		switch (val)
		{
			case 0:
				return EventConflation.None;
			case 1:
				return EventConflation.First;
			case 2:
				return EventConflation.Changes;
			case 3:
				return EventConflation.All;
			default:
				return EventConflation.All;
		}
	}

	private StillSequenceCamera2Options.Facing decodeFacing(int val) throws JSONException
	{
		switch (val)
		{
			case 0:
				return StillSequenceCamera2Options.Facing.Back;
			case 1:
				return StillSequenceCamera2Options.Facing.Front;
			case 2:
				return StillSequenceCamera2Options.Facing.External;
			default:
				return StillSequenceCamera2Options.Facing.Back;
		}
	}

	private CallBackOptions convertCallBackOptions(JSONObject json) throws JSONException
	{
		boolean includeImage = json.getBoolean("includeImage");
        EventConflation conflateHits = decodeEventConflation(json.getInt("conflateHits"));
        EventConflation conflateBlanks = decodeEventConflation(json.getInt("conflateBlanks"));
        EventConflation conflateErrors = decodeEventConflation(json.getInt("conflateErrors"));
        int debounceBlanks = json.getInt("debounceBlanks");
        int debounceErrors = json.getInt("debounceErrors");

		CallBackOptions res = new CallBackOptions(includeImage, conflateHits, debounceBlanks, conflateBlanks, debounceErrors, conflateErrors);
		return res;
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
			int resolution = 1024*768;
			if (arg_object != null)
				resolution = arg_object.optInt("resolution", 1024*768);
			startScanning(resolution, callbackContext);
			return true;
		}
		// show preview
		else if (ACTION_SHOW_PREVIEW.equals(action)) {
            int x = arg_object.getInt("x"); // ...or optInt()?
            int y = arg_object.getInt("y"); // ...or optInt()?
            int w = arg_object.getInt("w"); // ...or optInt()?
            int h = arg_object.getInt("h"); // ...or optInt()?
			showPreview(x, y, w, h, callbackContext);
			return true;
		}
		// hide preview
		else if (ACTION_HIDE_PREVIEW.equals(action)) {
			hidePreview(callbackContext);
			return true;
		}
		// stop scanning 
		else if (ACTION_STOP_SCANNING.equals(action)) {
        	stopScanning(callbackContext);
			return true;
		}
        else if (ACTION_SIMULATE.equals(action)) {
            String barcode = arg_object.getString("barcode");
			simulate(barcode);
            return true;
        }
		// the action doesn't exist
		return false;
	}

	private void simulate(final String barcode)
	{
	    IBarcodeScanner.BarcodeInfo bc = new IBarcodeScanner.BarcodeInfo(barcode, null);
        OnHit(bc, null);
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

    private IBarcodeScanner mScanner = null;

    // callback that will be used to send back data to the cordova app
    private CallbackContext mScanCallback;
    private HandlerThread mScanCallbackThread;
    private Handler mScanCallbackHandler;
    private int mRequestedResolution;
    private void retryStartScanning(final CallbackContext callbackContext) {
    	startScanning(mRequestedResolution, callbackContext);
    }
    
    private void startScanning(int resolution, final CallbackContext callbackContext) {
        Log.d(TAG, "Start scanning");
        mRequestedResolution = resolution;

		//showPreview(0, 0, 1024, 1024, callbackContext);

        if (mScanner == null) {
            boolean hasCameraPermission = requestCameraPermission(CALL_START_WHEN_DONE, callbackContext);
            if (!hasCameraPermission) {
                Log.d(TAG, "Postpone scanning");
                return;
            }

            //mScanner = BarcodeScannerFactory.Create(cordova.getActivity(), (android.view.TextureView)null, resolution);
            mScanner = 
				BarcodeScannerFactory
				.builder(mPreview)
				.resolution(resolution)
                .conflateHits(EventConflation.Changes)
                .debounceBlanks(10)
                .conflateBlanks(EventConflation.First)
                .debounceErrors(3)
                .conflateErrors(EventConflation.First)
				.build(cordova.getActivity());
        }

        mScanCallback = callbackContext;

        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                cordova.getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });

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
                    mScanner.StartScan(FastBarcodeScannerPlugin.this, mScanCallbackHandler);
                    // Create an OK result:
                    JSONObject returnObj = new JSONObject();
                    addProperty(returnObj, "startScanning", "true");
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, returnObj);
                    pluginResult.setKeepCallback(true); // make sure the callback is kept open
                    // Return:
                    //callbackContext.sendPluginResult(pluginResult);
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

	private boolean mPreviewInitialized = false;
	private TextureView mPreview = null;
	private void initPreview() {
		if (mPreviewInitialized)
			return;

		Log.v(TAG, "Init preview");

        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
				mPreview = new TextureView(cordova.getActivity());
				FrameLayout.LayoutParams cameraPreviewParams = _position(0, 66, 412, 297);
					//new FrameLayout.LayoutParams(
					//	412,// w
					//	1024,//297,// h
					//	Gravity.LEFT | Gravity.TOP
					//);
				//cameraPreviewParams.leftMargin = 0;// x;
			    //cameraPreviewParams.topMargin = 66;// y;

				((ViewGroup) webView.getView().getParent()).addView(mPreview, cameraPreviewParams);

				webView.getView().bringToFront();
				//mPreview.setVisibility(View.INVISIBLE);
				
				//mPreview.setVisibility(View.VISIBLE);
                //webView.getView().setBackgroundColor(Color.argb(1, 0, 0, 0));

				mPreviewInitialized = true;
            }
        });

		//showPreview(0, 0, 1024, 1024, null);
	}

	private FrameLayout.LayoutParams _position(int x, int y, int w, int h)
	{
		Log.v(TAG, "POSITION (" + x + ", " + y + ", " + w + ", " + h + ")");

		FrameLayout.LayoutParams res = 
			new FrameLayout.LayoutParams(
				w,
				h,
				Gravity.LEFT | Gravity.TOP
			);
		
		res.leftMargin = x;
		res.topMargin = y;

		return res;
	}

	private void showPreview(final int x, final int y, final int w, final int h, final CallbackContext callbackContext) {
		Log.v(TAG, "showPreview(" + x + ", " + y + ", " + w + ", " + h + ")");
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
				//mPreview.setVisibility(View.VISIBLE);
		        mPreview.setLayoutParams(_position(x, y, w, h));
                webView.getView().setBackgroundColor(Color.argb(1, 0, 0, 0));
            }
        });
	}

	private void hidePreview(final CallbackContext callbackContext) {
		Log.v(TAG, "Hide preview");

        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                webView.getView().setBackgroundColor(Color.WHITE);
				mPreview.setVisibility(View.INVISIBLE);
            }
        });
	}

    private void stopScanning(final CallbackContext callbackContext) {
        Log.v(TAG, "Stop scanning");

        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                cordova.getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });

        mScanCallback = null;

        if (mScanner == null) {
            return;
        }

        try {
            mScanner.StopScan();
        } catch (Exception e) {
            e.printStackTrace();
            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
            callbackContext.sendPluginResult(pluginResult);
        }

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
            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
            callbackContext.sendPluginResult(pluginResult);
        }

        callbackContext.sendPluginResult(
                new PluginResult(PluginResult.Status.OK, (String) null)
        );
    }

    @Override
    public void OnBlank() {
        if (mScanCallback != null) {
            Log.d(TAG, "Blank");
            JSONObject returnObj = new JSONObject();
            addProperty(returnObj, "barcode", null);
            PluginResult result = new PluginResult(PluginResult.Status.OK, returnObj);
            result.setKeepCallback(true);
            mScanCallback.sendPluginResult(result);
        }
    }

    @Override
    public void OnHit(IBarcodeScanner.BarcodeInfo barcodeInfo, Image source) {
        //Log.d(TAG, "Start barcode");

        String barcode = null;
        if (barcodeInfo != null)
            barcode = barcodeInfo.barcode;

        Log.d(TAG, "Barcode: " + barcode);

        final byte[] serialized = (source == null) ? null : ImageDecoder.Serialize(source);
        final int width = (source == null) ? 0 : source.getWidth();
        final int height = (source == null) ? 0 : source.getHeight();
        final int format = (source == null) ? ImageFormat.UNKNOWN : source.getFormat();

        if (mScanCallback != null) {
            //Log.d(TAG, "Callback");
            JSONObject returnObj = new JSONObject();
            addProperty(returnObj, "barcode", barcode);
            PluginResult result = new PluginResult(PluginResult.Status.OK, returnObj);
            result.setKeepCallback(true);
            mScanCallback.sendPluginResult(result);
        }
    }

    @Override
    public void OnHits(IBarcodeScanner.BarcodeInfo[] barcodes, Image source) {
    //public void onMultipleBarcodeAvailable(IBarcodeScanner.BarcodeInfo[] barcodes, byte[] image, int format, int width, int height){

        final byte[] serialized = (source == null) ? null : ImageDecoder.Serialize(source);
        final int width = (source == null) ? 0 : source.getWidth();
        final int height = (source == null) ? 0 : source.getHeight();
        final int format = (source == null) ? ImageFormat.UNKNOWN : source.getFormat();

        if( mScanCallback != null ) {
            JSONObject returnObj = new JSONObject();
            addProperty(returnObj, "barcodes", barcodes);
            PluginResult result = new PluginResult(PluginResult.Status.OK, returnObj);
            result.setKeepCallback(true);
            mScanCallback.sendPluginResult(result);
        }
    }

    @Override
    public void OnError(Exception error){
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
    public void onResume(boolean multitasking) 
	{
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
                retryStartScanning(ctx);
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
