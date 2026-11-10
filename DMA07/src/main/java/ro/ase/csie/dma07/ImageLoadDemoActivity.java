package ro.ase.csie.dma07;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

/**
 * Example 6 — load an image from a URL the right way. Glide does the network
 * download on a background thread, caches it, and binds it to the ImageView —
 * no manual threading, no BitmapFactory. A fresh random image each tap proves
 * the network fetch is really happening.
 */
public class ImageLoadDemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_image_demo);

        ImageView iv = findViewById(R.id.ivPoster);
        Button btnLoad = findViewById(R.id.btnLoad);

        btnLoad.setOnClickListener(v ->
                Glide.with(this)
                        .load("https://picsum.photos/600/900?random=" + System.currentTimeMillis())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .centerCrop()
                        .into(iv));
    }
}
