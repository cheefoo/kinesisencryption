package kinesisencryption.dao;

import java.io.Serializable;

/**
 * Example Object that is read into Kinesis record to be encrypted before it is fed to a Kinesis Stream
 *
 */
public class TickerSalesObject implements Serializable
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

    public TickerSalesObject()
    {

    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public void setTickerSymbol(String tickerSymbol) {
        this.tickerSymbol = tickerSymbol;
    }

    public String getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(String salesPrice) {
        this.salesPrice = salesPrice;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
