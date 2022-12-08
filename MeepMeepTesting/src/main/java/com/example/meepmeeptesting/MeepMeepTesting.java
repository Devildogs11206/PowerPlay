package com.example.meepmeeptesting;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.noahbres.meepmeep.MeepMeep;
import com.noahbres.meepmeep.roadrunner.DefaultBotBuilder;
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity;
import com.noahbres.meepmeep.roadrunner.trajectorysequence.TrajectorySequenceBuilder;

import java.util.Arrays;

public class MeepMeepTesting {
    public static void main(String[] args) {
        MeepMeep meepMeep = new MeepMeep(800);

        RoadRunnerBotEntity myBot = new DefaultBotBuilder(meepMeep)
                // Set bot constraints: maxVel, maxAccel, maxAngVel, maxAngAccel, track width
                .setDimensions(12, 14)
                .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
                .followTrajectorySequence(drive -> {
                    Pose2d start = new Pose2d(0, 0, 0);
                    Pose2d end = getJunctionPose("Z2");
                    Pose2d[] poses = getPoses(start, end);
                    TrajectorySequenceBuilder builder = drive.trajectorySequenceBuilder(start);
                    Arrays.stream(poses).forEach(builder::lineToLinearHeading);
                    return builder.build();
                }
            );

        meepMeep.setBackground(MeepMeep.Background.FIELD_POWERPLAY_OFFICIAL)
                .setDarkMode(true)
                .setBackgroundAlpha(0.95f)
                .addEntity(myBot)
                .start();
    }

    public static double TILE_WIDTH = 23.062;

    public static Pose2d getJunctionPose(String label){
        return new Pose2d(
            -('3'- label.charAt(1)) * TILE_WIDTH,
            +('X'- label.charAt(0)) * TILE_WIDTH
        );
    }

    public static Pose2d[] getPoses(Pose2d start, Pose2d end){
        return new Pose2d[] {end};
    }
}