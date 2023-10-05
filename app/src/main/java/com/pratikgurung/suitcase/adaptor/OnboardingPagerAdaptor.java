package com.pratikgurung.suitcase.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.material.button.MaterialButton;
import com.pratikgurung.suitcase.R;

public class OnboardingPagerAdaptor extends PagerAdapter {
    private int[] images;
    private String[] texts;
    private LayoutInflater layoutInflater;
    private Context context;

    public OnboardingPagerAdaptor(Context context, int[] images, String[] texts) {
        this.context = context;
        this.images = images;
        this.texts = texts;
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.onboarding_item, container, false);

        ImageView imageView = view.findViewById(R.id.imageViewOB);
        TextView textView = view.findViewById(R.id.textViewOB);

        imageView.setImageResource(images[position]);
        textView.setText(texts[position]);


        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
