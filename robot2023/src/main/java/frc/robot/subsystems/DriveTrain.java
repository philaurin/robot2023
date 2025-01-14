package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.sensors.WPI_PigeonIMU;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.RelativeEncoder;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Constants.DriveTrainConstants;

import static edu.wpi.first.wpilibj.PneumaticsModuleType.CTREPCM;

public class DriveTrain extends SubsystemBase {

    private final Constants.HardwareConstants hardwareConstants = Constants.currentHardwareConstants();

    private static final double GO_STRAIGHT_COMP_DEFAULT = 0.23;

    private final CANSparkMax m_leftLeader = new CANSparkMax(hardwareConstants.getLeftLeaderDeviceId(), MotorType.kBrushless);
    private final CANSparkMax m_leftFollower = new CANSparkMax(hardwareConstants.getLeftFollowerDeviceId(), MotorType.kBrushless);
    private final CANSparkMax m_rightLeader = new CANSparkMax(hardwareConstants.getRightLeaderDeviceId(), MotorType.kBrushless);
    private final CANSparkMax m_rightFollower = new CANSparkMax(hardwareConstants.getRightFollowerDeviceId(), MotorType.kBrushless);

    private final Solenoid m_shifter = new Solenoid(hardwareConstants.getPneumaticsModuleType(), hardwareConstants.getShifterSolenoidChannelId());

    private final WPI_TalonSRX m_spareTalon = new WPI_TalonSRX(hardwareConstants.getSpareTalonDeviceNumber());
    private final WPI_PigeonIMU m_gyro = new WPI_PigeonIMU(m_spareTalon);

    private final DifferentialDrive m_drive = new DifferentialDrive(m_leftLeader, m_rightLeader);
    private final RelativeEncoder m_leftLeaderEncoder = m_leftLeader.getEncoder();
    private final RelativeEncoder m_rightLeaderEncoder = m_rightLeader.getEncoder();

    public DriveTrain() {
        m_leftLeader.restoreFactoryDefaults();
        m_leftFollower.restoreFactoryDefaults();
        m_rightLeader.restoreFactoryDefaults();
        m_rightFollower.restoreFactoryDefaults();

        m_leftLeader.setIdleMode(IdleMode.kBrake);
        m_leftFollower.setIdleMode(IdleMode.kBrake);
        m_rightLeader.setIdleMode(IdleMode.kBrake);
        m_rightFollower.setIdleMode(IdleMode.kBrake);

        m_leftFollower.follow(m_leftLeader);
        m_rightFollower.follow(m_rightLeader);

        m_rightLeader.setInverted(true);

        m_leftLeaderEncoder.setPositionConversionFactor(DriveTrainConstants.kEncoderPositionConversionFactorLow);
        m_rightLeaderEncoder.setPositionConversionFactor(DriveTrainConstants.kEncoderPositionConversionFactorLow);

        m_leftLeader.burnFlash();
        m_leftFollower.burnFlash();
        m_rightLeader.burnFlash();
        m_rightFollower.burnFlash();

        m_spareTalon.configFactoryDefault();
        m_gyro.configFactoryDefault();

        m_drive.setMaxOutput(0.8);

        shiftLow();
        reset();

        //addChild("Drive", m_drive);
        //addChild("Gyro", m_gyro);
        addChild("Shifter", m_shifter);

        SmartDashboard.putNumber("DT/goStraightComp", GO_STRAIGHT_COMP_DEFAULT);
    }

    public void arcadeDrive(double xSpeed, double zRotation) {
        
        final double clampedSpeed = MathUtil.clamp(xSpeed, -1.0, 1.0);

        double comp = SmartDashboard.getNumber("DT/goStraightComp", GO_STRAIGHT_COMP_DEFAULT);
        double goStraightCompensation = clampedSpeed * comp;

        if (Math.abs(clampedSpeed) > 0.0) {
            //System.out.println("goStraightComp: " + goStraightCompensation);
        }
        m_drive.arcadeDrive(clampedSpeed, zRotation + goStraightCompensation);
    }

    public void disableSafety() {
        m_drive.setSafetyEnabled(false);
    }

    public void enableSafety() {
        m_drive.setSafetyEnabled(true);
    }

    public double getHeading() {
        return m_gyro.getAngle();
    }

    public double getPosition() {
        return (m_leftLeaderEncoder.getPosition() + m_rightLeaderEncoder.getPosition()) / 2.0;
    }

    public void reset() {
        m_leftLeaderEncoder.setPosition(0.0);
        m_rightLeaderEncoder.setPosition(0.0);

        m_gyro.reset();
    }

    public void shiftHigh() {
        m_shifter.set(true);
    }

    public void shiftLow() {
        m_shifter.set(false);
    }

    public void stop() {
        m_drive.arcadeDrive(0.0, 0.0);
    }

    @Override
    public void periodic() {
        SmartDashboard.putNumber("DT/Left Distance (m)", m_leftLeaderEncoder.getPosition());
        // SmartDashboard.putNumber("Left Speed (m/s)", m_leftLeaderEncoder.getVelocity());
        // SmartDashboard.putNumber("Left Bus Voltage", m_leftLeader.getBusVoltage());
        // SmartDashboard.putNumber("Left Current", m_leftLeader.getOutputCurrent());
        // SmartDashboard.putNumber("Left Applied Output", m_leftLeader.getAppliedOutput());
        // SmartDashboard.putNumber("Left Temperature (C)", m_leftLeader.getMotorTemperature());

        SmartDashboard.putNumber("DT/Right Distance (m)", m_rightLeaderEncoder.getPosition());
        // SmartDashboard.putNumber("Right Speed (m/s)", m_rightLeaderEncoder.getVelocity());
        // SmartDashboard.putNumber("Right Bus Voltage", m_rightLeader.getBusVoltage());
        // SmartDashboard.putNumber("Right Current", m_rightLeader.getOutputCurrent());
        // SmartDashboard.putNumber("Right Applied Output", m_rightLeader.getAppliedOutput());
        // SmartDashboard.putNumber("Right Temperature (C)", m_rightLeader.getMotorTemperature());

        SmartDashboard.putNumber("DT/Heading", getHeading());
    }
}
