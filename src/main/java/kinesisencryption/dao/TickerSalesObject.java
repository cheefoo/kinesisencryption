package kinesisencryption.dao;

/**
 * Object that is read into Kinesis record to be encrypted before it is fed to a Kinesis Stream
 * 
 */
public class TickerSalesObject
{
    private String tickerSymbol;
    private String salesPrice;
    private String orderId;
    private String timeStamp;

    public TickerSalesObject(String tickerSymbol, String salesPrice, String orderId, String timeStamp)
    {
        this.tickerSymbol = tickerSymbol;
        this.salesPrice = salesPrice;
        this.orderId = orderId;
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString()
    {
        return "TickerSalesObject{" +
                "tickerSymbol='" + tickerSymbol + '\'' +
                ", salesPrice='" + salesPrice + '\'' +
                ", orderId='" + orderId + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                '}';
    }
}
