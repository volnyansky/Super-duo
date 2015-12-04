package it.jaschke.alexandria.data;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by Stas on 15.11.15.
 */
public class ImageViewBinding {
    @BindingAdapter("bind:imageUrl")
    public static void loadImage(ImageView imageView, String v) {
        if (v==null || v.isEmpty()) {
            imageView.setImageResource(android.R.color.transparent);
            return;
        }
        Picasso.with(imageView.getContext()).load(v).into(imageView);
    }
}
