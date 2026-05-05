import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Tests {

    @Test
    void testBodyFreeFloating() {
        Body body = new Body(1.0, 2.0, 3.0, 4.0, 5.0);
        assertEquals(1.0, body.getMass());
        assertEquals(2.0, body.getRadius());
        assertEquals(3.0, body.getVelocity());
        assertEquals(4.0, body.getAngleX());
        assertEquals(5.0, body.getAngleY());
    }

    @Test
    void testBodyFreeFloatingNoMotion() {
        Body body = new Body(1.0, 2.0);
        assertEquals(1.0, body.getMass());
        assertEquals(2.0, body.getRadius());
        assertNull(body.getVelocity());
    }

    @Test
    void testBodyOrbitingElliptical() {
        Body orbiting = new Body(10.0, 5.0);
        Body body = new Body(1.0, 2.0, orbiting, 100.0, 200.0);
        assertEquals(1.0, body.getMass());
        assertEquals(2.0, body.getRadius());
        assertEquals(orbiting, body.getOrbits());
        assertEquals(100.0, body.getPerigee());
        assertEquals(200.0, body.getApogee());
    }

    @Test
    void testBodyOrbitingCircular() {
        Body orbiting = new Body(10.0, 5.0);
        Body body = new Body(1.0, 2.0, orbiting, 150.0);
        assertEquals(1.0, body.getMass());
        assertEquals(2.0, body.getRadius());
        assertEquals(orbiting, body.getOrbits());
        assertEquals(150.0, body.getAltitude());
        assertEquals(150.0, body.getPerigee());
        assertEquals(150.0, body.getApogee());
    }

    @Test
    void testBodySetters() {
        Body body = new Body(1.0, 2.0);
        body.setMass(2.0);
        assertEquals(2.0, body.getMass());
        body.setRadius(3.0);
        assertEquals(3.0, body.getRadius());
        body.setVelocity(4.0);
        assertEquals(4.0, body.getVelocity());
        body.setAngleX(90.0);
        assertEquals(90.0, body.getAngleX());
        body.setAngleY(180.0);
        assertEquals(180.0, body.getAngleY());
    }

    @Test
    void testStarFreeFloating() {
        Star star = new Star("TestStar", 1.0, 1.0, 100.0, 0.0, 0.0, 1.0, 5772.0);
        assertEquals(1.0 * 1.989e30, star.getMass());
        assertEquals(1.0 * 6.957e8, star.getRadius());
        assertEquals(100.0, star.getVelocity());
        assertEquals(0.0, star.getAngleX());
        assertEquals(0.0, star.getAngleY());
    }

    @Test
    void testStarFreeFloatingNoMotion() {
        Star star = new Star("TestStar", 1.0, 1.0, 1.0, 5772.0);
        assertEquals(1.0 * 1.989e30, star.getMass());
        assertEquals(1.0 * 6.957e8, star.getRadius());
    }

    @Test
    void testStarOrbitingElliptical() {
        Body orbiting = new Body(10.0, 5.0);
        Star star = new Star("TestStar", 1.0, 1.0, orbiting, 100.0, 200.0, 1.0, 5772.0);
        assertEquals(1.0 * 1.989e30, star.getMass());
        assertEquals(1.0 * 6.957e8, star.getRadius());
        assertEquals(orbiting, star.getOrbits());
        assertEquals(100.0, star.getPerigee());
        assertEquals(200.0, star.getApogee());
    }

    @Test
    void testStarOrbitingCircular() {
        Body orbiting = new Body(10.0, 5.0);
        Star star = new Star("TestStar", 1.0, 1.0, orbiting, 150.0, 1.0, 5772.0);
        assertEquals(1.0 * 1.989e30, star.getMass());
        assertEquals(1.0 * 6.957e8, star.getRadius());
        assertEquals(orbiting, star.getOrbits());
        assertEquals(150.0, star.getAltitude());
    }

    @Test
    void trueIsTrue() {
        assertTrue(true);
    }
}
