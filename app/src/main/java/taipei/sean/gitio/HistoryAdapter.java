package taipei.sean.gitio;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.CLIPBOARD_SERVICE;

public class HistoryAdapter extends RecyclerView.Adapter {
    final private int _dbVer = 1;
    private SeanDBHelper db;
    private Context context;
    private ArrayList<HistoryActivity.Store> iList;

    HistoryAdapter(Context context) {
        this.context = context;
        db = new SeanDBHelper(context, "data.db", null, _dbVer);
        iList = db.getHistory();
    }

    public void updateList() {
        iList = db.getHistory();
        notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        HistoryItemCell view = new HistoryItemCell(parent.getContext());
        ViewGroup.LayoutParams params = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);

        return new DummyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final HistoryActivity.Store store = iList.get(position);
        HistoryItemCell itemCell = (HistoryItemCell) holder.itemView;


        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm", Locale.US);
        Date date = new Date(store.getDate());
        final String code = store.getCode();
        final String url = store.getUrl();
        final String time = sdf.format(date);
        final long id = store.getId();
        itemCell.setCodeAndUrlAndDate(code, url, time);

        itemCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(context.getString(R.string.app_name), "https://git.io/" + code);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                }
                Toast.makeText(context, context.getString(R.string.copied), Toast.LENGTH_SHORT).show();
            }
        });

        itemCell.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(view.getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.delete_title)
                        .setMessage(R.string.delete_message)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.deleteHistory(id);
                                iList = db.getHistory();
                                notifyDataSetChanged();
                            }

                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return iList.size();
    }

    class DummyViewHolder extends RecyclerView.ViewHolder {
        DummyViewHolder(View itemView) {
            super(itemView);
        }
    }
}
