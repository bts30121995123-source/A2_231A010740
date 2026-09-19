package vn.edu.vhu.ltdd.a2stopwatch;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "A2_231A010740";

    // Khóa lưu trạng thái vào Bundle
    private static final String KEY_RUNNING = "running";
    private static final String KEY_ACCUMULATED = "accumulated";
    private static final String KEY_START = "start";
    private static final String KEY_RECREATE = "recreate";

    // NC1 + NC2
    private static final String KEY_LAPS = "laps";
    private static final String KEY_STOP_BACKGROUND = "stop_background";

    private TextView tvTime, tvStatus, tvRecreate;
    private Button btnStartPause, btnReset;

    // NC1
    private Button btnLap;
    private TextView tvLaps;
    private ArrayList<String> laps = new ArrayList<>();

    // NC2
    private CheckBox cbStopBackground;

    // Trạng thái của đồng hồ
    private boolean running = false;
    private long accumulated = 0L;
    private long startTime = 0L;
    private int recreateCount = 0;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            updateTimeText();
            handler.postDelayed(this, 100);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        tvTime = findViewById(R.id.tvTime);
        tvStatus = findViewById(R.id.tvStatus);
        tvRecreate = findViewById(R.id.tvRecreate);
        btnStartPause = findViewById(R.id.btnStartPause);
        btnReset = findViewById(R.id.btnReset);

        // NC1
        btnLap = findViewById(R.id.btnLap);
        tvLaps = findViewById(R.id.tvLaps);

        // NC2
        cbStopBackground = findViewById(R.id.cbStopBackground);

        if (savedInstanceState != null) {
            running = savedInstanceState.getBoolean(KEY_RUNNING);
            accumulated = savedInstanceState.getLong(KEY_ACCUMULATED);
            startTime = savedInstanceState.getLong(KEY_START);
            recreateCount = savedInstanceState.getInt(KEY_RECREATE) + 1;

            // NC1: khôi phục danh sách vòng
            ArrayList<String> savedLaps = savedInstanceState.getStringArrayList(KEY_LAPS);
            if (savedLaps != null) {
                laps = savedLaps;
            }

            // NC2: khôi phục trạng thái checkbox
            cbStopBackground.setChecked(
                    savedInstanceState.getBoolean(KEY_STOP_BACKGROUND));

            Log.d(TAG, "onCreate: KHÔI PHỤC trạng thái, running=" + running
                    + ", accumulated=" + accumulated + "ms");
        } else {
            Log.d(TAG, "onCreate: khởi tạo mới (savedInstanceState = null)");
        }

        btnStartPause.setOnClickListener(v -> {
            if (running) {
                pauseStopwatch();
            } else {
                startStopwatch();
            }
        });

        btnReset.setOnClickListener(v -> resetStopwatch());

        // NC1
        btnLap.setOnClickListener(v -> addLap());

        updateUi();
        updateLapList();
    }

    // ---------------- Logic đồng hồ ----------------

    /** Tổng thời gian đã trôi qua (ms). */
    private long elapsed() {
        return running
                ? accumulated + (SystemClock.elapsedRealtime() - startTime)
                : accumulated;
    }

    private void startStopwatch() {
        running = true;
        startTime = SystemClock.elapsedRealtime();
        startTicking();
        updateUi();
        Log.i(TAG, "BẮT ĐẦU đếm giờ");
    }

    private void pauseStopwatch() {
        accumulated += SystemClock.elapsedRealtime() - startTime;
        running = false;
        stopTicking();
        updateUi();
        Log.i(TAG, "TẠM DỪNG tại " + accumulated + "ms");
    }

    private void resetStopwatch() {
        running = false;
        accumulated = 0L;
        startTime = 0L;
        stopTicking();
        updateUi();
        Log.i(TAG, "ĐẶT LẠI về 00:00.0");
    }

    private void startTicking() {
        handler.removeCallbacks(ticker);
        handler.post(ticker);
    }

    private void stopTicking() {
        handler.removeCallbacks(ticker);
    }

    // ---------------- Cập nhật giao diện ----------------

    private void updateTimeText() {
        long ms = elapsed();
        long phut = ms / 60000;
        long giay = (ms % 60000) / 1000;
        long phanMuoi = (ms % 1000) / 100;

        tvTime.setText(String.format(
                Locale.getDefault(),
                "%02d:%02d.%d",
                phut, giay, phanMuoi));
    }

    private void updateUi() {
        updateTimeText();
        btnStartPause.setText(running ? R.string.pause : R.string.start);
        tvStatus.setText(running ? R.string.status_running : R.string.status_paused);
        tvRecreate.setText(getString(R.string.recreate_count, recreateCount));
    }

    // ---------------- NC1: Vòng (Lap) ----------------

    private void addLap() {
        long ms = elapsed();
        long phut = ms / 60000;
        long giay = (ms % 60000) / 1000;
        long phanMuoi = (ms % 1000) / 100;

        String lapTime = String.format(
                Locale.getDefault(),
                "Vòng %d: %02d:%02d.%d",
                laps.size() + 1, phut, giay, phanMuoi);

        laps.add(lapTime);
        updateLapList();
    }

    private void updateLapList() {
        StringBuilder builder = new StringBuilder();

        for (String lap : laps) {
            builder.append(lap).append("\n");
        }

        tvLaps.setText(builder.toString());
    }

    // ---------------- Vòng đời ----------------

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG,
                "onResume – bật lại việc cập nhật giao diện nếu đồng hồ đang chạy");

        if (running) {
            startTicking();
        }

        updateUi();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Dừng cập nhật giao diện để tiết kiệm pin;
        // đồng hồ vẫn tính đúng nhờ SystemClock.elapsedRealtime().
        stopTicking();

        Log.d(TAG, "onPause – tạm dừng cập nhật giao diện");
    }

    @Override
    protected void onStop() {
        super.onStop();

        // NC2: nếu được tick thì dừng đồng hồ khi ra nền
        if (cbStopBackground.isChecked() && running) {
            pauseStopwatch();
        }

        Log.d(TAG, "onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onDestroy() {
        stopTicking();
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    // ---------------- Lưu & khôi phục trạng thái ----------------

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_RUNNING, running);
        outState.putLong(KEY_ACCUMULATED, accumulated);
        outState.putLong(KEY_START, startTime);
        outState.putInt(KEY_RECREATE, recreateCount);

        // NC1
        outState.putStringArrayList(KEY_LAPS, laps);

        // NC2
        outState.putBoolean(
                KEY_STOP_BACKGROUND,
                cbStopBackground.isChecked());

        Log.d(TAG,
                "onSaveInstanceState – đã lưu " + elapsed() + "ms vào Bundle");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState – được gọi sau onStart()");
    }
}