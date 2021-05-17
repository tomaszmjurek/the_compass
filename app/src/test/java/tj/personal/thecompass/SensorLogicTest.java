package tj.personal.thecompass;

import android.location.Location;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;


public class SensorLogicTest {

    // Location
    @Mock
    private Location currentLocation;
    @Mock
    private Location destLocation;

    Contract.SensorLogic sensorLogic;

    @Before
    public void setUp() throws Exception {
        sensorLogic = new SensorLogic();

        currentLocation = Mockito.mock(Location.class);
        currentLocation.setLatitude(0);
        currentLocation.setLongitude(0);

        destLocation = Mockito.mock(Location.class);
        destLocation.setLatitude(10);
        destLocation.setLongitude(0);
    }


    @Test
    public void test_calculate_distance_to_destination() {
        float result = sensorLogic.calculateDistanceToDestination();
        assertEquals(currentLocation.distanceTo(destLocation), result, 1);
    }

    @Test
    public void test_calculate_arrow_orientation() {
        float result = sensorLogic.calculateArrowOrientation();
        assertEquals(0, result, 0.01);
    }

}