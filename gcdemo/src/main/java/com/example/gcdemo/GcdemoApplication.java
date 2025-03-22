package com.example.gcdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.SortedMap;
import java.util.TreeMap;

@SpringBootApplication
public class GcdemoApplication extends Object{

	public static void main(String[] args) {
		SpringApplication.run(GcdemoApplication.class, args);

	// to enable GC log
	//		edit -> configuration -> Add VM options
	//		add below configurations
	//		-Xlog:gc*,gc+phases=debug:file=gc.log:time,uptime,level,tags:filecount=5,filesize=10M

	}

}
