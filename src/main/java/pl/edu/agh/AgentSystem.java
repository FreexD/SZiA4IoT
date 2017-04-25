package pl.edu.agh;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import pl.edu.agh.agents.IoTAgent;

import jade.util.leap.Properties;
import pl.edu.agh.agents.controller.ControllerAgent;

/**
 * Created by mw on 24/04/17.
 *
 * Class used to create agents and run the whole application.
 *
 * @author mw
 * @see jade.wrapper.AgentContainer
 */
public class AgentSystem {

    public static void main(String[] args) {
        Properties pp = new Properties();
        pp.setProperty(Profile.GUI, Boolean.TRUE.toString());
        Profile p = new ProfileImpl(pp);
        AgentContainer ac = jade.core.Runtime.instance().createMainContainer(p);
        try {
            ac.acceptNewAgent("iot1", new IoTAgent()).start();
            ac.acceptNewAgent("iot2", new IoTAgent()).start();
            ac.acceptNewAgent("controller", new ControllerAgent()).start();
        } catch (StaleProxyException e) {
            throw new Error(e);
        }
    }
}
