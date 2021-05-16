package tj.personal.thecompass;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import tj.personal.thecompass.dialogs.EnableLocationDialog;
import tj.personal.thecompass.dialogs.SetDestinationDialog;

public class MainActivity extends AppCompatActivity implements SensorEventListener, SetDestinationDialog.SetDestinationDialogListener {

    private SensorManager mSensorManger;

    private String TAG = this.getClass().getSimpleName();
    private float[] mGravity = new float[3];
    private float[] mGeometric = new float[3];
    private float azimuth = 0f;
    private float currentAzimuth = 0f;

    // Layout elements
    private ImageView compassImage;
    private Button setDestinationBtn;
    private TextView destinationTV;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager mLocationManager;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        compassImage = findViewById(R.id.compassImage);
        setDestinationBtn = findViewById(R.id.setDestinationBtn);
        destinationTV = findViewById(R.id.destination_text_view);
        initServices();
    }

    private void initServices() {
        mSensorManger = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
        mSensorManger.registerListener(this,
                mSensorManger.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManger.registerListener(this,
                mSensorManger.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManger.unregisterListener(this);
        stopLocationUpdates();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // MOVE TO CONTROLLER
        final float alpha = 0.97f;
        synchronized (this) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//                Log.v(TAG, "ACCEL: " + sensorEvent.values[0] + " " + sensorEvent.values[1] + " " + sensorEvent.values[2]);
                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * sensorEvent.values[0];
                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * sensorEvent.values[1];
                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * sensorEvent.values[2];
            }

            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//                Log.v(TAG, "MAGNETIC: " + sensorEvent.values[0] + " " + sensorEvent.values[1] + " " + sensorEvent.values[2]);
                mGeometric[0] = alpha * mGeometric[0] + (1 - alpha) * sensorEvent.values[0];
                mGeometric[1] = alpha * mGeometric[1] + (1 - alpha) * sensorEvent.values[1];
                mGeometric[2] = alpha * mGeometric[2] + (1 - alpha) * sensorEvent.values[2];
            }

            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeometric);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;
//                Log.v(TAG, String.valueOf(azimuth));

                Animation animation = new RotateAnimation(-currentAzimuth, -azimuth,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);

                currentAzimuth = azimuth;
                animation.setDuration(500);
                animation.setRepeatCount(0);
                animation.setFillAfter(true);

                compassImage.startAnimation(animation);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void onClickSetDestinationBtn(View view) {
        // Pause compass animation to save memory
        if (currentLocation != null) {
            onPause();
            DialogFragment dialog = new SetDestinationDialog();
            dialog.show(getSupportFragmentManager(), "SetDestinationDialog");
        } else {
            Toast.makeText(this, "No GPS location detected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDialogConfirmClick(double lat, double lng) {
        Log.v(TAG, "INPUT " + lat);
        // Resume compass animation
        onResume();
        calculateDistanceToDestination(lat, lng);
    }

    private void calculateDistanceToDestination(double lat, double lng) {
        // MOVE TO CONTROLLER
        Location destLocation = new Location("");
        destLocation.setLatitude(lat);
        destLocation.setLongitude(lng);

        if (currentLocation != null) {
            float distanceInMeters = destLocation.distanceTo(currentLocation);
//            destinationTV.setText(getString(R.string.destination_distance_text) + distanceInMeters + " m");
            Log.v(TAG, String.valueOf(distanceInMeters));
        }
    }

    private void getLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(50000);
        locationRequest.setFastestInterval(50000);
        locationRequest.setSmallestDisplacement(170f); // 170 m = 0.1 mile
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //todo set according to your app function
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                if (!locationResult.getLocations().isEmpty()) {
                    currentLocation = locationResult.getLastLocation();
                }
            }
        };
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null /* Looper */
            );
        } else {
            showAlertLocation();
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void showAlertLocation() {
        DialogFragment dialog = new EnableLocationDialog();
        dialog.show(getSupportFragmentManager(), "EnableLocationDialog");
    }
}



