package com.hadisi.usbhosttool.utils;

/**
 * Created by wugang on 2015/11/12.
 */
public class Commands {
    public static String[] baseCommands = {"zigbee配置", "继电器控制", "TEST",
            "315配置", "温湿度"};

    public static String[][] commands_dis = {
            new String[]{"(1)获取固件版本", "(2)设定模块的PAN ID为特定值XXXX",
                    "(3)读取模块的PAN ID值", "(4)读取模块的Short Address",
                    "(5)设置模块的串口波特率XX", "(6)读取模块的MAC地址", "(7)将模块设定为Coordinator",
                    "(8)将模块设定为Router", "(9)读取模块的节点类型", "(10)设置模块的无线频道XX",
                    "(11)读取模块的无线频道", "(12)模块软件重启", "(13)设置模块的数据传输方式",
                    "(14)查询网络状态及信号强度", "(15)设置使所有的配置指令失效", "(16)读取当前模块配置信息",
                    "(17)获取所有设置指令", "(18)设置Router地址", "(19)获取Router地址",
                    "(20)设置传输模式", "(21)读取I/O口", "(22)设置主动上报", "(23)设置非主动上报"},
            new String[]{"开第一路", "开第二路", "开第三路", "开第四路", "关第一路", "关第二路", "关第三路", "关第四路"},
            new String[]{"A5 A0 01 00 00 FF FF FF 5A", "A5 B0 01 00 FF FF FF FF 5A", "A5 B0 01 01 FF FF FF FF 5A",
                    "A5 B0 01 02 FF FF FF FF 5A", "A5 00 00 FF FF FF FF FF 5A", "A5 30 01 FF FF FF FF FF 5A", "A5 80 01 00 FF FF FF FF 5A", "A5 80 01 01 FF FF FF FF 5A"},
            new String[]{"bluetooth1", "bluetooth2", "bluetooth2",
                    "bluetooth2", "bluetooth2"},
            new String[]{"查询地址01的温湿度值（16进制）"}};

    public static String[][] commands_com = {
            new String[]{"get version", "set panid = ", "get panid",
                    "get shortadd", "set baud = XX", "get macadd",
                    "set mode = C", "set mode = R", "get mode",
                    "set channel = ", "get channel", "restart", "daiding",
                    "get signalstrength", "clear", "get config", "help",
                    "set routeradd = ", "get routeradd", "set transfermode = ", "get module IO", "set mode report[Y]", "set mode report[N]"},
            new String[]{" 01050010FF008DFF", "01050011FF00DC3F", "01050012FF002C3F", " 01050013FF007DFF",
                    "010500100000CC0F", "0105001100009DCF", " 0105001200006DCF", "0105001300003C0F"},
            new String[]{"A5A0010000FFFFFF5A", "A5B00100FFFFFFFF5A", "A5B00101FFFFFFFF5A", "A5B00102FFFFFFFF5A", "A50000FFFFFFFFFF5A", "A53001FFFFFFFFFF5A", "A5800100FFFFFFFF5A", "A5800101FFFFFFFF5A"},
            new String[]{"bluetooth1", "bluetooth2", "bluetooth2",
                    "bluetooth2", "bluetooth2"},
            new String[]{"010300000002C40B"}};
}
