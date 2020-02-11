/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.a.elmadapter.obd.obd.commands.fuel;


import com.a.elmadapter.obd.obd.commands.PercentageObdCommand;
import com.a.elmadapter.obd.obd.enums.AvailableCommand;

/**
 * Get ethanol level in percentage
 */
public class EthanolLevelCommand extends PercentageObdCommand {

    /**
     * <p>Constructor for FuelLevelCommand.</p>
     */
    public EthanolLevelCommand() {
        super(AvailableCommand.ETHANOL_LEVEL);
    }

    /**
     * <p>getFuelLevel.</p>
     *
     * @return a float.
     */
    public float getFuelLevel() {
        return percentage;
    }

}
