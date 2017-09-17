# iot-waveshare-epd

Conversion of python waveshare library for their epaper display into java, specifically for androidthings. This library will allow you to write your android layout directly to the e-paper display.

# Usage

The driver appears to write from the top left down, which means your android layout file will need to abide by this. You will need to input your screen size in relation to what is stated ont he waveshare website into xDot and yDot values in the class.

In your activity you will need to use code such as follows;

        EPAPER egItems = new EPAPER();
        //.........
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.activity_main,(ViewGroup)findViewById(R.id.main_layout));
        layout.setDrawingCacheEnabled(true);
        layout.buildDrawingCache();
        layout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        layout.layout(0, 0, layout.getMeasuredWidth(), layout.getMeasuredHeight());
        Bitmap toEPD = Bitmap.createBitmap(128, 296, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(toEPD);
        layout.draw(canvas);
        egItems.dispDrawPicture(0,0,toEPD,0,0);
        
        
        
# Notes

This is pretty much a straight conversion written from python to java by myself so is relatively messy and unoptimised, I imagine there would be better ways of doing a lot of the stuff along with the fact the code needs tidying up anyway, but for the moment this appears to work for what it needs to at the moment.
