package com.iec.nav;

import com.graphhopper.ResponsePath;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;

/**
 * Extracts maneuver information from GraphHopper instructions.
 * Used to drive turn arrow and next-turn distance.
 */
public class ManeuverExtractor {

    /**
     * Returns the action/sign of the first maneuver.
     * Example values:
     * 0 = continue
     * 2 = turn left
     * 4 = turn right
     */
    public int extractAction(ResponsePath path) {

        InstructionList instructions = path.getInstructions();

        if (instructions == null || instructions.size() == 0) {
            return 0; // default: continue
        }

        Instruction first = instructions.get(0);
        return first.getSign();
    }

    /**
     * Returns distance to the next maneuver in meters.
     */
    public double extractDistance(ResponsePath path) {

        InstructionList instructions = path.getInstructions();

        if (instructions == null || instructions.size() == 0) {
            return 0;
        }

        Instruction first = instructions.get(0);
        return first.getDistance();
    }
}
