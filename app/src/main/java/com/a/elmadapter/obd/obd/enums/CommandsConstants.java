package com.a.elmadapter.obd.obd.enums;

import com.a.elmadapter.obd.obd.commands.ObdCommand;
import com.a.elmadapter.obd.obd.commands.control.DistanceMILOnCommand;
import com.a.elmadapter.obd.obd.commands.control.DistanceSinceCCCommand;
import com.a.elmadapter.obd.obd.commands.control.DtcNumberCommand;
import com.a.elmadapter.obd.obd.commands.control.EquivalentRatioCommand;
import com.a.elmadapter.obd.obd.commands.control.ModuleVoltageCommand;
import com.a.elmadapter.obd.obd.commands.control.TimeSinceCCCommand;
import com.a.elmadapter.obd.obd.commands.control.TimeSinceMILOnCommand;
import com.a.elmadapter.obd.obd.commands.control.TimingAdvanceCommand;
import com.a.elmadapter.obd.obd.commands.engine.AbsoluteLoadCommand;
import com.a.elmadapter.obd.obd.commands.engine.LoadCommand;
import com.a.elmadapter.obd.obd.commands.engine.MassAirFlowCommand;
import com.a.elmadapter.obd.obd.commands.engine.OilTempCommand;
import com.a.elmadapter.obd.obd.commands.engine.RPMCommand;
import com.a.elmadapter.obd.obd.commands.engine.RelativeThrottlePositionCommand;
import com.a.elmadapter.obd.obd.commands.engine.RuntimeCommand;
import com.a.elmadapter.obd.obd.commands.engine.SpeedCommand;
import com.a.elmadapter.obd.obd.commands.engine.ThrottlePositionCommand;
import com.a.elmadapter.obd.obd.commands.fuel.ConsumptionRateCommand;
import com.a.elmadapter.obd.obd.commands.fuel.EthanolLevelCommand;
import com.a.elmadapter.obd.obd.commands.fuel.FindFuelTypeCommand;
import com.a.elmadapter.obd.obd.commands.fuel.FuelLevelCommand;
import com.a.elmadapter.obd.obd.commands.fuel.FuelTrimCommand;
import com.a.elmadapter.obd.obd.commands.fuel.WidebandAirFuelRatioCommand;
import com.a.elmadapter.obd.obd.commands.pressure.BarometricPressureCommand;
import com.a.elmadapter.obd.obd.commands.pressure.FuelPressureCommand;
import com.a.elmadapter.obd.obd.commands.pressure.FuelRailPressureCommand;
import com.a.elmadapter.obd.obd.commands.pressure.IntakeManifoldPressureCommand;
import com.a.elmadapter.obd.obd.commands.protocol.AvailablePidsCommand21to40;
import com.a.elmadapter.obd.obd.commands.protocol.AvailablePidsCommand41to60;
import com.a.elmadapter.obd.obd.commands.temperature.AirIntakeTemperatureCommand;
import com.a.elmadapter.obd.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.a.elmadapter.obd.obd.commands.temperature.EngineCoolantTemperatureCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by elton on 01/02/18.
 */

public class CommandsConstants {
    public static final Map<Integer, Class<? extends ObdCommand>> SUPPORTED_COMMANDS = new HashMap<>();

    static {
        // 01 to 20
        SUPPORTED_COMMANDS.put(0x01, DtcNumberCommand.class);
        SUPPORTED_COMMANDS.put(0x04, LoadCommand.class);
        SUPPORTED_COMMANDS.put(0x05, EngineCoolantTemperatureCommand.class);
        SUPPORTED_COMMANDS.put(0x06, FuelTrimCommand.class);
        SUPPORTED_COMMANDS.put(0x07, FuelTrimCommand.class);
        SUPPORTED_COMMANDS.put(0x08, FuelTrimCommand.class);
        SUPPORTED_COMMANDS.put(0x09, FuelTrimCommand.class);
        SUPPORTED_COMMANDS.put(0x0A, FuelPressureCommand.class);
        SUPPORTED_COMMANDS.put(0x0B, IntakeManifoldPressureCommand.class);
        SUPPORTED_COMMANDS.put(0x0C, RPMCommand.class);
        SUPPORTED_COMMANDS.put(0x0D, SpeedCommand.class);
        SUPPORTED_COMMANDS.put(0x0E, TimingAdvanceCommand.class);
        SUPPORTED_COMMANDS.put(0x0F, AirIntakeTemperatureCommand.class);
        SUPPORTED_COMMANDS.put(0x10, MassAirFlowCommand.class);
        SUPPORTED_COMMANDS.put(0x11, ThrottlePositionCommand.class);
        SUPPORTED_COMMANDS.put(0x1F, RuntimeCommand.class);
        SUPPORTED_COMMANDS.put(0x20, AvailablePidsCommand21to40.class);

        // 21 to 40
        SUPPORTED_COMMANDS.put(0x21, DistanceMILOnCommand.class);
        SUPPORTED_COMMANDS.put(0x23, FuelRailPressureCommand.class);
        SUPPORTED_COMMANDS.put(0x2F, FuelLevelCommand.class);
        SUPPORTED_COMMANDS.put(0x31, DistanceSinceCCCommand.class);
        SUPPORTED_COMMANDS.put(0x33, BarometricPressureCommand.class);
        SUPPORTED_COMMANDS.put(0x34, WidebandAirFuelRatioCommand.class);
        SUPPORTED_COMMANDS.put(0x40, AvailablePidsCommand41to60.class);

        // 41 to 60
        SUPPORTED_COMMANDS.put(0x42, ModuleVoltageCommand.class);
        SUPPORTED_COMMANDS.put(0x43, AbsoluteLoadCommand.class);
        SUPPORTED_COMMANDS.put(0x44, EquivalentRatioCommand.class);
        SUPPORTED_COMMANDS.put(0x45, RelativeThrottlePositionCommand.class);
        SUPPORTED_COMMANDS.put(0x46, AmbientAirTemperatureCommand.class);
        SUPPORTED_COMMANDS.put(0x4D, TimeSinceMILOnCommand.class);
        SUPPORTED_COMMANDS.put(0x4E, TimeSinceCCCommand.class);
        SUPPORTED_COMMANDS.put(0x51, FindFuelTypeCommand.class);
        SUPPORTED_COMMANDS.put(0x52, EthanolLevelCommand.class);
        SUPPORTED_COMMANDS.put(0x5C, OilTempCommand.class);
        SUPPORTED_COMMANDS.put(0x5E, ConsumptionRateCommand.class);
    }

    private CommandsConstants() {
    }
}
