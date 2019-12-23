package cn.wbu.yaoguo.videocar.constant;

/**
 * 控制指令
 */
public abstract class Instruction {

    /**
     * 前进
     */
    public static String CAR_FORWARD = "cfw";

    /**
     * 后退
     */
    public static String CAR_BACK = "cbo";

    /**
     * 左转
     */
    public static String CAR_TURN_LEFT = "clt";

    /**
     * 右转
     */
    public static String CAR_TURN_RIGHT = "crt";

    /**
     * 松开
     */
    public static String KEY_UP = "cop";

    /**
     * 向上
     */
    public static String SE_UP = "sefw";

    /**
     * 向下
     */
    public static String SE_DOWN = "sebo";

    /**
     * 向左
     */
    public static String SE_LEFT = "selt";

    /**
     * 向右
     */
    public static String SE_RIGHT = "sert";

    /**
     * 按键松开
     */
    public static String SE_KEY_UP = "seop";

    /**
     * 电机左上
     */
    public static String EM_LEFT_UP = "emlfw";

    /**
     * 电机左下
     */
    public static String EM_LEFT_DOWN = "emlbo";

    /**
     * 电机右上
     */
    public static String EM_RIGHT_UP = "emrfw";

    /**
     * 电机右下
     */
    public static String EM_RIGHT_DOWN = "emrbo";

    /**
     * 遥控
     */
    public static String MODE_CONTROL = "ct";

    /**
     * 避障
     */
    public static String MODE_AUTO = "oa";

}
