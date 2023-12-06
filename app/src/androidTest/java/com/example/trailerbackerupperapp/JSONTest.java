package com.example.trailerbackerupperapp;

import static org.junit.Assert.assertEquals;

import static Online.Packet.getDataFromJSONString;

import org.junit.Test;

import java.util.UUID;

import Online.DefaultOnlineCommands;
import Online.Packet;

public class JSONTest {

    @Test
    public void testPacketIdentity(){
        Packet actual = new Packet(
                DefaultOnlineCommands.CONTROL_SIGNAL + DefaultOnlineCommands.STEERING_ANGLE,
                1.2334456,
                UUID.randomUUID()
                );
        assertEquals("cnt;sa;", getDataFromJSONString(actual.toJSONString(),"command"));
        assertEquals(actual.toLongString(), Packet.fromJSONString(actual.toJSONString()).toLongString());
    }

    @Test
    public void getSimpleDataFromJSONString(){
        String jString = "{\"data\":\"object\"}";

        assertEquals("object", getDataFromJSONString(jString, "data"));
    }
    @Test
    public void getMoreDataFromJSONString(){
        String jString = "{\"data\":\"object\", \"data1\":\"object1\", \"data2\":\"object2\", \"data3\":\"object3\"}";

        assertEquals("object2", getDataFromJSONString(jString, "data2"));
    }
}
