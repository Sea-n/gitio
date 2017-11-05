package taipei.sean.gitio;

import android.app.NotificationManager;
import android.app.RemoteInput;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.onesignal.OneSignal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {
    final private int _dbVer = 1;
    private SeanDBHelper db;
    private String shortUrl;
    private Context _context = this;
    private boolean isProccing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        db = new SeanDBHelper(this, "data.db", null, _dbVer);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return navItemSelected(item);
            }
        });


        String appName = getString(R.string.app_name);
        String footerStr = "";
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String verName = pInfo.versionName;
            int verCode = pInfo.versionCode;
            footerStr = getString(R.string.nav_footer, appName, verName, String.valueOf(verCode));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("main", "Name Not Found", e);
            if (footerStr.equals("")) {
                footerStr = e.getLocalizedMessage();
            }
        }

        final TextView footer = findViewById(R.id.main_footer);
        footer.setText(footerStr);


        final TextInputEditText oriUrlView = findViewById(R.id.original_url);
        final TextInputEditText codeView = findViewById(R.id.code);
        final Button submitButton = findViewById(R.id.submit);

        oriUrlView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    String text = oriUrlView.getText().toString();
                    if (!text.equals("")) {
                        Pattern p = Pattern.compile(getString(R.string.github_url_regex));
                        Matcher m = p.matcher(text);
                        if (!m.matches()) {
                            oriUrlView.setError(getString(R.string.url_wrong));
                        }
                    }
                }
            }
        });

        oriUrlView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String value = editable.toString();
                db.updateParam("url", value);
            }
        });

        codeView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String value = editable.toString();
                db.updateParam("code", value);
            }
        });


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shortenUrl();
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            if (null != extras) {
                String url = extras.getString("url");
                if (null != url) {
                    db.updateParam("url", url);
                }
            }
            Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
            if (null != remoteInput) {
                CharSequence replyCS = remoteInput.getCharSequence("code");
                if (null != replyCS) {
                    String reply = replyCS.toString();
                    db.updateParam("code", reply);
                }
            }

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (null != notificationManager) {
                notificationManager.cancel(1);
            }
        }

        String url = db.getParam("url");
        if (null != url)
            oriUrlView.setText(url);

        String code = db.getParam("code");
        if (null != code)
            codeView.setText(code);

        Intent serviceIntent = new Intent(this, ClipboardService.class);
        startService(serviceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_copy:
                copy();
                break;
            default:
                Log.w("option", "Press unknown " + id);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean navItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.history:
                Intent historyIntent = new Intent(this, HistoryActivity.class);
                startActivity(historyIntent);
                break;
            case R.id.setting:
                Intent settingIntent = new Intent(this, SettingActivity.class);
                startActivity(settingIntent);
            default:
                break;
        }
        return true;
    }

    private void shortenUrl() {
        if (isProccing)
            return;
        isProccing = true;
        final TextInputEditText oriUrlView = findViewById(R.id.original_url);
        final TextInputEditText codeView = findViewById(R.id.code);
        final TextView resultView = findViewById(R.id.result);
        final String oriUrl = oriUrlView.getText().toString();
        final String code = codeView.getText().toString();

        resultView.setText(R.string.processing);
        shortUrl = null;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String respStr = "";
                String respLocation = "";
                int respCode = -1;
                try {
                    MultipartBody.Builder builder = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("url", oriUrl);

                    if (code.length() > 0)
                        builder.addFormDataPart("code", code);

                    RequestBody requestBody = builder.build();

                    Request request = new Request.Builder()
                            .url("https://git.io/")
                            .post(requestBody)
                            .build();
                    OkHttpClient client = new OkHttpClient();
                    Response resp = client.newCall(request).execute();
                    ResponseBody respBody = resp.body();
                    if (null != respBody)
                        respStr = respBody.string();
                    respLocation = resp.header("Location");
                    respCode = resp.code();
                } catch (final MalformedURLException e) {
                    Log.e("api", "Malformed URL", e);
                    final String finalResultText = e.getLocalizedMessage();
                    Handler handler = new Handler(_context.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            resultView.setText(finalResultText);
                        }
                    });
                } catch (final IOException e) {
                    Log.e("api", "IO", e);
                    final String finalResultText = e.getLocalizedMessage();
                    Handler handler = new Handler(_context.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            resultView.setText(finalResultText);
                        }
                    });
                } catch (final NullPointerException e) {
                    Log.e("api", "Null Pointer", e);
                    final String finalResultText = e.getLocalizedMessage();
                    Handler handler = new Handler(_context.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            resultView.setText(finalResultText);
                        }
                    });
                }

                String resultText = "";
                if (respCode > 0) {
                    resultText += respCode + ": ";
                }
                if (!respStr.equals(""))
                    resultText += respStr + "\n\n";
                if (null != respLocation && !respLocation.equals("")) {
                    resultText += "Location: " + respLocation + "\n\n";
                    shortUrl = respLocation;
                }

                if (!resultText.equals("")) {
                    final String finalResultText = resultText;
                    Handler handler = new Handler(_context.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            resultView.setText(finalResultText);
                        }
                    });
                }


                if (null == shortUrl) {
                    Log.e("main", "no short url");
                    isProccing = false;
                    return;
                }

                boolean autoCopy = getSharedPreferences("data", MODE_PRIVATE).getBoolean("auto_copy", true);
                if (autoCopy) {

                    Handler handler = new Handler(_context.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            copy();
                        }
                    });

                }

                Pattern p = Pattern.compile("https://git.io/(.+)");
                Matcher m = p.matcher(shortUrl);
                if (m.matches()) {
                    String code = m.group(1);
                    db.insertHistory(oriUrl, code);
                } else
                    db.insertHistory(oriUrl, shortUrl);

                isProccing = false;
            }
        });
        thread.start();
    }

    private void copy() {
        if (null == shortUrl || shortUrl.equals(""))
            return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (null == clipboard)
            return;
        ClipData clip = ClipData.newPlainText(getString(R.string.app_name), shortUrl);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, R.string.copied, Toast.LENGTH_LONG).show();
    }
}