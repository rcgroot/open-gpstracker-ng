package nl.sogeti.android.gpstracker.map;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import nl.sogeti.android.gpstracker.v2.R;

public class TrackMapActivityFragment extends Fragment {

    public TrackMapActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_track_map, container, false);
    }
}
