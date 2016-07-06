package com.hadisi.usbhosttool.protocol;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
import com.hadisi.usbhosttool.utils.Utils;

/**
 * Created by wugang on 2015/11/13.
 */
public class FTxConnection {
    public static D2xxManager ftD2xx = null;
    private FT_Device ftDev = null;
    private byte[] readData;
    private char[] readDataToText;
    public static final int readLength = 512;

    public readThread read_thread;
    private Context DeviceUARTContext;
    private boolean uart_configured = false;
    public boolean bReadThreadGoing = false;

    private int DevCount = -1;
    private int currentIndex = -1;
    private int openIndex = 0;

    public int iavailable = 0;

    private IFTxConnection mMessageListener;

    public FTxConnection(Context context, D2xxManager mftD2xx) {

        ftD2xx = mftD2xx;

        DeviceUARTContext = context;

        readData = new byte[readLength];
        readDataToText = new char[readLength];
    }

    public interface IFTxConnection {
        /**
         * 硬件返回的数据
         *
         * @param message
         */
        void onMessage(String message);
    }

    public void setMessageFTxListener(IFTxConnection listener) {
        mMessageListener = listener;
    }

    /**
     * 打开设备
     */
    public void openUsbDevice() {
        if (DevCount <= 0) {
            createDeviceList();
        }
        connectFunction();
    }

    /**
     * 设置通信参数
     *
     * @param baudRate
     * @param dataBit
     * @param stopBit
     * @param parity
     * @param flowControl
     */
    public void setConfig(int baudRate, byte dataBit, byte stopBit, byte parity, byte flowControl) {
        SetConfig(baudRate, dataBit, stopBit, parity, flowControl);
    }

    /**
     * 释放资源
     */
    public void release() {
        disconnectFunction();
    }

    public void stop() {
        disconnectFunction();
    }

    public void resume() {
        connectFunction();
    }

    /**
     * 写入数据
     *
     * @param message
     */
    public void writeMessage(String message) {
        if (DevCount <= 0 || ftDev == null) {
            Utils.showToast(DeviceUARTContext, "设备未打开");
        } else if (uart_configured == false) {
            Utils.showToast(DeviceUARTContext, "串口未配置");
            return;
        } else {
            SendMessage(message);
        }
    }

    /**
     * 写入数据，如果hex是16则告知下位机是16进制
     *
     * @param message
     * @param i       为16
     */
    public void writeMessage(String message, int hex) {
        if (DevCount <= 0 || ftDev == null) {
            Utils.showToast(DeviceUARTContext, "设备未打开");
        } else if (uart_configured == false) {
            Utils.showToast(DeviceUARTContext, "串口未配置");
            return;
        } else {
            SendMessage(message, hex);
        }
    }

    public void createDeviceList() {
        int tempDevCount = ftD2xx.createDeviceInfoList(DeviceUARTContext);

        if (tempDevCount > 0) {
            if (DevCount != tempDevCount) {
                DevCount = tempDevCount;
                // updatePortNumberSelector();
            }
        } else {
            DevCount = -1;
            currentIndex = -1;
        }
    }

    private void connectFunction() {
        // TODO Auto-generated method stub
        int tmpProtNumber = openIndex + 1;

        if (currentIndex != openIndex) {
            if (null == ftDev) {
                try {
                    ftDev = ftD2xx.openByIndex(DeviceUARTContext, openIndex);
                } catch (Exception e) {

                }
            } else {
                synchronized (ftDev) {
                    ftDev = ftD2xx.openByIndex(DeviceUARTContext, openIndex);
                }
            }
            uart_configured = false;
        } else {
            Utils.showToast(DeviceUARTContext, "Device port " + tmpProtNumber + " is already opened");
            return;
        }

        if (ftDev == null) {
            Utils.showToast(DeviceUARTContext, "open device port(" + tmpProtNumber + ") NG, ftDev == null");
            Utils.showToast(DeviceUARTContext, "打开出错，请检查是否连接RS232设备");
            return;
        }

        if (true == ftDev.isOpen()) {
            currentIndex = openIndex;
            Utils.showToast(DeviceUARTContext, "open device port(" + tmpProtNumber + ") OK");
            Utils.showToast(DeviceUARTContext, "打开成功");

            if (false == bReadThreadGoing) {
                read_thread = new readThread(handler);
                read_thread.start();
                bReadThreadGoing = true;
            }
        } else {
            Utils.showToast(DeviceUARTContext, "open device port(" + tmpProtNumber + ") NG");
            // Toast.makeText(DeviceUARTContext, "Need to get permission!",
            // Toast.LENGTH_SHORT).show();
        }
    }

    public void disconnectFunction() {
        DevCount = -1;
        currentIndex = -1;
        bReadThreadGoing = false;
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (ftDev != null) {
            synchronized (ftDev) {
                if (true == ftDev.isOpen()) {
                    ftDev.close();
                }
            }
        }
    }

    public void SetConfig(int baud, byte dataBits, byte stopBits, byte parity,
                          byte flowControl) {
        if (ftDev == null) {
            Utils.showToast(DeviceUARTContext, "设置出错，请检查是否连接RS232设备");
            return;
        }
        if (ftDev.isOpen() == false) {
            Log.e("j2xx", "SetConfig: device not open");
            return;
        }

        // configure our port
        // reset to UART mode for 232 devices
        ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);

        ftDev.setBaudRate(baud);

        switch (dataBits) {
            case 7:
                dataBits = D2xxManager.FT_DATA_BITS_7;
                break;
            case 8:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
            default:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
        }

        switch (stopBits) {
            case 1:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
            case 2:
                stopBits = D2xxManager.FT_STOP_BITS_2;
                break;
            default:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
        }

        switch (parity) {
            case 0:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
            case 1:
                parity = D2xxManager.FT_PARITY_ODD;
                break;
            case 2:
                parity = D2xxManager.FT_PARITY_EVEN;
                break;
            case 3:
                parity = D2xxManager.FT_PARITY_MARK;
                break;
            case 4:
                parity = D2xxManager.FT_PARITY_SPACE;
                break;
            default:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
        }

        ftDev.setDataCharacteristics(dataBits, stopBits, parity);

        short flowCtrlSetting;
        switch (flowControl) {
            case 0:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
            case 1:
                flowCtrlSetting = D2xxManager.FT_FLOW_RTS_CTS;
                break;
            case 2:
                flowCtrlSetting = D2xxManager.FT_FLOW_DTR_DSR;
                break;
            case 3:
                flowCtrlSetting = D2xxManager.FT_FLOW_XON_XOFF;
                break;
            default:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
        }

        // TODO : flow ctrl: XOFF/XOM
        // TODO : flow ctrl: XOFF/XOM
        ftDev.setFlowControl(flowCtrlSetting, (byte) 0x0b, (byte) 0x0d);

        uart_configured = true;
        Utils.showToast(DeviceUARTContext, "设置成功");
    }

    public void SendMessage(String message) {
        if (ftDev.isOpen() == false) {
            Log.e("j2xx", "SendMessage: device not open");
            return;
        }

        ftDev.setLatencyTimer((byte) 16);
        // ftDev.purge((byte) (D2xxManager.FT_PURGE_TX |
        // D2xxManager.FT_PURGE_RX));

        String writeData = message;
        byte[] OutData = writeData.getBytes();
        ftDev.write(OutData, writeData.length());
    }

    /**
     * 写入数据，如果hex是16则告知下位机是16进制
     *
     * @param message
     * @param i       为16
     */
    public void SendMessage(String message, int hex) {
        if (ftDev.isOpen() == false) {
            Log.e("j2xx", "SendMessage: device not open");
            return;
        }

        ftDev.setLatencyTimer((byte) 16);
        // ftDev.purge((byte) (D2xxManager.FT_PURGE_TX |
        // D2xxManager.FT_PURGE_RX));

        String writeData;

        if (hex == 16) {
            writeData = "[" + message + "]";
        } else {
            writeData = message;
        }
        byte[] OutData = writeData.getBytes();
        ftDev.write(OutData, writeData.length());
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (iavailable > 0) {
                mMessageListener.onMessage(String.copyValueOf(readDataToText, 0, iavailable));
            }
        }
    };

    private class readThread extends Thread {
        Handler mHandler;

        readThread(Handler h) {
            mHandler = h;
            this.setPriority(Thread.MIN_PRIORITY);
        }

        @Override
        public void run() {
            int i;

            while (true == bReadThreadGoing) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }

                synchronized (ftDev) {
                    iavailable = ftDev.getQueueStatus();
                    if (iavailable > 0) {

                        if (iavailable > readLength) {
                            iavailable = readLength;
                        }

                        ftDev.read(readData, iavailable);
                        for (i = 0; i < iavailable; i++) {
                            readDataToText[i] = (char) readData[i];
                        }
                        Message msg = mHandler.obtainMessage();
                        mHandler.sendMessage(msg);
                    }
                }
            }
        }

    }
}
