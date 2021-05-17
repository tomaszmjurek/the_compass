package tj.personal.thecompass;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import androidx.fragment.app.DialogFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.jetbrains.annotations.NotNull;

import kotlin.Pair;
import tj.personal.thecompass.Contract;

public class Model implements Contract.Model {

    // Compass
//    private SensorManager mSensorManger;
    private float[] mGravity = new float[3];
    private float[] mGeometric = new float[3];
    private float azimuth = 0f;
    private float currentAzimuth = 0f;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager mLocationManager;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private Location destLocation;


    @Override
    public Pair<Float, Float> calculateCompassOrientation(SensorEvent sensorEvent) {
        final float alpha = 0.97f;
        synchronized (this) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * sensorEvent.values[0];
                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * sensorEvent.values[1];
                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * sensorEvent.values[2];
            }

            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
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

                float prevAzimuth = currentAzimuth;
                currentAzimuth = azimuth;
                return new Pair<>(prevAzimuth, currentAzimuth);
            }
        }
        return null;
    }

    @Override
    public Location getCurrentLocation() {
        return currentLocation;
    }

    @Override
    public void setCurrentLocation(Location location) {
        this.currentLocation = location;
    }

    @Override
    public float calculateDistanceToDestination() {
        float distanceInMeters = 0;
        if (currentLocation != null && destLocation != null) {
            distanceInMeters = destLocation.distanceTo(currentLocation);
//            Log.v(TAG, getString(R.string.destination_distance_text) + distanceInMeters + " m");
        }
        return distanceInMeters;
    }

    @Override
    public float calculateArrowOrientation() {
        float angle = 0f;
        if (currentLocation != null && destLocation != null) {
            float bearing = currentLocation.bearingTo(destLocation);

            GeomagneticField geoField = new GeomagneticField(
                    Double.valueOf(currentLocation.getLatitude()).floatValue(),
                    Double.valueOf(currentLocation.getLongitude()).floatValue(),
                    Double.valueOf(currentLocation.getAltitude()).floatValue(),
                    System.currentTimeMillis()
            );

            float heading = currentAzimuth;
            heading += geoField.getDeclination();
            heading = (bearing + heading);
//            float prevAngle = normalizeDegree(currentAzimuth);
            angle = normalizeDegree(heading);
        }
        return angle;
    }

    private float normalizeDegree(float value) {
        return (value + 360) % 360;
    }

    @Override
    public void setDestination(double lat, double lng) {
        destLocation = new Location("");
        destLocation.setLatitude(lat);
        destLocation.setLongitude(lng);
    }
}
