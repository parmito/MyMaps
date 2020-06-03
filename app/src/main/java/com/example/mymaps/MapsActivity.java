package com.example.mymaps;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    Button btnInitialDatePicker, btnFinalDatePicker;
    private int mInitialYear, mInitialMonth, mInitialDay;
    private int mFinalYear, mFinalMonth, mFinalDay;
    private String strInitialDate = new String();
    private String strFinalDate = new String();
    private static boolean boLastIgnitionState = false;


    final private String strEsnFiatIdea = "B4E62DE9567D";
    final private String strEsnCitroen = "3C71BF9DA1FC";
    private String strEsn = strEsnFiatIdea;

    Timer timer;
    TimerTask timerTask;
    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();

    private static final int COLOR_BLACK = 0xFF000000;
    private static final int COLOR_BLUE = 0xFF0000FF;
    private static final int COLOR_GREEN = 0xFF00FF00;
    private static final int COLOR_RED = 0xFFFF0000;
    private static final int COLOR_CYAN = 0xFF00FFFF;
    private static final int COLOR_PINK = 0xFFFF00FF;
    private static final int COLOR_PURPLE = 0xFF800080;
    private static final int COLOR_MAGENTA =0XFF808000;

    private static int[] iColor = { COLOR_CYAN,
            COLOR_RED,
            COLOR_BLUE,
            COLOR_GREEN,
            COLOR_PINK,
            COLOR_PURPLE,
            COLOR_MAGENTA,
            COLOR_BLACK};

    ArrayList<String> latitude = new ArrayList<>();
    ArrayList<String> longitude = new ArrayList<>();
    ArrayList<String> altitude = new ArrayList<>();
    ArrayList<String> ignicao = new ArrayList<>();

    List<LatLng> mapPoints = new ArrayList<LatLng>();
    private ProgressDialog mProgressDialog;

    protected void SetInitialDate(String Date)
    {
        this.strInitialDate = Date;
    }
    protected void SetFinalDate(String Date)
    {
        this.strFinalDate = Date;
    }
    protected String GetInitialDate()
    {
        return(this.strInitialDate);
    }
    protected String GetFinalDate()
    {
        return(this.strFinalDate);
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 1000ms the TimerTask will run every 30000ms
        timer.schedule(timerTask, 10000, 30000); //
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    public void run() {
                        if(false) {
                            //get the current timeStamp
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss a");
                            final String strDate = simpleDateFormat.format(calendar.getTime());

                            //show the toast
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(getApplicationContext(), strDate, duration);
                            toast.show();
                        }

                        final Calendar c = Calendar.getInstance();
                        mInitialYear = c.get(Calendar.YEAR);
                        mInitialMonth = c.get(Calendar.MONTH);
                        mInitialDay = c.get(Calendar.DAY_OF_MONTH) ;


                        String strCurrentDate = (mInitialMonth+1)+"/"+mInitialDay+"/"+mInitialYear;
                        //Some url endpoint that you may have
                        /*String myUrl = "http://gpslogger.esy.es/pages/upload/CreateMarkerCitrixJson.php?esn=B4E62DE9567D&fromdate=3/11/2020%2000:00:00&todate=3/11/2020%2023:59:59";*/   //String to place our result in
                        String myUrl = new String("http://gpslogger.esy.es/pages/upload/CreateMarkerCitrixJson.php?esn="+strEsn+"&fromdate="+strCurrentDate+"%2000:00:00&todate="+strCurrentDate+"%2023:59:59");
                        String result;   //Instantiate new instance of our class
                        String route;
                        HttpGetRequest getRequest = new HttpGetRequest();   //Perform the doInBackground method, passing in our url

                        mMap.clear();            /* get JSONObject from JSON file*/
                        mapPoints.clear();

                        mProgressDialog.setTitle("Route");
                        mProgressDialog.setMessage("Please wait, calculating");
                        mProgressDialog.show();

                        getRequest.setListener(new OnCompleteListener() {
                            @Override
                            public void OnComplete(String string) {
                                Log.i("ROUTE", string);

                                /* get JSONObject from JSON file*/


                                double x,y,xPrevious, yPrevious;
                                int ignicao;

                                String  strId,
                                        strNsat,
                                        strHdop,
                                        strLat,
                                        strLongi,
                                        strAltitude,
                                        strCourse,
                                        strSpeed,
                                        strIgnicao,
                                        strDatedev,
                                        strDate,
                                        strVoltage,
                                        strTemperature,
                                        strOdometer,
                                        strDistance;

                                x = 0;
                                y = 0;
                                xPrevious = 0;
                                yPrevious = 0;
                                try {
                                    JSONObject obj = new JSONObject(string);
                                    JSONArray routeArray = obj.getJSONArray("route");
                                    // implement for loop for getting users list data

                                    int iIndexColor = 0;

                                    if(routeArray.length() > 0) {
                                        /*for (int i = 0; i < routeArray.length(); i++) {*/

                                        int i = routeArray.length() -1;
                                        JSONObject route = routeArray.getJSONObject(i);
                                        x = route.getDouble("lat");
                                        y = route.getDouble("longi");
                                        ignicao = route.getInt("ignicao");

                                        strId = route.getString("id");
                                        strNsat = route.getString("nsat");
                                        strHdop = route.getString("hdop");
                                        strLat = route.getString("lat");
                                        strLongi = route.getString("longi");
                                        strAltitude = route.getString("altitude");
                                        strCourse = route.getString("course");
                                        strSpeed = route.getString("speed");
                                        strIgnicao = route.getString("ignicao");
                                        strDatedev = route.getString("datedev");
                                        strDate = route.getString("date");
                                        strVoltage = route.getString("voltage");
                                        strTemperature = route.getString("temperature");
                                        strOdometer = route.getString("odometer");
                                        strDistance = route.getString("distance");


                                        if (false) {
                                            latitude.add(route.getString("lat"));
                                            longitude.add(route.getString("longi"));
                                            Log.i("Lat:", latitude.get(i).toString());
                                            Log.i("Longi:", longitude.get(i).toString());
                                        }

                                        if (ignicao == 0)/* Parking Icon*/ {
                                            /* Markers*/
                                            mapPoints.add(new LatLng(x, y));
                                            mMap.addMarker(new MarkerOptions()
                                                    .position(mapPoints.get(0))
                                                    .snippet("id=" + strId + "," +
                                                            "sat=" + strNsat + "," +
                                                            "hdop=" + strHdop + "," +
                                                            "lat=" + strLat + "," +
                                                            "lon=" + strLongi + "," +
                                                            "alt=" + strAltitude + "," +
                                                            "crs=" + strCourse + "," +
                                                            "spd=" + strSpeed + "," +
                                                            "ign=" + strIgnicao + "," +
                                                            "ev=" + strDatedev + "," +
                                                            "srv=" + strDate + "," +
                                                            "volt=" + strVoltage + "," +
                                                            "temp=" + strTemperature + "," +
                                                            "odo=" + strOdometer + "," +
                                                            "dis=" + strDistance)
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.parking_32)));
                                            boLastIgnitionState = true;
                                        } else {
                                            /* Markers*/
                                            mapPoints.add(new LatLng(x, y));
                                            if(strEsn == strEsnCitroen){
                                            mMap.addMarker(new MarkerOptions()
                                                    .position(mapPoints.get(0))
                                                    .snippet("id=" + strId + "," +
                                                            "sat=" + strNsat + "," +
                                                            "hdop=" + strHdop + "," +
                                                            "lat=" + strLat + "," +
                                                            "lon=" + strLongi + "," +
                                                            "alt=" + strAltitude + "," +
                                                            "crs=" + strCourse + "," +
                                                            "spd=" + strSpeed + "," +
                                                            "ign=" + strIgnicao + "," +
                                                            "ev=" + strDatedev + "," +
                                                            "srv=" + strDate + "," +
                                                            "volt=" + strVoltage + "," +
                                                            "temp=" + strTemperature + "," +
                                                            "odo=" + strOdometer + "," +
                                                            "dis=" + strDistance)
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.c3_ico)));
                                        }
                                        else
                                        {
                                            if (strEsn == strEsnFiatIdea) {
                                                /* Markers*/
                                                mapPoints.add(new LatLng(x, y));
                                                mMap.addMarker(new MarkerOptions()
                                                        .position(mapPoints.get(0))
                                                        .snippet("id=" + strId + "," +
                                                                    "sat=" + strNsat + "," +
                                                                    "hdop=" + strHdop + "," +
                                                                    "lat=" + strLat + "," +
                                                                    "lon=" + strLongi + "," +
                                                                    "alt=" + strAltitude + "," +
                                                                    "crs=" + strCourse + "," +
                                                                    "spd=" + strSpeed + "," +
                                                                    "ign=" + strIgnicao + "," +
                                                                    "ev=" + strDatedev + "," +
                                                                    "srv=" + strDate + "," +
                                                                    "volt=" + strVoltage + "," +
                                                                    "temp=" + strTemperature + "," +
                                                                    "odo=" + strOdometer + "," +
                                                                    "dis=" + strDistance)
                                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.idea_ico)));
                                                }
                                            }
                                        }
                                        /* Polyline*/
                                        if(false) {
                                            if (xPrevious != 0 && yPrevious != 0) {
                                                Polyline routeLine = mMap.addPolyline(new PolylineOptions()
                                                        .clickable(true)
                                                        .add(new LatLng(xPrevious, yPrevious), new LatLng(x, y)));

                                                routeLine.setColor(iColor[iIndexColor]);
                                                routeLine.setWidth(15);

                                                if (boLastIgnitionState == true) {
                                                    if (iIndexColor < (iColor.length - 1)) {
                                                        iIndexColor++;
                                                    }
                                                    boLastIgnitionState = false;
                                                }
                                                /*routeLine.setGeodesic(true);*/
                                                /*routeLine.setTag("A");*/
                                            }
                                        }
                                        xPrevious = x;
                                        yPrevious = y;
                                        /*}*/
                                        mMap.setMinZoomPreference(10);
                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(mapPoints.get(0)));
                                        mProgressDialog.dismiss();
                                    }
                                    else{
                                        mProgressDialog.dismiss();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        getRequest.execute(myUrl);
                    }
                });
            }
        };
    }

    @Override
    public Context getBaseContext() {
        return super.getBaseContext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Switch refreshSwitch = (Switch) findViewById(R.id.idRefreshswitch); // initiate Switch

        btnInitialDatePicker = (Button) findViewById(R.id.idInitialDate);
        btnFinalDatePicker = (Button) findViewById(R.id.idFinalDate);

        btnInitialDatePicker.setOnClickListener((View.OnClickListener) this);
        btnFinalDatePicker.setOnClickListener((View.OnClickListener) this);

        refreshSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked == true) {
                    Log.i("SwitchStateOn=", "" + isChecked);
                    startTimer();
                }else{
                    stoptimertask();
                    Log.i("SwitchStateOff=", "" + isChecked);
                }
            }
        });

        mProgressDialog = new ProgressDialog(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //get the spinner from the xml.
        Spinner spinner = findViewById(R.id.idVehiclespinner);
        //create a list of items for the spinner.
        String[] items = new String[]{"FiatIdea", "C3"};
        int[] images = {R.drawable.idea,R.drawable.c3 };

        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        CustomAdapter customAdapter=new CustomAdapter(getApplicationContext(),images,items);
        //set the spinners adapter to the previously created one.
        spinner.setAdapter(customAdapter);

        /*adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);*/
        /*dropdown.setAdapter(adapter);*/
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {

                switch (position) {
                    case 0:
                        Log.i("FiatIdea", "0");
                        strEsn = strEsnFiatIdea;
                        break;
                    case 1:
                        Log.i("Citroen", "1");
                        strEsn = strEsnCitroen;
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                strEsn = strEsnFiatIdea;
            };


        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng Brazil = new LatLng(-23, -46);
        /*mMap.addMarker(new MarkerOptions().position(Brazil));*/
        mMap.setMinZoomPreference((float) 7.5);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Brazil));
        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Retrieve the data from the marker.

                Toast.makeText(getApplicationContext(),
                        marker.getSnippet(),
                        Toast.LENGTH_LONG).show();

                // Return false to indicate that we have not consumed the event and that we wish
                // for the default behavior to occur (which is for the camera to move such that the
                // marker is centered and for the marker's info window to open, if it has one).
                return false;

            }
        });

        final Calendar c = Calendar.getInstance();
        mInitialYear = c.get(Calendar.YEAR);
        mInitialMonth = c.get(Calendar.MONTH);
        mInitialDay = c.get(Calendar.DAY_OF_MONTH) ;


        String strCurrentDate = (mInitialMonth+1)+"/"+mInitialDay+"/"+mInitialYear;
        //Some url endpoint that you may have
        /*String myUrl = "http://gpslogger.esy.es/pages/upload/CreateMarkerCitrixJson.php?esn=B4E62DE9567D&fromdate=3/11/2020%2000:00:00&todate=3/11/2020%2023:59:59";*/   //String to place our result in
        /*String myUrl = new String("http://gpslogger.esy.es/pages/upload/CreateMarkerCitrixJson.php?esn=B4E62DE9567D&fromdate="+strCurrentDate+"%2000:00:00&todate="+strCurrentDate+"%2023:59:59");*/
        String myUrl = new String("http://gpslogger.esy.es/pages/upload/CreateMarkerCitrixJson.php?esn="+strEsn+"&fromdate="+strCurrentDate+"%2000:00:00&todate="+strCurrentDate+"%2023:59:59");
        String result;   //Instantiate new instance of our class
        String route;
        HttpGetRequest getRequest = new HttpGetRequest();   //Perform the doInBackground method, passing in our url

        mMap.clear();            /* get JSONObject from JSON file*/
        mapPoints.clear();

        mProgressDialog.setTitle("Route");
        mProgressDialog.setMessage("Please wait, calculating");
        mProgressDialog.show();

        getRequest.setListener(new OnCompleteListener() {
            @Override
            public void OnComplete(String string) {
                Log.i("ROUTE", string);

                /* get JSONObject from JSON file*/


                double x,y,xPrevious, yPrevious;
                int ignicao;

                String  strId,
                        strNsat,
                        strHdop,
                        strLat,
                        strLongi,
                        strAltitude,
                        strCourse,
                        strSpeed,
                        strIgnicao,
                        strDatedev,
                        strDate,
                        strVoltage,
                        strTemperature,
                        strOdometer,
                        strDistance;

                x = 0;
                y = 0;
                xPrevious = 0;
                yPrevious = 0;
                try {
                    JSONObject obj = new JSONObject(string);
                    JSONArray routeArray = obj.getJSONArray("route");
                    // implement for loop for getting users list data

                    int iIndexColor = 0;

                    if(routeArray.length() > 0) {
                        for (int i = 0; i < routeArray.length(); i++) {


                            JSONObject route = routeArray.getJSONObject(i);
                            x = route.getDouble("lat");
                            y = route.getDouble("longi");
                            ignicao = route.getInt("ignicao");

                            strId = route.getString("id");
                            strNsat = route.getString("nsat");
                            strHdop = route.getString("hdop");
                            strLat = route.getString("lat");
                            strLongi = route.getString("longi");
                            strAltitude = route.getString("altitude");
                            strCourse = route.getString("course");
                            strSpeed = route.getString("speed");
                            strIgnicao = route.getString("ignicao");
                            strDatedev = route.getString("datedev");
                            strDate = route.getString("date");
                            strVoltage = route.getString("voltage");
                            strTemperature = route.getString("temperature");
                            strOdometer = route.getString("odometer");
                            strDistance = route.getString("distance");


                            if (false) {
                                latitude.add(route.getString("lat"));
                                longitude.add(route.getString("longi"));
                                Log.i("Lat:", latitude.get(i).toString());
                                Log.i("Longi:", longitude.get(i).toString());
                            }

                            if (ignicao == 0)/* Parking Icon*/ {
                                /* Markers*/
                                mapPoints.add(new LatLng(x, y));
                                mMap.addMarker(new MarkerOptions()
                                        .position(mapPoints.get(i))
                                        .snippet("id=" + strId + "," +
                                                "sat=" + strNsat + "," +
                                                "hdop=" + strHdop + "," +
                                                "lat=" + strLat + "," +
                                                "lon=" + strLongi + "," +
                                                "alt=" + strAltitude + "," +
                                                "crs=" + strCourse + "," +
                                                "spd=" + strSpeed + "," +
                                                "ign=" + strIgnicao + "," +
                                                "ev=" + strDatedev + "," +
                                                "srv=" + strDate + "," +
                                                "volt=" + strVoltage + "," +
                                                "temp=" + strTemperature + "," +
                                                "odo=" + strOdometer + "," +
                                                "dis=" + strDistance)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.parking_32)));
                                boLastIgnitionState = true;
                            } else {
                                /* Markers*/
                                mapPoints.add(new LatLng(x, y));
                                mMap.addMarker(new MarkerOptions()
                                        .position(mapPoints.get(i))
                                        .snippet("id=" + strId + "," +
                                                "sat=" + strNsat + "," +
                                                "hdop=" + strHdop + "," +
                                                "lat=" + strLat + "," +
                                                "lon=" + strLongi + "," +
                                                "alt=" + strAltitude + "," +
                                                "crs=" + strCourse + "," +
                                                "spd=" + strSpeed + "," +
                                                "ign=" + strIgnicao + "," +
                                                "ev=" + strDatedev + "," +
                                                "srv=" + strDate + "," +
                                                "volt=" + strVoltage + "," +
                                                "temp=" + strTemperature + "," +
                                                "odo=" + strOdometer + "," +
                                                "dis=" + strDistance)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.yellow_dot)));
                            }

                            /* Polyline*/
                            if (xPrevious != 0 && yPrevious != 0) {
                                Polyline routeLine = mMap.addPolyline(new PolylineOptions()
                                        .clickable(true)
                                        .add(new LatLng(xPrevious, yPrevious), new LatLng(x, y)));

                                routeLine.setColor(iColor[iIndexColor]);
                                routeLine.setWidth(15);

                                if (boLastIgnitionState == true) {
                                    if (iIndexColor < (iColor.length - 1)) {
                                        iIndexColor++;
                                    }
                                    boLastIgnitionState = false;
                                }
                                /*routeLine.setGeodesic(true);*/
                                /*routeLine.setTag("A");*/
                            }
                            xPrevious = x;
                            yPrevious = y;
                        }
                        mMap.setMinZoomPreference(10);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(mapPoints.get(0)));
                        mProgressDialog.dismiss();
                    }
                    else{
                        mProgressDialog.dismiss();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        getRequest.execute(myUrl);
    }

    @Override
    public void onClick(View v) {

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        if (v == btnInitialDatePicker) {

            mInitialYear = c.get(Calendar.YEAR);
            mInitialMonth = c.get(Calendar.MONTH);
            mInitialDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog InitialdatePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            String strInitialDate = (monthOfYear+1)+"/"+dayOfMonth+"/"+year;
                            SetInitialDate(strInitialDate);

                            Log.i("CALENDAR",strInitialDate);
                        }
                    }, mInitialYear, mInitialMonth, mInitialDay);
            InitialdatePickerDialog.show();
        }

        if (v == btnFinalDatePicker) {

            mFinalYear = c.get(Calendar.YEAR);
            mFinalMonth = c.get(Calendar.MONTH);
            mFinalDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog FinaldatePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            String strFinalDate = (monthOfYear+1)+"/"+dayOfMonth+"/"+year;;
                            SetFinalDate(strFinalDate);

                            Log.i("CALENDAR",GetFinalDate());

                            /*String myUrl = "http://gpslogger.esy.es/pages/upload/CreateMarkerCitrixJson.php?esn=B4E62DE9567D&fromdate=3/11/2020%2000:00:00&todate=3/11/2020%2023:59:59";*/   //String to place our result in
                            /*String myUrl = new String("http://gpslogger.esy.es/pages/upload/CreateMarkerCitrixJson.php?esn=B4E62DE9567D&fromdate="+GetInitialDate()+"%2000:00:00&todate="+GetFinalDate()+"%2023:59:59");*/
                            String myUrl = new String("http://gpslogger.esy.es/pages/upload/CreateMarkerCitrixJson.php?esn="+strEsn+"&fromdate="+GetInitialDate()+"%2000:00:00&todate="+GetFinalDate()+"%2023:59:59");
                            Log.i("URL",myUrl);

                            String result;   //Instantiate new instance of our class
                            String route;
                            HttpGetRequest getRequest = new HttpGetRequest();   //Perform the doInBackground method, passing in our url

                            mMap.clear();
                            mapPoints.clear();

                            mProgressDialog.setTitle("Route");
                            mProgressDialog.setMessage("Please wait, calculating");
                            mProgressDialog.show();

                            getRequest.setListener(new OnCompleteListener() {
                                @Override
                                public void OnComplete(String string) {
                                    Log.i("ROUTE", string);

                                    /* get JSONObject from JSON file*/

                                    double x,y,xPrevious, yPrevious;
                                    int ignicao;

                                    String  strId,
                                            strNsat,
                                            strHdop,
                                            strLat,
                                            strLongi,
                                            strAltitude,
                                            strCourse,
                                            strSpeed,
                                            strIgnicao,
                                            strDatedev,
                                            strDate,
                                            strVoltage,
                                            strTemperature,
                                            strOdometer,
                                            strDistance;

                                    x = 0;
                                    y = 0;
                                    xPrevious = 0;
                                    yPrevious = 0;
                                    try {
                                        JSONObject obj = new JSONObject(string);
                                        JSONArray routeArray = obj.getJSONArray("route");
                                        // implement for loop for getting users list data
                                        int iIndexColor = 0;
                                        if(routeArray.length() > 0) {
                                            for (int i = 0; i < routeArray.length(); i++) {

                                                JSONObject route = routeArray.getJSONObject(i);
                                                x = route.getDouble("lat");
                                                y = route.getDouble("longi");
                                                ignicao = route.getInt("ignicao");

                                                strId = route.getString("id");
                                                strNsat = route.getString("nsat");
                                                strHdop = route.getString("hdop");
                                                strLat = route.getString("lat");
                                                strLongi = route.getString("longi");
                                                strAltitude = route.getString("altitude");
                                                strCourse = route.getString("course");
                                                strSpeed = route.getString("speed");
                                                strIgnicao = route.getString("ignicao");
                                                strDatedev = route.getString("datedev");
                                                strDate = route.getString("date");
                                                strVoltage = route.getString("voltage");
                                                strTemperature = route.getString("temperature");
                                                strOdometer = route.getString("odometer");
                                                strDistance = route.getString("distance");


                                                if (false) {
                                                    latitude.add(route.getString("lat"));
                                                    longitude.add(route.getString("longi"));
                                                    Log.i("Lat:", latitude.get(i).toString());
                                                    Log.i("Longi:", longitude.get(i).toString());
                                                }

                                                if (ignicao == 0)/* Parking Icon*/ {
                                                    /* Markers*/
                                                    mapPoints.add(new LatLng(x, y));
                                                    mMap.addMarker(new MarkerOptions()
                                                            .position(mapPoints.get(i))
                                                            .snippet("id=" + strId + "," +
                                                                    "sat=" + strNsat + "," +
                                                                    "hdop=" + strHdop + "," +
                                                                    "lat=" + strLat + "," +
                                                                    "lon=" + strLongi + "," +
                                                                    "alt=" + strAltitude + "," +
                                                                    "crs=" + strCourse + "," +
                                                                    "spd=" + strSpeed + "," +
                                                                    "ign=" + strIgnicao + "," +
                                                                    "ev=" + strDatedev + "," +
                                                                    "srv=" + strDate + "," +
                                                                    "volt=" + strVoltage + "," +
                                                                    "temp=" + strTemperature + "," +
                                                                    "odo=" + strOdometer + "," +
                                                                    "dis=" + strDistance)
                                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.parking_32)));
                                                    boLastIgnitionState = true;
                                                } else {
                                                    /* Markers*/
                                                    mapPoints.add(new LatLng(x, y));
                                                    mMap.addMarker(new MarkerOptions()
                                                            .position(mapPoints.get(i))
                                                            .snippet("id=" + strId + "," +
                                                                    "sat=" + strNsat + "," +
                                                                    "hdop=" + strHdop + "," +
                                                                    "lat=" + strLat + "," +
                                                                    "lon=" + strLongi + "," +
                                                                    "alt=" + strAltitude + "," +
                                                                    "crs=" + strCourse + "," +
                                                                    "spd=" + strSpeed + "," +
                                                                    "ign=" + strIgnicao + "," +
                                                                    "ev=" + strDatedev + "," +
                                                                    "srv=" + strDate + "," +
                                                                    "volt=" + strVoltage + "," +
                                                                    "temp=" + strTemperature + "," +
                                                                    "odo=" + strOdometer + "," +
                                                                    "dis=" + strDistance)
                                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.yellow_dot)));
                                                }

                                                /* Polyline*/
                                                if (xPrevious != 0 && yPrevious != 0) {
                                                    Polyline routeLine = mMap.addPolyline(new PolylineOptions()
                                                            .clickable(true)
                                                            .add(new LatLng(xPrevious, yPrevious), new LatLng(x, y)));

                                                    routeLine.setColor(iColor[iIndexColor]);
                                                    routeLine.setWidth(15);

                                                    if (boLastIgnitionState == true) {
                                                        if (iIndexColor < (iColor.length - 1)) {
                                                            iIndexColor++;
                                                        }
                                                        boLastIgnitionState = false;
                                                    }
                                                    /*routeLine.setGeodesic(true);*/
                                                    /*routeLine.setTag("A");*/
                                                }
                                                xPrevious = x;
                                                yPrevious = y;
                                            }
                                            mMap.setMinZoomPreference(10);
                                            mMap.moveCamera(CameraUpdateFactory.newLatLng(mapPoints.get(routeArray.length()-1)));
                                            mProgressDialog.dismiss();
                                        }else{
                                            mProgressDialog.dismiss();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            getRequest.execute(myUrl);
                        }
                    }, mFinalYear, mFinalMonth, mFinalDay);
            FinaldatePickerDialog.show();
        }
    }
}


