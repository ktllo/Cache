package org.leolo.test.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.leolo.util.Cache;

public class TestCache {

	@Test
	public void basic() {
		Cache<Integer, String> cache = new Cache<>();
		if(cache.size()!=0) fail("Init size");
		for(int i=0;i<1000;i++)
			cache.put(i, Integer.toString(i));
		if(cache.size()!=1000) fail("Incorrect size");
		cache.setMaxSize(500);
		cache.purge();
		if(cache.size()!=500) fail("Incorrect size post purge, size "+cache.size());
		cache.clear();
		if(cache.size()!=0) fail("Clear size");
	}
	
	@Test
	public void LRU() throws InterruptedException{
		Cache<Integer, String> cache = new Cache<>();
		if(cache.size()!=0) fail("Init size");
		for(int i=0;i<1000;i++)
			cache.put(i, Integer.toString(i));
		if(cache.size()!=1000) fail("Incorrect size");
		for(int i=0;i<1000;i+=2){
			String s = cache.get(i);
			if(!Integer.toString(i).equals(s)) fail("Incorrect Value");
			Thread.sleep(1);
		}
		cache.setMaxSize(500);
		cache.purge();
		if(cache.size()!=500) fail("Incorrect size post purge, size "+cache.size());
		for(int i=0;i<1000;i+=2){
			String s = cache.get(i);
			if(!Integer.toString(i).equals(s)) fail("Incorrect Value");
		}
		for(int i=1;i<1000;i+=2){
			String s = cache.get(i);
			if(s!=null) fail("Incorrect Value");
		}
		
	}
	
	@Test
	public void autoPurge() {
		Cache<Integer, String> cache = new Cache<>();
		if(cache.size()!=0) fail("Init size");
		cache.setMaxSize(500);
		cache.setAutoPurge(true);
		for(int i=0;i<1000;i++)
			cache.put(i, Integer.toString(i));
		if(cache.size()!=500) fail("Incorrect size");
		
		cache.purge();
		if(cache.size()!=500) fail("Incorrect size post purge, size "+cache.size());
		cache.clear();
		if(cache.size()!=0) fail("Clear size");
		cache.setAutoPurge(false);
		for(int i=0;i<1000;i++)
			cache.put(i, Integer.toString(i));
		if(cache.size()!=1000) fail("Incorrect size");
		cache.purge();
		if(cache.size()!=500) fail("Incorrect size post purge, size "+cache.size());
		cache.clear();
		if(cache.size()!=0) fail("Clear size");
	}
	
	@Test
	public void maxAge() throws InterruptedException{
		Cache<Integer, String> cache = new Cache<>();
		if(cache.size()!=0) fail("Init size");
		cache.setMaxSize(50000);
		cache.setPolicy(Cache.RemovalPolicy.LEAST_RECENTLY_USER_WITH_MAXIMUM_AGE);
		cache.setMaxAge(1000);
		for(int i=0;i<500;i++)
			cache.put(i, Integer.toString(i));
		Thread.sleep(1002);
		for(int i=500;i<1000;i++)
			cache.put(i, Integer.toString(i));
		if(cache.size()!=1000) fail("Incorrect size");
		cache.purge();
		if(cache.size()!=500) fail("Incorrect size post purge, size "+cache.size());
		for(int i=0;i<500;i++){
			String s = cache.get(i);
			if(s!=null) fail("Incorrect Value");
		}
		for(int i=500;i<1000;i++){
			String s = cache.get(i);
			if(!Integer.toString(i).equals(s)) fail("Incorrect Value");
		}
	}

}
