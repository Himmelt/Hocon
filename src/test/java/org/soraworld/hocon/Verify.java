package org.soraworld.hocon;

import org.soraworld.hocon.api.Handle;
import org.soraworld.hocon.api.Hocon;
import org.soraworld.hocon.api.Options;
import org.soraworld.hocon.bind.BindContext;
import org.soraworld.hocon.bind.BindReport;
import org.soraworld.hocon.bind.BindingPolicy;
import org.soraworld.hocon.bind.Setting;
import org.soraworld.hocon.bind.TypeAdapter;
import org.soraworld.hocon.doc.Doc;
import org.soraworld.hocon.doc.DocArray;
import org.soraworld.hocon.doc.DocNode;
import org.soraworld.hocon.doc.DocObject;
import org.soraworld.hocon.doc.DocPath;
import org.soraworld.hocon.doc.DocScalar;
import org.soraworld.hocon.error.BindException;
import org.soraworld.hocon.error.ConfException;
import org.soraworld.hocon.error.ParseException;
import org.soraworld.hocon.printer.PrintOptions;
import org.soraworld.hocon.printer.Printer;
import org.soraworld.hocon.syntax.Parser;
import org.soraworld.hocon.text.Escaper;
import org.soraworld.hocon.text.Newline;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 无依赖自检程序：{@code ./gradlew verify} 或直接 {@code java org.soraworld.hocon.Verify}.
 *
 * @author Himmelt
 */
public final class Verify {

    private static int passed;
    private static int failed;
    private static final List<String> failures = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("hocon-verify");
        try {
            groupLexical();
            groupRoundTrip();
            groupComments();
            groupInline();
            groupKeys();
            groupAtomic(dir);
            groupBinding(dir);
            groupDiagnostics(dir);
            groupPath();
        } finally {
            deleteRecursively(dir);
        }
        System.out.println();
        System.out.println("通过 " + passed + " 项，失败 " + failed + " 项");
        for (String failure : failures) {
            System.out.println("  FAIL " + failure);
        }
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ================================================================ A 词法

    private static void groupLexical() throws Exception {
        section("A 词法：引号、转义、注释风格、换行风格");
        Doc doc = Parser.parse("#! header\na = 1 // 行尾双斜杠\nb = \"x\\ty\"\nc = 'no'\n", "lex.conf");
        eq(1, doc.head().size(), "文件头被识别");
        ok(doc.root().containsKey("a"), "普通键");
        eq("x\ty", ((DocScalar) doc.root().get("b")).lexeme(), "双引号内转义被解码");
        eq("'no'", ((DocScalar) doc.root().get("c")).lexeme(), "单引号不是引号语法");

        Doc crlf = Parser.parse("a = 1\r\nb = 2\r\n", "crlf.conf");
        eq(Newline.CRLF, crlf.newline(), "CRLF 被识别");
        ok(Printer.print(crlf).contains("\r\n"), "CRLF 被保持写出");

        Doc indented = Parser.parse("a {\n    b = 1\n}\n", "indent.conf");
        eq(4, indented.indentUnit(), "缩进宽度被识别");
        ok(Printer.print(indented).contains("\n    b = 1"), "缩进宽度被保持写出");

        String[] nasty = {"", " ", "a b", "a:b", "a=b", "#x", "//x", "a\nb", "a\rb", "a\tb",
                "C:\\new\\test", "\u0007", "中文 值", "\"quoted\"", "null", "1.50", "{}", "[]"};
        for (String text : nasty) {
            eq(text, Escaper.unquote(Escaper.quote(text)), "转义互逆 " + repr(text));
        }
    }

    // ================================================================ B 保真往返

    private static void groupRoundTrip() {
        section("B 保真往返：print∘parse 幂等");
        String[] corpus = {
                "",
                "a = 1\n",
                "# 只有注释\n",
                "a {\n  b = 1\n}\n",
                "a = []\n",
                "a = {}\n",
                "a = [1, 2, 3]\n",
                "a = {b = 1, c = {d = 2}}\n",
                "a = [\n  {\n    k = 1\n  }\n  {\n    k = 2\n  }\n]\n",
                "# 头\n\n# 说明\na = 1\n\n\nb = 2\n",
                "a = \"x\\ny\"\n",
                "a = \"C:\\\\new\\\\test\"\n",
                "a = null\nb = \"null\"\nc = 1.50\n",
                "中文键 = 中文值\n",
                "\"a.b\" = 1\n",
                "#! head\n\n# c\na = 1\n",
        };
        for (String source : corpus) {
            try {
                String once = Printer.print(Parser.parse(source, "rt.conf"));
                String twice = Printer.print(Parser.parse(once, "rt.conf"));
                eq(once, twice, "幂等 " + repr(source));
            } catch (Exception e) {
                fail("幂等 " + repr(source) + " 抛异常 " + e);
            }
        }
    }

    // ================================================================ C 注释与顺序

    private static void groupComments() {
        section("C 注释、空行、键顺序");
        String source =
                "#! demo.conf\n"
                        + "\n"
                        + "# 第一段\n"
                        + "alpha = 1\n"
                        + "\n"
                        + "# 第二段\n"
                        + "beta = 2\n"
                        + "gamma = 3\n";
        Doc doc = parse(source);
        eq("[alpha, beta, gamma]", doc.root().keys().toString(), "键顺序保持");
        eq(1, doc.head().size(), "文件头");
        ok(hasComment(doc.root().get("alpha"), "第一段"), "alpha 上的注释");
        ok(hasComment(doc.root().get("beta"), "第二段"), "beta 上的注释");
        ok(hasBlank(doc.root().get("beta")), "beta 前的空行");
        eq(source, Printer.print(doc), "整段文本逐字节保持");

        // 末尾注释与纯注释文件（曾经被整体丢掉）
        eq("# 只有注释\n", Printer.print(parse("# 只有注释\n")), "纯注释文件");
        eq("a = 1\n\n# 末尾说明\n", Printer.print(parse("a = 1\n\n# 末尾说明\n")), "文件末尾注释");
        eq("a {\n  b = 1\n  # 对象尾部注释\n}\n",
                Printer.print(parse("a {\n  b = 1\n  # 对象尾部注释\n}\n")), "对象尾部注释");
    }

    // ================================================================ D 内联与展开

    private static void groupInline() {
        section("D 内联写法与展开写法等价");
        Doc inline = parse("a = [1, 2, 3]\nb = {x = 1}\nc = {}\nd = []\ne = {}\n");
        Doc expanded = parse("a = [\n  1\n  2\n  3\n]\nb = {\n  x = 1\n}\nc {\n}\nd = [\n]\ne {\n}\n");
        eq("3", String.valueOf(((DocArray) inline.root().get("a")).size()), "内联数组元素数");
        eq("3", String.valueOf(((DocArray) expanded.root().get("a")).size()), "展开数组元素数");
        eq("1", lexeme(inline, "b.x"), "内联映射内容");
        eq("1", lexeme(expanded, "b.x"), "展开映射内容");
        eq("0", String.valueOf(((DocObject) inline.root().get("c")).size()), "内联空映射");
        eq("0", String.valueOf(((DocObject) expanded.root().get("e")).size()), "展开空映射");
        // 内联保持内联、展开保持展开
        ok(Printer.print(inline).contains("a = [1, 2, 3]"), "内联写法保持一行");
        ok(Printer.print(expanded).contains("a = [\n  1"), "展开写法保持展开");
    }

    // ================================================================ E 键语义

    private static void groupKeys() {
        section("E 键与路径：含点键不丢数据");
        Doc doc = parse("\"example.com\" = 80\napp.log = \"x\"\n");
        ok(doc.root().containsKey("example.com"), "含点键是字面键");
        eq("80", ((DocScalar) doc.root().get("example.com")).lexeme(), "含点键值正确");
        String printed = Printer.print(doc);
        ok(printed.contains("\"example.com\""), "含点键写出时加引号");
        eq("80", lexeme(parse(printed), DocPath.of("example.com")), "含点键往返不丢");

        // Map<String, Integer> 键含点 → 往返不丢（旧实现会静默变空映射）
        Map<String, Integer> ports = new LinkedHashMap<>();
        ports.put("example.com", 80);
        ports.put("plain", 81);
        PortConfig config = new PortConfig();
        config.ports = ports;
        String text = Printer.print(writeDoc(config));
        PortConfig back = readDoc(text, PortConfig.class);
        eq("{example.com=80, plain=81}", back.ports.toString(), "含点键的 Map 往返");
        ok(text.contains("\"example.com\""), "Map 含点键写出加引号");
    }

    private static void groupPath() {
        section("E2 路径口径");
        Doc doc = parse("a {\n  b = 1\n}\n\"a.b\" = 2\n");
        eq("1", lexeme(doc, "a.b"), "点分路径寻址嵌套");
        eq("2", lexeme(doc, DocPath.of("a.b")), "字面量段寻址含点键");
        ok(doc.get(DocPath.parse("a.b.c")) == null, "越过标量继续下探返回 null");
        ok(doc.get(DocPath.parse("nope")) == null, "不存在的路径返回 null");
    }

    // ================================================================ F 原子性

    private static void groupAtomic(Path dir) throws Exception {
        section("F 原子性：坏文件不破坏内存与磁盘");
        File file = dir.resolve("atomic.conf").toFile();
        Files.write(file.toPath(), "a = 1\nb = 2\nc = 3\n".getBytes(StandardCharsets.UTF_8));

        AtomicConfig config = new AtomicConfig();
        Handle<AtomicConfig> handle = Hocon.of(config, file);
        handle.load();
        eq("1", String.valueOf(config.a), "首次加载 a");
        eq("3", String.valueOf(config.c), "首次加载 c");
        String goodDoc = Printer.print(handle.document());

        Files.write(file.toPath(), "a = 1\nGARBAGE line\nc = 3\n".getBytes(StandardCharsets.UTF_8));
        byte[] corrupted = Files.readAllBytes(file.toPath());
        boolean threw = false;
        try {
            handle.load();
        } catch (ConfException e) {
            threw = true;
            ok(e instanceof ParseException, "抛的是 ParseException");
            ok(e.getMessage().contains(":2:"), "异常带行列号：" + e.getMessage());
        }
        ok(threw, "坏文件必须抛异常");
        eq("1", String.valueOf(config.a), "失败后内存对象未被污染");
        eq("3", String.valueOf(config.c), "失败后内存对象仍完整");
        eq(goodDoc, Printer.print(handle.document()), "失败后文档未被污染");
        ok(java.util.Arrays.equals(corrupted, Files.readAllBytes(file.toPath())), "失败后磁盘文件字节不变");

        // 失败后仍然可以正常保存（写的是上一次成功加载的文档）
        handle.save();
        String afterSave = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        ok(afterSave.contains("a = 1") && afterSave.contains("c = 3"), "失败后保存写回完整文档：" + repr(afterSave));

        // 保存的原子性：临时文件不残留
        try (Stream<Path> stream = Files.list(dir)) {
            boolean leftover = stream.anyMatch(path -> path.getFileName().toString().endsWith(".tmp"));
            ok(!leftover, "不残留 .tmp 文件");
        }
    }

    // ================================================================ G 绑定

    private static void groupBinding(Path dir) throws Exception {
        section("G 绑定：字段读写、容器、枚举、嵌套、自定义适配器");
        File file = dir.resolve("bind.conf").toFile();
        Files.write(file.toPath(),
                ("#! bind.conf\n\n# 端口\nport = 8080\n"
                        + "name = \"srv\"\n"
                        + "kind = ADVANCED\n"
                        + "hosts = [\n  a.com\n  b.com\n]\n"
                        + "ports = {\n  example.com = 80\n}\n"
                        + "nested {\n  x = 7\n}\n"
                        + "spawn {\n  x = 3\n  y = 4\n}\n"
                        + "\n# 用户自己加的键，应当保留\ncustom.user.key = hello\n").getBytes(StandardCharsets.UTF_8));

        Options options = Options.build();
        options.adapters().register(new PointAdapter());

        Server server = new Server();
        Handle<Server> handle = Hocon.of(server, file, options);
        handle.load();

        eq("8080", String.valueOf(server.port), "int 字段");
        eq("srv", server.name, "String 字段");
        eq(Kind.ADVANCED.name(), String.valueOf(server.kind), "枚举字段");
        eq("[a.com, b.com]", server.hosts.toString(), "List 字段");
        eq("{example.com=80}", server.ports.toString(), "Map 含点键字段");
        eq("7", String.valueOf(server.nested.x), "嵌套对象字段");
        eq("(3,4)", server.spawn.toString(), "自定义适配器字段");
        ok(handle.lastReport().unknown().size() == 1, "未知键被报告：" + handle.lastReport().unknown());
        ok(handle.lastReport().isEmpty() == false, "报告非空");

        // 改值后保存：注释、顺序、用户额外键都必须还在
        server.port = 9090;
        server.name = "changed";
        handle.save();
        String saved = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        ok(saved.contains("# 端口"), "保存后原有注释还在");
        ok(saved.contains("#! bind.conf"), "保存后文件头还在");
        ok(saved.contains("\"custom.user.key\" = hello"), "保存后用户额外键还在（含点键自动加引号）");
        ok(saved.contains("port = 9090"), "保存后新值生效");
        ok(saved.contains("\"example.com\" = 80"), "保存后含点键仍在");
        ok(saved.indexOf("port") < saved.indexOf("name"), "保存后键顺序不变");
        eq(saved, Printer.print(readFile(file)), "保存结果可再次解析一致");

        // 再 load 一次，验证保存后的文件能被读回
        Server again = new Server();
        Handle<Server> second = Hocon.of(again, file, options);
        second.load();
        eq("9090", String.valueOf(again.port), "二次加载 port");
        eq("changed", again.name, "二次加载 name");
        eq("{example.com=80}", again.ports.toString(), "二次加载 Map");
        eq("(3,4)", again.spawn.toString(), "二次加载自定义适配器");

        // 静态字段模式
        StaticConfig.value = 0;
        File staticFile = dir.resolve("static.conf").toFile();
        Files.write(staticFile.toPath(), "value = 42\n".getBytes(StandardCharsets.UTF_8));
        Hocon.of(StaticConfig.class, staticFile).load();
        eq("42", String.valueOf(StaticConfig.value), "静态字段模式");

        // 缺失项自动回写
        File writeBackFile = dir.resolve("writeback.conf").toFile();
        Files.write(writeBackFile.toPath(), "# 只有 a\n".getBytes(StandardCharsets.UTF_8));
        Options backOptions = Options.build();
        backOptions.policy().missing(BindingPolicy.Missing.WRITE_BACK);
        AtomicConfig writeBack = new AtomicConfig();
        Hocon.of(writeBack, writeBackFile, backOptions).load();
        String backText = new String(Files.readAllBytes(writeBackFile.toPath()), StandardCharsets.UTF_8);
        ok(backText.contains("a = 1"), "缺失项已按默认值回写：" + repr(backText));
        ok(backText.contains("# 只有 a"), "回写后原注释仍在");
    }

    // ================================================================ H 诊断

    private static void groupDiagnostics(Path dir) throws Exception {
        section("H 诊断：缺失 / 未知 / 类型不符");
        File file = dir.resolve("diag.conf").toFile();
        Files.write(file.toPath(),
                "port = abc\nlangauge = zh_cn\n".getBytes(StandardCharsets.UTF_8));
        Server server = new Server();
        Handle<Server> handle = Hocon.of(server, file);
        handle.load();
        BindReport report = handle.lastReport();
        ok(report.mismatched().size() >= 1, "类型不符被报告：" + report.mismatched());
        eq("25565", String.valueOf(server.port), "类型不符时保留默认值");
        ok(!report.unknown().isEmpty(), "未知键被报告：" + report.unknown());
        ok(!report.missing().isEmpty(), "缺失项被报告：" + report.missing().size() + " 项");
        ok(report.toString().contains("langauge"), "报告里能直接看到拼错的键");

        // 严格策略下必须抛异常
        Options strict = Options.build();
        strict.policy().mismatch(BindingPolicy.Mismatch.ERROR);
        boolean threw = false;
        try {
            Hocon.of(new Server(), file, strict).load();
        } catch (BindException e) {
            threw = true;
            ok(e.getMessage().contains("port"), "严格模式异常带配置路径：" + e.getMessage());
        }
        ok(threw, "严格模式类型不符必须抛异常");

        // 缺适配器 → 明确异常而不是 NPE
        File noAdapterFile = dir.resolve("noadapter.conf").toFile();
        Files.write(noAdapterFile.toPath(), "when = 2020-01-01\n".getBytes(StandardCharsets.UTF_8));
        NoAdapterConfig noAdapter = new NoAdapterConfig();
        Handle<NoAdapterConfig> noAdapterHandle = Hocon.of(noAdapter, noAdapterFile);
        noAdapterHandle.load();
        ok(noAdapterHandle.lastReport().mismatched().size() == 1, "缺适配器进入诊断");
        ok(noAdapter.when == null, "缺适配器时字段保持默认值");
    }

    // ================================================================ 辅助

    private static Doc parse(String source) {
        try {
            return Parser.parse(source, "verify.conf");
        } catch (Exception e) {
            fail("解析失败 " + repr(source) + " -> " + e);
            return new Doc();
        }
    }

    private static Doc readFile(File file) {
        try {
            return Hocon.read(file);
        } catch (Exception e) {
            fail("读取失败 " + file + " -> " + e);
            return new Doc();
        }
    }

    private static String lexeme(Doc doc, String dotted) {
        return lexeme(doc, DocPath.parse(dotted));
    }

    private static String lexeme(Doc doc, DocPath path) {
        DocNode node = doc.get(path);
        return node instanceof DocScalar ? ((DocScalar) node).lexeme() : null;
    }

    private static boolean hasComment(DocNode node, String text) {
        if (node == null || node.before() == null) {
            return false;
        }
        for (org.soraworld.hocon.doc.Trivia trivia : node.before()) {
            if (trivia.isComment() && trivia.text().equals(text)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasBlank(DocNode node) {
        if (node == null || node.before() == null) {
            return false;
        }
        for (org.soraworld.hocon.doc.Trivia trivia : node.before()) {
            if (trivia.isBlank()) {
                return true;
            }
        }
        return false;
    }

    private static Doc writeDoc(Object source) {
        try {
            Options options = Options.build();
            options.adapters().register(new PointAdapter());
            Doc doc = new Doc();
            org.soraworld.hocon.bind.Binder.write(source, org.soraworld.hocon.bind.Schema.of(source.getClass()),
                    doc.root(), options.adapters(), options.policy(), new BindReport());
            return doc;
        } catch (Exception e) {
            fail("写文档失败 " + e);
            return new Doc();
        }
    }

    private static <T> T readDoc(String text, Class<T> type) {
        try {
            T instance = type.getDeclaredConstructor().newInstance();
            Options options = Options.build();
            options.adapters().register(new PointAdapter());
            Doc doc = Parser.parse(text, "mem.conf");
            org.soraworld.hocon.bind.Binder.read(doc, org.soraworld.hocon.bind.Schema.of(type), instance,
                    options.adapters(), options.policy(), new BindReport());
            return instance;
        } catch (Exception e) {
            fail("读文档失败 " + e);
            return null;
        }
    }

    private static void deleteRecursively(Path dir) throws Exception {
        try (Stream<Path> stream = Files.walk(dir)) {
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (Exception ignored) {
                }
            });
        }
    }

    private static String repr(String text) {
        StringBuilder builder = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                builder.append("\\n");
            } else if (c == '\r') {
                builder.append("\\r");
            } else if (c == '\t') {
                builder.append("\\t");
            } else if (c < 0x20) {
                builder.append("\\u").append(String.format("%04X", (int) c));
            } else {
                builder.append(c);
            }
        }
        return builder.append('"').toString();
    }

    private static void section(String name) {
        System.out.println();
        System.out.println("=== " + name);
    }

    private static void ok(boolean condition, String label) {
        if (condition) {
            passed++;
        } else {
            failed++;
            failures.add(label);
            System.out.println("  [FAIL] " + label);
        }
    }

    private static void eq(Object expected, Object actual, String label) {
        boolean same = expected == null ? actual == null : expected.equals(actual);
        if (!same) {
            System.out.println("  [FAIL] " + label + "\n         期望 " + repr(String.valueOf(expected))
                    + "\n         实际 " + repr(String.valueOf(actual)));
        }
        ok(same, label);
    }

    private static void fail(String label) {
        failed++;
        failures.add(label);
        System.out.println("  [FAIL] " + label);
    }

    // ================================================================ 测试用类型

    /**
     * 服务配置.
     */
    public static class Server {

        @Setting(comment = "服务端口")
        public int port = 25565;
        @Setting
        public String name = "default";
        @Setting
        public Kind kind = Kind.BASIC;
        @Setting
        public List<String> hosts = new ArrayList<>();
        @Setting
        public Map<String, Integer> ports = new LinkedHashMap<>();
        @Setting
        public Nested nested = new Nested();
        @Setting
        public Point spawn = new Point(0, 0);
    }

    /**
     * 嵌套配置.
     */
    public static class Nested {

        @Setting
        public int x = 1;
    }

    /**
     * 枚举配置项.
     */
    public enum Kind {
        /**
         * 基础.
         */
        BASIC,
        /**
         * 高级.
         */
        ADVANCED
    }

    /**
     * 无适配器的类型.
     */
    public static class NoAdapterConfig {

        @Setting
        public java.time.LocalDate when = null;
    }

    /**
     * 原子性测试用.
     */
    public static class AtomicConfig {

        @Setting
        public int a = 1;
        @Setting
        public int b = 2;
        @Setting
        public int c = 3;
    }

    /**
     * 静态字段模式.
     */
    public static class StaticConfig {

        @Setting
        public static int value = 0;
    }

    /**
     * 含点键的映射配置.
     */
    public static class PortConfig {

        @Setting
        public Map<String, Integer> ports = new LinkedHashMap<>();
    }

    /**
     * 无 {@code @Setting} 注解的自定义类型.
     */
    public static final class Point {

        public final int x;
        public final int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    /**
     * 自定义适配器.
     */
    public static final class PointAdapter implements TypeAdapter<Point> {

        @Override
        public Point read(DocNode node, BindContext context) throws BindException {
            if (!(node instanceof DocObject)) {
                throw new BindException("期望对象，实际是 " + node.typeName(), context.path().toString());
            }
            DocObject object = (DocObject) node;
            DocNode x = object.get("x");
            DocNode y = object.get("y");
            if (!(x instanceof DocScalar) || !(y instanceof DocScalar)) {
                throw new BindException("缺少 x 或 y", context.path().toString());
            }
            return new Point(((DocScalar) x).asInt(), ((DocScalar) y).asInt());
        }

        @Override
        public DocNode write(Point value, BindContext context) {
            DocObject object = new DocObject();
            object.put("x", DocScalar.of(value.x));
            object.put("y", DocScalar.of(value.y));
            return object;
        }
    }
}
