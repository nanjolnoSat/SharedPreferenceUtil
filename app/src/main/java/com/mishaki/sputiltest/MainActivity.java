package com.mishaki.sputiltest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mishaki.sputil.BaseSharedPreferencesUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BaseSharedPreferencesUtil util = new BaseSharedPreferencesUtil() {
            @Override
            protected String getSpName() {
                return "name";
            }
        };
        Person person1 = new Person();
        person1.name = "myPerson";
        util.put(person1);
        Person person2 = util.get(Person.class);
        Log.v("MainActivityMsg","name1:" + person2.name);
        Person person3 = new Person();
        util.get(person3);
        Log.v("MainActivityMsg","name2:" + person3.name);

        PersonUtil personUtil = util.getControlObject(PersonUtil.class);
        int age = personUtil.setAge(19).setInfo("mishaki",true).age();
        Log.v("MainActivityMsg","age:" + age);
        Person person4 = util.get(Person.class);
        Log.v("MainActivityMsg","name3:" + person4.name);
        Log.v("MainActivityMsg","sex:" + person4.sex);

        Log.v("MainActivityMsg","ignoreTest:" + util.getString("person-ignoreTest"));
    }
}
