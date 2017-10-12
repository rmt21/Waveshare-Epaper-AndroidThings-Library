
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.Image;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

/*
 * Created by Reece on 28/08/2017
 */

public class EpdGpioSetup {

    private static int xDot = 128;
    private static int yDot = 296;
    private static long DELAYTIME = 5;
    private static int SPI_MAX_SPEED = 2000000;

    private static int rst = 17;
    private static int dc= 25;
    private static int busy = 24;
    private static int cs = 8;

    public static Gpio RST, DC, BUSY, CS, NEXT, PREV, ENTER;
    SpiDevice device;
    PeripheralManagerService service;

    Paint paint;
    Bitmap preImage, image;
    Canvas canvas;
    Matrix matrix;

    private final static byte[] LUTDefault_full = new byte[] {(byte)0x32,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x11,(byte)0x12,(byte)0x12,(byte)0x22,(byte)0x22,(byte)0x66,(byte)0x69,(byte)0x69,(byte)0x59,(byte)0x58,(byte)0x99,(byte)0x99,(byte)0x88,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0xF8,(byte)0xB4,(byte)0x13,(byte)0x51,(byte)0x35,(byte)0x51,(byte)0x51,(byte)0x19,(byte)0x01,(byte)0x00};
    private final static byte[] LUTDefault_part = new byte[] {(byte)0x32,(byte)0x10,(byte)0x18,(byte)0x18,(byte)0x08,(byte)0x18,(byte)0x18,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x13,(byte)0x14,(byte)0x44,(byte)0x12,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
    private final static byte[] GDOControl = new byte[] {(byte)0x01,(byte)((yDot-1)%256),(byte)((yDot-1)/256),(byte)0x00};
    private final static byte[] softstart = new byte[] {(byte)0x0c,(byte)0xd7,(byte)0xd6,(byte)0x9d};
    private final static byte[] VCOMVol = new byte[] {(byte)0x2c, (byte)0xa8};
    private final static byte[] DummyLine = new byte[] {(byte)0x3a, (byte)0x1a};
    private final static byte[] Gatetime = new byte[] {(byte)0x3b,(byte) 0x08};
    private final static byte[] RamDataEntryMode = new byte[] {(byte)0x11, (byte)0x01};

    private static byte[] progress_head = new byte[] {
            0x00,0x00,0x00,0x00,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC};
    private static byte[] progress_zero = new byte[] {
            0x00,0x00,0x00,0x00,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,
            0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,
    };
    private static byte[] progress_start = new byte[] {
            0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,
            0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,
    };
    private static byte[] progress_Spare = new byte[] {
            0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,
            0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC
    };
    private static byte[] progress_full = new byte[] {
            0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,
            0x30,0x0C,0x30,0x0C,0x30,0x0C,0x30,0x0C,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x00,0x00,0x00,0x00,
    };
    private static byte[] progress_end = new byte[] {
            0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,
            0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x3F,(byte)0xFC,0x00,0x00,0x00,0x00,
    };

    public EpdPhysical()
    {
        this.service = new PeripheralManagerService();
        try {
            //  List<String> spibus =  service.getSpiBusList();
            //List<String> gpiolist =  service.getGpioList();
            RST = service.openGpio("BCM17");
            DC = service.openGpio("BCM25");
            BUSY = service.openGpio("BCM24");
            DC.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            DC.setActiveType(Gpio.ACTIVE_HIGH);
            RST.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            RST.setActiveType(Gpio.ACTIVE_HIGH);
            BUSY.setDirection(Gpio.DIRECTION_IN);
            BUSY.setActiveType(Gpio.ACTIVE_HIGH);

            NEXT = service.openGpio("BCM27");
            NEXT.setDirection(Gpio.DIRECTION_IN);
            NEXT.setEdgeTriggerType(Gpio.EDGE_FALLING);
            PREV = service.openGpio("BCM22");
            PREV.setDirection(Gpio.DIRECTION_IN);
            PREV.setEdgeTriggerType(Gpio.EDGE_FALLING);
            ENTER = service.openGpio("BCM23");
            ENTER.setDirection(Gpio.DIRECTION_IN);
            ENTER.setEdgeTriggerType(Gpio.EDGE_FALLING);

            while(device == null)
            {
                device = getDevice();
            }
            bitmapImage();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void bitmapImage()
    {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        image = Bitmap.createBitmap(128, 296, Bitmap.Config.ARGB_8888);
        matrix = new Matrix();
        matrix.postScale(-1, 1, image.getWidth() / 2, image.getHeight() / 2);
        canvas = new Canvas(image);
        canvas.drawColor(Color.WHITE);
    }

    private SpiDevice getDevice()
    {
        try {
            SpiDevice dev = service.openSpiDevice("SPI0.0");
            dev.setMode(SpiDevice.MODE0);
            dev.setFrequency(2000000);
            dev.setBitsPerWord(8);
            Thread.sleep(DELAYTIME);
            return dev;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void resetAll()
    {

        try {
            RST.setValue(false);
            sleep();

            if (RST.getValue() == false)
            {
                Log.d("SYSTEM MESSAGE", "RESET COMPLETE");
            }
            else
            {
                Log.d("SYSTEM MESSAGE", "RESET FAILED");
            }
            RST.setValue(true);
            //sleep();
            //set register
            Log.d("SYSTEM MESSAGE", "SET REGISTER");
            write(GDOControl); //panel config
            write(softstart); //x+ y-
            write(VCOMVol); //vcom set
            write(DummyLine); // dummy line per gate
            write(Gatetime); // gate time setting
            write(RamDataEntryMode); //x+ y-
            setRamArea(0x00,(xDot-1)/8,(yDot-1)%256,(yDot-1)/256,0x00,0x00);
            setRamPointer((byte)0x00,(byte)((yDot-1)%256),(byte)((yDot-1)/256));
            Log.d("SYSTEM MESSAGE", "SET REGISTER END");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean readBusy()
    {
        try {
            for (int i = 0; i < 400; i++) {
                if (BUSY.getValue() == false) {
                    Log.d("SYS MESSAGE", "continue");
                    return true;
                }
                else
                {
                    Log.d("SYS MESSAGE", "busy");
                }
                Thread.sleep(DELAYTIME);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void writeCMD(byte command)
    {
        try {
            DC.setValue(false);
            byte[] buffer = new byte[]{command};
            device.write(buffer, buffer.length);
            Log.d("WRITECMD", String.valueOf(buffer[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeCMD_pl(byte command, byte para)
    {
        try {
            readBusy();
            DC.setValue(false);
            byte[] buffer = new byte[]{command};
            device.write(buffer, buffer.length);
            Log.d("WRITECMDPL", String.valueOf(buffer[0]));
            DC.setValue(true);
            buffer = new byte[]{para};
            device.write(buffer, buffer.length);
            Log.d("WRITECMDPL", String.valueOf(buffer[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void powerOn()
    {
        writeCMD_pl((byte)0x22, (byte)0xc0);
        writeCMD((byte)0x20);
    }

    private void write(byte[] value)
    {
        try {
            DC.setValue(false);
            byte[] buffer = new byte[]{value[0]};
            device.write(buffer, buffer.length);
            Log.d("WRITE", String.valueOf(buffer[0]));
            DC.setValue(true);

            buffer = new byte[value.length-1];
            for (int i=0; i< value.length-1; i++ )
            {
                buffer[i] =value[i+1];
                Log.d("WRITE", String.valueOf(buffer[i]));
            }

            device.write(buffer, buffer.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeDispRam(int xSize, int ySize, byte[] dispBuff, int xStart, int xEnd, int yStart, int yEnd)
    {
        if ((xSize%8) != 0)
        {
            xSize = xSize+(8-xSize%8);
        }

        xSize = xSize/8;

        int num = 0;
        readBusy();
        try {
            DC.setValue(false);
            byte[] buffer = new byte[]{(byte)0x24};
            device.write(buffer, buffer.length);
            Log.d("WRITERAM", String.valueOf(buffer[0]));
            DC.setValue(true);
            for (int i=0; i< xSize; i++)
            {
                for (int ii=0; ii< ySize; ii++)
                {
                    if (num < dispBuff.length) {
                        buffer = new byte[]{dispBuff[num]};
                        device.write(buffer, buffer.length);
                        num++;
                    }
                }
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void writeDispRamMono(int xSize, int ySize, byte[] dispBuff, int xStart, int xEnd, int yStart, int yEnd)
    {
        if ((xSize%8) != 0)
        {
            xSize = xSize+(8-xSize%8);
        }
        readBusy();
        try {
            DC.setValue(false);
            byte[] buffer = new byte[]{(byte)0x24};
            device.write(buffer, buffer.length);
            Log.d("WRITEMONO", String.valueOf(buffer[0]));
            DC.setValue(true);
            device.write(dispBuff, dispBuff.length);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setRamArea(int xStart, int xEnd, int yStart, int yStart1, int yEnd, int yEnd1)
    {
        byte[] ramAreaX = new byte[3];
        ramAreaX[0] = (byte)0x44;
        ramAreaX[1] = (byte)xStart;
        ramAreaX[2] = (byte)xEnd;
        byte[] ramAreaY = new byte[5];
        ramAreaY[0] = (byte)0x45;
        ramAreaY[1] = (byte)yStart;
        ramAreaY[2] = (byte)yStart1;
        ramAreaY[3] = (byte) yEnd;
        ramAreaY[4] = (byte) yEnd1;

        write(ramAreaX);
        write(ramAreaY);
    }

    private void setRamPointer(int addrX, int addrY, int addrY1)
    {
        byte[] ramPointerX = new byte[2];
        ramPointerX[0] = (byte)0x4e;
        ramPointerX[1] = (byte)addrX;
        byte[] ramPointerY = new byte[3];
        ramPointerY[0] = (byte)0x4f;
        ramPointerY[1] = (byte)addrY;
        ramPointerY[2] =(byte) addrY1;

        write(ramPointerX);
        write(ramPointerY);
    }

    private void partDisplay(int ramXST,int ramXEND,int ramYST,int ramYST1,int ramYEND,int ramYEND1)
    {
        setRamArea(ramXST, ramXEND, ramYST, ramYST1, ramYEND, ramYEND1);
        setRamPointer(ramXST, ramYST, ramYST1);
    }

    private void update()
    {
        writeCMD_pl((byte)0x22, (byte)0xc7);
        writeCMD((byte)0x20);
        writeCMD((byte)0xff);
    }

    private void updatePart()
    {
        writeCMD_pl((byte)0x22, (byte)0x04);
        writeCMD((byte)0x20);
        writeCMD((byte)0xff);
    }

    private void epdFull()
    {
        resetAll();
        write(LUTDefault_full);
        powerOn();
    }

    private void epdPart()
    {
        resetAll();
        write(LUTDefault_part);
        powerOn();
    }

    private void dispFull(byte[] dispBuffer, int label)
    {
        setRamPointer(0,((yDot-1)%256),((yDot-1)/256));

        if (label ==0)
        {

            writeDispRamMono(xDot, yDot, dispBuffer, 0, xDot, 0, yDot);
        }
        else
        {
            writeDispRam(xDot, yDot, dispBuffer, 0, xDot, 0, yDot);
        }
        update();
    }

    private void dispPart(int xStart, int xEnd, int yStart, int yEnd, byte[] dispBuffer, int label)
    {
        if (label ==0)
        {
            partDisplay((xStart/8),(xEnd/8),(yEnd%256),(yEnd/256),(yStart%256),(yStart/256));
            byte[] buffer = new byte[]{dispBuffer[0]};
            writeDispRamMono(xEnd-xStart, yEnd-yStart+1, buffer, xStart, xEnd, yStart, yEnd);
            // sleep();
            partDisplay((xStart/8),(xEnd/8),(yEnd%256),(yEnd/256),(yStart%256),(yStart/256));
            writeDispRamMono(xEnd-xStart, yEnd-yStart+1, buffer, xStart, xEnd, yStart, yEnd);
        }
        else
        {
            partDisplay((xStart/8),(xEnd/8),(yEnd%256),(yEnd/256),(yStart%256),(yStart/256));
            writeDispRam(xEnd-xStart, yEnd-yStart+1, dispBuffer, xStart, xEnd, yStart, yEnd);
            updatePart();
            // sleep();
            partDisplay((xStart/8),(xEnd/8),(yEnd%256),(yEnd/256),(yStart%256),(yStart/256));
            writeDispRam(xEnd-xStart, yEnd-yStart+1, dispBuffer, xStart, xEnd, yStart, yEnd);
        }
    }


    // public app layer

    //clear full screen

    public void dispClearFull()
    {
        Log.d("DISPLAY UPDATE", "CLEAR");
        epdFull();
        // sleep();
        byte[] buffer = new byte[]{(byte)0xff};
        dispFull(buffer, 0);
        // sleep();
    }

    public void dispClearPart()
    {
        Log.d("DISPLAY UPDATE", "CLEAR PART");
        epdPart();
        try {
            Thread.sleep(DELAYTIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        byte[] buffer = new byte[]{(byte)0xff};
        dispPart(0,xDot-1,0,yDot-1,buffer,0);
        try {
            Thread.sleep(DELAYTIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void dispClearFullPic(byte[] dispBuffer)
    {
        dispFull(dispBuffer, 1);
    }

    public void dispClearPartPic(int xStart, int xEnd, int yStart, int yEnd, byte dispBuffer)
    {
        byte[] buffer = new byte[]{dispBuffer};
        dispPart(xStart,xEnd,yStart,yEnd, buffer,1);
    }

    // character size
    public void dispCharSize(char data, char size, char mode, char next)
    {

    }

    // write string to disp
    public void dispWriteString(int x, int y, String input, int size)
    {
        canvas.drawText(input,x,y, paint);
        canvas.drawBitmap(image, matrix, paint);
        int arraySize = image.getByteCount();
        int[] pixels = new int[arraySize];
        image.getPixels(pixels, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
        dispFull(imageConvert(pixels),1);
    }


    public byte[] imageConvert(int[] input)
    {

        byte[] convert = new byte[(input.length)/8];

        int count =0;

        String bin = "";

        for (int i=0; i< input.length/8; i++)
        {
            for (int ii=0; ii< 8; ii++)
            {
                if (input[count] == -1)
                {
                    bin = bin+ "1";
                }
                else
                {
                    bin = bin+ "0";
                }
                count++;
            }
            int conv = Integer.valueOf(bin,2);
            convert[i] = (byte) conv;
            bin = "";
        }
        return convert;

    }
    // draw picture
    public void dispDrawPicture(int x, int y, Bitmap input, int xSize, int ySize)
    {
        canvas.drawBitmap(input, matrix, paint);
        int arraySize = input.getByteCount();
        int[] pixels = new int[arraySize];
        image.getPixels(pixels, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
        dispFull(imageConvert(pixels),1);
    }

    public void dispDrawPartPicture(int x, int y, Bitmap input, int xSize, int ySize)
    {
        Bitmap pImage = Bitmap.createBitmap(xSize, ySize, Bitmap.Config.ARGB_8888);
        Canvas pCanvas = new Canvas(pImage);
        pCanvas.drawColor(Color.WHITE);
        Rect src = new Rect(x, y, (x+xSize), (y+ySize));
        Rect dest = new Rect(0, 0, pImage.getWidth(), pImage.getHeight());
        pCanvas.drawBitmap(input, src, dest, paint);
        int arraySize = pImage.getByteCount();
        int[] pixels = new int[arraySize];
        pImage.getPixels(pixels, 0, pImage.getWidth(), 0, 0, pImage.getWidth(), pImage.getHeight());
        //dispFull(imageConvert(pixels),1);
        dispPart(x, (x+pImage.getWidth()), y, (y+pImage.getHeight()), imageConvert(pixels), 1);
    }


    public void sleep()
    {
        try {
            Thread.sleep(DELAYTIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Bitmap compress(Bitmap input)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        input.compress(Bitmap.CompressFormat.JPEG, 10, stream);
        byte[] byteArray = stream.toByteArray();
        Bitmap output = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        return output;
    }
}

