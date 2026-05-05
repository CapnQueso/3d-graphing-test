import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Simple 2D top-down swing visualizer for the N-body simulation
 * No external dependencies required
 * 
 * @author capnqueso
 * @date 5/4/26
 */
public class SimulationFX extends JFrame {

    private static final double AU = 1.496e11;
    private static final double SCALE = 6.0 / AU;

    // ── Camera state ──────────────────────────────────────────────────────────
    private double camX = 0; // pan offset in world meters
    private double camY = 0;
    private double camZoom = 1.0; // zoom multiplier
    private boolean camLocked = true; // lock to largest body
    private double mouseX, mouseY; // last mouse position for drag delta

    private final Simulation sim;
    private final JPanel canvas;
    private final JPanel statsPanel;
    private final JTextField dtField;

    public SimulationFX(Simulation sim) {
        this.sim = sim;

        setTitle("N-Body Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.decode("#07070f"));

        // ── Canvas ────────────────────────────────────────────────────────────
        canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawScene((Graphics2D) g);
            }
        };
        canvas.setBackground(Color.decode("#07070f"));
        canvas.setPreferredSize(new Dimension(800, 600));

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                camLocked = false; // any manual pan breaks lock
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                double dx = e.getX() - mouseX;
                double dy = e.getY() - mouseY;
                mouseX = e.getX();
                mouseY = e.getY();

                // Convert screen pixels back to world meters
                double scale = baseScale() * camZoom;
                camX -= dx / scale;
                camY += dy / scale; // flip Y
                canvas.repaint();
            }
        });
        // ── Scroll zoom ───────────────────────────────────────────────────────────
        canvas.addMouseWheelListener(e -> {
            double factor = e.getWheelRotation() < 0 ? 1.15 : 1.0 / 1.15;
            camZoom = Math.max(0.05, Math.min(200.0, camZoom * factor));
            canvas.repaint();
        });

        // ── Stats panel ───────────────────────────────────────────────────────
        statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(Color.decode("#0d0d1f"));
        statsPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.decode("#2a2a5a")));
        statsPanel.setPreferredSize(new Dimension(300, 600));

        JScrollPane statsScroll = new JScrollPane(statsPanel);
        statsScroll.setBackground(Color.decode("#0d0d1f"));
        statsScroll.setBorder(null);
        statsScroll.setPreferredSize(new Dimension(310, 600));

        // ── Controls ──────────────────────────────────────────────────────────
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        controls.setBackground(Color.decode("#05050f"));
        controls.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#1a1a3a")));

        JLabel dtLabel = new JLabel("dt (seconds):");
        dtLabel.setForeground(Color.decode("#8080aa"));
        dtLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));

        dtField = new JTextField("86400", 8);
        dtField.setBackground(Color.decode("#12122a"));
        dtField.setForeground(Color.decode("#d0d0ff"));
        dtField.setCaretColor(Color.decode("#d0d0ff"));
        dtField.setBorder(BorderFactory.createLineBorder(Color.decode("#3a3a6a")));
        dtField.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JButton stepBtn = new JButton("▶  Step");
        stepBtn.setBackground(Color.decode("#1e1e4a"));
        stepBtn.setForeground(Color.decode("#a0a0ff"));
        stepBtn.setFocusPainted(false);
        stepBtn.setBorder(BorderFactory.createLineBorder(Color.decode("#4040aa")));
        stepBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        stepBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        stepBtn.addActionListener(e -> doStep(stepBtn));

        // Auto-run toggle
        JToggleButton autoBtn = new JToggleButton("⏸  Auto");
        autoBtn.setBackground(Color.decode("#12122a"));
        autoBtn.setForeground(Color.decode("#6060aa"));
        autoBtn.setFocusPainted(false);
        autoBtn.setBorder(BorderFactory.createLineBorder(Color.decode("#2a2a5a")));
        autoBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JToggleButton lockBtn = new JToggleButton("⊙  Locked");
        lockBtn.setSelected(true);
        lockBtn.setBackground(Color.decode("#12122a"));
        lockBtn.setForeground(Color.decode("#f5c842"));
        lockBtn.setFocusPainted(false);
        lockBtn.setBorder(BorderFactory.createLineBorder(Color.decode("#4a4a1a")));
        lockBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lockBtn.addActionListener(e -> {
            camLocked = lockBtn.isSelected();
            lockBtn.setText(camLocked ? "⊙  Locked" : "⊙  Free");
            lockBtn.setForeground(camLocked ? Color.decode("#f5c842") : Color.decode("#6060aa"));
        });

        // also keep lock in sync when user manually pans
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                if (camLocked) {
                    camLocked = false;
                    lockBtn.setSelected(false);
                    lockBtn.setText("⊙  Free");
                    lockBtn.setForeground(Color.decode("#6060aa"));
                }
            }
        });

        Timer autoTimer = new Timer(16, e -> doStep(stepBtn)); // ~60fps
        autoBtn.addActionListener(e -> {
            if (autoBtn.isSelected()) {
                autoBtn.setText("⏵  Running");
                stepBtn.setEnabled(false);
                autoTimer.start();
            } else {
                autoBtn.setText("⏸  Auto");
                stepBtn.setEnabled(true);
                autoTimer.stop();
            }
        });

        controls.add(dtLabel);
        controls.add(dtField);
        controls.add(stepBtn);
        controls.add(autoBtn);

        // ── Layout ────────────────────────────────────────────────────────────
        add(canvas, BorderLayout.CENTER);
        add(statsScroll, BorderLayout.EAST);
        add(controls, BorderLayout.SOUTH);

        refreshStats();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Step + refresh ────────────────────────────────────────────────────────
    private void doStep(JButton stepBtn) {
        try {
            double dt = Double.parseDouble(dtField.getText().trim());
            sim.step(dt);
            canvas.repaint();
            refreshStats();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid timestep.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Draw scene ────────────────────────────────────────────────────────────
    private void drawScene(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        List<Body> bodies = sim.getBodies();
        if (bodies == null || bodies.isEmpty())
            return;

        int W = canvas.getWidth();
        int H = canvas.getHeight();

        // If locked, center camera on the most massive body
        if (camLocked) {
            Body anchor = largestBody(bodies);
            if (anchor != null) {
                camX = anchor.getX();
                camY = anchor.getY();
            }
        }

        double scale = baseScale() * camZoom;

        // World → screen transform
        // sx = (worldX - camX) * scale + W/2
        // sy = -(worldY - camY) * scale + H/2

        // Grid
        g.setColor(new Color(255, 255, 255, 12));
        for (int gx = 0; gx < W; gx += 60)
            g.drawLine(gx, 0, gx, H);
        for (int gy = 0; gy < H; gy += 60)
            g.drawLine(0, gy, W, gy);

        // Time + zoom overlay
        g.setColor(new Color(100, 100, 180, 180));
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.drawString(String.format("t = %.2f days", sim.getTime() / 86400.0), 10, 18);
        g.drawString(String.format("zoom: %.2fx", camZoom), 10, 34);

        Color[] palette = {
                Color.decode("#4af0c4"),
                Color.decode("#c07aff"),
                Color.decode("#ff7a7a"),
                Color.decode("#7ac8ff"),
                Color.decode("#ffa07a"),
        };

        for (int i = 0; i < bodies.size(); i++) {
            Body b = bodies.get(i);
            Color c = bodyColor(b, palette, i);

            double px = (b.getX() - camX) * scale + W / 2.0;
            double py = -(b.getY() - camY) * scale + H / 2.0;

            double r = computeRadius(b);

            // Skip drawing if way off screen
            if (px < -200 || px > W + 200 || py < -200 || py > H + 200)
                continue;

            // Glow
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 35));
            g.fill(new Ellipse2D.Double(px - r * 2, py - r * 2, r * 4, r * 4));

            // Body
            g.setColor(c);
            g.fill(new Ellipse2D.Double(px - r, py - r, r * 2, r * 2));

            // Label
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 200));
            g.setFont(new Font("Monospaced", Font.PLAIN, 11));
            g.drawString(bodyName(b), (int) (px + r + 3), (int) (py - 2));
        }
    }

    /**
     * Base scale in pixels/meter — fits all bodies on screen at zoom=1
     * Recalculated every frame so it adapts as bodies move apart
     */
    private double baseScale() {
        List<Body> bodies = sim.getBodies();
        if (bodies == null || bodies.size() < 2)
            return 250.0 / 1.496e11;

        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (Body b : bodies) {
            minX = Math.min(minX, b.getX());
            maxX = Math.max(maxX, b.getX());
            minY = Math.min(minY, b.getY());
            maxY = Math.max(maxY, b.getY());
        }

        double rangeX = Math.max(maxX - minX, 1e9);
        double rangeY = Math.max(maxY - minY, 1e9);
        int W = Math.max(canvas.getWidth(), 1);
        int H = Math.max(canvas.getHeight(), 1);

        return Math.min((W - 80) / rangeX, (H - 80) / rangeY) * 0.85;
    }

    /**
     * Returns the most massive body in the list — used as camera anchor
     */
    private Body largestBody(List<Body> bodies) {
        Body largest = null;
        double maxMass = -1;
        for (Body b : bodies) {
            if (b.getMass() != null && b.getMass() > maxMass) {
                maxMass = b.getMass();
                largest = b;
            }
        }
        return largest;
    }

    // ── Stats panel ───────────────────────────────────────────────────────────
    private void refreshStats() {
        statsPanel.removeAll();

        JLabel timeLabel = new JLabel(String.format("  t = %.2f days", sim.getTime() / 86400.0));
        timeLabel.setForeground(Color.decode("#6060cc"));
        timeLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        timeLabel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
        statsPanel.add(timeLabel);
        statsPanel.add(makeSeparator());

        List<Body> bodies = sim.getBodies();
        if (bodies == null) {
            statsPanel.revalidate();
            statsPanel.repaint();
            return;
        }

        Color[] palette = {
                Color.decode("#f5c842"), Color.decode("#4af0c4"),
                Color.decode("#c07aff"), Color.decode("#ff7a7a"),
                Color.decode("#7ac8ff"), Color.decode("#ffa07a"),
        };

        for (int i = 0; i < bodies.size(); i++) {
            Body b = bodies.get(i);
            Color c = bodyColor(b, palette, i);

            double speed = Math.sqrt(
                    b.getVelocityX() * b.getVelocityX() +
                            b.getVelocityY() * b.getVelocityY() +
                            b.getVelocityZ() * b.getVelocityZ());

            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(Color.decode("#0d0d22"));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, c),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

            card.add(statLabel(bodyName(b), c, 12, true));
            card.add(statLabel("Type:   " + b.getClass().getSimpleName(), "#8888bb", 11, false));
            card.add(statLabel(String.format("Mass:   %.3e kg", b.getMass()), "#8888bb", 11, false));
            card.add(statLabel(String.format("Pos:    (%.2e, %.2e) m", b.getX(), b.getY()), "#8888bb", 11, false));
            card.add(statLabel(String.format("Speed:  %.2f m/s", speed), "#8888bb", 11, false));

            // Star-specific fields
            if (b instanceof Star s) {
                card.add(statLabel(String.format("Temp:   %.0f K", s.getTemperature()), "#ffcc88", 11, false));
                card.add(statLabel(String.format("Class:  %s", s.getSpectralClass()), "#ffcc88", 11, false));
                card.add(statLabel(String.format("L:      %.3e W", s.getLuminosity()), "#ffcc88", 11, false));
            }

            statsPanel.add(card);
            statsPanel.add(Box.createVerticalStrut(4));
        }

        statsPanel.revalidate();
        statsPanel.repaint();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JLabel statLabel(String text, Color c, int size, boolean bold) {
        JLabel l = new JLabel(text);
        l.setForeground(c);
        l.setFont(new Font("Monospaced", bold ? Font.BOLD : Font.PLAIN, size));
        return l;
    }

    private JLabel statLabel(String text, String hex, int size, boolean bold) {
        return statLabel(text, Color.decode(hex), size, bold);
    }

    private JSeparator makeSeparator() {
        JSeparator s = new JSeparator();
        s.setForeground(Color.decode("#2a2a5a"));
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }

    private Color bodyColor(Body b, Color[] palette, int index) {
        if (b instanceof Star s) {
            return switch (s.getSpectralClass()) {
                case 'O' -> Color.decode("#aabbff"); // blue-white
                case 'B' -> Color.decode("#c0d0ff"); // blue-white
                case 'A' -> Color.decode("#f0f4ff"); // white
                case 'F' -> Color.decode("#ffffc0"); // yellow-white
                case 'G' -> Color.decode("#f5c842"); // yellow (Sol)
                case 'K' -> Color.decode("#ffaa44"); // orange
                default -> Color.decode("#ff6633"); // M: red dwarf
            };
        }
        return palette[index % palette.length];
    }

    private double computeRadius(Body b) {
        if (b.getRadius() == null || b.getRadius() <= 0)
            return 5.0;
        return Math.max(4.0, Math.min(18.0, Math.log10(b.getRadius()) * 4.0 - 20.0));
    }

    private String bodyName(Body b) {
        String n = b.getName();
        if (n != null && !n.isEmpty())
            return n;
        return b.getClass().getSimpleName() + "@" + Integer.toHexString(b.hashCode()).substring(0, 4);
    }

    // ── Static launcher ───────────────────────────────────────────────────────
    public static void launch(Simulation sim) {
        SwingUtilities.invokeLater(() -> new SimulationFX(sim));
    }
}