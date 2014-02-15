package com.willwinder.universalgcodesender.types;

import com.willwinder.universalgcodesender.AbstractController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: MerrellM
 * Date: 2/14/14
 * Time: 4:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class Job {

    private List<String> rawLines;
    private List<GcodeCommand> commands;

    public Job() {
        rawLines = new ArrayList<String>();
    }

    public Job(List<String> lines) {
        rawLines = new ArrayList<String>(lines);
    }

    public List<String> getRawLines() {
        return rawLines;
    }

    public List<GcodeCommand> getCommands() {
        if (commands == null) {
            // TODO generate commands
        }
        return commands;
    }

}
