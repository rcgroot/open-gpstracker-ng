package nl.sogeti.android.gpstracker.map;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toolbar;

import butterknife.Bind;
import butterknife.ButterKnife;
import nl.sogeti.android.gpstracker.v2.R;


public class TrackMapActivity extends Activity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_map);
        ButterKnife.bind(this);
        setActionBar(toolbar);
    }
}
