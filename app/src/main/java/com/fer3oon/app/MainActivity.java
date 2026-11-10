package com.fer3oon.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;

import androidx.credentials.*;
import androidx.credentials.exceptions.*;
import com.google.android.libraries.identity.googleid.*;
import com.google.firebase.auth.*;

import java.util.concurrent.*;

public class MainActivity extends Activity {

    final String GOLD = "#E8C46A";
    final String BG = "#080808";
    final String CARD = "#191919";

    FirebaseAuth auth;
    CredentialManager credentialManager;
    ExecutorService executor;
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
            android.content.res.ColorStateList.valueOf(
                Color.parseColor(color)
            )
        );
        b.setOnClickListener(v -> action.run());
        return b;
    }

    LinearLayout createRoot() {
        ScrollView scroll = new ScrollView(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setPadding(dp(18), dp(20), dp(18), dp(20));
        root.setBackgroundColor(Color.parseColor(BG));

        scroll.addView(root);
        setContentView(scroll);
        return root;
    }

    void addHeader(LinearLayout root) {
        TextView title = makeText("👑 Fer3oon", 32, GOLD);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, Typeface.BOLD);
        root.addView(title);

        TextView subtitle = makeText("منصة التداول الذكية", 15, "#C9B987");
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle);
    }

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);

        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        auth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(getBaseContext());
        executor = Executors.newSingleThreadExecutor();

        if (auth.getCurrentUser() != null) {
            showDashboard();
        } else {
            showLoginScreen();
        }
    }

    void showLoginScreen() {
        LinearLayout root = createRoot();
        addHeader(root);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(16), dp(25), dp(16), dp(25));
        panel.setBackgroundColor(Color.parseColor(CARD));

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(45), 0, dp(20));
        root.addView(panel, params);

        TextView heading = makeText("مرحبًا بك في Fer3oon", 21, GOLD);
        heading.setGravity(Gravity.CENTER);
        heading.setTypeface(null, Typeface.BOLD);
        panel.addView(heading);

        TextView note = makeText(
            "سجّل الدخول باستخدام حساب Google للمتابعة",
            15, "#FFFFFF"
        );
        note.setGravity(Gravity.CENTER);
        panel.addView(note);

        Button login = makeButton(
            "🌐 تسجيل الدخول باستخدام Google",
            GOLD,
            this::startGoogleSignIn
        );

        LinearLayout.LayoutParams buttonParams =
            new LinearLayout.LayoutParams(-1, dp(58));
        buttonParams.topMargin = dp(20);
        panel.addView(login, buttonParams);

        TextView footer = makeText(
            "تسجيل الدخول لا يشغّل محرك التداول",
            13, "#C9B987"
        );
        footer.setGravity(Gravity.CENTER);
        root.addView(footer);
    }

    void startGoogleSignIn() {
        Toast.makeText(this, "جاري فتح تسجيل الدخول...", Toast.LENGTH_SHORT).show();

        GetGoogleIdOption googleOption =
            new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(
                    getString(R.string.default_web_client_id)
                )
                .build();

        GetCredentialRequest request =
            new GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build();

        credentialManager.getCredentialAsync(
            this,
            request,
            new CancellationSignal(),
            executor,
            new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                @Override
                public void onResult(GetCredentialResponse result) {
                    runOnUiThread(() ->
                        handleGoogleCredential(result.getCredential())
                    );
                }

                @Override
                public void onError(GetCredentialException e) {
                    runOnUiThread(() ->
                        Toast.makeText(
                            MainActivity.this,
                            "تعذر تسجيل الدخول: " + e.getMessage(),
                            Toast.LENGTH_LONG
                        ).show()
                    );
                }
            }
        );
    }

    void handleGoogleCredential(Credential credential) {
        if (!(credential instanceof CustomCredential)
            || !GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                .equals(credential.getType())) {

            Toast.makeText(
                this,
                "نوع حساب Google غير مدعوم",
                Toast.LENGTH_LONG
            ).show();
            return;
        }

        try {
            CustomCredential custom = (CustomCredential) credential;

            GoogleIdTokenCredential googleCredential =
                GoogleIdTokenCredential.createFrom(custom.getData());

            AuthCredential firebaseCredential =
                GoogleAuthProvider.getCredential(
                    googleCredential.getIdToken(),
                    null
                );

            auth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(
                            this,
                            "تم تسجيل الدخول بنجاح",
                            Toast.LENGTH_SHORT
                        ).show();
                        showDashboard();
                    } else {
                        Toast.makeText(
                            this,
                            "فشل تسجيل الدخول إلى Firebase",
                            Toast.LENGTH_LONG
                        ).show();
                    }
                });

        } catch (Exception e) {
            Toast.makeText(
                this,
                "تعذر قراءة بيانات حساب Google",
                Toast.LENGTH_LONG
            ).show();
        }
    }


    LinearLayout dashboardCard(String heading) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(15), dp(16), dp(15));
        card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        android.graphics.drawable.GradientDrawable bg =
            new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#202020"), Color.parseColor("#111111")}
            );
        bg.setCornerRadius(dp(18));
        bg.setStroke(dp(1), Color.parseColor("#66512B"));
        card.setBackground(bg);
        card.setElevation(dp(3));

        TextView title = makeText(heading, 17, GOLD);
        title.setTypeface(null, Typeface.BOLD);
        card.addView(title);

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(9), 0, dp(12));
        return card;
    }

    void addMetric(LinearLayout row, String label, String value) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(5), dp(10), dp(5), dp(10));

        TextView number = makeText(value, 20, "#FFFFFF");
        number.setTypeface(null, Typeface.BOLD);
        number.setGravity(Gravity.CENTER);
        item.addView(number);

        TextView caption = makeText(label, 12, "#B9A77A");
        caption.setGravity(Gravity.CENTER);
        item.addView(caption);

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(0, -2, 1);
        row.addView(item, params);
    }

    void showDashboard() {
        LinearLayout root = createRoot();
        addHeader(root);

        TextView brand = makeText("لوحة القيادة الملكية", 14, "#B9A77A");
        brand.setGravity(Gravity.CENTER);
        root.addView(brand);

        LinearLayout account = dashboardCard("👑  الحساب والمحفظة");
        TextView demo = makeText("حساب التداول التجريبي DEMO", 15, "#FFFFFF");
        account.addView(demo);

        String accountState = "حساب DEMO غير مربوط";
        try {
            String[] saved = SecureCredentials.load(this);
            if (saved != null && saved.length >= 3 &&
                !saved[0].isEmpty() && !saved[1].isEmpty() &&
                !saved[2].isEmpty()) {
                accountState = "بيانات حساب DEMO محفوظة بشكل مشفّر";
            }
        } catch (Exception ignored) {}

        TextView accountInfo = makeText(accountState, 13, "#C9B987");
        account.addView(accountInfo);

        LinearLayout metrics = new LinearLayout(this);
        metrics.setOrientation(LinearLayout.HORIZONTAL);
        metrics.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        addMetric(metrics, "الرصيد", "—");
        addMetric(metrics, "الأرباح", "—");
        addMetric(metrics, "الخسائر", "—");
        account.addView(metrics);
        root.addView(account);

        LinearLayout chart = dashboardCard("📈  الرسم البياني");
        TextView chartPlaceholder = makeText(
            "بانتظار الاتصال بمصدر الأسعار\nستظهر حركة السوق هنا عند الربط",
            14, "#AAAAAA"
        );
        chartPlaceholder.setGravity(Gravity.CENTER);
        chartPlaceholder.setPadding(0, dp(25), 0, dp(25));
        chart.addView(chartPlaceholder);
        root.addView(chart);

        LinearLayout system = dashboardCard("⚙  حالة النظام");
        status = makeText("● المحرك متوقف", 18, "#E8C46A");
        status.setTypeface(null, Typeface.BOLD);
        system.addView(status);

        TextView connection = makeText(
            "الاتصال بالوسيط: غير متصل",
            13, "#BBBBBB"
        );
        system.addView(connection);

        TextView mode = makeText(
            "وضع التشغيل: DEMO فقط",
            13, "#BBBBBB"
        );
        system.addView(mode);
        root.addView(system);

        LinearLayout controls = dashboardCard("التحكم في المحرك");

        Button start = makeButton(
            "▶  تشغيل المحرك",
            "#D8B45D",
            () -> status.setText("المحرك متوقف — التشغيل غير متاح في نسخة التطوير")
        );
        controls.addView(start, new LinearLayout.LayoutParams(-1, dp(58)));

        Button stop = makeButton(
            "■  إيقاف المحرك",
            "#B94A50",
            () -> status.setText("● المحرك متوقف")
        );
        LinearLayout.LayoutParams stopParams =
            new LinearLayout.LayoutParams(-1, dp(58));
        stopParams.topMargin = dp(10);
        controls.addView(stop, stopParams);
        root.addView(controls);

        Button settings = makeButton(
            "⚙  إعدادات حساب الوسيط",
            "#D8B45D",
            this::showAccountSettings
        );
        LinearLayout.LayoutParams settingsParams =
            new LinearLayout.LayoutParams(-1, dp(55));
        settingsParams.topMargin = dp(5);
        root.addView(settings, settingsParams);

        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            TextView accountEmail = makeText(
                "حساب Google: " + user.getEmail(),
                12, "#B9A77A"
            );
            accountEmail.setGravity(Gravity.CENTER);
            root.addView(accountEmail);
        }

        TextView disclaimer = makeText(
            "نسخة تطوير — لا يتم إرسال أوامر تداول",
            12, "#888888"
        );
        disclaimer.setGravity(Gravity.CENTER);
        root.addView(disclaimer);
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
            Toast.makeText(
                this,
                "تعذر قراءة البيانات المحفوظة",
                Toast.LENGTH_LONG
            ).show();
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("إعدادات حساب DEMO")
            .setView(form)
            .setPositiveButton("حفظ مشفّر", null)
            .setNeutralButton("تسجيل الخروج من Google", (d, w) -> signOutGoogle())
            .setNegativeButton("إلغاء", null)
            .create();

        dialog.setOnShowListener(v ->
            dialog.getButton(-1).setOnClickListener(view -> {
                String email = emailField.getText().toString().trim();
                String key = keyField.getText().toString().trim();
                String password = passwordField.getText().toString();

                if (email.isEmpty() || key.isEmpty() || password.isEmpty()) {
                    Toast.makeText(
                        this,
                        "أكمل بيانات الحساب",
                        Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                try {
                    SecureCredentials.save(this, email, key, password);
                    Toast.makeText(
                        this,
                        "تم حفظ بيانات DEMO بشكل مشفّر",
                        Toast.LENGTH_LONG
                    ).show();
                    dialog.dismiss();
                } catch (Exception e) {
                    Toast.makeText(
                        this,
                        "فشل الحفظ المشفّر",
                        Toast.LENGTH_LONG
                    ).show();
                }
            })
        );

        dialog.show();
    }

    void signOutGoogle() {
        auth.signOut();

        showLoginScreen();

        credentialManager.clearCredentialStateAsync(
            new ClearCredentialStateRequest(),
            new CancellationSignal(),
            executor,
            new CredentialManagerCallback<Void, ClearCredentialException>() {
                @Override
                public void onResult(Void result) {
                }

                @Override
                public void onError(ClearCredentialException e) {
                    android.util.Log.e(
                        "Fer3oon",
                        "Credential state clearing failed",
                        e
                    );
                }
            }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
