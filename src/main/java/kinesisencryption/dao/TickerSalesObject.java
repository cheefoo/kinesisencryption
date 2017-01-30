package kinesisencryption.dao;

/**
 * Created by temitayo on 1/29/17.
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
