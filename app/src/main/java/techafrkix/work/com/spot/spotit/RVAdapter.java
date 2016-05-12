package techafrkix.work.com.spot.spotit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import techafrkix.work.com.spot.bd.Spot;

/**
 * Created by techafrkix0 on 19/04/2016.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.SpotViewHolder>{

    List<Spot> spots;

    public static class SpotViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView spotDate;
        TextView spotHash;
        ImageView spotPhoto;

        SpotViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            spotDate = (TextView)itemView.findViewById(R.id.txtDate);
            spotHash = (TextView)itemView.findViewById(R.id.txtHash);
            spotPhoto = (ImageView)itemView.findViewById(R.id.imgSpot);
        }
    }

    RVAdapter(List<Spot> spots){
        this.spots = spots;
    }

    @Override
    public int getItemCount() {
        return spots.size();
    }

    @Override
    public SpotViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.liste_spots, viewGroup, false);
        SpotViewHolder pvh = new SpotViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(SpotViewHolder spotViewHolder, int i) {
        try {
            spotViewHolder.spotDate.setText(spots.get(i).getDate());
            spotViewHolder.spotHash.setText(spots.get(i).getGeohash());
            Bitmap bitmap = BitmapFactory.decodeFile(spots.get(i).getPhotokey());
            spotViewHolder.spotPhoto.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 350, 250, false));
        }catch (Exception e){
            Log.e("spot", e.getMessage());}
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
