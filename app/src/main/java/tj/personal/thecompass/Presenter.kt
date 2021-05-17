package tj.personal.thecompass

import android.hardware.SensorEvent
import com.google.android.gms.location.LocationResult
import kotlin.math.round

class Presenter(
        private var mainView: Contract.View?,
        private val sensorLogic: Contract.SensorLogic): Contract.Presenter {

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        val azimuthPair :Pair<Float,Float>? = sensorLogic.calculateCompassOrientation(sensorEvent) //listener
        val arrowAngle = sensorLogic.calculateArrowOrientation()
        if (mainView != null && azimuthPair != null) {
            mainView!!.animateCompass(azimuthPair.first, azimuthPair.second)
            mainView!!.animateArrow(arrowAngle)
        }
    }

    override fun onLocationChanged(locationResult: LocationResult?) {
        if (locationResult == null) return

        if (locationResult.locations.isNotEmpty()) {
            sensorLogic.setCurrentLocation(locationResult.lastLocation)
            getDistanceToDestination()
        }
    }

    override fun onDestinationSet(lat: Double, lng: Double) {
        sensorLogic.setDestination(lat, lng)
        getDistanceToDestination()
    }

    private fun getDistanceToDestination() {
        val distance = sensorLogic.calculateDistanceToDestination()
        if (mainView != null) {
            mainView!!.showDistanceToDestination(round(distance))
        }
    }

    override fun checkIsCurrentLocationSet(): Boolean {
        if (sensorLogic.getCurrentLocation() != null) return true
        return false
    }

}