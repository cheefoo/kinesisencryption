package kinesisencryption.dao;


import java.io.Serializable;
/**
 * Example Object that is read into Kinesis record and used in test cases
 *
 */

public class BootCarObject implements Serializable
{
	private String name;
	private String year;
	private String odometer;




	public BootCarObject(String name, String year, String odometer)
	{
		super();
		this.name = name;
		this.year = year;
		this.odometer = odometer;
	}
	@Override
	public String toString()
	{
		return  name + "," + year + "," + odometer;
	}




}
