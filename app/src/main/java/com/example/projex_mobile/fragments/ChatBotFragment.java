package com.example.projex_mobile.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.R;
import com.example.projex_mobile.api.ApiService;
import com.example.projex_mobile.api.RetrofitClient;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatBotFragment extends Fragment {

    private LinearLayout messageContainer;
    private ScrollView messageScroll;
    private EditText edtQuestion;
    private TextView btnSend;
    private TextView typingBubble;
    private String token = "";
    private boolean waitingResponse = false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.chatbot_fragment, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences(
                "user_prefs",
                Context.MODE_PRIVATE
        );
        token = prefs.getString("token", "");

        messageContainer = view.findViewById(R.id.messageContainer);
        messageScroll = view.findViewById(R.id.messageScroll);
        edtQuestion = view.findViewById(R.id.edtQuestion);
        btnSend = view.findViewById(R.id.btnSend);

        TextView btnBack = view.findViewById(R.id.btnBack);
        TextView chipToday = view.findViewById(R.id.chipToday);
        TextView chipOverdue = view.findViewById(R.id.chipOverdue);
        TextView chipProjects = view.findViewById(R.id.chipProjects);
        LinearLayout chatHeader = view.findViewById(R.id.chatHeader);

        setupPanelDrag(chatHeader);

        btnBack.setOnClickListener(v -> requireActivity()
                .getSupportFragmentManager()
                .popBackStack());

        btnSend.setOnClickListener(v -> sendCurrentQuestion());

        edtQuestion.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendCurrentQuestion();
                return true;
            }
            return false;
        });

        chipToday.setOnClickListener(v -> submitQuestion("Liet ke task cua toi hom nay"));
        chipOverdue.setOnClickListener(v -> submitQuestion("Toi co task nao qua han khong?"));
        chipProjects.setOnClickListener(v -> submitQuestion("Tong quan cac project cua toi"));

        addBotMessage("Chao ban, toi co the tra loi ve task, project, deadline va thong bao trong Projex.");
    }

    private void sendCurrentQuestion() {
        submitQuestion(edtQuestion.getText().toString());
    }

    private void submitQuestion(String rawQuestion) {
        String question = rawQuestion == null ? "" : rawQuestion.trim();

        if (question.isEmpty()) {
            return;
        }

        if (waitingResponse) {
            Toast.makeText(
                    requireContext(),
                    "Dang doi chatbot tra loi",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    "Ban can dang nhap lai",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        edtQuestion.setText("");
        addUserMessage(question);
        setWaiting(true);
        showTyping();

        Map<String, String> body = new HashMap<>();
        body.put("question", question);

        ApiService apiService = RetrofitClient.getApiService(null);
        apiService.chatBot(token, body)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (!isAdded()) return;

                        setWaiting(false);
                        hideTyping();

                        if (response.isSuccessful() && response.body() != null) {
                            String answer = getString(response.body(), "answer");
                            if (answer == null || answer.trim().isEmpty()) {
                                answer = "Chatbot khong tra ve cau tra loi.";
                            }
                            addBotMessage(answer);
                        } else {
                            addBotMessage("Khong the lay cau tra loi. Ma loi: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        if (!isAdded()) return;

                        setWaiting(false);
                        hideTyping();
                        addBotMessage("Loi ket noi chatbot: " + t.getMessage());
                    }
                });
    }

    private void setWaiting(boolean waiting) {
        waitingResponse = waiting;
        btnSend.setEnabled(!waiting);
        btnSend.setAlpha(waiting ? 0.55f : 1f);
        edtQuestion.setEnabled(!waiting);
    }

    private void showTyping() {
        typingBubble = createBubble("Projex AI dang suy nghi...", false);
        typingBubble.setAlpha(0.72f);
        messageContainer.addView(typingBubble);
        scrollToBottom();
    }

    private void hideTyping() {
        if (typingBubble != null) {
            messageContainer.removeView(typingBubble);
            typingBubble = null;
        }
    }

    private void addUserMessage(String message) {
        messageContainer.addView(createBubble(message, true));
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        messageContainer.addView(createBubble(message, false));
        scrollToBottom();
    }

    private TextView createBubble(String message, boolean fromUser) {
        TextView bubble = new TextView(requireContext());
        bubble.setText(message);
        bubble.setTextColor(Color.WHITE);
        bubble.setTextSize(14f);
        bubble.setLineSpacing(dp(2), 1f);
        bubble.setPadding(dp(10), dp(8), dp(10), dp(8));
        bubble.setBackgroundResource(fromUser
                ? R.drawable.bg_chat_user_bubble
                : R.drawable.bg_chat_bot_bubble);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.width = Math.min(
                dp(230),
                (int) (getResources().getDisplayMetrics().widthPixels * 0.72f)
        );
        params.gravity = fromUser ? Gravity.END : Gravity.START;
        params.setMargins(0, dp(4), 0, dp(4));
        bubble.setLayoutParams(params);
        return bubble;
    }

    private void setupPanelDrag(View dragHandle) {
        View panel = requireActivity().findViewById(R.id.chatbot);
        if (panel == null || dragHandle == null) {
            return;
        }

        View parent = (View) panel.getParent();
        final float[] downRawX = new float[1];
        final float[] downRawY = new float[1];
        final float[] startX = new float[1];
        final float[] startY = new float[1];

        dragHandle.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downRawX[0] = event.getRawX();
                    downRawY[0] = event.getRawY();
                    startX[0] = panel.getX();
                    startY[0] = panel.getY();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - downRawX[0];
                    float dy = event.getRawY() - downRawY[0];

                    panel.setX(clamp(
                            startX[0] + dx,
                            0,
                            parent.getWidth() - panel.getWidth()
                    ));
                    panel.setY(clamp(
                            startY[0] + dy,
                            0,
                            parent.getHeight() - panel.getHeight()
                    ));
                    return true;

                default:
                    return false;
            }
        });
    }

    private void scrollToBottom() {
        messageScroll.post(() -> messageScroll.fullScroll(View.FOCUS_DOWN));
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

    private String getString(JsonObject object, String key) {
        if (object == null
                || !object.has(key)
                || object.get(key).isJsonNull()) {
            return null;
        }

        return object.get(key).getAsString();
    }
}
