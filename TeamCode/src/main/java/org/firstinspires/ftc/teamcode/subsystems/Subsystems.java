package org.firstinspires.ftc.teamcode.subsystems;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Hardware;

public class Subsystems {
    public DriveSubsystem drive;

    public Subsystems(Hardware hardware, Telemetry telemetry) {
        drive = new DriveSubsystem(hardware, telemetry);
    }
}
