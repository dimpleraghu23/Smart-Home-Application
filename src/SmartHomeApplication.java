import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

// Device interface
interface Device {
    void turnOn();
    void turnOff();
    String getStatus();
    int getId();
    String getType();
}

// Light class
class Light implements Device {
    private boolean isOn = false;
    private int id;

    public Light(int id) {
        this.id = id;
    }

    @Override
    public void turnOn() {
        isOn = true;
    }

    @Override
    public void turnOff() {
        isOn = false;
    }

    @Override
    public String getStatus() {
        return "Light " + id + " is " + (isOn ? "On" : "Off");
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getType() {
        return "light";
    }
}

// Thermostat class
class Thermostat implements Device {
    private boolean isOn = false;
    private int temperature = 70; // Default temperature
    private int id;

    public Thermostat(int id) {
        this.id = id;
    }

    @Override
    public void turnOn() {
        isOn = true;
        // Set a random temperature between 60 and 80
        temperature = 60 + new Random().nextInt(21);
    }

    @Override
    public void turnOff() {
        isOn = false;
    }

    @Override
    public String getStatus() {
        return "Thermostat " + id + " is " + (isOn ? "On, set to " + temperature + " degrees" : "Off");
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getType() {
        return "thermostat";
    }
}

// DoorLock class
class DoorLock implements Device {
    private boolean isLocked = true;
    private int id;

    public DoorLock(int id) {
        this.id = id;
    }

    @Override
    public void turnOn() {
        isLocked = true;
    }

    @Override
    public void turnOff() {
        isLocked = false;
    }

    @Override
    public String getStatus() {
        return "Door " + id + " is " + (isLocked ? "Locked" : "Unlocked");
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getType() {
        return "door";
    }
}

// Device Factory
class DeviceFactory {
    public static Device createDevice(String type, int id) {
        switch (type) {
            case "light":
                return new Light(id);
            case "thermostat":
                return new Thermostat(id);
            case "door":
                return new DoorLock(id);
            default:
                throw new IllegalArgumentException("Unknown device type");
        }
    }
}

// Scheduler and Trigger Classes
class ScheduledTask {
    int deviceId;
    LocalTime time;
    String command;

    public ScheduledTask(int deviceId, LocalTime time, String command) {
        this.deviceId = deviceId;
        this.time = time;
        this.command = command;
    }
}

class Trigger {
    String condition;
    String action;

    public Trigger(String condition, String action) {
        this.condition = condition;
        this.action = action;
    }
}

// CentralHub class
class CentralHub {
    private List<Device> devices = new ArrayList<>();
    private List<ScheduledTask> scheduledTasks = new ArrayList<>();
    private List<Trigger> triggers = new ArrayList<>();

    public void addDevice(Device device) {
        devices.add(device);
    }

    public Device getDevice(int id) {
        for (Device device : devices) {
            if (device.getId() == id) {
                return device;
            }
        }
        return null;
    }

    public void addScheduledTask(int deviceId, LocalTime time, String command) {
        scheduledTasks.add(new ScheduledTask(deviceId, time, command));
    }

    public void addTrigger(String condition, String action) {
        triggers.add(new Trigger(condition, action));
    }

    public void showDeviceStatus() {
        for (Device device : devices) {
            System.out.println(device.getStatus());
        }
    }

    public void executeScheduledTasks(LocalTime currentTime) {
        for (ScheduledTask task : scheduledTasks) {
            if (task.time.equals(currentTime)) {
                Device device = getDevice(task.deviceId);
                if (device != null) {
                    if (task.command.equals("Turn On")) {
                        device.turnOn();
                    } else if (task.command.equals("Turn Off")) {
                        device.turnOff();
                    }
                }
            }
        }
    }

    public void checkTriggers(int currentTemperature) {
        for (Trigger trigger : triggers) {
            String[] parts = trigger.condition.split(" ");
            String conditionType = parts[0];
            int triggerValue = Integer.parseInt(parts[2]);
            if (conditionType.equals("temperature") && currentTemperature > triggerValue) {
                Device device = getDevice(Integer.parseInt(trigger.action.replace("turnOff(", "").replace(")", "")));
                if (device != null) {
                    device.turnOff();
                }
            }
        }
    }
}

// Main class
public class SmartHomeApplication {

    public static void main(String[] args) {
        CentralHub hub = new CentralHub();

        // Create and add devices
        Device light = DeviceFactory.createDevice("light", 1);
        Device thermostat = DeviceFactory.createDevice("thermostat", 2);
        Device doorLock = DeviceFactory.createDevice("door", 3);

        hub.addDevice(light);
        hub.addDevice(thermostat);
        hub.addDevice(doorLock);

        // Add a schedule and trigger
        hub.addScheduledTask(2, LocalTime.of(6, 0), "Turn On");
        hub.addTrigger("temperature > 75", "turnOff(1)");

        // Simulate user commands
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Enter the device ID (1 for light, 2 for thermostat, 3 for door) or type 'exit' to quit:");
            String deviceInput = scanner.nextLine();

            if ("exit".equalsIgnoreCase(deviceInput)) {
                break;
            }

            int deviceId;
            try {
                deviceId = Integer.parseInt(deviceInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid device ID. Please try again.");
                continue;
            }

            Device selectedDevice = hub.getDevice(deviceId);

            if (selectedDevice == null) {
                System.out.println("Device not found. Please try again.");
                continue;
            }

            if (selectedDevice instanceof Thermostat) {
                System.out.println("Enter a command for thermostat (turnOn, turnOff, status):");
            } else if (selectedDevice instanceof DoorLock) {
                System.out.println("Enter a command for door (lock, unlock, status):");
            } else {
                System.out.println("Enter a command (turnOn, turnOff, status):");
            }

            String command = scanner.nextLine();

            switch (command.toLowerCase()) {
                case "turnon":
                    selectedDevice.turnOn();
                    break;
                case "turnoff":
                    selectedDevice.turnOff();
                    break;
                case "status":
                    System.out.println(selectedDevice.getStatus());
                    break;
                case "lock":
                    if (selectedDevice instanceof DoorLock) {
                        selectedDevice.turnOn(); // Lock door
                        System.out.println("Door locked.");
                    } else {
                        System.out.println("Invalid command for this device.");
                    }
                    break;
                case "unlock":
                    if (selectedDevice instanceof DoorLock) {
                        selectedDevice.turnOff(); // Unlock door
                        System.out.println("Door unlocked.");
                    } else {
                        System.out.println("Invalid command for this device.");
                    }
                    break;
                default:
                    System.out.println("Invalid command. Try again.");
            }

            // For demonstration, we assume the current time and temperature
            LocalTime currentTime = LocalTime.now();
            int currentTemperature = 76; // Example temperature

            hub.executeScheduledTasks(currentTime);
            hub.checkTriggers(currentTemperature);
            hub.showDeviceStatus();
        }

        scanner.close();
    }
}
