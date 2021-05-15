package tj.personal.thecompass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

import tj.personal.thecompass.dialogs.SetDestinationDialog;

public class MainActivity extends AppCompatActivity implements SensorEventListener, SetDestinationDialog.SetDestinationDialogListener {

    private SensorManager mSensorManger;
    private String TAG = this.getClass().getSimpleName();
    private float[] mGravity = new float[3];
    private float[] mGeometric = new float[3];
    private float azimuth = 0f;
    private float currentAzimuth = 0f;
    private ImageView compassImage;
    private Button setDestinationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        compassImage = findViewById(R.id.compassImage);
        setDestinationBtn = findViewById(R.id.setDestinationBtn);
        initServices();
    }

    private void initServices() {
        mSensorManger = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
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
            boolean success = SensorManager.getRotationMatrix(R,I, mGravity,mGeometric);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = (float)Math.toDegrees(orientation[0]);
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
        onPause();
        DialogFragment dialog = new SetDestinationDialog();
        dialog.show(getSupportFragmentManager(),"SetDestinationDialog");
    }

    @Override
    public void onDialogConfirmClick(@NotNull String latitude) {
        Log.v(TAG, "INPUT " + latitude);
        // Resume compass animation
        onResume();
    }
}



