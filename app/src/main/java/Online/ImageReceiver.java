package Online;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.widget.ImageView;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.trailerbackerupper.R;
import com.example.trailerbackerupperapp.MainActivity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ImageReceiver {
    MainActivity mActClass;
    public ImageReceiver(MainActivity mainActivity){
        mActClass = mainActivity;
    }
    public void start(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket(PORT, InetAddress.getByName("192.168.1.102"));
                    byte[] dat = new byte[0];
                    dumpBuffer(socket);

                    while (true) {
                        byte[] seg = new byte[MAX_DGRAM];
                        DatagramPacket packet = new DatagramPacket(seg, seg.length);
                        socket.receive(packet);

                        if ((seg[0] & 0xFF) > 1) {
                            dat = concatenateArrays(dat, copyOfRange(seg, 1, seg.length));
                        } else {
                            dat = concatenateArrays(dat, copyOfRange(seg, 1, seg.length));

                            // Decode the image using Android's BitmapFactory
                            ByteArrayInputStream bais = new ByteArrayInputStream(dat);
                            Bitmap image = BitmapFactory.decodeStream(bais);

                            // Update UI with the received image
                            mActClass.updateTrailerView(image);
                            // Reset the buffer
                            dat = new byte[0];
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private static final int PORT = 25565;
    private static final int MAX_DGRAM = 65536;

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
