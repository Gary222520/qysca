package nju.edu.cn.qysca.utils.parser;

/**
 * 表示pom间的父子关系
 */
public class HasParentRelationship {
    public static final String[] HEADERS = {"fg", "fa", "fv", "tg", "ta", "tv"};
    private final String fg;
    private final String fa;
    private final String fv;
    private final String tg;
    private final String ta;
    private final String tv;
    public HasParentRelationship(String fg, String fa, String fv, String tg, String ta, String tv){
        this.fg = fg;
        this.fa = fa;
        this.fv = fv;
        this.tg = tg;
        this.ta = ta;
        this.tv = tv;
    }

    public String toCsvString(){
        return fg+ "," + fa + "," + fv + "," + tg + "," + ta + "," + tv;
    }
}
