package pl.edu.agh.agents.controller;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class ControllerAgent extends Agent {
    private ControllerGui gui;
    private int preferredTemperature;

    protected void setup() {
        preferredTemperature = 22;
        gui = new ControllerGui(this);
        gui.display();
    }

    protected void takeDown() {
        gui.dispose();
    }

    public void setPreferredTemperature(final int temperature) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                preferredTemperature = temperature;
                System.out.println("Set preferred temperature to: " + preferredTemperature);
            }
        });
    }

}
