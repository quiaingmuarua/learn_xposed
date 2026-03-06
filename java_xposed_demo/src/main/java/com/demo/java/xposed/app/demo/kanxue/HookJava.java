package com.demo.java.xposed.app.demo.kanxue;
import android.app.Application;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookJava implements IXposedHookLoadPackage {
    public static Field getClassField(ClassLoader classloader, String class_name,
                                      String filedName) {

        try {
            Class obj_class = classloader.loadClass(class_name);//Class.forName(class_name);
            Field field = obj_class.getDeclaredField(filedName);
            field.setAccessible(true);
            return field;
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static Object getClassFieldObject(ClassLoader classloader, String class_name, Object obj,
                                             String filedName) {

        try {
            Class obj_class = classloader.loadClass(class_name);//Class.forName(class_name);
            Field field = obj_class.getDeclaredField(filedName);
            field.setAccessible(true);
            Object result = null;
            result = field.get(obj);
            return result;
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static Object invokeStaticMethod(String class_name,
                                            String method_name, Class[] pareTyple, Object[] pareVaules) {

        try {
            Class obj_class = Class.forName(class_name);
            Method method = obj_class.getMethod(method_name, pareTyple);
            return method.invoke(null, pareVaules);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static Object getFieldOjbect(String class_name, Object obj,
                                        String filedName) {
        try {
            Class obj_class = Class.forName(class_name);
            Field field = obj_class.getDeclaredField(filedName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static ClassLoader getClassloader() {
        ClassLoader resultClassloader = null;
        Object currentActivityThread = invokeStaticMethod(
                "android.app.ActivityThread", "currentActivityThread",
                new Class[]{}, new Object[]{});
        Object mBoundApplication = getFieldOjbect(
                "android.app.ActivityThread", currentActivityThread,
                "mBoundApplication");
        Application mInitialApplication = (Application) getFieldOjbect("android.app.ActivityThread",
                currentActivityThread, "mInitialApplication");
        Object loadedApkInfo = getFieldOjbect(
                "android.app.ActivityThread$AppBindData",
                mBoundApplication, "info");
        Application mApplication = (Application) getFieldOjbect("android.app.LoadedApk", loadedApkInfo, "mApplication");
        resultClassloader = mApplication.getClassLoader();
        return resultClassloader;
    }

    public void GetClassLoaderClasslist(ClassLoader classLoader) {
        //private final DexPathList pathList;
        //public static java.lang.Object getObjectField(java.lang.Object obj, java.lang.String fieldName)
        XposedBridge.log("start dealwith classloader:" + classLoader);
        Object pathListObj = XposedHelpers.getObjectField(classLoader, "pathList");
        //private final Element[] dexElements;
        Object[] dexElementsObj = (Object[]) XposedHelpers.getObjectField(pathListObj, "dexElements");
        for (Object i : dexElementsObj) {
            //private final DexFile dexFile;
            Object dexFileObj = XposedHelpers.getObjectField(i, "dexFile");
            //private Object mCookie;
            Object mCookieObj = XposedHelpers.getObjectField(dexFileObj, "mCookie");
            //private static native String[] getClassNameList(Object cookie);
            //    public static java.lang.Object callStaticMethod(java.lang.Class<?> clazz, java.lang.String methodName, java.lang.Object... args) { /* compiled code */ }
            Class DexFileClass = XposedHelpers.findClass("dalvik.system.DexFile", classLoader);

            String[] classlist = (String[]) XposedHelpers.callStaticMethod(DexFileClass, "getClassNameList", mCookieObj);
            for (String classname : classlist) {
                XposedBridge.log(dexFileObj + "---" + classname);
            }
        }
        XposedBridge.log("end dealwith classloader:" + classLoader);

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        Log.i("Xposed01", loadPackageParam.packageName);
        XposedBridge.log("HookJava->app packagename" + loadPackageParam.packageName);
        if (loadPackageParam.packageName.equals("com.kanxue.xposedhook01")) {
            XposedBridge.log("kanxue " + loadPackageParam.packageName);
            /* public static de.robv.android.xposed.XC_MethodHook.Unhook findAndHookMethod(java.lang.Class<?> clazz, java.lang.String methodName, java.lang.Object... parameterTypesAndCallback) { *//* compiled code *//* }

            public static de.robv.android.xposed.XC_MethodHook.Unhook findAndHookMethod(java.lang.String className, java.lang.ClassLoader classLoader, java.lang.String methodName, java.lang.Object... parameterTypesAndCallback) { *//* compiled code *//* }
             */
            ClassLoader classLoader = loadPackageParam.classLoader;

            XposedBridge.log("loadPackageParam.classLoader->" + classLoader);
/*
            01-09 05:26:25.678 29011-29011/? I/Xposed: loadPackageParam.classLoader->dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/com.kanxue.xposedhook01-1/base.apk"],nativeLibraryDirectories=[/data/app/com.kanxue.xposedhook01-1/lib/arm, /vendor/lib, /system/lib]]]
            01-09 05:26:25.679 29011-29011/? I/Xposed: start dealwith classloader:dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/com.kanxue.xposedhook01-1/base.apk"],nativeLibraryDirectories=[/data/app/com.kanxue.xposedhook01-1/lib/arm, /vendor/lib, /system/lib]]]
            01-09 05:26:25.681 29011-29011/? I/Xposed: /data/app/com.kanxue.xposedhook01-1/base.apk---com.qihoo.util.Configuration
            01-09 05:26:25.681 29011-29011/? I/Xposed: /data/app/com.kanxue.xposedhook01-1/base.apk---com.qihoo.util.DtcLoader
            01-09 05:26:25.681 29011-29011/? I/Xposed: /data/app/com.kanxue.xposedhook01-1/base.apk---com.qihoo.util.QHDialog
            01-09 05:26:25.681 29011-29011/? I/Xposed: /data/app/com.kanxue.xposedhook01-1/base.apk---com.qihoo.util.ᵢˋ
            01-09 05:26:25.681 29011-29011/? I/Xposed: /data/app/com.kanxue.xposedhook01-1/base.apk---com.qihoo.util.ᵢˎ
            01-09 05:26:25.681 29011-29011/? I/Xposed: /data/app/com.kanxue.xposedhook01-1/base.apk---com.qihoo.util.ᵢˏ
            01-09 05:26:25.681 29011-29011/? I/Xposed: /data/app/com.kanxue.xposedhook01-1/base.apk---com.stub.StubApp
            01-09 05:26:25.681 29011-29011/? I/Xposed: end dealwith classloader:dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/com.kanxue.xposedhook01-1/base.apk"],nativeLibraryDirectories=[/data/app/com.kanxue.xposedhook01-1/lib/arm, /vendor/lib, /system/lib]]]
*/

            GetClassLoaderClasslist(classLoader);

            ClassLoader parent = classLoader.getParent();
            while (parent != null) {
                XposedBridge.log("parent->" + parent);
                if (parent.toString().contains("BootClassLoader")) {

                } else {
                    GetClassLoaderClasslist(parent);
                }
                parent = parent.getParent();
            }

/*
* 01-09 05:05:34.284 24462-24462/? I/Xposed: loadPackageParam.classLoader->dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/com.kanxue.xposedhook01-1/base.apk"],nativeLibraryDirectories=[/data/app/com.kanxue.xposedhook01-1/lib/arm, /vendor/lib, /system/lib]]]
01-09 05:05:34.284 24462-24462/? I/Xposed: parent->dalvik.system.PathClassLoader[DexPathList[[dex file "/data/dalvik-cache/xposed_XResourcesSuperClass.dex", dex file "/data/dalvik-cache/xposed_XTypedArraySuperClass.dex"],nativeLibraryDirectories=[/vendor/lib, /system/lib]]]
01-09 05:05:34.284 24462-24462/? I/Xposed: parent->java.lang.BootClassLoader@6c4cdad
* */


            Class StubAppClass=XposedHelpers.findClass("com.stub.StubApp",loadPackageParam.classLoader);
            Method[] methods=StubAppClass.getDeclaredMethods();
            for(Method i:methods){
                XposedBridge.log("com.stub.StubApp->"+i);
            }
            XposedHelpers.findAndHookMethod("com.stub.StubApp", loadPackageParam.classLoader, "onCreate", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    XposedBridge.log("com.stub.StubApp->onCreate beforeHookedMethod");
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    XposedBridge.log("com.stub.StubApp->onCreate afterHookedMethod");

                    ClassLoader finalClassLoader=getClassloader();
                    XposedBridge.log("finalClassLoader->" + finalClassLoader);
                    GetClassLoaderClasslist(finalClassLoader);
                    XposedHelpers.findAndHookMethod("com.kanxue.xposedhook01.Student", finalClassLoader, "privatefunc", String.class, int.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Object[] objectarray = param.args;
                            String arg0 = (String) objectarray[0];
                            int arg1 = (int) objectarray[1];
                            XposedBridge.log("beforeHookedMethod11 privatefunc->arg0:" + arg0 + "---arg1:" + arg1);

                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);

                            String result = (String) param.getResult();
                            XposedBridge.log("afterHookedMethod11 privatefunc->result:" + result);

                        }
                    });

                    Class personClass = XposedHelpers.findClass("com.kanxue.xposedhook01.Student$person", finalClassLoader);
                    XposedHelpers.findAndHookMethod(personClass, "getpersonname", String.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            XposedBridge.log("beforeHookedMethod getpersonname->" + param.args[0]);
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XposedBridge.log("afterHookedMethod getpersonname->" + param.getResult());
                        }
                    });


                }
            });

            /*Class StuClass = classLoader.loadClass("com.kanxue.xposedhook01.Student");*/


//            public static String publicstaticfunc(String arg1, int arg2) {
//                String result = privatestaticfunc("privatestaticfunc", 200);
//                return arg1 + "---" + arg2 + "---" + result;
//            }
//
/*            XposedHelpers.findAndHookMethod(StuClass, "publicstaticfunc", String.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Object[] objectarray=param.args;
                    String arg0= (String) objectarray[0];
                    int arg1= (int) objectarray[1];
                    objectarray[0]="changedbyxposedjava";
                    objectarray[1]=888;
                    XposedBridge.log("beforeHookedMethod publicstaticfunc->arg0:"+arg0+"---arg1:"+arg1);

                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    String result= (String) param.getResult();
                    param.setResult("changedbyxposed->afterHookedMethod");
                    XposedBridge.log("afterHookedMethod publicstaticfunc->result:"+result);

                }
            });*/
            /*XposedHelpers.findAndHookMethod(StuClass, "privatestaticfunc", String.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Object[] objectarray = param.args;
                    String arg0 = (String) objectarray[0];
                    int arg1 = (int) objectarray[1];
                    objectarray[0] = "changedbyxposedjava";
                    objectarray[1] = 888;
                    XposedBridge.log("beforeHookedMethod privatestaticfunc->arg0:" + arg0 + "---arg1:" + arg1);

                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    String result = (String) param.getResult();
                    param.setResult("changedbyxposed->afterHookedMethod");
                    XposedBridge.log("afterHookedMethod privatestaticfunc->result:" + result);

                }
            });*/
//            private static String privatestaticfunc(String arg1, int arg2) {
//                return arg1 + "---" + arg2;
//            }
//
/*
            XposedHelpers.findAndHookMethod(StuClass, "publicfunc", String.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Object[] objectarray = param.args;
                    String arg0 = (String) objectarray[0];
                    int arg1 = (int) objectarray[1];
                    objectarray[0] = "changedbyxposedjava";
                    objectarray[1] = 888;
                    XposedBridge.log("beforeHookedMethod publicfunc->arg0:" + arg0 + "---arg1:" + arg1);

                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    String result = (String) param.getResult();
                    param.setResult("changedbyxposed->afterHookedMethod");
                    XposedBridge.log("afterHookedMethod publicfunc->result:" + result);

                }
            });
*/
//            public String publicfunc(String arg1, int arg2) {
//                String result = privatefunc("privatefunc", 300);
//                return arg1 + "---" + arg2 + "---" + result;
//            }
//
/*            XposedHelpers.findAndHookMethod(StuClass, "privatefunc", String.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Object[] objectarray = param.args;
                    String arg0 = (String) objectarray[0];
                    int arg1 = (int) objectarray[1];
                    XposedBridge.log("beforeHookedMethod privatefunc->arg0:" + arg0 + "---arg1:" + arg1);

                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    String result = (String) param.getResult();
                    XposedBridge.log("afterHookedMethod privatefunc->result:" + result);

                }
            });*/
//            private String privatefunc(String arg1, int arg2) {
//                return arg1 + "---" + arg2;
//            }


/*            XposedHelpers.findAndHookMethod("com.kanxue.xposedhook01.Student", loadPackageParam.classLoader, "privatefunc", String.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Object[] objectarray = param.args;
                    String arg0 = (String) objectarray[0];
                    int arg1 = (int) objectarray[1];
                    XposedBridge.log("beforeHookedMethod11 privatefunc->arg0:" + arg0 + "---arg1:" + arg1);

                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    String result = (String) param.getResult();
                    XposedBridge.log("afterHookedMethod11 privatefunc->result:" + result);

                }
            });

            Class personClass = XposedHelpers.findClass("com.kanxue.xposedhook01.Student$person", loadPackageParam.classLoader);
            XposedHelpers.findAndHookMethod(personClass, "getpersonname", String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    XposedBridge.log("beforeHookedMethod getpersonname->" + param.args[0]);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    XposedBridge.log("afterHookedMethod getpersonname->" + param.getResult());
                }
            });
            *///    public static java.lang.Class<?> findClass(java.lang.String className, java.lang.ClassLoader classLoader)
            //Class StuClassByXposed=XposedHelpers.findClass("com.kanxue.xposedhook01.Student",classLoader);

        }
    }
}
