package taipei.sean.gitio;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;


public class HistoryItemCell extends FrameLayout {
    TextView codeView;
    TextView urlView;
    TextView dateView;

    public HistoryItemCell(Context context) {
        super(context);

        codeView = new TextView(context);
        codeView.setTextColor(Color.BLUE);
        codeView.setTextSize(24);
        codeView.setLines(1);
        codeView.setEllipsize(TextUtils.TruncateAt.END);
        addView(codeView);

        urlView = new TextView(context);
        urlView.setTextColor(Color.BLACK);
        urlView.setTextSize(16);
        urlView.setLines(1);
        urlView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        addView(urlView);

        dateView = new TextView(context);
        dateView.setTextColor(Color.LTGRAY);
        dateView.setTextSize(12);
        dateView.setGravity(Gravity.RIGHT);
        addView(dateView);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = 160;

        dateView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), 48);

        codeView.measure(MeasureSpec.makeMeasureSpec(width - dateView.getMeasuredWidth() - 16, MeasureSpec.AT_MOST), 96);
        urlView.measure(MeasureSpec.makeMeasureSpec(width - dateView.getMeasuredWidth() - 36, MeasureSpec.AT_MOST), 64);

        setMeasuredDimension(width, height);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int height = bottom - top;
        int width = right - left;

        codeView.layout(0, 0, width - dateView.getMeasuredWidth() - 16, 96);

        urlView.layout(12, 96, width - dateView.getMeasuredWidth() - 24, 160);

        dateView.layout(width - dateView.getMeasuredWidth(), 112, width, height);
    }

    public void setCodeAndUrlAndDate(String code, String url, String date) {
        codeView.setText(code);
        urlView.setText(url);
        dateView.setText(date);
    }
}
