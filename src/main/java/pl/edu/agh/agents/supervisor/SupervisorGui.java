package pl.edu.agh.agents.supervisor;

import jade.core.AID;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SupervisorGui extends JFrame {

    private SupervisorAgent agent;
    private JComboBox<String> box;
    private JTextField maxTemperatureField;
    private JTextField minTemperatureField;
    private JLabel currentTemperatureLabel;

    SupervisorGui(SupervisorAgent agent){
        this.agent = agent;
        createControllersBox();
        createControllerOptions();
        createSaveButton();
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
        box = new JComboBox<String>();
        JLabel label1 = new JLabel("Controllers ");
        JPanel panel1 = new JPanel(new BorderLayout());
        panel1.add(label1, BorderLayout.WEST);
        panel1.add(box, BorderLayout.CENTER);
        add(panel1, BorderLayout.NORTH);
    }

    private void createControllerOptions() {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(4, 2));

        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new GridLayout(1, 2));
        p.add(new JLabel("Status:"));
        ButtonGroup group = new ButtonGroup();

        JRadioButton enabledButton = new JRadioButton("enabled");
        enabledButton.setActionCommand("enabled");
        enabledButton.setSelected(true);
        group.add(enabledButton);
        radioPanel.add(enabledButton);

        JRadioButton disabledButton = new JRadioButton("disabled");
        disabledButton.setActionCommand("disabled");
        disabledButton.setSelected(false);
        group.add(disabledButton);
        radioPanel.add(disabledButton);

        p.add(radioPanel);

        p.add(new JLabel("Temperature: "));
        currentTemperatureLabel = new JLabel("0");
        p.add(currentTemperatureLabel);


        p.add(new JLabel("Minimum temperature:"));
        minTemperatureField = new JTextField(15);
        p.add(minTemperatureField);

        p.add(new JLabel("Maximum temperature:"));
        maxTemperatureField = new JTextField(15);
        p.add(maxTemperatureField);

        getContentPane().add(p, BorderLayout.CENTER);
    }

    private void createSaveButton(){
        JButton addButton = new JButton("Save");
        //addButton.addActionListener(new SetPreferredTemperatureListener());
        JPanel p = new JPanel();
        p.add(addButton);
        getContentPane().add(p, BorderLayout.SOUTH);
    }

    public void updateControllersBox(AID[] controllers) {
        box.removeAllItems();
        for (AID controller : controllers) {
            box.addItem(controller.getName());
        }
    }





}
