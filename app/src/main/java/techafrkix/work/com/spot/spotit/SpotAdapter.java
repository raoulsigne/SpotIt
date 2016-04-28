package techafrkix.work.com.spot.spotit;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import techafrkix.work.com.spot.bd.Spot;

/**
 * Created by techafrkix0 on 25/04/2016.
 */
public class SpotAdapter extends ArrayAdapter<Spot> {

    public SpotAdapter(Context context, ArrayList<Spot> spots) {
        super(context, 0, spots);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView spotDate;
        TextView spotHash;
        ImageView spotPhoto;

        // Get the data item for this position
        Spot spot = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.itemliste, parent, false);
        }

        // Lookup view for data population
        spotDate = (TextView)convertView.findViewById(R.id.txtDate);
        spotHash = (TextView)convertView.findViewById(R.id.txtHash);
        spotPhoto = (ImageView)convertView.findViewById(R.id.imgSpot);

        // Populate the data into the template view using the data object
        try {
            spotDate.setText(spot.getDate());
            spotHash.setText(spot.getGeohash());
            Bitmap bitmap = BitmapFactory.decodeFile(spot.getPhoto());

            Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            float scaleHt =(float) width/bitmap.getWidth();
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, (int) (bitmap.getWidth()*scaleHt), true);

            spotPhoto.setImageBitmap(scaled);
        }catch (Exception e){
            Log.e("spot", e.getMessage());}

        // Return the completed view to render on screen
        return convertView;
    }
}