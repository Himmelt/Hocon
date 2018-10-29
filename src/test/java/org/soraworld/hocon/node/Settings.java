package org.soraworld.hocon.node;

import org.soraworld.hocon.reflect.SubEnum;

import java.util.*;

public class Settings {

    @Setting(path = "parent.abc", comment = "comment.abc")
    public int abc;
    @Setting(path = "parent.string", comment = "comment.string")
    public String string;
    @Setting(path = "parent.string", nullable = true, comment = "comment.string")
    public String string2;
    @Setting(comment = "comment.maps")
    public Map<String, Integer> maps;
    @Setting(comment = "Gusk \nsdddd\r\\\\\"")
    //public HashSet<List<Integer>> set;
    public Set<List<Integer>> set;
    @Setting(path = "parent2.abc.def")
    public SubEnum subEnum;
    @Setting
    protected ArrayList<String> arrayList = new ArrayList<>();
    @Setting
    protected AnoSetting ssb = new AnoSetting();

    @Setting
    public final TreeMap<String, TreeMap<String, String>> treeMap = new TreeMap<>();

}
