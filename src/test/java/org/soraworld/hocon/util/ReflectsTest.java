package org.soraworld.hocon.util;

import org.junit.Test;
import org.soraworld.hocon.TestClazz;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class ReflectsTest {

    private Collection<Number> numberCollection = null;
    private ArrayList<String> stringArrayList = null;
    private ArrayList<Integer> integerArrayList = null;
    private TestClazz.AList<String, TestClazz.SA> stringSAAList = null;
    private TestClazz.AList<Short, TestClazz.SA> shortSAAList = null;
    private Map<String, Number> stringNumberMap = null;
    private Map<String, String> stringStringMap = null;
    private TestClazz.AMap<Integer> integerAMap = null;
    private TestClazz.AMap<String> stringAMap = null;

    @Test
    public void isAssignableFrom() throws NoSuchFieldException {
        Type numberCollection = getClass().getDeclaredField("numberCollection").getGenericType();
        Type stringArrayList = getClass().getDeclaredField("stringArrayList").getGenericType();
        Type integerArrayList = getClass().getDeclaredField("integerArrayList").getGenericType();
        Type stringSAAList = getClass().getDeclaredField("stringSAAList").getGenericType();
        Type shortSAAList = getClass().getDeclaredField("shortSAAList").getGenericType();
        Type stringNumberMap = getClass().getDeclaredField("stringNumberMap").getGenericType();
        Type stringStringMap = getClass().getDeclaredField("stringStringMap").getGenericType();
        Type integerAMap = getClass().getDeclaredField("integerAMap").getGenericType();
        Type stringAMap = getClass().getDeclaredField("stringAMap").getGenericType();

        System.out.println("numberCollection > stringArrayList " + Reflects.isAssignableFrom(numberCollection, stringArrayList));
        System.out.println("numberCollection > stringArrayList " + Reflects.isAssignableFrom(numberCollection, stringArrayList));
        System.out.println("numberCollection > integerArrayList " + Reflects.isAssignableFrom(numberCollection, integerArrayList));
        System.out.println("numberCollection > stringSAAList " + Reflects.isAssignableFrom(numberCollection, stringSAAList));
        System.out.println("numberCollection > shortSAAList " + Reflects.isAssignableFrom(numberCollection, shortSAAList));
        System.out.println("stringNumberMap > integerAMap " + Reflects.isAssignableFrom(stringNumberMap, integerAMap));
        System.out.println("stringNumberMap > stringAMap " + Reflects.isAssignableFrom(stringNumberMap, stringAMap));
        System.out.println("stringStringMap > integerAMap " + Reflects.isAssignableFrom(stringStringMap, integerAMap));
        System.out.println("stringStringMap > stringAMap " + Reflects.isAssignableFrom(stringStringMap, stringAMap));
    }
}
