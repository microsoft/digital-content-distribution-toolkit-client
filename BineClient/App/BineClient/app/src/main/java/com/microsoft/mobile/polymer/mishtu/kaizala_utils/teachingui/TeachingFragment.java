package com.microsoft.mobile.polymer.mishtu.kaizala_utils.teachingui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.microsoft.mobile.polymer.mishtu.R;
import com.microsoft.mobile.polymer.mishtu.databinding.FragmentTeachingBinding;

public class TeachingFragment extends Fragment {

    private static final String LOG_TAG = "TeachingFragment";
    private FrameLayout mContent;
    //touch detector surface which can be used for anywhere whenever touch dismissible surface
    // is required like dismissing the tooltip
    protected FrameLayout mTouchDetector;
    private ToolTipRelativeLayout mContentOverlay;
    private RectangleOverlayView mConversationHolderOverlay;
    private FragmentTeachingBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_teaching, container, false);
        mContentOverlay = binding.teachingContentOverlay;
        mTouchDetector = binding.touchDetector;
        mConversationHolderOverlay =  binding.rectangleOverlay;

        return binding.getRoot();
    }

    @Override
    public void onPause() {
        super.onPause();
        ToolTipManager.getInstance().clearToolTipsAndHandlers();
    }

    public ToolTipRelativeLayout getTeachingOverlayView() {
        return mContentOverlay;
    }

    public FrameLayout getMainContent() {
        return mContent;
    }

    public RectangleOverlayView getConversationHolderOverlay() {
        return mConversationHolderOverlay;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        ToolTipManager.getInstance().showToolTipOnConfigChange(TeachingFragment.this);
    }

    @Override
    public void onDestroy() {
        if(mContent != null) {
            mContent.removeAllViews();
            mContent = null;
        }
        if(mContentOverlay != null) {
            mContentOverlay.removeAllViews();
            mContentOverlay = null;
        }
        if(mTouchDetector != null) {
            mTouchDetector.removeAllViews();
            mTouchDetector = null;
        }
        super.onDestroy();
    }
}
