package Enums;

import java.util.*;

public enum CommunicationCode {
    RegisterClient("1"),
    ClientRegistered("2"),
    ClientAlreadyExists("3"),
    ClientSavingError("4"),
    ValidateTelephone("5"),//Server to ClientApp
    GetClientDetails("6"),
    NewOrder("7"),//ClientApp to Server
    NewMessage("8"),//ClientApp to Server
    ReceiverNotConnectedToServer("9"),//Server to ClientApp
    MessageDelivered("10"),//Server to ClientApp(Receiver ClientApp)
    OrderDelivered("11"),//Server to ClientApp (Sender)
    OrderSent("12"),
    ServerProcessingError("13"),
    MessageReceived("14"),
    HistoryRequest("15"),
    HistoryReport("16");

    private String codeId;
    CommunicationCode (String codeId) {
        this.codeId = codeId;
    }

    private static final Map<String,CommunicationCode> lookup = new HashMap<>();

    static {
        for(CommunicationCode cc : EnumSet.allOf(CommunicationCode.class))
            lookup.put(cc.getCodeId(), cc);
    }

    public String getCodeId(){
        return this.codeId;
    }
    public static ArrayList<String> codes(){
        var codes = new ArrayList<String>();
        for (CommunicationCode cc : CommunicationCode.values()) {
            codes.add(cc.getCodeId());
        }
        return codes;
    }
    public static CommunicationCode getCode(String codeId){
        return lookup.get(codeId);
    }
}

