package org.soraworld.hocon.reflect;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

public class ReflectsTest {

    TypeToken t0 = new TypeToken<Object>() {
    };
    TypeToken t1 = new TypeToken<Enum>() {
    };
    TypeToken t2 = new TypeToken<Enum<?>>() {
    };
    TypeToken t4 = new TypeToken<Enum<? extends SubEnum>>() {
    };
    TypeToken t3 = new TypeToken<Enum<SubEnum>>() {
    };
    TypeToken t5 = new TypeToken<SubEnum>() {
    };
    TypeToken t7 = new TypeToken<Collection<?>>() {
    };
    TypeToken t8 = new TypeToken<ArrayList>() {
    };

    // t0 > t1/t2 > t4 > t3 > t5
    @Test
    public void isSuperOf() {
        System.out.println(t0.isSuperTypeOf(t1));//true
        System.out.println(t0.isSuperTypeOf(t2));//true
        System.out.println(t0.isSuperTypeOf(t3));//true
        System.out.println(t0.isSuperTypeOf(t4));//true
        System.out.println(t0.isSuperTypeOf(t5));//true
        System.out.println("-----------------------------");
        System.out.println(t1.isSuperTypeOf(t2));//true
        System.out.println(t1.isSuperTypeOf(t3));//true
        System.out.println(t1.isSuperTypeOf(t4));//true
        System.out.println("============================================");
        System.out.println(t2.isSuperTypeOf(t1));//true
        System.out.println(t2.isSuperTypeOf(t3));//true
        System.out.println(t2.isSuperTypeOf(t4));//true
        System.out.println("============================================");
        System.out.println(t3.isSuperTypeOf(t1));//false
        System.out.println(t3.isSuperTypeOf(t2));//false
        System.out.println(t3.isSuperTypeOf(t4));//false
        System.out.println("============================================");
        System.out.println(t4.isSuperTypeOf(t1));//false
        System.out.println(t4.isSuperTypeOf(t2));//false
        System.out.println(t4.isSuperTypeOf(t3));//true
        System.out.println(t4.isSuperTypeOf(t5));//true
        System.out.println(t4.isSuperTypeOf(t3));//true
        System.out.println("-----------------------------------");
        System.out.println(t5.isSuperTypeOf(t3));//false
        System.out.println(t3.isSuperTypeOf(t5));//true
        System.out.println(t7.isSuperTypeOf(t8));//true

    }
}
