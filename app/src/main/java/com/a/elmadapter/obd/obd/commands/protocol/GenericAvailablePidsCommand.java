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
import com.a.elmadapter.obd.obd.commands.PersistentCommand;
import com.a.elmadapter.obd.obd.enums.AvailableCommand;
import com.a.elmadapter.obd.obd.enums.CommandsConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Retrieve available PIDs ranging from 21 to 40.
 */
public abstract class GenericAvailablePidsCommand extends PersistentCommand {

    private List<Class<? extends ObdCommand>> supportedCommands = new ArrayList<>();
    private int padding = 0;

    /**
     * Default constructor.
     *
     * @param command a {@link String} object.
     */
    GenericAvailablePidsCommand(AvailableCommand command, int pad) {
        super(command);
        padding = pad;
    }

    /**
     * Copy constructor.
     *
     * @param other a {@link GenericAvailablePidsCommand} object.
     */
    GenericAvailablePidsCommand(GenericAvailablePidsCommand other) {
        super(other);
        padding = other.padding;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performCalculations() {
        supportedCommands.clear();
        String bits = getBinaryStringHex(getCalculatedResult());
        fillAvailableCommands(bits, padding);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormattedResult() {
        StringBuilder sb = new StringBuilder();
        for (Class<? extends ObdCommand> cls : supportedCommands) {
            sb.append(cls.getName()).append(' ');
        }
        return "[ " + sb.toString() + "]";
    }

    public ArrayList<Class<? extends ObdCommand>> getListClassResult() {
        return new ArrayList<>(supportedCommands);
    }

    /**
     * {@inheritDoc}
     */
    @Override

    public String getCalculatedResult() {
        // First 4 characters are a copy of the command code, don't return those
        return String.valueOf(rawData).substring(4);
    }

    private String getBinaryStringHex(String hexString) {
        Long result = Long.parseLong(hexString, 16);
        return Long.toBinaryString(result);
    }

    private void fillAvailableCommands(String bits, int pad) {
        bits = fillBits(bits);

        for (int i = 0; i < bits.length(); i++) {
            int pid = i + pad + 1;
            if (bits.charAt(i) == '1' && CommandsConstants.SUPPORTED_COMMANDS.containsKey(pid) && i != 0x1F) {
                supportedCommands.add(CommandsConstants.SUPPORTED_COMMANDS.get(pid));
            }
        }
    }

    private String fillBits(String str) {
        int size = 32 - str.length();
        if (size == 0) {
            return str;
        }

        char[] repeat = new char[size];
        Arrays.fill(repeat, '0');
        return new String(repeat) + str;
    }

    public final List<Class<? extends ObdCommand>> getSupportedCommands() {
        return supportedCommands;
    }

}
