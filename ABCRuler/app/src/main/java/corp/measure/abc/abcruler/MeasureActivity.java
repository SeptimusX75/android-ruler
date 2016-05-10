package corp.measure.abc.abcruler;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MeasureActivity extends AppCompatActivity {

    private RulerView mRulerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);
        mRulerView = ((RulerView) findViewById(R.id.ruler_view));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_measure_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toggle_detail_ticks:
                if (item.isChecked()) {
                    item.setChecked(false);
                    mRulerView.setDisplayAllLabels(false);
                } else {
                    item.setChecked(true);
                    mRulerView.setDisplayAllLabels(true);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
