package com.a.elmadapter.obd.obd.commands.control;

import com.a.elmadapter.obd.obd.commands.PersistentCommand;
import com.a.elmadapter.obd.obd.enums.AvailableCommand;

import java.util.regex.Matcher;

import static com.a.elmadapter.obd.obd.utils.RegexUtils.STARTS_WITH_ALPHANUM_PATTERN;

public class CalibrationIdCommand extends PersistentCommand {

    private String nameId;

    public CalibrationIdCommand() {
        super(AvailableCommand.CALIBRATION_ID);
    }

    public CalibrationIdCommand(CalibrationIdCommand other) {
        super(other);
    }

    @Override
    protected void performCalculations() {
        final String result = getResult();
        String workingData;

        if (result.contains(":")) {  // CAN(ISO-15765) protocol.
            // 9 is xxx490201, xxx is bytes of information to follow.
            workingData = result.replaceAll(".:", "").substring(9);
            Matcher m = STARTS_WITH_ALPHANUM_PATTERN.matcher(convertHexToString(workingData));
            if (m.find()) {
                workingData = result.replaceAll("0:49", "").replaceAll(".:", "");
            }
        } else {  // ISO9141-2, KWP2000 Fast and KWP2000 5Kbps (ISO15031) protocols.
            workingData = result.replaceAll("49020.", "");
        }
        nameId = convertHexToString(workingData).replaceAll("[\u0000-\u001f]", "");
        nameId = nameId.replaceAll("I", "");
    }

    @Override
    public String getFormattedResult() {
        return getCalculatedResult();
    }

    @Override
    public String getCalculatedResult() {
        return nameId;
    }

    private String convertHexToString(String hex) {
        StringBuilder sb = new StringBuilder();
        // 49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {

            // grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            // convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            // convert the decimal to character
            sb.append((char) decimal);
        }

        return sb.toString();
    }

}
