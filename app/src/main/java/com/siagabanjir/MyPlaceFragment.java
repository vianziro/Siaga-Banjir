package com.siagabanjir;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.siagabanjir.places.MyPlaces;

public class MyPlaceFragment extends Fragment implements OnMapClickListener,
		OnMapLongClickListener, ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener {
	public GoogleMap peta = null;
	private Marker currentMarker;
	private SupportMapFragment mapFragment;
	private Context context;
	private LocationClient locationClient;
	private LocationRequest locationRequest;
	private boolean locationEnabled;
	public boolean addingMyPlace;
	private ActionMode actionMode;
	private LocationManager locationManager;

	private HashMap<LatLng, String> myPlaces;
	private boolean longClickEnable;
	
	//Initialize zoom value
	private float previousZoomLevel = -1.0f;

	public MyPlaceFragment(Context context) {
		this.context = context;
		myPlaces = new MyPlaces(context).getPlaces();
		
		//Flurry log
		FlurryAgent.logEvent("View_MyPlace");

	}

	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.setHasOptionsMenu(true);
		View rootView = inflater.inflate(R.layout.fragment_myplace, container,
				false);
		

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentManager fm = getChildFragmentManager();
		mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
		if (mapFragment == null) {
			mapFragment = SupportMapFragment.newInstance();
			fm.beginTransaction().replace(R.id.map, mapFragment).commit();
		}

	}

	@Override
	public void onResume() {
		super.onResume();

		initializeMap();
		setupUserLocation();
		
	}

	private void setupUserLocation() {
		checkLocationService();
		
		locationClient = new LocationClient(context, this, this);
		locationRequest = new LocationRequest();

		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(5);
		locationRequest.setFastestInterval(1);

		LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				&& !locationManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			locationEnabled = false;
			Toast.makeText(context,
					"Enable location services for accurate data",
					Toast.LENGTH_SHORT).show();
		}

		else {
			locationEnabled = true;
		}

		locationClient.connect();

	}

	private void checkLocationService() {
		// TODO Auto-generated method stub
		
		boolean gpsEnabled = false, networkEnabled = false;
		
		if (locationManager == null) {
			locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		}
		
		try {
			gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
			
		}
		
		try {
			networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception ex) {
			
		}
		
		if(!gpsEnabled && !networkEnabled){
			AlertDialog.Builder dialog = new AlertDialog.Builder(context);
	        dialog.setMessage(getResources().getString(R.string.locationnotice));
	        dialog.setNegativeButton("Settings", new DialogInterface.OnClickListener() {

	            @Override
	            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
	                // TODO Auto-generated method stub
	                Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	                context.startActivity(myIntent);
	                //get gps
	            }
	        });
	        dialog.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {

	            @Override
	            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
	                // TODO Auto-generated method stub
	            	((MainActivity)getActivity()).switchTab(0);
	            }
	        });
	        
	        dialog.setCancelable(false);
	        dialog.show();

	    }
		
	}

	public OnCameraChangeListener getCameraChangeListener() {
		return new OnCameraChangeListener() {
			@Override
			public void onCameraChange(CameraPosition position) {
				
				if (previousZoomLevel != position.zoom) {
					//Flurry log					
					Map<String, String> zoomParams = new HashMap<String, String>();
					zoomParams.put("previous", "" + previousZoomLevel);
					zoomParams.put("Current", "" + position.zoom);
					FlurryAgent.logEvent("ZoomLevel", zoomParams, true);
					
					previousZoomLevel = position.zoom;
				}
			}
		};
	}
	

	private void initializeMap() {
		if (peta == null) {
			peta = mapFragment.getMap();

			// check if map is created successfully or not
			if (peta == null) {
				Toast.makeText(this.getActivity().getApplicationContext(),
						"Error showing map", Toast.LENGTH_SHORT).show();
			} else {
				peta.setOnMapLongClickListener(this);
				peta.setMyLocationEnabled(true);
				peta.getUiSettings().setMyLocationButtonEnabled(true);

				peta.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-6.2297465,106.829518), 11));
				
				previousZoomLevel = peta.getCameraPosition().zoom;
				peta.setOnMapLongClickListener(this);
				peta.setOnCameraChangeListener(getCameraChangeListener());
			}

		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		// locationClient.setMockMode(true);
		Location location = locationClient.getLastLocation();
		if (location != null) {
			//Flurry log
			HashMap<String, String> locParams = new HashMap<String, String>();
			locParams.put("Lat", "" + locationClient.getLastLocation().getLatitude());
			locParams.put("Long", "" +  locationClient.getLastLocation().getLongitude());
			
			FlurryAgent.logEvent("User_Location", locParams, true);
			
			
			
			Geocoder geocoder = new Geocoder(context);
			try {
				List<Address> address = geocoder.getFromLocation(
						location.getLatitude(), location.getLongitude(), 1);
				address.get(0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/** Toast.makeText(
					context,
					"Initial location: " + location.getLatitude() + ", "
							+ location.getLongitude(), Toast.LENGTH_LONG)
					.show(); **/
			LatLng currentLoc = new LatLng(location.getLatitude(),
					location.getLongitude());
			refreshMap(currentLoc);

		} else if (location == null && locationEnabled) {
			locationClient.requestLocationUpdates(locationRequest, this);
		}
	}

	public void refreshMap(LatLng currentLoc) {
		DataPintuAir.initLocation();
		
		myPlaces = new MyPlaces(context).getPlaces();

		peta.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15));

		MarkerOptions marker = new MarkerOptions();
		marker.position(currentLoc);
		Address addr;
		try {
			addr = new Geocoder(context).getFromLocation(
					marker.getPosition().latitude,
					marker.getPosition().longitude, 1).get(0);
			System.out.println();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// marker.draggable(true);
		marker.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_mylocation));
		marker.title("Current Location");
		marker.snippet("");

		peta.addMarker(marker);

		for (String name : DataPintuAir.locationPintuAir.keySet()) {
			LatLng loc = DataPintuAir.locationPintuAir.get(name);
			
			MarkerOptions markerPintuAir = new MarkerOptions().position(loc);
			markerPintuAir.snippet("");
			markerPintuAir.title("Pintu Air " + name);
			markerPintuAir.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location));
			
			peta.addMarker(markerPintuAir);
			/**
			 * CircleOptions circle = new CircleOptions();
			 * 
			 * int strokeColor = 0xffff0000; // red outline int shadeColor =
			 * 0x44ff0000;
			 * 
			 * circle.center(loc); circle.radius(4000.0f);
			 * circle.strokeColor(strokeColor); circle.fillColor(shadeColor);
			 * 
			 * peta.addCircle(circle);
			 **/
		}

		for (LatLng loc : myPlaces.keySet()) {
			MarkerOptions markerPlaces = new MarkerOptions().position(loc);
			markerPlaces.position(loc);
			markerPlaces.snippet("");
			markerPlaces.title(myPlaces.get(loc));
			markerPlaces.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.ic_savedlocation));
			
			peta.addMarker(markerPlaces);
		}

		peta.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker markerPlace) {
				LatLng loc = markerPlace.getPosition();
				
				Intent i = new Intent(context, RekomendasiFollowActivity.class);
				ArrayList<DataPintuAir> inArea = checkLocation(loc);
				i.putParcelableArrayListExtra("inarea", inArea);
				i.putExtra("nama", myPlaces.get(loc));
				i.putExtra("lat", loc.latitude);
				i.putExtra("long", loc.longitude);

				startActivityForResult(i, 1);
			}
		});

	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.myplacemap, menu);
		//super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case (1): {
			if (resultCode == Activity.RESULT_OK) {
				peta.clear();
				myPlaces = new MyPlaces(context).getPlaces();
				Location lastLoc = locationClient.getLastLocation();
				refreshMap(new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude()));
				peta.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(data.getDoubleExtra("lat", lastLoc.getLatitude()), data.getDoubleExtra("long", lastLoc.getLongitude())), 15));
				addingMyPlace = false;
				
				ArrayList<String> followed = data.getStringArrayListExtra("followed");
				String strFollowed = "";
				if (followed.size() == 2) {
					strFollowed = followed.get(0) + " dan " + followed.get(1);
				} else {
					for (int ii=0; ii<followed.size(); ii++) {
						if (ii != followed.size() - 1) {
							strFollowed += followed.get(ii);
							strFollowed += ", ";
						} else {
							strFollowed += "dan " + followed.get(ii);
						}
					}
				}
				
				if (!followed.isEmpty()) {
					Toast.makeText(context, getResources().getString(R.string.follownotif) + " " + strFollowed + ".", Toast.LENGTH_LONG).show();
				}
			}
			break;
		}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		locationClient.removeLocationUpdates(this);
		Toast.makeText(
				context,
				"Location: " + location.getLatitude() + ", "
						+ location.getLongitude(), Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onMapLongClick(LatLng newLoc) {
		// if (addingMyPlace) return;
		
		longClickEnable = true;

		if (currentMarker != null) {
			currentMarker.remove();
		}
		final LatLng loc = newLoc;
		final MarkerOptions marker = new MarkerOptions();
		marker.position(newLoc);
		marker.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_mylocation));
		currentMarker = peta.addMarker(marker);
		
		//Flurry log
		Map<String, String> newMarkerParams = new HashMap<String, String>();
		newMarkerParams.put("ZoomLevel", "" + peta.getCameraPosition().zoom);
		FlurryAgent.logEvent("NewMarker_ZoomLevel", newMarkerParams, true);
		
		final ArrayList<DataPintuAir> inArea = checkLocation(marker.getPosition());

		if (addingMyPlace)
			return;
		((ActionBarActivity) context).getSupportActionBar().setNavigationMode(
				ActionBar.NAVIGATION_MODE_STANDARD);
		
		
		
		actionMode = ((ActionBarActivity) context)
				.startSupportActionMode(new ActionMode.Callback() {

					@Override
					public boolean onPrepareActionMode(ActionMode mode,
							Menu menu) {
						// TODO Auto-generated method stub
						return false;
					}

					@Override
					public void onDestroyActionMode(ActionMode mode) {
						// TODO Auto-generated method stub
						
						if (addingMyPlace) {
							currentMarker.remove();
							((ActionBarActivity) context).getSupportActionBar()
									.setNavigationMode(
											ActionBar.NAVIGATION_MODE_TABS);
							addingMyPlace = false;
							
							Intent i = new Intent(MyPlaceFragment.this
									.getActivity().getBaseContext(),
									RekomendasiFollowActivity.class);
							i.putParcelableArrayListExtra("inarea", inArea);
							i.putExtra("lat", loc.latitude);
							i.putExtra("long", loc.longitude);
							startActivityForResult(i, 1);
							
							
						}
					}

					@Override
					public boolean onCreateActionMode(ActionMode mode, Menu menu) {
						// TODO Auto-generated method stub
						addingMyPlace = true;
						mode.getMenuInflater()
								.inflate(R.menu.add_actions, menu);

						return true;
					}

					@Override
					public boolean onActionItemClicked(ActionMode mode,
							MenuItem item) {
						// TODO Auto-generated method stub
						switch (item.getItemId()) {
						case R.id.action_add:
							disableActionMode();
							return true;
						}

						return false;
					}
				});
	}
	
	
	

	@Override
	public void onMapClick(LatLng arg0) {
		// TODO Auto-generated method stub
	}

	public static ArrayList<DataPintuAir> checkLocation(LatLng loc) {
		ArrayList<DataPintuAir> inArea = DataPintuAir.checkLocation(loc);

		/**
		 * String pintuAir = ""; for (String locName : inArea.keySet()) {
		 * pintuAir += locName + ", "; }
		 * 
		 * Toast.makeText( context, "Selected location: " +
		 * marker.getPosition().latitude + ", " + marker.getPosition().longitude
		 * + "\nNearest floodgates: " + pintuAir, Toast.LENGTH_LONG).show();
		 **/

		return inArea;

	}
	



	public void disableActionMode() {
		currentMarker.remove();
		addingMyPlace = false;
		longClickEnable = false;
		actionMode.finish();
		((ActionBarActivity) context).getSupportActionBar()
		.setNavigationMode(
				ActionBar.NAVIGATION_MODE_TABS);
		
	}

}
