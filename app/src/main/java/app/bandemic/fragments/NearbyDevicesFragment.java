package app.bandemic.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

import app.bandemic.R;

public class NearbyDevicesFragment extends Fragment {

    private RelativeLayout layout;

    private List<NearbyDeviceView> nearbyDeviceViews = new ArrayList<>();


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.nearby_devices_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layout = view.findViewById(R.id.layout);
    }

    private class NearbyDeviceView {
        ImageView iv;
        SpringAnimation animX;
        SpringAnimation animY;
    }

    private void addNearbyDevice() {
        ImageView newIV = new ImageView(this.getContext());
        newIV.setImageDrawable(getResources().getDrawable(R.drawable.nearby_device));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = 0;
        layoutParams.leftMargin = 0;
        layoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.nearby_device_myself);
        layoutParams.addRule(RelativeLayout.ALIGN_TOP, R.id.nearby_device_myself);
        layout.addView(newIV, layoutParams);

        final SpringAnimation anim1X = new SpringAnimation(newIV,
                DynamicAnimation.TRANSLATION_X);
        final SpringAnimation anim1Y = new SpringAnimation(newIV,
                DynamicAnimation.TRANSLATION_Y);
        anim1X.setSpring(new SpringForce().setStiffness(SpringForce.STIFFNESS_LOW));
        anim1Y.setSpring(new SpringForce().setStiffness(SpringForce.STIFFNESS_LOW));

        NearbyDeviceView ndv = new NearbyDeviceView();
        ndv.iv = newIV;
        ndv.animX = anim1X;
        ndv.animY = anim1Y;

        nearbyDeviceViews.add(ndv);
    }

    private void removeNearbyDevice() {
        if (nearbyDeviceViews.size() > 0) {
            NearbyDeviceView removed = nearbyDeviceViews.remove(0);
            removed.animX.cancel();
            removed.animY.cancel();
            layout.removeView(removed.iv);
        }
    }

    private void updateDevicePositions(double[] distances) {
        if (nearbyDeviceViews.size() < distances.length) {
            addNearbyDevice();
        }
        if (nearbyDeviceViews.size() > distances.length) {
            removeNearbyDevice();
        }

        double angle_min = 0f / 180f * Math.PI;
        double angle_max = Math.PI - angle_min;
        double angle_range = angle_max - angle_min;

        for (int i = 0; i < nearbyDeviceViews.size(); i++) {
            double angle = angle_min + (angle_range / (nearbyDeviceViews.size() + 1)) * (i + 1);
            double min_distance_on_screen = 100d;
            double max_distance_on_screen = 300d;
            double distance_scaling = 3d; // Make bigger to make effect of actual distance stronger in near range
            double distance = max_distance_on_screen - (1d/((distances[i]/ distance_scaling)+1d) * (max_distance_on_screen - min_distance_on_screen));
            double top = -Math.sin(angle) * distance;
            double left = Math.cos(angle) * distance;
            NearbyDeviceView ndv = nearbyDeviceViews.get(i);
            ndv.animX.animateToFinalPosition((float) left);
            ndv.animY.animateToFinalPosition((float) top);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(nearbyDevicesBroadcastReceiver,
                new IntentFilter("nearby-devices"));
    }

    private BroadcastReceiver nearbyDevicesBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double[] distances = intent.getDoubleArrayExtra("distances");

            updateDevicePositions(distances);
        }
    };

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(this.getContext())
                .unregisterReceiver(nearbyDevicesBroadcastReceiver);
        super.onPause();
    }
}