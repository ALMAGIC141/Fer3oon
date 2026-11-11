package com.fer3oon.app;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;

import androidx.credentials.*;
import androidx.credentials.exceptions.*;
import com.google.android.libraries.identity.googleid.*;
import com.google.firebase.auth.*;

import java.util.concurrent.*;

public class MainActivity extends Activity {

    // لوحة الألوان الملكية الفرعونية الفاخرة (Cinematic Obsidian Black & Royal Gold)
    private static final String COLOR_BG_DEEP       = "#070707"; // أسود سبجي فائق العمق
    private static final String COLOR_CARD_TOP      = "#1B1711"; // تدرج البطاقة العلوي
    private static final String COLOR_CARD_BOTTOM   = "#0E0C09"; // تدرج البطاقة السفلي
    private static final String COLOR_GOLD_ROYAL    = "#E6C364"; // ذهب ملكي ساطع
    private static final String COLOR_GOLD_BRIGHT   = "#F7E6B8"; // ذهب مشرق للنصوص الرئيسية
    private static final String COLOR_GOLD_BORDER   = "#6B582C"; // حدود ذهبية نحاسية رفيعة
    private static final String COLOR_GOLD_BADGE_BG = "#2E2412"; // خلفية شارات الحالة
    private static final String COLOR_TEXT_MUTED    = "#A69B89"; // نصوص رمادية ذهبية خافتة
    private static final String COLOR_TEXT_SECONDARY= "#8A8172"; // نصوص التذييل والتوضيحات
    private static final String COLOR_SUCCESS       = "#34D399"; // أخضر زمردي للربح
    private static final String COLOR_DANGER        = "#F87171"; // أحمر ملكي هادئ للخسارة

    FirebaseAuth auth;
    CredentialManager credentialManager;
    ExecutorService executor;
    TextView status;
    Button googleLoginButton;
    boolean isSigningIn = false;

    // دوال مساعدة لإنشاء عناصر واجهة المستخدم الملكية (UI Helpers)
    int dp(float n) {
        return (int) (n * getResources().getDisplayMetrics().density);
    }

    TextView makeText(String value, int size, String color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(Color.parseColor(color));
        if (bold) {
            t.setTypeface(null, Typeface.BOLD);
        }
        return t;
    }

    TextView makeText(String value, int size, String color) {
        return makeText(value, size, color, false);
    }

    LinearLayout createRoyalCard(String titleText, String badgeText) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor(COLOR_CARD_TOP), Color.parseColor(COLOR_CARD_BOTTOM)}
        );
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1), Color.parseColor(COLOR_GOLD_BORDER));
        card.setBackground(bg);
        card.setElevation(dp(4));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(params);

        if (titleText != null && !titleText.isEmpty()) {
            LinearLayout headerRow = new LinearLayout(this);
            headerRow.setOrientation(LinearLayout.HORIZONTAL);
            headerRow.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            headerRow.setGravity(Gravity.CENTER_VERTICAL);
            headerRow.setPadding(0, 0, 0, dp(10));

            TextView title = makeText(titleText, 16, COLOR_GOLD_BRIGHT, true);
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            );
            headerRow.addView(title, titleParams);

            if (badgeText != null && !badgeText.isEmpty()) {
                TextView badge = makeText(badgeText, 11, COLOR_GOLD_ROYAL, true);
                badge.setPadding(dp(8), dp(3), dp(8), dp(3));
                GradientDrawable badgeBg = new GradientDrawable();
                badgeBg.setColor(Color.parseColor(COLOR_GOLD_BADGE_BG));
                badgeBg.setCornerRadius(dp(8));
                badgeBg.setStroke(dp(1), Color.parseColor(COLOR_GOLD_BORDER));
                badge.setBackground(badgeBg);
                headerRow.addView(badge);
            }

            card.addView(headerRow);
        }

        return card;
    }

    Button createRoyalButton(String label, String bgColor, String textColor, String strokeColor, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.parseColor(textColor));
        b.setTextSize(15);
        b.setTypeface(null, Typeface.BOLD);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(bgColor));
        bg.setCornerRadius(dp(12));
        if (strokeColor != null) {
            bg.setStroke(dp(1), Color.parseColor(strokeColor));
        }
        b.setBackground(bg);
        b.setOnClickListener(v -> action.run());
        return b;
    }

    LinearLayout setupRootScroll() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor(COLOR_BG_DEEP));
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setPadding(dp(18), dp(22), dp(18), dp(24));
        root.setBackgroundColor(Color.parseColor(COLOR_BG_DEEP));

        scroll.addView(root);
        setContentView(scroll);
        return root;
    }

    void addRoyalHeader(LinearLayout root, String subtitleText) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER);
        header.setPadding(0, dp(10), 0, dp(18));

        TextView brand = makeText("🦁👑 Fer3oon", 30, COLOR_GOLD_BRIGHT, true);
        brand.setGravity(Gravity.CENTER);
        header.addView(brand);

        TextView title = makeText("المنصة الملكية للتداول الذكي", 14, COLOR_GOLD_ROYAL, false);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(3), 0, dp(2));
        header.addView(title);

        if (subtitleText != null && !subtitleText.isEmpty()) {
            TextView sub = makeText(subtitleText, 12, COLOR_TEXT_MUTED, false);
            sub.setGravity(Gravity.CENTER);
            header.addView(sub);
        }

        TextView sandbox = makeText("بيئة تجريبية معزولة ومحمية • DEMO Sandbox", 11, COLOR_TEXT_SECONDARY, false);
        sandbox.setGravity(Gravity.CENTER);
        sandbox.setPadding(0, dp(4), 0, 0);
        header.addView(sandbox);

        root.addView(header);
    }

    void addMetricCell(LinearLayout row, String label, String value, String valueColor) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(dp(6), dp(10), dp(6), dp(10));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#120F0B"));
        bg.setCornerRadius(dp(10));
        bg.setStroke(dp(1), Color.parseColor("#3D3219"));
        cell.setBackground(bg);

        TextView val = makeText(value, 18, valueColor, true);
        val.setGravity(Gravity.CENTER);
        cell.addView(val);

        TextView lbl = makeText(label, 12, COLOR_TEXT_MUTED, false);
        lbl.setGravity(Gravity.CENTER);
        lbl.setPadding(0, dp(3), 0, 0);
        cell.addView(lbl);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(dp(3), 0, dp(3), 0);
        row.addView(cell, params);
    }

    void addTimeframeChip(LinearLayout row, String text, boolean active) {
        TextView chip = new TextView(this);
        chip.setText(text);
        chip.setTextSize(12);
        chip.setTypeface(null, Typeface.BOLD);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(12), dp(6), dp(12), dp(6));

        GradientDrawable bg = new GradientDrawable();
        if (active) {
            chip.setTextColor(Color.parseColor("#14110C"));
            bg.setColor(Color.parseColor(COLOR_GOLD_ROYAL));
        } else {
            chip.setTextColor(Color.parseColor(COLOR_TEXT_MUTED));
            bg.setColor(Color.parseColor("#1A1610"));
            bg.setStroke(dp(1), Color.parseColor("#3D3219"));
        }
        bg.setCornerRadius(dp(8));
        chip.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(dp(4), 0, dp(4), 0);
        row.addView(chip, params);
    }

    LinearLayout makeNavTile(String iconTitle, String subtitle, Runnable action) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.HORIZONTAL);
        tile.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        tile.setGravity(Gravity.CENTER_VERTICAL);
        tile.setPadding(dp(14), dp(12), dp(14), dp(12));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#15120D"));
        bg.setCornerRadius(dp(12));
        bg.setStroke(dp(1), Color.parseColor("#44371C"));
        tile.setBackground(bg);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        TextView title = makeText(iconTitle, 15, COLOR_GOLD_BRIGHT, true);
        textCol.addView(title);

        TextView sub = makeText(subtitle, 12, COLOR_TEXT_MUTED, false);
        sub.setPadding(0, dp(2), 0, 0);
        textCol.addView(sub);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        );
        tile.addView(textCol, textParams);

        TextView arrow = makeText("‹", 18, COLOR_GOLD_ROYAL, true);
        arrow.setPadding(dp(4), 0, dp(4), 0);
        tile.addView(arrow);

        LinearLayout.LayoutParams tileParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        tileParams.setMargins(0, 0, 0, dp(8));
        tile.setLayoutParams(tileParams);

        tile.setOnClickListener(v -> action.run());
        return tile;
    }

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);

        getWindow().setStatusBarColor(Color.parseColor(COLOR_BG_DEEP));
        getWindow().setNavigationBarColor(Color.parseColor(COLOR_BG_DEEP));

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
        LinearLayout root = setupRootScroll();
        addRoyalHeader(root, "بوابة الدخول الآمنة");

        LinearLayout card = createRoyalCard("تسجيل الدخول الملكي", "حساب معتمد");

        TextView heading = makeText("مرحبًا بك في منصة Fer3oon", 18, COLOR_GOLD_BRIGHT, true);
        heading.setGravity(Gravity.CENTER);
        heading.setPadding(0, dp(8), 0, dp(6));
        card.addView(heading);

        TextView note = makeText(
            "سجّل الدخول عبر حساب Google الموثق للوصول إلى لوحة القيادة التجريبية وإدارة مفاتيح API بأمان عالي.",
            13,
            COLOR_TEXT_MUTED,
            false
        );
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, 0, 0, dp(18));
        card.addView(note);

        googleLoginButton = createRoyalButton(
            "🌐  تسجيل الدخول باستخدام Google",
            COLOR_GOLD_ROYAL,
            "#14110C",
            null,
            this::startGoogleSignIn
        );
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(52)
        );
        card.addView(googleLoginButton, btnParams);

        TextView securityBadge = makeText(
            "🔒 الدخول محمي عبر Google Credential Manager\nالمحرك معطل بالكامل في هذه النسخة لأغراض التطوير والأمان",
            12,
            COLOR_TEXT_SECONDARY,
            false
        );
        securityBadge.setGravity(Gravity.CENTER);
        securityBadge.setPadding(0, dp(14), 0, dp(4));
        card.addView(securityBadge);

        root.addView(card);
    }

    void setGoogleLoginEnabled(boolean enabled) {
        if (googleLoginButton != null) {
            googleLoginButton.setEnabled(enabled);
        }
    }

    void startGoogleSignIn() {
        if (isSigningIn) return;

        isSigningIn = true;
        setGoogleLoginEnabled(false);

        Toast.makeText(this, "جاري فتح تسجيل الدخول الموثق...", Toast.LENGTH_SHORT).show();

        try {
            GetGoogleIdOption googleOption =
                new GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(getString(R.string.default_web_client_id))
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
                        runOnUiThread(() -> {
                            try {
                                Credential credential =
                                    result == null ? null : result.getCredential();

                                if (credential == null) {
                                    isSigningIn = false;
                                    setGoogleLoginEnabled(true);
                                    Toast.makeText(
                                        MainActivity.this,
                                        "لم تكتمل استجابة Google، حاول مرة أخرى",
                                        Toast.LENGTH_LONG
                                    ).show();
                                    return;
                                }

                                handleGoogleCredential(credential);
                            } catch (Exception e) {
                                isSigningIn = false;
                                setGoogleLoginEnabled(true);
                                Toast.makeText(
                                    MainActivity.this,
                                    "حدث خطأ أثناء استقبال حساب Google",
                                    Toast.LENGTH_LONG
                                ).show();
                            }
                        });
                    }
                    @Override
                    public void onError(GetCredentialException e) {
                        runOnUiThread(() -> {
                            isSigningIn = false;
                            setGoogleLoginEnabled(true);

                            if (!(e instanceof GetCredentialCancellationException)) {
                                Toast.makeText(
                                    MainActivity.this,
                                    "تعذر تسجيل الدخول: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                                ).show();
                            }
                        });
                    }
                }
            );
        } catch (Exception e) {
            isSigningIn = false;
            setGoogleLoginEnabled(true);
            Toast.makeText(
                this,
                "تعذر بدء تسجيل الدخول",
                Toast.LENGTH_LONG
            ).show();
        }
    }

    void handleGoogleCredential(Credential credential) {
        if (!(credential instanceof CustomCredential)
            || !GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                .equals(credential.getType())) {

            isSigningIn = false;
            setGoogleLoginEnabled(true);
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
                    isSigningIn = false;
                    setGoogleLoginEnabled(true);

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
            isSigningIn = false;
            setGoogleLoginEnabled(true);
            Toast.makeText(
                this,
                "تعذر قراءة بيانات حساب Google",
                Toast.LENGTH_LONG
            ).show();
        }
    }

    void showDashboard() {
        LinearLayout root = setupRootScroll();
        addRoyalHeader(root, "لوحة التحكم وإدارة المحفظة الملكية");

        // 1. بطاقة حساب التداول التجريبي (DEMO Account Status Card)
        LinearLayout accountCard = createRoyalCard("👑  حساب التداول التجريبي", "DEMO Account");
        String accountState = "حساب DEMO غير مربوط — أضف المفاتيح من الإعدادات";
        String emailSnippet = "";
        try {
            String[] saved = SecureCredentials.load(this);
            if (saved != null && saved.length >= 3 &&
                saved[0] != null && !saved[0].trim().isEmpty() &&
                saved[1] != null && !saved[1].trim().isEmpty() &&
                saved[2] != null && !saved[2].trim().isEmpty()) {
                accountState = "بيانات حساب DEMO مشفّرة عتادياً داخل AndroidKeyStore";
                emailSnippet = "البريد المعتمد: " + saved[0];
            }
        } catch (Exception ignored) {}

        TextView accountStateText = makeText(accountState, 13, COLOR_GOLD_ROYAL, false);
        accountStateText.setPadding(0, 0, 0, dp(4));
        accountCard.addView(accountStateText);

        if (!emailSnippet.isEmpty()) {
            TextView emailText = makeText(emailSnippet, 12, COLOR_TEXT_MUTED, false);
            emailText.setPadding(0, 0, 0, dp(8));
            accountCard.addView(emailText);
        }
        root.addView(accountCard);

        // 2. أقسام الرصيد والأرباح والخسائر (Balance, Profit, and Loss)
        LinearLayout balanceCard = createRoyalCard("💰  المؤشرات المالية (DEMO)", "غير مباشر");
        LinearLayout metricsRow = new LinearLayout(this);
        metricsRow.setOrientation(LinearLayout.HORIZONTAL);
        metricsRow.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        metricsRow.setPadding(0, dp(4), 0, dp(4));

        addMetricCell(metricsRow, "الرصيد الكلي", "—", COLOR_GOLD_BRIGHT);
        addMetricCell(metricsRow, "صافي الأرباح", "—", COLOR_SUCCESS);
        addMetricCell(metricsRow, "إجمالي الخسائر", "—", COLOR_DANGER);
        balanceCard.addView(metricsRow);

        TextView metricsNote = makeText(
            "المؤشرات المالية الحية غير متوفرة في نسخة التطوير (بيانات تجريبية DEMO)",
            11,
            COLOR_TEXT_SECONDARY,
            false
        );
        metricsNote.setGravity(Gravity.CENTER);
        metricsNote.setPadding(0, dp(8), 0, 0);
        balanceCard.addView(metricsNote);
        root.addView(balanceCard);

        // 3. الهوية البصرية الملكية (بدون أي مخطط سعري)
        LinearLayout identityCard = createRoyalCard(
            "👑  الهوية الملكية لـ Fer3oon",
            "Royal Dashboard"
        );

        android.widget.ImageView identityImage =
            new android.widget.ImageView(this);
        identityImage.setImageResource(R.drawable.fer3oon_dashboard);
        identityImage.setScaleType(
            android.widget.ImageView.ScaleType.FIT_CENTER
        );
        identityImage.setAdjustViewBounds(true);
        identityImage.setContentDescription(
            "التصميم الداخلي الملكي بالأسود والذهبي"
        );
        identityImage.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        identityCard.addView(identityImage);
        root.addView(identityCard);

        // 4. حالة المحرك ومؤشر أمان DEMO (Engine Status & DEMO Safety Indicator)
        LinearLayout engineCard = createRoyalCard("⚙  حالة المحرك وصمام الأمان", "وضع آمن");

        TextView engineStatusText = makeText("● المحرك متوقف (آمن)", 15, COLOR_GOLD_ROYAL, true);
        engineStatusText.setPadding(0, dp(2), 0, dp(6));
        engineCard.addView(engineStatusText);

        TextView safetyDetails = makeText(
            "● وضع التشغيل: تجريبي حصرياً (DEMO-only)\n● صمام الأمان: التداول الحقيقي وإرسال الأوامر معطل برمجياً بالكامل\n● خدمة الخلفية TradingService: معطلة تماماً وغير مستدعاة",
            12,
            COLOR_TEXT_MUTED,
            false
        );
        safetyDetails.setPadding(0, 0, 0, dp(14));
        engineCard.addView(safetyDetails);

        // 5. عناصر التحكم بالتشغيل والإيقاف (Start & Stop Controls)
        LinearLayout controlsRow = new LinearLayout(this);
        controlsRow.setOrientation(LinearLayout.HORIZONTAL);
        controlsRow.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        Button startBtn = createRoyalButton(
            "▶  تشغيل المحرك (تجريبي)",
            "#221B0B",
            COLOR_GOLD_ROYAL,
            COLOR_GOLD_BORDER,
            () -> {
                engineStatusText.setText("● المحرك متوقف — التشغيل معطل في وضع الأمان والتطوير");
                Toast.makeText(this, "صمام الأمان: محرك التداول معطل تماماً لأغراض التطوير والأمان", Toast.LENGTH_SHORT).show();
            }
        );
        LinearLayout.LayoutParams startParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
        startParams.leftMargin = dp(6);
        controlsRow.addView(startBtn, startParams);

        Button stopBtn = createRoyalButton(
            "■  إيقاف المحرك",
            "#1E1010",
            COLOR_DANGER,
            "#5C2626",
            () -> {
                engineStatusText.setText("● المحرك متوقف (آمن)");
                Toast.makeText(this, "تم تأكيد إيقاف المحرك في وضع الأمان", Toast.LENGTH_SHORT).show();
            }
        );
        LinearLayout.LayoutParams stopParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
        stopParams.rightMargin = dp(6);
        controlsRow.addView(stopBtn, stopParams);

        engineCard.addView(controlsRow);
        root.addView(engineCard);

        // 6. قسم التنقل والخدمات الملكية (Navigation)
        LinearLayout navCard = createRoyalCard("⚡  الخدمات والإعدادات الملكية", "5 أقسام");

        navCard.addView(makeNavTile("👤  الملف الشخصي", "معلومات الحساب والبريد المعتمد", this::showProfileDialog));
        navCard.addView(makeNavTile("🔑  إعدادات وسيط التداول (DEMO)", "إدارة مفاتيح Capital.com المشفرة عتادياً", this::showAccountSettings));
        navCard.addView(makeNavTile("📊  الإحصائيات والتحليلات", "ملخص الأداء والمؤشرات العامة", this::showStatisticsDialog));
        navCard.addView(makeNavTile("📜  سجل النشاط والعمليات", "سجل الأحداث وتنبيهات الأمان", this::showActivityHistoryDialog));
        navCard.addView(makeNavTile("🛡  إعدادات التطبيق والأمان", "خيارات الحماية ومعلومات الإصدار", this::showAppSettingsDialog));

        root.addView(navCard);

        // التذييل وبيانات الجلسة
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            TextView accountEmail = makeText(
                "حساب Google النشط: " + user.getEmail(),
                12,
                COLOR_TEXT_MUTED,
                false
            );
            accountEmail.setGravity(Gravity.CENTER);
            accountEmail.setPadding(0, dp(4), 0, dp(2));
            root.addView(accountEmail);
        }

        TextView disclaimer = makeText(
            "نسخة تطوير خاصة (DEMO Sandbox) — محرك التداول معطل كلياً لحماية الأصول",
            11,
            COLOR_TEXT_SECONDARY,
            false
        );
        disclaimer.setGravity(Gravity.CENTER);
        disclaimer.setPadding(0, 0, 0, dp(12));
        root.addView(disclaimer);
    }

    void showProfileDialog() {
        FirebaseUser user = auth.getCurrentUser();
        String email = (user != null && user.getEmail() != null) ? user.getEmail() : "غير مسجل";
        String uid = (user != null && user.getUid() != null) ? user.getUid() : "—";

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        content.setPadding(dp(20), dp(16), dp(20), dp(16));

        content.addView(makeText("البريد الإلكتروني المعتمد:", 13, COLOR_GOLD_ROYAL, true));
        content.addView(makeText(email, 14, "#FFFFFF", false));

        TextView uidLabel = makeText("معرف المستخدم (UID):", 13, COLOR_GOLD_ROYAL, true);
        uidLabel.setPadding(0, dp(10), 0, 0);
        content.addView(uidLabel);
        content.addView(makeText(uid, 12, COLOR_TEXT_MUTED, false));

        TextView authType = makeText("نوع المصادقة: Google Identity / Firebase Auth", 12, COLOR_TEXT_SECONDARY, false);
        authType.setPadding(0, dp(12), 0, 0);
        content.addView(authType);

        new AlertDialog.Builder(this)
            .setTitle("الملف الشخصي الملكي")
            .setView(content)
            .setPositiveButton("إغلاق", null)
            .setNeutralButton("تسجيل الخروج من Google", (d, w) -> signOutGoogle())
            .show();
    }

    void showStatisticsDialog() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        content.setPadding(dp(20), dp(16), dp(20), dp(16));

        content.addView(makeText("إحصائيات الأداء (نسخة التطوير)", 15, COLOR_GOLD_ROYAL, true));
        TextView body = makeText(
            "● إجمالي العمليات: غير متوفر في نسخة التطوير\n● نسبة الصفقات الناجحة: —\n● أقصى تراجع (Drawdown): —\n● التداول الحقيقي: معطل برمجياً ومحصور في وضع المحاكاة DEMO",
            13,
            "#FFFFFF",
            false
        );
        body.setPadding(0, dp(8), 0, 0);
        content.addView(body);

        new AlertDialog.Builder(this)
            .setTitle("الإحصائيات والتحليلات")
            .setView(content)
            .setPositiveButton("إغلاق", null)
            .show();
    }

    void showActivityHistoryDialog() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        content.setPadding(dp(20), dp(16), dp(20), dp(16));

        content.addView(makeText("سجل النشاط والأمان الأخير", 15, COLOR_GOLD_ROYAL, true));
        TextView logs = makeText(
            "● جلسة المستخدم: تم تسجيل الدخول الآمن بنجاح.\n● التشفير العتادي: مفاتيح التداول محمية بنظام AndroidKeyStore.\n● حالة المحرك: متوقف — التداول معطل برمجياً في وضع الأمان.\n● سجل عمليات التداول غير متاح في نسخة التطوير.",
            13,
            COLOR_TEXT_MUTED,
            false
        );
        logs.setPadding(0, dp(8), 0, 0);
        content.addView(logs);

        new AlertDialog.Builder(this)
            .setTitle("سجل النشاط")
            .setView(content)
            .setPositiveButton("إغلاق", null)
            .show();
    }

    void showAppSettingsDialog() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        content.setPadding(dp(20), dp(16), dp(20), dp(16));

        content.addView(makeText("إعدادات الأمان والتطبيق", 15, COLOR_GOLD_ROYAL, true));
        TextView info = makeText(
            "● التطبيق: Fer3oon Royal Trading Dashboard\n● الإصدار: 1.0 (DEMO Development Build)\n● معيار التشفير: AES-256-GCM عتادي معتمد\n● شبكة الاتصال: حركة المرور الصريحة (Cleartext) محظورة كلياً\n● الحماية: لا يتم تخزين مفاتيح الوسيط بنص صريح على الإطلاق",
            13,
            "#FFFFFF",
            false
        );
        info.setPadding(0, dp(8), 0, 0);
        content.addView(info);

        new AlertDialog.Builder(this)
            .setTitle("إعدادات التطبيق")
            .setView(content)
            .setPositiveButton("إغلاق", null)
            .show();
    }

    void showAccountSettings() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        form.setPadding(dp(20), dp(12), dp(20), dp(12));

        TextView intro = makeText(
            "أدخل بيانات حساب Capital.com التجريبي (DEMO). سيتم تشفيرها عتادياً داخل AndroidKeyStore باستخدام AES-256-GCM ولن تخرج من جهازك.",
            12,
            COLOR_TEXT_MUTED,
            false
        );
        intro.setPadding(0, 0, 0, dp(12));
        form.addView(intro);

        EditText emailField = new EditText(this);
        emailField.setHint("البريد الإلكتروني للوسيط (DEMO)");
        emailField.setTextColor(Color.WHITE);
        emailField.setHintTextColor(Color.parseColor(COLOR_TEXT_SECONDARY));
        emailField.setSingleLine(true);
        form.addView(emailField);

        EditText keyField = new EditText(this);
        keyField.setHint("Capital.com DEMO API Key");
        keyField.setTextColor(Color.WHITE);
        keyField.setHintTextColor(Color.parseColor(COLOR_TEXT_SECONDARY));
        keyField.setSingleLine(true);
        keyField.setInputType(129); // textPassword
        form.addView(keyField);

        EditText passwordField = new EditText(this);
        passwordField.setHint("كلمة مرور API (DEMO Password)");
        passwordField.setTextColor(Color.WHITE);
        passwordField.setHintTextColor(Color.parseColor(COLOR_TEXT_SECONDARY));
        passwordField.setSingleLine(true);
        passwordField.setInputType(129); // textPassword
        form.addView(passwordField);

        try {
            String[] saved = SecureCredentials.load(this);
            if (saved != null && saved.length >= 3) {
                if (saved[0] != null) emailField.setText(saved[0]);
                if (saved[1] != null) keyField.setText(saved[1]);
                if (saved[2] != null) passwordField.setText(saved[2]);
            }
        } catch (Exception e) {
            Toast.makeText(
                this,
                "تعذر قراءة البيانات المحفوظة",
                Toast.LENGTH_LONG
            ).show();
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("إعدادات حساب الوسيط (DEMO)")
            .setView(form)
            .setPositiveButton("حفظ مشفّر عتادياً", null)
            .setNeutralButton("تسجيل الخروج من Google", (d, w) -> signOutGoogle())
            .setNegativeButton("إلغاء", null)
            .create();

        dialog.setOnShowListener(v ->
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                String email = emailField.getText().toString().trim();
                String key = keyField.getText().toString().trim();
                String password = passwordField.getText().toString();

                if (email.isEmpty() || key.isEmpty() || password.isEmpty()) {
                    Toast.makeText(
                        this,
                        "يرجى إكمال جميع حقول بيانات الحساب",
                        Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                try {
                    SecureCredentials.save(this, email, key, password);
                    Toast.makeText(
                        this,
                        "تم حفظ بيانات DEMO بشكل مشفّر داخل KeyStore",
                        Toast.LENGTH_LONG
                    ).show();
                    dialog.dismiss();
                    showDashboard();
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
