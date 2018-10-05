# Hocon
Simple Hocon Configuration Library
轻量 Hocon 配置库

[ ![Download](https://api.bintray.com/packages/himmelt/Minecraft/Hocon/images/download.svg) ](https://bintray.com/himmelt/Minecraft/Hocon/_latestVersion)

### 简介
这是一个轻量级的 Hocon 配置库，没有其他依赖项。
注意，这是一个使用 Hocon 格式的配置库，而不是完整的 Hocon 解析库。

该配置库除了使用结点 -- Node 手动获取配置外，还可以使用 `@Setting` 注解字段，以自动获取配置。
使用 NodeMap 的 `void modify(Object target);` 方法，可以用结点的值修改目标对象对应名称用 `@Setting` 注解的字段。
使用 NodeMap 的 `void extract(Object source);` 方法，可以提取源对象对应名称用 `@Setting` 注解的字段的值到结点。
另外该库支持对注释内容的翻译，可通过设置翻译器实现本地化效果。

具体使用方法可以查看 [Javadoc](https://docs.soraworld.org/hocon/)

#### Maven 使用 jcenter
```xml
<dependency>
  <groupId>org.soraworld</groupId>
  <artifactId>hocon</artifactId>
  <version>1.0.9</version>
  <type>pom</type>
</dependency>
```

#### Gradle 使用 jcenter
```groovy
compile 'org.soraworld:hocon:1.0.9'
```

#### 手动添加请下载
[发行版](https://gitee.com/himmelt/Hocon/releases)

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
