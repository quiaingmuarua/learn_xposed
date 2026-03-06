package com.demo.java.xposed.device;

import android.content.ContentResolver;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.util.Random;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by Lyh on
 * 2020/1/11
 */
public class XposedRandomDevice {

    private static String TAG = "XposedInto";



    private Class<?> PhoneInfoClass = null;

    private Class<?> PhoneInfoClass2 = null;

    private Class<?> PhoneInfoClass3 = null;

    private Class<?> PhoneInfoClass4 = null;


    private String[] devives = new String[]{
            "MI 9", "MI 8", "MI 6", "MI 5", "MI 4", "MI 3",
            "OPPO A5", "OPPO A37", "OPPO A59", "OPPO A59s", "OPPO A57", "OPPO A77", "OPPO R15",
            "OPPO A79", "OPPO A73", "OPPO R9", "OPPO R9plus",
            "OPPO R11", "OPPO R11plus", "OPPO R11s", "OPPO R11splus", "OPPO R11", "OPPO R15"
    };

    private String[] changshang = new String[]{
            "Xiaomi", "OPPO", "VIVO", "HUAWEI",
    };

    private String[] banben = new String[]{
            "4.4", "5.0", "5.1", "6.0", "7.0", "7.1", "8.0", "9.0",
    };


    private int getRandom(int maxValue) {
        return new Random().nextInt(maxValue);
    }

    public void RandomPhoneInfo(ClassLoader mLoader) {
        try {
            PhoneInfoClass = Class.forName("android.os.Build", true, mLoader);
            Log.e(TAG, "拿到了 手机 设备信息的 bin对象 ");
            XposedHelpers.findAndHookMethod(PhoneInfoClass, "getString",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            String arg = (String) param.args[0];
                            switch (arg) {
                                case "ro.product.model":
                                    //MI 9
                                    String devive = devives[getRandom(devives.length - 1)];
                                    Log.e(TAG, "获取 手机 设备信息 返回的 结果是  " + devive);
                                    param.setResult(devive);

                                    break;
                                case "ro.product.brand": {
                                    //Xiaomi
                                    String string = changshang[getRandom(changshang.length - 1)];
                                    param.setResult(string);
                                    Log.e(TAG, "获取 手机 品牌信息  返回的结果 是    " + string);

                                    break;
                                }
                                case "ro.product.name":
                                    //cepheus
                                    Log.e(TAG, "获取 手机 产品 名字  返回的结果是   " + param.getResult().toString());
                                    break;
                                case "ro.build.id": {
                                    String string = banben[getRandom(banben.length - 1)];
                                    param.setResult(string);
                                    Log.e(TAG, "获取 手机 版本   返回的结果是   " + string);
                                    break;
                                }
                                case "no.such.thing":
                                    param.setResult(System.currentTimeMillis() + "");
                                    Log.e(TAG, "获取 手机 序列号   ");
                                    break;
                                case "ro.build.host":
                                    Log.e(TAG, "获取 手机 Host   " + param.getResult().toString());
                                    break;
                                case "ro.build.version.release": {
                                    String string = banben[getRandom(banben.length - 1)];
                                    param.setResult(string);
                                    Log.e(TAG, "获取 安卓版本号  返回结果是  " + string);
                                    break;
                                }
                            }

                        }
                    }
            );
            PhoneInfoClass2 = Class.forName("android.telephony.TelephonyManager", true, mLoader);
            if (PhoneInfoClass2 == null) {
                PhoneInfoClass2 = Class.forName("android.telephony.TelephonyManager");
            }
            if (PhoneInfoClass2 != null) {
                XposedHelpers.findAndHookMethod(PhoneInfoClass2, "getDeviceId", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        String imei = getIMEI();
                        param.setResult(imei);
                        Log.e(TAG, "获取IMEI被调用了  返回的 结果 是 " + imei);
                    }
                });
            }
            PhoneInfoClass3 = Class.forName("android.provider.Settings", true, mLoader);

            if (PhoneInfoClass3 == null) {
                PhoneInfoClass3 = Class.forName("android.provider.Settings");
            }
            if (PhoneInfoClass3 != null) {
                XposedHelpers.findAndHookMethod(PhoneInfoClass3,
                        "getString",
                        ContentResolver.class,
                        String.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);
                                String string = System.currentTimeMillis() + "832";
                                param.setResult(string);
                                Log.e(TAG, "获取AndroidID 被调用了 返回结果是 " + string);
                            }
                        });
            }
            PhoneInfoClass4 = Class.forName("java.net.InetAddress", true, mLoader);

            if (PhoneInfoClass4 == null) {
                PhoneInfoClass4 = Class.forName("java.net.InetAddress");
            }
            if (PhoneInfoClass4 != null) {
                XposedHelpers.findAndHookMethod(PhoneInfoClass4,
                        "getHostAddress",
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);
                                //192.168.123.71
                                param.setResult(getRandomIp());
                                Log.e(TAG, "获取IP地址  被调用了 返回结果是 " + param.getResult().toString());
                            }
                        });
            }
            //Hook 获取mac地址
            XposedHelpers.findAndHookMethod(WifiInfo.class, "getMacAddress", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    String mac = getMac();
                    param.setResult(mac);
                    Log.e(TAG, "获取MAC  被调用了 返回结果是 " + mac);

                }
            });
            //Hook 获取IMSI地址
            XposedHelpers.findAndHookMethod(TelephonyManager.class, "getSubscriberId", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    String mac = getImsi();
                    param.setResult(mac);
                    Log.e(TAG, "获取getImsi  被调用了 返回结果是 " + mac);

                }
            });
            //Hook 获取IMEI 26+ 以上版本的方法
            XposedHelpers.findAndHookMethod(TelephonyManager.class, "getImei", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    String mac = getIMEI();
                    param.setResult(mac);
                    Log.e(TAG, "获取getIMEI  26+  被调用了 返回结果是 " + mac);

                }
            });
            //Hook 获取 ICCID
            XposedHelpers.findAndHookMethod(TelephonyManager.class, "getIccId", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    String mac = getIMEI();
                    param.setResult(mac);
                    Log.e(TAG, "获取 getIccId（ICCID）  被调用了 返回结果是 " + mac);

                }
            });
            //Hook MSISDN
            XposedHelpers.findAndHookMethod(TelephonyManager.class, "getLine1Number", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    param.setResult("");
                    Log.e(TAG, "获取 getLine1Number（MSISDN）  ");

                }
            });
            //Hook 运营商名字
            XposedHelpers.findAndHookMethod(TelephonyManager.class, "getSimOperatorName", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    String string = changshang[getRandom(changshang.length - 1)];
                    param.setResult(string);
                    Log.e(TAG, "获取 getSimOperatorName（运营商名字）");
                }
            });
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "初始化 设备信息 ClassNotFoundException 异常  " + e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * 随机Imsi
     */
    private static String getImsi() {
        // 460022535025034
        String title = "4600";
        int second = 0;
        do {
            second = new Random().nextInt(8);
        } while (second == 4);
        int r1 = 10000 + new Random().nextInt(90000);
        int r2 = 10000 + new Random().nextInt(90000);
        return title + "" + second + "" + r1 + "" + r2;
    }

    /**
     * 随机MAC
     */
    private static String getMac() {
        char[] char1 = "abcdef".toCharArray();
        char[] char2 = "0123456789".toCharArray();
        StringBuffer mBuffer = new StringBuffer();
        for (int i = 0; i < 6; i++) {
            int t = new Random().nextInt(char1.length);
            int y = new Random().nextInt(char2.length);
            int key = new Random().nextInt(2);
            if (key == 0) {
                mBuffer.append(char2[y]).append(char1[t]);
            } else {
                mBuffer.append(char1[t]).append(char2[y]);
            }

            if (i != 5) {
                mBuffer.append(":");
            }
        }
        return mBuffer.toString();
    }

    /**
     * 随机IMEI
     *
     * @return
     */
    private static String getIMEI() {// calculator IMEI
        int r1 = 1000000 + new Random().nextInt(9000000);
        int r2 = 1000000 + new Random().nextInt(9000000);
        String input = r1 + "" + r2;
        char[] ch = input.toCharArray();
        int a = 0, b = 0;
        for (int i = 0; i < ch.length; i++) {
            int tt = Integer.parseInt(ch[i] + "");
            if (i % 2 == 0) {
                a = a + tt;
            } else {
                int temp = tt * 2;
                b = b + temp / 10 + temp % 10;
            }
        }
        int last = (a + b) % 10;
        if (last == 0) {
            last = 0;
        } else {
            last = 10 - last;
        }
        return input + last;
    }
    /**
     * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
     *
     * @return 应用程序是/否获取Root权限
     */
    public static boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd="chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }

    public void getRoot(Context context) {
        String apkRoot="chmod 777 "+ context.getPackageCodePath();
        boolean b = upgradeRootPermission(apkRoot);
        if(b){
            Toast.makeText(context,"以获取root权限",Toast.LENGTH_LONG).show();
        }
        return ;
    }


    public static String getRandomIp() {

        // 需要排除监控的ip范围
        int[][] range = {{607649792, 608174079}, // 36.56.0.0-36.63.255.255
                {1038614528, 1039007743}, // 61.232.0.0-61.237.255.255
                {1783627776, 1784676351}, // 106.80.0.0-106.95.255.255
                {2035023872, 2035154943}, // 121.76.0.0-121.77.255.255
                {2078801920, 2079064063}, // 123.232.0.0-123.235.255.255
                {-1950089216, -1948778497}, // 139.196.0.0-139.215.255.255
                {-1425539072, -1425014785}, // 171.8.0.0-171.15.255.255
                {-1236271104, -1235419137}, // 182.80.0.0-182.92.255.255
                {-770113536, -768606209}, // 210.25.0.0-210.47.255.255
                {-569376768, -564133889}, // 222.16.0.0-222.95.255.255
        };

        Random rdint = new Random();
        int index = rdint.nextInt(10);
        String ip = num2ip(range[index][0] + new Random().nextInt(range[index][1] - range[index][0]));
        return ip;
    }

    /*
     * 将十进制转换成IP地址
     */
    public static String num2ip(int ip) {
        int[] b = new int[4];
        String x = "";
        b[0] = (int) ((ip >> 24) & 0xff);
        b[1] = (int) ((ip >> 16) & 0xff);
        b[2] = (int) ((ip >> 8) & 0xff);
        b[3] = (int) (ip & 0xff);
        x = Integer.toString(b[0]) + "." + Integer.toString(b[1]) + "." + Integer.toString(b[2]) + "." + Integer.toString(b[3]);

        return x;
    }
}
