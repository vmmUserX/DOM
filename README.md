# JForex/JAVA
dom.java - Depth of Market, Strategy

DOM shows limit orders and cumulative horizontal volumes on the sides BID, ASK like Level II on the stock market.

<p align="center">
<img src="https://user-images.githubusercontent.com/40513889/59625567-48fe3880-9142-11e9-8c35-0626604a1fa6.jpg" alt="DOM"/>
</p>

There is an old riddle: "How to dance with a 500-pound gorilla?" Answer: "Any way she wants".
Any day you will see an active market makers. You need to know what they are doing. Whether they buy or sell at a certain price level? Do buying and selling with your own accounts or record transactions in the accounts of their customers? If the market makers take for themselves what they want to buy at a low price and sell high. This means that the purchase of the Instrument are on the bid (Buy Limit), and sales - ask (Sell Limit). As a rule, market makers buy when the market is down (Buy Limit) and sell (Sell Limit) when the market goes up, then there are the counterparty of the transaction. 
   With the advent of new technologies, a detailed study of the tape is gradually transformed into a field of study clusters. The cluster volume is, in fact, the aggregate value of the volume within a calculation period.
   
   
<b>Scaner_v1.java - Strategy</b>

<p align="center">
<img src="https://user-images.githubusercontent.com/40513889/59628320-11df5580-9149-11e9-9b7e-a43e1c5ec8cf.png" />
</p>

This strategy is not trading robot. This is a wrapper for any trading strategies. As an example, put my.

The main task of the scanner search for the signals at the main trading strategy.
The scanner looking for the signals for one pair and one period for the free version and in Historical tester only. This version inserts the positions manually. Manual entry into the market will provide more accurate one.

In paid version any number of instruments and periods in life or demo. I use 7 pairs for 3 periods M5-M15-H1. The ideal signals are obtained when signal 3 period at the same time. In this version, when you run the strategy in the list of signal appears as a green or red arrow and sound for Instrument. Go to the Instrument and to open a position manually. In this shell I can insert any strategy according to your strategy.

PitVolumeDivergence.java - Indicator

The indicator Pit Volume Divergence consists of two parts. One part above the zero level shows the volume of buy and sells for each bar . The second part are shown below the zero point is the difference between ASK>BID and BID>ASK.

Positioning in the terminal, one or two indicators with the upper and lower part.

This indicator is useful for those who uses in trade volumes. For example, VSA. Volume is the key to the truth. It is directly linked to the price movement and the relationship this complex. Figuratively speaking it is the powerhouse of the market.
   Suppose, on the chart, good rally, strong bull market, and there is a bar with narrow range and very high volume. Tell us about anything high volume? Yes, he says. Many retail traders are buying and your purchase price will not let down. And when many retail traders are buying, and then a smarts will sell and not put a price up. The narrow bar, the end of the upward movement of prices (Rally). The market is ready to fall. And no fundamental forces can not stop the market from falling.


