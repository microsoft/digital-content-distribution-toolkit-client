package com.microsoft.mobile.polymer.mishtu.ui.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.microsoft.mobile.polymer.mishtu.R;
import com.microsoft.mobile.polymer.mishtu.databinding.ViewSearchBinding;

public class SearchView extends FrameLayout {

    ViewSearchBinding binding;

    private Context context;
    private OnSearchViewListener listener;
    private boolean isEditable;

    public SearchView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SearchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SearchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        binding = ViewSearchBinding.inflate(inflater, this, true);
        binding.idBackButton.setOnClickListener(view -> listener.onBackButtonCLicked());

        binding.idMic.setOnClickListener(view -> {
            Log.e("here","mic");
            if (isEditable) {
                binding.inputText.setText("");
                listener.onMicButtonClicked();
            } else {
                listener.onMicButtonClicked();
            }
        });

        binding.inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                listener.onTextChanged(editable.toString());

            }
        });

        binding.inputText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                listener.onInputEnterKeyCLicked(binding.inputText.getText().toString());
            }
            return false;
        });
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setData(String text, boolean isEditable, boolean showBack, boolean showSoftKeyBoard) {
        this.isEditable = isEditable;
        if (isEditable) {
            binding.idBackButton.setVisibility(showBack ? VISIBLE : GONE);
            binding.inputText.setEnabled(true);
            if (showSoftKeyBoard) {
                binding.inputText.setCursorVisible(true);
                binding.inputText.setHint(text);
            } else {
                binding.inputText.setCursorVisible(false);
                binding.inputText.setText(text);
            }
            if (!showBack) {
                //binding.idMic.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bn_close_black));
            }
            } else {
            binding.inputText.setFocusable(false);
            binding.inputText.setFocusable(false);
            binding.inputText.setEnabled(false);
            binding.inputText.setCursorVisible(false);
            binding.inputText.setKeyListener(null);
        }
    }

    public void setOnSearchViewListener(OnSearchViewListener listener) {
        this.listener = listener;
    }

    public interface OnSearchViewListener {

        void onBackButtonCLicked();

        void onMicButtonClicked();

        void onTextChanged(String charSequence);

        void onInputEnterKeyCLicked(String charSequence);
    }
}
