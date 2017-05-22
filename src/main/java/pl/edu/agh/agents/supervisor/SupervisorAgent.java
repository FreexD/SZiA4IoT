package pl.edu.agh.agents.supervisor;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SupervisorAgent extends Agent{

    private static final Logger logger = LoggerFactory.getLogger(SupervisorAgent.class);

    private SupervisorGui gui;
    private int interval = 6000;
    private AID[] controllers;


    protected void setup() {
        logger.info("Supervisor agent " + getAID().getLocalName() + " created.");
        gui = new SupervisorGui(this);
        gui.display();
        addBehaviour(new SearchControllersBehaviour(this, interval));
    }

    @Override
    protected void takeDown(){
        gui.dispose();
        logger.info("Supervisor agent " + getAID().getName() + " terminating.");
    }

    public void saveControllerParameters(String name, boolean enabled, int min, int max){
        //TODO implement
        logger.info("Setting " + name + " to " + enabled + " " + min + " " + max);
    }

    private class SearchControllersBehaviour extends TickerBehaviour {

        SearchControllersBehaviour(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("controller");
            template.addServices(sd);
            try {
                logger.info("Searching for controllers.");
                DFAgentDescription[] result = DFService.search(myAgent, template);
                controllers = new AID[result.length];
                for (int i = 0; i < result.length; ++i) {
                    controllers[i] = result[i].getName();
                }
                if(result.length == 0)
                    logger.info("Not found any controller.");
                else {
                    logger.info("Found " + result.length + " controllers.");
                    gui.updateControllersBox(controllers);
                }
            } catch (FIPAException fe) {
                logger.warn("Error during search.");
                fe.printStackTrace();
            }
        }
    }

}
