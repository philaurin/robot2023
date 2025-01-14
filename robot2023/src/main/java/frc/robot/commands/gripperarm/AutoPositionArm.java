// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.gripperarm;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.GripperArm;
import frc.robot.subsystems.GripperArm.VerticalArmPosition;

/**
 * An example command that uses an example subsystem.
 */
public class AutoPositionArm extends CommandBase {
    private static final double DEADBAND = 0.2;
    private static final double KP = 0.5;
    private static final double KI = 0.0;
    private static final double KD = 0.0;
    private static final double CLAMP_PID_SPEED = 0.8;
    @SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
    private final GripperArm m_gripperArm;
    private final PIDController pid = new PIDController(KP, KI, KD);
    private final ArmPosition desiredPosition;

    private boolean completedHoming = false;

    public AutoPositionArm(GripperArm gripperArm, ArmPosition desiredPosition) {
        m_gripperArm = gripperArm;
        this.desiredPosition = desiredPosition;
        // Use addRequirements() here to declare subsystem dependencies.
        addRequirements(gripperArm);
        pid.setTolerance(0.5);
    }

    // Called when the command is initially scheduled.
    @Override
    public void initialize() {
    }

    @Override
    public void execute() {
        double speed = 0.0;
        boolean vertArmInDesiredPos = isVerticalArmInDesiredPosition();
        if (!vertArmInDesiredPos) {
            completedHoming = false;
            speed = -1.0;
            if (m_gripperArm.getEncoderDistance() < desiredPosition.verticalArmPosition.minAngleEncoderValue) {
                m_gripperArm.moveVerticalArm(desiredPosition.verticalArmPosition);
            }
        } else {
            completedHoming = true;
            speed = calculatePidMovement(desiredPosition.encoderAngle);
        }

        SmartDashboard.putNumber("AutoPosArm/speed", speed);
        SmartDashboard.putNumber("AutoPosArm/setpoint", pid.getSetpoint());
        SmartDashboard.putBoolean("AutoPosArm/isAtSetpoint", pid.atSetpoint());
        SmartDashboard.putBoolean("AutoPosArm/vertArmInPos", vertArmInDesiredPos);

        m_gripperArm.moveForearm(speed);
    }

    private boolean isVerticalArmInDesiredPosition() {
        return m_gripperArm.getCurrentVerticalArmPosition() == desiredPosition.verticalArmPosition;
    }

    private double calculatePidMovement(double desiredEncoderAngle) {
        pid.setSetpoint(desiredEncoderAngle);
        double output = pid.calculate(m_gripperArm.getEncoderDistance(), desiredEncoderAngle);
        double speed = MathUtil.clamp(output, -CLAMP_PID_SPEED, CLAMP_PID_SPEED);
        return speed;
    }

    // Called once the command ends or is interrupted.
    @Override
    public void end(boolean interrupted) {
    }

    // Returns true when the command should end.
    @Override
    public boolean isFinished() {
        return completedHoming && pid.atSetpoint() && isVerticalArmInDesiredPosition();
    }

    public enum ArmPosition {
        HOME(0.0, VerticalArmPosition.REAR),
        PICK_ELEM_FLOOR(3.25, VerticalArmPosition.FORWARD),
        PICK_ELEM_STATION(15.0, VerticalArmPosition.REAR),
        PLACE_ELEM_TOP(31.5, VerticalArmPosition.FORWARD),
        PLACE_ELEM_MID(18.5, VerticalArmPosition.CENTRE);

        private final double encoderAngle;
        private final VerticalArmPosition verticalArmPosition;
        ArmPosition(double encoderAngle, VerticalArmPosition verticalArmPosition) {
            this.encoderAngle = encoderAngle;
            this.verticalArmPosition = verticalArmPosition;
        }
    }
}
