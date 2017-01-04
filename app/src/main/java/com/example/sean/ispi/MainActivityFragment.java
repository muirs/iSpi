package com.example.sean.ispi;

import android.location.Location;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    private final String TAG = "ispi";

    private TextView mID;
    private TextView mStats;
    private Button mCheck;
    //private Button mHint;
    private Button mNew;
    private WebView mImage;

    private double mLat = 0.0;
    private double mLong = 0.0;

    private float range;
    private int stats;
    private String picId;
    private String picPath;
    private double picLat = 0;
    private double picLong = 0;

    //boolean toggle = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mID = (TextView) view.findViewById(R.id.mID);
        mStats = (TextView) view.findViewById(R.id.mStats);
        mCheck = (Button) view.findViewById(R.id.mCheck);
        //mHint = (Button) view.findViewById(R.id.mHint);
        mNew = (Button) view.findViewById(R.id.mNew);
        mImage = (WebView) view.findViewById(R.id.mImg);

        mCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).updateLocation();
                Location lastLocation = ((MainActivity) getActivity()).getLocation();
                  if(lastLocation != null){
                    /*if((mLat-1) <= lastLocation.getLatitude() && lastLocation.getLatitude() <= (mLat+1) && mLong-1 <= lastLocation.getLongitude() && lastLocation.getLongitude() <= mLong + 1)
                        Toast.makeText(getContext(), "Wrong! Try looking somewhere else!", Toast.LENGTH_SHORT).show();
                    else {
                        Toast.makeText(getContext(), "You found it!", Toast.LENGTH_SHORT).show();
                        stats++;
                        mStats.setText(stats);}*/

                    stats++;
                    mStats.setText(Integer.toString(stats));

                }
            }
        });
        mNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            try {
                ((MainActivity) getActivity()).fetchPhoto();
                mImage.loadUrl("http://seanpmuir.com/ispi/uploads/" + picPath);
                mID.setText(picId);
            } catch(Exception e){

            }
            }
        });

        return view;
    }

    public void setRange(float r){
        range = r;
        Toast.makeText(getContext(), "Range is " + Float.toString(range), Toast.LENGTH_SHORT).show();
    }

    public void resetStats(){
        stats = 0;
        Toast.makeText(getContext(), "Score Reset!", Toast.LENGTH_SHORT).show();
    }

    public void getData(String s){
        String[] parts = s.split(",");
        picId = parts[0];
        picPath = parts[1];
        picLat = Double.valueOf(parts[2]);
        picLong = Double.valueOf(parts[2]);
    }
}
