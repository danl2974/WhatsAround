package com.dl2974.whatsaround;

import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.dl2974.whatsaround.PlacesClient.PlacesCallType;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements 
LocationsListFragment.OnLocationTypeSelectedListener,
HomeGridFragment.OnPlaceTypeSelectedListener,
FactualFragment.OnLocationSelectedListener,
FactualFragment.OnUserLocationChange,
//LocationFragment.MapListener,
CustomMapFragment.MapListener,
GoogleMap.InfoWindowAdapter,
SingleFragment.SingleLocationMapListener,
SingleLocationFragment.SingleLocationMapListener,
CustomStreetViewFragment.StreetMapListener,
LocationListener,
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener {
	
    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;
	Location userLocation;
	HashMap<String,Object> gridPhotoState = new HashMap<String,Object>();
	HashMap<String,Object> activityLocationData;
	ArrayList<HashMap<String,Object>> placesLocations;
	HashMap<String,Object> placesLocationDetailsData;
	ArrayList<HashMap<String,String>> localLocations;
	ArrayList<HashMap<String,Object>> yelpLocations;
	GoogleMap map;
	public Marker activeMarker;
	GroundOverlay closeButton;
	Projection projection;
	int factualCategoryId;
	boolean googlePlayServicesConnected;
	final static String MAP_FRAGMENT = "mapfragment";
	final static String SINGLE_MAP_FRAGMENT = "singlemapfragment";
	final static String STREET_MAP_FRAGMENT = "streetmapfragment";
	ArrayList<String> yelpMarkers = new ArrayList<String>();
	private String yelpFilter;
	private String placesFilter;
	private String placesKey = null;
	private boolean connectionRetry = false;


	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.container);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		if (findViewById(R.id.fragment_container) != null) {
			 if (savedInstanceState != null) {
				    Log.i("Test savedInstanceState", String.valueOf(savedInstanceState.size()));
	                return;
	            }
		}

		Intent mIntent = this.getIntent();
		if (mIntent.getExtras() != null){
		    this.userLocation = mIntent.getParcelableExtra(InitialActivity.LOCATION_EXTRA);
	    }
		else{Log.i("startmain","no location in intent");}
		
		
		int availableCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (availableCode == ConnectionResult.SUCCESS)
		{
	          if(this.mLocationRequest == null){		
	             this.mLocationRequest = LocationRequest.create();
	          }
	          this.mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	          
	          if(this.mLocationClient == null){
	             this.mLocationClient = new LocationClient(this, this, this); 
	          }
	          if(!this.mLocationClient.isConnected()){
	            this.mLocationClient.connect();
	          }
	          
		}
		else{
			Dialog gpErrorDialog = GooglePlayServicesUtil.getErrorDialog(availableCode, this, 0);
			gpErrorDialog.show();
			//do Toast here
		}
		
		/*
		if (findViewById(R.id.fragment_container) != null) {
			 if (savedInstanceState != null) {
	                return;
	            }
			 
			 //LocationsListFragment llFragment = new LocationsListFragment();
			 //llFragment.setArguments(getIntent().getExtras());
	         //getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, llFragment).commit();
			 
			 
			 //InitialFragment initFrag = new InitialFragment();
			 //getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, initFrag).commit();
			 
		 		 
         ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	     NetworkInfo networkInfo = connMgr.getActiveNetworkInfo(); 

	        
	     if (networkInfo != null && networkInfo.isConnected() && this.userLocation != null) {
	    	 
	    	 initGridHome();
		 
	     }
	     else{
	       	  AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
	       	  alertBuilder
	       	  .setTitle("Where Are You?")
	       	  .setMessage("Your current location isn't available currently from your device. Are we allowed to find you?")
	             .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	           	  @Override
	                 public void onClick(DialogInterface dialog, int id) {
	           		  dialog.cancel();
	           		  Toast.makeText(MainActivity.this, "Trying To Find Your Location", Toast.LENGTH_LONG).show(); 
	           		  MainActivity.this.connectionRetry = true;
	               	  MainActivity.this.mLocationClient.connect();
	                 }
	             })
	             .setNegativeButton("No", new DialogInterface.OnClickListener() {
	           	  @Override
	                 public void onClick(DialogInterface dialog, int id) {
	                      dialog.cancel();
	                 }
	             });
	       	  AlertDialog alertDialog = alertBuilder.create();
	       	  alertDialog.show();
	       	  
	         } //end ELSE
			 
		     
			 
		}
		*/
		
	}
	
	
	
    @Override
    protected void onStart() {
        super.onStart();
        verifyConnectivity();
    }
	

    
    private void verifyConnectivity(){
    	
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
  	    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo(); 

  	    if (networkInfo != null && networkInfo.isConnected()) {
  	    	 
  	 
  	 	     return;
  		 
  	     }
  	     else{
  	       	  AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
  	       	  alertBuilder
  	       	  .setTitle("Where Are You?")
  	       	  .setMessage("Your current location isn't available currently from your device. Are we allowed to find you?")
  	             .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
  	           	  @Override
  	                 public void onClick(DialogInterface dialog, int id) {
  	           		  dialog.cancel();
  	           		  Toast.makeText(MainActivity.this, "Trying To Find Your Location", Toast.LENGTH_LONG).show(); 
  	           		  MainActivity.this.connectionRetry = true;
  	               	  MainActivity.this.mLocationClient.connect();
  	                 }
  	             })
  	             .setNegativeButton("No", new DialogInterface.OnClickListener() {
  	           	  @Override
  	                 public void onClick(DialogInterface dialog, int id) {
  	                      dialog.cancel();
  	                 }
  	             });
  	       	  AlertDialog alertDialog = alertBuilder.create();
  	       	  alertDialog.show();
  	       	  
  	         } //end ELSE
    	
    	
    }
	
    
    
    
	/*
	@Override
	protected void onStart (){
		
		 super.onStart();
		
		 if (this.gridPhotoState != null)
		 {
			 startGridFragment(this.gridPhotoState);
			 return;
		 }
		 
		 ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	     NetworkInfo networkInfo = connMgr.getActiveNetworkInfo(); 
	     String locTest = this.userLocation != null ? "true" : "false";
	     Log.i("InitGridCheck", String.format("%s %s %s", String.valueOf(networkInfo.isAvailable()), String.valueOf(networkInfo.isConnected()), locTest)  );
	        
	     if (networkInfo != null && networkInfo.isConnected() && this.userLocation != null) {
	    	 
		    initGridHome();
		 
	     }
	     else{
	       	  AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
	       	  alertBuilder
	       	  .setTitle("Where Are You?")
	       	  .setMessage("Your current location isn't available from your device. Are we allowed to find you?")
	             .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	           	  @Override
	                 public void onClick(DialogInterface dialog, int id) {
	           		  dialog.cancel();
	           		  Toast.makeText(MainActivity.this, "Trying To Find Your Location", Toast.LENGTH_LONG).show(); 
	           		  MainActivity.this.connectionRetry = true;
	               	  MainActivity.this.mLocationClient.connect();
	                 }
	             })
	             .setNegativeButton("No", new DialogInterface.OnClickListener() {
	           	  @Override
	                 public void onClick(DialogInterface dialog, int id) {
	                      dialog.cancel();
	                 }
	             });
	       	  AlertDialog alertDialog = alertBuilder.create();
	       	  alertDialog.show();
	       	  
	         } //end ELSE
		
	}
	*/
	
	
	private void initGridHome(){
		
	      HomeGridFragment hgFragment = new HomeGridFragment();
		  hgFragment.setLocation(this.userLocation);
		  FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
		  mFragmentTransaction.add(R.id.fragment_container, hgFragment);
		  mFragmentTransaction.addToBackStack(null);
		  mFragmentTransaction.commit();
	 
	}
    
	
	public void startGridFragment(HashMap<String,Object> locationTypePhotoMap){
		 
        //start original in onCreate
        HomeGridFragment hgFragment = new HomeGridFragment();
        hgFragment.setTypePhotoMap(locationTypePhotoMap);
        hgFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, hgFragment,"gridfragment").commit();
        //getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, hgFragment).commit();
        //end original in onCreate
		
	}

	
	public void onLocationTypeSelected(int categoryId) {
		//RESTORE BELOW COMMENTED BLOCK WITH FRAGMENT USING FactualClient
		/*
		FactualFragment fFragment = new FactualFragment();
		fFragment.setCategoryId(categoryId);
		this.factualCategoryId = categoryId;
		
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, fFragment);
        transaction.addToBackStack(null);

        transaction.commit();
        */
        //BYPASS FACTUAL FRAG with LIST...GO DIRECT TO MAP..
        this.factualCategoryId = categoryId;
        CustomMapFragment mapFragment = CustomMapFragment.newInstance();
		
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mapFragment, MAP_FRAGMENT);
        transaction.addToBackStack(null);

        transaction.commit();
		
	}
	
	
	public void onLocationTypeFilter(String filter){
		//this.yelpFilter = filter;
		this.placesFilter = filter;
		
        CustomMapFragment mapFragment = CustomMapFragment.newInstance();
		
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mapFragment, MAP_FRAGMENT);
        transaction.addToBackStack(null);
        transaction.commit();
	}
	
	public void onPlaceTypeFilter(String filter){
		//this.yelpFilter = filter;
		this.placesFilter = filter;
		
        CustomMapFragment mapFragment = CustomMapFragment.newInstance();
		
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mapFragment, MAP_FRAGMENT);
        transaction.addToBackStack(null);

        transaction.commit();
	}	
	
	
	public void onLocationSelected(HashMap<String,String> locationMap) {
		/*
		
        //GoogleMapOptions gmo = (new GoogleMapOptions()).zoomControlsEnabled(false).rotateGesturesEnabled(false);
        CustomMapFragment mapFragment = CustomMapFragment.newInstance();
        mapFragment.setLocationData(locationMap);
		
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mapFragment, MAP_FRAGMENT);
        transaction.addToBackStack(null);

        transaction.commit();
		*/
		
	}
	
	
	
	@SuppressLint("NewApi")
	public void onSingleLocationView(HashMap<String,Object> locationData){
		
		this.activityLocationData = locationData;
		
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentByTag(MAP_FRAGMENT);

        GoogleMap map = mapFragment.getMap();

        
        //GoogleMap map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.gmap)).getMap();
       // GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.gmap)).getMap();

        //SINGLE LOCATION AND USER POSITION
        LatLng locationLongLat = new LatLng( Double.valueOf((String) locationData.get("latitude")), Double.valueOf((String) locationData.get("longitude")) );
        LatLng userLocationLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());        
        //map.setMyLocationEnabled(true);
        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLongLat, 13));
         //Marker marker = map.addMarker(new MarkerOptions().position(locationLongLat).title(locationData.get("name")).snippet(markerSnippet));
         Marker marker = map.addMarker(new MarkerOptions().position(locationLongLat).title((String) locationData.get("name")));
         Marker userLocationMarker = map.addMarker(new MarkerOptions().position(userLocationLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action)));
         map.setInfoWindowAdapter(this);
         marker.showInfoWindow();
         map.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLongLat, 12));
         map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
         
         map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
        	 
        	 @Override
             public void onInfoWindowClick(Marker marker) {
        		 
        		 String websiteUrl = (String) MainActivity.this.activityLocationData.get("website");
        		 Uri webpage = Uri.parse(websiteUrl);
        		 Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
        		 startActivity(webIntent);
        	 }
         });
         //SINGLE LOCATION AND USER POSITION
         
         
	}
	
	
	public void onUserCenteredLocationsView(){
		
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        
        
        if (networkInfo != null && networkInfo.isConnected() && this.userLocation != null) {
        	
        	/*
        	// FACTUAL and YELP
		    FactualClient fc = new FactualClient(userLocation.getLatitude(), userLocation.getLongitude(), 20000);
		    this.localLocations = fc.getLocationsByCategory(this.factualCategoryId);
		  
		    YelpClient yc = new YelpClient(userLocation.getLatitude(), userLocation.getLongitude(), 20000);
		    yc.setLocationTypeFilter(this.yelpFilter);
		    this.yelpLocations = yc.formatLocations();
		    */	
        	
        	HashMap<String,Object> searchParams = new HashMap<String,Object>();
        	searchParams.put("location", String.format("%s,%s", userLocation.getLatitude(), userLocation.getLongitude() ));
        	searchParams.put("radius", "5000");
        	searchParams.put("types", this.placesFilter);
        	
        	/*
        	ApplicationInfo appInfo = null;
        	if(this.placesKey == null){
			try {
				appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
				Bundle bundle = appInfo.metaData;
	            this.placesKey = bundle.getString("com.google.android.maps.v2.API_KEY");
			    } catch (NameNotFoundException e) {
				Log.e("ApplicationInfo NameNotFoundException", e.getMessage());
			    }
            }
			*/
        	
        	PlacesClient pc = new PlacesClient(searchParams, PlacesCallType.search);
        	this.placesLocations = pc.getPlacesData();
        	Log.i("Places data size ", String.valueOf(this.placesLocations.size()) );
		
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentByTag(MAP_FRAGMENT);
		map = mapFragment.getMap();
		map.setMyLocationEnabled(true);
		//map.setMapType(GoogleMap.MAP_TYPE_HYBRID); // Satellite Map
        projection = map.getProjection();
        LatLng userLocationLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        //Marker userLocationMarker = map.addMarker(new MarkerOptions().position(userLocationLatLng).title("YOU").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action)));
        //userLocationMarker.showInfoWindow();
        CircleOptions circleOptions = new CircleOptions().center(userLocationLatLng).radius(500).fillColor(0x880099ff).strokeColor(0xaa0099ff).strokeWidth(1.0f);
        Circle circle = map.addCircle(circleOptions);
        
        
        for (HashMap<String,Object> pl : placesLocations){
        	LatLng locationLongLat = new LatLng( Double.valueOf( (String) pl.get("latitude")), Double.valueOf( (String) pl.get("longitude")) );
        	int mapDrawable = getResources().getIdentifier(resolveCategoryName(0), "drawable", this.getPackageName());
        	Marker marker = map.addMarker(new MarkerOptions().position(locationLongLat).title((String) pl.get("name")).icon(BitmapDescriptorFactory.fromResource(mapDrawable)));
        	
        	//Marker marker = map.addMarker(new MarkerOptions().position(locationLongLat).title(ll.get("name")).icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant)));
        	//Marker marker = map.addMarker(new MarkerOptions().position(locationLongLat).alpha(0.7f).title(ll.get("name")));
        	//marker.showInfoWindow();
        }        
        
        //FACTUAL and YELP
        /*
        for (HashMap<String,String> ll : localLocations){
        	LatLng locationLongLat = new LatLng( Double.valueOf(ll.get("latitude")), Double.valueOf(ll.get("longitude")) );
        	int mapDrawable = getResources().getIdentifier(resolveCategoryName(this.factualCategoryId), "drawable", this.getPackageName());
        	Marker marker = map.addMarker(new MarkerOptions().position(locationLongLat).title(ll.get("name")).icon(BitmapDescriptorFactory.fromResource(mapDrawable)));
        	//Marker marker = map.addMarker(new MarkerOptions().position(locationLongLat).title(ll.get("name")).icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant)));
        	//Marker marker = map.addMarker(new MarkerOptions().position(locationLongLat).alpha(0.7f).title(ll.get("name")));
        	//marker.showInfoWindow();
        }
        
        //YELP markers
        for (HashMap<String,Object> yl : yelpLocations){
        	LatLng locationLongLat = new LatLng( Double.valueOf((String) yl.get("latitude")), Double.valueOf( (String) yl.get("longitude")) );
        	//Marker marker = map.addMarker(new MarkerOptions().position(locationLongLat).title( (String) yl.get("name")).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
        	Marker marker = map.addMarker(new MarkerOptions().position(locationLongLat).title( (String) yl.get("name")).icon(BitmapDescriptorFactory.fromResource(R.drawable.yelp)));
        	yelpMarkers.add(marker.getId());
        	
        }        
        */
        
        map.setInfoWindowAdapter(this);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocationLatLng, 12));
        map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener(){
        
        	@Override
            public boolean onMarkerClick(Marker marker){
        		
        		if(marker.equals(MainActivity.this.activeMarker)){ // Close marker if active one is re-clicked
        			resetMapMarker();
        			return true;
        		}
        		else{
        		  if (MainActivity.this.activeMarker != null){
        			  resetMapMarker();
        		  }	
        		  
        		  MainActivity.this.setActiveMarker(marker);
        		  marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_active));
        		  
        		  //int mapDrawable = getResources().getIdentifier(resolveCategoryName(0), "drawable", MainActivity.this.getPackageName());
        		  //MarkerOptions markerOptions = new MarkerOptions().position(marker.getPosition()).title(marker.getTitle()).icon(BitmapDescriptorFactory.fromResource(mapDrawable));
        		  //marker.remove();
        		  //map.addMarker(markerOptions);
        		  
        		}
        		/*
        		double latOffset = marker.getPosition().latitude - 0.003;
        		Log.i("LatOffset", String.valueOf(marker.getPosition().latitude) + " " + String.valueOf(latOffset));
        		MainActivity.this.closeButton =  map.addGroundOverlay(new GroundOverlayOptions()
        		        .position(new LatLng(latOffset, marker.getPosition().longitude), 1000)
        				.image(BitmapDescriptorFactory.fromResource(R.drawable.close))); 
                */
        		return false;
        	}
        	
        });
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener(){

            @Override
            public void onMapClick(LatLng point) {
                  
            	if(MainActivity.this.activeMarker != null){
            		//Restore original marker
            		Log.i("Test MapClick", MainActivity.this.activeMarker.getTitle());
            		resetMapMarker();           		
            		//MainActivity.this.activeMarker.setRotation(0.0f);
            		//int mapDrawable = getResources().getIdentifier(resolveCategoryName(0), "drawable", MainActivity.this.getPackageName());
            		//MainActivity.this.activeMarker.setIcon(BitmapDescriptorFactory.fromResource(mapDrawable)); 
            		//MainActivity.this.activeMarker.hideInfoWindow();
	
            	}
            	/*
            	if((Math.abs(point.longitude  -  MainActivity.this.closeButton.getPosition().longitude) < .004) 
            			&& (Math.abs(point.latitude  -  MainActivity.this.closeButton.getPosition().latitude) < .004))
            	{
            		MainActivity.this.closeButton.remove();
            	}
            	*/

            }

        });
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
       	 
       	 @Override
            public void onInfoWindowClick(Marker marker) {

       		if(yelpMarkers.contains(marker.getId())){
       		 
          		 String websiteUrl = (String) MainActivity.this.yelpLocations.get(resolveYelpLocationIndex(marker.getTitle())).get("link");
           		 Uri webpage = Uri.parse(websiteUrl);
           		 Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
           		 startActivity(webIntent);
       			
       		}
       		else{
       			
       		     startSingleFragment(marker);
       		     
       		}
       		
       	 }
        });
		
		
        
	   }//end IF
        
       //Present Dialog Box if Location not known
       else{
      	  AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
      	  alertBuilder
      	  .setTitle("Where Are You?")
      	  .setMessage("Your current location isn't available from your device. Are we allowed to find you?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
          	  @Override
                public void onClick(DialogInterface dialog, int id) {
          		  dialog.cancel();
          		  Toast.makeText(MainActivity.this, "Trying To Find Your Location", Toast.LENGTH_LONG).show(); 
          		  MainActivity.this.connectionRetry = true;
              	  MainActivity.this.mLocationClient.connect(); 
                }
            })
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
          	  @Override
                public void onClick(DialogInterface dialog, int id) {
                     dialog.cancel();
                }
            });
      	  AlertDialog alertDialog = alertBuilder.create();
      	  alertDialog.show();
      	  
        } //end ELSE       
        
        
        
	}
	
	
	public View getInfoWindow(Marker marker){
		
		//if (this.activeMarker != null){
			//resetMapMarker();
		//}
		//this.activeMarker = marker;
		//marker.setRotation(180.0f);
		
		LinearLayout infoWindowImageView =  (LinearLayout) getLayoutInflater().inflate(R.layout.info_window_image, null);
		//infoWindowView.setBackgroundResource(getResources().getIdentifier("info_window_bg", "drawable", this.getPackageName()););
		//infoWindowView.setBackgroundResource(R.drawable.custom_info_bubble);
		if(yelpMarkers.contains(marker.getId())){
			int markerIndex = resolveYelpLocationIndex(marker.getTitle());
			TextView iw_name = (TextView) infoWindowImageView.findViewById(R.id.iw_name);
			iw_name.setText((String) this.yelpLocations.get(markerIndex).get("name"));
			
			TextView iw_website = (TextView) infoWindowImageView.findViewById(R.id.iw_website);
			iw_website.setClickable(true);
			String websiteUrl = (String) this.yelpLocations.get(markerIndex).get("link");
			String link = String.format("<a href='%s'>%s</a>", websiteUrl, websiteUrl );
			iw_website.setText(Html.fromHtml(link));
			
			ImageView iv = (ImageView) infoWindowImageView.findViewById(R.id.info_window_imageview);
			iv.setImageBitmap((Bitmap) this.yelpLocations.get(markerIndex).get("image"));
			iv.setVisibility(View.VISIBLE);

			return infoWindowImageView;		
		}
		else{
			int markerIndex = resolvePlacesIndex(marker.getTitle());
			Log.i("Places", "markerIndex " + String.valueOf(markerIndex));
			HashMap<String,Object> detailsParams = new HashMap<String,Object>();
			detailsParams.put("placeid", (String) this.placesLocations.get(markerIndex).get("place_id"));
			PlacesClient dpc = new PlacesClient(detailsParams, PlacesCallType.details);
			this.placesLocationDetailsData = dpc.getPlacesData().get(0);
			Log.i("Places", "placesLocationDetailsData " + String.valueOf(this.placesLocationDetailsData));
			
			View infoWindowView = getLayoutInflater().inflate(R.layout.info_window, null);
			TextView iw_name = (TextView) infoWindowView.findViewById(R.id.iw_name);
			iw_name.setText((String) this.placesLocations.get(markerIndex).get("name"));
			TextView iw_address = (TextView) infoWindowView.findViewById(R.id.iw_address);
			iw_address.setText((String) this.placesLocationDetailsData.get("formatted_address"));
			TextView iw_hours = (TextView) infoWindowView.findViewById(R.id.iw_hours);
			iw_hours.setText((String) this.placesLocationDetailsData.get("hours"));
			
			
			TextView iw_telephone = (TextView) infoWindowView.findViewById(R.id.iw_telephone);
			iw_telephone.setClickable(true);
			iw_telephone.setText((String) this.placesLocationDetailsData.get("formatted_phone_number"));
			iw_telephone.setOnClickListener(new View.OnClickListener() {
				  @Override
				  public void onClick(View v) {
					  Intent callIntent = new Intent(Intent.ACTION_CALL);
					  callIntent.setData(Uri.parse((String) MainActivity.this.placesLocationDetailsData.get("formatted_phone_number")));
					  startActivity(callIntent);
				  }
				});
			
			if(this.placesLocationDetailsData.get("website") != null){
			  TextView iw_website = (TextView) infoWindowView.findViewById(R.id.iw_website);
			  iw_website.setClickable(true);
			  String websiteUrl = (String) this.placesLocationDetailsData.get("website");
			  String link = String.format("<a href='%s'>%s</a>", websiteUrl, websiteUrl );
			  iw_website.setText(Html.fromHtml(link));
			}
			
	    /*		
		View infoWindowView = getLayoutInflater().inflate(R.layout.info_window, null);	
		int markerIndex = resolveLocationIndex(marker.getTitle());
		TextView iw_name = (TextView) infoWindowView.findViewById(R.id.iw_name);
		iw_name.setText(this.localLocations.get(markerIndex).get("name"));
		TextView iw_address = (TextView) infoWindowView.findViewById(R.id.iw_address);
		iw_address.setText(this.localLocations.get(markerIndex).get("address") + "\n" +  this.localLocations.get(markerIndex).get("locality") + " " + this.localLocations.get(markerIndex).get("region") + " " + this.localLocations.get(markerIndex).get("postcode"));
		TextView iw_hours = (TextView) infoWindowView.findViewById(R.id.iw_hours);
		iw_hours.setText(this.localLocations.get(markerIndex).get("hours_display"));
		TextView iw_telephone = (TextView) infoWindowView.findViewById(R.id.iw_telephone);
		iw_telephone.setText(this.localLocations.get(markerIndex).get("tel"));
		
		TextView iw_website = (TextView) infoWindowView.findViewById(R.id.iw_website);
		iw_website.setClickable(true);
		String websiteUrl = this.localLocations.get(markerIndex).get("website");
		String link = String.format("<a href='%s'>%s</a>", websiteUrl, websiteUrl );
		iw_website.setText(Html.fromHtml(link));
		*/
		
		//CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Double.valueOf(this.localLocations.get(markerIndex).get("latitude")),  Double.valueOf(this.localLocations.get(markerIndex).get("longitude")) )).zoom(15).bearing(90).tilt(65).build();
		//map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		return infoWindowView;
		}
		
		
	}	
	
	public View getInfoContents(Marker marker){

		View infoWindowView =  getLayoutInflater().inflate(R.layout.info_window, null);
		
		/*
		TextView textData = (TextView) infoWindowView.findViewById(R.id.location_info);
		textData.setText(marker.getSnippet());
		*/
			
		//SINGLE LOCATION AND USER POSITION
		TextView iw_name = (TextView) infoWindowView.findViewById(R.id.iw_name);
		iw_name.setText((String) this.activityLocationData.get("name"));
		TextView iw_address = (TextView) infoWindowView.findViewById(R.id.iw_address);
		iw_address.setText(this.activityLocationData.get("address") + "\n" +  this.activityLocationData.get("locality") + " " + this.activityLocationData.get("region") + " " + this.activityLocationData.get("postcode"));
		TextView iw_hours = (TextView) infoWindowView.findViewById(R.id.iw_hours);
		iw_hours.setText((String) this.activityLocationData.get("hours_display"));
		TextView iw_telephone = (TextView) infoWindowView.findViewById(R.id.iw_telephone);
		iw_telephone.setText((String) this.activityLocationData.get("tel"));
		
		TextView iw_website = (TextView) infoWindowView.findViewById(R.id.iw_website);
		iw_website.setClickable(true);
		String websiteUrl = (String) this.activityLocationData.get("website");
		String link = String.format("<a href='%s'>%s</a>", websiteUrl, websiteUrl );
		iw_website.setText(Html.fromHtml(link));
		//SINGLE LOCATION AND USER POSITION
        
		
		return infoWindowView;
		
	}
	
	public void updateUserLocation(Location location){
		
		this.userLocation = location;
		
	}
	
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	*/
	
	private String resolveCategoryName(int id){
		String name = "place";
		int[] FACTUAL_IDS = {2, 62, 123, 149, 177, 308, 312, 372, 415, 430};
		String[] names = {"car","restaurant","mall","restaurant","departmentstore","group","bar","bar","bus","airport"};
		for(int i = 0;i<FACTUAL_IDS.length;i++){
			if(FACTUAL_IDS[i] == id){
				name = names[i];
				return name;
			}
		}
		return name;
	}
	
	private int resolveLocationIndex(String name){
		int index = -1;
		for(int i=0; i < localLocations.size(); i++){
			if( name.equals(localLocations.get(i).get("name")) ){
				index = i;
				return index;
			}
		}
		return index;
		
	}
	
	private int resolvePlacesIndex(String name){
		int index = -1;
		for(int i=0; i < placesLocations.size(); i++){
			if( name.equals(placesLocations.get(i).get("name")) ){
				index = i;
				return index;
			}
		}
		return index;
		
	}	
	
	private int resolveYelpLocationIndex(String name){
		int index = -1;
		for(int i=0; i < yelpLocations.size(); i++){
			if( name.equals(yelpLocations.get(i).get("name")) ){
				index = i;
				return index;
			}
		}
		return index;
		
	}
	
	public void startSingleFragment(Marker marker) {
		
		int markerIndex = resolvePlacesIndex(marker.getTitle());
		HashMap<String,Object> singleLocationData = this.placesLocations.get(markerIndex);

		//SingleFragment sFragment = new SingleFragment();
		//sFragment.setSingleLocationData(singleLocationData);
		//sFragment.setSingleLocationDetailsData(this.placesLocationDetailsData);
		
		//CustomMapFragment gmapFragment = CustomMapFragment.newInstance();
		//gmapFragment.setSingleLocationData(singleLocationData);
		
		SingleLocationFragment sFragment = new SingleLocationFragment();
		sFragment.setSingleLocationData(singleLocationData);
		sFragment.setSingleLocationDetailsData(this.placesLocationDetailsData);
		
		CustomMapFragment gmapFragment = CustomMapFragment.newInstance();
		gmapFragment.setSingleLocationData(singleLocationData);
		
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, sFragment);
        transaction.add(R.id.single_map, gmapFragment, SINGLE_MAP_FRAGMENT);
        transaction.addToBackStack(null);

        transaction.commit();
		
	}
	
	public void onSingleMapViewCreated(HashMap<String,Object> singleLocationData){
		
		//getSupportFragmentManager().dump("", null, new PrintWriter(System.out, true), null);
		try{
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag(SINGLE_MAP_FRAGMENT);
		GoogleMap gmap = mapFragment.getMap();
		//FragmentManager fragmentManager = getSupportFragmentManager();
		//GoogleMap gmap = ((SupportMapFragment) fragmentManager.findFragmentByTag(SINGLE_MAP_FRAGMENT)).getMap();
    	gmap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    	LatLng locationLongLat = new LatLng( Double.valueOf((String) singleLocationData.get("latitude")), Double.valueOf((String) singleLocationData.get("longitude")) );
        Marker singleMarker = gmap.addMarker(new MarkerOptions().position(locationLongLat).title((String) singleLocationData.get("name")));
        singleMarker.showInfoWindow();
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLongLat, 17));
        gmap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
		}catch(Exception e){Log.i("SingleLocationCustomMap Main", e.getMessage() );}
	    
	}
	
	
	public void onSingleMapStreetViewRequest(HashMap<String,Object> singleLocationData){
		
		Log.i("MainActivity onSingleMapStreetViewRequest", "inside");
		CustomStreetViewFragment streetFragment = CustomStreetViewFragment.newInstance();
		streetFragment.setSingleLocationData(singleLocationData);
		
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.single_map, streetFragment, STREET_MAP_FRAGMENT);
        transaction.addToBackStack(null);
        transaction.commit();
		
	}
	
	public void onStreetMapLocationView(HashMap<String,Object> singleLocationData){
	
	 Log.i("MainActivity onStreetMapLocationView", "inside");
     StreetViewPanorama svPanorama = ((SupportStreetViewPanoramaFragment)
		        getSupportFragmentManager().findFragmentByTag(STREET_MAP_FRAGMENT)).getStreetViewPanorama();
     
     LatLng locationLongLat = new LatLng( Double.valueOf((String) singleLocationData.get("latitude")), Double.valueOf((String) singleLocationData.get("longitude")) );
     Log.i("MainActivity onStreetMapLocationView", String.format("%f %f", locationLongLat.latitude, locationLongLat.longitude));
     svPanorama.setPosition(locationLongLat);
		
	}
	
	public void onSingleMapAerialViewRequest(HashMap<String,Object> singleLocationData){
		
		Log.i("MainActivity onSingleMapAerialViewRequest", "inside");
		CustomMapFragment gmapFragment = CustomMapFragment.newInstance();
		gmapFragment.setSingleLocationData(singleLocationData);
		
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.single_map, gmapFragment, SINGLE_MAP_FRAGMENT);
        transaction.addToBackStack(null);
        transaction.commit();
		
	}
	
	
	
    @Override
    public void onLocationChanged(Location location) {
    	
    	this.userLocation = location;
    	Log.i("MainActivityOnLocationChanged", String.format("location changed: lat %f long %f", location.getLatitude(), location.getLongitude()) );
    	
    }
	
	@Override
	public void onConnected(Bundle bundle) {
	
		googlePlayServicesConnected = true;
		this.userLocation = mLocationClient.getLastLocation();
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
		
		if(this.connectionRetry){
			//onUserCenteredLocationsView();
			initGridHome();
			this.connectionRetry = false;
		}
		else{
			initGridHome();
		}
		
	}
	
    @Override
    public void onDisconnected() {
        
    	googlePlayServicesConnected = false;
    	
    }
    
	 @Override
	 public void onConnectionFailed(ConnectionResult connectionResult) {
		  
		 //FILL
		 
	 }
	 
	 @Override
	 public void onStop() {

	        if (mLocationClient.isConnected()) {
	        	mLocationClient.removeLocationUpdates(this);
	        }
	        mLocationClient.disconnect();

	        super.onStop();
	    }
	 
	 
	 
	 @Override
	 public boolean onCreateOptionsMenu(Menu menu) {
	     
	     MenuInflater inflater = getMenuInflater();
	     inflater.inflate(R.menu.main, menu);
	     return super.onCreateOptionsMenu(menu);
	 }
	 
	 
	 @SuppressLint("NewApi")
	 @Override
	 public boolean onOptionsItemSelected(MenuItem item) {
		 
	    int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			//Log.i("onOptionsItemSelected", String.format("%d", item.getItemId()) );
			//NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
	        //NavUtils.navigateUpFromSameTask(this);
		    if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
		        //this.finish();
		        startActivity(new Intent(this, MainActivity.class));
		    } else {
		    	getSupportFragmentManager().popBackStack();
		    }
			return true;
		} else if (itemId == R.id.home_icon) {
			//Log.i("onOptionsItemSelected home icon", String.format("%d", item.getItemId()) );
			startActivity(new Intent(this, MainActivity.class));
			return true;
		}
	     return super.onOptionsItemSelected(item);
	 }
	 
	 
	 
	 @Override
	 public void onConfigurationChanged(Configuration newConfig) {
	     super.onConfigurationChanged(newConfig);
	     setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	 } 
	 
	
    private void killOldMap() {
        SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.gmap));

        if(mapFragment != null) {
            FragmentManager fM = getSupportFragmentManager();
            int commit = fM.beginTransaction().remove(mapFragment).commit();
            Log.i("MainActivity", String.format("inside killOldMap not null %d", commit));
        }

    }


    private void resetMapMarker(){
    	
    	
    	if (this.activeMarker != null){
    	  int mapDrawable = getResources().getIdentifier(resolveCategoryName(0), "drawable", MainActivity.this.getPackageName());
		  this.activeMarker.setIcon(BitmapDescriptorFactory.fromResource(mapDrawable));
		  this.activeMarker.hideInfoWindow();
    	}

    	
    }
    
    public void setActiveMarker(Marker marker){
    	
    	this.activeMarker = marker;
        
    }
    
    private double calculateLatOffset(double lat, int meters){
    	
    	//Earth�s radius, sphere
    	int er = 6378137;
        double dLat = meters/er;
        
        return lat + dLat * 180/Math.PI;
    	
    }
    

    
}
