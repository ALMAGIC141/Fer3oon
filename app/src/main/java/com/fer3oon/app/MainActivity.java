package com.fer3oon.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout root, markets, positions, logs;
    TextView status, chosen, reason, count;
    EditText email, apiKey, password;
    Spinner lot;
    Handler handler = new Handler();

    final String GOLD = "#E8C46A";
    final String BG = "#080808";
    final String CARD = "#191919";

    int dp(float n) {
        return (int)(n * getResources().getDisplayMetrics().density);
    }

    TextView text(String value, int size, String color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(Color.parseColor(color));
        t.setPadding(dp(8), dp(8), dp(8), dp(8));
        return t;
    }

    LinearLayout section(String title) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(12), dp(14), dp(12));
        box.setBackgroundColor(Color.parseColor(CARD));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            -1, -2
        );
        lp.setMargins(0, dp(8), 0, dp(8));
        root.addView(box, lp);
        TextView heading = text(title, 19, GOLD);
        heading.setTypeface(null, Typeface.BOLD);
        box.addView(heading);
        return box;
    }

    EditText field(LinearLayout box, String hint, boolean secret) {
        EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setHint(hint);
        e.setHintTextColor(Color.GRAY);
        e.setTextColor(Color.WHITE);
        e.setTextSize(15);
        if (secret) e.setInputType(129);
        box.addView(e, new LinearLayout.LayoutParams(-1, dp(52)));
        return e;
    }

    Button button(LinearLayout box, String label, boolean stop, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.BLACK);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
            Color.parseColor(stop ? "#B83E48" : GOLD)
        ));
        b.setOnClickListener(v -> action.run());
        box.addView(b, new LinearLayout.LayoutParams(-1, dp(52)));
        return b;
    }

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(10), dp(14), dp(18));
        root.setBackgroundColor(Color.parseColor(BG));
        scroll.addView(root);
        setContentView(scroll);

        TextView title = text("👑 Fer3oon", 30, GOLD);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, Typeface.BOLD);
        root.addView(title);

        LinearLayout dashboard = section("لوحة Fer3oon الملكية");
        status = text("الحالة: متوقف", 17, GOLD);
        dashboard.addView(status);

        markets = new LinearLayout(this);
        markets.setOrientation(LinearLayout.VERTICAL);
        dashboard.addView(markets);
        for (String name : new String[]{"EURUSD", "USDJPY", "GOLD"}) {
            markets.addView(text(name + "  |  السعر: --  |  الإشارة: --", 16, "#FFFFFF"));
        }

        chosen = text("السوق المختار: --", 19, GOLD);
        dashboard.addView(chosen);
        reason = text("في انتظار تحليل الأسواق", 14, "#C9B987");
        dashboard.addView(reason);

        LinearLayout account = section("الاتصال بحساب Capital.com DEMO");
        email = field(account, "البريد الإلكتروني", false);
        apiKey = field(account, "API Key", true);
        password = field(account, "كلمة مرور API", true);

        lot = new Spinner(this);
        String[] lots = {"0.01", "0.02", "0.05", "0.10", "0.20"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, lots
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lot.setAdapter(adapter);
        account.addView(lot);

        button(account, "▶ تشغيل", false, this::startTrading);
        button(account, "■ إيقاف", true, this::stopTrading);

        LinearLayout open = section("الصفقات المفتوحة");
        count = text("0 / 3", 20, GOLD);
        open.addView(count);
        positions = new LinearLayout(this);
        positions.setOrientation(LinearLayout.VERTICAL);
        open.addView(positions);
        positions.addView(text("لا توجد بيانات بعد", 15, "#FFFFFF"));

        LinearLayout history = section("الصفقات المنتهية");
        history.addView(text("سيتم عرض سجل الصفقات هنا", 14, "#FFFFFF"));

        LinearLayout activity = section("سجل العمليات");
        logs = new LinearLayout(this);
        logs.setOrientation(LinearLayout.VERTICAL);
        activity.addView(logs);
        logs.addView(text("التطبيق جاهز", 14, GOLD));
    }

    void startTrading() {
        String e = email.getText().toString().trim();
        String k = apiKey.getText().toString().trim();
        String p = password.getText().toString();

        if (e.isEmpty() || k.isEmpty() || p.isEmpty()) {
            status.setText("أكمل بيانات حساب DEMO");
            return;
        }

        Intent i = new Intent(this, TradingService.class);
        i.setAction(TradingService.ACTION_START);
        i.putExtra("email", e);
        i.putExtra("key", k);
        i.putExtra("password", p);
        i.putExtra("lot", lot.getSelectedItem().toString());
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(i);
        else startService(i);

        apiKey.setText("");
        password.setText("");
        status.setText("المحرك غير مفعل - نسخة اختبار");
    }

    void stopTrading() {
        Intent i = new Intent(this, TradingService.class);
        i.setAction(TradingService.ACTION_STOP);
        startService(i);
        status.setText("تم طلب إيقاف المحرك");
    }
}
