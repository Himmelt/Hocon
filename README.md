# Hocon

Simple Hocon Configuration Library

轻量 HOCON 风格配置库

![Hocon](https://github.com/Himmelt/Hocon/workflows/Hocon/badge.svg)

### 简介

这是一个轻量级的 HOCON 风格配置库，**零第三方依赖，只要求 JDK 8**（在 JDK 8 / 21 上均已验证）。

设计上以**配置文档**为中心：手动编辑留下的注释、空行、键顺序、缩进与换行风格都被完整保留，
程序改值只动值、不动这些。`@Setting` 注解字段可以与文档双向绑定，加载时还能给出
"哪些项缺失、哪些键不认识、哪些类型不符"的诊断清单。

### 依赖

#### Maven
```xml
<dependency>
  <groupId>org.soraworld</groupId>
  <artifactId>hocon</artifactId>
  <version>2.0.0-SNAPSHOT</version>
</dependency>
```

#### Gradle
```groovy
compile 'org.soraworld:hocon:2.0.0-SNAPSHOT'
```

#### 仓库
```groovy
repositories {
    maven {
        url 'https://maven.pkg.github.com/himmelt/hocon'
        credentials {
            username = 'Your Github Username'
            password = System.getenv("PACKAGES_TOKEN")//Your personal access token (classic)
        }
    }
}
```

### 快速开始

```java
public class Config {

    @Setting(comment = "服务端口")
    public int port = 25565;

    @Setting
    public String language = "zh_cn";

    @Setting
    public List<String> hosts = new ArrayList<>();
}

Handle<Config> handle = Hocon.of(Config.class, new File("config/server.conf"));
Config config = handle.load();

// 配置里把 language 拼成了 langauge 时，这里会直接指出来
handle.lastReport().unknown().forEach(System.out::println);
handle.lastReport().missing().forEach(System.out::println);

config.port = 9090;
handle.save();          // 注释、空行、键顺序、用户自己加的键都还在
```

### 配置格式

#### 注释与文件头

`#` 与 `//` 到行尾为注释，`#!` 在文件最前面是文件头。注释与空行**永远被保留**，保存时原样写回。

```hocon
#! server.conf

# 服务器设置
server {
  # 端口
  port = 8080

  # 下面这段是用户自己加的，程序不会动它
  custom.note = hello
}
```

#### 基础结点

键与值之间用 `=` 连接。值里含空格、引号、结构字符或控制字符时会自动加双引号并转义。

```hocon
key1 = abc
key2 = "字符串 值2"
key3 = 123.456
key4 = false
key5 = null        # 空值（NULL 类型）
key6 = "null"      # 字符串 "null"，与 key5 不是一回事
```

#### 列表结点

```hocon
# 展开写法
hosts = [
  a.example.com
  "b.example.com"
]
# 一行写法，两种写法解析结果完全一致
flags = [true, false, null]
empty = []
listlist = [
  [1, 2]
  [3, 4]
]
```

#### 映射结点

`key {` 与 `key = {` 等价。

```hocon
int_map {
  key1 = 233
  key2 = 456
}
空映射 {}
"!himmelt&shiki" {}          # 含特殊字符的键加引号
inline = {a = 1, b = 2}      # 一行写法
嵌套 {
  子结点 {
    孙子结点 {
      键 = 1234567
    }
  }
}
```

#### 键的引号规则

| 场景 | 写法 | 说明 |
|---|---|---|
| 键本身含 `.` | `"example.com" = 80` | 含点键写出时**自动**加引号，读回来仍是同一个键 |
| 键含空格或结构字符 | `"my key" = 1` | 自动加引号 |
| 值含空格 | `name = "my server"` | 自动加引号 |
| 值含换行、反斜杠 | `path = "C:\\new\\test"` | 自动转义，读回来与写入前**严格相等** |

#### 路径与含点键

键是**字面量**，路径是**显式**的——两者不混。想按层级访问就构造 `DocPath`：

```java
Doc doc = Hocon.read(new File("config/server.conf"));

doc.get(DocPath.parse("server.port"));      // 按层级寻址
doc.get(DocPath.of("server", "port"));      // 同上，每段都是字面量
doc.get(DocPath.of("example.com"));         // 取字面键 "example.com"
doc.get(DocPath.parse("example.com"));      // null —— parse 会按 '.' 切成两段
```

数组用下标访问，不把 `1` 当成路径段，避免"`1` 是键还是下标"的猜测：

```java
DocArray hosts = (DocArray) doc.get(DocPath.parse("server.hosts"));
DocNode first = hosts.get(0);
```

### 注解绑定

```java
@Setting(comment = "服务端口")     // 注释：结点还没有注释时写进文件，已有注释时永不覆盖
public int port = 25565;

@Setting(path = "server.port")    // 指定配置路径，留空则用字段名
public int port = 25565;

@Setting(translate = false)       // 注释不交给翻译器
public String note = "";
```

| 字段类型 | 支持情况 |
|---|---|
| `String` / `char` / `boolean` / 各种数值（含 `BigDecimal`、`BigInteger`） | 内置 |
| 枚举 | 内置（按名字，失败时列出全部合法值） |
| `List` / `Set` / `Queue` / `Deque` 及其具体实现 | 内置 |
| `Map<K, V>`（键支持字符串与数值，**含点键不会丢**） | 内置 |
| `DocNode` 及其子类 | 内置（原样绑定文档结点） |
| 自己带 `@Setting` 字段的类 | 内置（递归绑定） |
| 其他类型 | 需要 `TypeAdapter`，否则给出明确异常并指出是哪个字段 |

自定义适配器：

```java
public final class LocationAdapter implements TypeAdapter<Location> {
    @Override
    public Location read(DocNode node, BindContext context) throws BindException {
        DocObject object = (DocObject) node;
        return new Location(((DocScalar) object.get("world")).lexeme(),
                ((DocScalar) object.get("x")).asInt());
    }

    @Override
    public DocNode write(Location value, BindContext context) {
        DocObject object = new DocObject();
        object.put("world", DocScalar.of(value.getWorld()));
        object.put("x", DocScalar.of(value.getX()));
        return object;
    }
}

Options options = Options.build();
options.adapters().register(new LocationAdapter());   // 类型自动从泛型推断
Handle<Config> handle = Hocon.of(Config.class, file, options);
```

### 加载与保存的语义

| 方向 | 语义 |
|---|---|
| `load()` | **原子**：解析或绑定失败会抛异常，内存对象与磁盘文件都不受影响 |
| `load()` | 缺失项保留字段默认值并进 `missing()`；类型不符保留默认值并进 `mismatched()`；不认识的键进 `unknown()` |
| `save()` | 默认 **`MERGE`**：在已有文档上按路径原地更新，已有条目连注释一起保留，新条目追加到末尾 |
| `save()` | **原子**：先写同目录 `.tmp` 再原子改名，失败不会留下半截配置 |

策略可调：

```java
Options options = Options.build();
options.policy()
       .missing(BindingPolicy.Missing.WRITE_BACK)   // 缺失项用默认值自动补写回文件
       .unknown(BindingPolicy.Unknown.REMOVE)       // 删掉不认识的键
       .mismatch(BindingPolicy.Mismatch.ERROR)      // 类型不符直接抛异常
       .saveMode(BindingPolicy.SaveMode.MERGE);     // 默认
```

注释本地化：

```java
options.commentTranslator(key -> bundle.getString(key, key));
```

### 分层架构

```
api      Hocon / Handle / Options          业务门面：加载·保存·诊断·迁移
  ↑
bind     Schema / Adapter / Binder         对象绑定：写是「合并」，不是「重建」
  ↑         ↑ mirror  Mirror                唯一允许反射的地方
doc      Doc / DocScalar / DocObject …     文档模型：纯数据，不含 IO（中心枢纽）
  ↑
syntax   Lexer / Parser                    词法与语法：字符 → 记号 → 文档
printer  Printer / PrintOptions            写出与格式化策略
  ↑
text     Escaper / Position / Newline      引号与转义的唯一实现
```

依赖方向严格单向、无环。三个关键取舍：

1. **`Doc` 就是抽象语法树**，`Parser` 直接产出它，不做中间 AST——省一次深拷贝与整树遍历；
2. **引号与转义只有一份实现**（`text.Escaper`），写出侧与读入侧共用，因此
   `unquote(quote(x)).equals(x)` 恒成立；
3. **读取一律"先构建、后整体替换"**，失败即抛带 `文件:行:列` 的异常，不可能出现
   "读坏一次、内存剩半截、下次保存抹掉原文件"。

`doc` 与 `printer` 都不依赖文件系统，因此解析、打印、绑定三者可以完全脱离 IO 单测。

### 构建与自检

```bash
./gradlew build          # 编译（已开启 -Xlint:all，零告警）
./gradlew verify         # 运行无依赖自检程序（112 项）
```

自检程序在 `src/test/java/org/soraworld/hocon/Verify.java`，覆盖：
转义互逆、`print∘parse` 幂等（逐字节）、注释与空行保真、内联/展开等价、
含点键往返、坏文件不破坏内存与磁盘、绑定与诊断清单、静态字段模式、缺失项回写。

### 与完整 HOCON 的差别

本库是"使用 HOCON 风格语法的配置库"，**不是**完整 HOCON 实现。明确不支持：

- `include` 文件包含；
- `${...}` 变量替换与 `+=` 追加；
- 多行字符串 `"""`；
- `a = ${b} { c = 1 }` 这类合并语义。

`a.b = 1` 这种写法会被解析成**字面键** `a.b`（要让层级生效请写成 `a { b = 1 }`）；
这是刻意的取舍——键与路径分离之后，"含点键丢数据"这类问题在结构上不会发生。

### 从 1.x 迁移

2.0 是破坏性重构，包的职责与类名都变了：

| 1.x | 2.0 |
|---|---|
| `new FileNode(file, options)` + `load()` + `modify(this)` | `Hocon.of(Config.class, file).load()` |
| `NodeMap` / `NodeList` / `NodeBase` / `FileNode` | `Doc` / `DocObject` / `DocArray` / `DocScalar` |
| `NodeMap.put("a.b", v)`（按 `.` 拆层级） | `doc.set(DocPath.of("a.b"), v)`（字面键）/ `DocPath.parse("a.b")`（层级） |
| `@Setting(trans = 0b0001)` | `@Setting(translate = true)` |
| `TypeSerializer` | `TypeAdapter` + `Adapters.register` |
| `load(backup, keepComments)` | 无这两个开关：加载是原子的，注释永远解析进文档，是否写出由 `PrintOptions.emitComments` 决定 |
| `Paths` | `DocPath` |

`1.x` 的源码保留在 git 历史（提交 `181432c`）中，需要对照可随时 `git show`。
