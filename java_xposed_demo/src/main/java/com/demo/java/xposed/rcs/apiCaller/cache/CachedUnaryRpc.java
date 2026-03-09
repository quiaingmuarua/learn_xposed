package com.demo.java.xposed.rcs.apiCaller.cache;



import static com.demo.java.xposed.base.BaseAppHook.protoObjToHex;

import android.text.TextUtils;

import com.example.command.model.CommandException;
import com.example.command.model.ErrorCode;
import com.demo.java.xposed.rcs.hook.messages.Rcs.TachyonRegistrationToken;
import com.demo.java.xposed.utils.LogUtils;
import com.demo.java.xposed.utils.collection.StringUtils;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.UUID;

public class CachedUnaryRpc {


    private static TachyonRegistrationToken tachyonRegistrationToken;


    private static Object InternalGrpcChannel;

    private static Object Metadata;

    public static String DEFAULT_TACHYONREGISTRATION_TOKEN_HEX = "0a2462663337323133302d386433312d343065342d616534382d3637636632323466333165611a035243533276007f291512f8aa70fc3021e99474b48626bc911723663bd3b10d35a9a71d4368f603e45388d8900c10f1c549792c8ef8b3a5f05c9fd6c360832f9d087654872148af9a9273dab25d8dec354b5720b0158b763bf7386f85d984e84544da61206c1c1f8a9b3e0ba38c2347ef5488e0195c8fbd6c3936d73a451887b1d30920053804480152386d657373616765732e616e64726f69645f32303234303531395f30355f726330302e70686f6e652e6f70656e626574615f64796e616d6963d00c03";


    public static String DEFAULT_KICKOFF_HEX = "0af4010a2439316239323533342d343731382d343064612d396162652d3364663164616631356562311a03524353327d007f291512d82a9b4f04143d1e5ae7fe85bc435cae449d2e36657c460cce1ba21590010b045d2540555f78f42059bb0c1e4a06f9b64489e96eb4f9cfc6a18ecb5a4932c4c4822a8b585bfd85cface0e690ca375c585549b8442eb4e6ec24d15e01747e4ab208ad006a2be6c8ecacb79f33b58e991a4bce8ec4ae2f53763a451887b1d30920053804480152386d657373616765732e616e64726f69645f32303234303531395f30355f726330302e70686f6e652e6f70656e626574615f64796e616d6963d00c0312330802122034343434386239386137663534396432623635306662336238373665316432621a0352435332080a060a022b3110011a150801120c2b31343034333736393736361a03524353";


    public static void cacheMetadata(Object metadata) throws ClassNotFoundException {
        if(metadata!=null &&metadata.toString().contains("x-goog-api-key")){
            Metadata = metadata;
            LogUtils.show("cache_obj metadata metadata= " + Metadata);
        }
    }

    public static void cacheInternalGrpcChannel(Object internalGrpcChannel) throws ClassNotFoundException {
        InternalGrpcChannel = internalGrpcChannel;
        LogUtils.show("cache_obj InternalGrpcChannel InternalGrpcChannel= " + InternalGrpcChannel);
    }


    public static void cacheOAuthToken(byte[] oAuthToken) throws InvalidProtocolBufferException {

        if (tachyonRegistrationToken == null) {
            tachyonRegistrationToken = TachyonRegistrationToken.parseFrom(StringUtils.HexStringToBytes(DEFAULT_TACHYONREGISTRATION_TOKEN_HEX));

        }

        TachyonRegistrationToken.Builder tachyonRegistrationTokenBuilder = tachyonRegistrationToken.toBuilder();
        tachyonRegistrationTokenBuilder.setOAuthToken(ByteString.copyFrom(oAuthToken));
        tachyonRegistrationToken = tachyonRegistrationTokenBuilder.build();

        LogUtils.show("cache_obj OAuthToken tachyonRegistrationToken= " + protoObjToHex(tachyonRegistrationToken));

    }


    public static void cacheTokenFromHex(String tachyonRegistrationTokenHex) {
        try {
            if(TextUtils.isEmpty(tachyonRegistrationTokenHex)){
                return;
            }
            LogUtils.show("cache_obj TokenFromHex tachyonRegistrationTokenHex= " + tachyonRegistrationTokenHex);

            tachyonRegistrationToken = TachyonRegistrationToken.parseFrom(StringUtils.HexStringToBytes(tachyonRegistrationTokenHex));
        } catch (InvalidProtocolBufferException e) {
            LogUtils.printStackErrInfo("cacheTokenFromHex err=" ,e);
        }

    }

    public static TachyonRegistrationToken getNewTokenWithoutClearFlag() {
        LogUtils.show("getNewToken tachyonRegistrationToken= " + tachyonRegistrationToken);
        if (tachyonRegistrationToken == null) {
            throw new CommandException(ErrorCode.TOKEN_IS_NULL,"getNewTokenWithoutClearFlag tachyonRegistrationToken is null");
        }
        TachyonRegistrationToken.Builder tachyonRegistrationTokenBuilder = tachyonRegistrationToken.toBuilder();
        tachyonRegistrationTokenBuilder.setRefreshRequestId(UUID.randomUUID().toString());
        return tachyonRegistrationTokenBuilder.build();
    }


    public static TachyonRegistrationToken getNewToken() {
        LogUtils.show("getNewToken tachyonRegistrationToken= " + tachyonRegistrationToken);
        if (tachyonRegistrationToken == null) {
            throw new CommandException(ErrorCode.TOKEN_IS_NULL,"getNewToken tachyonRegistrationToken is null");
        }
        TachyonRegistrationToken.Builder tachyonRegistrationTokenBuilder = tachyonRegistrationToken.toBuilder();
        tachyonRegistrationTokenBuilder.setRefreshRequestId(UUID.randomUUID().toString());
        tachyonRegistrationTokenBuilder.clearSomeFlag();
        return tachyonRegistrationTokenBuilder.build();
    }

    public static Object getInternalGrpcChannel() {
        return InternalGrpcChannel;
    }

    public static Object getMetadata() {
        return Metadata;
    }

    public static  TachyonRegistrationToken getTachyonRegistrationToken(){
        return tachyonRegistrationToken;
    }




}
