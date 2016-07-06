package com.hadisi.usbhosttool.protocol;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;

import com.hadisi.usbhosttool.utils.Utils;

import java.io.IOException;

/**
 * Created by wugang on 2015/11/13.
 * <p>
 * CH34x芯片OTG通信
 */
public class CH34xConnection {

    public static String DEVICE_CH340 = "1a86:7523";
    public static String DEVICE_CH341 = "1a86:5523";
    private UsbManager mUsbmanager;
    private Context mContext;
    private String mString;
    private String mDeviceStr;

    private byte[] readBuffer;
    private byte[] writeBuffer;
    private int actualNumBytes;
    public boolean READ_ENABLE = false;
    private boolean UARTINITFLAG = false;

    private CH34xAndroidDriver uartInterface_ch34;
    private readThread_ch34x handlerThread_ch34x;

    protected final Object ThreadLock = new Object();

    private ICH34xConnection mMessageListener;

    public CH34xConnection(UsbManager manager, Context context, String AppName, String deviceStr) {
        mUsbmanager = manager;
        mContext = context;
        mString = AppName;
        mDeviceStr = deviceStr;

        /* allocate buffer */
        writeBuffer = new byte[512];
        readBuffer = new byte[9];

        uartInterface_ch34 = new CH34xAndroidDriver(mUsbmanager, mContext, mString, mDeviceStr);

        if (!READ_ENABLE) {
            READ_ENABLE = true;
            handlerThread_ch34x = new readThread_ch34x(handler);
            handlerThread_ch34x.start();
        }
    }

    public interface ICH34xConnection {
        /**
         * 硬件返回的数据
         *
         * @param message
         */
        void onMessage(String message);
    }

    public void setMessageCH34xListener(ICH34xConnection listener) {
        mMessageListener = listener;
    }

    /**
     * 打开设备
     */
    public void openUsbDevice() {
        if (uartInterface_ch34.isConnected()) {
            UARTINITFLAG = uartInterface_ch34.UartInit();
            Utils.showToast(mContext, "打开成功");
        } else {
            Utils.showToast(mContext, "打开失败，请检查是否连接设备");
        }
    }

    /**
     * 配置通信参数
     *
     * @param baudRate
     * @param dataBit
     * @param stopBit
     * @param parity
     * @param flowControl
     */
    public void setConfig(int baudRate, byte dataBit, byte stopBit, byte parity, byte flowControl) {
        if (UARTINITFLAG) {
            if (uartInterface_ch34.SetConfig(baudRate, dataBit, stopBit, parity, flowControl)) {
                Utils.showToast(mContext, "设置成功");
            } else {
                Utils.showToast(mContext, "设置失败，请检查是否连接设备");
            }
        } else {
            Utils.showToast(mContext, "设置失败，请检查是否连接设备");
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        if (uartInterface_ch34 != null) {
            if (uartInterface_ch34.isConnected()) {
                uartInterface_ch34.CloseDevice();
            }
            uartInterface_ch34 = null;
        }
    }

    public void stop() {
        if (READ_ENABLE = true)
            READ_ENABLE = false;
    }

    public void resume() {
        if (2 == uartInterface_ch34.ResumeUsbList()) {
            uartInterface_ch34.CloseDevice();
        }
    }

    /**
     * 写入数据
     *
     * @param message
     */
    public void writeMessage(String message) {
        int numBytes = 0;
        int mLen = 0;
        if (message.length() != 0) {
            numBytes = message.length();
            for (int i = 0; i < numBytes; i++) {
                writeBuffer[i] = (byte) message.charAt(i);
            }
        }
        try {
            mLen = uartInterface_ch34.WriteData(writeBuffer, numBytes);
        } catch (IOException e) {
            Utils.showToast(mContext, "写入数据错误");
            e.printStackTrace();
        }
        if (mLen != numBytes) {
            Utils.showToast(mContext, "写入数据错误");
        }
    }

    /**
     * 写入数据，如果hex是16则告知下位机是16进制
     *
     * @param message
     * @param
     */
    public void writeMessage(String message, int hex) {
        int numBytes = 0;
        int mLen = 0;
        if (hex != 16) {
            return;
        }
        numBytes = message.length();
        try {
            mLen = uartInterface_ch34.WriteData(hexStringToBytes(message), numBytes / 2);
        } catch (IOException e) {
            Utils.showToast(mContext, "写入数据错误");
            e.printStackTrace();
        }
        if (mLen != numBytes / 2) {
            Utils.showToast(mContext, "写入数据错误");
        }
    }

    /**
     * 写入zigbee参数，需要校验
     *
     * @param message
     * @param hex
     * @param isCheck
     */
    public void writeZigbeeMessage(String message, int hex, boolean isCheck) {
        int numBytes = 0;
        int mLen = 0;
        if (hex != 16) {
            return;
        }
        numBytes = message.length();
        try {
            if (isCheck)
                mLen = uartInterface_ch34.WriteData(zigbeeommandCheck(hexStringToBytes(message)), numBytes / 2 + 1);
            else
                mLen = uartInterface_ch34.WriteData(hexStringToBytes(message), numBytes / 2);
        } catch (IOException e) {
            Utils.showToast(mContext, "写入数据错误");
            e.printStackTrace();
        }
        if (mLen != numBytes / 2) {
            Utils.showToast(mContext, "写入数据错误");
        }
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (actualNumBytes != 0x00) {
//                        mMessageListener.onMessage(new String(readBuffer, 0,
//                                actualNumBytes));
                        mMessageListener.onMessage(bytesToHexString(readBuffer, actualNumBytes));
                        actualNumBytes = 0;
                    }
                    break;
            }


        }
    };

    /* usb input data handler */
    private class readThread_ch34x extends Thread {
        /* constructor */
        Handler mhandler;

        readThread_ch34x(Handler h) {
            mhandler = h;
            this.setPriority(Thread.MIN_PRIORITY);
        }

        public void run() {
            while (READ_ENABLE) {
                Message msg = mhandler.obtainMessage();
                msg.what = 0;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
//				Log.d(TAG, "Thread");
                synchronized (ThreadLock) {
                    if (uartInterface_ch34 != null) {
                        actualNumBytes = uartInterface_ch34.ReadData(readBuffer, 64);

                        if (actualNumBytes > 0) {
                            mhandler.sendMessage(msg);
                        }
                    }
                }
            }
        }
    }

    /**
     * byte数组相加取低八位
     *
     * @param olebytes
     * @return
     */
    public static byte[] zigbeeommandCheck(byte[] olebytes) {
        byte tem = 0;
        byte[] newbytes = new byte[olebytes.length + 1];
        for (int i = 0; i < olebytes.length; i++) {
            tem = (byte) (tem + olebytes[i]);
        }
        for (int j = 0; j < newbytes.length; j++) {
            if (j == olebytes.length)
                newbytes[j] = (byte) (tem & 0xff);
            else
                newbytes[j] = olebytes[j];
        }
        return newbytes;
    }

    /**
     * Convert hex string to byte[]
     *
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String bytesToHexString(byte[] src, int actualNumBytes) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || actualNumBytes <= 0) {
            return null;
        }
        for (int i = 0; i < actualNumBytes; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
