package org.firstinspires.ftc.teamcode.commands;

import com.arcrobotics.ftclib.command.Command;

public class AutonomousCommands extends Commands {
    public Command execute() {
        return drive.input(() -> 0,() -> 0,() -> 1).withTimeout(3000).andThen(
            drive.input(() -> 0,() -> 0,() -> 0),
            wait.seconds(300)
        );
    }
}
