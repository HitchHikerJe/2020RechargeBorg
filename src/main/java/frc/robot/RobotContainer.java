/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import com.analog.adis16448.frc.ADIS16448_IMU;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.commands.*;
import frc.robot.commands.autoCommands.*;
import frc.robot.subsystems.*;
import edu.wpi.first.wpilibj2.command.*;

/**
 * This class is where the bulk of the robot should be declared.  Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls).  Instead, the structure of the robot
 * (including subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  //subsystems
  private final DriveTrain driveTrain = new DriveTrain();
  private final Collector collector = new Collector();
  private final Shooter shooter = new Shooter();
  private final Stats stats = new Stats();

  //Unfinished subsystems -> need testing
  private final Hopper hopper = new Hopper();
  private final Climber climber = new Climber();

  //controllers
  XboxController xbox = new XboxController(0);
  Joystick flight = new Joystick(1);

  //commands -> use these to reconfigure button bindings later
  //button bindings use lambda expressions: look them up if you are confused
  //check WPILIB for methods to get different types of input and Driver Station to get port numbers
  private final Drive drive = new Drive(driveTrain, () -> xbox.getRawAxis(1), () -> xbox.getRawAxis(4));
  
  //The button bindings for these can be changed here
  private final Collect collect = new Collect(collector, () -> xbox.getAButton());              //currently runs with the A button
  private final Shoot shoot = new Shoot(shooter, () -> flight.getRawButton(1));          //currently runs with the flight trigger
  
  //Unfinished commands -> need testing
  private final MoveBalls hopp = new MoveBalls(hopper, () -> flight.getRawButtonPressed(2));    //currently runs with thumb button on flight stick
  private final Climb climb = new Climb(climber, () -> flight.getRawButtonPressed(12));         //currently runs with base button #12 on flight stick

  //Shuffleboard/smartdashboard integration and statistics
  private final UpdateStats updateStats = new UpdateStats(stats, () -> {return true;});

  //cameras
  private CameraServer camera1 = CameraServer.getInstance();
  private CameraServer camera2 = CameraServer.getInstance();

  //IMUs
  private ADIS16448_IMU imu = new ADIS16448_IMU();

  public RobotContainer() {
    //subsystem.setDefaultCommand(command);
    driveTrain.setDefaultCommand(drive);
    collector.setDefaultCommand(collect);
    shooter.setDefaultCommand(shoot);
    hopper.setDefaultCommand(hopp);
    climber.setDefaultCommand(climb);
    stats.setDefaultCommand(updateStats);

    //start cameras
    camera1.startAutomaticCapture();
    camera2.startAutomaticCapture();

    configureButtonBindings();
  }

  /**
   * Use this method to define your button->command mappings.  Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a
   * {@link edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    //see WPILIB if you want to do commands the way they want...
    //I prefer to do it the way i already have, making this method useless
    //it can probably be deleted
  }


  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    SmartDashboard.putStringArray("Auto Select", new String[]{"right", "mid", "left"});
    String autoString = SmartDashboard.getString("Auto Select", "right");
    SequentialCommandGroup auto;
    if(autoString.equals("right")){
      auto = new SequentialCommandGroup(
        new AutoShoot(shooter, hopper, 7, false),
        new AutoDrive(driveTrain, -.5, 0, 3),
        new AutoCollect(collector, .5)
      );
    }
    else if(autoString.equals("mid")){
      auto = new SequentialCommandGroup(
        new AutoShoot(shooter, hopper, 7, false),
        new AutoTurn(driveTrain, imu, .5, 120),
        new AutoDrive(driveTrain, -.5, 0, 33)
      );
    }
    else{
      auto = new SequentialCommandGroup(
        new ParallelCommandGroup(
          new AutoDrive(driveTrain, -.5, 0, 3),
          new AutoCollect(collector, .5)
        ),
        new AutoTurn(driveTrain, imu, .5, 180)
      );
    }
    return auto;
  }
}
