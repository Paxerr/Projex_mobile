package com.example.projex_mobile;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.projex_mobile.fragments.AccountFragment;
import com.example.projex_mobile.fragments.ChatBotFragment;
import com.example.projex_mobile.fragments.HomeFragment;
import com.example.projex_mobile.fragments.NotificationFragment;
import com.example.projex_mobile.fragments.SpaceListFragment;
import com.example.projex_mobile.fragments.TaskFragment;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout navHome, navSpaces, navNotifications, navTask, navUser;
    private ImageView ivHome, ivSpaces, ivNotifications, ivTask, ivUser;
    private TextView tvHome, tvSpaces, tvNotifications, tvTask, tvUser;
    private TextView btnChatBot;
    private FrameLayout chatbotContainer;

    private static final int ACTIVE_COLOR = 0xFF85ADFF;
    private static final int INACTIVE_COLOR = 0xFF6B7280;
    private static final String KEY_SELECTED_TAB = "key_selected_tab";

    private int selectedTabId = R.id.nav_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        initViews();
        setupNavigation();
        setupChatBotButton();

        getSupportFragmentManager().addOnBackStackChangedListener(
                this::updateChatBotButtonVisibility
        );

        if (savedInstanceState != null) {

            selectedTabId =
                    savedInstanceState.getInt(
                            KEY_SELECTED_TAB,
                            R.id.nav_home
                    );

            setSelectedNav(selectedTabId);

        } else {

            showFragmentByTab(R.id.nav_home);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_SELECTED_TAB, selectedTabId);
        super.onSaveInstanceState(outState);
    }

    private void initViews() {
        navHome = findViewById(R.id.nav_home);
        navSpaces = findViewById(R.id.nav_spaces);
        navNotifications = findViewById(R.id.nav_notifications);
        navTask = findViewById(R.id.nav_tasks);
        navUser = findViewById(R.id.nav_user);

        ivHome = findViewById(R.id.iv_home);
        ivSpaces = findViewById(R.id.iv_spaces);
        ivNotifications = findViewById(R.id.iv_notifications);
        ivTask = findViewById(R.id.iv_tasks);
        ivUser = findViewById(R.id.iv_user);

        tvHome = findViewById(R.id.tv_home);
        tvSpaces = findViewById(R.id.tv_spaces);
        tvNotifications = findViewById(R.id.tv_notifications);
        tvTask = findViewById(R.id.tv_tasks);
        tvUser = findViewById(R.id.tv_user);
        chatbotContainer = findViewById(R.id.chatbot);
    }

    private void setupChatBotButton() {
        FrameLayout contentRoot = findViewById(android.R.id.content);

        btnChatBot = new TextView(this);
        btnChatBot.setText("AI");
        btnChatBot.setTextColor(Color.WHITE);
        btnChatBot.setTextSize(16f);
        btnChatBot.setTypeface(null, android.graphics.Typeface.BOLD);
        btnChatBot.setGravity(Gravity.CENTER);
        btnChatBot.setBackgroundResource(R.drawable.bg_chat_fab);
        btnChatBot.setElevation(dp(10));
        btnChatBot.setClickable(true);
        btnChatBot.setFocusable(true);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                dp(58),
                dp(58)
        );
        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.setMargins(0, 0, dp(18), dp(92));

        contentRoot.addView(btnChatBot, params);
        setupDraggableChatButton(contentRoot);
        updateChatBotButtonVisibility();
    }

    private void setupDraggableChatButton(FrameLayout contentRoot) {
        final float[] downRawX = new float[1];
        final float[] downRawY = new float[1];
        final float[] startX = new float[1];
        final float[] startY = new float[1];
        final boolean[] moved = new boolean[1];
        final int dragSlop = dp(6);

        btnChatBot.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downRawX[0] = event.getRawX();
                    downRawY[0] = event.getRawY();
                    startX[0] = v.getX();
                    startY[0] = v.getY();
                    moved[0] = false;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - downRawX[0];
                    float dy = event.getRawY() - downRawY[0];

                    if (Math.abs(dx) > dragSlop || Math.abs(dy) > dragSlop) {
                        moved[0] = true;
                    }

                    float nextX = clamp(
                            startX[0] + dx,
                            0,
                            contentRoot.getWidth() - v.getWidth()
                    );
                    float nextY = clamp(
                            startY[0] + dy,
                            0,
                            contentRoot.getHeight() - v.getHeight()
                    );

                    v.setX(nextX);
                    v.setY(nextY);
                    return true;

                case MotionEvent.ACTION_UP:
                    if (!moved[0]) {
                        v.performClick();
                        openChatBot();
                    }
                    return true;

                default:
                    return false;
            }
        });
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> showHome());
        navSpaces.setOnClickListener(v -> showSpaces());
        navNotifications.setOnClickListener(v -> showNotifications());
        navTask.setOnClickListener(v -> showTask());
        navUser.setOnClickListener(v -> showUser());
    }

    private void showHome() {
        selectedTabId = R.id.nav_home;
        showFragmentByTab(selectedTabId);
    }

    public void showHomeFromChild() {
        selectedTabId = R.id.nav_home;
        getSupportFragmentManager().popBackStackImmediate(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
        );
        showFragmentByTab(selectedTabId);
    }

    private void showSpaces() {
        selectedTabId = R.id.nav_spaces;
        showFragmentByTab(selectedTabId);
    }

    private void showNotifications() {
        selectedTabId = R.id.nav_notifications;
        showFragmentByTab(selectedTabId);
    }

    private void showTask() {
        selectedTabId = R.id.nav_tasks;
        showFragmentByTab(selectedTabId);
    }

    private void showUser() {
        selectedTabId = R.id.nav_user;
        showFragmentByTab(selectedTabId);
    }

    private void openChatBot() {
        if (chatbotContainer != null) {
            chatbotContainer.setVisibility(View.VISIBLE);
        }

        Fragment current = getSupportFragmentManager()
                .findFragmentById(R.id.chatbot);

        if (current instanceof ChatBotFragment) {
            return;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.chatbot, new ChatBotFragment())
                .addToBackStack(null)
                .commit();

        if (btnChatBot != null) {
            btnChatBot.setVisibility(View.GONE);
        }
    }

    private void updateChatBotButtonVisibility() {
        if (btnChatBot == null) {
            return;
        }

        Fragment current = getSupportFragmentManager()
                .findFragmentById(R.id.chatbot);
        boolean isChatOpen = current instanceof ChatBotFragment;

        if (chatbotContainer != null) {
            chatbotContainer.setVisibility(isChatOpen ? View.VISIBLE : View.GONE);
        }

        btnChatBot.setVisibility(isChatOpen ? View.GONE : View.VISIBLE);
    }

    private void showFragmentByTab(int tabId) {
        Fragment fragment;

        if (tabId == R.id.nav_spaces) {
            fragment = new SpaceListFragment();
        } else if (tabId == R.id.nav_notifications) {
            fragment = new NotificationFragment();
        } else if (tabId == R.id.nav_tasks) {
            fragment = new TaskFragment();
        } else if (tabId == R.id.nav_user) {
            fragment = new AccountFragment();
        } else {
            fragment = new HomeFragment();
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment current = fm.findFragmentById(R.id.frame_container);

        if (current != null && current.getClass().equals(fragment.getClass())) {
            setSelectedNav(tabId);
            return;
        }

        fm.beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();

        setSelectedNav(tabId);
        updateChatBotButtonVisibility();
    }

    private void setSelectedNav(int selectedId) {
        int[] ids = {
                R.id.nav_home,
                R.id.nav_spaces,
                R.id.nav_notifications,
                R.id.nav_tasks,
                R.id.nav_user
        };

        ImageView[] icons = {
                ivHome,
                ivSpaces,
                ivNotifications,
                ivTask,
                ivUser
        };

        TextView[] texts = {
                tvHome,
                tvSpaces,
                tvNotifications,
                tvTask,
                tvUser
        };

        for (int i = 0; i < ids.length; i++) {
            int color = ids[i] == selectedId ? ACTIVE_COLOR : INACTIVE_COLOR;
            icons[i].setColorFilter(color);
            texts[i].setTextColor(color);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private float clamp(float value, float min, float max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(value, max));
    }
}
