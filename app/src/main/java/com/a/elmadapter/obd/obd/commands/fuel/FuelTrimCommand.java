/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.a.elmadapter.obd.obd.commands.fuel;

import com.a.elmadapter.obd.obd.commands.PercentageObdCommand;
import com.a.elmadapter.obd.obd.enums.AvailableCommand;
import com.a.elmadapter.obd.obd.enums.FuelTrim;

/**
 * Fuel Trim.
 */
public class FuelTrimCommand extends PercentageObdCommand {

    /**
     * Default constructor.
     * <p>
     * Will read the bank from parameters and construct the command accordingly.
     * Please, see FuelTrim enum for more details.
     */
    private FuelTrimCommand(final FuelTrim bank) {
        super(bank.getValue());
    }

    /**
     * <p>Constructor for FuelTrimCommand.</p>
     */
    public FuelTrimCommand() {
        this(FuelTrim.SHORT_TERM_BANK_1);
    }

    /**
     * <p>performCalculations.</p>
     */
    @Override
    protected void performCalculations() {
        // ignore first two bytes [hh hh] of the response
        percentage = (100f / 128) * buffer.get(2) - 100;
    }

    @Override
    public String getNameCommand() {
        return AvailableCommand.SHORT_TERM_BANK_1.getValue();
    }

}
