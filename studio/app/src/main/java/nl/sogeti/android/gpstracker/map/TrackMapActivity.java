package nl.sogeti.android.gpstracker.map;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import nl.sogeti.android.gpstracker.data.Track;
import nl.sogeti.android.gpstracker.v2.R;
import nl.sogeti.android.gpstracker.v2.databinding.ActivityTrackMapBinding;


public class TrackMapActivity extends Activity {

    private Track track;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTrackMapBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_track_map);
        track = new Track(getString(R.string.app_name));
        binding.setTrack(track);
        setActionBar(binding.toolbar);
    }
}
