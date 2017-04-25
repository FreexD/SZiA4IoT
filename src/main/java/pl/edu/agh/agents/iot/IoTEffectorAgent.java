package pl.edu.agh.agents.iot;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.agents.room.TemperatureAgent;

/**
 * Created by mw on 24/04/17.
 *
 * Class implementing IoT device behavior.
 *
 * @author mw
 * @see Agent
 */
public class IoTEffectorAgent extends Agent {

    private int temperature;
    private static final int INTERVAL = 6000;
    private static final int BOTTOM_TEMPERATURE_LIMIT = -20;
    private static final int TOP_TEMPERATURE_LIMIT = 30;
    private static final Logger logger = LoggerFactory.getLogger(IoTEffectorAgent.class);
    private TemperatureAgent temperatureAgent;

    public IoTEffectorAgent(TemperatureAgent temperatureAgent){
        this.temperatureAgent = temperatureAgent;
    }

    @Override
    protected void setup(){
        logger.info("IoT agent " + getAID().getName() + " initialized.");
        registerEffector();
        addBehaviour(new OnSetTemperatureReceivingBehavior());
        addBehaviour(new ModifyTemperatureBehavior(this, INTERVAL));
    }

    @Override
    protected void takeDown(){
        logger.info("Temperature effector agent " + getAID().getName() + " terminating.");
    }

    private int getTemperature(){
        return temperature;
    }

    private void setTemperature(int temperature){
        this.temperature = temperature;
    }

    private void registerEffector(){
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("temperature-effector");
        sd.setName("JADE-temperature-effector");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private class OnSetTemperatureReceivingBehavior extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            ACLMessage msg = myAgent.receive(mt);
            if(msg != null){
                String message = msg.getContent();
                ACLMessage reply = msg.createReply();
                try {
                    Integer suggestedTemperature = Integer.parseInt(message);
                    if(suggestedTemperature >= BOTTOM_TEMPERATURE_LIMIT && suggestedTemperature <= TOP_TEMPERATURE_LIMIT){
                        reply.setPerformative(ACLMessage.INFORM);
                        setTemperature(suggestedTemperature);
                        reply.setContent(String.valueOf(getTemperature()));
                        logger.info("Effector temperature was changed to " + getTemperature() + ".");
                    } else {
                        reply.setPerformative(ACLMessage.FAILURE);
                        reply.setContent("Suggested effector temperature was not between " + BOTTOM_TEMPERATURE_LIMIT + " and " + TOP_TEMPERATURE_LIMIT + ".");
                        logger.info("Suggested effector temperature was not between " + BOTTOM_TEMPERATURE_LIMIT + " and " + TOP_TEMPERATURE_LIMIT + ".");
                    }
                } catch (NumberFormatException e) {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("Suggested effector temperature was not a valid integer.");
                    logger.info("Suggested effector temperature was not a valid integer.");
                }
                myAgent.send(reply);
            } else {
                block();
            }

        }
    }

    //add ticker behavior for increasing temperature
    private class ModifyTemperatureBehavior extends TickerBehaviour {

        ModifyTemperatureBehavior(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            ACLMessage modifyMsg = new ACLMessage(ACLMessage.PROPOSE);
            modifyMsg.setConversationId("temperature-modification");
            modifyMsg.setContent(String.valueOf(temperature));
            modifyMsg.addReceiver(temperatureAgent.getAID());
            myAgent.send(modifyMsg);
        }
    }
}
