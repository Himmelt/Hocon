package org.soraworld.hocon.node;

import org.soraworld.hocon.reflect.SubEnum;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Settings {

    @Setting(comment = "comment abc")
    public int abc;
    @Setting(comment = "comment string")
    public String string;
    @Setting(comment = "comment maps")
    public Map<String, Integer> maps;
    @Setting(comment = "Gusk \nsdddd\r\\\\\"")
    //public HashSet<List<Integer>> set;
    public Set<List<Integer>> set;
    @Setting
    public SubEnum subEnum;

}
