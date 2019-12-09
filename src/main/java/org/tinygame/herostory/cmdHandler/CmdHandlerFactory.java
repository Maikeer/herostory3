package org.tinygame.herostory.cmdHandler;

import com.google.protobuf.GeneratedMessageV3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.msg.GameMsgProtocol;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URL;
import java.util.*;

/**
 * 指令处理器工厂
 */
public final class CmdHandlerFactory {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(CmdHandlerFactory.class);
    /**
     * 处理器字典
     */
    static private Map<Class<?>, ICmdHandler<? extends GeneratedMessageV3>> _handlerMap = new HashMap<>();

    /**
     * 私有化类默认构造器
     */
    private CmdHandlerFactory() {
    }

    /**
     * 初始化
     */
    static public void init() {
        Class<?>[] innerClazzArray = GameMsgProtocol.class.getDeclaredClasses();

//        for (Class<?> innerClazz : innerClazzArray) {
//            if (!GeneratedMessageV3.class.isAssignableFrom(innerClazz)) {
//                continue;
//            }
//
//            String clazzName = innerClazz.getSimpleName();
//            clazzName = clazzName.toLowerCase();
//
//            for (GameMsgProtocol.MsgCode msgCode : GameMsgProtocol.MsgCode.values()) {
//                String strMsgCode = msgCode.name();
//                strMsgCode = strMsgCode.replaceAll("_", "");
//                strMsgCode = strMsgCode.toLowerCase();
//
//                if (!strMsgCode.startsWith(clazzName)) {
//                    continue;
//                }
//
//            }
//
//        }

        try {
//            Object returnObj = innerClazz.getDeclaredMethod("getDefaultInstance").invoke(innerClazz);

            Package aPackage = ICmdHandler.class.getPackage();
            String pk = aPackage.getName();
            String path = pk.replace('.', '/');
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            URL resource = classloader.getResource(path);
            File file = new File(resource.getPath());
            boolean exists = file.exists();
            ArrayList<Class> classes = new ArrayList<>();
            if(exists){
                File[] files = file.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        boolean b = pathname.getName().endsWith(".class");

                        if(b){
                            try {
                                String name=pathname.getName();
                                Class<?> aClass = Class.forName(pk+"."+name.substring(0, name.length() - 6));
                                if(ICmdHandler.class.isAssignableFrom(aClass)&&!aClass.equals(ICmdHandler.class)){
                                    classes.add(aClass);
                                    return true;
                                }
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            return false;
                        }else{
                            return false;
                        }

                    }
                });
                for (Class c: classes) {
                    Type[] genericInterfaces = c.getGenericInterfaces();
                    Type type= genericInterfaces[0];
                    if(type instanceof ParameterizedType){
                        ParameterizedType pType= (ParameterizedType) type;
                        if(null == pType){
                            continue;
                        }
                        Type[] actualTypeArguments = pType.getActualTypeArguments();
                        for (Type t:actualTypeArguments){
                            System.err.println(t);
                            ICmdHandler<? extends GeneratedMessageV3> newInstance = (ICmdHandler<? extends GeneratedMessageV3>) c.getConstructor().newInstance();
                            Class<?> forName = Class.forName(t.getTypeName());
                            _handlerMap.put(forName,  newInstance);
                            LOGGER.info("{} <==> {}", forName.getName(), newInstance.getClass().getName());
                        }
                    }

                }
            }

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        System.err.println("初始化完毕");
//        _handlerMap.put(GameMsgProtocol.UserEntryCmd.class, new UserEntryCmdHandler());
//        _handlerMap.put(GameMsgProtocol.WhoElseIsHereCmd.class, new WhoElseIsHereCmdHandler());
//        _handlerMap.put(GameMsgProtocol.UserMoveToCmd.class, new UserMoveToCmdHandler());
    }

    /**
     * 创建指令处理器工厂
     *
     * @param msgClazz 消息类
     * @return
     */
    static public ICmdHandler<? extends GeneratedMessageV3> create(Class<?> msgClazz) {
        if (null == msgClazz) {
            return null;
        }

        return _handlerMap.get(msgClazz);
    }
}
