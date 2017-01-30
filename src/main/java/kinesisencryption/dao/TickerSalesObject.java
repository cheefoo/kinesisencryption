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

    public String getTickerSymbol()
    {
        return tickerSymbol;
    }

    public void setTickerSymbol(String tickerSymbol)
    {
        this.tickerSymbol = tickerSymbol;
    }

    public String getSalesPrice()
    {
        return salesPrice;
    }

    public void setSalesPrice(String salesPrice)
    {
        this.salesPrice = salesPrice;
    }

    public String getOrderId()
    {
        return orderId;
    }

    public void setOrderId(String orderId)
    {
        this.orderId = orderId;
    }

    public String getTimeStamp()
    {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp)
    {
        this.timeStamp = timeStamp;
    }
}
