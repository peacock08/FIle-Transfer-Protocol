package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import data.DataFile;
import data.SEND_TYPE;

public class ClientSocketThread extends Thread {

    private Socket socket;
    private boolean isStop = false;


    InputStream is;
    ISocketListener iSocketListener;

    OutputStream os;
    SEND_TYPE sendType = SEND_TYPE.DO_NOT_SEND;
    String message;
    String fileName;

    DataFile dataFile;
    private long fileSize;
    private String fileNameReceived;
    private long currentSize;
    DataFile m_dtf;

    public ClientSocketThread(ISocketListener iSocketListener) throws Exception {
        this.iSocketListener = iSocketListener;
        m_dtf = new DataFile();
    }

    public void setSocket(String serverIp, int port) {
        try {

            socket = new Socket(serverIp, port);

            System.out.println("Connected: " + socket);

            os = socket.getOutputStream();
            is = socket.getInputStream();

            iSocketListener.showDialog("CONNECTED TO SERVER", "INFOR");
            SendDataThread sendDataThread = new SendDataThread();
            sendDataThread.start();
        } catch (Exception e) {

            System.out.println("Can't connect to server");
            iSocketListener.showDialog("Can't connect to Server", "ERROR");
        }
    }

    @Override
    public void run() {

        while (!isStop) {
            try {
                    readData();

            } catch (Exception e) {

                connectServerFail();
                e.printStackTrace();
                break;
            }
        }
        closeSocket();
    }
    void readString(Object obj) throws Exception {
        String str = obj.toString();
        if (str.equals("STOP"))
            isStop = true;
        else if (str.contains("START_SEND_FILE")) {
            this.sendType = SEND_TYPE.START_SEND_FILE;
        } else if (str.contains("SEND_FILE")) {
            String[] fileInfor = str.split("--");
            fileNameReceived = fileInfor[1];
            fileSize = Integer.parseInt(fileInfor[2]);
            System.out.println("File Size: " + fileSize);
            currentSize = 0;
            m_dtf.clear();
            this.sendString("START_SEND_FILE");
        } else if (str.contains("END_FILE")) {
            iSocketListener.chooserFileToSave(m_dtf);
        } else if (str.contains("ALL_FILE")) {
            String[] listFile = str.split("--");
            String[] yourArray = Arrays.copyOfRange(listFile, 1, listFile.length);
            iSocketListener.updateListFile(yourArray);
        } else if (str.contains("ERROR")) {
            String[] list = str.split("--");
            iSocketListener.showDialog(list[1], "ERROR");
        }
    }


    void readData() throws Exception {
        try {
            System.out.println("Recieving...");
            ObjectInputStream ois = new ObjectInputStream(is);
            Object obj = ois.readObject();

            if (obj instanceof String) {
                    readString(obj);

            } else if (obj instanceof DataFile) {
                    readFile(obj);
            }
        } catch (Exception e) {

            e.printStackTrace();
            connectServerFail();
            closeSocket();
        }
    }


    void readFile(Object obj) throws Exception {
        DataFile dtf = (DataFile) obj;
        currentSize += 512;

        int percent = (int) (currentSize * 100 / fileSize);

        m_dtf.appendByte(dtf.data);
        iSocketListener.setProgress(percent);
    }

    class SendDataThread extends Thread {
        @Override
        public void run() {

            while (!isStop) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
                if (sendType != SEND_TYPE.DO_NOT_SEND)
                    sendData();
            }
        }
    }

    private void sendData() {

            if (sendType == SEND_TYPE.SEND_STRING) {
                sendMessage(message);
            } else if (sendType == SEND_TYPE.SEND_FILE) {
                File source = new File(fileName);
                InputStream fin;
                try {
                    fin = new FileInputStream(source);
                    long lenghtOfFile = source.length();

                    sendMessage("SEND_FILE" + "--" + fileName + "--" + lenghtOfFile);
                    fin.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (sendType == SEND_TYPE.START_SEND_FILE) {
                File source = new File(fileName);
                InputStream fin = null;
                long lenghtOfFile = source.length();

                byte[] buf = new byte[512];
                long total = 0;
                int len;
                try {
                    fin = new FileInputStream(source);
                    while ((len = fin.read(buf)) != -1) {
                        total += len;
                        DataFile dtf = new DataFile();
                        dtf.data = buf;
                        sendMessage(dtf);
                        iSocketListener.setProgress((int) (total * 100 / lenghtOfFile));
                    }
                } catch (Exception e) {

                        e.printStackTrace();
                }

                sendMessage("END_FILE--" + fileName + "--" + lenghtOfFile);

            }

            sendType = SEND_TYPE.DO_NOT_SEND;
    }

    void sendString(String str) {
            System.out.println("SENDING STRING	" + str);
            sendType = SEND_TYPE.SEND_STRING;
            message = str;
    }

    void sendFile(String fileName) {
            System.out.println("SENDING FILE	");
            sendType = SEND_TYPE.SEND_FILE;
            this.fileName = fileName;
    }

    public synchronized void sendMessage(Object obj) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(os);

            if (obj instanceof String) {
                String message = obj.toString();
                oos.writeObject(message);
                oos.flush();
                //The flush() method flushes the data of one stream and sends it to another stream. 
                //It is required if you have connected one stream to another stream.
            }

            else if (obj instanceof DataFile) {
                oos.writeObject(obj);
                oos.flush();
            }
        } catch (Exception e) {

        }
    }

    private void connectServerFail() {
            iSocketListener.showDialog("Can't connect to Server", "ERROR");
            isStop = true;
            closeSocket();
    }

    public void closeSocket() {
        isStop = true;
        try {
            this.sendString("STOP");
            if (is != null)
                is.close();
            if (os != null)
                os.close();
            if (socket != null)
                socket.close();
            iSocketListener.showDialog("Closed socket", "INFOR");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
