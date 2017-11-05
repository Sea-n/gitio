package taipei.sean.gitio;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingActivity extends AppCompatActivity {
    final private int _dbVer = 1;
    private SeanDBHelper db;
    private String shortUrl;
    private Context _context = this;
    private boolean isProccing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        final Switch readClipboard = findViewById(R.id.read_clipboard);
        final Switch autoCopy = findViewById(R.id.auto_copy);

        final SharedPreferences preferences = getSharedPreferences("data", MODE_PRIVATE);

        readClipboard.setChecked(preferences.getBoolean("read_clipboard", true));
        autoCopy.setChecked(preferences.getBoolean("auto_copy", true));

        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.edit()
                        .putBoolean("read_clipboard", readClipboard.isChecked())
                        .putBoolean("auto_copy", autoCopy.isChecked())
                        .apply();
            }
        };

        readClipboard.setOnCheckedChangeListener(listener);
        autoCopy.setOnCheckedChangeListener(listener);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH) {
            readClipboard.setCursorVisible(false);
        }

    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(this, ClipboardService.class);
        startService(intent);
        super.onDestroy();
    }
}