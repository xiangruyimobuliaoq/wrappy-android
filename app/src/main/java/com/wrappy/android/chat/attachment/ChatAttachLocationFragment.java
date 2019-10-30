package com.wrappy.android.chat.attachment;


import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.wrappy.android.R;
import com.wrappy.android.common.utils.InputUtils;

public class ChatAttachLocationFragment extends Fragment implements OnClickListener, OnMapReadyCallback {
    private static final String KEY_CAMERA_POSITION = "camera_position";

    private static final int REQUEST_ACCESS_FINE_LOCATION = 0;
    private static final int REQUEST_CHECK_LOCATION_SETTINGS = 1;

    private ProgressBar mProgressBar;
    private View mPinIcon;

    private View mNoticePermissionNotGrantedGroup;
    private View mNoticeLocationNotEnabledGroup;

    private GoogleMap mGoogleMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private CameraPosition mCameraPosition;

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            Location location = locationResult.getLastLocation();
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()),
                    15)
            );
            mFusedLocationProviderClient.removeLocationUpdates(this);
        }
    };

    public CameraPosition getCameraPosition() {
        return mGoogleMap.getCameraPosition();
    }

    public String getLatLngZoom() {
        if (mGoogleMap == null) {
            return "0,0";
        }

        return mGoogleMap.getCameraPosition().target.latitude + "," +
                mGoogleMap.getCameraPosition().target.longitude + "," +
                mGoogleMap.getCameraPosition().zoom;
    }

    public String getLatLng() {
        if (mGoogleMap == null) {
            return "0,0";
        }
        return mGoogleMap.getCameraPosition().target.latitude + "," +
                mGoogleMap.getCameraPosition().target.longitude;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_attach_location_map, container, false);

        if (savedInstanceState != null) {
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        mProgressBar = view.findViewById(R.id.loading);
        mPinIcon = view.findViewById(R.id.map_pin_icon);
        mNoticePermissionNotGrantedGroup = view.findViewById(R.id.notice_permission_not_granted);
        mNoticeLocationNotEnabledGroup = view.findViewById(R.id.notice_location_not_enabled);

        mNoticePermissionNotGrantedGroup
                .findViewById(R.id.notice_permission_not_granted_settings)
                .setOnClickListener(this);

        mNoticeLocationNotEnabledGroup
                .findViewById(R.id.notice_location_not_enabled_settings)
                .setOnClickListener(this);
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (mGoogleMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mCameraPosition);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onDestroyView() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        super.onDestroyView();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        checkLocationPermissions();
    }

    private void checkLocationPermissions() {
        boolean isGranted = false;
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            isGranted = true;
        } else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ACCESS_FINE_LOCATION);
        }
        initUI(isGranted);
    }

    private void initUI(boolean isLocationPermissionEnabled) {
        if (!isLocationPermissionEnabled) {
            showPermissionDisabledUI();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);
        mNoticePermissionNotGrantedGroup.setVisibility(View.GONE);
        checkLocationSettingEnabled();
    }

    private void checkLocationSettingEnabled() {
        LocationRequest locationRequest = createLocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(getActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(getActivity(), locationSettingsResponse -> {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            mProgressBar.setVisibility(View.VISIBLE);
            mNoticeLocationNotEnabledGroup.setVisibility(View.GONE);
            initMap();
        });

        task.addOnFailureListener(getActivity(), exception -> {
            if (exception instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) exception;
                    startIntentSenderForResult(
                            resolvable.getResolution().getIntentSender(),
                            REQUEST_CHECK_LOCATION_SETTINGS,
                            null, 0, 0, 0, null);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }

    private void initMap() {
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commitAllowingStateLoss();
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        mProgressBar.setVisibility(View.GONE);
        mPinIcon.setVisibility(View.VISIBLE);

        //setup google map here
        mGoogleMap.setMinZoomPreference(10);
        mGoogleMap.setBuildingsEnabled(false);

        try {
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

            if (mCameraPosition != null) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            } else {
                mFusedLocationProviderClient.requestLocationUpdates(createLocationRequest(), mLocationCallback, null);
            }
        } catch (SecurityException e) {
            showPermissionDisabledUI();
        }
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void showPermissionDisabledUI() {
        mProgressBar.setVisibility(View.GONE);
        mNoticeLocationNotEnabledGroup.setVisibility(View.GONE);
        mNoticePermissionNotGrantedGroup.setVisibility(View.VISIBLE);
    }

    private void showLocationSettingDisabledUI() {
        mProgressBar.setVisibility(View.GONE);
        mNoticeLocationNotEnabledGroup.setVisibility(View.VISIBLE);
        mNoticePermissionNotGrantedGroup.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_LOCATION_SETTINGS:
                if (resultCode == Activity.RESULT_OK) {
                    checkLocationSettingEnabled();
                } else {
                    showLocationSettingDisabledUI();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            initUI(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.notice_permission_not_granted_settings:
                startActivity(InputUtils.createAppSettingsIntent(getContext()));
                break;
            case R.id.notice_location_not_enabled_settings:
                checkLocationSettingEnabled();
                break;

        }
    }
}