package tj.personal.thecompass

import android.hardware.SensorEvent
import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

class Presenter(
        private var mainView: Contract.View?,
        private val model: Contract.Model): Contract.Presenter {

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        val azimuthPair :Pair<Float,Float>? = model.calculateCompassOrientation(sensorEvent) //listener
        val arrowAngle = model.calculateArrowOrientation()
        if (mainView != null && azimuthPair != null) {
            mainView!!.animateCompass(azimuthPair.first, azimuthPair.second)
            mainView!!.animateArrow(arrowAngle)
        }
    }

    override fun onLocationChanged(locationResult: LocationResult?) {
        if (locationResult == null) return

        if (locationResult.locations.isNotEmpty()) {
            model.setCurrentLocation(locationResult.lastLocation)
            getDistanceToDestination()
        }
    }

    override fun onDestinationSet(lat: Double, lng: Double) {
        model.setDestination(lat, lng)
        getDistanceToDestination()
    }

    private fun getDistanceToDestination() {
        val distance = model.calculateDistanceToDestination()
        if (mainView != null) {
            mainView!!.showDistanceToDestination(distance)
        }
    }

    override fun checkIsCurrentLocationSet(): Boolean {
        if (model.getCurrentLocation() != null) return true
        return false
    }


}