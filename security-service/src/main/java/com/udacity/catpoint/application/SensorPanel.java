package com.udacity.catpoint.application;

import com.udacity.catpoint.data.ArmingStatus;
import com.udacity.catpoint.data.Sensor;
import com.udacity.catpoint.data.SensorType;
import com.udacity.catpoint.service.SecurityService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Panel that allows users to add sensors to their system. Sensors may be
 * manually set to "active" and "inactive" to test the system.
 */
public class SensorPanel extends JPanel {

    private final SecurityService securityService;

    private final JLabel panelLabel = new JLabel("Sensor Management");
    private final JLabel newSensorName = new JLabel("Name:");
    private final JLabel newSensorType = new JLabel("Sensor Type:");
    private final JTextField newSensorNameField = new JTextField();
    private final JComboBox<SensorType> newSensorTypeDropdown =
            new JComboBox<>(SensorType.values());
    private final JButton addNewSensorButton = new JButton("Add New Sensor");

    private JPanel sensorListPanel;
    private JPanel newSensorPanel;

    public SensorPanel(SecurityService securityService) {
        super();
        this.securityService = securityService;
        setLayout(new MigLayout());

        // ✅ FIX: define font locally (no StyleService dependency)
        panelLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        addNewSensorButton.addActionListener(e ->
                addSensor(new Sensor(
                        newSensorNameField.getText(),
                        (SensorType) newSensorTypeDropdown.getSelectedItem()
                ))
        );

        newSensorPanel = buildAddSensorPanel();
        sensorListPanel = new JPanel(new MigLayout());

        updateSensorList(sensorListPanel);

        add(panelLabel, "wrap");
        add(newSensorPanel, "span");
        add(sensorListPanel, "span");
    }

    /**
     * Builds the panel with the form for adding a new sensor
     */
    private JPanel buildAddSensorPanel() {
        JPanel p = new JPanel(new MigLayout());
        p.add(newSensorName);
        p.add(newSensorNameField, "width 50:100:200");
        p.add(newSensorType);
        p.add(newSensorTypeDropdown, "wrap");
        p.add(addNewSensorButton, "span 3");
        return p;
    }

    /**
     * Requests the current list of sensors and updates the provided panel to display them.
     */
    private void updateSensorList(JPanel p) {
        p.removeAll();

        securityService.getSensors().stream().sorted().forEach(sensor -> {
            JLabel sensorLabel = new JLabel(
                    String.format("%s (%s): %s",
                            sensor.getName(),
                            sensor.getSensorType(),
                            sensor.getActive() ? "Active" : "Inactive")
            );

            JButton sensorToggleButton =
                    new JButton(sensor.getActive() ? "Deactivate" : "Activate");

            JButton sensorRemoveButton = new JButton("Remove Sensor");

            // ✅ Disable toggle when system is armed
            sensorToggleButton.setEnabled(
                    securityService.getArmingStatus() == ArmingStatus.DISARMED
            );

            sensorToggleButton.addActionListener(
                    e -> setSensorActivity(sensor, !sensor.getActive())
            );
            sensorRemoveButton.addActionListener(
                    e -> removeSensor(sensor)
            );

            p.add(sensorLabel, "width 300");
            p.add(sensorToggleButton, "width 100");
            p.add(sensorRemoveButton, "wrap");
        });

        repaint();
        revalidate();
    }

    private void setSensorActivity(Sensor sensor, Boolean isActive) {
        securityService.changeSensorActivationStatus(sensor, isActive);
        updateSensorList(sensorListPanel);
    }

    private void addSensor(Sensor sensor) {
        if (securityService.getSensors().size() < 4) {
            securityService.addSensor(sensor);
            updateSensorList(sensorListPanel);
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "To add more than 4 sensors, please subscribe to our Premium Membership!"
            );
        }
    }

    private void removeSensor(Sensor sensor) {
        securityService.removeSensor(sensor);
        updateSensorList(sensorListPanel);
    }
}
