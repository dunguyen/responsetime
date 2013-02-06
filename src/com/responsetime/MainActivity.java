package com.responsetime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity implements android.view.View.OnClickListener{
	
	private Long stepSize = 50l; //stepsize in milliseconds
	
	private Long maxTimeNoExpNoLoad = 1000l; //max waiting time in milliseconds for no expectation and no loading bar
	private Long maxTimeNoExpLoad = 1000l;
	private Long maxTimeExpNoLoad = 1000l;
	private Long maxTimeExpLoad = 1000l;
	
	private int state = 0;
	
	private final int STATE_NO_EXP_NO_LOAD = 0;
	private final int STATE_NO_EXP_LOAD = 1;
	private final int STATE_EXP_NO_LOAD = 2;
	private final int STATE_EXP_LOAD = 3;
	
	private LinkedList<Long> noExpNoLoad;
	private LinkedList<Long> noExpLoad;
	private LinkedList<Long> expNoLoad;
	private LinkedList<Long> expLoad;

	private Button click;
	private LinearLayout llayout;
	private Button ok;
	private Button notOk;
	private TextView thankYou;
	private ProgressBar loadingBar;
	private Button send;
	
	private Editor editor;
	
	private long startTime;
	private long endTime;
	
	private Set<String> logSet;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
    }
    
    @Override
    protected void onResume()
    {
    	super.onResume();
    	SharedPreferences memory = this.getSharedPreferences("ResponseTime", 0);
        editor = memory.edit();
        
        click = (Button) findViewById(R.id.buttonClick);
        click.setOnClickListener(this);
        
        ok = (Button) findViewById(R.id.buttonOk);
        ok.setOnClickListener(this);
        
        notOk = (Button) findViewById(R.id.buttonNotOk);
        notOk.setOnClickListener(this);
        
        send = (Button) findViewById(R.id.buttonSend);
        send.setOnClickListener(this);
        
        llayout = (LinearLayout) findViewById(R.id.linearLayout);
        
        thankYou = (TextView) findViewById(R.id.textThankYou);
        
        loadingBar = (ProgressBar) findViewById(R.id.loadingBar);
        
        
        
        logSet = new HashSet<String>();
        
        noExpNoLoad = generateList(stepSize, maxTimeNoExpNoLoad);
        noExpLoad = generateList(stepSize, maxTimeNoExpLoad);
        expNoLoad = generateList(stepSize, maxTimeExpNoLoad);
        expLoad = generateList(stepSize, maxTimeExpLoad);
        
        thankYou.setVisibility(View.INVISIBLE);
        send.setVisibility(View.INVISIBLE);
        
        click.setVisibility(View.VISIBLE);
    }
    
    public LinkedList<Long> generateList(Long interval, Long max)
    {
    	ArrayList<Long> list = new ArrayList<Long>();
    
    	for(Long i = 0l; i < max; i = i + interval)
    	{
    		list.add(i);
    	}
    	
    	Collections.shuffle(list);
    	
    	return new LinkedList<Long>(list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void startTest()
    {
    	click.setVisibility(View.INVISIBLE);
    	
    	//wait for x milliseconds
    	startTime = System.currentTimeMillis();
    	
    	// TODO: loadingBar - didLoad = true/false
    	Long interval = 0l;
    	switch(state)
    	{
    	case STATE_NO_EXP_NO_LOAD:
    		try
    		{
        		interval = noExpNoLoad.removeFirst();
        		break;
    		}
    		catch(NoSuchElementException e)
    		{
    			state = STATE_NO_EXP_LOAD;
    		}
    	case STATE_NO_EXP_LOAD:
    		try
    		{
        		interval = noExpLoad.removeFirst();
        		loadingBar.setVisibility(View.VISIBLE);
        		break;
    		}
    		catch(NoSuchElementException e)
    		{
    			// state = STATE_EXP_NO_LOAD;
    			// click.setText("Load AMAZINGLY much data!!!!!11!!!1!");
    		}
    	case STATE_EXP_NO_LOAD:
    		try
    		{
        		interval = expNoLoad.removeFirst();
        		break;
    		}
    		catch(NoSuchElementException e)
    		{
    			state = STATE_EXP_LOAD;
    		}
    	case STATE_EXP_LOAD:
    		try
    		{
        		interval = expLoad.removeFirst();
        		loadingBar.setVisibility(View.VISIBLE);
        		break;
    		}
    		catch(NoSuchElementException e)
    		{
    		}
    	default:
    		break;
    	
    	}
    	
    	new WaitTask().execute(interval); // TODO
    }
    
    private class WaitTask extends AsyncTask<Long, Void, Boolean>
    {
		@Override
		protected Boolean doInBackground(Long... params) 
		{
			try 
			{
				Thread.sleep(params[0]);
				return true;
			} 
			catch (InterruptedException e) 
			{}
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) 
		{
			super.onPostExecute(result);
			
			loadingBar.setVisibility(View.INVISIBLE);
			
			if(result)
			{
				if(noExpLoad.isEmpty())
				{
					thankYou.setVisibility(View.VISIBLE);
					send.setVisibility(View.VISIBLE);
				}
				else
				{
			    	llayout.setVisibility(View.VISIBLE);
					endTime = System.currentTimeMillis();
				}
			}
		}
    	
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.buttonClick:
			startTest();
			break;
			
		case R.id.buttonOk:
			log(true);
			break;
			
		case R.id.buttonNotOk:
			log(false);
			break;
			
		case R.id.buttonSend:
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
	        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"madsbf@gmail.com"});
	        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, logSet.toString());
	        emailIntent.setType("plain/text");
	        startActivity(Intent.createChooser(emailIntent, "Send email..."));
	        
	        break;
		
		default:
			break;
		
		}
	}
	
	public void log(Boolean ok)
	{
		Boolean expected = false;
		if(!click.getText().equals("Click me!"))
		{
			expected = true;
		}
		
		logSet.add(state + ";" + (endTime - startTime) + ";" + ok);
		llayout.setVisibility(View.INVISIBLE);
		click.setVisibility(View.VISIBLE);
	}
	
	

    
}
