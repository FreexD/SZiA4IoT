package pl.edu.agh.agents.iot;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
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
 * Class implementing IoT sensor behavior.
 *
 * @author mw
 * @see jade.core.Agent
 */
public class IoTSensorAgent extends Agent {

    private static final Logger logger = LoggerFactory.getLogger(IoTSensorAgent.class);
    private TemperatureAgent temperatureAgent;

    public IoTSensorAgent(TemperatureAgent temperatureAgent){
        this.temperatureAgent = temperatureAgent;
    }

    @Override
    protected void setup(){
        logger.info("Temperature sensor agent " + getAID().getName() + " initialized.");
        registerSensor();
        addBehaviour(new OnGetTemperatureReceivingBehavior());
    }

    @Override
    protected void takeDown(){
        logger.info("Temperature sensor agent " + getAID().getName() + " terminating.");
    }

    private void registerSensor(){
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("temperature-sensor");
        sd.setName("JADE-temperature-sensor");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private class OnGetTemperatureReceivingBehavior extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.MatchConversationId("get-temperature"));
            ACLMessage msg = myAgent.receive(mt);
            if(msg != null){
                ACLMessage reply = msg.createReply();

                ACLMessage measureMsg = new ACLMessage(ACLMessage.REQUEST);
                measureMsg.setConversationId("temperature-measurement");
                measureMsg.setContent("get");
                measureMsg.addReceiver(temperatureAgent.getAID());
                myAgent.send(measureMsg);
                addBehaviour(new OnTemperatureMeasurementReceivingBehavior(reply));
            } else {
                block();
            }
        }
    }

    private class OnTemperatureMeasurementReceivingBehavior extends OneShotBehaviour {

        private ACLMessage reply;

        public OnTemperatureMeasurementReceivingBehavior(ACLMessage reply){
            super();
            this.reply = reply;
        }

        public void action() {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("temperature-measurement"));
            ACLMessage msg = myAgent.receive(mt);
            if(msg != null){
                try {
                    Integer measuredTemperature = Integer.parseInt(msg.getContent());
                    reply.setContent(String.valueOf(measuredTemperature));
                    reply.setPerformative(ACLMessage.INFORM);
                    logger.info("Temperature is " + measuredTemperature + ".");
                } catch (NumberFormatException e) {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("Temperature measurement failed.");
                    logger.info("Temperature measurement failed.");
                }
                myAgent.send(this.reply);
            } else {
                block();
            }
        }
    }
}
