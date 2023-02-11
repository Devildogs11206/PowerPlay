package org.firstinspires.ftc.teamcode.commands;

import static org.firstinspires.ftc.teamcode.game.Alliance.RED;
import static org.firstinspires.ftc.teamcode.game.Config.config;
import static org.firstinspires.ftc.teamcode.game.Side.NORTH;
import static org.firstinspires.ftc.teamcode.game.Side.SOUTH;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.RunCommand;
import com.arcrobotics.ftclib.command.SelectCommand;

import org.firstinspires.ftc.teamcode.game.Alliance;
import org.firstinspires.ftc.teamcode.game.Junction;
import org.firstinspires.ftc.teamcode.game.Side;
import org.firstinspires.ftc.teamcode.hacks.Offsets;

import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "unchecked"})
public class DriveCommands extends Commands {
    public static final double INTAKE_OFFSET = -4.75;
    public static final double IS_BUSY_OFFSET = -0.25;

    public Command setDrivePower(double power) {
        return new InstantCommand(() -> subsystems.drive.power = power, subsystems.drive);
    }

    public Command input(DoubleSupplier strafe, DoubleSupplier forward, DoubleSupplier turn) {
        return new RunCommand(
            () -> subsystems.drive.inputs(
                strafe.getAsDouble(),
                forward.getAsDouble(),
                turn.getAsDouble()
            ), subsystems.drive
        );
    }

    public Command strafe(double distance) {
        return complete(() -> subsystems.drive.strafe(distance));
    }

    public Command forward(double distance) {
        return complete(() -> subsystems.drive.forward(distance));
    }

    public Command turn(double heading) {
        return complete(() -> subsystems.drive.turn(heading));
    }

    public Command toPose(Pose2d pose, Consumer<Offsets>... consumers) {
        return complete(
            () -> subsystems.drive.to(
                subsystems.nav.getTransitionPoses(
                    subsystems.drive.getPose(),
                    pose, consumers
                )
            )
        );
    }

    public Command toTile(Supplier<String> supplier) {
        return drive.toTile(supplier.get());
    }

    public Command toTile(String label) {
        Pose2d pose = subsystems.nav.getTilePose(label);
        return drive.toPose(pose);
    }

    public Command toTile(String label, Consumer<Offsets>... consumers) {
        return drive.toPose(
            subsystems.nav.getTilePose(label),
            consumers
        );
    }

    public Command toJunction() {
        return new SelectCommand(
            () -> toJunction(config.junction)
        );
    }

    public Command toJunctionAuto(String label) {
        return toJunction(label, o -> o.startTileX = -4);
    }

    public Command toJunction(String label, Consumer<Offsets>... consumers) {
        return drive.toPose(
            subsystems.nav.getJunctionPose(label),
            o -> o.endX = o.endY = INTAKE_OFFSET,
            o -> o.set(consumers)
        ).alongWith(
            lift.toJunction(Junction.get(label))
        ).andThen(
            drive.setDrivePower(0.25)
        );
    }

    public Command toStack() {
        return toStack(config.alliance, config.side, o -> o.endTileX = +5.25);
    }

    public Command toStackAuto() {
        return toStack(config.alliance, config.side, o -> o.startTileX = -4);
    }

    public Command toStackRight() {
        return toStack(config.alliance, config.alliance == RED ? NORTH : SOUTH);
    }

    public Command toStackLeft() {
        return toStack(config.alliance, config.alliance == RED ? SOUTH : NORTH);
    }

    public Command toStack(Alliance alliance, Side side, Consumer<Offsets>... consumers) {
        return new SelectCommand(
            () -> drive.toPose(
                subsystems.nav.getStackPose(alliance, side),
                o -> o.endX = o.endY = INTAKE_OFFSET,
                o -> o.set(consumers)
            ).alongWith(
                lift.toIntake(0)
            )
        );
    }

    public Command toSubstation() {
        return new SelectCommand(
            () -> drive.toPose(
                subsystems.nav.getSubstationPose(config.alliance, config.side),
                o -> o.endX = o.endY = INTAKE_OFFSET
            ).alongWith(
                lift.toIntake(0)
            )
        );
    }

    public Command toTerminal(Alliance alliance, Side side) {
        return drive.toPose(
            subsystems.nav.getTerminalPose(alliance, side),
            o -> o.endX = o.endY = INTAKE_OFFSET
        ).alongWith(
            lift.toIntake(0)
        );
    }

    private Command complete(Runnable runnable) {
        return new InstantCommand(runnable, subsystems.drive).andThen(
            wait.until(() -> !subsystems.drive.isBusy(IS_BUSY_OFFSET))
        );
    }
}
