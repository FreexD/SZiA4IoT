package pl.edu.agh.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mw on 24/04/17.
 *
 * Class implementing IoT device behavior.
 *
 * @author mw
 * @see jade.core.Agent
 */
public class IoTAgent extends Agent {

    private int temperature;
    private static final int BOTTOM_TEMPERATURE_LIMIT = -20;
    private static final int TOP_TEMPERATURE_LIMIT = 30;
    private static final Logger logger = LoggerFactory.getLogger(IoTAgent.class);

    /**
     * Run on agent initialization.
     */
    @Override
    protected void setup(){
        logger.info("IoT agent " + getAID().getName() + " initialized.");
        addBehaviour(new IoTReceivingBehavior());
    }

    /**
     * Run on agent termination. Do cleanup stuff here.
     */
    @Override
    protected void takeDown(){
        logger.info("IoT agent " + getAID().getName() + " terminating.");
    }

    private int getTemperature(){
        return temperature;
    }

    private void setTemperature(int temperature){
        this.temperature = temperature;
    }

    /**
     * Cyclic receiving behavior for IoT device.
     *
     * @see jade.core.behaviours.CyclicBehaviour
     * @see jade.lang.acl.MessageTemplate
     * @see jade.lang.acl.ACLMessage
     */
    private class IoTReceivingBehavior extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if(msg != null){
                String message = msg.getContent();
                ACLMessage reply = msg.createReply();

                if(message.equals("get")){
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(String.valueOf(getTemperature()));
                    logger.info("Temperature is " + getTemperature() + ".");
                } else {
                    try {
                        Integer suggestedTemperature = Integer.parseInt(message);
                        if(suggestedTemperature >= BOTTOM_TEMPERATURE_LIMIT && suggestedTemperature <= TOP_TEMPERATURE_LIMIT){
                            reply.setPerformative(ACLMessage.INFORM);
                            setTemperature(suggestedTemperature);
                            reply.setContent(String.valueOf(getTemperature()));
                            logger.info("Temperature was set to " + getTemperature() + ".");
                        } else {
                            reply.setPerformative(ACLMessage.FAILURE);
                            reply.setContent("Temperature is not between " + BOTTOM_TEMPERATURE_LIMIT + " and " + TOP_TEMPERATURE_LIMIT + ".");
                            logger.info("Temperature is not between " + BOTTOM_TEMPERATURE_LIMIT + " and " + TOP_TEMPERATURE_LIMIT + ".");
                        }
                    } catch (NumberFormatException e) {
                        reply.setPerformative(ACLMessage.FAILURE);
                        reply.setContent("Temperature is not a valid integer.");
                        logger.info("Temperature is not a valid integer.");
                    }
                }
                myAgent.send(reply);
            } else {
                block();
            }

        }
    }
}
