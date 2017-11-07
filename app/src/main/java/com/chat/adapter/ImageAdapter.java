package com.chat.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.chat.R;
import com.chat.ui.MainActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;


/**
 * Created by Sergey on 03.03.2017.
 */

public class ImageAdapter extends PagerAdapter {
    private Context context;
    private List<String> imagePath;
    private LayoutInflater inflater;
    private Bitmap myBitmap;
    private Handler handler;
    private OnClickListener onClickListener;
    private boolean scaleTypeCenter;
    private boolean isRemove;
    private boolean isButtonOpen;
//    private ProgressBarControl progress;

    public interface OnClickListener {
        void onClick(int position);

        void onClickDelete(String pathDel);

    }

    public ImageAdapter(Context context, List<String> imagePath, OnClickListener onClickListener, boolean scaleType, boolean isRemove) {
        this.context = context;
        this.imagePath = imagePath;
        this.onClickListener = onClickListener;
        this.scaleTypeCenter = scaleType;
        this.isRemove = isRemove;
        handler = new Handler();

//        if (context instanceof ProgressBarControl) {
//            progress = (ProgressBarControl) context;
//        } else {
//            //  throw new ClassCastException( " must implement ProgressBarControl");//context.toString() +
//        }
    }

    @Override
    public int getCount() {
        return imagePath == null ? 1 : imagePath.size();
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (RelativeLayout) object;
    }


    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        if (context != null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        final View view = inflater.inflate(R.layout.item_images, container, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageViewPager);
        final ImageView imageViewDelteBtn = (ImageView) view.findViewById(R.id.imageDeleteBtn);


        Callback callback = new Callback() {
            @Override
            public void onSuccess() {
//                progress.hideProgress();
            }

            @Override
            public void onError() {
//                progress.hideProgress();
            }
        };

        if (scaleTypeCenter) imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        final String path = imagePath != null ? imagePath.get(position) : "";

        if (path.contains("https://")) {
//            progress.showProgress();
            Picasso.with(context)
                    .load(path)
                    .placeholder(context.getResources().getDrawable(R.drawable.placeholder))
                    .error(context.getResources().getDrawable(R.drawable.ic_error_image))
                    .into(imageView, callback);
        } else {
//            progress.showProgress();
            Picasso.with(context)
                    .load(new File(path))
                    .placeholder(context.getResources().getDrawable(R.drawable.placeholder))
                    .error(context.getResources().getDrawable(R.drawable.ic_error_image))
                    .into(imageView, callback);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.layout_btn_delete);
                if (isRemove && !isButtonOpen) {
                    isButtonOpen = true;
                    imageViewDelteBtn.setVisibility(View.VISIBLE);
                    animationBtnDelete(linearLayout, 160f, 0f);
                } else {
                    isButtonOpen = false;
                    animationBtnDelete(linearLayout, 0f, 160f);
                }
                imageViewDelteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (onClickListener != null)
                            onClickListener.onClickDelete(path);
                        ((MainActivity) context).onBackPressed();
                    }
                });
                if (onClickListener != null)
                    onClickListener.onClick(position);
            }
        });
        container.addView(view);

        return view;
    }

    private void animationBtnDelete(LinearLayout linearLayout, float stert, float end) {
        ObjectAnimator animation = ObjectAnimator.ofFloat(linearLayout, "translationY", stert, end);
        animation.setDuration(500);
        animation.start();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
}
