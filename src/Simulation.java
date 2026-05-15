
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Multi-body simulation main simulation runner class using 3d graphics to make
 * it look pretty
 *
 * @author capnqueso
 * @date 5/4/26
 */
public class Simulation {

    /**
     * Gravitational constant in m^3 kg^-1 s^-2
     */
    private static final double G = 6.674e-11;

    /**
     * List of all bodies in the simulation
     */
    private List<Body> bodies = new ArrayList<>();

    /**
     * Time passed in the simulation (in seconds) This will be used to calculate
     * positions of bodies in orbit, and will be able to be set and got
     */
    private double time;

    private static final double AU = 1.496e11; // Astronomical unit in meters

    // MAKE SURE THIS FILEPATH IS CORRECT
    private static final String DIR = "3d-graphing-test/src/"; // Directory for output files

    public static void main(String[] args) throws InterruptedException {
        Simulation sim = new Simulation();

        // Setup your system
        Star sun = new Star("Sol", 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 5778);
        Body earth = new Body("Earth", 5.972e24, 6.371e6, sun, 1.47e11);
        Body mars = new Body("Mars", 6.39e23, 3.389e6, sun, 2.27e11);

        sim.addBody(sun);
        sim.addBody(earth);
        sim.addBody(mars);

        // WRITE METADATA ONCE
        try (PrintWriter metaWriter = new PrintWriter(new FileWriter(DIR + "metadata.txt"))) {
            StringBuilder sb = new StringBuilder();
            for (Body b : sim.bodies) {
                String type = (b instanceof Star) ? "STAR" : "PLANET";
                double temp = (b instanceof Star) ? ((Star) b).getTemperature() : 0;
                sb.append(type).append(",").append(b.getName()).append(",")
                        .append(b.getRadius()).append(",").append(b.getMass()).append(",")
                        .append(temp).append("|");
            }
            metaWriter.println(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Simulation running... Open index.html to view live.");

        // LIVE LOOP
        while (true) {
            sim.step(3600); // 1 hour per step

            // We overwrite live.txt every frame so it never gets too big
            try (PrintWriter writer = new PrintWriter(new FileWriter(DIR + "live.txt"))) {
                StringBuilder line = new StringBuilder();
                for (Body b : sim.bodies) {
                    line.append(b.getX()).append(" ").append(b.getY()).append(" ").append(b.getZ()).append("|");
                }
                line.append(sim.time);
                writer.println(line.toString());
            } catch (IOException e) {
                // If the browser is reading while we are writing, just skip this frame
            }

            // Limit the speed so we don't melt the CPU
            Thread.sleep(10);
        }
    }

    /**
     * Advances the simulation by one timestep using Velocity Verlet integration
     * O(n²) per step
     */
    public void step(double dt) {
        // Update positions using current vel and acc
        for (Body b : bodies) {
            b.setX(b.getX() + b.getVelocityX() * dt + 0.5 * b.getAccX() * dt * dt);
            b.setY(b.getY() + b.getVelocityY() * dt + 0.5 * b.getAccY() * dt * dt);
            b.setZ(b.getZ() + b.getVelocityZ() * dt + 0.5 * b.getAccZ() * dt * dt);
        }

        // Compute new accelerations from new positions
        computeAccelerations();

        // Update velocities using average of old and new acc
        for (Body b : bodies) {
            b.setVelocityX(b.getVelocityX() + 0.5 * (b.getAccPrevX() + b.getAccX()) * dt);
            b.setVelocityY(b.getVelocityY() + 0.5 * (b.getAccPrevY() + b.getAccY()) * dt);
            b.setVelocityZ(b.getVelocityZ() + 0.5 * (b.getAccPrevZ() + b.getAccZ()) * dt);
        }

        time += dt;
    }

    private void computeAccelerations() {
        // Save old acc, zero out new
        for (Body bi : bodies) {
            bi.saveAcc(); // copies acc
            bi.zeroAcc();
        }

        // Newton's third law lets us do half the pairs
        for (int i = 0; i < bodies.size(); i++) {
            Body bi = bodies.get(i);
            for (int j = i + 1; j < bodies.size(); j++) {
                Body bj = bodies.get(j);

                double dx = bj.getX() - bi.getX();
                double dy = bj.getY() - bi.getY();
                double dz = bj.getZ() - bi.getZ();

                double r2 = dx * dx + dy * dy + dz * dz;
                double r = Math.sqrt(r2);
                double r3 = r2 * r;

                // Shared factor avoids computing G twice per pair
                double f = G / r3;

                // bi pulled toward bj, bj pulled toward bi (equal and opposite)
                bi.addAcc(f * bj.getMass() * dx,
                        f * bj.getMass() * dy,
                        f * bj.getMass() * dz);

                bj.addAcc(-f * bi.getMass() * dx,
                        -f * bi.getMass() * dy,
                        -f * bi.getMass() * dz);
            }
        }
    }

    // get set
    public void addBody(Body b) {
        bodies.add(b);
    }

    public void removeBody(Body b) {
        bodies.remove(b);
    }

    public List<Body> getBodies() {
        return bodies;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }
}
