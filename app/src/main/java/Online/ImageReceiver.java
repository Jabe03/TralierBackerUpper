package Online;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
//import android.os.Bundle;
//import android.widget.ImageView;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.trailerbackerupper.R;
import com.example.trailerbackerupperapp.MainActivity;
import com.example.trailerbackerupperapp.customwidgets.DebugLayout;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ImageReceiver {
    private static final int PORT = 25565;
    private static final int MAX_DGRAM = 65536;

    public static int count = 0;

    public static byte[] lastPacket = new byte[0];
    MainActivity act;
    public ImageReceiver(MainActivity mainActivity){
        act = mainActivity;
    }
    public void start(){
        act.updateTrailerView(act.tempGetBitMapOfTrailer());
        Log.d("ImageReceiver", "Starting");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket(null);
                    socket.bind(new InetSocketAddress(PORT));
                    Log.d("ImageReceiver", "Opened socket");
                    Log.d("ImageReceiver", "Socket address: " + socket.getInetAddress());
                    byte[] dat = new byte[0];
                    dumpBuffer(socket);
                    //Log.d("ImageReceiver", "dumped buffer");

                    while (true) {
                        if(DebugLayout.debug){
                            Thread.sleep(300);
                        }
                        else {
                            count++;
                            Log.d("ImageReceiver", "looping");

                            byte[] seg = new byte[MAX_DGRAM];
                            DatagramPacket packet = new DatagramPacket(seg, seg.length);
                            Log.d("ImageReceiver", "Waiting for a packet...");
                            socket.receive(packet);
                            Log.d("ImageReceiver", "Packet received, processing");

                            if ((seg[0] & 0xFF) > 1) {
                                dat = concatenateArrays(dat, copyOfRange(seg, 1, seg.length));
                            } else {
                                dat = concatenateArrays(dat, copyOfRange(seg, 1, seg.length));
                                lastPacket = dat;
                                // Decode the image using Android's BitmapFactory
                                ByteArrayInputStream bais = new ByteArrayInputStream(dat);
                                Bitmap image = BitmapFactory.decodeStream(bais);

                                // Update UI with the received image
                                Log.d("ImageReceiver", "Updating image on MainActivity");
                                act.updateTrailerView(image);
                                // Reset the buffer
                                dat = new byte[0];

                            }
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }


    private void dumpBuffer(DatagramSocket socket) throws IOException {
        // Emptying buffer frame.
        while (true) {
            byte[] seg = new byte[MAX_DGRAM];
            DatagramPacket packet = new DatagramPacket(seg, seg.length);
            socket.receive(packet);
            System.out.println(seg[0]);
            if ((seg[0] & 0xFF) == 1) {
                System.out.println("Finish emptying buffer.");
                break;
            }
        }
    }

    private static byte[] concatenateArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    private static byte[] copyOfRange(byte[] original, int from, int to) {
        int length = to - from;
        byte[] copy = new byte[length];
        System.arraycopy(original, from, copy, 0, Math.min(length, original.length - from));
        return copy;
    }
}
