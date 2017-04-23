package com.example.sanat.seegreen;

import com.example.sanat.seegreen.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
@SuppressWarnings("ALL")
public class VisionActivity extends Activity implements SurfaceHolder.Callback{
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

/*
    private static final String CLIENT_ID = "kin9DY83dmGWnpn4kF_E2ys6wjmeciOAuWQbhmmS";
    private static final String PRIVATE_KEY = "ZyTk5dNKyiHOZzEBBG5v3yNh9efjMP6aGjUdcjfo";
*/
    private static final String CLIENT_ID="4b9a3d182d654d79a1bd545ef5c65858";
    private static final String PRIVATE_KEY="dc75137b9f764897809298012b186395";


    SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceHolder;
    Camera mCamera;
    TextView dummyVal;
    boolean mPreviewRunning;
    String vision_result;
    Button submit_button;
    boolean continue_scheduling_task;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vision);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });


        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
        submit_button = (Button) findViewById(R.id.submit_button);
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vision_result == "") {
                    Toast t = Toast.makeText(getApplicationContext(), "Result is Null. Continue to scan!", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.TOP|Gravity.CENTER, 0,0);
                    t.show();
                }
                else {
                    continue_scheduling_task = false;
                    timer.cancel();
                    mCamera.release();
                    mCamera = null;

                    Log.v("INSIDE INTENT", "STOPPING TIMEREEERRE");
                    Intent intent = new Intent(getApplicationContext(), VisionServerResult.class);
                    intent.putExtra("vision_result", vision_result);
                    startActivity(intent);
                }



            }
        });


        mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        vision_result = "";
        continue_scheduling_task = true;

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 50);

        if(mCamera != null) {
            mCamera.release();
            Log.v("HERRE", "reached here");
        }
//        openCamera();
            mCamera = Camera.open();
        callAsynchronousTask();
    }

    private Thread cameraThread = null;
    private void openCamera(){
        if(cameraThread == null) cameraThread = new Thread() {
            @Override
            public void run() {
                openHelper();
            }
        };
        cameraThread.start();
        cameraThread.run();

    }
    private void openHelper(){
        try{
            mCamera = Camera.open();
            Log.v("CAMERA", "Camera opened??");
        }
        catch (RuntimeException e) {
            Log.e("CAMERA LOG", "failed to open camera");
        }
    }

    private void callAsynchronousTask() {
        final Handler handler = new Handler();
        timer = new Timer();
        final TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCamera.takePicture(null,null, mPictureCallback);
                    }
                });
            }
        };
        if(continue_scheduling_task)
            timer.schedule(doAsynchronousTask,0,4000);
        else {
            Log.v("TIMER", "TIMER GONEQ!!!!!!!!!!!!!!");
            timer.cancel();
        }
    }

    List<String> result = new ArrayList<String>();

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @SuppressWarnings("ConstantConditions")
        public void onPictureTaken(byte[] data, Camera c) {
            c.startPreview();
//            dummyVal.setText(Environment.getExternalStorageDirectory().toString());
//            Log.v("FILE LOCATION", Environment.getExternalStorageDirectory().getAbsoluteFile().getAbsolutePath().toString());

            try {
                File f = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/"
                        + System.currentTimeMillis() + ".jpeg");

                Log.v ("File", f.getAbsolutePath());

                Log.v("HERE", "HERERE");
                FileOutputStream outStream = new FileOutputStream(f);
                outStream.write(data);
                outStream.close();

                Log.v("LOC", Environment.getExternalStorageDirectory().getAbsolutePath());

               // final ClarifaiClient client =  new ClarifaiBuilder(CLIENT_ID, PRIVATE_KEY).buildSync();

                String file_string = f.toString();
                MediaType binary = MediaType.parse("application/octet-stream; charset=utf-8");
                final OkHttpClient httpclient = new OkHttpClient();
                RequestBody body = RequestBody.create(binary, data);
               /* final List<ClarifaiOutput<Concept>> predictionResults =
                        client.getDefaultModels().generalModel() // You can also do Clarifai.getModelByID("id") to get custom models
                                .predict()
                                .withInputs(
                                        ClarifaiInput.forImage(ClarifaiImage.of(String.valueOf(body)))
                                        )
                                                .executeSync() // optionally, pass a ClarifaiClient parameter to override the default client instance with another one
                                                .get();
*/

                Request request = new Request.Builder()
                        .url("https://westus.api.cognitive.microsoft.com/vision/v1.0/analyze?visualFeatures=Tags")
                        .addHeader("Ocp-Apim-Subscription-Key", CLIENT_ID)
                        .post(body)
                        .build();
                Response response = null;
                httpclient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.v("FAIL", e.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String jsonStr = response.body().string();
                        Log.v("API resp", jsonStr);
                        //  String response =
                        //  btncapture.setBackgroundColor(Color.rgb(220,221,221));

                        result = parseJson(jsonStr);


                        if(result.size()!= 0) {
                            Log.v("JSON STR", result.toString());
                            /*runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // result.setText(resultJson);
                                }
                            });*/
                        }
                        else
                            Log.v("JSON STR", "Empty result");
                        response.close();
                    }
                });


                Log.v("REACHING HERE","ERERGERGE");
                // Prediction List Iteration
  /*              for (int i = 0; i < predictionResults.size(); i++) {

                    ClarifaiOutput<Concept> clarifaiOutput = predictionResults.get(i);

                    List<Concept> concepts = clarifaiOutput.data();

                    int k = 0;
                    if(concepts != null && concepts.size() > 0) {
                        for (int j = 0; j < concepts.size(); j++) {
                            if(!(concepts.get(j).name().equals("no person") ||
                                   concepts.get(j).name().equals("blur"))) {
                                result.add(concepts.get(j).name());
                                k++;
                                if(k>=5)
                                    break;
                            }
                        }
                    }
                }
*/

                if(result.size()!=0){
                    Toast.makeText(getApplicationContext(), result.toString(), Toast.LENGTH_LONG).show();
                    vision_result = "";
                    for(int i=0; i < result.size(); i++){
                        if(!result.get(i).equals("cluttered")){
                            vision_result += result.get(i) + ",";
                        }
                    }
                    vision_result = vision_result.substring(0, vision_result.length()-1);
                }
                else
                    Toast.makeText(getApplicationContext(), "[]", Toast.LENGTH_SHORT).show();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    private List<String> parseJson(String jsonStr)  {

        if(jsonStr!= null) {
            try{
                JSONObject jsonObj = new JSONObject(jsonStr);

                List<String> result = new ArrayList<String>();

                // Getting JSON Array node
                JSONArray lists = jsonObj.getJSONArray("tags");

                int j = 0;
                // looping through All Contacts
                for (int i = 0; i < lists.length(); i++) {
                    JSONObject c = lists.getJSONObject(i);

                    String[] values = c.getString("name").split(",");
                    for (String s : values) {
                        s = s.toLowerCase();
                        if (s.equals("indoor") || s.equals("indoors")) {
                        } else {
                            result.add(s);
                            j++;
                            if(j>=5)
                                break;
                        }
                    }
                }
                return result;

            }catch (final JSONException e) {
                Log.e("Tag", "Json parsing error: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
        }
        return null;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w,
                               int h) {
        if (mPreviewRunning) {
            //mCamera.stopPreview();
        }
        Camera.Parameters p = mCamera.getParameters();
        List<Camera.Size> previewSizes = p.getSupportedPreviewSizes();
        p.set("orientation", "portrait");

        Camera.Size prevSize = getOptimalPreviewSize(previewSizes, w, h);

        Camera.Size  previewSize = previewSizes.get(previewSizes.size()-1);
        // p.setPreviewSize(previewSize.width, previewSize.height);
        Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        if(display.getRotation() == Surface.ROTATION_0) {
            mCamera.setDisplayOrientation(90);
        }

        if(display.getRotation() == Surface.ROTATION_270){
            mCamera.setDisplayOrientation(180);
        }
        p.setPreviewSize(prevSize.width, prevSize.height);
        mCamera.setParameters(p);
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
        mPreviewRunning = true;

    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.2;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            //Log.d(TAG, "Checking size " + size.width + "w " + size.height
            //      + "h");
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the
        // requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // mCamera.stopPreview();
        mPreviewRunning = false;
        if(mCamera != null) {
            Log.v("Surface Destroyed", "INSIDE SURFACE DESTROYED");
            mCamera.release();
            continue_scheduling_task = false;
            mCamera = null;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };


    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onStop(){
        if(mCamera != null) {
            Log.v("Stop", "INSIDE STOP");
            mCamera.release();
            continue_scheduling_task = false;
            mCamera = null;
        }
        super.onStop();
    }

    @Override
    public void onRestart(){
        super.onRestart();
        if (mCamera == null)
            mCamera.open();
    }
    @Override
    public void onPause(){
        super.onPause();
        if(mCamera != null) {
           Log.v("ONPAUSE", "INside on Pause");
           mCamera.release();
           mCamera = null;
        }

    }

}

