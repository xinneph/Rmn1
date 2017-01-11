package com.raymarine.wi_fish.rmn.rmn1;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;

public class SounderService extends Service {
    private static final String TAG = SounderService.class.getSimpleName();

    private IBinder serviceBinder;
    private SounderInterface sounderInterface;

    private final ISounder sounderImplementation = new ISounder() {
        public void connect() {
            sounderInterface.acquireMessage1();
            sounderInterface.acquireMessage2();
        }
        public void shutdown() {
            stopSelf();
        }
        public void print() {
            sounderInterface.printMessages();
        }
    };

    public static class SndBinder extends Binder {
        private final SounderService service;

        private SndBinder(SounderService service) {
            this.service = service;
        }

        public ISounder getISounder() {
            return service.sounderImplementation;
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "CREATING SERVICE");
        sounderInterface = new SounderInterface(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "DESTROYING SERVICE");
        if (sounderInterface != null) {
            sounderInterface.close();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        serviceBinder = new SndBinder(this);
        return serviceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        serviceBinder = null;
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}

interface ISounder {
    void connect();
    void shutdown();
    void print();
}

class SounderInterface {
    private final Message1 message1 = new Message1();
    private final Message2 message2 = new Message2();

    public SounderInterface(Context ctx) {}
    public void close() {
        MessageList.reset();
    }
    public void acquireMessage1() {
        MessageList.decodeMessage(1);
    }
    public void acquireMessage2() {
        MessageList.decodeMessage(2);
    }
    public void printMessages() {
        MessageList.printMessage(1);
        MessageList.printMessage(2);
    }
}

class MessageList {
    private static final String TAG = MessageList.class.getSimpleName();

    private static SparseArray<ISonarReceivedMessage> messageList = new SparseArray<>();
    private static ISonarReceivedMessage get(int id) {
        return messageList.get(id);
    }
    public static void register(ISonarReceivedMessage message) {
        messageList.put(message.getMessageId(), message);
    }
    public static void reset() {
        Log.d(TAG, "reset()");
        for (int i = 0; i < messageList.size(); ++i) {
            messageList.valueAt(i).reset();
        }
    }
    public static void decodeMessage(int id) {
        ISonarReceivedMessage message = get(id);
        message.decodeMessage();
    }
    public static void printMessage(int id) {
        Log.i(TAG, "printMessage(" + id + ")");
        ISonarReceivedMessage message = get(id);
        Log.i(TAG, message == null ? "null" : message.getValue());
    }
}

interface ISonarReceivedMessage {
    int getMessageId();
    String getValue();

    void reset();
    boolean decodeMessage();
}

class Message1 implements ISonarReceivedMessage {
    private boolean reset = true;

    public Message1() {
        MessageList.register(this);
    }
    @Override
    public int getMessageId() {
        return 1;
    }
    @Override
    public String getValue() {
        return "This is message 1. " + reset;
    }
    @Override
    public void reset() {
        reset = true;
    }
    @Override
    public boolean decodeMessage() {
        reset = false;
        return true;
    }
}

class Message2 implements ISonarReceivedMessage {
    private boolean reset = true;

    public Message2() {
        MessageList.register(this);
    }
    @Override
    public int getMessageId() {
        return 2;
    }
    @Override
    public String getValue() {
        return "A message 2. " + reset;
    }
    @Override
    public void reset() {
        reset = true;
    }
    @Override
    public boolean decodeMessage() {
        reset = false;
        return true;
    }
}