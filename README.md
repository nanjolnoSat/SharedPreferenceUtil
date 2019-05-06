# SharedPreferenceUtil
这是一个SharedPreference的工具类,需继承BaseSharedPreferencesUtil,重写getSpName方法,设置name<br/>
使用的时候用new的方法创建工具类,里面提供了一推set,get方法,这些没啥好说的<br/>
还提供了直接给一个Object就将Object的所有属性设置到sp里面,用法<br/>
<pre>
  [@SpPrefix("该类里面所有字段的key的前缀")]
  //这里有一个用处就是,如果需要将通过json拿到的bean保存到sp
  //又害怕bean里面的某些字段和别的bean一样,就可以加个前缀
  public class Person{
    [@SpValue("如果没有这个注解,则使用字段名称作为key,否则使用这里面的内容作为key")]
    public String name;
    public int age;
  }
  public static void main(String[] args){
    BaseSharedPreferencesUtil util = new BasePreferencesUtil(){
      @Override
      protected String getSpName() {
        return "name";
      }
    };
    
    //保存sp
    Person person1 = new Person();
    person1.name = "name";
    person1.age = 10;
    //这样就完成了保存操作
    util.put(person);
    
    //获取sp
    //方法1
    Person person2 = new Person();
    //可以给person2的属性设置数据作为sp获取不到时的默认值
    util.get(person2);
    //方法2
    Person person3 = util.get(Person.class);
  }
  
  //通过代理的方法设置/获取sp里面的数据
  interface PersonUtil{
    //get模式
    //get模式的话,返回类型表示获取的key的类型
    //可以给一个参数,参数类型必须和返回类型一样
    //该惭怍作为当该key是空值时的默认值
    @SpGet
    int getAge();
    
    @SpGet
    int getAge(int age);
    
    //set模式
    //set模式返回值必须该interface的类型
    //每个参数必须有SpKey的注解,否则不会起作用
    //参数可以为多个
    @SpSet
    PersonUtil setPerson(@SpKey("name") String name,@SpKey("age") int age);
    
    @SpSet
    PersonUtil setAge(@SpKey("age") int age);
  }
  public static void main(String[] args){
    BaseSharedPreferencesUtil util = new BasePreferencesUtil(){
      @Override
      protected String getSpName() {
        return "name";
      }
    };
    PersonUtil personUtil = util.getControlObject(PersonUtil.class);
    int age1 = personUtil.getAge();
    int age2 = personUtil.getAge(5);
    personUtil.setPerson("mishaki",2);
    personUtil.setAge(3);
  }
</pre>
