package org.firstinspires.ftc.teamcode.opmodes;

import org.firstinspires.ftc.teamcode.subsystems.DriveSubsystem;

public class AutoOpMode extends OpMode {
    @Override
    public void initialize() {
        DriveSubsystem.MAX_POWER = 1;

        super.initialize();

        schedule(
            commands.autonomous.execute()
        );
    }
}
