package org.sec.model;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class ResultInfo {
    public static final int HIGH_RISK = 3;
    public static final int MID_RISK = 2;
    public static final int LOW_RISK = 1;

    private String vulName;
    private int risk;
    private final List<String> chains = new ArrayList<>();

    public String getVulName() {
        return vulName;
    }

    public void setVulName(String vulName) {
        this.vulName = vulName;
    }

    public int getRisk() {
        return risk;
    }

    public void setRisk(int risk) {
        this.risk = risk;
    }

    public List<String> getChains() {
        return chains;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.vulName);
        sb.append("\n");
        for (String s : chains) {
            sb.append("\t");
            sb.append(s);
            sb.append("\n");
        }
        return sb.toString();
    }
}
