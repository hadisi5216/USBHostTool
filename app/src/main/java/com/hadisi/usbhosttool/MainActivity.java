package com.hadisi.usbhosttool;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.ftdi.j2xx.D2xxManager;
import com.hadisi.usbhosttool.adapter.CommandAdapter;
import com.hadisi.usbhosttool.myview.CustomEdiText;
import com.hadisi.usbhosttool.myview.DividerItemDecoration;
import com.hadisi.usbhosttool.protocol.CH34xConnection;
import com.hadisi.usbhosttool.protocol.FTxConnection;
import com.hadisi.usbhosttool.utils.Commands;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static String ACTION_USB_PERMISSION = "com.hadisi.usbhosttool.USB_PERMISSION";
    public static D2xxManager ftD2xx = null;

    private Toolbar toolbar;

    /* graphical objects */
    protected CustomEdiText readText;
    protected EditText writeText;
    protected Spinner baudSpinner;
    ;
    protected Spinner stopSpinner;
    protected Spinner dataSpinner;
    protected Spinner paritySpinner;
    protected Spinner flowSpinner;
    protected Spinner portSpinner;

    protected RecyclerView baseCommandList;
    protected RecyclerView commandList;

    protected Button configButton;
    protected Button openButton;
    protected Button writeButton;
    protected CheckBox vlaues16;

    /* local variables */
    protected int baudRate; /* baud rate */
    protected byte stopBit; /* 1:1stop bits, 2:2 stop bits */
    protected byte dataBit; /* 8:8bit, 7: 7bit */
    protected byte parity; /* 0: none, 1: odd, 2: even, 3: mark, 4: space */
    protected byte flowControl; /* 0:none, 1: flow control(CTS,RTS) */
    protected int portNumber; /* port number */

    protected int openIndex = 0;

    private CommandAdapter mBaseCommandAdapter;
    private CommandAdapter mCommandAdapter;

    private String[] commandsDis;

    private CH34xConnection mCH34xConnection;
    private FTxConnection mFTxConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            ftD2xx = D2xxManager.getInstance(this);
        } catch (D2xxManager.D2xxException ex) {
            ex.printStackTrace();
        }
        setContentView(R.layout.activity_main);
        SetupD2xxLibrary();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("USB TO CH340");
        setSupportActionBar(toolbar);

        initView();

        setAdapter();

        connectionCh34x();

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    private void SetupD2xxLibrary() {
        // TODO Auto-generated method stub
        if (!ftD2xx.setVIDPID(0x0403, 0xada1))
            Log.i("ftd2xx-java", "setVIDPID Error");
    }

    /**
     * 连接CH34x设备
     */
    private void connectionCh34x() {
        mCH34xConnection = new CH34xConnection((UsbManager) getSystemService(Context.USB_SERVICE), this, ACTION_USB_PERMISSION, CH34xConnection.DEVICE_CH340);
        mCH34xConnection.setMessageCH34xListener(new CH34xConnection.ICH34xConnection() {
            @Override
            public void onMessage(String message) {
                readText.append("-->RE：" + message + "\n");
            }
        });
    }

    /**
     * 连接FTx设备
     */
    private void connectionFTx() {
        mFTxConnection = new FTxConnection(this, ftD2xx);
        mFTxConnection.setMessageFTxListener(new FTxConnection.IFTxConnection() {
            @Override
            public void onMessage(String message) {
                readText.append("-->RE：" + message + "\n");
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mCH34xConnection != null)
            mCH34xConnection.resume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCH34xConnection != null)
            mCH34xConnection.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCH34xConnection != null)
            mCH34xConnection.release();
    }

    private void setAdapter() {
        mBaseCommandAdapter = new CommandAdapter(this, Commands.baseCommands);
        baseCommandList.setAdapter(mBaseCommandAdapter);
        baseCommandList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        baseCommandList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        commandsDis = Commands.commands_dis[0];
        showCommandList(0);
        commandList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        commandList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mBaseCommandAdapter.setSelectPosition(0);

        mBaseCommandAdapter.setOnItemClickListener(new CommandAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                mBaseCommandAdapter.setSelectPosition(postion);
                mBaseCommandAdapter.notifyDataSetChanged();

                showCommandList(postion);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    private void showCommandList(final int pos) {
        commandsDis = Commands.commands_dis[pos];
        mCommandAdapter = new CommandAdapter(getApplicationContext(), commandsDis);
        commandList.setAdapter(mCommandAdapter);
        mCommandAdapter.setOnItemClickListener(new CommandAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                writeText.setText(Commands.commands_com[pos][postion]);
                mCommandAdapter.setSelectPosition(postion);
                mCommandAdapter.notifyDataSetChanged();
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    private void initView() {
        openButton = (Button) findViewById(R.id.openButton);
        configButton = (Button) findViewById(R.id.configButton);
        writeButton = (Button) findViewById(R.id.WriteButton);
        vlaues16 = (CheckBox) findViewById(R.id.vlaues_16);
        openButton.setOnClickListener(this);
        configButton.setOnClickListener(this);
        writeButton.setOnClickListener(this);

        readText = (CustomEdiText) findViewById(R.id.ReadValues);
        // readText.setInputType(0);
        writeText = (EditText) findViewById(R.id.WriteValues);

        baseCommandList = (RecyclerView) findViewById(R.id.baseOrderList);
        commandList = (RecyclerView) findViewById(R.id.OrderList);

        baudSpinner = (Spinner) findViewById(R.id.baudRateValue);
        ArrayAdapter<CharSequence> baudAdapter = ArrayAdapter
                .createFromResource(this, R.array.baud_rate,
                        R.layout.spinner_textview);
        baudAdapter.setDropDownViewResource(R.layout.spinner_textview);
        baudSpinner.setAdapter(baudAdapter);
        baudSpinner.setSelection(4);
        /* by default it is 9600 */
        baudRate = 9600;

        stopSpinner = (Spinner) findViewById(R.id.stopBitValue);
        ArrayAdapter<CharSequence> stopAdapter = ArrayAdapter
                .createFromResource(this, R.array.stop_bits,
                        R.layout.spinner_textview);
        stopAdapter.setDropDownViewResource(R.layout.spinner_textview);
        stopSpinner.setAdapter(stopAdapter);
        /* default is stop bit 1 */
        stopBit = 1;

        dataSpinner = (Spinner) findViewById(R.id.dataBitValue);
        ArrayAdapter<CharSequence> dataAdapter = ArrayAdapter
                .createFromResource(this, R.array.data_bits,
                        R.layout.spinner_textview);
        dataAdapter.setDropDownViewResource(R.layout.spinner_textview);
        dataSpinner.setAdapter(dataAdapter);
        dataSpinner.setSelection(1);
        /* default data bit is 8 bit */
        dataBit = 8;

        paritySpinner = (Spinner) findViewById(R.id.parityValue);
        ArrayAdapter<CharSequence> parityAdapter = ArrayAdapter
                .createFromResource(this, R.array.parity,
                        R.layout.spinner_textview);
        parityAdapter.setDropDownViewResource(R.layout.spinner_textview);
        paritySpinner.setAdapter(parityAdapter);
        /* default is none */
        parity = 0;

        flowSpinner = (Spinner) findViewById(R.id.flowControlValue);
        ArrayAdapter<CharSequence> flowAdapter = ArrayAdapter
                .createFromResource(this, R.array.flow_control,
                        R.layout.spinner_textview);
        flowAdapter.setDropDownViewResource(R.layout.spinner_textview);
        flowSpinner.setAdapter(flowAdapter);
        /* default flow control is is none */
        flowControl = 0;

        portSpinner = (Spinner) findViewById(R.id.portValue);
        ArrayAdapter<CharSequence> portAdapter = ArrayAdapter.createFromResource(this,
                R.array.port_list_1, R.layout.spinner_textview);
        portAdapter.setDropDownViewResource(R.layout.spinner_textview);
        portSpinner.setAdapter(portAdapter);
        portNumber = 1;

		/* set the adapter listeners for baud */
        baudSpinner.setOnItemSelectedListener(new MyOnBaudSelectedListener());
        /* set the adapter listeners for stop bits */
        stopSpinner.setOnItemSelectedListener(new MyOnStopSelectedListener());
        /* set the adapter listeners for data bits */
        dataSpinner.setOnItemSelectedListener(new MyOnDataSelectedListener());
        /* set the adapter listeners for parity */
        paritySpinner
                .setOnItemSelectedListener(new MyOnParitySelectedListener());
        /* set the adapter listeners for flow control */
        flowSpinner.setOnItemSelectedListener(new MyOnFlowSelectedListener());
        /* set the adapter listeners for port number */
        portSpinner.setOnItemSelectedListener(new MyOnPortSelectedListener());
    }

    public class MyOnBaudSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            baudRate = Integer.parseInt(parent.getItemAtPosition(pos)
                    .toString());
        }

        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    public class MyOnStopSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            stopBit = (byte) Integer.parseInt(parent.getItemAtPosition(pos)
                    .toString());
        }

        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    public class MyOnDataSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            dataBit = (byte) Integer.parseInt(parent.getItemAtPosition(pos)
                    .toString());
        }

        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    public class MyOnParitySelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            String parityString = new String(parent.getItemAtPosition(pos)
                    .toString());
            if (parityString.compareTo("none") == 0) {
                parity = 0;
            } else if (parityString.compareTo("odd") == 0) {
                parity = 1;
            } else if (parityString.compareTo("even") == 0) {
                parity = 2;
            } else if (parityString.compareTo("mark") == 0) {
                parity = 3;
            } else if (parityString.compareTo("space") == 0) {
                parity = 4;
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    public class MyOnFlowSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            String flowString = new String(parent.getItemAtPosition(pos)
                    .toString());
            if (flowString.compareTo("none") == 0) {
                flowControl = 0;
            } else if (flowString.compareTo("CTS/RTS") == 0) {
                flowControl = 1;
            } else if (flowString.compareTo("DTR/DSR") == 0) {
                flowControl = 2;
            } else if (flowString.compareTo("XOFF/XON") == 0) {
                flowControl = 3;
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    public class MyOnPortSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            openIndex = Integer.parseInt(parent.getItemAtPosition(pos)
                    .toString()) - 1;
        }

        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_rs232) {
            if (mCH34xConnection != null) {
                mCH34xConnection.release();
                mCH34xConnection = null;
            }
            connectionFTx();
            toolbar.setTitle("USB TO RS232");
        }
        if (id == R.id.action_ch340) {
            if (mFTxConnection != null) {
                mFTxConnection.release();
                mFTxConnection = null;
            }
            connectionCh34x();
            toolbar.setTitle("USB TO CH340");
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openButton:
                if (mCH34xConnection != null)
                    mCH34xConnection.openUsbDevice();
                if (mFTxConnection != null) {
                    mFTxConnection.openUsbDevice();
                }
                break;
            case R.id.configButton:
                if (mCH34xConnection != null)
                    mCH34xConnection.setConfig(baudRate, dataBit, stopBit, parity, flowControl);
                if (mFTxConnection != null) {
                    mFTxConnection.setConfig(baudRate, dataBit, stopBit, parity, flowControl);
                }
                break;
            case R.id.WriteButton:
                if (mCH34xConnection != null)
                    if (vlaues16.isChecked())
//                        mCH34xConnection.writeMessage(writeText.getText().toString().trim(), 16);
                        mCH34xConnection.writeZigbeeMessage(writeText.getText().toString().trim(), 16, true);
                    else
                        mCH34xConnection.writeMessage(writeText.getText().toString().trim());
                if (mFTxConnection != null) {
                    if (vlaues16.isChecked())
                        mFTxConnection.writeMessage(writeText.getText().toString().trim(), 16);
                    else
                        mFTxConnection.writeMessage(writeText.getText().toString().trim());
                }
                readText.append("-->WR：" + writeText.getText().toString().trim() + "\n");
                break;
        }
    }
}
