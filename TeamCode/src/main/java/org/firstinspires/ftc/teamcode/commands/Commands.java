package org.firstinspires.ftc.teamcode.commands;

import org.firstinspires.ftc.teamcode.subsystems.Subsystems;

import java.util.Arrays;

public class Commands {
    public static Subsystems subsystems;
    public static DriveCommands drive = new DriveCommands();
    public static WaitCommands wait = new WaitCommands();
    public static AutonomousCommands autonomous = new AutonomousCommands();

    public Commands(Subsystems... subsystems) {
        Commands.subsystems = Arrays.stream(subsystems).findFirst().orElse(null);
    }
}
