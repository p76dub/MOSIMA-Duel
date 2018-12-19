package princ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.jme3.app.SimpleApplication;

import env.jme.NewEnv;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import sma.agents.FinalAgent;
import sma.user.MyAgent;


public class Principal {
	
	private static String hostname = "127.0.0.1"; 
	private static HashMap<String, ContainerController> containerList=new HashMap<String, ContainerController>();// container's name - container's ref
	private static List<AgentController> agentList;// agents's ref
	private static NewEnv env;// static ref of the real environment

	public static void main(String[] args){

		//0) Create the environment
		env = NewEnv.launchRandom(64);
		//env = NewEnv.launch("PerlinMap");
		//env = Environment.launch("circleMap3");
		emptyPlatform(containerList);

		//2) create agents and add them to the platssSSSform.
		agentList=createAgents(containerList);

		//3) launch agents
		startAgents(agentList);

	}
	
	
	/**********************************************
	 * 
	 * Methods used to create an empty platform
	 * 
	 **********************************************/

	/**
	 * Create an empty platform composed of 1 main container and 3 containers.
	 * 
	 * @return a ref to the platform and update the containerList
	 */
	private static Runtime emptyPlatform(HashMap<String, ContainerController> containerList){

		Runtime rt = Runtime.instance();

		// 1) create a platform (main container+DF+AMS)
		Profile pMain = new ProfileImpl(hostname, 25565, null);
		System.out.println("Launching a main-container..."+pMain);
		AgentContainer mainContainerRef = rt.createMainContainer(pMain); //DF and AMS are include

		// 2) create the containers
		containerList.putAll(createContainers(rt));

		// 3) create monitoring agents : rma agent, used to debug and monitor the platform; sniffer agent, to monitor communications; 
		//createMonitoringAgents(mainContainerRef);

		System.out.println("Plaform ok");
		return rt;

	}

	/**
	 * Create the containers used to hold the agents 
	 * @param rt The reference to the main container
	 * @return an Hmap associating the name of a container and its object reference.
	 * 
	 * note: there is a smarter way to find a container with its name, but we go fast to the goal here. Cf jade's doc.
	 */
	private static HashMap<String,ContainerController> createContainers(Runtime rt) {
		String containerName;
		ProfileImpl pContainer;
		ContainerController containerRef;
		HashMap<String, ContainerController> containerList=new HashMap<String, ContainerController>();//bad to do it here.


		System.out.println("Launching containers ...");

		containerName="container0";
		pContainer = new ProfileImpl(null, 8888, null);
		System.out.println("Launching container "+pContainer);
		containerRef = rt.createAgentContainer(pContainer); //ContainerController replace AgentContainer in the new versions of Jade.
		containerList.put(containerName, containerRef);


		System.out.println("Launching containers done");
		return containerList;
	}


	private static List<AgentController> createAgents(HashMap<String, ContainerController> containerList) {
		System.out.println("Launching agents...");
		ContainerController c;
		String agentName;
		List<AgentController> agentList=new ArrayList<AgentController>();
		
		
		
		c = containerList.get("container0");
		agentName="Player1";
		try {
			Object[] objtab=new Object[]{env, true};//used to give informations to the agent (the behaviours to trigger)
			AgentController	ag=c.createNewAgent(agentName, MyAgent.class.getName(),objtab);
			agentList.add(ag);
			System.out.println(agentName+" launched");
			
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

		/*agentName="MyAgent";
		try {
			Object[] objtab=new Object[]{env, true};//used to give informations to the agent (the behaviours to trigger)
			AgentController	ag=c.createNewAgent(agentName, FinalAgent.class.getName(),objtab);
			agentList.add(ag);
			System.out.println(agentName+" launched");

		} catch (StaleProxyException e) {
			e.printStackTrace();
		}*/
		
		agentName="Dummy";
		try {
			Object[] objtab=new Object[]{env, false};//used to give informations to the agent
			AgentController	ag=c.createNewAgent(agentName,FinalAgent.class.getName(),objtab);
			agentList.add(ag);
			System.out.println(agentName+" launched");
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		
		System.out.println("Agents launched...");
		return agentList;
	}

	/**
	 * Start the agents
	 * @param agentList
	 */
	private static void startAgents(List<AgentController> agentList){

		System.out.println("Starting agents...");


		for(final AgentController ac: agentList){
			try {
				ac.start();
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		System.out.println("Agents started...");
	}
}
