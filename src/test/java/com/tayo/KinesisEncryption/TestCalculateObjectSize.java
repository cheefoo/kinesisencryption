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
       String real = "Volvo 740 GL, 134000, 2012";
        try
        {
            int sizeString = KinesisEncryptionUtils.calculateSizeOfObject(car.toString());

            int sizeObject = KinesisEncryptionUtils.calculateSizeOfObject(car);
            int sizeRealString = KinesisEncryptionUtils.calculateSizeOfObject(real);
            System.out.println("car size String is : " + sizeString);
            System.out.println("car size  Object : " + sizeObject);
            System.out.println("Real size  String : " + sizeRealString);
            Assert.assertTrue("Correct", sizeString>1);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
