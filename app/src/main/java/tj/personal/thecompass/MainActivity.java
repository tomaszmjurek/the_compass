package tj.personal.thecompass;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
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

public class MainActivity extends AppCompatActivity implements SensorEventListener, SetDestinationDialog.SetDestinationDialogListener, Contract.View {

    private String TAG = this.getClass().getSimpleName();

    private Contract.Presenter presenter;

    // Layout elements
    private ImageView compassImage;
    private TextView destinationTV;
    private ImageView arrowImage;

    // Compass
    private SensorManager mSensorManger;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        compassImage = findViewById(R.id.compassImage);
        destinationTV = findViewById(R.id.destination_text_view);
        destinationTV.setText(getString(R.string.destination_info));
        arrowImage = findViewById(R.id.arrow_image);
        arrowImage.setVisibility(View.INVISIBLE);

        presenter = new Presenter(this, new SensorLogic());
        initServices();
    }

    private void initServices() {
        mSensorManger = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); //?
        initLocationUpdates();
    }

    private void initLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                presenter.onLocationChanged(locationResult);
            }
        };
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

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManger.unregisterListener(this);
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        presenter.onSensorChanged(sensorEvent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void onClickSetDestinationBtn(View view) {
        if (presenter.checkIsCurrentLocationSet()) {
            // Pause compass animation to save memory
            onPause();
            DialogFragment dialog = new SetDestinationDialog();
            dialog.show(getSupportFragmentManager(), "SetDestinationDialog");
        } else {
            Toast.makeText(this, "No GPS location detected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDialogConfirmClick(double lat, double lng) {
        // Resume compass animation
        presenter.onDestinationSet(lat, lng);
        arrowImage.setVisibility(View.VISIBLE);
        onResume();
    }


    private void showAlertLocation() {
        DialogFragment dialog = new EnableLocationDialog();
        dialog.show(getSupportFragmentManager(), "EnableLocationDialog");
    }

    @Override
    public void animateCompass(float currentAzimuth, float azimuth) {
        Animation animation = new RotateAnimation(-currentAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        animation.setDuration(500);
        animation.setRepeatCount(0);
        animation.setFillAfter(true);

        compassImage.startAnimation(animation);
    }

    @Override
    public void animateArrow(float angle) {
        Animation animation = new RotateAnimation(angle, angle, //?
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        animation.setDuration(100);
        animation.setRepeatCount(0);
        animation.setFillAfter(true);

        arrowImage.startAnimation(animation);
    }

    @Override
    public void showDistanceToDestination(float distanceInMeters) {
        destinationTV.setText(String.format(getString(R.string.destination_distance_text), distanceInMeters));
    }
}



