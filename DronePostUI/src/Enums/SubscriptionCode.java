package Enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum SubscriptionCode {
    NO_SUBSCRIPTION("0", "-"),
    SMALL_PACKAGE("1", "50 shipments for $99"),
    BIG_PACKAGE("2", "150 shipments for $179");

    private String code;
    private String description;

    private static final Map<String,SubscriptionCode> codeLookup = new HashMap<>();
    private static final Map<String,SubscriptionCode> descriptionLookup = new HashMap<>();
    static {
        for(SubscriptionCode sc : EnumSet.allOf(SubscriptionCode.class))
            codeLookup.put(sc.getCode(), sc);
    }
    static {
        for(SubscriptionCode sc : EnumSet.allOf(SubscriptionCode.class))
            descriptionLookup.put(sc.getDescription(), sc);
    }
    SubscriptionCode(String code, String description){
        this.code = code;
        this.description = description;
    }

    public final String getCode(){
        return this.code;
    }
    public static String getCode(String description) { return descriptionLookup.get(description).getCode(); }
    public final String getDescription(){
        return this.description;
    }
    public static String getDescription(String code){
        return codeLookup.get(code).getDescription();
    }
}
