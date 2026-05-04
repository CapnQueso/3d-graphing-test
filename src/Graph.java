import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.ColorCube;
import javax.media.j3d.*;
import javax.vecmath.*;

import org.jogamp.java3d.Alpha;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.RotationInterpolator;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.universe.SimpleUniverse;

/**
 * Multi-body simulation
 * using graphs to make it look pretty
 * @author capnqueso
 */
public class Graph extends Applet {

    public Graph() {
        setLayout(new BorderLayout());

        // Step 1: Create Canvas3D 
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas3D = new Canvas3D(config);
        add("Center", canvas3D);

        // Step 2: Create SimpleUniverse [cite: 701, 707]
        SimpleUniverse simpleU = new SimpleUniverse(canvas3D);

        // Step 3 & 4: Construct and Compile Content Branch 
        BranchGroup scene = createSceneGraph();
        scene.compile();

        // Move the camera back so we can see the donut [cite: 237]
        simpleU.getViewingPlatform().setNominalViewingTransform();

        // Step 5: Add to Universe 
        simpleU.addBranchGraph(scene);
    }

    public BranchGroup createSceneGraph() {
        BranchGroup objRoot = new BranchGroup();

        // Create a transform group for rotation (so the donut spins) [cite: 402]
        TransformGroup objSpin = new TransformGroup();
        objSpin.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objRoot.addChild(objSpin);

        // ADD THE DONUT HERE
        // If your library has it: objSpin.addChild(new Torus(0.4f, 0.2f));
        // Otherwise, the tutorial's standard "Hello World" uses ColorCube:
        objSpin.addChild(new ColorCube(0.4));

        // Animation: Rotate the object [cite: 420, 422]
        Alpha rotationAlpha = new Alpha(-1, 4000);
        RotationInterpolator rotator = new RotationInterpolator(rotationAlpha, objSpin);
        BoundingSphere bounds = new BoundingSphere();
        rotator.setSchedulingBounds(bounds);
        objSpin.addChild(rotator);

        return objRoot;
    }

    public static void main(String[] args) {
        new MainFrame(new Graph(), 500, 500); // [cite: 256]
    }
}
