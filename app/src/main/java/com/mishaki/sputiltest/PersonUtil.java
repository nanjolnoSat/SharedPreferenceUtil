package com.mishaki.sputiltest;

import com.mishaki.sputil.annotation.SpGet;
import com.mishaki.sputil.annotation.SpKey;
import com.mishaki.sputil.annotation.SpSet;

public interface PersonUtil {
    @SpGet("person-age")
    int age();

    @SpSet
    PersonUtil setAge(@SpKey("person-age") int age);

    @SpSet
    PersonUtil setInfo(@SpKey("person-name") String name, @SpKey("person-sex") boolean sex);
}
