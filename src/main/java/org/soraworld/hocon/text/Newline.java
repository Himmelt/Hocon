package org.soraworld.hocon.text;

/**
 * 换行风格.
 * 读入时识别、写出时可沿用，避免"用 LF 打开、保存成 CRLF"这类无谓的全文件 diff.
 *
 * @author Himmelt
 */
public enum Newline {

    /**
     * Unix 风格 {@code \n}.
     */
    LF("\n"),
    /**
     * Windows 风格 {@code \r\n}.
     */
    CRLF("\r\n");

    private final String text;

    Newline(String text) {
        this.text = text;
    }

    /**
     * 获取换行文本.
     *
     * @return 换行文本
     */
    public String text() {
        return text;
    }

    /**
     * 从源文本推断换行风格.
     *
     * @param source 源文本
     * @return 含 {@code \r\n} 时返回 {@link #CRLF}，否则 {@link #LF}
     */
    public static Newline detect(String source) {
        return source != null && source.indexOf('\r') >= 0 ? CRLF : LF;
    }
}
