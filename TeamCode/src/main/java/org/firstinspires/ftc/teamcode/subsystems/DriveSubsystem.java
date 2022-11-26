package org.firstinspires.ftc.teamcode.subsystems;

import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODER;
import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.STOP_AND_RESET_ENCODER;
import static com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE;
import static com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.FORWARD;
import static com.qualcomm.robotcore.hardware.DcMotorSimple.Direction.REVERSE;

import com.acmerobotics.dashboard.config.Config;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Hardware;

@Config
public class DriveSubsystem extends HardwareSubsystem {
    public static double PULSE_PER_ROTATION = 537.7;
    public static double DISTANCE_PER_ROTATION = 3.81414599 * Math.PI;
    public static double DISTANCE_PER_PULSE = DISTANCE_PER_ROTATION / PULSE_PER_ROTATION;
    public static double MIN_POWER = 0.2;
    public static double MAX_POWER = 1.0;
    public static boolean SQUARE_INPUTS = false;
    public static double MOVE_DECELERATION = 12;
    public static double TURN_DECELERATION = 45;
    public static double TURN_TOLERANCE = 1;
    private double targetHeading = 0;

    public DriveSubsystem(Hardware hardware, Telemetry telemetry) {
        super(hardware, telemetry);

        hardware.imu.init();

        hardware.driveLeftFront.setDirection(REVERSE);
        hardware.driveLeftFront.setZeroPowerBehavior(BRAKE);
        hardware.driveRightFront.setDirection(FORWARD);
        hardware.driveRightFront.setZeroPowerBehavior(BRAKE);
        hardware.driveLeftRear.setDirection(REVERSE);
        hardware.driveLeftRear.setZeroPowerBehavior(BRAKE);
        hardware.driveRightRear.setDirection(FORWARD);
        hardware.driveRightRear.setZeroPowerBehavior(BRAKE);

        resetEncoders();
    }

    @Override
    public void periodic() {
        telemetry.addData("Drive (Heading)","%.2f deg", hardware.imu.getHeading());
        telemetry.addData("Drive (LF)","%.2f pow, %d pos, %.2f dist", hardware.driveLeftFront.getPower(), hardware.driveLeftFront.getCurrentPosition(), hardware.driveLeftFront.getCurrentPosition() * DISTANCE_PER_PULSE);
        telemetry.addData("Drive (RF)","%.2f pow, %d pos, %.2f dist", hardware.driveRightFront.getPower(), hardware.driveRightFront.getCurrentPosition(), hardware.driveRightFront.getCurrentPosition() * DISTANCE_PER_PULSE);
        telemetry.addData("Drive (LR)","%.2f pow, %d pos, %.2f dist", hardware.driveLeftRear.getPower(), hardware.driveLeftRear.getCurrentPosition(), hardware.driveLeftRear.getCurrentPosition() * DISTANCE_PER_PULSE);
        telemetry.addData("Drive (RR)","%.2f pow, %d pos, %.2f dist", hardware.driveRightRear.getPower(), hardware.driveRightRear.getCurrentPosition(), hardware.driveRightRear.getCurrentPosition() * DISTANCE_PER_PULSE);
        telemetry.update();
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
        if (SQUARE_INPUTS) {
            strafe = (strafe * strafe) * (strafe * Math.abs(strafe)) ;
            forward = (forward * forward) * (forward * Math.abs(forward));
            turn = (turn * turn) * (turn * Math.abs(turn));
        }

        // Since left stick can be pushed in all directions to control the robot's movements, its "power" must be the actual
        // distance from the center, or the hypotenuse of the right triangle formed by left_stick_x and left_stick_y
        double r = Math.hypot(strafe, forward);

        // Angle between x axis and "coordinates" of left stick
        double robotAngle = Math.atan2(forward, strafe) - Math.PI / 4;

        double lf = MAX_POWER * (r * Math.cos(robotAngle) + turn);
        double lr = MAX_POWER * (r * Math.sin(robotAngle) + turn);
        double rf = MAX_POWER * (r * Math.sin(robotAngle) - turn);
        double rr = MAX_POWER * (r * Math.cos(robotAngle) - turn);

        hardware.driveLeftFront.setPower(lf);
        hardware.driveRightFront.setPower(rf);
        hardware.driveLeftRear.setPower(lr);
        hardware.driveRightRear.setPower(rr);
    }

    public void move(double strafe, double forward, double distance) {
        move(strafe, forward, targetHeading, distance);
    }

    public void move(double strafe, double forward, double heading, double distance) {
        double deceleration = (distance - getDistance()) / MOVE_DECELERATION;
        double turn = getRemainderLeftToTurn(targetHeading = heading) / TURN_DECELERATION;
        if (strafe != 0) strafe = clamp(MIN_POWER, strafe, deceleration);
        if (forward != 0) forward = clamp(MIN_POWER, forward, deceleration);
        inputs(strafe, forward, turn);
    }

    public void turn(double power, double heading) {
        power = Math.abs(power);
        double turn = clamp(MIN_POWER, power, getRemainderLeftToTurn(targetHeading = heading) / TURN_DECELERATION * power);
        inputs(0, 0, turn);
    }

    public void stop() {
        inputs(0,0,0);
    }

    public double getDistance() {
        return (
            Math.abs(hardware.driveLeftFront.getCurrentPosition()) +
            Math.abs(hardware.driveLeftRear.getCurrentPosition()) +
            Math.abs(hardware.driveRightFront.getCurrentPosition()) +
            Math.abs(hardware.driveRightRear.getCurrentPosition())
        ) / 4d * DISTANCE_PER_PULSE;
    }

    public void setHeading() {
        targetHeading = getHeading();
    }

    public double getHeading() {
        return hardware.imu.getHeading();
    }

    public double getRemainderLeftToTurn(double heading) {
        return normalizeHeading(
            getHeading() - heading
        );
    }

    public double normalizeHeading(double heading) {
        if (heading > +180) heading -= 360;
        if (heading < -180) heading += 360;
        return heading;
    }

    public void resetEncoders() {
        hardware.driveLeftFront.setMode(STOP_AND_RESET_ENCODER);
        hardware.driveLeftFront.setMode(RUN_USING_ENCODER);
        hardware.driveRightFront.setMode(STOP_AND_RESET_ENCODER);
        hardware.driveRightFront.setMode(RUN_USING_ENCODER);
        hardware.driveLeftRear.setMode(STOP_AND_RESET_ENCODER);
        hardware.driveLeftRear.setMode(RUN_USING_ENCODER);
        hardware.driveRightRear.setMode(STOP_AND_RESET_ENCODER);
        hardware.driveRightRear.setMode(RUN_USING_ENCODER);
    }

    private double clamp(double min, double max, double value) {
        double sign = min < 0 || max < 0 || value < 0 ? -1 : 1;
        double result = Math.min(Math.abs(max), Math.max(Math.abs(min), Math.abs(value)));
        return sign * result;
    }
}
