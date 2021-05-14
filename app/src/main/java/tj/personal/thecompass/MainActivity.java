package tj.personal.thecompass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManger;
//    private lateinit var mOrientationListener: SensorEventListener
//    private lateinit var mMagneticListener: SensorEventListener
////    private lateinit var accelerometerValues: FloatArray
////    private lateinit var magneticFieldValues: FloatArray
//    private lateinit var mOrientationSensor: Sensor
//    private var mMagneticSensor: Sensor? = null

//    private lateinit var mGeomagnetic: FloatArray
    private float[] mGravity = new float[3];
    private float[] mGeometric = new float[3];
    private float azimuth = 0f;
    private float currentAzimuth = 0f;
    private ImageView compassImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        compassImage = findViewById(R.id.compassImage);
        initServices();
    }

    private void initServices() {
        mSensorManger = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
//        mOrientationSensor = mSensorManger.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//        mMagneticSensor = mSensorManger.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
     }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManger.registerListener( this,
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
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
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
            boolean success = SensorManager.getRotationMatrix(R,I, mGravity,mGeometric);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = (float)Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;

                Animation animation = new RotateAnimation(-currentAzimuth, -azimuth,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);

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

}


