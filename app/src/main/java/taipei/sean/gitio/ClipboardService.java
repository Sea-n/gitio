package taipei.sean.gitio;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ClipboardService extends Service {
    private ClipboardManager.OnPrimaryClipChangedListener listener = new ClipboardManager.OnPrimaryClipChangedListener() {
        public void onPrimaryClipChanged() {
            performClipboardCheck();
        }
    };

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH)
            return;

        if (!getSharedPreferences("data", MODE_PRIVATE).getBoolean("read_clipboard", true))
            return;

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.addPrimaryClipChangedListener(listener);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    private void performClipboardCheck() {
        if (!getSharedPreferences("data", MODE_PRIVATE).getBoolean("read_clipboard", true))
            return;

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (null != clipboard && clipboard.hasPrimaryClip()) {
            ClipData cd = clipboard.getPrimaryClip();
            if (cd.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                String text = cd.getItemAt(0).getText().toString();

                Pattern p = Pattern.compile(".*(" + getString(R.string.github_url_regex) + ").*", Pattern.DOTALL);
                Matcher m = p.matcher(text);
                if (m.matches()) {
                    String url = m.group(1);
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    if (null == notificationManager) {
                        Log.e("service", "notification manager is null");
                        return;
                    }
                    NotificationChannel channelCopy;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        channelCopy = new NotificationChannel("copy", getString(R.string.notification_title), NotificationManager.IMPORTANCE_MIN);
                        channelCopy.setDescription(getString(R.string.notification_copy_description));
                        notificationManager.createNotificationChannel(channelCopy);
                    }

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("url", url);

                    TaskStackBuilder taskStack = TaskStackBuilder.create(this);
                    taskStack.addParentStack(MainActivity.class);
                    taskStack.addNextIntent(intent);
                    PendingIntent pendingIntent = taskStack.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                    Notification.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        builder = new Notification.Builder(this, "copy");
                    } else {
                        builder = new Notification.Builder(this);
                    }
                    builder.setSmallIcon(R.mipmap.github_mark)
                            .setContentTitle(getString(R.string.notification_title))
                            .setContentText(url);

                    RemoteInput remoteInput = new RemoteInput.Builder("code")
                            .setLabel(getString(R.string.code))
                            .build();

                    Notification.Action action;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        action = new Notification.Action.Builder(Icon.createWithResource(this, R.mipmap.github_mark), getString(R.string.shorten), pendingIntent).addRemoteInput(remoteInput).build();
                        builder.addAction(action);
                    } else {
                        action = new Notification.Action.Builder(R.mipmap.github_mark, getString(R.string.shorten), pendingIntent).addRemoteInput(remoteInput).build();
                        builder.addAction(action);
                    }

                    Notification notification = builder.build();
                    notificationManager.notify(1, notification);

                    Toast.makeText(this, url, Toast.LENGTH_SHORT).show();

                }
            }
        }
    }
}
