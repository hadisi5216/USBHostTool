package com.hadisi.usbhosttool.utils;

/**
 * Created by wugang on 2015/11/12.
 */
public class Commands {
    public static String[] baseCommands = {"常用指令", "继电器控制"};

    public static String[][] commands_dis = {
            new String[]{"获取设备硬件地址"},
            new String[]{"开第一路", "开第二路", "开第三路", "开第四路", "关第一路", "关第二路", "关第三路", "关第四路"}
    };

    public static String[][] commands_com = {
            new String[]{"getaddress"},
            new String[]{" 01050010FF008DFF", "01050011FF00DC3F", "01050012FF002C3F", " 01050013FF007DFF",
                    "010500100000CC0F", "0105001100009DCF", " 0105001200006DCF", "0105001300003C0F"}
    };
}
