package com.udacity.catpoint.service;

import com.udacity.catpoint.application.StatusListener;
import com.udacity.catpoint.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.awt.image.BufferedImage;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityServiceTest {

    @Mock
    private ImageService imageService;

    @Mock
    private SecurityRepository securityRepository;

    private SecurityService securityService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        securityService = new SecurityService(securityRepository, imageService);
    }

    /* ---------------- SENSOR BEHAVIOR ---------------- */

    @Test
    void armedSystem_sensorActivated_setsPendingAlarm() {
        Sensor sensor = new Sensor("Door", SensorType.DOOR);

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    void pendingAlarm_secondSensorActivation_setsAlarm() {
        Sensor sensor = new Sensor("Window", SensorType.WINDOW);

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void alarmActive_sensorChange_doesNothing() {
        Sensor sensor = new Sensor("Garage", SensorType.DOOR);

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository, never()).setAlarmStatus(any());
    }

    @Test
    void deactivatingPendingAlarmSensor_setsNoAlarm() {
        Sensor sensor = new Sensor("Window", SensorType.WINDOW);
        sensor.setActive(true);

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getSensors()).thenReturn(Set.of(sensor));

        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    /* ---------------- CAT DETECTION ---------------- */

    @Test
    void catDetectedWhileArmedHome_setsAlarm() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        BufferedImage image = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        securityService.processImage(image);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void noCatAndNoActiveSensors_setsNoAlarm() {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getSensors()).thenReturn(Set.of());
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);

        BufferedImage image = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        securityService.processImage(image);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    /* ---------------- ARMING / DISARMING ---------------- */

    @Test
    void disarmingSystem_setsNoAlarm() {
        securityService.setArmingStatus(ArmingStatus.DISARMED);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void armingSystem_resetsAllSensors() {
        Sensor sensor = new Sensor("Motion", SensorType.MOTION);
        sensor.setActive(true);

        when(securityRepository.getSensors()).thenReturn(Set.of(sensor));

        securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);

        assertFalse(sensor.getActive());
    }

    @Test
    void armingSystem_withPreviousCatImage_setsAlarm() {
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        BufferedImage image = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        securityService.processImage(image);

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    /* ---------------- STATUS LISTENERS ---------------- */

    @Test
    void removeStatusListener_stopsNotifications() {
        StatusListener listener = mock(StatusListener.class);

        securityService.addStatusListener(listener);
        securityService.removeStatusListener(listener);

        securityService.setAlarmStatus(AlarmStatus.ALARM);

        verify(listener, never()).notify(any(AlarmStatus.class));
    }

    /* ---------------- SENSOR ADD / REMOVE ---------------- */

    @Test
    void addSensor_addsSensorToRepository() {
        Sensor sensor = new Sensor("New", SensorType.DOOR);

        securityService.addSensor(sensor);

        verify(securityRepository).addSensor(sensor);
    }

    @Test
    void removeSensor_removesSensorFromRepository() {
        Sensor sensor = new Sensor("Old", SensorType.WINDOW);

        securityService.removeSensor(sensor);

        verify(securityRepository).removeSensor(sensor);
    }

    @Test
    void catDetectedWhileArmedAway_doesNotTriggerAlarm() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        securityService.processImage(new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB));

        verify(securityRepository, never()).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void armingSystem_deactivatesAllSensors() {
        Sensor s1 = new Sensor("Door", SensorType.DOOR);
        Sensor s2 = new Sensor("Window", SensorType.WINDOW);
        s1.setActive(true);
        s2.setActive(true);

        when(securityRepository.getSensors()).thenReturn(Set.of(s1, s2));

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        assertFalse(s1.getActive());
        assertFalse(s2.getActive());
    }
    @Test
    void activatingAlreadyActiveSensor_doesNothing() {
        Sensor sensor = new Sensor("Test", SensorType.DOOR);
        sensor.setActive(true);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository, never()).setAlarmStatus(any());
    }
    @Test
    void alarmActive_sensorDeactivation_doesNothing() {
        Sensor sensor = new Sensor("Test", SensorType.WINDOW);
        sensor.setActive(true);

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository, never()).setAlarmStatus(any());
    }
    @Test
    void sensorActivated_whileDisarmed_doesNothing() {
        Sensor sensor = new Sensor("Test", SensorType.MOTION);

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository, never()).setAlarmStatus(any());
    }
    @Test
    void deactivatingSensor_whenAlarmActive_doesNothing() {
        Sensor sensor = new Sensor("Test", SensorType.WINDOW);
        sensor.setActive(true);

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository, never()).setAlarmStatus(any());
    }
    @Test
    void armingSystem_withoutCatImage_doesNotTriggerAlarm() {
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(securityRepository, never()).setAlarmStatus(AlarmStatus.ALARM);
    }
    @Test
    void catDetected_whileArmedAway_doesNotTriggerAlarm() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);

        BufferedImage image = new BufferedImage(5,5,BufferedImage.TYPE_INT_RGB);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        securityService.processImage(image);

        verify(securityRepository, never()).setAlarmStatus(AlarmStatus.ALARM);
    }









    /* ---------------- PARAMETERIZED ---------------- */

    @ParameterizedTest
    @EnumSource(
            value = AlarmStatus.class,
            names = {"NO_ALARM", "ALARM"}
    )
    void deactivatingSensor_doesNotChangeAlarm_whenNotPending(AlarmStatus status) {
        Sensor sensor = new Sensor("Test", SensorType.WINDOW);

        when(securityRepository.getAlarmStatus()).thenReturn(status);

        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository, never()).setAlarmStatus(any());
    }

}
