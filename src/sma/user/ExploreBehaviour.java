package sma.user;

import com.jme3.math.Vector3f;
import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import org.jpl7.JPL;
import org.jpl7.Query;
import org.jpl7.Term;
import sma.AbstractAgent;
import weka.core.Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This is the ExploreBehaviour. 80% of the time, the behaviour selects the highest point and moves the agent.
 * Otherwise, a random move is made.
 */
public class ExploreBehaviour extends SimpleBehaviour {

    // Some constants
    public static final int RANDOM_PROBABILITY = 20;
    public static final int PRECISION = 8;
    public static final int MAX_DISTANCE = 20;

    // Attributes
    private final Random random;
    private boolean isDone;
    private Vector3f computedTarget;
    private Vector3f startPosition;

    /**
     * Create a new Explore behaviour, executed by the provided agent.
     * @param a the agent
     */
    public ExploreBehaviour(Agent a) {
        super(a);
        random = new Random(System.currentTimeMillis());
        isDone = false;
    }

    @Override
    public void action() {
        MyAgent agent = getMyAgent();
        agent.lastAction = Situation.EXPLORE;

        if (computedTarget == null) { // Select the move this behaviour will make
            startPosition = new Vector3f(getMyAgent().getCurrentPosition());
            /*if (random.nextInt(100) <= RANDOM_PROBABILITY) { // Random move
                computedTarget = getRandomTarget();
            } else {  // Go to the highest position
                computedTarget = findHighestNeighbor();
            }*/
            computedTarget = getBestNeighbour();
            if (computedTarget == null) {
                computedTarget = getRandomTarget();
            }
            agent.moveTo(computedTarget);
        }

        // The behaviour runs until target is reached or max distance
        isDone = computedTarget.distance(agent.getCurrentPosition()) <= PRECISION
                || startPosition.distance(agent.getCurrentPosition()) >= MAX_DISTANCE
                || agent.getAgentSituation().enemyInSight
        ;
    }

    @Override
    public boolean done() {
        boolean done = isDone;
        if (done) {
            isDone = false;
            computedTarget = null;
        }
        return done;
    }

    // TOOLS
    private MyAgent getMyAgent() {
        return (MyAgent) getAgent();
    }

    private Vector3f getRandomTarget() {
        MyAgent agent = getMyAgent();
        ArrayList<Vector3f> points = agent.sphereCast(
                agent.getSpatial(),
                6, //AbstractAgent.NEIGHBORHOOD_DISTANCE,
                AbstractAgent.CLOSE_PRECISION,
                //AbstractAgent.VISION_ANGLE
                (float) (2*Math.PI)
        );
        return points.get(random.nextInt(points.size()));
    }

    private Vector3f findHighestNeighbor(){
        MyAgent agent = getMyAgent();
        ArrayList<Vector3f> points = agent.sphereCast(
                agent.getSpatial(),
                6, //AbstractAgent.NEIGHBORHOOD_DISTANCE,
                AbstractAgent.CLOSE_PRECISION,
                //AbstractAgent.VISION_ANGLE
                (float) (2*Math.PI)
        );

        return getHighest(points);
    }

    private Vector3f getBestNeighbour() {
        MyAgent agent = getMyAgent();
        ArrayList<Vector3f> points = agent.sphereCast(
                agent.getSpatial(),
                6, //AbstractAgent.NEIGHBORHOOD_DISTANCE,
                AbstractAgent.CLOSE_PRECISION,
                //AbstractAgent.VISION_ANGLE
                (float) (2*Math.PI)
        );
        List<Vector3f> candidates = evaluatePoints(points);
        if (candidates.size() != 0) {
            return candidates.get(new Random().nextInt(candidates.size()));
        }
        return null;
    }

    private Vector3f getHighest(ArrayList<Vector3f> points){
        float maxHeight = -256;
        Vector3f best = null;

        for(Vector3f v3 : points){
            if (v3.getY() > maxHeight){
                best = v3;
                maxHeight = v3.getY();
            }
        }
        return best;
    }

    public boolean isAGoodPosition(Vector3f position) {
        MyAgent agt = getMyAgent();
        agt.teleport(position);

        Situation sit = agt.getAgentSituation();
        Instance instance = new Instance(agt.getInstances().numAttributes());

        instance.setDataset(agt.getInstances());
        instance.setValue(0, (double) sit.averageAltitude);
        instance.setValue(1, (double) sit.maxAltitude);
        instance.setValue(2, (double) sit.currentAltitude);
        instance.setValue(3, (double) sit.fovValue);
        instance.setValue(4, sit.lastAction);
        instance.setValue(5, (double) sit.life);
        instance.setValue(6, "NOTINSIGHT"); // Not used
        // instance.setValue(6, "VICTORY"); // Not used

        try {
            return agt.eval(instance).equals("INSIGHT");
            // return agt.eval(instance).equals("VICTORY");
        } catch (Exception e) {
            return false;
        }
    }

    private List<Vector3f> evaluatePoints(List<Vector3f> list) {
        MyAgent agt = getMyAgent();
        Vector3f current = agt.getCurrentPosition();
        List<Vector3f> points = new ArrayList<>();

        for (Vector3f point : list) {
            if (new Query("goodPosition", new Term[] {JPL.newJRef(this), JPL.newJRef(point)}).hasSolution()) {
                points.add(point);
            }
        }

        agt.teleport(current);
        return points;
    }
}
