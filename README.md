🏠 CatPoint Home Security System : 
A modular Java-based home security application developed as part of the Udacity Java Application Deployment Project.
The system detects security threats using sensors and camera images, triggering alarms based on system state and defined rules.
## Features
- 97%+ unit test coverage
- Live GUI with cat detection
- Arm / Disarm security system
- Sensor activation & deactivation
- Camera image processing for cat detection
- Maven multi-module build

## Run Application
```bash
mvn clean install
cd security-service
java -jar target/security-service-1.0-SNAPSHOT.jar
