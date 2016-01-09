package nl.sogeti.android.gpstracker.map;

import android.app.Activity;
import android.os.Bundle;

import nl.sogeti.android.gpstracker.v2.R;


public class TrackMapActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_map);
    }
}
