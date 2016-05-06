package techafrkix.work.com.spot.techafrkix.work.com.spot.test;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import java.io.IOException;

import techafrkix.work.com.spot.spotit.DetailSpot_New;
import techafrkix.work.com.spot.spotit.R;

public class AddFromCameraActivity extends Activity implements View.OnClickListener{

    private Preview mPreview;
    Camera mCamera;
    int numberOfCameras;
    int cameraCurrentlyLocked;

    SurfaceView surfaceView;
    Button button;

    // The first rear facing camera
    int defaultCameraId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Create a RelativeLayout container that will hold a SurfaceView,
        // and set it as the content of our activity.
        mPreview = new Preview(this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate our menu which can gather user input for switching camera
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.camera_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.switch_cam:
                // check for availability of multiple cameras
                if (numberOfCameras == 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(this.getString(R.string.camera_alert))
                            .setNeutralButton("Close", null);
                    AlertDialog alert = builder.create();
                    alert.show();
                    return true;
                }

                // OK, we have multiple cameras.
                // Release this camera -> cameraCurrentlyLocked
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mPreview.setCamera(null);
                    mCamera.release();
                    mCamera = null;
                }

                // Acquire the next camera and request Preview to reconfigure
                // parameters.
                mCamera = Camera
                        .open((cameraCurrentlyLocked + 1) % numberOfCameras);
                cameraCurrentlyLocked = (cameraCurrentlyLocked + 1)
                        % numberOfCameras;
                mPreview.switchCamera(mCamera);

                // Start the preview
                mCamera.startPreview();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
    ImageView done,clear;
    SurfaceHolder mHolder;
    Size mPreviewSize;
    List<Size> mSupportedPreviewSizes;
    Camera mCamera;

    Preview(final Context context) {
        super(context);

        mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView);
        shutter = new Button(context);

        done = new ImageView(context);
        done.setBackground(getResources().getDrawable(R.drawable.ic_done_black_24dp));
        clear = new ImageView(context);
        clear.setBackground(getResources().getDrawable(R.drawable.ic_clear_black_24dp));
        shutter.setBackground(getResources().getDrawable(R.drawable.round_button));
        shutter.setWidth(200);
        shutter.setHeight(200);
        addView(shutter);
        addView(done);
        addView(clear);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //on prend une photo
        shutter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mCamera != null) {
                        PhotoHandler ph = new PhotoHandler(context, mCamera);
                        mCamera.takePicture(null, null, ph);
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
                        Bundle bundle = new Bundle();
                        Intent itDetailSpot = new Intent(context,DetailSpot_New.class);
                        itDetailSpot.putExtras(bundle);
                        context.startActivity(itDetailSpot);
                    }
                }
                catch (Exception e) {
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
                        mCamera.startPreview();
                    }
                }
                catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            requestLayout();
            try {
                mCamera.setPreviewDisplay(mHolder);
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
                Log.i("test", width +"  "+height);
                child.layout(0, 0, width, width);
                child1.layout(300, 800, 500, 1000);

                child2.layout(100, 850, 200, 1000);
                child3.layout(600, 850, 700, 1000);
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
    public PhotoHandler(Context context, Camera c) {
        this.context = context;
        this.camera = c;
        photoFile = "";
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

        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Matrix matrix = new Matrix();
        matrix.postRotate(270);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0,
                width, height, matrix, true);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = dateFormat.format(new Date());
        photoFile = "Picture_" + date + ".jpg";

        String filename = pictureFileDir.getPath() + File.separator + photoFile;

        File pictureFile = new File(filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            boolean result = rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            //fos.write(bytes);
            fos.close();
            if (result) {
                Toast.makeText(context, "New Image saved:" + photoFile,
                        Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(context, "Couldn't save image:" + photoFile,
                        Toast.LENGTH_LONG).show();
            }
            // camera.startPreview();
        } catch (Exception error) {
            Log.d("PhotoHandler", "File" + filename + "not saved: "
                    + error.getMessage());
            Toast.makeText(context, "Image could not be saved.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "CameraAPIDemo");
    }
}
