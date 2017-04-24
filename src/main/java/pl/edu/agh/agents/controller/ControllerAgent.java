package pl.edu.agh.agents.controller;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerAgent extends Agent {

    private static final Logger logger = LoggerFactory.getLogger(ControllerAgent.class);

    private ControllerGui gui;
    private int preferredTemperature;

    protected void setup() {
        logger.info("Controller agent " + getAID().getLocalName() + " created.");
        preferredTemperature = 22;
        gui = new ControllerGui(this);
        gui.display();
    }

    protected void takeDown() {
        gui.dispose();
        logger.info("Controller agent " + getAID().getName() + " terminating.");
    }

    public void setPreferredTemperature(final int temperature) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                preferredTemperature = temperature;
                logger.info("Set preferred temperature to: " + preferredTemperature);
            }
        });
    }

}
