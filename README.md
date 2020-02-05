# Hocon
Simple Hocon Configuration Library
轻量 Hocon 配置库

![Hocon](https://github.com/Himmelt/Hocon/workflows/Hocon/badge.svg)

### 简介
这是一个轻量级的 Hocon 配置库，没有其他依赖项。
注意，这是一个使用 Hocon 格式的配置库，而不是完整的 Hocon 解析库。

该配置库除了使用结点 -- Node 手动获取配置外，还可以使用 `@Setting` 注解字段，以自动获取配置。
使用 NodeMap 的 `void modify(Object target);` 方法，可以用结点的值修改目标对象对应名称用 `@Setting` 注解的字段。
使用 NodeMap 的 `void extract(Object source);` 方法，可以提取源对象对应名称用 `@Setting` 注解的字段的值到结点。
另外该库支持对注释内容的翻译，可通过设置翻译器实现本地化效果。

具体使用方法可以查看 [Javadoc](https://docs.soraworld.org/hocon/)

### 使用依赖
#### Maven
```xml
<dependency>
  <groupId>org.soraworld</groupId>
  <artifactId>hocon</artifactId>
  <version>1.2.1</version>
  <type>pom</type>
</dependency>
```
#### Gradle
```groovy
compile 'org.soraworld:hocon:1.2.1'
```
#### 仓库
```groovy
repositories {
    jcenter()
    maven {
        url = 'https://oss.jfrog.org/artifactory/oss-release-local/' //''https://oss.jfrog.org/artifactory/libs-release/'
    }
    maven {
        url = 'https://oss.jfrog.org/artifactory/oss-snapshot-local/' //'https://oss.jfrog.org/artifactory/libs-snapshot/'
    }
}
```

### 专用HOCON格式
0. #### 根结点

根结点即文件所对应的结点, 根结点必定是一个 `NodeMap` 结点.
根结点下存储 key<->value 键值对, 其中 value 值可以是
基础结点(NodeBase), 列表结点(NodeList), 映射结点(NodeMap)

1. #### 基础结点(值实质是字符串)

键 和 值 之间用 `=` 连接, 如果字符串含有空格或其他特殊字符,
则需要在字符串两端加双引号 `"`
详细特殊字符的正则表达为 ``` `.*[":=,+?`!@#$^&*{}\[\]\\].* ```
```hocon
# 井号开头的行全部认为是注释
key1 = abc
key2 = "字符串值2"
key3 = 123.456
key4 = false
# 值的内容代表 空 null
keyNull = null
# 值的内容代表字符串 "null"
stringNull = "null"
```

2. #### 列表结点

键 和 值 之间用 `=` 连接, 如果列表为空, 直接接 关闭的方括号 `[]`,
如过列表不为空, 接 左方括号 `[` , 新行填列表元素, 每个元素都新起一行.
元素结束, 新行 接 右方括号 `]`.
缩进只是为了美观, 元素可以不用缩进, 顶行写也是可以的.
```hocon
# 字符串列表
stringlist = [
  "字符串列表元素1"
  字符串列表元素2
  "string elemnet 3"
  HelloWorld
]
# 整数列表
intlist = [
  1
  3
  5
  7
]
# 空列表
emptylist = []
# (整数)列表列表
listlist = [
  [
    1
    2
    4
  ]
  [
    6
    8
    9
  ]
]
# 映射列表(其存储的元素是 映射)
maplist = [
  # 第一个映射
  {
    key1 = abc
    key2 = 123
  }
  # 第二个映射
#缩进只是为了美观, 元素可以不用缩进, 顶行写也是可以的.
{
                          key1 = false
hello = "Hello World"
      }
]
```

3. #### 映射结点

键 和 值 之间没有内容, 如果映射为空, 直接接 关闭的花括号 `{}`,
键 只能为字符串, 如果键含有特殊字符 则要加双引号 `"`,
如过映射不为空, 接 左花括号 `{` , 新行填 映射键值对, 每个键值对都新起一行.
元素结束, 新行 接 右花括号 `}`.
缩进只是为了美观, 元素可以不用缩进, 顶行写也是可以的.
```hocon
# 整数映射
int_map {
  key1 = 233
  ke2 = 456
  xxx = 5749850394
}
# 空映射
# 键名可以为中文(常规使用时不建议)
空映射 {}
# 特殊字符的键要加 引号 ""
"!himmelt&shiki" {
  # 这样也是空映射
}
# 嵌套映射, 和 yaml/json 等 的层级关系一样
父结点 {
  键1 = 值1
  子基础结点 = 值2
  空列表1 = []
  列表2 = [
    1
    3
    4
  ]
  子列表结点 = [
    {
      键1 = 值1
      键2 = 值2
      空列表1 = []
    }
    {
       键11 = 值11
       键22 = 值22
       空列表11 = []
    }
  ]
  子映射结点 {
      孙子键1 = "孙子值1"
      孙子映射结点 {
        重孙子键 = 1234567
      }
  }
}
```

### 示例
```java
public class Test{
    
    @Setting(comment = "comment.lang")
    protected String lang = "zh_cn";
    @Setting(comment = "comment.debug")
    protected boolean debug = false;

    protected final Path confile;
    protected final Options options = Options.build();

    public Manager(Path path) {
        this.path = path;
        this.options.setTranslator(this::trans);
        this.options.registerType(new LocationSerializer());
        this.confile = path.resolve(plugin.getId().replace(' ', '_') + ".conf");
    }
    
    public boolean load() {
        try {
            FileNode rootNode = new FileNode(confile.toFile(), options);
            rootNode.load(true);
            rootNode.modify(this);
            options.setDebug(debug);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
```
