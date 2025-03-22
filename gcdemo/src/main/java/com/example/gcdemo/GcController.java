package com.example.gcdemo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gc")
public class GcController {

    static List<Byte[]> oldList = new ArrayList<>();

    @GetMapping("/eden")
    public void increaseMemory() throws InterruptedException {
        List<Byte[]> list = new ArrayList<>();

        System.out.println("in GC controller...=========================");
        list.add(new Byte[82400]);
        Thread.sleep(1000);
        System.out.println("=======in GC controller...=========================");
    }

    @GetMapping("/oldgen")
    public void increaseoldGen() throws InterruptedException {

        System.out.println("in Old Gen GC controller...=========================");
        oldList.add(new Byte[42400]);
        Thread.sleep(1000);
        System.out.println("=======out Old Gen GC controller...=========================" + oldList.size());
    }
}
