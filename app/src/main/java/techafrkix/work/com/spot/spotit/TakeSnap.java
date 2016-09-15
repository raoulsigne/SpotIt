package techafrkix.work.com.spot.spotit;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.content.Context;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;

public class TakeSnap extends Fragment implements View.OnClickListener, CameraCallback{

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    protected Preview mPreview;
    Camera mCamera;
    int numberOfCameras;
    int cameraCurrentlyLocked;
    public double longitude, latitude;
    Button button;

    Context _context;
    private OnFragmentInteractionListener mListener;

    // The first rear facing camera
    int defaultCameraId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _context = getActivity();

        try {
            longitude = getArguments().getDouble("longitude");
            latitude = getArguments().getDouble("latitude");
            Log.i("parametre", " longitude=" + longitude + "; latitude=" + latitude);
        }catch (Exception e){
            latitude = 0;
            longitude = 0;
        }
        // Hide the window title.
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Create a RelativeLayout container that will hold a SurfaceView,
        // and set it as the content of our activity.
        mPreview = new Preview(_context);
        mPreview.longitude = longitude;
        mPreview.latitude = latitude;

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

        FrameLayout layout = new FrameLayout(_context);
        FrameLayout.LayoutParams layoutparams=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT, Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        layout.setLayoutParams(layoutparams);
        layout.addView(mPreview);

        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        try {
            if (mCamera != null){
                PhotoHandler ph = new PhotoHandler(_context, mCamera);
                mCamera.takePicture(null, null, ph);
            }
        }
        catch (Exception e) {
            Log.d("Camera", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();


        try {
            releaseCameraAndPreview();
            mCamera = Camera.open();
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }


        // Open the default i.e. the first rear facing camera.

        cameraCurrentlyLocked = defaultCameraId;
        mPreview.setCamera(mCamera);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if (mCamera != null) {
            mPreview.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private void releaseCameraAndPreview() {
        mPreview.setCamera(null);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(_context,"Vous ne serez pas capable de faire des spots", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void detail(Bundle bundle) {
        mListener.onRegisterSpot(bundle);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onRegisterSpot(Bundle bundle);
    }
// ----------------------------------------------------------------------

    /**
     * A simple wrapper around a Camera and a SurfaceView that renders a centered preview of the Camera
     * to the surface. We need to center the SurfaceView because not all devices have cameras that
     * support preview sizes at the same aspect ratio as the device's display.
     */
    class Preview extends ViewGroup implements SurfaceHolder.Callback {
        private final String TAG = "Preview";
        private CameraCallback mListener;

        SurfaceView mSurfaceView;
        Button shutter;
        Button done,clear,cameramode;
        TextView txtValider, txtAnnuler;
        LinearLayout linearLayout;
        SurfaceHolder mHolder;
        Size mPreviewSize;
        List<Size> mSupportedPreviewSizes;
        Camera mCamera;

        public double longitude, latitude;
        private int currentCameraId;

        Preview(final Context context) {
            super(context);

            try {
                this.mListener = ((CameraCallback) TakeSnap.this);
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement AdapterCallback.");
            }

            currentCameraId = CameraInfo.CAMERA_FACING_BACK;
            //this.setBackgroundColor(getResources().getColor(R.color.fondsnap));
            mSurfaceView = new SurfaceView(context);
            addView(mSurfaceView);
            shutter = new Button(context);

            txtAnnuler = new TextView(context);
            txtAnnuler.setTextColor(getResources().getColor(R.color.blanc));
            txtAnnuler.setText("Cancel");
            txtValider = new TextView(context);
            txtValider.setTextColor(getResources().getColor(R.color.blanc));
            txtValider.setText("  Done");
            linearLayout = new LinearLayout(context);
            linearLayout.setBackgroundColor(getResources().getColor(R.color.fondsnap));
            cameramode = new Button(context);
            cameramode.setBackground(getResources().getDrawable(R.drawable.ic_refresh_white_24dp));
            done = new Button(context);
            done.setBackground(getResources().getDrawable(R.drawable.done));
            clear = new Button(context);
            clear.setBackground(getResources().getDrawable(R.drawable.cancel));
            shutter.setBackground(getResources().getDrawable(R.drawable.round_button));
            done.setVisibility(INVISIBLE);
            clear.setVisibility(INVISIBLE);
            txtAnnuler.setVisibility(INVISIBLE);
            txtValider.setVisibility(INVISIBLE);
            addView(shutter);
            addView(done);
            addView(clear);
            addView(cameramode);
            addView(linearLayout);
            addView(txtValider);
            addView(txtAnnuler);

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = mSurfaceView.getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            final PhotoHandler ph = new PhotoHandler(context, mCamera);

//            if (mCamera != null)
//                mCamera.setDisplayOrientation(90);
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

                            done.setVisibility(VISIBLE);
                            clear.setVisibility(VISIBLE);
                            txtAnnuler.setVisibility(VISIBLE);
                            txtValider.setVisibility(VISIBLE);
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
                            Log.i("parametre", photo);
                            Bundle bundle = new Bundle();
                            bundle.putDouble("longitude", longitude);
                            bundle.putDouble("latitude", latitude);
                            bundle.putString("image", photo);
                            Log.i("photo", photo);

                            mListener.detail(bundle);
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

                            done.setVisibility(INVISIBLE);
                            clear.setVisibility(INVISIBLE);
                            txtAnnuler.setVisibility(INVISIBLE);
                            txtValider.setVisibility(INVISIBLE);
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

                        Camera.Parameters parameters = mCamera.getParameters();
                        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        }
                        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            List<String> flashModes = parameters.getSupportedFlashModes();
                            if (flashModes == null || flashModes.isEmpty() || flashModes.size() == 1 && flashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {

                            }
                            else {
                                if (flashModes.contains(android.hardware.Camera.Parameters.FLASH_MODE_AUTO)) {
                                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                                }
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
                if (flashModes == null || flashModes.isEmpty() || flashModes.size() == 1 && flashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {

                }
                else {
                    if (flashModes.contains(android.hardware.Camera.Parameters.FLASH_MODE_AUTO)) {
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    }
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

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
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
                final View child5 = getChildAt(5);
                final View child6 = getChildAt(6);
                final View child7 = getChildAt(7);

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
                    Drawable d = getResources().getDrawable(R.drawable.done);
                    int pixels_btn = d.getIntrinsicWidth();

                    //calcul de la taille des éléments en fonction des pixels (unité dp)
                    final float scale = getContext().getResources().getDisplayMetrics().density;
                    int pixels = (int) (40 * scale + 0.5f);
                    //int pixels_btn = (int) (h_done * scale + 0.5f);
                    int padding = (int) (50 * scale + 0.5f);
                    int txt_width = (int) (45 * scale + 0.5f);

                    int hauteur = height - width;
                    //positionnement de la surface du preview
                    child.layout(0, 0, width, height);
                    //positionnement du bouton shutter
                    child1.layout(width/2-pixels, width+hauteur/2-pixels, width/2+pixels, width+hauteur/2+pixels);
                    //positionnement du bouton valider
                    child2.layout(padding, width+hauteur/2 - pixels_btn/2, padding + pixels_btn, width+hauteur/2 + pixels_btn/2);
                    //positionnement du texte du bouton valider
                    child6.layout(padding - txt_width/2 + pixels_btn/2, width+hauteur/2 + pixels_btn, padding + txt_width/2 + pixels_btn/2, width+hauteur/2 + 2*pixels_btn);
                    //positionnement du bouton annuler
                    child3.layout(width - padding - pixels_btn, width+hauteur/2 - pixels_btn/2, width - padding, width+hauteur/2 + pixels_btn/2);
                    //positionnement du texte du bouton annuler
                    child7.layout(width - padding - pixels_btn - txt_width/2 + pixels_btn/2, width+hauteur/2 + pixels_btn, width - padding + txt_width/2 + pixels_btn/2, width+hauteur/2 + 2*pixels_btn);
                    //positionnement du bouton de changement du mode de la camera
                    child4.layout(width/2-pixels_btn/2, 0, width/2+pixels_btn/2, pixels_btn);
                    //positionnement du layout qui va cacher une partie de l'écran et heberger les boutons
                    child5.layout(0, width, width, height);
                }
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, acquire the camera and tell it where
            // to draw.
            try {
                if (mCamera != null) {
                    mCamera.setPreviewDisplay(holder);
                    Log.i("test", "teqdqfqfqrgrg'gtghtdh");
                }
            } catch (IOException exception) {
                Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Surface will be destroyed when we return, so stop the preview.
            if (mCamera != null) {
                mCamera.stopPreview();
            }
        }

        private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
            final double ASPECT_TOLERANCE = 0.1;
            double targetRatio=(double)h / w;

            if (sizes == null) return null;

            Camera.Size optimalSize = null;
            double minDiff = Double.MAX_VALUE;

            int targetHeight = h;

            for (Camera.Size size : sizes) {
                double ratio = (double) size.width / size.height;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }

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
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // Now that the size is known, set up the camera parameters and begin
            // the preview.

            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(_context,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.CAMERA)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }else {
                try {
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                    requestLayout();

                    mCamera.setParameters(parameters);
                    mCamera.setDisplayOrientation(90);
                    mCamera.startPreview();
                }catch (Exception e){
                    Log.e("error", e.getMessage());
                }
            }
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

            //roter la photo pour permettre qu'elle apparaisse comme elle a été prise rotation de 90 dégré
            bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, height, height, matrix, true);
        }

        public String saveImage(int id){
            File pictureFileDir = getDir();
            String temps = String.valueOf(System.currentTimeMillis());
            photoFile = temps + ".jpg";

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
                int value = 0;
                if (height <= width) {
                    value = height;
                } else {
                    value = width;
                }
                Matrix matrix = new Matrix();
                matrix.postRotate(270);
                rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0,
                        value, value, matrix, true);
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                Bitmap resized = Bitmap.createScaledBitmap(rotatedBitmap, 800, 800, true);
                boolean result = resized.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                //fos.write(bytes);
                fos.close();
                if (result) {
                    Log.i("photo", filename);
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
            return new File(DBServer.DOSSIER_IMAGE);
        }
    }

}

interface CameraCallback{
    public void detail(Bundle bundle);
}

