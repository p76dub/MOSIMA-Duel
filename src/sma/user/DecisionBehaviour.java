package sma.user;

import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import org.jpl7.JPL;
import org.jpl7.Query;
import org.jpl7.Term;

import java.util.ArrayList;
import java.util.Random;

/**
 * This behaviour uses prolog to decide what to do. The return code depends on the decision made.
 */
public class DecisionBehaviour extends OneShotBehaviour {

    public static final float SAVE_CURRENT_PROBA = 0.0f;

    // What this behaviour returns
    public static final int EXPLORE = 0;
    public static final int HUNT = 1;
    public static final int ATTACK = 2;
    public static final int DEAD = 3;

    /**
     * Internal enum representing available actions.
     */
    private enum Actions {
        EXPLORE(DecisionBehaviour.EXPLORE) {
            @Override
            public Query getPrologQuery(MyAgent agent) {
                return new Query(
                        "explore",
                        new Term[] {JPL.newJRef(agent)}
                );
            }
        },
        ATTACK(DecisionBehaviour.ATTACK) {
            @Override
            public Query getPrologQuery(MyAgent agent) {
                return new Query(
                        "attack",
                        new Term[] {JPL.newJRef(agent)}
                );
            }
        };

        // Return code
        private final int returnCode;

        // Constructor
        Actions(int returnCode) {
            this.returnCode = returnCode;
        }

        // Return code associated to the action
        public int getReturnCode() {
            return returnCode;
        }

        // The prolog query associated to the action
        public abstract Query getPrologQuery(MyAgent agent);

        // TOOLS

        /**
         * Helper method to create a prolog query from a list of terme.
         * @param behavior name of the prolog rule
         * @param terms a list of terms
         * @return a string, which is a valid call to prolog
         */
        private static String prologQuery(String behavior, ArrayList<Object> terms) {
            StringBuilder query = new StringBuilder(behavior + "(");
            for (Object t: terms) {
                query.append(t).append(",");
            }
            String result = query.substring(0,query.length() - 1) + ")";
            System.out.println(result);
            return result;
        }
    }

    // What the behaviour will return
    private int returnValue;

    // Is it the first run ?
    private boolean firstRun;

    // What was the previous situation
    private Situation sit;

    private final Random random;

    /**
     * Create a new DecisionBehaviour, executed by Agent a.
     * @param a the Agent
     */
    public DecisionBehaviour(Agent a) {
        super(a);
        firstRun = true;
        sit = getMyAgent().getAgentSituation();
        random = new Random(System.currentTimeMillis());
    }

    @Override
    public void action() {
        if (firstRun) {  // Required because otherwise, the agent kills dummy before hitting the ground
            firstRun = false;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
        MyAgent agent = getMyAgent();
        Situation currentSituation = agent.getAgentSituation();

        if (agent.dead || sit.victory) {
            returnValue = DEAD;
        } else {
            for (Actions action : Actions.values()) {
                currentSituation = agent.getAgentSituation(); // Will be more precise than using the previous one
                if (action.getPrologQuery(getMyAgent()).hasSolution()) {
                    returnValue = action.getReturnCode();
                }
            }
        }
        if (currentSituation.enemyInSight || sit.timeSinceLastShot != currentSituation.timeSinceLastShot
                || random.nextFloat() <= SAVE_CURRENT_PROBA) {  // Situation changed, write it
            currentSituation.writeCurrentStateToCSVFile(
                    "ressources/learningBase/states.csv"
            );
        }
        sit = currentSituation;
    }

    @Override
    public int onEnd() {
        return returnValue;
    }

    // TOOLS
    /**
     * Get the agent (casted in a MyAgent instant).
     */
    private MyAgent getMyAgent() {
        return (MyAgent) getAgent();
    }
}
