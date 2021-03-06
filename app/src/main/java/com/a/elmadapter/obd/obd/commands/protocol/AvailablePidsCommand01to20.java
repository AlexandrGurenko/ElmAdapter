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
package com.a.elmadapter.obd.obd.commands.protocol;

import com.a.elmadapter.obd.obd.commands.ObdCommand;
import com.a.elmadapter.obd.obd.enums.AvailableCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * Retrieve available PIDs ranging from 21 to 40.
 */
public class AvailablePidsCommand01to20 extends GenericAvailablePidsCommand {

    private List<Class<? extends ObdCommand>> supportedCommands = new ArrayList<>();

    public AvailablePidsCommand01to20() {
        super(AvailableCommand.PIDS_01_20, 0);
    }

    /**
     * Copy constructor.
     *
     * @param other a {@link AvailablePidsCommand01to20} object.
     */
    public AvailablePidsCommand01to20(AvailablePidsCommand01to20 other) {
        super(other);
    }

    @Override
    public String getNameCommand() {
        return AvailableCommand.PIDS_01_20.getValue();
    }


}
