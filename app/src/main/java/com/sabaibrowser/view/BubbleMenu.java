package com.sabaibrowser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.sabaibrowser.R;

public class BubbleMenu extends FrameLayout {

    boolean isOpen = false;
    Bubble mainFab;

    public BubbleMenu(Context context) {
        super(context);
        init();
    }

    public BubbleMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.bubble_menu, this);
        mainFab = (Bubble) findViewById(R.id.main_fab);
        mainFab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleOpenMenu();
            }
        });
    }

    void toggleOpenMenu() {
        if (isOpen) {
            mainFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_white));
        } else {
            mainFab.setImageDrawable(getResources().getDrawable(R.drawable.logo_fab_icon));
        }
        isOpen = !isOpen;
    }
}
