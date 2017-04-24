package pl.edu.agh.agents.controller;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerAgent extends Agent {

    private static final Logger logger = LoggerFactory.getLogger(ControllerAgent.class);

    private ControllerGui gui;
    private int preferredTemperature;
    private int currentTemperature;
    private AID[] temperatureSensors;
    private int interval = 20000;

    protected void setup() {
        logger.info("Controller agent " + getAID().getLocalName() + " created.");
        preferredTemperature = 22;
        gui = new ControllerGui(this);
        gui.display();
        addBehaviour(new SearchSensorsBehaviour(this, interval));
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

    private class SearchSensorsBehaviour extends TickerBehaviour {

        public SearchSensorsBehaviour(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("temperature-sensor");
            template.addServices(sd);
            try {
                logger.info("Searching for temperature sensors.");
                DFAgentDescription[] result = DFService.search(myAgent, template);
                temperatureSensors = new AID[result.length];
                if(result.length == 0)
                    logger.info("Not found any temperature sensor.");
                else
                    myAgent.addBehaviour(new GetTemperatureBehaviour());
            } catch (FIPAException fe) {
                logger.warn("Error during search.");
                fe.printStackTrace();
            }
        }
    }

    private class GetTemperatureBehaviour extends OneShotBehaviour {

        private int repliesCount = 0;
        private int meanTemperature = 0;

        @Override
        public void action() {
            logger.info("Sending temperature requests to sensors.");
            /* Send request */
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            for(AID temperatureSensor : temperatureSensors) {
                msg.addReceiver(temperatureSensor);
            }
            msg.setContent("get");
            msg.setConversationId("get-temperature");
            myAgent.send(msg);

            /* Receive reply */
            MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("get-temperature");
            ACLMessage reply = myAgent.receive(messageTemplate);
            if (reply != null && reply.getPerformative() == ACLMessage.INFORM) {
                repliesCount++;
                int temp = Integer.parseInt(reply.getContent());
                meanTemperature += temp;
                if (repliesCount >= temperatureSensors.length) {
                    // received all replies
                    meanTemperature = meanTemperature / temperatureSensors.length;
                    currentTemperature = meanTemperature;
                    logger.info("Got all responses, current temperature: " + currentTemperature);
                }
            } else {
                block();
            }
        }
    }

}
