package com.example.messages.cache;


import com.example.command.util.SimpleLogUtils;



public class XposedClassCacher {

    public static Class<?> callOptionsClass;

    public static Class<?> deadlineClass;

    public static Class<?> clientCallListenerClass;
    public static Class<?> ClientCallImplClass;

    // Class<?> ClientCallsClass = classLoader.loadClass("edzr");
    //Class<?> GrpcChannelClass = classLoader.loadClass("edax");
    // Class<?> MetadataGrpcClass = classLoader.loadClass("edfb");
    //Class<?> ContinuationUnitClass = classLoader.loadClass("eetw");
    public static Class<?> ClientCallsClass;
    public static Class<?> GrpcChannelClass;
    public static Class<?> MetadataGrpcClass;
    public static Class<?> ContinuationUnitClass;


    //ClassLoader classLoader = lpparam.classLoader;
    //Class<?> StatusClass = classLoader.loadClass("io.grpc.Status");
    public static Class<?> StatusClass;

    //  Class<?> MethodDescriptorClass = classLoader.loadClass("edff");
    public static Class<?> MethodDescriptorClass;

    // Class<?> ManagedChannelImpl = classLoader.loadClass("edtl");
    public static Class<?> ManagedChannelImpl;


    //Class<?> RegRefreshRpcHandlerClass = classLoader.loadClass("czvn");
    public static Class<?> RegRefreshRpcHandlerClass;

    //Class<?> TachyonRegistrationToken = classLoader.loadClass("ebrl");
    public static Class<?> TachyonRegistrationToken;
    //  Class<?> RetrieveRegistrationIdHandlerClass = classLoader.loadClass("bkjr");
    public static Class<?> RetrieveRegistrationIdHandlerClass ;

    //   Class<?> LookupRegisteredRequetsClass = classLoader.loadClass("ebmg");
    public static Class<?> LookupRegisteredRequetsClass;


    //Class<?> KickGroupUsersRequestClass = classLoader.loadClass("ebtj");
    public static Class<?> KickGroupUsersRequestClass;


    //Class<?> AddGroupUsersRequestClass = classLoader.loadClass("ebsf");
    public static  Class<?> AddGroupUsersRequestClass;

    //Class<?> StubClientCallsClass = classLoader.loadClass("eeeb");
    public static Class<?> StubClientCallsClass;


    //Class<?> ReceiveMessagesRequestsRpcClass = classLoader.loadClass("ebmv");
    public static  Class<?> ReceiveMessagesRequestsRpcClass;

    public static void run(ClassLoader classLoader) {

        try {
            SimpleLogUtils.show("ClassManager run");

            callOptionsClass = classLoader.loadClass("edaw");
            deadlineClass = classLoader.loadClass("edcf");
            clientCallListenerClass = classLoader.loadClass("edba");
            ClientCallImplClass = classLoader.loadClass("edmx");
            ClientCallsClass = classLoader.loadClass("edzr");
            GrpcChannelClass = classLoader.loadClass("edax");
            MetadataGrpcClass = classLoader.loadClass("edfb");
            ContinuationUnitClass = classLoader.loadClass("eetw");
            MethodDescriptorClass = classLoader.loadClass("edff");
            ManagedChannelImpl = classLoader.loadClass("edtl");
            TachyonRegistrationToken=classLoader.loadClass("ebrl");
            RetrieveRegistrationIdHandlerClass=classLoader.loadClass("bkjr");
            RegRefreshRpcHandlerClass=classLoader.loadClass("czvn");
            LookupRegisteredRequetsClass=classLoader.loadClass("ebmg");
            StubClientCallsClass = classLoader.loadClass("eeeb");
            KickGroupUsersRequestClass = classLoader.loadClass("ebtj");
            AddGroupUsersRequestClass=classLoader.loadClass("ebsf");
            StatusClass=classLoader.loadClass("io.grpc.Status");
            ReceiveMessagesRequestsRpcClass=classLoader.loadClass("ebmv");
            SimpleLogUtils.show("ClassManager init success");
        } catch (Exception e) {
            SimpleLogUtils.show("ClassManager init error " + e.getMessage());
        }

    }


}
