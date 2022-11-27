package com.microsoft.mobile.polymer.mishtu.ui.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.microsoft.mobile.polymer.mishtu.R;
import com.microsoft.mobile.polymer.mishtu.databinding.ViewTopContentBinding;
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload;
import com.microsoft.mobile.polymer.mishtu.ui.activity.ViewAllContentActivity;
import com.microsoft.mobile.polymer.mishtu.ui.adapter.TopContentAdapter;
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils;
import com.microsoft.mobile.polymer.mishtu.utils.OnItemClickListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

public class TopContentListView extends FrameLayout implements TopContentAdapter.OnMovieClickListener {

    ViewTopContentBinding topMoviesItemBinding;
    TopContentAdapter adapter;
    private Context context;
    private int width = 140;
    private int height = 210;
    private String title = "";
    private OnItemClickListener listener;
    private String contentProviderId = "";
    public static String CALLED_FROM = "TopContentListView";

    public TopContentListView(@NonNull Context context) {
        super(context);
        init(context);
        width = Math.round(context.getResources().getDimension(R.dimen.dp_140));
        height = Math.round(context.getResources().getDimension(R.dimen.dp_210));
    }

    public TopContentListView(@NonNull Context context, int width, int height) {
        super(context);
        this.width = width;
        this.height = height;
        init(context);
    }

    public TopContentListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TopContentListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        topMoviesItemBinding = ViewTopContentBinding.inflate(inflater, this, true);
        adapter = new TopContentAdapter(context, this, width, height,
                Math.round(context.getResources().getDimension(R.dimen.dp_13)),
                false,
                true,
                true,
                true,
                true);
        topMoviesItemBinding.recyclerView.setAdapter(adapter);
        topMoviesItemBinding.parentCardView.setVisibility(View.GONE);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }


    public void setData(List<ContentDownload> bookingModels, String title, Drawable icon, String contentProviderId) {
      Log.d("ContentProvider_tcl",bookingModels.toString());
        if (bookingModels.isEmpty()) {
            topMoviesItemBinding.parentCardView.setVisibility(GONE);
            topMoviesItemBinding.iconOtt.setVisibility(GONE);

        }
        else {
            topMoviesItemBinding.parentCardView.setVisibility(View.VISIBLE);
            //topMoviesItemBinding.lblTopMovies.setText(title);
            String freeText = "";
            if(bookingModels.get(0).isMovie()) {
                freeText = context.getString(R.string.text_free_movies);
            }
            else {
                freeText = context.getString(R.string.text_free_series);
            }
            topMoviesItemBinding.freeTextview.setText(freeText);

            this.title = title;
            if (icon != null) {
                AppUtils.Companion.loadImage(context, AppUtils.Companion.getContentProviderWaterMarkLogoURL(contentProviderId), topMoviesItemBinding.iconOtt);
                topMoviesItemBinding.iconOtt.setVisibility(VISIBLE);
            } else {
                topMoviesItemBinding.iconOtt.setVisibility(GONE);
            }
        }
        this.contentProviderId = contentProviderId;
        adapter.submitList(bookingModels);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onMovieClicked(@NonNull ContentDownload content) {
        if (listener != null) listener.onItemCLicked(content);
    }

    @Override
    public void onViewAllCLicked(@NotNull ContentDownload content) {
        HashMap<String,Object> additionalAttr = new HashMap();
        additionalAttr.put(ViewAllContentActivity.EXTRA_CONTENT,content) ;
        additionalAttr.put(ViewAllContentActivity.EXTRA_CALLED_FROM,TopContentListView.CALLED_FROM) ;
        AppUtils.Companion.startViewAllContentActivity(context,
                true,
                adapter.getHubConnected(),
                title,
                true,
                additionalAttr);
    }

    public void setHubConnected(Boolean connected) {
        adapter.setHubConnected(connected);
    }
}
