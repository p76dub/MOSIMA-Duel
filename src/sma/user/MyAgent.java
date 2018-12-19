package sma.user;

import env.jme.NewEnv;
import env.jme.Situation;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import org.jpl7.Query;
import sma.actionsBehaviours.PrologBehavior;
import sma.agents.FinalAgent;

import java.util.ArrayList;

/**
 * Customized agent using prolog. It is based on FinalAgent for compatibility purpose. This agent is using an FSM rather
 * than a couple of ticker behaviours.
 * The agent is relying on several behaviours :
 * <ul>
 *     <li>A decision behaviour using prolog</li>
 *     <li>An explore behaviour</li>
 *     <li>An attack behaviour</li>
 * </ul>
 */
public class MyAgent extends FinalAgent {

    // public static final int PERIOD = 1000;
    /**
     * Some constants to represent states
     */
    private final String DECISION = "D";  // Decision state
    private final String EXPLORE = "E";   // Exploration state
    private final String ATTACK = "A";    // Attack state
    private final String DEAD = "DEAD";

    // File where reasoning rules are
    public static final String REASONING_FILE = "./ressources/prolog/duel/reasoning.pl";

    private Situation sit;

    /**
     * Setup the agent.
     */
    @Override
    protected void setup() {
        offPoints = new ArrayList<>(); // legacy support
        defPoints = new ArrayList<>(); // legacy support
        super.setup();

        // Get situation
        sit = Situation.getCurrentSituation(this);

        // Some hacking ...
        PrologBehavior.sit = sit;

        // load prolog file
        loadDecisionFile();
    }

    /**
     * Override deployment in FinalAgent (changed visibility).
     * This method create an FSM an register all states and transitions.
     */
    @Override
    protected void deployment() {
        // Deploy agent
        final Object[] args = getArguments();
        deployAgent((NewEnv) args[0], true);

        // Create FSM
        final FSMBehaviour fsm = new FSMBehaviour();

        // Register states
        fsm.registerFirstState(new DecisionBehaviour(this), DECISION);
        fsm.registerState(new ExploreBehaviour(this), EXPLORE);
        fsm.registerState(new AttackBehaviour(this), ATTACK);
        fsm.registerLastState(new OneShotBehaviour(this) { // Won't work all the time because of System.exit(0) in NewEnv
            @Override
            public void action() {
                System.out.println("I'm dead ! ;-(");
                /*Situation sit = ((MyAgent) getAgent()).getAgentSituation();
                sit.writeFinalStateToCSVFile("ressources/learningBase/" + (sit.victory ? "victory" : "defeat")
                        + "/" + System.currentTimeMillis() + ".csv"
                );*/  // Made in NewEnv instead, safer
            }
        }, DEAD);


        // Register transitions
        fsm.registerDefaultTransition(EXPLORE, DECISION);
        fsm.registerTransition(DECISION, EXPLORE, DecisionBehaviour.EXPLORE);
        fsm.registerDefaultTransition(DECISION, DECISION);
        fsm.registerDefaultTransition(ATTACK, DECISION);
        fsm.registerTransition(DECISION, ATTACK, DecisionBehaviour.ATTACK);
        fsm.registerTransition(DECISION, DEAD, DecisionBehaviour.DEAD);

        addBehaviour(fsm);
    }

    /**
     * Helper method designed to get the agent situation.
     * @return Situation
     */
    public Situation getAgentSituation() {
        sit = Situation.getCurrentSituation(this);  // If not called, situation is never updated !!!!
        PrologBehavior.sit = sit;  // legacy support
        return sit;
    }

    /**
     * Load prolog file for the decision process.
     */
    private void loadDecisionFile() {
        try {
            String prolog = "consult('" + REASONING_FILE + "')";
            if (!Query.hasSolution(prolog)) {
                System.err.println("Cannot open file " + prolog);
                System.exit(0);
            }
        } catch(Exception e) {
            System.err.println("Behaviour file for Prolog agent not found");
            System.exit(0);
        }
    }
}
