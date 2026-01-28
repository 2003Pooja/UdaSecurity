package com.udacity.catpoint.application;

import com.udacity.catpoint.data.AlarmStatus;
import com.udacity.catpoint.data.ArmingStatus;

public interface StatusListener {

    void notify(AlarmStatus alarmStatus);

    void notify(ArmingStatus armingStatus);

    void sensorStatusChanged();

    void catDetected(boolean catDetected);
}
