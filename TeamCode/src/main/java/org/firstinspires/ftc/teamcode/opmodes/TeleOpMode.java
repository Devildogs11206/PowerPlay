package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.modules.DriveModule;

@TeleOp(name = "Teleop")
public class TeleOpMode extends OpMode {
    @Override
    public void initialize() {
        super.initialize();

        new DriveModule(this);
    }
}
