package org.sec.app;

import com.beust.jcommander.Parameter;

import java.util.List;

public class Command {
    @Parameter(names = {"-h", "--help"}, description = "Help Info", help = true)
    public boolean help;

    @Parameter(description = "Scan Jar File")
    public  List<String> jars;

    @Parameter(names = {"-m", "--module"}, description = "Use Module")
    public String module;

    @Parameter(names = {"--debug"}, description = "Debug")
    public boolean isDebug;
}
