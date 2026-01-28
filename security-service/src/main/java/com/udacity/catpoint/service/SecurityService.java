package com.udacity.catpoint.service;

import com.udacity.catpoint.application.StatusListener;
import com.udacity.catpoint.data.AlarmStatus;
import com.udacity.catpoint.data.ArmingStatus;
import com.udacity.catpoint.data.SecurityRepository;
import com.udacity.catpoint.data.Sensor;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

/**
 * Service that receives information about changes to the security system.
 */
public class SecurityService {

    private static final float CAT_CONFIDENCE_THRESHOLD = 0.5f;

    private final ImageService imageService;
    private final SecurityRepository securityRepository;
    private BufferedImage currentImage;
    private final Set<StatusListener> statusListeners = new HashSet<>();

    public SecurityService(SecurityRepository securityRepository, ImageService imageService) {
        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }

    /**
     * Sets the current arming status for the system.
     */
    public void setArmingStatus(ArmingStatus armingStatus) {
        securityRepository.setArmingStatus(armingStatus);

        if (armingStatus == ArmingStatus.DISARMED) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        } else {
            securityRepository.getSensors()
                    .forEach(sensor -> sensor.setActive(false));

            if (currentImage != null &&
                    imageService.imageContainsCat(currentImage, 0.5f)) {
                setAlarmStatus(AlarmStatus.ALARM);
            }
        }

        statusListeners.forEach(listener -> listener.notify(armingStatus));
    }


    /**
     * Handles alarm status changes based on cat detection.
     */
    private void catDetected(boolean cat) {
        if (cat && getArmingStatus() == ArmingStatus.ARMED_HOME) {
            setAlarmStatus(AlarmStatus.ALARM);
        } else if (!cat &&
                securityRepository.getSensors().stream().noneMatch(Sensor::getActive)) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }
        statusListeners.forEach(sl -> sl.catDetected(cat));
    }

    public void addStatusListener(StatusListener statusListener) {
        statusListeners.add(statusListener);
    }

    public void removeStatusListener(StatusListener statusListener) {
        statusListeners.remove(statusListener);
    }

    public void setAlarmStatus(AlarmStatus status) {
        securityRepository.setAlarmStatus(status);
        statusListeners.forEach(sl -> sl.notify(status));
    }

    private void handleSensorActivated() {
        if (securityRepository.getArmingStatus() == ArmingStatus.DISARMED) {
            return;
        }

        switch (securityRepository.getAlarmStatus()) {
            case NO_ALARM -> setAlarmStatus(AlarmStatus.PENDING_ALARM);
            case PENDING_ALARM -> setAlarmStatus(AlarmStatus.ALARM);
        }
    }

    private void handleSensorDeactivated() {
        switch (securityRepository.getAlarmStatus()) {
            case PENDING_ALARM -> setAlarmStatus(AlarmStatus.NO_ALARM);
        }
    }

    public void changeSensorActivationStatus(Sensor sensor, boolean active) {
        if (sensor.getActive() == active) {
            return;
        }

        sensor.setActive(active);
        securityRepository.updateSensor(sensor);

        if (securityRepository.getAlarmStatus() == AlarmStatus.ALARM) {
            return;
        }

        if (active) {
            handleSensorActivated();
        } else {
            handleSensorDeactivated();
        }
    }

    /**
     * Process an image from the camera.
     */
    public void processImage(BufferedImage image) {
        this.currentImage = image;
        boolean catDetected =
                imageService.imageContainsCat(image, CAT_CONFIDENCE_THRESHOLD);
        catDetected(catDetected);
    }

    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }

    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }
}
