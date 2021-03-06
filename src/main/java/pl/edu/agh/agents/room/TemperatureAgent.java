package pl.edu.agh.agents.room;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mw on 24/04/17.
 *
 * Class implementing IoT sensor behavior.
 *
 * @author mw
 * @see Agent
 */
public class TemperatureAgent extends Agent {

    private int temperature;
    private static final Logger logger = LoggerFactory.getLogger(TemperatureAgent.class);

    @Override
    protected void setup(){
        logger.info("Temperature agent " + getAID().getName() + " initialized.");
        addBehaviour(new OnGetTemperatureReceivingBehavior());
        addBehaviour(new OnEffectorSetTemperatureReceivingBehavior());
    }

    @Override
    protected void takeDown(){
        logger.info("Temperature agent " + getAID().getName() + " terminating.");
    }

    private int getTemperature(){
        return temperature;
    }

    private void setTemperature(int temperature){
        this.temperature = temperature;
    }

    private class OnGetTemperatureReceivingBehavior extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.MatchConversationId("temperature-measurement"));
            ACLMessage msg = myAgent.receive(mt);
            if(msg != null){
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(String.valueOf(getTemperature()));
                logger.info("Temperature is " + getTemperature() + ".");
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    private class OnEffectorSetTemperatureReceivingBehavior extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
                    MessageTemplate.MatchConversationId("temperature-modification"));
            ACLMessage msg = myAgent.receive(mt);
            if(msg != null){
                String message = msg.getContent();
                ACLMessage reply = msg.createReply();
                try {
                    Integer suggestedTemperature = Integer.parseInt(message);
                    if(suggestedTemperature != temperature) {
                        int temperatureDiff = temperature - suggestedTemperature;
                        reply.setPerformative(ACLMessage.INFORM);
                        setTemperature((temperatureDiff < 0) ? temperature + 1 : temperature - 1);
                        reply.setContent(String.valueOf(getTemperature()));
                        logger.info("Temperature was changed to " + getTemperature() + ".");
                    } else {
                        reply.setPerformative(ACLMessage.FAILURE);
                        reply.setContent("Temperature was not changed.");
                        logger.info("Temperature was not changed.");
                    }
                } catch (NumberFormatException e) {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("Suggested temperature was not a valid integer.");
                    logger.info("Suggested temperature is not a valid integer.");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }
}
