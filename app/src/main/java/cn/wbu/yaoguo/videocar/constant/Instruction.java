package cn.wbu.yaoguo.videocar.constant;

/**
 * 控制指令
 */
public abstract class Instruction {

    /**
     * 前进
     */
    public static final String CAR_FORWARD = "cfw";

    /**
     * 后退
     */
    public static final String CAR_BACK = "cbo";

    /**
     * 左转
     */
    public static final String CAR_TURN_LEFT = "clt";

    /**
     * 右转
     */
    public static final String CAR_TURN_RIGHT = "crt";

    /**
     * 松开
     */
    public static final String KEY_UP = "cop";

    /**
     * 向上
     */
    public static final String SE_UP = "sefw";

    /**
     * 向下
     */
    public static final String SE_DOWN = "sebo";

    /**
     * 向左
     */
    public static final String SE_LEFT = "selt";

    /**
     * 向右
     */
    public static final String SE_RIGHT = "sert";

    /**
     * 按键松开
     */
    public static final String SE_KEY_UP = "seop";

    /**
     * 电机左上
     */
    public static final String EM_UP = "emlfw";

    /**
     * 电机左下
     */
    public static final String EM_DOWN = "emlbo";

    /**
     * 开灯
     */
    public static final String LIGHT_ON = "lao";

    /**
     * 关灯
     */
    public static final String LIGHT_OFF = "las";

    /**
     * 关机
     */
    public static final String SHUTDOWN = "pshut";

    /**
     * 遥控
     */
    public static final String MODE_CONTROL = "ct";

    /**
     * 避障
     */
    public static final String MODE_AUTO = "oa";

    /**
     * 寻迹
     */
    public static final String MODE_TR = "tr";

}
