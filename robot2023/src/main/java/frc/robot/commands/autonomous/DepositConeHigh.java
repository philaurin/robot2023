package frc.robot.commands.autonomous;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.commands.autonomous.drivetrain.DriveStraight;
import frc.robot.subsystems.DriveTrain;

public class DepositConeHigh extends SequentialCommandGroup {

    public DepositConeHigh(DriveTrain driveTrain) {
        addCommands(new DriveStraight(-1.48, driveTrain).withTimeout(5.0));
    }
}
