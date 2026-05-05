/**
 * Multi-body simulation star class
 * using 3d graphics to make it look pretty
 * 
 * @author capnqueso
 * @date 5/4/26
 */
public class Star extends Body {

    /**
     * Stars Brightness
     * used for display
     */
    private Double brightness;

    /**
     * temperture of the star (in kelvin)
     * used for display
     */
    private double temperature;

    /**
     * charachter to determine color class
     * used for display
     */
    private char spectralClass;

    /**
     * energy output in watts
     */
    private Double luminosity;

    /**
     * 1 solar mass (for easier input)
     * 1 solar mass is equal to 1.989 * 10^30 kg
     */
    private static final double solarMass = 1.989e30;

    /**
     * 1 solar radius (for easier input)
     * 1 solar radius is equal to 6.957 * 10^8 m
     */
    private static final double solarRadius = 6.957e8;

    /**
     * Free-floating star with full motion vector
     */
    public Star(String name, Double mass, Double radius, Double velocity, Double angleX, Double angleY,
            Double brightness, double temperature) {
        super(name, mass * solarMass, radius * solarRadius); // free floating, set vx, vy, vz later
        this.brightness = brightness;
        this.temperature = temperature;
        this.luminosity = calcLuminosity(temperature, radius * solarRadius);
        this.spectralClass = calcSpectralClass(temperature);
    }

    /**
     * Free-floating star, motion unknown or to be calculated later
     */
    public Star(String name, Double mass, Double radius, Double brightness, double temperature) {
        super(name, mass * solarMass, radius * solarRadius);
        this.brightness = brightness;
        this.temperature = temperature;
        this.luminosity = calcLuminosity(temperature, radius * solarRadius);
        this.spectralClass = calcSpectralClass(temperature);
    }

    /**
     * Orbiting star with elliptical orbit
     * (e.g. binary star systems)
     */
    public Star(String name, Double mass, Double radius, Body orbits, Double perigee, Double apogee,
            Double brightness, double temperature) {
        super(name, mass * solarMass, radius * solarRadius, orbits, perigee, apogee);
        this.brightness = brightness;
        this.temperature = temperature;
        this.luminosity = calcLuminosity(temperature, radius * solarRadius);
        this.spectralClass = calcSpectralClass(temperature);
    }

    /**
     * Orbiting star with circular orbit
     */
    public Star(String name, Double mass, Double radius, Body orbits, Double altitude,
            Double brightness, double temperature) {
        super(name, mass * solarMass, radius * solarRadius, orbits, altitude);
        this.brightness = brightness;
        this.temperature = temperature;
        this.luminosity = calcLuminosity(temperature, radius * solarRadius);
        this.spectralClass = calcSpectralClass(temperature);
    }

    /**
     * Calculates luminosity using Stefan-Boltzmann law
     * L = 4 * pi * r^2 * sigma * T^4
     */
    private double calcLuminosity(double temperature, double radius) {
        final double stefanBoltzmann = 5.670374419e-8;
        return 4 * Math.PI * Math.pow(radius, 2) * stefanBoltzmann * Math.pow(temperature, 4);
    }

    /**
     * Approximates spectral class from temperature
     */
    private char calcSpectralClass(double temperature) {
        if (temperature >= 33000)
            return 'O';
        if (temperature >= 10000)
            return 'B';
        if (temperature >= 7300)
            return 'A';
        if (temperature >= 6000)
            return 'F';
        if (temperature >= 5300)
            return 'G';
        if (temperature >= 3900)
            return 'K';
        return 'M';
    }

    // GETTERS



    /**
     * Gets the star's luminosity (energy output in watts)
     * @return the luminosity of the star
     */
    public Double getLuminosity() {
        return luminosity;
    }

    /**
     * Gets the star's temperature in Kelvin
     * @return the temperature of the star
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * Gets the star's spectral class
     * @return the spectral class character (O, B, A, F, G, K, or M)
     */
    public char getSpectralClass() {
        return spectralClass;
    }

    // SETTERS (debug)

    /**
     * Sets the star's brightness
     * @param brightness the brightness value to set
     */
    public void setBrightness(Double brightness) {
        this.brightness = brightness;
    }

    /**
     * Sets the star's temperature in Kelvin
     * Also recalculates luminosity and spectral class based on new temperature
     * @param temperature the temperature in Kelvin to set
     */
    public void setTemperature(double temperature) {
        this.temperature = temperature;
        this.luminosity = calcLuminosity(temperature, this.getRadius());
        this.spectralClass = calcSpectralClass(temperature);
    }

    /**
     * Sets the star's luminosity (energy output in watts)
     * @param luminosity the luminosity value to set
     */
    public void setLuminosity(Double luminosity) {
        this.luminosity = luminosity;
    }
}
