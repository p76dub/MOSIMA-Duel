package sma.user;

import com.jme3.math.Vector3f;
import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import org.jpl7.Query;
import sma.AbstractAgent;

/**
 * This behaviour is designed to make the agent attack a target.
 */
public class AttackBehaviour extends OneShotBehaviour {

    private Vector3f enemyPosition;
    private String enemy;
    private Situation sit;

    public AttackBehaviour(Agent agent) {
        super(agent);
    }

    private MyAgent getMyAgent() {
        return (MyAgent) getAgent();
    }

    @Override
    public void action() {
        MyAgent agent = getMyAgent();
        this.sit = agent.getAgentSituation();
        this.enemy = sit.enemy;
        enemyPosition = agent.getEnemyLocation(enemy);

        boolean openFire = askForFirePermission();

        //System.out.println(openFire);
        if (!openFire) return;

        agent.goTo(enemyPosition);

        if (agent.isVisible(enemy, AbstractAgent.VISION_DISTANCE)){
            enemyPosition = agent.getEnemyLocation(enemy);
            agent.lookAt(enemyPosition);

            System.out.println("Enemy visible, FIRE !");
            agent.lastAction = Situation.SHOOT;
            agent.shoot(enemy);
        }

        try {
            Thread.sleep(1000);  // To compete with Dummy
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean askForFirePermission(){
        String query = "toOpenFire("
                + sit.enemyInSight +","
                + sit.impactProba+")";

        return Query.hasSolution(query);
    }
}
