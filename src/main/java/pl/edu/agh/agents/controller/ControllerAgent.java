package pl.edu.agh.agents.controller;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
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
    private AID[] temperatureEffectors;
    private int interval = 10000;

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
                for (int i = 0; i < result.length; ++i) {
                    temperatureSensors[i] = result[i].getName();
                }
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

        @Override
        public void action() {
            logger.info("Sending temperature requests to sensors.");
            /* Send request */
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            for(AID temperatureSensor : temperatureSensors) {
                msg.addReceiver(temperatureSensor);
                logger.info("Sent temperature request to: " + temperatureSensor.getLocalName());
            }
            msg.setContent("get");
            msg.setConversationId("get-temperature");
            myAgent.send(msg);
            myAgent.addBehaviour(new ReceiveTemperatureBehaviour());
        }

    }

    private class ReceiveTemperatureBehaviour extends Behaviour {

        private int repliesCount = 0;
        private int temperatureSum = 0;

        @Override
        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("get-temperature");
            ACLMessage reply = myAgent.receive(messageTemplate);
            if (reply != null && reply.getPerformative() == ACLMessage.INFORM) {
                repliesCount++;
                int temp = Integer.parseInt(reply.getContent());
                temperatureSum += temp;
            } else {
                block();
            }
        }

        @Override
        public boolean done() {
            if (repliesCount >= temperatureSensors.length) {
                temperatureSum = temperatureSum / temperatureSensors.length;
                currentTemperature = temperatureSum;
                logger.info("Got all responses, current temperature: " + currentTemperature);
                myAgent.addBehaviour(new SearchEffectorsBehaviour());
                return true;
            } else {
                return false;
            }
        }
    }

    private class SearchEffectorsBehaviour extends OneShotBehaviour {

        @Override
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("temperature-effector");
            template.addServices(sd);
            try {
                logger.info("Searching for temperature effectors.");
                DFAgentDescription[] result = DFService.search(myAgent, template);
                temperatureEffectors = new AID[result.length];
                for (int i = 0; i < result.length; ++i) {
                    temperatureEffectors[i] = result[i].getName();
                }
                if(result.length == 0)
                    logger.info("Not found any temperature effector.");
                else
                    myAgent.addBehaviour(new SendPreferredTemperatureToEffectorBehaviour());
            } catch (FIPAException fe) {
                logger.warn("Error during search.");
                fe.printStackTrace();
            }
        }
    }

    private class SendPreferredTemperatureToEffectorBehaviour extends OneShotBehaviour {

        @Override
        public void action() {
            logger.info("Sending preferred temperature to effectors.");
            /* Send request */
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            for(AID temperatureEffector : temperatureEffectors) {
                msg.addReceiver(temperatureEffector);
                logger.info("Sent preferred temperature to: " + temperatureEffector.getLocalName());
            }
            msg.setContent(String.valueOf(preferredTemperature));
            myAgent.send(msg);
        }
    }

}
