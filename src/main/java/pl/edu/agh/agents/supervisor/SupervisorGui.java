package pl.edu.agh.agents.supervisor;

import jade.core.AID;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SupervisorGui extends JFrame {

    private SupervisorAgent agent;
    private JComboBox box;

    SupervisorGui(SupervisorAgent agent){
        this.agent = agent;
        createControllersBox();
        addWindowListener(new	WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                agent.doDelete();
            }
        } );
        setResizable(true);
    }

    public void display() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        setVisible(true);
    }

    private void createControllersBox() {
        box = new JComboBox();
        JLabel label1 = new JLabel("Controllers");
        JPanel panel1 = new JPanel(new BorderLayout());
        panel1.add(label1, BorderLayout.WEST);
        panel1.add(box, BorderLayout.CENTER);
        add(panel1, BorderLayout.NORTH);
    }

    public void updateControllersBox(AID[] controllers) {
        box.removeAllItems();
        for (AID controller : controllers) {
            box.addItem(controller.getName());
        }
    }

}
