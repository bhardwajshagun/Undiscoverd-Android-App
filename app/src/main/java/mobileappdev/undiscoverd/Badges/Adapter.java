package mobileappdev.undiscoverd.Badges;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import mobileappdev.undiscoverd.R;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    List<String> imageTitles; // list of Image Titles
    List<Integer> images; // list of images
    LayoutInflater inflater; // layout inflator

    List<Integer> badgePositions;

    // Constructor of the Adapter class
    public Adapter(Context c, List<String> imageTitles, List<Integer> images, List<Integer> bp) {
        this.imageTitles = imageTitles;
        this.images = images;
        this.inflater = LayoutInflater.from(c);
        this.badgePositions = bp;
    }

    // view holder on createViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.badges, parent, false);
        String toastString;
        if (badgePositions.size() > 0) {
            toastString = "You have " + badgePositions.size() + " badges! Click the tiles to see which ones you unlocked!";
        } else {
            toastString = "You have no badges unlocked";
        }
        Toast.makeText(view.getContext(), toastString, Toast.LENGTH_LONG).show();
        return new ViewHolder(view, badgePositions);
    }

    // binds the text and images to the viewholder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(imageTitles.get(position));
        holder.gridIcon.setImageResource(images.get(position));
    }

    // gets the item count titles
    @Override
    public int getItemCount() {
        return imageTitles.size();
    }

    //View holder class
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        ImageView gridIcon;

        // viewholder method
        public ViewHolder(@NonNull View itemView, List<Integer> validBadges) {
            super(itemView);
            title = itemView.findViewById(R.id.badgesTitle);
            gridIcon = itemView.findViewById(R.id.badgesImages);
            title.setVisibility(View.INVISIBLE);
            gridIcon.setVisibility(View.INVISIBLE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (validBadges.contains(getAdapterPosition())) {
                        gridIcon.setVisibility(v.VISIBLE);
                        title.setVisibility(v.VISIBLE);
                    }
                }
            });

        }

    }
}
