package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.TrajectoryBuilder;
import com.arcrobotics.ftclib.drivebase.MecanumDrive;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.qualcomm.hardware.bosch.BNO055IMU;

import org.firstinspires.ftc.robotcore.external.Consumer;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Hardware;
import org.firstinspires.ftc.teamcode.hacks.Odometry;

@Config
public class DriveSubsystem extends HardwareSubsystem {
    public static double PULSE_PER_ROTATION = 537.7;
    public static double DISTANCE_PER_ROTATION = 3.78 * Math.PI;
    public static double DISTANCE_PER_PULSE = DISTANCE_PER_ROTATION / PULSE_PER_ROTATION;
    public static double MIN_POWER = 0.2;
    public static double MAX_POWER = 1.0;
    public static Motor.RunMode RUN_MODE = Motor.RunMode.VelocityControl;
    public static boolean DRIVE_FIELD_CENTRIC = false;
    public static boolean SQUARE_INPUTS = false;
    public static boolean AUTO_INVERT = false;
    private double targetHeading = 0;

    private final MecanumDrive drive;
    private final Odometry odometry;

    public DriveSubsystem(Hardware hardware, Telemetry telemetry) {
        super(hardware, telemetry);

        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.RADIANS;
        hardware.imu.initialize(parameters);

        hardware.driveLeftFront.setInverted(true);
        hardware.driveLeftRear.setInverted(true);

        hardware.drive.setRunMode(RUN_MODE);
        hardware.drive.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        hardware.drive.setDistancePerPulse(DISTANCE_PER_PULSE);
        hardware.drive.resetEncoder();

        drive = new MecanumDrive(
            AUTO_INVERT,
            hardware.driveLeftFront,
            hardware.driveLeftRear,
            hardware.driveRightRear,
            hardware.driveRightFront
        );

        odometry = new Odometry(hardware, telemetry);
    }

    @Override
    public void periodic() {
        telemetry.addData("Drive (Heading)", "%.2f deg", getHeading());
        telemetry.addData("Drive (LF)", "%.2f pow, %d pos, %.2f dist", hardware.driveLeftFront.motor.getPower(), hardware.driveLeftFront.getCurrentPosition(), hardware.driveLeftFront.getCurrentPosition() * DISTANCE_PER_PULSE);
        telemetry.addData("Drive (RF)", "%.2f pow, %d pos, %.2f dist", hardware.driveRightFront.motor.getPower(), hardware.driveRightFront.getCurrentPosition(), hardware.driveRightFront.getCurrentPosition() * DISTANCE_PER_PULSE);
        telemetry.addData("Drive (LR)", "%.2f pow, %d pos, %.2f dist", hardware.driveLeftRear.motor.getPower(), hardware.driveLeftRear.getCurrentPosition(), hardware.driveLeftRear.getCurrentPosition() * DISTANCE_PER_PULSE);
        telemetry.addData("Drive (RR)", "%.2f pow, %d pos, %.2f dist", hardware.driveRightRear.motor.getPower(), hardware.driveRightRear.getCurrentPosition(), hardware.driveRightRear.getCurrentPosition() * DISTANCE_PER_PULSE);
    }

    public enum DrivePower {
        LOW(0.25), MEDIUM(0.5), HIGH(1);

        public final double power;

        DrivePower(double power) {
            this.power = power;
        }
    }

    public void setDrivePower(DrivePower drivePower) {
        MAX_POWER = drivePower.power;
    }

    public void inputs(double strafe, double forward, double turn) {
        if (DRIVE_FIELD_CENTRIC) drive.driveFieldCentric(strafe, forward, turn, getHeading(), SQUARE_INPUTS);
        else drive.driveRobotCentric(strafe, forward, turn, SQUARE_INPUTS);
    }

    public void move(double strafe, double forward, double distance) {
        if (strafe != 0) strafe(distance);
        if (forward != 0) forward(distance);
    }

    public void strafe(double distance) {
        followTrajectoryAsync(
            builder -> {
                if (distance < 0) builder.strafeLeft(-distance);
                else builder.strafeRight(distance);
            }
        );
    }

    public void forward(double distance) {
        followTrajectoryAsync(
            builder -> {
                if (distance < 0) builder.back(-distance);
                else builder.forward(distance);
            }
        );
    }

    public void turn(double power, double heading) {
        odometry.turnAsync(
            Math.toRadians(heading)
        );
    }

    public void setHeading() {
        targetHeading = getHeading();
    }

    public double getHeading() {
        return Math.toDegrees(hardware.imu.getAngularOrientation().firstAngle);
    }

    public boolean isBusy() {
        return odometry.isBusy();
    }

    private void followTrajectoryAsync(Consumer<TrajectoryBuilder> consumer) {
        Pose2d pose = odometry.getPoseEstimate();
        TrajectoryBuilder builder = odometry.trajectoryBuilder(pose);
        consumer.accept(builder);
        odometry.followTrajectoryAsync(builder.build());
    }
}