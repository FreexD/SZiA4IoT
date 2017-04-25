package pl.edu.agh.agents.iot;

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
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if(msg != null){
                String message = msg.getContent();
                ACLMessage reply = msg.createReply();
                if(message.equals("get")){
                    ACLMessage measureMsg = new ACLMessage(ACLMessage.REQUEST);
                    measureMsg.setContent("get");
                    measureMsg.addReceiver(temperatureAgent.getAID());

                    reply.setPerformative(ACLMessage.INFORM);
//                    TODO
//                    reply.setContent(String.valueOf(getTemperature()));
//                    logger.info("Temperature is " + getTemperature() + ".");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }
}
