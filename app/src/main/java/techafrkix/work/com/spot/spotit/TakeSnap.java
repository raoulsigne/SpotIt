package techafrkix.work.com.spot.spotit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.app.AlertDialog;
import android.content.Context;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TakeSnap extends Activity implements View.OnClickListener{

    private Preview mPreview;
    Camera mCamera;
    int numberOfCameras;
    int cameraCurrentlyLocked;
    public double longitude, latitude;
    SurfaceView surfaceView;
    Button button;

    // The first rear facing camera
    int defaultCameraId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        longitude = extras.getDouble("longitude");
        latitude = extras.getDouble("latitude");
        Log.i("parametre", " longitude=" + longitude + "; latitude=" + latitude);

        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Create a RelativeLayout container that will hold a SurfaceView,
        // and set it as the content of our activity.
        mPreview = new Preview(this);
        mPreview.longitude = longitude;
        mPreview.latitude = latitude;
        setContentView(mPreview);

        // Find the total number of cameras available
        numberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the default camera
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                defaultCameraId = i;
            }
        }
    }

    @Override
    public void onClick(View view) {
        try {
            if (mCamera != null){
                PhotoHandler ph = new PhotoHandler(this, mCamera);
                mCamera.takePicture(null, null, ph);
            }
        }
        catch (Exception e) {
            Log.d("Camera", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Open the default i.e. the first rear facing camera.
        mCamera = Camera.open();
        cameraCurrentlyLocked = defaultCameraId;
        mPreview.setCamera(mCamera);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if (mCamera != null) {
            mPreview.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
    }
}

// ----------------------------------------------------------------------

/**
 * A simple wrapper around a Camera and a SurfaceView that renders a centered preview of the Camera
 * to the surface. We need to center the SurfaceView because not all devices have cameras that
 * support preview sizes at the same aspect ratio as the device's display.
 */
class Preview extends ViewGroup implements SurfaceHolder.Callback {
    private final String TAG = "Preview";

    SurfaceView mSurfaceView;
    Button shutter;
    ImageView done,clear,cameramode;
    SurfaceHolder mHolder;
    Size mPreviewSize;
    List<Size> mSupportedPreviewSizes;
    Camera mCamera;

    public double longitude, latitude;
    private int currentCameraId;

    Preview(final Context context) {
        super(context);
        currentCameraId = CameraInfo.CAMERA_FACING_BACK;
        this.setBackgroundColor(getResources().getColor(R.color.fondsnap));
        mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView);
        shutter = new Button(context);

        cameramode = new ImageView(context);
        cameramode.setBackground(getResources().getDrawable(R.drawable.ic_refresh_white_24dp));
        done = new ImageView(context);
        done.setBackground(getResources().getDrawable(R.drawable.ic_done_white_24dp));
        clear = new ImageView(context);
        clear.setBackground(getResources().getDrawable(R.drawable.ic_clear_white_24dp));
        shutter.setBackground(getResources().getDrawable(R.drawable.round_button));
        shutter.setWidth(200);
        shutter.setHeight(200);
        addView(shutter);
        addView(done);
        addView(clear);
        addView(cameramode);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        final PhotoHandler ph = new PhotoHandler(context, mCamera);

        if (mCamera != null)
            mCamera.setDisplayOrientation(90);
        //on prend une photo
        shutter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mCamera != null) {
                        mCamera.takePicture(
                                new Camera.ShutterCallback() { @Override public void onShutter() { } },
                                new Camera.PictureCallback() { @Override public void onPictureTaken(byte[] data, Camera camera) { } },
                                ph);
                    }
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        //enregistrement de la photo
        done.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mCamera != null) {
                        String photo = ph.saveImage(currentCameraId);
                        Bundle bundle = new Bundle();
                        bundle.putDouble("longitude", longitude);
                        bundle.putDouble("latitude", latitude);
                        bundle.putString("image", photo);
                        Log.i("parametre", " longitude=" + longitude + "; latitude=" + latitude);
                        Intent itDetailSpot = new Intent(context, DetailSpot_New.class);
                        itDetailSpot.putExtras(bundle);
                        context.startActivity(itDetailSpot);
                        ((Activity) context).finish();
                    }
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        //on relance le preview pour prendre une nouvelle photo
        clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mCamera != null) {
                        mCamera.setDisplayOrientation(90);
                        mCamera.startPreview();
                    }
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        //handle camera mode front and rear
        if (Camera.getNumberOfCameras() == 1) {
            cameramode.setVisibility(View.INVISIBLE);
        } else {
            cameramode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCamera != null) {
                        mCamera.stopPreview();
                    }
                    //NB: if you don't release the current camera before switching, you app will crash
                    mCamera.release();

                    //swap the id of the camera to be used
                    if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    } else {
                        currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                    }
                    mCamera = Camera.open(currentCameraId);
                    //Code snippet for this method from somewhere on android developers, i forget where
                    //setCameraDisplayOrientation(CameraActivity.this, currentCameraId, mCamera);
                    Camera.Parameters parameters = mCamera.getParameters();
                    if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    }
                    if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        List<String> flashModes = parameters.getSupportedFlashModes();
                        if (flashModes.contains(android.hardware.Camera.Parameters.FLASH_MODE_AUTO)) {
                            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                        }
                    }
                    mCamera.setParameters(parameters);
                    mCamera.setDisplayOrientation(90);
                    try {
                        //this step is critical or preview on new camera will no know where to render to
                        mCamera.setPreviewDisplay(mHolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mCamera.startPreview();
                }
            });
        }
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            Camera.Parameters parameters = camera.getParameters();
            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            List<String> flashModes = parameters.getSupportedFlashModes();
            if (flashModes.contains(android.hardware.Camera.Parameters.FLASH_MODE_AUTO))
            {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            }
            requestLayout();
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.setParameters(parameters);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void switchCamera(Camera camera) {
        setCamera(camera);
        try {
            camera.setPreviewDisplay(mHolder);
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
        Camera.Parameters parameters = camera.getParameters();
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes.contains(android.hardware.Camera.Parameters.FLASH_MODE_AUTO))
        {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        }
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        requestLayout();

        camera.setParameters(parameters);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            //add surface preview
            final View child = getChildAt(0);
            final View child1 = getChildAt(1);
            final View child2 = getChildAt(2);
            final View child3 = getChildAt(3);
            final View child4 = getChildAt(4);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
                child1.layout(0, 0, 200, 200);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                int hauteur = height - width;
                //positionnement de la surface du preview
                child.layout(0, 0, width, width);
                //positionnement du bouton shutter
                child1.layout(width/2-100, width + hauteur/2 - 100, width/2+100, width + hauteur/2 + 100);
                //positionnement du bouton valider
                child2.layout(50, width + hauteur/2 - 75, 125, width + hauteur/2 + 50);
                //positionnement du bouton annuler
                child3.layout(width/2+200, width + hauteur/2 - 75, width/2+275, width + hauteur/2 + 50);
                //positionnement du bouton de changement du mode de la camera
                child4.layout(width/2-40, 0, width/2+40, 50);
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }


    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        requestLayout();

        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);
        mCamera.startPreview();
    }
}

class PhotoHandler implements Camera.PictureCallback {
    private final Context context;
    private final Camera camera;
    public String photoFile;
    public Bitmap rotatedBitmap, bmp;
    public PhotoHandler(Context context, Camera c) {
        this.context = context;
        this.camera = c;
        photoFile = "";
        rotatedBitmap = null;
        bmp = null;
    }

    @Override
    public void onPictureTaken(byte[] bytes, Camera cam) {
        Log.i("PhotoHandler", "Picture taken!");
        File pictureFileDir = getDir();

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            Log.d("PhotoHandler", "Can't create directory to save image.");
            Toast.makeText(context, "Can't create directory to save image.",
                    Toast.LENGTH_LONG).show();
            return;

        }

        //roter la photo pour permettre qu'elle apparaisse comme elle a été prise rotation de 90 dégré
        bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0,
                width, height, matrix, true);
    }

    public String saveImage(int id){
        File pictureFileDir = getDir();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = dateFormat.format(new Date());
        photoFile = "Picture_" + date + ".jpg";

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            Log.d("PhotoHandler", "Can't create directory to save image.");
            Toast.makeText(context, "Can't create directory to save image.",
                    Toast.LENGTH_LONG).show();
            return "";

        }
        String filename = pictureFileDir.getPath() + File.separator + photoFile;
        File pictureFile = new File(filename);

        if (id == CameraInfo.CAMERA_FACING_FRONT) {
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            Matrix matrix = new Matrix();
            matrix.postRotate(270);
            rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0,
                    width, height, matrix, true);
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            boolean result = rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
            //fos.write(bytes);
            fos.close();
            if (result) {
                return filename;
            }
            else {
                Toast.makeText(context, "Couldn't save image:" + photoFile,
                        Toast.LENGTH_LONG).show();
                return "";
            }
            // camera.startPreview();
        } catch (Exception error) {
            Log.d("PhotoHandler", "File" + filename + "not saved: "
                    + error.getMessage());
            Toast.makeText(context, "Image could not be saved.",
                    Toast.LENGTH_LONG).show();
            return "";
        }
    }

    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "SpotItPictures");
    }
}
