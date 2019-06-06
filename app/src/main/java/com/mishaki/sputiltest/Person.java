package com.mishaki.sputiltest;

import com.mishaki.sputil.annotation.SpIgnore;
import com.mishaki.sputil.annotation.SpPrefix;

@SpPrefix("person-")
public class Person {
    public String name;
    public int age;
    public boolean sex;
    @SpIgnore
    public String ignoreTest = "ignoreTest";
}
