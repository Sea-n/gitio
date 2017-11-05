package taipei.sean.gitio;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

public class HistoryActivity extends AppCompatActivity {
    final private int _dbVer = 1;
    private SeanDBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = new SeanDBHelper(this, "data.db", null, _dbVer);

        final HistoryAdapter listAdapter = new HistoryAdapter(getApplicationContext());

        final RecyclerView listView = findViewById(R.id.list);
        listView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        listView.setAdapter(listAdapter);
    }

    public static class Store {
        private long id, date;
        private String url, code;

        public Store() {
            url = "";
            code = "";
            this.id = -1;
            this.date = -1;
        }

        public Store(String url, String code) {
            this.url = url;
            this.code = code;
            this.id = -1;
            this.date = -1;
        }

        public Store(String url, String code, long id) {
            this.url = url;
            this.code = code;
            this.id = id;
            this.date = -1;
        }

        public Store(String url, String code, long id, long date) {
            this.url = url;
            this.code = code;
            this.id = id;
            this.date = date;
        }

        public long getId() {
            return id;
        }

        public String getUrl() {
            return this.url;
        }

        public String getCode() {
            return this.code;
        }

        public long getDate() {
            return date;
        }
    }
}
