package tj.personal.thecompass

import android.hardware.SensorEvent
import android.location.Location
import com.google.android.gms.location.LocationResult

interface Contract {
    interface View {
        fun animateCompass(currentAzimuth: Float, azimuth: Float)
        fun animateArrow(angle: Float)
        fun showDistanceToDestination(distanceInMeters: Float)
    }

    interface SensorLogic {
        fun calculateDistanceToDestination() : Float
        fun calculateArrowOrientation() : Float
        fun calculateCompassOrientation(sensorEvent: SensorEvent) : Pair<Float,Float>
        fun setDestination(lat: Double, lng: Double)
        fun getCurrentLocation() : Location
        fun setCurrentLocation(location: Location)
    }

    interface Presenter {
        fun onSensorChanged(sensorEvent: SensorEvent)
        fun onLocationChanged(locationResult: LocationResult?)
        fun onDestinationSet(lat: Double, lng: Double)
        fun checkIsCurrentLocationSet() : Boolean

    }
}