package jforex;

import java.util.*;
import java.text.*;
import java.io.File;
import java.math.BigDecimal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


import java.awt.*;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.concurrent.Callable;
import java.util.Date;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.border.Border;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.dukascopy.api.*;
import com.dukascopy.api.drawings.*;
import com.dukascopy.api.IIndicators.AppliedPrice;
import com.dukascopy.api.IIndicators.MaType;
import com.dukascopy.api.indicators.IIndicator;
import com.dukascopy.api.drawings.IOhlcChartObject.*;
import com.dukascopy.api.drawings.ISignalDownChartObject;
import com.dukascopy.api.indicators.OutputParameterInfo.DrawingStyle;
import com.dukascopy.api.feed.util.TimePeriodAggregationFeedDescriptor;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.drawings.ICustomWidgetChartObject;
import com.dukascopy.api.JFException;
import com.dukascopy.api.feed.IFeedDescriptor;
/***************************************************************************
 * Author: vmm
 * Version: v1.0
 * Trade system Prof+_v1_withAlert
 * © 2018
 **************************************************************************/
@RequiresFullAccess
public class Scaner_v1_withAlert implements IStrategy {
    private Set<Instrument> selectedInstruments = new HashSet<Instrument>();
    private Set<Period> selectedPeriods = new HashSet<Period>();
    private IConsole console;
    private IHistory history;
    private IIndicators indicators;
    private IEngine engine;
    private IIndicator indicator;
    private IChart chart;
    private IChart chartAll;
    private IOrder order = null;
    private IOhlcChartObject cobj = null;
    private IUserInterface userInterface;
    private IChartObject Resist;
    private IChartObject Support;
    private IContext context;
    private IAccount account;
    private IChartObject upPriceLine;
    private IChartObject dnPriceLine;
    
    public final Locale BASE_LOCALE = Locale.US;
    private final Currency BASE_CURRENCY = Currency.getInstance(BASE_LOCALE);
    private double percentage = 2;

    //
    @Configurable("")
    public Instrument showInstrument = Instrument.USDJPY;
    @Configurable("")
    public Period period = Period.FIFTEEN_MINS;
    @Configurable("")
    public OfferSide side = OfferSide.BID;
    @Configurable("")
    public double Amount = 0.01;
    @Configurable("")
    public double StopLoss = 20;
    @Configurable("")
    public double TakeProfit = 80;
    @Configurable("")
    public double Slippage = 2;
    @Configurable("orders in hand")
    public boolean manual = false;
    @Configurable("Just selected instrument & period")
    public boolean selected = false;
    //@Configurable("")
    public Filter filter = Filter.NO_FILTER;
    @Configurable("")
    public int trailingStep = 10;
    @Configurable("Trailing Stop")
    public boolean TrailingStop = false;
    //@Configurable("ELLIOTT .jfx file")
    //public File jfxFile = new File("C:\\temp\\ElliottOscillator1.jfx");
    @Configurable("Alarm")
    public boolean alarm = true;
    @Configurable("Alarm file")
    public File alarmFile = new File("C:\\temp\\Coin.wav");
    //@Configurable("Show Pivot line R/S")
    public boolean semafor = false;
    //@Configurable("Shift")
    private int Shift = 1;
    //
    public AppliedPrice appliedPrice = AppliedPrice.MEDIAN_PRICE;
    //
    public static final int OPEN = 0;
    public static final int CLOSE = 1;
    public static final int HIGH = 2;
    public static final int LOW = 3;
    //
    private int counterPair = 0;
    private int counterAll = 0;
    private int count = 0;

    private String strMacd = "";
    private String strElliot = "";
    private String strBars = "";

    private String str = "";
    private String strStoch = "";
    private String strCCI = "";
    private String strBar = "";
    // Indicators
    // SAR
    private double acceleration = 0.02;
    private double maximum = 0.2;
    // EMA, WMA
    //private int emaTimePeriod = 55;
    private int wmaTimePeriod = 10;
    private int emaTimePeriod = 20;
    private int candlesBefore = 2;
    private int candlesAfter = 0;
    public Period periodMA = Period.ONE_HOUR;
    // Stochastic
    private int fastKPeriod = 14;
    private int slowKPeriod = 3;
    private MaType slowKMaType = MaType.SMA;
    private int slowDPeriod = 3;
    private MaType slowDMaType = MaType.SMA;
    private int shift = 0;
    // BBand
    private int timePeriod = 10;
    private MaType maType = MaType.EMA;
    private double nbDevDn = 2;
    private double nbDevUp = 2;
    // MACD
    private int macdFastPeriod = 5; //8;
    private int macdSlowPeriod = 34; //13;
    private int macdSignalPeriod = 5; //21;
    // ADX
    public int adxPeriod = 14;
    // CCI
    public int cciPeriod = 14;
    //ELLOSC
    private Object[] optInputs;
    private int FastMAPeriod = 5;
    private int SlowMAPeriod = 34;
    private int elloscMaType = MaType.SMA.ordinal();
    private String indName = "ELLOSC";
    private double[] VBar;
    // MACD
    private double fast1 = 0;
    private double signal1 = 0;
    private double hist1 = 0;
    private double fast0 = 0;
    private double signal0 = 0;
    private double hist0 = 0;

    private double fastH1 = 0;
    private double slowH1 = 0;
    private double histH1 = 0;
    private double fastH0 = 0;
    private double slowH0 = 0;
    private double histH0 = 0;
    // TII
    private Object[] tiiInputs;
    private int tiiPeriod = 28;
    private int tiiMaType = MaType.SMA.ordinal();
    // End Indicators

    public Color color = Color.PINK;
    public Color fastColor = Color.GREEN;
    public Color slowColor = Color.RED;
    private int horizontalPosition = javax.swing.SwingConstants.LEFT;
    private int verticalPosition = javax.swing.SwingConstants.TOP;
    private boolean direction;
    private String mainstream = "";
    private String strength = "";
    // Pivot Point
    private double PP = 0;
    private double R1 = 0;
    private double R2 = 0;
    private double R3 = 0;
    private double S1 = 0;
    private double S2 = 0;
    private double S3 = 0;
    private double valDown2 = 0;
    private double valDown3 = 0;
    private double pipValue = 0;
    private double memBar = 0;
    // Table
    public JTable countTable;
    public TradeTableModel tableModel;
    ICustomWidgetChartObject obj;
    public int m = 0; private int MAX_ROW = 0;
    String[][] dt;
    // Informer
    private static final Dimension INFORMER_SIZE = new Dimension(150, 250); //default size of the Informer object.
    private static final OhlcAlignment ALIGNMENT = OhlcAlignment.VERTICAL;  // OhlcAlignment.AUTO, OhlcAlignment.HORIZONTAL, or OhlcAlignment.VERTICAL.
    private static final Color FILL_COLOR = null;                           //fill color of the Informer object.
    private static final float FILL_OPACITY = 0.0f;                         //fill opacity of the Informer object.
    private boolean isAlive = false;
    // Orders
    //private Map<String, MyOrder> longOrders = new HashMap<String, MyOrder>();
    //private Map<String, MyOrder> shortOrders = new HashMap<String, MyOrder>();

    private double StopLossPrice = 0.0;
    private double TakeProfitPrice = 0.0;
    private double Price = 0;
    private double pipBuy = 0;
    private double pipSell = 0;
    private int countSell = 0;
    private int countBuy = 0;
    private OrderCommand orderCmd = null;
    private String orderType;
    private String orderKey;
    //private JTextField tAmount;
    //JTextField tSlip;
    //private JTextField tSL;
    //private JTextField tTP;
    private JLabel lCountSell = new JLabel();
    private JLabel lCountBuy = new JLabel();
    private JLabel lPipsBuy = new JLabel("Profit Buy",JLabel.LEFT);
    private JLabel lPipsSell = new JLabel("Profit Sell",JLabel.LEFT);
    private SimpleDateFormat gmtSdf = new SimpleDateFormat("HH:mm");
    private int pipScale;
    // Advanced Options
    private ButtonGroup jrbGroupOptions = new ButtonGroup();
    private JRadioButton jrbMarket = new JRadioButton();
    private JRadioButton jrbStopLimit = new JRadioButton();
    private JRadioButton jrbStopBYask = new JRadioButton();
    private JRadioButton jrbStopBYbid = new JRadioButton();
    private JSpinner jtPrice;
    private JSpinner tAmount;
    private JSpinner tSlip;
    private JSpinner tSL;
    private JSpinner tTP;
    private JLabel lTime = new JLabel("00:00");
    //
    private double pipValueUP = 0;
    private double pipValueDN = 0;
    
    public void onStart(IContext context) throws JFException {
        this.context = context;
        this.console = context.getConsole();
        this.history = context.getHistory();
        this.indicators = context.getIndicators();
        this.engine = context.getEngine();
        this.account = context.getAccount();

        chart = context.getChart(showInstrument);
        gmtSdf.setTimeZone(TimeZone.getTimeZone("GMT+3"));

        // avoid to run outside of Historical Tester
        //if (this.engine.getType() != IEngine.Type.TEST){
        //    JOptionPane.showMessageDialog(null, "This strategy only runs on Historical Tester !", "Strategy Alert",JOptionPane.WARNING_MESSAGE);
        //    this.context.stop();
        //}
        // Subscribe to the periods
        if (selected == true) {
            selectedPeriods.add(period);
        } else {
            selectedPeriods.add(Period.FIVE_MINS);
            selectedPeriods.add(Period.FIFTEEN_MINS);
            selectedPeriods.add(Period.ONE_HOUR);
            Set<Period> periods = (Set<Period>)((HashSet<Period>)selectedPeriods).clone();
        }
        // Subscribe to the instruments
        if ((this.engine.getType() == IEngine.Type.TEST) || selected == true) {
            selectedInstruments.add(showInstrument);
            //selectedInstruments.add(Instrument.USDJPY);
        } else {
            selectedInstruments.add(Instrument.EURUSD);
            selectedInstruments.add(Instrument.EURJPY);
            selectedInstruments.add(Instrument.EURCAD);
            selectedInstruments.add(Instrument.USDJPY);
            selectedInstruments.add(Instrument.USDCAD);
            selectedInstruments.add(Instrument.AUDUSD);
            selectedInstruments.add(Instrument.NZDUSD);
            selectedInstruments.add(Instrument.GBPUSD);
            selectedInstruments.add(Instrument.GBPCHF);
            selectedInstruments.add(Instrument.GBPJPY);
            selectedInstruments.add(Instrument.USDCHF);
            selectedInstruments.add(Instrument.EURCHF);

        }
        Set<Instrument> instruments = (Set<Instrument>)((HashSet<Instrument>)selectedInstruments).clone();
        context.setSubscribedInstruments(instruments, true);
        // wait max 1 second for the instruments to get subscribed
        //int i = 10;
        //while (!context.getSubscribedInstruments().containsAll(instruments) && i>=0) {
        //       try {
        //          console.getOut().println("Instruments not subscribed yet " + i);
        //          Thread.sleep(1000);
        //       } catch (InterruptedException e) {
        //          console.getOut().println(e.getMessage());
        //       }
        //       i--;
        //}
        counterPair = selectedInstruments.size();
        counterAll = selectedInstruments.size()*selectedPeriods.size();
        if (chart != null) {
            if (this.engine.getType() == IEngine.Type.TEST) {

                chart.add(indicators.getIndicator("FRACTAL"), new Object[] {5});
                chart.add(indicators.getIndicator("EMA"), new Object[] {emaTimePeriod}, new Color[] {new Color(32,106,6)}, new DrawingStyle[] { DrawingStyle.LINE }, new int[] {2});       // 20 Green
                //chart.add(indicators.getIndicator("WMA"), new Object[] {wmaTimePeriod}, new Color[] {new Color(255,200,0)},  new DrawingStyle[] { DrawingStyle.LINE }, new int[] {2});   // 12 Orange
                chart.add(indicators.getIndicator("MACD"), new Object[] {macdFastPeriod, macdSlowPeriod, macdSignalPeriod}, new Color[]{new Color(112,146,190), new Color(255,200,0), new Color(32,106,6)}, new DrawingStyle[] {DrawingStyle.LINE, DrawingStyle.LINE}, new int[] {1});

                chart.add(indicators.getIndicator("STOCH"), new Object[] {fastKPeriod, slowKPeriod, MaType.SMA.ordinal(), slowDPeriod, MaType.SMA.ordinal()}, new Color[]{new Color(112,146,190), new Color(255,200,0)}, new DrawingStyle[] {DrawingStyle.LINE, DrawingStyle.LINE}, new int[] {1});
                chart.add(indicators.getIndicator("CCI"), new Object[] {cciPeriod}, new Color[] {new Color(255,200,0)}, new DrawingStyle[] { DrawingStyle.LINE }, new int[] {1});
                chart.add(indicators.getIndicator("ELLOSC"), new Object[] {FastMAPeriod, SlowMAPeriod, elloscMaType}, new Color[]{new Color(32,106,6)},  new DrawingStyle[] {DrawingStyle.HISTOGRAM}, new int[] {1});
                //
                //chart.add(indicators.getIndicator("TII"), new Object[] {14, MaType.SMA.ordinal()});
                //chart.add(indicators.getIndicator("BBANDS"), new Object[] {12, 2.0,2.0, MaType.EMA.ordinal()});
                //chart.add(indicators.getIndicator("SAR"));
                //chart.add(indicators.getIndicator("BBANDS"), new Object[]{12, 2.0, 2.0, MaType.EMA.ordinal()}, new Color[]{Color.green, Color.yellow, Color.red}, new DrawingStyle[] {DrawingStyle.DOTS, DrawingStyle.LINE, DrawingStyle.DOTS}, new int[] {1});
                
                // ELLIOTT
                //indicator = context.getIndicators().getIndicator(indName);
                //optInputs = new Object[] {FastMAPeriod, SlowMAPeriod, elloscMaType};
                // TII
                //indicator = context.getIndicators().getIndicator("TII");
                //tiiInputs = new Object[] {tiiPeriod, tiicMaType};
            } else {
                //plot only if the chart matches the parameters that are used for indicator calculation
                if(chart.getSelectedPeriod() != this.period  ){
                   printErr("For proper indicator values please change chart period to "+ this.period);
                }
                if(chart.getSelectedOfferSide() != this.side ){
                   printErr("For proper indicator values please change chart side to "+ this.side);
                }
            }
            if(obj == null) {
               double bid = history.getLastTick(showInstrument).getBid();
               getInformer(bid);
            }

        } else {
                // Print start message
                this.chart.setCommentHorizontalPosition(horizontalPosition);
                this.chart.setCommentVerticalPosition(verticalPosition);
                this.chart.setCommentColor(color);
                this.chart.setCommentFont(new Font("Serif",Font.BOLD,12));
                this.chart.comment("Expert: Trading Prof+ v1.0 with alert! - " + this.period);
        }

        // set lines R/S   = 2 * PP + high - 2 * LOW
        if (semafor){
         for (Instrument instrument : instruments) {
             IBar bar = history.getBar(instrument, Period.DAILY, side.BID, Shift);
             PP = ((bar.getHigh()+bar.getLow()+bar.getClose())/3);
             R1 = ((PP*2)-bar.getLow());
             R2 = (PP+bar.getHigh()-bar.getLow());
             R3 = (PP*2+bar.getHigh()-bar.getLow()*2);
             S1 = ((PP*2)-bar.getHigh());
             S2 = (PP+bar.getLow()-bar.getHigh());
             S3 = (PP*2+bar.getLow()-bar.getHigh()*2);
             valDown2 = ((bar.getHigh()+bar.getLow()+bar.getClose()+bar.getClose())/4);
             valDown3 = ((S1+valDown2)/2);
             //print(instrument+": UP-"+String.format("%2.7s",valUp)+", DownD-"+String.format("%2.7s",valDown1)+", DownU-"+String.format("%2.7s",valDown2)+", DownM-"+String.format("%2.7s",valDown3));
             // set lines on chart
             chart = context.getChart(instrument);
             if (chart != null) {
                 chart.remove("Resistance");
                 Resist = chart.getChartObjectFactory().createHorizontalLine("Resistance", R1);
                 Resist.setColor(Color.RED);
                 Resist.setVisibleInWorkspaceTree(false);
                 Resist.setLineStyle(LineStyle.DASH_DOT);
                 Resist.setText(String.format("%2.7s",R1));
                 chart.addToMainChart(Resist);

                 chart.remove("Support");
                 Support = chart.getChartObjectFactory().createHorizontalLine("Support", S1);
                 Support.setColor(new Color(0x228b22));
                 Support.setVisibleInWorkspaceTree(false);
                 Support.setLineStyle(LineStyle.DASH_DOT);
                 Support.setText(String.format("%2.7s",S1));
                 chart.addToMainChart(Support);
             }
         }
        }
        print("Expert: Trading Prof+ v1.0 with alert! - " + this.period);
        //this.direction = true;
        //IBar Bar0 = this.history.getBar(showInstrument, this.period, this.side.BID, 0);
        //drawSignal(this.direction,showInstrument,Bar0,chart);
    }
    /***************************************************************************
    * LONG
    *  Macd
    *  Stochastic больше 20
    *  CCI больше -100
    * SHORT
    *  Macd
    *  Stochastic меньше 80
    *  CCI меньше 100
    *
    * Если гистограмма расположена ниже сигнальной линии, то мы встаем только в короткие позиции.
    * Если гистограмм - выше сигнальной линии, то мы занимаем длинные позиции.
    *
    * bband[0] - Upper Band
    * bband[1] - Middle Band
    * bband[2] - Lower Band
    *
    * stoch[0][1] - last candle fast %K value.
    * stoch[1][1] - last candle slow %D value.
    * stoch[0][0] - next-to-last candle fast % K value.
    * stoch[1][0] - next-to-last candle slow %D value.
    *
    * maccd[0][1] - last fast value.
    * maccd[1][1] - last signal value.
    * maccd[2][1] - last hist-гистограмма value.
    * левее бары
    * maccd[0][0] - next-to-last fast value.
    * maccd[1][0] - next-to-last signal value.
    * maccd[2][0] - next-to-last hist-гистограмма value.
    **************************************************************************/
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (count == 0){str = ""; m = 0; MAX_ROW = 0; dt = new String[counterAll][4];}
        if (selectedInstruments.contains(instrument))
        {
        
         if (selectedPeriods.contains(period)) {
         
         double bid = history.getLastTick(instrument).getBid();
         double ask = history.getLastTick(instrument).getAsk();    
         IBar Bar0 = this.history.getBar(instrument, this.period, this.side.BID, 0);
         // setOrder
         if (instrument == showInstrument && period == this.period){
             // STOP UP BY Ask
             if (jrbStopBYask.isSelected() && (NormalizeDouble(bidBar.getClose(),5) >= NormalizeDouble(pipValueUP,5))){
                 setOrderBuy();
             }
             // STOP DOWN BY Bid
             if (jrbStopBYbid.isSelected() && (NormalizeDouble(pipValueDN,5) >= NormalizeDouble(bidBar.getClose(),5))){
                 setOrderSell();
             }
         }
         //Set<IChart> charts = context.getCharts(instrument);
         //chart = context.getChart(instrument);
         //    if (chart != null){
         //        this.direction = true;
         //        print("Yes"+chart.getFeedDescriptor());
         //        IChartObjectFactory factory = chart.getChartObjectFactory();
         //        IChartDependentChartObject signal = this.direction ?
         //           factory.createSignalUp("signalUp_" + System.currentTimeMillis(), Bar0.getTime(), Bar0.getLow() - instrument.getPipValue())
         //           : factory.createSignalDown("signalDownKey_" + System.currentTimeMillis(), Bar0.getTime(), Bar0.getHigh() + instrument.getPipValue());
         //        signal.setText("", new Font("Monospaced", Font.BOLD, 10));
         //        signal.setColor(this.direction ? fastColor : slowColor);
         //        signal.setStickToCandleTimeEnabled(false);
         //        //chart.add(signal);
         //        chart.addToMainChart(signal);
         //   }

             //if (selectedInstruments.contains(chart.getInstrument())) {this.direction = true; drawSignal(this.direction,chart.getInstrument(),Bar0);}

         //if (period == Period.FIVE_MINS){
              count ++;
              IBar prevBar = this.history.getBar(instrument, this.period, this.side.BID, 1);
              //IBar Bar0 = this.history.getBar(instrument, this.period, this.side.BID, 0);
              //double[] ema = indicators.ema(instrument, this.period,  this.side, AppliedPrice.CLOSE, emaTimePeriod, Filter.NO_FILTER, 2, prevBar.getTime(), 0);
              double[][] macd = indicators.macd(instrument, this.period, this.side, AppliedPrice.CLOSE, macdFastPeriod, macdSlowPeriod, macdSignalPeriod, Filter.ALL_FLATS, 2, prevBar.getTime(), 0);
              fast1 = macd[0][0]; signal1 = macd[1][0]; ; hist1 = macd[2][0]; fast0 = macd[0][1]; signal0 = macd[1][1]; hist0 = macd[2][1];
              double[][] stochastic = indicators.stoch(instrument, this.period, this.side, fastKPeriod, slowKPeriod , slowKMaType, slowDPeriod, slowDMaType, Filter.NO_FILTER, 3, prevBar.getTime(), 1);
              //double[] cci07 = indicators.cci(instrument, this.period, this.side,  7, Filter.NO_FILTER, 2, prevBar.getTime(), 0 );
              double[] cci14 = indicators.cci(instrument, this.period, this.side, 14, Filter.NO_FILTER, 3, prevBar.getTime(), 1 );
              //double[][] macd1H = indicators.macd(instrument, period.ONE_HOUR, this.side, AppliedPrice.CLOSE, macdFastPeriod, macdSlowPeriod, macdSignalPeriod, Filter.ALL_FLATS, 2, prevBar.getTime(), 0);
              //fastH1 = macd1H[0][0]; slowH1 = macd1H[1][0]; ; histH1 = macd1H[2][0]; fastH0 = macd1H[0][1]; slowH0 = macd1H[1][1]; histH0 = macd1H[2][1];
              double[] bbands = indicators.bbands(instrument, this.period, side.BID, appliedPrice, timePeriod, nbDevUp, nbDevDn, maType, 1);
              //double[] adx5 = indicators.adx(instrument, this.period, side.BID, 5, Filter.ALL_FLATS, 5, prevBar.getTime(), 0);
              //double[] adx8 = indicators.adx(instrument, this.period, side.BID, 8, Filter.ALL_FLATS, 5, prevBar.getTime(), 0);
              double[] adx14 = indicators.adx(instrument, this.period, side.BID, adxPeriod, Filter.ALL_FLATS, 5, prevBar.getTime(), 0);
              double[] sar = indicators.sar(instrument, this.period, this.side, acceleration, maximum, Filter.NO_FILTER, 2, prevBar.getTime(), 0);
              // Основной тренд
              double wma  = indicators.ema(instrument, this.period, side.BID, AppliedPrice.CLOSE, 10, Filter.NO_FILTER, candlesBefore, prevBar.getTime(), candlesAfter)[0];
              double ema  = indicators.ema(instrument, this.period, side.BID, AppliedPrice.CLOSE, 20, Filter.NO_FILTER, candlesBefore, prevBar.getTime(), candlesAfter)[0];
              double ema0 = indicators.ema(instrument, this.period, side.BID, AppliedPrice.CLOSE, 20, Filter.NO_FILTER, candlesBefore, prevBar.getTime(), candlesAfter)[0];
              double ema1 = indicators.ema(instrument, this.period, side.BID, AppliedPrice.CLOSE, 20, Filter.NO_FILTER, candlesBefore, prevBar.getTime(), candlesAfter)[1];
              pipValue = showInstrument.getPipValue();     // Для JPY = 0.01, USD = 0.0001
              pipScale = showInstrument.getPipScale();     // Для JPY = 2, USD = 4
              //TII
              //tiiInputs = new Object[] { tiiPeriod, tiiMaType };
              //Object[] patternUni = indicators.calculateIndicator(instrument, this.period, new OfferSide[] { side }, "TII", new AppliedPrice[] { appliedPrice },tiiInputs, Filter.ALL_FLATS, 2, prevBar.getTime(), 0);
              //double[] tiiVar = (double[]) patternUni[0];
              //print(instrument+": 1 - "+VBar[0]);
              //print(instrument+": 2 - "+VBar[1]);
              //if (ema1 < ema)
              //      this.mainstream = "SHORT";
              //else if (VBar[0] < VBar[1] && VBar[0] > 80)
              //      this.mainstream = "LONG";
              // Old variant
              if (ema1 < ema0)
                    this.mainstream = "SHORT";
              else if (ema1 > ema0)
                    this.mainstream = "LONG";
              //
              //if (((ema - wma)/pipValue > 2) && (signal1 < 0))
              //     this.mainstream = "LONG-Trend";
              //else if (((wma - ema)/pipValue > 2) && (signal1 > 0))
              //     this.mainstream = "SHORT-Trend";
              //else if (((ema - wma)/pipValue > 2) && (signal1 < fast1))
              //     this.mainstream = "SHORT-Correlation";
              //else
              //     this.mainstream = "NEUTRAL";
              // Сила тренда
              if (adx14[1] < 15)
                  this.strength = "Trendless";
              else if (adx14[1] > 40)
                  this.strength = "High Trend";
              else if (adx14[1] > 24)
                  {
                  if (adx14[4] < adx14[1])
                      this.strength = "Trend";
                  else
                      this.strength = "End Trend";
                  }
              else if (adx14[1] > 15)
                  {
                  if (adx14[4] < adx14[1])
                      this.strength = "Start Trend";
                  else
                      this.strength = "Trendless";
                  }
              else
                  this.strength = "Undefined";

              str = ""; strStoch = ""; strCCI = ""; strElliot = ""; strMacd = ""; 
              //strBars = "";
              //if ((adx8[1] < adx8[4]) && (adx8[1] > 25)) {

                  // MACD + Stochastic + CCI

                  //double bid = history.getLastTick(instrument).getBid();

                  //print(instrument+": Short, " + NormalizeDouble((fast0-fast1)*100000,3));

              //chart = context.getChart(instrument);
              //if (chart != null){
                  
                  /******************************
                  * Sell
                  * Индикаторы сверху вниз
                  ******************************/
                  if ((stochastic[0][1] > 60) &&  (cci14[1] > 10)) {
                       //if ((fast0 > 0) && (fast1 > 0) && (fast1 > slow1) && (fast0 < slow0) && (fastH1 < slowH1)) {
                       //print(fast1+" - "+fast0+" - "+NormalizeDouble((fast1-fast0)*100000,3)+" - "+ema[1]+" - "+ema[0]+" - "+NormalizeDouble((ema[1]-ema[0])*100000,3));
                       //if ((stochastic[0][1] > 70) && (stochastic[1][1] > 70) && (cci14[1] > 10) && NormalizeDouble((fast1-fast0)*100000,3)>15)
                       //if ((stochastic[0][1] > 65) && (stochastic[1][1] > 65) && (cci14[1] > 10) && (bid < sar))

                       /******************************
                       * MACD больше 0 и наклон линий вниз
                       * EMA 10 выше EMA 20
                       ******************************/
                       if ((fast0 > 0) && ((wma - ema)/pipValue > 2))
                       //if ((fast0 > 0) && (fast1 > fast0) && ((wma - ema)/pipValue > 2))
                       {
                           //print(fast1+" - "+fast0+" - "+NormalizeDouble((fast1-fast0)*100000,3)+" - "+ema[1]+" - "+ema[0]+" - "+NormalizeDouble((ema[1]-ema[0])*100000,3));
                           if (this.mainstream == "SHORT"){
                               if (bbands[0] < bid){
                                   dt[m][0] = instrument.name()+":"+period;
                                   dt[m][1] = "▼ SHORT-Trend";
                                   dt[m][2] = this.mainstream;
                                   dt[m][3] = this.strength;
                                   m ++; MAX_ROW += 1;
                                   str = instrument+":"+period+": SHORT-Trend/"+this.mainstream+", " + this.strength;
                                   chart = context.getChart(instrument);this.direction = false; drawSignal(this.direction,instrument,Bar0,chart,period,"SB");
                                   if (selected && manual) setData(this.direction, instrument, prevBar);
                               } else if (fast1 > fast0){
                                   dt[m][0] = instrument.name()+":"+period;
                                   dt[m][1] = "▼ SHORT-Trend";
                                   dt[m][2] = this.mainstream;
                                   dt[m][3] = this.strength;
                                   m ++; MAX_ROW += 1;
                                   str = instrument+":"+period+": SHORT-Trend/"+this.mainstream+", " + this.strength;
                                   chart = context.getChart(instrument);this.direction = false; drawSignal(this.direction,instrument,Bar0,chart,period,"ST");
                                   if (selected && manual) setData(this.direction, instrument, prevBar);
                               }
                           } else  if (this.mainstream == "LONG"){
                               if (bbands[0] < bid){
                                   dt[m][0] = instrument.name()+":"+period;;
                                   dt[m][1] = "▼ SHORT-Trend";
                                   dt[m][2] = this.mainstream;
                                   dt[m][3] = this.strength;
                                   m ++; MAX_ROW += 1;
                                   str = instrument+":"+period+": SHORT-Trend/"+this.mainstream+", " + this.strength;
                                   chart = context.getChart(instrument);this.direction = false; drawSignal(this.direction,instrument,Bar0,chart,period,"LB");
                                   if (selected && manual) setData(this.direction, instrument, prevBar);
                               } //else if (sar[1] < bid && sar[0] < bid){
                                 //  dt[m][0] = instrument.name()+":"+period;;
                                 //  dt[m][1] = "▼ SHORT-Correlation";
                                 //  dt[m][2] = this.mainstream;
                                 //  dt[m][3] = this.strength;
                                 //  m ++; MAX_ROW += 1;
                                 //  str = instrument+":"+period+": SHORT-Correlation/"+this.mainstream+", " + this.strength;
                                 //  chart = context.getChart(instrument);this.direction = false; drawSignal(this.direction,instrument,Bar0,chart,period,"LC");
                               //}
                               
                               //if(chart.comment("")==""){
                               //}
                               // if(instrument.name() == showInstrument.name()) {this.direction = false; drawSignal(this.direction,instrument,Bar0);}
                           }
                       }
                       //else if ((fast0 < 0) && (fast1 > fast0) && ((ema - wma)/pipValue > 2))
                       //{
                       //    str = instrument+": SHORT-Correlation/"+this.mainstream+", " + this.strength;
                       //    if(instrument.name() == showInstrument.name()) {this.direction = false; addSignal(this.direction,showInstrument,Bar0);}
                       //}
                  }

                  /******************************
                  * Buy
                  * Индикаторы снизу вверх
                  ******************************/
                  if ((stochastic[0][0] < 40) && (cci14[1] < -10)) {
                       //if ((fast0 < 0) && (fast1 < 0) && (fast1 < slow1) && (fast0 > slow0) && (fastH1 > slowH1)) {
                       //print(fast0+" - "+fast1+" - "+NormalizeDouble((fast0-fast1)*100000,3)+" - "+ema[0]+" - "+ema[1]+" - "+NormalizeDouble((ema[0]-ema[1])*100000,3));
                       //if ((fast0 < slow0) && (hist0 < 0) && (fast0 < 0) || (fast1 < slow1) && (fast0 > slow0) && (hist0 < 0) && (fast0 < 0))
                       //if ((stochastic[0][1] < 30) && (stochastic[1][1] < 30) && (cci14[1] < -10) && NormalizeDouble((fast0-fast1)*100000,3)>15)
                       //if ((stochastic[0][1] < 35) && (stochastic[1][1] < 35) && (cci14[1] < -10) && (bid > sar))

                       /******************************
                       * MACD меньше 0 и наклон линий вверх
                       * EMA 20 выше EMA 10
                       ******************************/
                       if ((fast0 < 0) && ((ema - wma)/pipValue > 2))
                       //if ((fast0 < 0) && (fast1 < fast0) && ((ema - wma)/pipValue > 2))
                       {
                           //printhis.mainstreamt(fast0+" - "+fast1+" - "+NormalizeDouble((fast0-fast1)*100000,3));
                           if (this.mainstream == "LONG"){
                               if (bbands[2] > bid){    
                                   dt[m][0] = instrument.name()+":"+period;;
                                   dt[m][1] = "▲ LONG-Trend";
                                   dt[m][2] = this.mainstream;
                                   dt[m][3] = this.strength;
                                   m ++; MAX_ROW += 1;
                                   str = instrument+":"+period+": LONG-Trend/"+this.mainstream+", " + this.strength;
                                   chart = context.getChart(instrument);this.direction = true; drawSignal(this.direction,instrument,Bar0,chart,period,"LB");
                                   if (selected && manual) setData(this.direction, instrument, prevBar);
                               } else if (fast1 < fast0){
                                   dt[m][0] = instrument.name()+":"+period;;
                                   dt[m][1] = "▲ LONG-Trend";
                                   dt[m][2] = this.mainstream;
                                   dt[m][3] = this.strength;
                                   m ++; MAX_ROW += 1;
                                   str = instrument+":"+period+": LONG-Trend/"+this.mainstream+", " + this.strength;
                                   chart = context.getChart(instrument);this.direction = true; drawSignal(this.direction,instrument,Bar0,chart,period,"LT");
                                   if (selected && manual) setData(this.direction, instrument, prevBar);
                               }           
                           } else if (this.mainstream == "SHORT"){
                              if (bbands[2] > bid){    
                                   dt[m][0] = instrument.name()+":"+period;;
                                   dt[m][1] = "▲ LONG-Trend";
                                   dt[m][2] = this.mainstream;
                                   dt[m][3] = this.strength;
                                   m ++; MAX_ROW += 1;
                                   str = instrument+":"+period+": LONG-Trend/"+this.mainstream+", " + this.strength;
                                   chart = context.getChart(instrument);this.direction = true; drawSignal(this.direction,instrument,Bar0,chart,period,"SB");
                                   if (selected && manual) setData(this.direction, instrument, prevBar);
                               } //else if (sar[1] < bid && sar[0] < bid){
                                 //  dt[m][0] = instrument.name()+":"+period;;
                                 //  dt[m][1] = "▲ LONG-Trend";
                                 //  dt[m][2] = this.mainstream;
                                 //  dt[m][3] = this.strength;
                                 //  m ++; MAX_ROW += 1;
                                 //  str = instrument+":"+period+": LONG-Trend/"+this.mainstream+", " + this.strength;
                                 //  chart = context.getChart(instrument);this.direction = true; drawSignal(this.direction,instrument,Bar0,chart,period,"SS");
                                 //  if (selected && manual) setData(this.direction, instrument, prevBar);
                               //} //else if (sar[1] > bid && sar[0] > bid){
                                  // dt[m][0] = instrument.name()+":"+period;;
                                  // dt[m][1] = "▲ LONG-Correlation";
                                  // dt[m][2] = this.mainstream;
                                  // dt[m][3] = this.strength;
                                  // m ++; MAX_ROW += 1;
                                  // str = instrument+":"+period+": LONG-Correlation/"+this.mainstream+", " + this.strength;
                                   //chart = context.getChart(instrument);this.direction = true; drawSignal(this.direction,instrument,Bar0,chart,period,"SC");
                               //}    
                           }
                       }
                       //else if ((fast0 > 0) && (fast1 < fast0) && ((wma - ema)/pipValue > 2))
                       //{
                       //    str = instrument+": LONG-Correlation/"+this.mainstream+", " + this.strength;
                       //    if(instrument.name() == showInstrument.name()) {this.direction = true; addSignal(this.direction,showInstrument,Bar0);}
                       //}
                  }
              //}
              //chart = context.getChart(showInstrument);
              //} else {
                  //  No Trend
              //}

              //if (strBars != "") {print("MinMaxBar: "+strBars);}
              //if ((strElliot != "")) {print("Elliott: "+strElliot);}
              //if ((strCCI != "")) {print("CCI: "+strCCI);}
              //if ((strMacd != "")) {print("Macd: "+strMacd);}
              // empty of fields
              //if (m < counter) {
              //    dt[m][0] = instrument.name();
              //    dt[m][1] = "";
              //    dt[m][2] = "";
              //    dt[m][3] = "";
              //    m ++;
              //}
              if ((str != "")) {
                   print(str);
                   chart = context.getChart(showInstrument);
              }
            }      // End period
            //if (str != "") print("End "+showInstrument);
            }      // End instrument
              //print(m+": "+counterPair+" - "+count+" - "+MAX_ROW);
              if (counterPair == count){
                  //if(chart == null){
                  //   chart = context.openChart(new TimePeriodAggregationFeedDescriptor(showInstrument, this.period, this.side, this.filter));
                  //}
                  // copy dt[][] into new array dd[]][]
                  if ((str != "")) {playSound(alarmFile);}
                  String[][] dd = new String[MAX_ROW][4];
                  if (MAX_ROW > 0){
                      int k = 0;

                      //print(MAX_ROW);
                      for (int i=0; i<m; i++){
                           if (dt[i][2]!=""){
                               for (int j=0; j<4; j++)
                                   {dd[k][j]=dt[i][j];}
                               k +=1;
                           }
                      }

                  }
                  tableModel.setData(dd);
                  //if (str != "") {
                  //    if (alarm) playSound(alarmFile);
                  //}
                 count = 0; str = ""; semafor = false; //strStoch = "";  strCCI = "";  strElliot = ""; strMacd = "";
              }  // End count instrument
          //}      // End period
        //}        // End instrument
    }

    public void onAccount(IAccount account) throws JFException {
        //AccountCurrency = account.getCurrency().toString();        // Счёт в валюте
        //Leverage = account.getLeverage();                          // Плечо = 100
        //AccountId= account.getAccountId();                         // Номер счёта
        //Equity = account.getEquity();                              // Депозит
        //UseofLeverage = account.getUseOfLeverage();                // Задействованная маржа
        //OverWeekendEndLeverage = account.getOverWeekEndLeverage(); // Плечо в выходные дни = 30
        //MarginCutLevel = account.getMarginCutLevel();              // Маржин колл ниже которого закрываются ордера банком
        //GlobalAccount = account.isGlobal();                        // Глобальный счёт
    }
    /******************************
    * context.getConsole()
    * getOut
    * getErr - Messages are shown in red color
    * getWarn - Messages by default are shown in yellow color
    * getInfo - Messages by default are shown in green color
    * getNotif - Messages by default are shown in blue color
    ******************************/
    public void onMessage(IMessage message) throws JFException {

      try {
         //print(message.getType());
         switch(message.getType()) {
           case ORDER_SUBMIT_OK:
               print("Order "+message.getOrder().getLabel()+" Status: "+message.getOrder().getState());
            break;
           case ORDER_FILL_OK:
            if(message.getOrder().getOrderCommand() == OrderCommand.BUY || message.getOrder().getOrderCommand() == OrderCommand.BUYLIMIT 
               || message.getOrder().getOrderCommand() == OrderCommand.BUYSTOP || message.getOrder().getOrderCommand() == OrderCommand.BUYSTOP_BYBID){
               lCountBuy.setText("<html><span style='font-size:11pt;color:white;'><b>"+countBuy+"</b></span></html>");
               //longOrders.put(order.getLabel(), new MyOrder(order));
               print("Order "+message.getOrder().getLabel()+" opened: "+message.getOrder().getOpenPrice());
            } else if(message.getOrder().getOrderCommand() == OrderCommand.SELL || message.getOrder().getOrderCommand() == OrderCommand.SELLLIMIT 
                      || message.getOrder().getOrderCommand() == OrderCommand.SELLSTOP || message.getOrder().getOrderCommand() == OrderCommand.SELLSTOP_BYASK){
               lCountSell.setText("<html><span style='font-size:11pt;color:white;'><b>"+countSell+"</b></span></html>");
               //shortOrders.put(order.getLabel(), new MyOrder(order));
               print("Order "+message.getOrder().getLabel()+" opened: "+message.getOrder().getOpenPrice());
            }
            break;
           case ORDER_CLOSE_OK:
             if(message.getOrder().getOrderCommand() == OrderCommand.BUY || message.getOrder().getOrderCommand() == OrderCommand.BUYLIMIT 
                || message.getOrder().getOrderCommand() == OrderCommand.BUYSTOP || message.getOrder().getOrderCommand() == OrderCommand.BUYSTOP_BYBID){
               countBuy--; pipBuy = 0; if (countBuy < 0) countBuy = 0;
               lPipsBuy.setText("<html><span style='font-size:11pt;color:white;'>pips: <b>"+String.valueOf(pipBuy)+"</b></span></html>");
               lCountBuy.setText("<html><span style='font-size:11pt;color:white;'><b>"+countBuy+"</b></span></html>");
               print("Order "+message.getOrder().getLabel()+" fully closed: "+order.getClosePrice());
            } else if(message.getOrder().getOrderCommand() == OrderCommand.SELL || message.getOrder().getOrderCommand() == OrderCommand.SELLLIMIT 
                      || message.getOrder().getOrderCommand() == OrderCommand.SELLSTOP || message.getOrder().getOrderCommand() == OrderCommand.SELLSTOP_BYASK){
               countSell--; pipSell = 0; if (countSell < 0) countSell = 0;
               lPipsSell.setText("<html><span style='font-size:11pt;color:white;'>pips: <b>"+String.valueOf(pipSell)+"</b></span><html>");
               lCountSell.setText("<html><span style='font-size:11pt;color:white;'><b>"+countSell+"</b></span></html>");
               print("Order "+message.getOrder().getLabel()+" fully closed: "+order.getClosePrice());
            }
            break;
         }
      } catch (Exception e) {
            e.printStackTrace();
            throw new JFException(e);
      }
    }

    public void onStop() throws JFException {
       //chart.comment("");
       chart.remove("Informer");
       chart.remove("Stop up");  
       chart.remove("Stop down");
       chart.remove(obj);
       //myForm.setVisible(false);
       //myForm.dispose();
    }

    public void onTick(Instrument instrument, ITick tick) throws JFException {
       /******************************
       * Остаток времени до закрытия свечи
       ******************************/
       if (!instrument.equals(showInstrument))
         return;
       //time in milliseconds
       long interval = chart.getSelectedPeriod().getInterval();
       long periodStart = tick.getTime() - tick.getTime() % interval;
       long periodEnd = periodStart + interval;
       //time in seconds
       double remainingTime = Double.valueOf( periodEnd - tick.getTime());
       SimpleDateFormat dt = new SimpleDateFormat("mm:ss");
       lTime.setText(dt.format(remainingTime));
       /******************************
       * End Время до закрытия
       ******************************/
       if (order != null) {
            if(order.getState() == IOrder.State.FILLED || order.getState() == IOrder.State.OPENED){
               pipValue = instrument.getPipValue(); pipBuy = 0; pipSell = 0;
               for(IOrder o: engine.getOrders()){
                   if(o.getOrderCommand() == OrderCommand.BUY){
                      pipBuy = NormalizeDouble(pipBuy + (tick.getBid() - o.getOpenPrice())/pipValue,1);
                   } else if(o.getOrderCommand() == OrderCommand.SELL) {
                      pipSell =  NormalizeDouble(pipSell + (o.getOpenPrice() - tick.getBid())/pipValue,1);
                   }
               }
               lPipsBuy.setText("<html><span style='font-size:11pt;color:white;'>pips: <b>"+String.valueOf(pipBuy)+"</b></span></html>");
               lPipsSell.setText("<html><span style='font-size:11pt;color:white;'>pips: <b>"+String.valueOf(pipSell)+"</b></span></html>");
            }
       }
    }


    //

    double NormalizeDouble(double val, int prec) {
       String pattern = "0.0";
       for(int i=1;i<prec;i++){
           if(i==prec-1) pattern += "#";
           else pattern += "0";
       }
       DecimalFormat applydecim = new DecimalFormat(pattern);
       Double ret = new Double(applydecim.format(val).replace(',', '.')).doubleValue();
       return(ret);
    }
    private static String arrayToString(double[][] arr) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < arr.length; r++) {
            for (int c = 0; c < arr[r].length; c++) {
                sb.append(String.format("[%s][%s] %.5f; ",r, c, arr[r][c]));
            }
            sb.append("; ");
        }
        return sb.toString();
    }
    private void playSound(File wavFile) throws JFException {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(wavFile);
            AudioFormat af = audioInputStream.getFormat();
            int nSize = (int) (af.getFrameSize() * audioInputStream.getFrameLength());
            byte[] audio = new byte[nSize];
            DataLine.Info info = new DataLine.Info(Clip.class, af, nSize);
            audioInputStream.read(audio, 0, nSize);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(af, audio, 0, nSize);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
            throw new JFException(e);
        }
    }
    private void drawSignal(boolean isLong, Instrument instrument, IBar previousBar, IChart chartX, Period per, String val) throws JFException {
        if (chartX != null){

            IChartObjectFactory factory = chartX.getChartObjectFactory();
                        IChartDependentChartObject signal = isLong
                        ? factory.createSignalUp("UP_"+lastTime(instrument), previousBar.getTime(), previousBar.getLow() - instrument.getPipValue())
                        : factory.createSignalDown("DOWN_"+lastTime(instrument), previousBar.getTime(), previousBar.getHigh() + instrument.getPipValue());
            signal.setText(val, new Font("Monospaced", Font.BOLD, 10));
            signal.setColor(isLong ? fastColor : slowColor);
            signal.setStickToCandleTimeEnabled(false);
            //if( val == "T") signal.setText("T"); else signal.setText("C");
            //signal.setText(per.name()); signal.setTooltip(per.name());
            //if (this.engine.getType() == IEngine.Type.TEST) {
            //    chart.remove(signal.getKey());
            //} else {
                IChartObject existSignal = isLong
                        ? (IChartObject) chart.get("UP_"+lastTime(instrument))
                        : (IChartObject) chart.get("DOWN_"+lastTime(instrument));
                if (existSignal != null) {
                    chart.remove(signal.getKey());
                }
            //}
            chartX.addToMainChart(signal);
                //for( IChartObject obj : chartX.getAll()){
                //    if (obj.getKey().startsWith(lastTime(instrument))) {
                //        chart.remove(signal.getKey());
                //    }
                //}
            //}
            //chartX.addToMainChart(signal);
            //print("generated key: " + signal.getKey());
            //chartX.addToMainChart(signal);
            //print("generated key: " + signal.getKey());
        }
    }
    public String lastTime(Instrument instrument) throws JFException {
           SimpleDateFormat gmtSdf = new SimpleDateFormat("HH:mm",Locale.ENGLISH);
           gmtSdf.setTimeZone(TimeZone.getTimeZone("GMT+3"));
           long from = System.currentTimeMillis();
           String output = instrument.name()+"_"+gmtSdf.format(from);
           return output;
    }
    private double getLot(Instrument instrument) throws JFException{
        double equity = account.getEquity();
        if(!BASE_CURRENCY.equals(account.getCurrency())){
           double bcBid = this.history.getLastTick(Instrument.fromString(account.getCurrency()+"/"+BASE_CURRENCY)).getBid();
           equity *= bcBid;
           //console.getOut().println("we did get through this !");
        }
        double latestBid =  this.history.getLastTick(showInstrument).getBid();
        double limit = equity * percentage;
        double lot = (new BigDecimal(limit/latestBid/(10000))).setScale(4, BigDecimal.ROUND_DOWN).doubleValue();
        //myConsole.getOut().println("Equity:- " + myAccount.getEquity());
        //myConsole.getOut().println("BASE_CURRENCY:- " + BASE_CURRENCY);
        //myConsole.getOut().println("latestBid:- " + latestBid);
        //myConsole.getOut().println("myInstrument:- " + myInstrument);
        //myConsole.getOut().println("Lot Size:- " + lot);
        return lot;
    }
    private void setData(boolean isLong, Instrument instrument, IBar previousBar) throws JFException {
        int var = 1;
        if (jrbMarket.isSelected()) {
            if (isLong){
                Price = previousBar.getClose();
                jtPrice.setValue(Price);
                tAmount.setValue(Amount);
                tSL.setValue(StopLoss);
                tTP.setValue(TakeProfit);
            } else {
                Price = previousBar.getClose();
                jtPrice.setValue(Price);
                tAmount.setValue(Amount);
                tSL.setValue(StopLoss);
                tTP.setValue(TakeProfit);
            }
        } else if (jrbStopBYask.isSelected() || jrbStopBYbid.isSelected()) {
            if (isLong){
                Price = NormalizeDouble((previousBar.getHigh() + (pipValue*0)),pipScale);
                jtPrice.setValue(Price);
                tAmount.setValue(getLot(instrument));
                tSL.setValue((Price - chart.getMinPrice())*100+var);
                tTP.setValue((chart.getMaxPrice() - Price)*100-var);
            } else {
                Price = NormalizeDouble((previousBar.getLow() - (pipValue*0)),pipScale);
                jtPrice.setValue(Price);
                tAmount.setValue(getLot(instrument));
                tSL.setValue((chart.getMaxPrice() - Price)*100-var);
                tTP.setValue((Price - chart.getMinPrice())*100+var);
            }
        } else if (jrbStopLimit.isSelected()) {
            if (isLong){
                Price = NormalizeDouble((previousBar.getLow() - (pipValue*0)),pipScale);
                jtPrice.setValue(Price);
                tAmount.setValue(getLot(instrument));
                tSL.setValue((Price - chart.getMinPrice())*100+var);
                tTP.setValue((chart.getMaxPrice() - Price)*100-var);
            } else {
                Price = NormalizeDouble((previousBar.getHigh() + (pipValue*var)),pipScale);
                jtPrice.setValue(Price);
                tAmount.setValue(getLot(instrument));
                tSL.setValue((chart.getMaxPrice() - Price)*100-var);
                tTP.setValue((Price - chart.getMinPrice())*100+var);
            }
        }

    }
    /******************************
    * Info
    * rmer for trading of value
    ******************************/
    private void getInformer(double bid) {
       //TradeTableModel tableModel;
       tableModel = new TradeTableModel();
       countTable = new JTable(tableModel);

       countTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
       countTable.setColumnSelectionAllowed(false);
       countTable.getTableHeader().setReorderingAllowed(false); // запрет на перетаскивание колонок
       countTable.editingStopped(null);
       countTable.removeEditor();
       countTable.removeNotify();
       //Изменяем высоту строк таблицы
       //table.setRowHeight(0,10);
       //Изменяем ширину столбцов таблицы
       countTable.getColumnModel().getColumn(0).setPreferredWidth(100);
       countTable.getColumnModel().getColumn(1).setPreferredWidth(110);
       countTable.getColumnModel().getColumn(2).setPreferredWidth(70);
       countTable.getColumnModel().getColumn(3).setPreferredWidth(60);
       countTable.getColumnModel().getColumn(0).setResizable(false);
       countTable.getColumnModel().getColumn(1).setResizable(false);
       countTable.getColumnModel().getColumn(2).setResizable(false);
       countTable.getColumnModel().getColumn(3).setResizable(false);
       JTableHeader header = countTable.getTableHeader();
       Border border = BorderFactory.createLineBorder(new Color(0x8b8989));  // Цвет рамки заголовка
       header.setBorder(border);
       header.setBackground(new Color(0x8b8989));

       //countTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
       countTable.setVisible(true);
       countTable.setDefaultRenderer(Object.class, new DateCellRenderer());

       obj = chart.getChartObjectFactory().createChartWidget();
       //obj.setText("Price marker adder");
       obj.setFillColor(new Color(112,146,190));
       obj.setColor(FILL_COLOR);
       obj.setFillOpacity(0.5f);
       if(INFORMER_SIZE != null) obj.setPreferredSize(new Dimension(370,225));
       obj.setChartObjectListener(new ChartObjectListener() {
            @Override
            public void deleted(ChartObjectEvent e) {
                isAlive = false;
            }
            @Override public void selected(ChartObjectEvent e) {}
            @Override public void moved(ChartObjectEvent e) {}
            @Override public void highlightingRemoved(ChartObjectEvent e) {}
            @Override public void highlighted(ChartObjectEvent e) {}
            @Override public void deselected(ChartObjectEvent e) {}
            @Override public void attrChanged(ChartObjectEvent e) {}
       });
       // show Informer

       //panel.setPreferredSize(new Dimension(150,150));
       //panel.setMinimumSize(new Dimension(250, 150));
       //panel.setMaximumSize(new Dimension(350, 400));
       String title = selected ? "Scaner_v1.0 for "+showInstrument+" & "+period : "Scaner_v1.0 for "+counterPair+" currency pairs, & 1H-15M-5M periods";
       final JLabel label = new JLabel(title);
       lTime.setPreferredSize(new Dimension(60,20));
       lTime.setForeground(Color.white);
       lTime.setHorizontalAlignment(JTextField.LEFT);
       //label.setBackground(new Color(112,146,190));
       label.setForeground(Color.orange);
       label.setHorizontalAlignment(JLabel.CENTER);
       label.setPreferredSize(new Dimension(360,20));
       JScrollPane scrollPane = new JScrollPane(countTable);
       //scrollPane.setPreferredSize(new Dimension(300,150));

       /******************************
       * Informer for trading of value
       * Кнопки, поля
       * SpinnerNumberModel(value, min, max, step)
       ******************************/
       //
       JPanel jPanelOptions = new JPanel();
       jPanelOptions.setLayout(new GridLayout(1,5));
       jrbMarket.setSelected(true);
       jrbMarket.setText("<html><span style='font-size:10pt;color:black;'>Market</span></html>");
       jrbStopLimit.setText("<html><span style='font-size:10pt;color:black;'>Stop Limit</span></html>");
       jrbStopBYask.setText("<html><span style='font-size:10pt;color:black;'>StopBYask</span></html>");
       jrbStopBYbid.setText("<html><span style='font-size:10pt;color:black;'>StopBYbid</span></html>");

       
       //
       JPanel bottom = new JPanel();
       bottom.setLayout(new GridLayout(3,4));
       bottom.setBackground(Color.LIGHT_GRAY);
       //bottom.setPreferredSize(new Dimension(300,60));
       // Price
       SpinnerModel prices = new SpinnerNumberModel(new Double(bid), new Double(0), new Double(1000), new Double(showInstrument.getPipValue()/10));
       jtPrice = new JSpinner(prices);
       String mask = "";
       if (showInstrument.getPipScale() == 2) mask = "000.000"; else mask = "0.00000";
       JSpinner.DefaultEditor editPrice = new JSpinner.NumberEditor(jtPrice, mask);
       editPrice.getTextField().setHorizontalAlignment(JTextField.CENTER);
       editPrice.getTextField().setColumns(10);
       jtPrice.setEditor(editPrice);
       ((JSpinner.DefaultEditor)jtPrice.getEditor()).getTextField().addKeyListener(new KeyListener(){
            @Override
            public void keyPressed(KeyEvent e) {
            }
            @Override
            public void keyReleased(KeyEvent e) {
               //System.out.println("PRESSED!");
            }
            @Override
            public void keyTyped(KeyEvent e) {
                //JOptionPane.showMessageDialog(null, "keyTyped !", "Strategy Alert",JOptionPane.WARNING_MESSAGE);
               char c = e.getKeyChar();
               if (!((c >= '0') && (c <= '9') || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE) || c == '.')) {
                    e.consume();
               }
               if((c == '.') && (jtPrice.getValue().toString() == ".")){
                    e.consume();
               }
            }
       });
       // Amount
       //tAmount = new JTextField(Double.toString(Amount));
       SpinnerModel amount = new SpinnerNumberModel(Amount, 0, 10, 0.01);
       tAmount = new JSpinner(amount);
       JSpinner.DefaultEditor editAmount = new JSpinner.NumberEditor(tAmount, "#0.0#");
       editAmount.getTextField().setHorizontalAlignment(JTextField.CENTER);
       editAmount.getTextField().setColumns(4);
       tAmount.setEditor(editAmount);
       ((JSpinner.DefaultEditor)tAmount.getEditor()).getTextField().addKeyListener(new KeyListener(){
            @Override
            public void keyPressed(KeyEvent e) {
            }
            @Override
            public void keyReleased(KeyEvent e) {
               //System.out.println("PRESSED!");
            }
            @Override
            public void keyTyped(KeyEvent e) {
                //JOptionPane.showMessageDialog(null, "keyTyped !", "Strategy Alert",JOptionPane.WARNING_MESSAGE);
               char c = e.getKeyChar();
               if (!((c >= '0') && (c <= '9') || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE) || c == '.')) {
                    e.consume();
               }
               if((c == '.') && (tAmount.getValue().toString() == ".")){
                    e.consume();
               }
            }
       });
       // Slip
       //tSlip = new JTextField(String.format("%1.3s", (int)Slippage));
       SpinnerModel slip = new SpinnerNumberModel(Slippage, 0, 100, 1);
       tSlip = new JSpinner(slip);
       JSpinner.DefaultEditor editSlip = new JSpinner.NumberEditor(tSlip, "#0");
       editSlip.getTextField().setHorizontalAlignment(JTextField.CENTER);
       editSlip.getTextField().setColumns(2);
       tSlip.setEditor(editSlip);
       ((JSpinner.DefaultEditor)tSlip.getEditor()).getTextField().addKeyListener(new KeyListener(){
            @Override
            public void keyPressed(KeyEvent e) {
            }
            @Override
            public void keyReleased(KeyEvent e) {
               //System.out.println("PRESSED!");
            }
            @Override
            public void keyTyped(KeyEvent e) {
                //JOptionPane.showMessageDialog(null, "keyTyped !", "Strategy Alert",JOptionPane.WARNING_MESSAGE);
               char c = e.getKeyChar();
               if (!((c >= '0') && (c <= '9') || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE) || c == '.')) {
                    e.consume();
               }
               if((c == '.') && (tSlip.getValue().toString() == ".")){
                    e.consume();
               }
            }
       });
       // SL
       //tSL = new JTextField(String.format("%1.3s", (int)StopLoss));
       SpinnerModel sl = new SpinnerNumberModel(StopLoss, 0, 1000, 1);
       tSL = new JSpinner(sl);
       JSpinner.DefaultEditor editSL = new JSpinner.NumberEditor(tSL, "###0");
       editSL.getTextField().setHorizontalAlignment(JTextField.CENTER);
       editSL.getTextField().setColumns(4);
       tSL.setEditor(editSL);
       ((JSpinner.DefaultEditor)tSL.getEditor()).getTextField().addKeyListener(new KeyListener(){
            @Override
            public void keyPressed(KeyEvent e) {
            }
            @Override
            public void keyReleased(KeyEvent e) {
               //System.out.println("PRESSED!");
            }
            @Override
            public void keyTyped(KeyEvent e) {
                //JOptionPane.showMessageDialog(null, "keyTyped !", "Strategy Alert",JOptionPane.WARNING_MESSAGE);
               char c = e.getKeyChar();
               if (!((c >= '0') && (c <= '9') || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE) || c == '.')) {
                    e.consume();
               }
               if((c == '.') && (tSL.getValue().toString() == ".")){
                    e.consume();
               }
            }
       });
       // TP
       //tTP = new JTextField(String.format("%1$1.3s", (int)TakeProfit));
       SpinnerModel tp = new SpinnerNumberModel(TakeProfit, 0, 1000, 1);
       tTP = new JSpinner(tp);
       JSpinner.DefaultEditor editTP = new JSpinner.NumberEditor(tTP, "###0");
       editTP.getTextField().setHorizontalAlignment(JTextField.CENTER);
       editTP.getTextField().setColumns(4);
       tTP.setEditor(editTP);
       ((JSpinner.DefaultEditor)tTP.getEditor()).getTextField().addKeyListener(new KeyListener(){
            @Override
            public void keyPressed(KeyEvent e) {
            }
            @Override
            public void keyReleased(KeyEvent e) {
               //System.out.println("PRESSED!");
            }
            @Override
            public void keyTyped(KeyEvent e) {
                //JOptionPane.showMessageDialog(null, "keyTyped !", "Strategy Alert",JOptionPane.WARNING_MESSAGE);
               char c = e.getKeyChar();
               if (!((c >= '0') && (c <= '9') || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE) || c == '.')) {
                    e.consume();
               }
               if((c == '.') && (tTP.getValue().toString() == ".")){
                    e.consume();
               }
            }
       });
       // Вывод переключателей
       jPanelOptions.add(jrbMarket);
       jPanelOptions.add(jrbStopLimit);
       jPanelOptions.add(jtPrice);
       jPanelOptions.add(jrbStopBYask);
       jPanelOptions.add(jrbStopBYbid);
       jrbGroupOptions.add(jrbMarket);
       jrbGroupOptions.add(jrbStopLimit);
       jrbGroupOptions.add(jrbStopBYask);
       jrbGroupOptions.add(jrbStopBYbid);
       //
       JLabel lAmount = new JLabel("Lot ",JLabel.RIGHT);
       JLabel lSlip = new JLabel(" Slippage",JLabel.LEFT);
       JLabel lSL = new JLabel("Stop Loss ",JLabel.RIGHT);
       JLabel lTP = new JLabel(" Take Profit",JLabel.LEFT);
       // Выравнивание текста
       //JScrollBar scrollBarAmount = new JScrollBar(JScrollBar.VERTICAL);
       //tAmount.setHorizontalAlignment(JTextField.CENTER);
       //tSlip.setHorizontalAlignment(JTextField.CENTER);
       //tSL.setHorizontalAlignment(JTextField.CENTER);
       //tTP.setHorizontalAlignment(JTextField.CENTER);
       //
       lCountSell.setSize(60, 30);
       lCountSell.setToolTipText("");
       lCountSell.setText("<html><span style='font-size:11pt;color:white;'><b>0</b></span></html>");

       lPipsSell.setSize(100, 30);
       lPipsSell.setToolTipText("");
       lPipsSell.setText("<html><span style='font-size:11pt;color:white;'>pips: <b>0.0</b></span></html>");
       lPipsBuy.setSize(100, 30);
       lPipsBuy.setToolTipText("");
       lPipsBuy.setText("<html><span style='font-size:11pt;color:white;'>pips: <b>0.0</b></span></html>");

       lCountBuy.setSize(60, 30);
       lCountBuy.setToolTipText("");
       lCountBuy.setText("<html><span style='font-size:11pt;color:white;'><b>0</b></span></html>");
       //
       final JButton btnBuy = new JButton();
       final JButton btnSell = new JButton();
       final JButton closeSell = new JButton();
       final JButton closeBuy = new JButton();
       //
       closeSell.setText("<html><div style='border: 1px red solid;background-color:#ffdcdc;text-align:center;font-size:10pt;margin-left:-15;height:10;width:74;padding:0px;'>Close Sell</div></html>");
       closeSell.setActionCommand("closeSELL");
       //closeAll.setText("<html><div style='border: 1px blue solid;background-color:#dcffff;text-align:center;font-size:12pt;margin-left:-15;height:25;width:90;padding:3px;'>Close All</div></html>");
       closeBuy.setText("<html><div style='border: 1px green solid;background-color:#e6ffe6;text-align:center;font-size:10pt;margin-left:-15;height:10;width:74;padding:0px;'>Close Buy</div></html>");
       closeBuy.setActionCommand("closeBUY");
       //closeSell.setText("Close Sell");
       //closeBuy.setText("Close Buy");
       bottom.add(lAmount); bottom.add(tAmount);
       bottom.add(tSlip); bottom.add(lSlip);
       bottom.add(lSL); bottom.add(tSL);
       bottom.add(tTP); bottom.add(lTP);

       btnSell.setVisible(true);
       btnSell.setText("<html><div style='border: 1px red solid;background-color:#ffdcdc;text-align:center;font-size:10pt;margin-left:-15;height:10;width:74;padding:0px;'>Sell</div></html>");
       btnSell.setActionCommand("SELL");
       btnBuy.setVisible(true);
       btnBuy.setText("<html><div style='border: 1px green solid;background-color:#e6ffe6;text-align:center;font-size:10pt;margin-left:-15;height:10;width:74;padding:0px;'>Buy</div></html>");
       btnBuy.setActionCommand("BUY");

       bottom.add(btnSell);
       bottom.add(closeSell);
       bottom.add(closeBuy);
       bottom.add(btnBuy);
       
       //
       JPanel panel = obj.getContentPanel();
       panel.setLayout(null);
       panel.setPreferredSize(new Dimension(360,200));

       lTime.setSize(60, 20);
       lTime.setLocation(10,0);

       label.setSize(360, 20);
       label.setLocation(5,0);

       scrollPane.setSize(360, 115);
       scrollPane.setLocation(5,20);

       jPanelOptions.setSize(360, 15);    // добавть ещё одну панель
       jPanelOptions.setLocation(5,137);

       //jPanelLabel.setSize(360, 15);   // Price
      // jPanelLabel.setLocation(150,137);

       bottom.setSize(360, 50);    // добавть ещё одну панель
       bottom.setLocation(5,155);  //

       panel.add(lTime);
       panel.add(label);
       panel.add(scrollPane);
       panel.add(jPanelOptions);
       //panel.add(lPrice);
       panel.add(bottom);
       panel.add(lCountSell);
       panel.add(lPipsSell);
       panel.add(lPipsBuy);
       panel.add(lCountBuy);
        // Counts
       lCountSell.setLocation(45,200);
       lPipsSell.setLocation(110,200);
       lPipsBuy.setLocation(200,200);
       lCountBuy.setLocation(315,200);
       //
       chart.add(obj);
       // Прослушки для кнопок и галочек
       btnBuy.addActionListener(new ButtonListener());
       btnSell.addActionListener(new ButtonListener());
       closeSell.addActionListener(new ButtonListener());
       closeBuy.addActionListener(new ButtonListener());

       //add allow listener
       jrbMarket.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
             try {
                  chart.remove("Stop up");
                  chart.remove("Stop down");
               if (selected && manual) {
                   IBar prevBar = history.getBar(showInstrument, period, side.BID, 1);
                   setData(direction, showInstrument, prevBar);
               }
             } catch (Exception e1) {
               context.getConsole().getOut().println(e1.getCause());
             }
           }
       });
       jrbStopLimit.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
             try {
               if (selected && manual) {
                   IBar prevBar = history.getBar(showInstrument, period, side.BID, 1);
                   setData(direction, showInstrument, prevBar);
               }
             } catch (Exception e1) {
               context.getConsole().getOut().println(e1.getCause());
             }
           }
       });
       jrbStopBYask.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
             try {
                  setDataLine();
               if (selected && manual) {
                   IBar prevBar = history.getBar(showInstrument, period, side.BID, 1);
                   setData(direction, showInstrument, prevBar);
               }
             } catch (Exception e1) {
               context.getConsole().getOut().println(e1.getCause());
             }
           }
       });
       jrbStopBYbid.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
             try {
                  setDataLine();
               if (selected && manual) {
                   IBar prevBar = history.getBar(showInstrument, period, side.BID, 1);
                   setData(direction, showInstrument, prevBar);
               }
             } catch (Exception e1) {
               context.getConsole().getOut().println(e1.getCause());
             }
           }
       });
       // Price
       jtPrice.addChangeListener(new ChangeListener() {
           @Override
           public void stateChanged(ChangeEvent event) {
             try {
                 double lastBar = history.getLastTick(showInstrument).getBid();
                 if (jrbStopBYask.isSelected()) {
                     pipValueUP = NormalizeDouble(Double.parseDouble(jtPrice.getValue().toString())+(showInstrument.getPipValue()*2),5);
                     //print(NormalizeDouble(lastBar,5)+" - "+NormalizeDouble(pipValueUP,5));
                     if (NormalizeDouble(lastBar+(showInstrument.getPipValue()*2),5) <= NormalizeDouble(pipValueUP,5)){
                         chart.remove("Stop up");
                         upPriceLine = chart.getChartObjectFactory().createHorizontalLine("Stop up", pipValueUP);
                         upPriceLine.setVisibleInWorkspaceTree(false);
                         upPriceLine.setLineStyle(LineStyle.DOT);
                         upPriceLine.setColor(new Color(0x228b22));
                         chart.add(upPriceLine);
                     } else {
                         pipValueUP = NormalizeDouble(lastBar,5) + (showInstrument.getPipValue()*2);
                     }
                  } else if (jrbStopBYbid.isSelected()) {
                     pipValueDN = NormalizeDouble(Double.parseDouble(jtPrice.getValue().toString())-(showInstrument.getPipValue()*2),5); 
                     if (NormalizeDouble(lastBar-(showInstrument.getPipValue()*2),5) >= NormalizeDouble(pipValueDN,5)){ 
                         chart.remove("Stop down");
                         dnPriceLine = chart.getChartObjectFactory().createHorizontalLine("Stop down", pipValueDN);
                         dnPriceLine.setVisibleInWorkspaceTree(false);
                         dnPriceLine.setLineStyle(LineStyle.DOT);
                         dnPriceLine.setColor(Color.RED);
                         chart.add(dnPriceLine);
                     } else {
                         pipValueDN = NormalizeDouble(lastBar,5)-(showInstrument.getPipValue()*2);  
                     }
                  } else {
                     chart.remove("Stop up");  
                     chart.remove("Stop down");
                  }
                 } catch (Exception e1) {
               context.getConsole().getOut().println(e1.getCause());
             }
           }
       });
       
    } // End getInformer
    //
    private void setDataLine() throws JFException {
           double lastBar = history.getLastTick(showInstrument).getBid();
           if (jrbStopBYask.isSelected()) {
               pipValueUP = NormalizeDouble(Double.parseDouble(jtPrice.getValue().toString())+(showInstrument.getPipValue()*2),5);
               //print(NormalizeDouble(lastBar,5)+" - "+NormalizeDouble(pipValueUP,5));
               if (NormalizeDouble(lastBar+(showInstrument.getPipValue()*2),5) <= NormalizeDouble(pipValueUP,5)){
                   upPriceLine = chart.getChartObjectFactory().createHorizontalLine("Stop up", pipValueUP);
                   upPriceLine.setVisibleInWorkspaceTree(false);
                   upPriceLine.setLineStyle(LineStyle.DOT);
                   upPriceLine.setColor(new Color(0x228b22));
                   chart.add(upPriceLine);
               } else {
                 pipValueUP = NormalizeDouble(lastBar,5) + (showInstrument.getPipValue()*2);  
               }
           } else {
               this.chart.remove("Stop up");
           }
           if (jrbStopBYbid.isSelected()) {
               pipValueDN = NormalizeDouble(Double.parseDouble(jtPrice.getValue().toString())-(showInstrument.getPipValue()*2),5);
               if (NormalizeDouble(lastBar-(showInstrument.getPipValue()*2),5) >= NormalizeDouble(pipValueDN,5)){
                   dnPriceLine = chart.getChartObjectFactory().createHorizontalLine("Stop down", pipValueDN);
                   dnPriceLine.setVisibleInWorkspaceTree(false);
                   dnPriceLine.setLineStyle(LineStyle.DOT);
                   dnPriceLine.setColor(Color.RED);
                   chart.add(dnPriceLine); 
               } else {
                   pipValueDN = NormalizeDouble(lastBar,5) - (showInstrument.getPipValue()*2);   
               }
           } else {
               this.chart.remove("Stop down");
           }
      
    } // End setDataLine()


    private boolean validateFields() throws JFException {
       try {
            Price = Double.parseDouble(jtPrice.getValue().toString());
            Amount = Double.parseDouble(tAmount.getValue().toString());
            Slippage = Double.parseDouble(tSlip.getValue().toString());
            StopLoss = Double.parseDouble(tSL.getValue().toString());
            TakeProfit = Double.parseDouble(tTP.getValue().toString());
            //Price = Double.parseDouble(editPrice.getTextField().getText());
            //Amount = Double.parseDouble(editAmount.getTextField().getText());
            //Slippage = Double.parseDouble(editSlip.getTextField().getText());
            //StopLoss = Double.parseDouble(editSL.getTextField().getText());
            //TakeProfit = Double.parseDouble(editTP.getTextField().getText());
       } catch (Exception e1) {
                //e1.printStackTrace();
                context.getConsole().getOut().println(e1.getCause());
       }
       return true;
    } // end validateprices

    //
    public class ButtonListener implements ActionListener { ButtonListener() {}

        public void actionPerformed(ActionEvent e) {
          if (e.getActionCommand().equals("SELL")) {
              setOrderSell();
              //print(e.getActionCommand());
          } else if (e.getActionCommand().equals("BUY")) {
              setOrderBuy();
              //print(e.getActionCommand());
          } else if (e.getActionCommand().equals("closeSELL")) {
             try {
                         for(IOrder o: engine.getOrders()){
                             if (o.getOrderCommand() == OrderCommand.SELL || o.getOrderCommand() == OrderCommand.SELLLIMIT 
                                 || o.getOrderCommand() == OrderCommand.SELLSTOP || o.getOrderCommand() == OrderCommand.SELLSTOP_BYASK) {
                                 o.close(); countSell = 0; pipSell = 0;
                             }
                         }
                         
                         //context.getConsole().getOut().println("Close Sell orders");
             } catch (JFException ex) {
                      context.getConsole().getOut().println(ex);
             }

             //print(e.getActionCommand());
          } else if (e.getActionCommand().equals("closeBUY")) {
             try {
                         for(IOrder o: engine.getOrders()){
                             if (o.getOrderCommand() == OrderCommand.BUY || o.getOrderCommand() == OrderCommand.BUYLIMIT 
                                 || o.getOrderCommand() == OrderCommand.BUYSTOP || o.getOrderCommand() == OrderCommand.BUYSTOP_BYBID) {
                                 o.close(); countBuy = 0; pipBuy = 0;
                             }
                         }
                         //context.getConsole().getOut().println("Close Sell orders");
             } catch (JFException ex) {
                      context.getConsole().getOut().println(ex);
             }

             //print(e.getActionCommand());
          }

        }
    } // End class ButtonListener
     private void placeOrder(String orderType) throws JFException {
            //OrderCommand orderCmd = null;
            if ("BUY".equals(orderType)){
                if (jrbMarket.isSelected() == true){
                    orderCmd = OrderCommand.BUY;
                    orderKey = "BUY_"+(countBuy++);
                }
                if (jrbStopBYbid.isSelected() == true){
                    orderCmd = OrderCommand.BUYSTOP_BYBID;
                    orderKey = "BUYSTOP_BYBID_"+(countBuy++);
                }
                if (jrbStopBYask.isSelected() == true){
                    orderCmd = OrderCommand.BUYSTOP;
                    orderKey = "BUYSTOP_"+(countBuy++);
                }
                if (jrbStopLimit.isSelected() == true){
                    orderCmd = OrderCommand.BUYLIMIT;
                    orderKey = "BUYLIMIT_"+(countBuy++);
                }
            }else if ("SELL".equals(orderType)){
                if (jrbMarket.isSelected() == true){
                    orderCmd = OrderCommand.SELL;
                    orderKey = "SELL_"+(countSell++);
                }
                if (jrbStopBYbid.isSelected() == true){
                    orderCmd = OrderCommand.SELLSTOP;
                    orderKey = "SELLSTOP_"+(countSell++);
                }
                if (jrbStopBYask.isSelected() == true){
                    orderCmd = OrderCommand.SELLSTOP_BYASK;
                    orderKey = "SELLSTOP_BYASK_"+(countSell++);
                }
                if (jrbStopLimit.isSelected() == true){
                    orderCmd = OrderCommand.SELLLIMIT;
                    orderKey = "SELLLIMIT_"+(countSell++);
                }
            }

        }// end placeOrder
    private void setOrderBuy() {
      try {
             //boolean fieldsOk = validateFields(); if (!fieldsOk){return;}
             //
             if (TrailingStop) {
                 placeOrder("BUY");
                 order = engine.submitOrder(orderKey, showInstrument, OrderCommand.BUY, Amount);
                 Thread.sleep(2000);
                 pipValue = showInstrument.getPipValue();
                 double bid = history.getLastTick(showInstrument).getBid();
                 double ask = history.getLastTick(showInstrument).getAsk();
                 StopLossPrice = orderCmd.isLong()
                  ? bid - StopLoss * pipValue
                  : ask + StopLoss * pipValue;
                 if (orderCmd.isLong()) {
                     order.setStopLossPrice(StopLossPrice, side.BID, trailingStep); //longOrders.put(order.getLabel(), new MyOrder(order));
                 } else {
                     order.setStopLossPrice(StopLossPrice, side.ASK, trailingStep); //shortOrders.put(order.getLabel(), new MyOrder(order));
                 }
             } else {
                 placeOrder("BUY");
                 if (jrbMarket.isSelected()){
                      if (selected && manual){
                        double Amount = Double.parseDouble(tAmount.getValue().toString());
                        double Slippage = Double.parseDouble(tSlip.getValue().toString());
                        double StopLoss = Double.parseDouble(tSL.getValue().toString());
                        double TakeProfit = Double.parseDouble(tTP.getValue().toString());
                        pipValue = showInstrument.getPipValue();
                        double bid = history.getLastTick(showInstrument).getBid();
                        double ask = history.getLastTick(showInstrument).getAsk();
                        StopLossPrice = orderCmd.isLong()
                         ? bid - StopLoss * pipValue
                         : ask + StopLoss * pipValue;
                        TakeProfitPrice = orderCmd.isLong()
                         ? bid + TakeProfit * pipValue
                         : ask - TakeProfit * pipValue;
                        Price = 0;
                        order = engine.submitOrder(orderKey, showInstrument, orderCmd, Amount, Price, Slippage, StopLossPrice, TakeProfitPrice);
                    } else {
                        pipValue = showInstrument.getPipValue();
                        double bid = history.getLastTick(showInstrument).getBid();
                        double ask = history.getLastTick(showInstrument).getAsk();
                        StopLossPrice = orderCmd.isLong()
                         ? bid - StopLoss * pipValue
                         : ask + StopLoss * pipValue;
                        TakeProfitPrice = orderCmd.isLong()
                         ? bid + TakeProfit * pipValue
                         : ask - TakeProfit * pipValue;
                        Price = 0;
                        order = engine.submitOrder(orderKey, showInstrument, orderCmd, Amount, Price, Slippage, StopLossPrice, TakeProfitPrice);
                    }
                } else {
                    if (selected && manual){
                        double Price = Double.parseDouble(jtPrice.getValue().toString());
                        double Amount = Double.parseDouble(tAmount.getValue().toString());
                        double Slippage = Double.parseDouble(tSlip.getValue().toString());
                        double StopLoss = Double.parseDouble(tSL.getValue().toString());
                        double TakeProfit = Double.parseDouble(tTP.getValue().toString());
                        StopLossPrice = orderCmd.isLong()
                         ? Price - StopLoss * pipValue
                         : Price + StopLoss * pipValue;
                        TakeProfitPrice = orderCmd.isLong()
                         ? Price + TakeProfit * pipValue
                         : Price - TakeProfit * pipValue;
                        order = engine.submitOrder(orderKey, showInstrument, orderCmd, Amount, Price, Slippage, StopLossPrice, TakeProfitPrice);
                    } else {
                        Price = Double.parseDouble(jtPrice.getValue().toString());
                        StopLossPrice = orderCmd.isLong()
                         ? Price - StopLoss * pipValue
                         : Price + StopLoss * pipValue;
                        TakeProfitPrice = orderCmd.isLong()
                         ? Price + TakeProfit * pipValue
                         : Price - TakeProfit * pipValue;
                        order = engine.submitOrder(orderKey, showInstrument, orderCmd, Amount, Price, Slippage, StopLossPrice, TakeProfitPrice);
                    }
                    //order = engine.submitOrder(orderKey, showInstrument, orderCmd, Amount, Price, Slippage, StopLossPrice, TakeProfitPrice);
                }
                //order.waitForUpdate(2000, IOrder.State.FILLED);
             }
             } catch (Exception e1) {
                   //context.getConsole().getOut().println(e1);
                   context.getConsole().getOut().println(e1.getCause());
                   //context.getConsole().getOut().println(e1);
             }  
    } // End setOrderBuy
    private void setOrderSell() {
      try {
             //boolean fieldsOk = validateFields(); if (!fieldsOk){return;}
             //
             if (TrailingStop) {
                 placeOrder("SELL");
                 order = engine.submitOrder(orderKey, showInstrument, OrderCommand.SELL, Amount);
                 Thread.sleep(2000);
                 pipValue = showInstrument.getPipValue();
                 double bid = history.getLastTick(showInstrument).getBid();
                 double ask = history.getLastTick(showInstrument).getAsk();
                 StopLossPrice = orderCmd.isLong()
                  ? bid - StopLoss * pipValue
                  : ask + StopLoss * pipValue;
                 if (orderCmd.isLong()) {
                    order.setStopLossPrice(StopLossPrice, side.BID, trailingStep); //longOrders.put(order.getLabel(), new MyOrder(order));
                 } else {
                    order.setStopLossPrice(StopLossPrice, side.ASK, trailingStep); //shortOrders.put(order.getLabel(), new MyOrder(order));
                 }
             } else {
                 placeOrder("SELL");
                 if (jrbMarket.isSelected()){
                    if (selected && manual){
                        double Amount = Double.parseDouble(tAmount.getValue().toString());
                        double Slippage = Double.parseDouble(tSlip.getValue().toString());
                        double StopLoss = Double.parseDouble(tSL.getValue().toString());
                        double TakeProfit = Double.parseDouble(tTP.getValue().toString());
                        pipValue = showInstrument.getPipValue();
                        double bid = history.getLastTick(showInstrument).getBid();
                        double ask = history.getLastTick(showInstrument).getAsk();
                        StopLossPrice = orderCmd.isLong()
                         ? bid - StopLoss * pipValue
                         : ask + StopLoss * pipValue;
                        TakeProfitPrice = orderCmd.isLong()
                         ? bid + TakeProfit * pipValue
                         : ask - TakeProfit * pipValue;
                        Price = 0;
                        order = engine.submitOrder(orderKey, showInstrument, orderCmd, Amount, Price, Slippage, StopLossPrice, TakeProfitPrice);
                    } else {
                        pipValue = showInstrument.getPipValue();
                        double bid = history.getLastTick(showInstrument).getBid();
                        double ask = history.getLastTick(showInstrument).getAsk();
                        StopLossPrice = orderCmd.isLong()
                         ? bid - StopLoss * pipValue
                         : ask + StopLoss * pipValue;
                        TakeProfitPrice = orderCmd.isLong()
                         ? bid + TakeProfit * pipValue
                         : ask - TakeProfit * pipValue;
                        Price = 0;
                        order = engine.submitOrder(orderKey, showInstrument, orderCmd, Amount, Price, Slippage, StopLossPrice, TakeProfitPrice);
                    }
                 } else {
                    if (selected && manual){
                        double Price = Double.parseDouble(jtPrice.getValue().toString());
                        double Amount = Double.parseDouble(tAmount.getValue().toString());
                        double Slippage = Double.parseDouble(tSlip.getValue().toString());
                        double StopLoss = Double.parseDouble(tSL.getValue().toString());
                        double TakeProfit = Double.parseDouble(tTP.getValue().toString());
                        StopLossPrice = orderCmd.isLong()
                         ? Price - StopLoss * pipValue
                         : Price + StopLoss * pipValue;
                        TakeProfitPrice = orderCmd.isLong()
                         ? Price + TakeProfit * pipValue
                         : Price - TakeProfit * pipValue;
                        order = engine.submitOrder(orderKey, showInstrument, orderCmd, Amount, Price, Slippage, StopLossPrice, TakeProfitPrice);
                    } else {
                        Price = Double.parseDouble(jtPrice.getValue().toString());
                        StopLossPrice = orderCmd.isLong()
                         ? Price - StopLoss * pipValue
                         : Price + StopLoss * pipValue;
                        TakeProfitPrice = orderCmd.isLong()
                         ? Price + TakeProfit * pipValue
                         : Price - TakeProfit * pipValue;
                        order = engine.submitOrder(orderKey, showInstrument, orderCmd, Amount, Price, Slippage, StopLossPrice, TakeProfitPrice);
                    }
                    //order = engine.submitOrder(orderKey, showInstrument, orderCmd, Amount, Price, Slippage, StopLossPrice, TakeProfitPrice);
                 }
                 //order.waitForUpdate(2000, IOrder.State.FILLED);
             }
             } catch (Exception e1) {
                   //e1.printStackTrace();
                   context.getConsole().getOut().println(e1.getCause());
                   //context.getConsole().getOut().println(e1);
             }  
    } // End setOrderSell
    /*******
    * Print
    *******/
    private void print(Object o) {
        console.getOut().println(o);
    }
    private void printErr(Object o) {
        console.getErr().println(o);
    }
    // Модель данных таблицы
    class TradeTableModel extends AbstractTableModel {
    private String[][] data = new String[0][0];

    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    public void setData(String[][] data) {
        this.data = data;
        fireTableDataChanged();
    }

    public int getRowCount() {
        return data.length;
    }

    public int getColumnCount() {
        return 4;
    }
    public void setValueAt(String value, int row, int col) {
          data[row][col] = value;
          fireTableCellUpdated(row, col);
    }
    public Object getValueAt(int row, int column) {
        if (data[row][column] == "0") {
            return "";
        } else {
            return data[row][column];
        }
    }

    public String getColumnName(int column) {

        switch (column) {
            case 0:
                return "FX Pairs - Period";
            case 1:
                return "   Signal-Direction";
            case 2:
                return " Main trend";
            case 3:
                return " Strength ";
            default:
                return "";
        }

    }
    }
    // Окрашивание окон
    public class DateCellRenderer extends JLabel implements TableCellRenderer {

    public DateCellRenderer() {
      setOpaque(true);
    }
    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
        //@param: table - глобально применяет окрашивание в указанным строкам, ячейкам, столбцам
        //@param: value - применяет окрашивание к указанному значению в ячейке
        //@param: isSelected - это понятно и без объяснений (применяет окрашивание к выделению)
        //@param: hasFocus - применяет окрашивание к выделенной ячейке
        //@param: row - применяет окрашивание в указанной по индексу строке
        //@param: column - применяет окрашивание в указанной по индексу колонке
        JLabel c = new JLabel();
        if(column==0){c.setHorizontalAlignment(JLabel.CENTER); c.setOpaque(true); c.setBackground(new Color(0xffe4e1)); c.setText(value.toString());}
        if(column==1){c.setHorizontalAlignment(JLabel.LEFT); c.setOpaque(true); c.setBackground(new Color(0xfff0f5)); c.setText(value.toString());}
        if(column==2){c.setHorizontalAlignment(JLabel.CENTER); c.setOpaque(true); c.setBackground(new Color(0xe6e6fa)); c.setText(value.toString());}
        if(column==3){c.setHorizontalAlignment(JLabel.CENTER); c.setOpaque(true); c.setBackground(new Color(0xfffacd)); c.setText(value.toString());}
        return c;
    }
    }
}
