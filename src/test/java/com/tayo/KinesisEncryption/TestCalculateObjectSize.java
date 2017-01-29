package com.tayo.KinesisEncryption;


import junit.framework.Assert;
import junit.framework.TestCase;
import kinesisencryption.dao.BootCarObject;
import kinesisencryption.utils.KinesisEncryptionUtils;

import java.io.IOException;

/**
 * Created by temitayo on 1/27/17.
 */
public class TestCalculateObjectSize extends TestCase
{
    BootCarObject car;

    public void setUp() throws Exception
    {
        super.setUp();
        car = new BootCarObject("Volvo 740 GL", "134000", "2012");

    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testCalculation()
    {
        try
        {
            int size = KinesisEncryptionUtils.calculateSizeOfObject(car);
            System.out.println("car size : " + size);
            Assert.assertTrue("Correct", size>1);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
