package pl.edu.agh.agents.controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ControllerGui extends JFrame {

    private ControllerAgent agent;
    private JTextField preferredTemperatureField;
    private JLabel currentTemperatureLabel;

    ControllerGui(ControllerAgent agent){
        this.agent = agent;

        createPreferredTemperatureField();
        createPreferredTemperatureButton();
        createCurrentTemperatureLabel();

        addWindowListener(new	WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                agent.doDelete();
            }
        } );
        setResizable(false);
    }

    public void display() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        setVisible(true);
    }

    private void createPreferredTemperatureField() {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(2, 2));
        p.add(new JLabel("Preferred temperature:"));
        preferredTemperatureField = new JTextField(15);
        p.add(preferredTemperatureField);
        getContentPane().add(p, BorderLayout.CENTER);
    }

    private void createPreferredTemperatureButton() {
        JButton addButton = new JButton("Set temperature");
        addButton.addActionListener(new SetPreferredTemperatureListener());
        JPanel p = new JPanel();
        p.add(addButton);
        getContentPane().add(p, BorderLayout.SOUTH);
    }

    private void createCurrentTemperatureLabel() {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(2, 2));
        p.add(new JLabel("Temperature: "));
        currentTemperatureLabel = new JLabel("0");
        currentTemperatureLabel.setFont(new Font("Serif", Font.BOLD, 12));
        currentTemperatureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(currentTemperatureLabel);
        getContentPane().add(p, BorderLayout.NORTH);
    }

    void setCurrentTemperatureLabelText(String text){
        this.currentTemperatureLabel.setText(text);
    }

    private class SetPreferredTemperatureListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            try {
                String sTemperature = preferredTemperatureField.getText().trim();
                int temperature = Integer.valueOf(sTemperature);
                agent.setPreferredTemperature(temperature);
            }
            catch (Exception exception) {
                JOptionPane.showMessageDialog(ControllerGui.this, "Wrong input value. " + exception.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
