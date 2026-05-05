import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Tests {

    @Test
    void trueIsTrue() {
        assertTrue(true);
    }

    @Test
    void bodyCircularOrbitInitialState() {
        Body central = new Body("Central", 5.972e24, 6.371e6);
        Body satellite = new Body("Satellite", 1.0e20, 1.0e5, central, 4.0e7);

        assertEquals("Satellite", satellite.getName());
        assertEquals(4.0e7, satellite.getX());
        assertEquals(0.0, satellite.getY());
        assertEquals(0.0, satellite.getZ());
        assertEquals(0.0, satellite.getVelocityX());
        assertTrue(satellite.getVelocityY() > 0.0);
        assertEquals(0.0, satellite.getVelocityZ());
        assertNotNull(satellite.getDensity());
        assertTrue(satellite.getDensity() > 0.0);
    }

    @Test
    void bodyDensityCalculationMatchesMassAndRadius() {
        double mass = 1.0e12;
        double radius = 1000.0;
        Body rock = new Body("Rock", mass, radius);
        double expectedDensity = mass / (Math.PI * Math.pow(radius, 2));

        assertEquals(expectedDensity, rock.getDensity());
    }

    @Test
    void bodyOrbitParameterSettersRecomputeVelocityAndPosition() {
        Body parent = new Body("Parent", 5.972e24, 6.371e6);
        Body orbiter = new Body("Orbiter", 1.0e20, 1.0e5, parent, 4.0e7, 4.2e7);
        double initialSpeed = Math.hypot(orbiter.getVelocityX(), orbiter.getVelocityY());

        orbiter.setPerigee(5.5e7);
        double updatedSpeed = Math.hypot(orbiter.getVelocityX(), orbiter.getVelocityY());

        assertNotEquals(initialSpeed, updatedSpeed);
        assertEquals(5.5e7, orbiter.getX());
        assertEquals(0.0, orbiter.getY());
    }

    @Test
    void starCalculatesLuminosityAndSpectralClass() {
        Star sol = new Star("Sol", 1.0, 1.0, 1.0, 5778);

        assertEquals('G', sol.getSpectralClass());
        assertEquals(5778.0, sol.getTemperature());

        double expected = 4 * Math.PI * Math.pow(6.957e8, 2) * 5.670374419e-8 * Math.pow(5778.0, 4);
        assertEquals(expected, sol.getLuminosity(), expected * 0.001);
    }

    @Test
    void starSetTemperatureUpdatesDerivedProperties() {
        Star star = new Star("Test", 1.0, 1.0, 1.0, 5000);

        star.setTemperature(10000);

        assertEquals(10000.0, star.getTemperature());
        assertEquals('B', star.getSpectralClass());
        assertTrue(star.getLuminosity() > 0.0);
    }

    @Test
    void simulationStepAdvancesTimeAndUpdatesBodyPositions() {
        Simulation sim = new Simulation();
        Body sun = new Body("Sun", 1.0e26, 1.0e8);
        Body probe = new Body("Probe", 1.0e20, 1.0e5);

        sun.setX(0.0);
        sun.setY(0.0);
        sun.setZ(0.0);
        probe.setX(1.0e7);
        probe.setY(0.0);
        probe.setZ(0.0);
        probe.setVelocityX(0.0);
        probe.setVelocityY(0.0);
        probe.setVelocityZ(0.0);

        sim.addBody(sun);
        sim.addBody(probe);
        sim.step(1.0);
        sim.step(1.0);

        assertEquals(2.0, sim.getTime());
        assertTrue(probe.getX() < 1.0e7);
        assertEquals(0.0, probe.getY());
        assertTrue(probe.getVelocityX() < 0.0);
    }

    @Test
    void simulationAddAndRemoveBodiesKeepsListInSync() {
        Simulation sim = new Simulation();
        Body a = new Body("A", 1.0e10, 1.0e3);
        Body b = new Body("B", 1.0e10, 1.0e3);

        sim.addBody(a);
        sim.addBody(b);

        assertEquals(2, sim.getBodies().size());

        sim.removeBody(a);
        assertEquals(1, sim.getBodies().size());
        assertSame(b, sim.getBodies().get(0));
    }
}
