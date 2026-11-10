package com.fer3oon.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
    final String GOLD = "#E8C46A";
    final String BG = "#080808";
    final String CARD = "#191919";
    TextView status;

    int dp(float n) {
        return (int)(n * getResources().getDisplayMetrics().density);
    }

    TextView makeText(String value, int size, String color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(Color.parseColor(color));
        t.setPadding(dp(10), dp(10), dp(10), dp(10));
        return t;
    }

    Button makeButton(String label, String color, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.BLACK);
        b.setTextSize(16);
        b.setTypeface(null, Typeface.BOLD);
        b.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(Color.parseColor(color))
        );
        b.setOnClickListener(v -> action.run());
        return b;
    }

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setPadding(dp(18), dp(20), dp(18), dp(20));
        root.setBackgroundColor(Color.parseColor(BG));
        scroll.addView(root);
        setContentView(scroll);

        TextView title = makeText("👑 Fer3oon", 32, GOLD);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, Typeface.BOLD);
        root.addView(title);

        TextView subtitle = makeText(
            "منصة التداول الذكية", 15, "#C9B987"
        );
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(16), dp(18), dp(16), dp(18));
        panel.setBackgroundColor(Color.parseColor(CARD));

        LinearLayout.LayoutParams panelParams =
            new LinearLayout.LayoutParams(-1, -2);
        panelParams.setMargins(0, dp(25), 0, dp(15));
        root.addView(panel, panelParams);

        TextView heading = makeText("حالة النظام", 20, GOLD);
        heading.setTypeface(null, Typeface.BOLD);
        panel.addView(heading);

        status = makeText("المحرك غير مفعّل", 18, "#FFFFFF");
        panel.addView(status);

        TextView note = makeText(
            "نسخة التطوير — التداول متوقف", 14, "#C9B987"
        );
        panel.addView(note);

        Button start = makeButton("▶ تشغيل", GOLD, () ->
            status.setText("المحرك غير مفعّل — التشغيل غير متاح حاليًا")
        );
        panel.addView(start, new LinearLayout.LayoutParams(-1, dp(55)));

        Button stop = makeButton("■ إيقاف", "#B83E48", () ->
            status.setText("النظام متوقف")
        );
        LinearLayout.LayoutParams stopParams =
            new LinearLayout.LayoutParams(-1, dp(55));
        stopParams.topMargin = dp(10);
        panel.addView(stop, stopParams);

        Button settings = makeButton(
            "⚙ إعدادات الحساب", GOLD, this::showAccountSettings
        );
        root.addView(settings, new LinearLayout.LayoutParams(-1, dp(55)));
    }

    void showAccountSettings() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(10), dp(20), dp(10));
        form.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        EditText emailField = new EditText(this);
        emailField.setHint("البريد الإلكتروني");
        emailField.setSingleLine(true);
        form.addView(emailField);

        EditText keyField = new EditText(this);
        keyField.setHint("Capital.com API Key");
        keyField.setSingleLine(true);
        keyField.setInputType(129);
        form.addView(keyField);

        EditText passwordField = new EditText(this);
        passwordField.setHint("كلمة مرور API");
        passwordField.setSingleLine(true);
        passwordField.setInputType(129);
        form.addView(passwordField);

        try {
            String[] saved = SecureCredentials.load(this);
            if (saved != null) {
                emailField.setText(saved[0]);
                keyField.setText(saved[1]);
                passwordField.setText(saved[2]);
            }
        } catch (Exception e) {
            Toast.makeText(this, "تعذر قراءة البيانات المحفوظة", Toast.LENGTH_LONG).show();
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("إعدادات حساب DEMO")
            .setView(form)
            .setPositiveButton("حفظ مشفّر", null)
            .setNegativeButton("إلغاء", null)
            .create();

        dialog.setOnShowListener(v -> dialog.getButton(-1).setOnClickListener(view -> {
            String email = emailField.getText().toString().trim();
            String key = keyField.getText().toString().trim();
            String password = passwordField.getText().toString();

            if (email.isEmpty() || key.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "أكمل بيانات الحساب", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                SecureCredentials.save(this, email, key, password);
                Toast.makeText(this, "تم حفظ بيانات DEMO بشكل مشفّر", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            } catch (Exception e) {
                Toast.makeText(this, "فشل الحفظ المشفّر", Toast.LENGTH_LONG).show();
            }
        }));

        dialog.show();
    }
}
