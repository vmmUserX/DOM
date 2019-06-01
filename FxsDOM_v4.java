//+-----------------------------------------------------------------+
//                                                       FxDOM.java |
//                                            https://itx-team.com/ |
//                                     © Copyright 2017 vmm@mail.ru |
//                                                                  |
//                                                                  |
//                                                                  | 
//+-----------------------------------------------------------------+
package jforex;

import com.dukascopy.api.*;
import com.dukascopy.api.RequiresFullAccess;
import com.dukascopy.api.util.*;
import com.dukascopy.api.feed.IFeedDescriptor;
import com.dukascopy.api.feed.util.*;
import static com.dukascopy.api.IOrder.State.*;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.chart.mouse.IChartPanelMouseEvent;
import com.dukascopy.api.chart.mouse.IChartPanelMouseListener;

import java.util.*;
import java.text.*;
import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.Color;
import java.awt.geom.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.JTextField.*;
import javax.swing.text.*;
import java.net.*;
import java.io.*;
//import com.dukascopy.api.system.IPreferences.Platform.PreferencesSettings;
//import com.dukascopy.api.system.IPreferences.Chart;
//import com.dukascopy.api.system.IPreferences.Chart.Orders;
//import com.dukascopy.api.system.IPreferences.PreferenceRoot;
//import com.dukascopy.api.system.IPerfStatData;
//import com.dukascopy.api.system.IPreferences.Platform;

//import com.dukascopy.api.instrument.*;

/**
 * Main Class of the strategy
 */
@RequiresFullAccess // Some functions may need this
public class FxsDOM_v4 implements IStrategy {
    // <editor-fold defaultstate="" desc="Main class variables">
    // base objects creation
    private IContext context;
    private IEngine engine = null;
    private IConsole console = null;
    private IHistory history = null;
    private IAccount account = null;
    private IChart chart;
    private IUserInterface userInterface;
    //private IFinancialInstrument fin;
    // configurable variables
    @Configurable("Instrument:") 
    public Instrument instrumentThis = Instrument.EURUSD;
    //@Configurable("Side")
    public OfferSide side = OfferSide.BID;
    //@Configurable("Trailing Stop")
    public boolean TrailingStop = false;
    public int trailingStep = 10;
    //public int fin1 = fin.getDefaultStopLossSlippage();
    //@Configurable("Size height:") 
    //public int setHigh = 1000;
    
    // other variables
    public fxDomForm freeForm = null;
    private double pipValue;
    private int pipScale;
    private double tickAsk = Double.NaN;
    private double tickBid = Double.NaN;
    private double BidVolume = 0;
    private double AskVolume = 0;
    private double BidTotalVolume = 0;
    private double AskTotalVolume = 0;
    private String volAsk = "";
    private String volBid = "";
    private double spread = 0;
    private String dBid = "000.000";
    private String dAsk = "000.000";
    private double MAX_SUM = Double.NaN;
       
    // </editor-fold>
    IChartObject maxPriceLine;
    IChartObject maxNumber;
    IChartPanelMouseListener listener;
    //
    JButton bLic;
    // 
    public double MAX_TOP_ADD = 0;
    public double MAX_LEV_TOP = 0;
    // Advanced Options
    JCheckBox tickBox = new JCheckBox("Always on Top",true);
    JCheckBox domVisible = new JCheckBox("DOM on center",true); public boolean windowVisible = true;
    JCheckBox bestPriceVisible = new JCheckBox("Only best price",false);  public boolean onlyBestPrice = false;
    
    JCheckBox topPanelVisible = new JCheckBox("Inside market ",true); public boolean topVisible = true;
    JCheckBox moveLineTPSL = new JCheckBox("Move TP&SL",false); public boolean moveLines = false;
    JCheckBox lifeLineVisible = new JCheckBox("Show 'life-line'",false); public boolean lineVisible = false;
    
    
    final JButton closeSell = new JButton();
    final JButton closeAll = new JButton();
    final JButton closeBuy = new JButton();
    JLabel ispred = new JLabel("",JLabel.CENTER);
    // Account
    private boolean GlobalAccount;
    private int MarginCutLevel;
    private int OverWeekendEndLeverage;
    private double UseofLeverage;
    private double Equity;
    private String AccountId = "";
    private String AccountCurrency = "";
    private double Leverage;
  
    //order variables
    private double Entry = Double.NaN;
    private double Amount = 0.01;
    private double StopLoss = 20;
    private double TakeProfit = 80;
    private double StopLossPrice = 0.0;
    private double TakeProfitPrice = 0.0;
    private double Slippage = 0;
    private IOrder order = null;
    private int countSell = 0;
    private int countBuy = 0;
    private int counter = 0; 
    private String orderType;
    private String orderKey;
    //
    private IChartObject StopLossLine;
    private IChartObject TakeProfitLine;    
    //private int slip = com.dukascopy.api.instrument.IFinancialInstrument.getDefaultMarketSlippage();
    
    /**
     * onStart Function
     * 
     * @param context
     * @throws JFException 
     */
    @Override
    public void onStart(IContext context) throws JFException {
       this.context = context;
       this.engine = context.getEngine();
       this.console = this.context.getConsole();
       this.history = this.context.getHistory();
       this.account = this.context.getAccount();
       //IChartObject breakEvenLine;
       //IFeedDescriptor feedDescriptor = new TimePeriodAggregationFeedDescriptor(instrumentThis, Period.TICK, OfferSide.BID);
       //chart = context.openChart(feedDescriptor);
       /** 
       / Set variable
       / pipValue - 10 000 для 5-значных EURUSD, 100 -для 3-значных USDJPY
       / pipScale - 4 для 5-значных EURUSD, 2 - для 3-значных USDJPY
       */
       pipValue = instrumentThis.getPipValue();
       pipScale = instrumentThis.getPipScale();
     
       //freeForm = new fxDomForm();

       if (this.engine.getType() == IEngine.Type.TEST){
           freeForm = new fxDomForm();
           freeForm.setVisible(true);
           //    JOptionPane.showMessageDialog(null, "This strategy do not run on Historical Tester !", "Strategy Alert",JOptionPane.WARNING_MESSAGE);
           //    this.context.stop();
       }else{
           //Set subscribedInstruments = new HashSet();
           //subscribedInstruments.add(instrumentThis);
           //this.context.setSubscribedInstruments(subscribedInstruments);
           freeForm = new fxDomForm();
           ITick tick = history.getLastTick(instrumentThis);
           freeForm.setVisible(true);
           onTick(instrumentThis, tick);

       }
       try {
             chart = context.getChart(instrumentThis);
             if(chart == null){
                console.getErr().println("No chart opened for "+instrumentThis+"!");
                context.stop();
             }
           } catch (Exception e) {
                   //context.getConsole().getOut().println(e);
                   context.getConsole().getOut().println(e.getCause());
           }  
       
       //TraceMouse();
       
    }
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
       
    }
    public void onAccount(IAccount account) throws JFException {
        AccountCurrency = account.getCurrency().toString();        // Счёт в валюте   
        Leverage = account.getLeverage();                          // Плечо = 100
        AccountId= account.getAccountId();                         // Номер счёта
        Equity = account.getEquity();                              // Депозит
        UseofLeverage = account.getUseOfLeverage();                // Задействованная маржа
        OverWeekendEndLeverage = account.getOverWeekEndLeverage(); // Плечо в выходные дни = 30
        MarginCutLevel = account.getMarginCutLevel();              // Маржин колл ниже которого закрываются ордера банком
        GlobalAccount = account.isGlobal();                        // Глобальный счёт
    }
    
    public void onMessage(IMessage message) throws JFException {
        // remove of the order on the table at order close
        //context.getConsole().getOut().println(message.getType()+" - "+message.getOrder().getOrderCommand());
        
        if(message.getType() == IMessage.Type.ORDER_SUBMIT_OK){
           if(message.getOrder().getOrderCommand() == OrderCommand.BUY){
               //countBuy++;
               freeForm.lCountBuy.setText("<html><span style='font-size:12pt;'><b>"+countBuy+"</b><span><html>");
           } else if(message.getOrder().getOrderCommand() == OrderCommand.SELL){
               //countSell++;
               freeForm.lCountSell.setText("<html><span style='font-size:12pt;'><b>"+countSell+"</b><span><html>");
           }  
        } else if(message.getType() == IMessage.Type.ORDER_CLOSE_OK){
           if(message.getOrder().getOrderCommand() == OrderCommand.BUY){
               countBuy--; if (countBuy < 0) countBuy = 0;
               freeForm.lCountBuy.setText("<html><span style='font-size:12pt;'><b>"+countBuy+"</b><span><html>");
           } else if(message.getOrder().getOrderCommand() == OrderCommand.SELL){
               countSell--; if (countSell < 0) countSell = 0;
               freeForm.lCountSell.setText("<html><span style='font-size:12pt;'><b>"+countSell+"</b><span><html>");
           } 
           StopLossPrice = 0.0; freeForm.tSLPrice.setText(""); 
           TakeProfitPrice = 0.0; freeForm.tTPPrice.setText("");
        } 
        
    }
    @Override
    public void onStop() throws JFException {
         //context.closeChart(chart);
         freeForm.setVisible(false);
         freeForm.dispose();
         //chart.removeMouseListener(listener);
         //IChart chart = context.getChart(instrumentThis);
         //if(chart != null){
         //   chart.remove("MaxPriceLine");
         //   chart.remove("MaxVolume");
         //}
    } // end onStop
    
    /**
     * onTick Function
     * 
     * @param instrument
     * @param tick
     * @throws JFException 
     */
    @Override
    public void onTick(Instrument instrument, final ITick tick) throws JFException {
        if (!instrument.equals(this.instrumentThis)){
            return;
        } else {
            
          try {
            
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                   // 5 sign
                   freeForm.updatePrices(tick);
                   freeForm.setVolumes5sign(tick.getAsks(), tick.getAskVolumes(), tick.getBids(), tick.getBidVolumes(), NormalizeDouble((tick.getAsk() - tick.getBid())/pipValue, 1));
                }
            });
           } catch (Exception e) {
                   context.getConsole().getOut().println(e);
                   //context.getConsole().getOut().println(e.getCause());
           }  
        }
    }
    /**************************************************
     * ORDERS
     * Submits an order at market  price with SL and TP.
     * Submits an order at STOP Buy,Sell with SL and TP.
     * Trailing Stop
     * 
     * @param orderCmd
     * @return
     * @throws JFException
     *************************************************/
    public void submitMarketOrder(String orderType){
        try {
            pipValue = instrumentThis.getPipValue();
            double bid = history.getLastTick(instrumentThis).getBid();
            double ask = history.getLastTick(instrumentThis).getAsk(); 
            OrderCommand orderCmd = null;
            if (orderType == "Buy")
                {orderCmd = OrderCommand.BUY; orderKey = orderType+"_"+(countBuy++);}
            else if (orderType == "Sell")
                {orderCmd = OrderCommand.SELL; orderKey = orderType+"_"+(countSell++);}
            //context.getConsole().getOut().println(orderCmd);    
            if (TrailingStop) {
                order = engine.submitOrder(getLabel(instrumentThis), instrumentThis, orderCmd, Amount);
                order.waitForUpdate(2000, IOrder.State.FILLED);
                if (orderCmd.isLong())
                   {order.setStopLossPrice(bid - StopLoss * pipValue, side.BID, trailingStep); //longOrders.put(order.getLabel(), new MyOrder(order));
                }
                else
                   {order.setStopLossPrice(ask + StopLoss * pipValue, side.ASK, trailingStep); //shortOrders.put(order.getLabel(), new MyOrder(order));
                }
            } else {
                //double openPrice = bid; 
                //calculate SL and TP prices
                StopLossPrice = orderCmd.isLong() 
                  ? bid - StopLoss * pipValue 
                  : ask + StopLoss * pipValue;
                TakeProfitPrice = orderCmd.isLong() 
                  ? bid + TakeProfit * pipValue 
                  : ask - TakeProfit * pipValue;
                order = engine.submitOrder(orderKey, instrumentThis, orderCmd, Amount,0, Slippage, StopLossPrice, TakeProfitPrice);
                // Draaw line
                //IChart chart = context.getChart(instrumentThis);
                // StopLoss Line
                //StopLossLine = chart.getChartObjectFactory().createPriceMarker("StopLossPrice", StopLossPrice);
                //StopLossLine.setText(orderKey);
                //StopLossLine.setColor(Color.RED); 
                //StopLossLine.setLineStyle(LineStyle.DOT);
                //StopLossLine.setOpacity(0.0f);
                //chart.addToMainChart(StopLossLine);
                // TakeProfit Line
                //TakeProfitLine = chart.getChartObjectFactory().createPriceMarker("TakeProfitPrice", TakeProfitPrice);
                //TakeProfitLine.setText(orderKey);
                //TakeProfitLine.setColor(Color.GREEN);
                //TakeProfitLine.setOpacity(0.0f);
                //TakeProfitLine.setLineStyle(LineStyle.DOT);
                
                //chart.addToMainChart(TakeProfitLine);
                //
                //freeForm.tSLPrice.setText(String.format("%1$3.3s",StopLossPrice)); 
                //freeForm.tTPPrice.setText(String.format("%1$3.3s",TakeProfitPrice));
                //context.getConsole().getOut().println("Open "+orderType+" order - "+bid);
 
            //if (orderCmd.isLong()) {
            //    longOrders.put(order.getLabel(), new MyOrder(order));
            //} else {
            //    shortOrders.put(order.getLabel(), new MyOrder(order));
            //}
            
            }
            //myOrders.add(order); 
        } catch (JFException e) {
            context.getConsole().getOut().println(e.getCause());
        }
   
    }
    private void submitStopOrder(OrderCommand orderCmd) throws JFException {
       try { 
           double price = 0;
        double bid = history.getLastTick(instrumentThis).getBid();
        double ask = history.getLastTick(instrumentThis).getAsk();
        pipValue = instrumentThis.getPipValue();        
        
        if (TrailingStop) {
            price = orderCmd.isLong()
              ? bid + pipValue * 2
              : ask - pipValue * 2;
            order = engine.submitOrder(getLabel(instrumentThis), instrumentThis, orderCmd, Amount, price);
            order.waitForUpdate(2000, IOrder.State.FILLED);
            if (orderCmd.isLong())
               {order.setStopLossPrice(bid - StopLoss * pipValue, side.BID, trailingStep);} //longOrders.put(getLabel(instrumentThis), new MyOrder(order));}
            else
               {order.setStopLossPrice(ask + StopLoss * pipValue, side.ASK, trailingStep);} //shortOrders.put(getLabel(instrumentThis), new MyOrder(order));}
        } else {
            //calculate SL and TP prices
            double stopLossPrice = orderCmd.isLong() 
              ? bid - StopLoss * pipValue 
              : ask + StopLoss * pipValue;
            double takeProfitPrice = orderCmd.isLong() 
              ? bid + TakeProfit * pipValue 
              : ask - TakeProfit * pipValue;
            price = orderCmd.isLong()
              ? bid + pipValue * 2
              : ask - pipValue * 2; 
          order = engine.submitOrder(getLabel(instrumentThis), instrumentThis, orderCmd, Amount, price, Slippage, StopLoss, TakeProfit);
          //if (orderCmd.isLong()) {
          //    longOrders.put(getLabel(showInstrument), new MyOrder(order));
          //} else {
          //    shortOrders.put(getLabel(showInstrument), new MyOrder(order));
          //}
        }
              
      } catch (JFException e) {
            e.printStackTrace();
        }  
    }
    // label orders *************
    protected String getLabel(Instrument instrument) {
           String label = instrument.name() + "_" + (counter++);
           return label;
    }
    // Mouse adapter
    public void TraceMouse() {
        
           chart.addMouseListener(false, listener = new IChartPanelMouseListener(){
           public void mouseClicked(IChartPanelMouseEvent e) { actionMouse(e, "mouse Clicked");}
           public void mousePressed(IChartPanelMouseEvent e) { actionMouse(e, "mouse Pressed");}
           public void mouseReleased(IChartPanelMouseEvent e) { actionMouse(e, "mouse Released");}
           public void mouseEntered(IChartPanelMouseEvent e) { actionMouse(e, "mouse Entered");}
           public void mouseExited(IChartPanelMouseEvent e) { actionMouse(e, "mouse Exited");}
           public void mouseDragged(IChartPanelMouseEvent e) { actionMouse(e, "mouse Dragged");}
           public void mouseMoved(IChartPanelMouseEvent e) { actionMouse(e, "mouse Moved");}
        }); 
    }
    private void actionMouse(IChartPanelMouseEvent e, String comment){
       //call e.getPrice and e.getTime instead of e.toString if you wish to customize the logging formatting or make use of the values
        //context.getConsole().getOut().println(String.format("price=%3.7s com=%s x=%s y=%s", e.getPrice(), e.toString(), e.getSourceEvent().getXOnScreen(), e.getSourceEvent().getYOnScreen()));
         if (order.getState() != FILLED) {
             context.getConsole().getOut().println("Can't close order - order not filled: " + order.getState());
             //context.stop();
         } else
             if (comment == "mouse Dragged"){
                 try {
                         if (order.getLabel() == StopLossLine.getText()){
                             context.getConsole().getOut().println(order.getLabel());
                             order.setStopLossPrice(e.getPrice());
                         }
                         if (order.getLabel() == TakeProfitLine.getText()){
                             context.getConsole().getOut().println(order.getLabel());
                             order.setTakeProfitPrice(e.getPrice());
                         }
                         //Order.close();
                         //order.waitForUpdate(1000, CANCELED);
                         //context.getConsole().getOut().println("Close Sell orders");
                 } catch (JFException ex) {
                         context.getConsole().getOut().println(ex);
             }
              
         }              
    }
    
    /**
     * Class fxDomForm
     * 
     * Creates the form window where we can setup our trades 
     */
    class fxDomForm extends  JFrame implements ActionListener{
        // Declare
        // <editor-fold defaultstate="collapsed" desc="Class variables">
        //final String TAB_NAME = "v4.1 Depth of Market \u00A9vmm";
        final String TAB_NAME = "v4.1 Depth of Market ©vmm";
        final String CUR_NAME = String.valueOf(instrumentThis);
        private JTable table = new JTable();
        public MarketDepthTableModel tableModel;
        //
        private double MAX_PRICE = Double.NaN;
        private double MAX_ASK_TOP = Double.NaN;
        private double MAX_ASK = Double.NaN;
        private double MAX_BID = Double.NaN;
        private double MAX_PRICE_ASK = Double.NaN;
        private double MAX_PRICE_BID = Double.NaN;
        private int s = 60;
        Map<String, Double> dataBid = new HashMap<String, Double>();
        Map<String, Double> dataAsk = new HashMap<String, Double>();
        // Declare Panels
        JPanel top = new JPanel();
        JPanel btop = new JPanel();
        JPanel mtop = new JPanel();
        JPanel tmiddle = new JPanel();
        // Middle 
        // Panels
        private JScrollPane scrollPane = new JScrollPane(table);
        private JPanel middle = new JPanel();
        private JPanel boxUp = new JPanel();
        // Declare Bottom
        JPanel bottom = new JPanel();
        JPanel bmiddle = new JPanel(); 
        // Declare Buttons
        JButton btnSell = new JButton();
        JButton btnBuy = new JButton();
        final JButton upButton = new JButton();
        // Declare Lebels
        JLabel currency = new JLabel("",JLabel.CENTER);
        JLabel ispred = new JLabel("",JLabel.CENTER);
        JLabel volumeBid = new JLabel("",JLabel.CENTER);
        JLabel volumeAsk = new JLabel("",JLabel.CENTER);
        JLabel lAmount = new JLabel("Lot ",JLabel.RIGHT);
        JLabel lSlip = new JLabel(" Slippage",JLabel.LEFT);
        JLabel lSL = new JLabel("Stop Loss ",JLabel.RIGHT);
        JLabel lTP = new JLabel(" Take Profit",JLabel.LEFT);
        JLabel lSLPrice = new JLabel("Price SL ",JLabel.RIGHT);
        JLabel lTPPrice = new JLabel(" Price TP",JLabel.LEFT);
        JLabel lPP = new JLabel(".",JLabel.CENTER);
        JLabel lCountSell = new JLabel();
        JLabel lCountBuy = new JLabel();
        // Declare TextFields
        private JTextField tAmount, tSlip, tSL, tTP, tSLPrice, tTPPrice;
        //private  JTextField tAmount = new JTextField();
        //private  JTextField tSlip = new JTextField();
        //private  JTextField tTP = new JTextField();
        //private  JTextField tSLPrice = new JTextField();
        //private  JTextField tTPPrice = new JTextField();
        //private  JTextField tCounter = new JTextField();;
                 
        /**
        * class contructor for Resize fxDomForm
        * 
        * 
        */
        public fxDomForm() {
               initPlaceControlsOnFrame();
        } 
        @Override
        public void paint(Graphics g) {
                Dimension d = getSize();
                Dimension m = getMaximumSize();
                boolean resize = d.width > m.width || d.height > m.height;
                d.width = Math.min(m.width, d.width);
                d.height = Math.min(m.height, d.height);
                if (resize) {
                    Point p = getLocation();
                    setVisible(false);
                    setSize(d);
                    setLocation(p);
                    setVisible(true);
                }
                super.paint(g);
        }
        
        
    /**
    * Class fxDomForm
    * 
    * Creates the form window where we can setup our trades 
    */
    @SuppressWarnings("unchecked")
    private void initPlaceControlsOnFrame() {
        // Main form
        setFont(new Font("Tahoma", 1, 10));
        setTitle(TAB_NAME);
        setAlwaysOnTop(true);
        setBackground(Color.WHITE);
     
        setResizable(true);
        setSize(340,1000);
        setMaximumSize(new Dimension(340,1500));
        setMinimumSize(new Dimension(340,700));
        //setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {context.stop();}
        });
        // End Main form
        // TOP
        // Надпись валюты
        btop.setLayout(new BorderLayout());
        btop.setPreferredSize(new Dimension(320,150));
        btop.setBackground(Color.LIGHT_GRAY);
        final String CUR_NAME = String.valueOf(instrumentThis);
        currency.setText(CUR_NAME);
        currency.setPreferredSize(new Dimension(300, 25));
        currency.setFont(new Font("Serif",Font.BOLD,18));
        currency.setForeground(Color.WHITE);
        btop.add(currency, BorderLayout.NORTH);
        
        // Кнопки Sell & Buy
        //top.setPreferredSize(new Dimension(300, 30));
        top.setLayout(null);
        //top.setLayout(new BorderLayout());
        top.setBackground(Color.LIGHT_GRAY);
        //TitledBorder topBorder = BorderFactory.createTitledBorder("Inside Market");
        //topBorder.setTitleJustification(TitledBorder.CENTER);
        //top.setBorder(topBorder);
        top.setPreferredSize(new Dimension(340,100));
        CurvedBorder border = new CurvedBorder(2, Color.gray, 10);
        // Кнопка Sell
        String sBid = "<html><div style='border: 1px #CCCCCC solid;background-color:#ffdcdc;height:85;width:125;margin-top:10;'>"
                    + "<div style='background-color:black;color:white;'><span style='font-size:11pt;text-align:left;'>&nbsp;&nbsp;<b>Bid</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0.0</span></div>"
                    + "<div style='text-align:center;margin-bottom:-5;'><span style='font-size:16pt;'>000.</span><span style='font-size:30pt;color:red;'>00</span><span style='font-size:16pt;'>0</span></div>"
                    + "<div style='text-align:center;'><span style='font-size:10pt;'><b>SELL</b></span></div>"
                    + "</div></html>";
        btnSell.setText(sBid);
        //btnSell.setPreferredSize(new Dimension(170, 40));
        btnSell.setBorder(border);
        btnSell.setVisible(true);
        btnSell.setSize(130, 67);
        btnSell.setLocation(15,0);
        btnSell.setToolTipText("Best price of Buyer");
        //btnSell.addActionListener(new ActionListener() {
        //    public void actionPerformed(ActionEvent event) {
        //        submitMarketOrder(OrderCommand.BUY);
        //    } 
        //});
        // Counts of Sell, Buy commands
        lCountSell.setSize(10, 30);
        lCountSell.setLocation(6,20);
        lCountSell.setToolTipText("");
        lCountSell.setText("<html><span style='font-size:12pt;'><b>0</b><span><html>");
        lCountBuy.setSize(10, 30);
        lCountBuy.setLocation(310,20);
        lCountBuy.setToolTipText("");
        lCountBuy.setText("<html><span style='font-size:12pt;'><b>0</b><span><html>");
        // Кнопка Buy
        String sAsk = "<html><div style='border: 1px #CCCCCC solid;background-color:#e6ffe6;height:85;width:125;margin-top:10;'>"
                    + "<div style='background-color:black;color:white;text-align:right;'><span style='font-size:11pt;'>0.0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>Ask</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span></div>"
                    + "<div style='text-align:center;margin-bottom:-5;'><span style='font-size:16pt;'>000.</span><span style='font-size:30pt;color:green;'>00</span><span style='font-size:16pt;'>0</span></div>"
                    + "<div style='text-align:center;'><span style='font-size:10pt;'><b>BUY</b></span></div>"
                    + "</div></html>";
        btnBuy.setText(sAsk);
        //btnBuy.setSize(100);
        //btnBuy.setPreferredSize(new Dimension(170, 40));
        btnBuy.setBorder(border);
        btnBuy.setVisible(true);
        btnBuy.setSize(128, 67);
        btnBuy.setLocation(175,0);
        btnBuy.setToolTipText("Best price of Seller");
        //btnSell.addActionListener(new ActionListener() {
        //    public void actionPerformed(ActionEvent event) {
        //        submitMarketOrder(OrderCommand.BUY);
        //    } 
        //});
        ispred.setVisible(true);
        ispred.setText("0");
        ispred.setSize(110, 80);
        ispred.setLocation(110,-9);
        //ispred.setPreferredSize(new Dimension(110, 110));
        //ispred.setFont(new Font("Serif",Font.BOLD,18));
        //ispred.setToolTipText("The possible formation of resistance or support");            
        //
        top.add(lCountSell);
        top.add(ispred);
        top.add(btnSell);
        
        top.add(btnBuy);
        top.add(lCountBuy);
        //
        mtop.setLayout(new GridLayout(3,4));
        mtop.setBackground(Color.LIGHT_GRAY);
        //mtop.setBackground(Color.WHITE);
        mtop.setPreferredSize(new Dimension(300,58));
        // SELL BUY
        // TextField
        tAmount = new JTextField(Double.toString(Amount)); 
        
        tSlip = new JTextField(String.format("%1.3s", (int)Slippage));
        tSL = new JTextField(String.format("%1.3s", (int)StopLoss));
        tTP = new JTextField(String.format("%1$1.3s", (int)TakeProfit));
        tSLPrice = new JTextField(""); 
        tTPPrice = new JTextField("");
        tSLPrice.setEditable(false); tTPPrice.setEditable(false);
        // Выравнивание текста
        tAmount.setHorizontalAlignment(JTextField.CENTER);
        tSlip.setHorizontalAlignment(JTextField.CENTER);
        tSL.setHorizontalAlignment(JTextField.CENTER);
        tTP.setHorizontalAlignment(JTextField.CENTER);
        tSLPrice.setHorizontalAlignment(JTextField.CENTER);
        tTPPrice.setHorizontalAlignment(JTextField.CENTER);
        // Ограничение количества символов
        AbstractDocument doc = (AbstractDocument) tAmount.getDocument();
        doc.setDocumentFilter(new LengthFilter(doc.getLength(), 5));
        AbstractDocument doc1 = (AbstractDocument) tSlip.getDocument();
        doc1.setDocumentFilter(new LengthFilter(doc1.getLength(), 4));
        AbstractDocument doc2 = (AbstractDocument) tSL.getDocument();
        doc2.setDocumentFilter(new LengthFilter(doc2.getLength(), 4));
        AbstractDocument doc3 = (AbstractDocument) tTP.getDocument();
        doc3.setDocumentFilter(new LengthFilter(doc3.getLength(), 4));
        //
        tAmount.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!((c >= '0') && (c <= '9') ||
                        (c == KeyEvent.VK_BACK_SPACE) ||
                        (c == KeyEvent.VK_DELETE) || 
                        c == '.')) {
                        e.consume();
                    }
                    if((c == '.') && tAmount.getText().contains(".")){
                        e.consume();
                    }
               }
                
        });
        tSL.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!((c >= '0') && (c <= '9') ||
                        (c == KeyEvent.VK_BACK_SPACE) ||
                        (c == KeyEvent.VK_DELETE) || 
                        c == '.')) {
                        e.consume();
                    }
                    if((c == '.') && tSL.getText().contains(".")){
                        e.consume();
                    }
                    //context.getConsole().getOut().println(tSL.getText());
                    //StopLoss = Double.valueOf(tSL.getText());
                }
        });
        tTP.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!((c >= '0') && (c <= '9') ||
                        (c == KeyEvent.VK_BACK_SPACE) ||
                        (c == KeyEvent.VK_DELETE) || 
                        c == '.')) {
                        e.consume();
                    }
                    if((c == '.') && tTP.getText().contains(".")){
                        e.consume();
                    }
                }
        }); 
        tSlip.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!((c >= '0') && (c <= '9') ||
                        (c == KeyEvent.VK_BACK_SPACE) ||
                        (c == KeyEvent.VK_DELETE) || 
                        c == '.')) {
                        e.consume();
                    }
                    if((c == '.') && tSlip.getText().contains(".")){
                        e.consume();
                    }
                }
        });
        //mtop.add(lCounter); mtop.add(tCounter);
        mtop.add(lAmount); mtop.add(tAmount);
        mtop.add(tSlip); mtop.add(lSlip);  
        mtop.add(lSL); mtop.add(tSL);
        //mtop.add(lPP);
        mtop.add(tTP); mtop.add(lTP); 
        //mtop.add(lPP);
        mtop.add(lSLPrice); mtop.add(tSLPrice);
        mtop.add(tTPPrice); mtop.add(lTPPrice); 
        //mtop.add(lPP); 
        btop.add(top, BorderLayout.CENTER);
        btop.add(mtop, BorderLayout.SOUTH);
        getContentPane().add(btop, BorderLayout.NORTH);
        // Суммарный объём ликвидности по Бид и Аск
        volumeBid.setSize(170, 15);
        volumeAsk.setSize(170, 15);
        volumeBid.setLocation(40,0);
        volumeAsk.setLocation(150,0);
        volumeBid.setText("<html><div style='text-align:center;width:170;'>sum Bid&nbsp;&nbsp;<b>0.0</b></div></html>");
        volumeAsk.setText("<html><div style='text-align:center;width:170;'><b>0.0</b>&nbsp;&nbsp;sum Ask</div></html>");
        volumeBid.setToolTipText("The total volume of liquidity on the Bid");
        volumeAsk.setToolTipText("The total volume of liquidity on the Ask");
        //upButton.setSize(56, 15);
        //upButton.setLocation(105,0);
        //upButton.setText("<html><p style='font-size:10pt;color:green;text-align:center;'>&#9650;</p></html>");
        //upButton.setToolTipText("Move price up");
        //upButton.addActionListener(new ActionListener() {
        //    public void actionPerformed(ActionEvent event) {
        //        MAX_TOP_ADD = NormalizeDouble(MAX_TOP_ADD - pipValue/10,(pipScale+1));
        //    } 
        //});
        //tmiddle.setLayout(null);
        //tmiddle.setPreferredSize(new Dimension(300, 15));
        //tmiddle.setBackground(Color.LIGHT_GRAY);
        //tmiddle.add(volumeBid);
        //tmiddle.add(upButton);
        //tmiddle.add(volumeAsk);
        //  
        middle.setLayout(new BorderLayout());
        middle.add(scrollPane, BorderLayout.CENTER);
        middle.add(tmiddle, BorderLayout.NORTH);
        getContentPane().add(middle, BorderLayout.CENTER);
        // Jtable settings
        // Jtable model
        tableModel = new MarketDepthTableModel();
        table.setModel(tableModel);
        //table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setColumnSelectionAllowed(false);
        table.getTableHeader().setReorderingAllowed(false); // запрет на перетаскивание колонок
        table.editingStopped(null);
        table.removeEditor();
        //это можно не писать и без него работает отлично
        table.removeNotify();
        //Изменяем высоту строк таблицы
        //table.setRowHeight(0,10);
        
        //Изменяем ширину столбцов таблицы
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(60);
        table.getColumnModel().getColumn(2).setPreferredWidth(72);
        table.getColumnModel().getColumn(3).setPreferredWidth(30);
        table.getColumnModel().getColumn(4).setPreferredWidth(30);
        table.getColumnModel().getColumn(5).setPreferredWidth(72);
        
        table.getColumnModel().getColumn(0).setResizable(false);
        table.getColumnModel().getColumn(1).setResizable(false);
        table.getColumnModel().getColumn(2).setResizable(false);
        table.getColumnModel().getColumn(3).setResizable(false);
        table.getColumnModel().getColumn(4).setResizable(false);
        table.getColumnModel().getColumn(5).setResizable(false);
        // Jable header
        JTableHeader header = table.getTableHeader();
        // Jable border
        Border tBorder = BorderFactory.createLineBorder(new Color(0x8b8989));
        //Border tBorder =  BorderFactory.createLineBorder(new  Color(0, 0, 0));
        header.setBorder(tBorder);
        header.setBackground(new Color(0x8b8989));
        table.setDefaultRenderer(Object.class, new DateCellRenderer());
       
        // positioning of all components on the window
        
        //GroupLayout layout = new  GroupLayout(getContentPane());
        
        // Buttons Panel
        bottom.setLayout(new BorderLayout());
        bottom.setPreferredSize(new Dimension(340, 85));
        TitledBorder upBorder = BorderFactory.createTitledBorder("");
        upBorder.setTitleJustification(TitledBorder.CENTER);
        
        tmiddle.setLayout(null);
        tmiddle.setPreferredSize(new Dimension(340, 20));
        tmiddle.setBackground(Color.WHITE);
        tmiddle.add(volumeBid);
        tmiddle.add(upButton);
        tmiddle.add(volumeAsk);
        bottom.add(tmiddle, BorderLayout.NORTH);       
        //bmiddle.setLayout(null);
        bmiddle.setPreferredSize(new Dimension(340, 30));
        bmiddle.setLayout(new GridLayout(1,2));
        // close Sell
        closeSell.setText("<html><div style='border: 1px red solid;background-color:#ffdcdc;text-align:center;font-size:12pt;margin-left:-15;height:25;width:90;padding:3px;'>Close Sell orders</div></html>");
        //ordSell.setSize(70, 20);
        //ordSell.setLocation(25,5);
        closeSell.setToolTipText("Close Sell orders");
        // close All
        closeAll.setText("<html><div style='border: 1px blue solid;background-color:#dcffff;text-align:center;font-size:12pt;margin-left:-15;height:25;width:90;padding:3px;'>Close All</div></html>");
        //ordSell.setSize(70, 20);
        //ordSell.setLocation(25,5);
        closeAll.setToolTipText("Close All");
        // close Buy
        closeBuy.setText("<html><div style='border: 1px green solid;background-color:#e6ffe6;text-align:center;font-size:12pt;margin-left:-15;height:25;width:90;padding:3px;'>Close Buy orders</div></html>");
        //ordBuy.setSize(52, 20);
        //ordBuy.setLocation(200,5);
        closeBuy.setToolTipText("Close Buy orders");
        bmiddle.add(closeSell);
        bmiddle.add(closeAll);
        bmiddle.add(closeBuy);
        bottom.add(bmiddle, BorderLayout.CENTER);  
        // Checks
        
        boxUp.setPreferredSize(new Dimension(340, 40));
        boxUp.setLayout(new GridLayout(2,2));
        bottom.add(boxUp, BorderLayout.SOUTH);
        boxUp.setBorder(upBorder);
        tickBox.setFont(new Font("Times New Roman", 1, 11));
        boxUp.add(tickBox);
        domVisible.setFont(new Font("Times New Roman", 1, 11));
        boxUp.add(domVisible);
        bestPriceVisible.setFont(new Font("Times New Roman", 1, 11));
        boxUp.add(bestPriceVisible);
        topPanelVisible.setFont(new Font("Times New Roman", 1, 11));
        boxUp.add(topPanelVisible);
        //moveLineTPSL.setFont(new Font("Times New Roman", 1, 11));
        //boxUp.add(moveLineTPSL);
        //lifeLineVisible.setFont(new Font("Times New Roman", 1, 11));
        //boxUp.add(lifeLineVisible);
        getContentPane().add(bottom, BorderLayout.SOUTH);
        // Прослушки для кнопок и галочек
        btnSell.addActionListener(this);
        btnBuy.addActionListener(this);
        closeSell.addActionListener(this);
        closeAll.addActionListener(this);
        closeBuy.addActionListener(this);
        
        tickBox.addActionListener(this);
        domVisible.addActionListener(this);
        bestPriceVisible.addActionListener(this);
        topPanelVisible.addActionListener(this);

    }
     
     // 5 sign
     public void setVolumes5sign(double[] asks, double[] askVols, double[] bids, double[] bidVols, double spread) {
        int i = 0; 
        int b = 0; 
        int a = asks.length-1;  
        double sum; 
        MAX_SUM = 0; 
        MAX_PRICE = 0;
        String rate; 
        boolean keyA = false;  
        boolean keyB = false;
        String[][] data = new String[s][8];
        // Top level
        if(windowVisible){
           //MAX_ASK_TOP = NormalizeDouble((asks[asks.length-1]+pipValue),(pipScale+1));
           MAX_ASK_TOP = NormalizeDouble((MAX_TOP_ADD+asks[asks.length-1]+(pipValue*2)),(pipScale+1));
        } else {      
           if (MAX_LEV_TOP == 0) {MAX_LEV_TOP = NormalizeDouble((asks[asks.length-1]+(pipValue*2)),(pipScale+1));}
           MAX_ASK_TOP = NormalizeDouble((MAX_LEV_TOP+MAX_TOP_ADD),(pipScale+1));
        }
        // Start   
        for (i = 0; i < s; i++) {
            MAX_ASK_TOP = NormalizeDouble((MAX_ASK_TOP-pipValue/10),(pipScale+1));
            
            // Поле 1 - price
            data[i][1] = String.format("%3.7s", MAX_ASK_TOP+"00000");
            rate = data[i][1];
   
            // Поле 3 - bid
            for (b = 0; b < bids.length-1; b++) {if (MAX_ASK_TOP==bids[b]) break;}
            if (MAX_ASK_TOP==bids[b]){
                if (b==0) {
                    data[i][6] = "1"; keyB = true;
                                
                    if (dataBid.containsKey(rate)){
                        sum = NormalizeDouble((dataBid.get(rate) + bidVols[0]),2);
                        dataBid.replace(String.format("%3.7s", MAX_ASK_TOP+"00000"), sum);
                    } else {
                        sum = bidVols[0];
                        dataBid.put(String.format("%3.7s", MAX_ASK_TOP+"00000"), sum);  
                    }
                   
                } else {
                    data[i][6] = "2";
                }
                data[i][3] = String.valueOf(bidVols[b]);
                if (b<bids.length-1) b = b + 1;
            } else {
                data[i][3] = ""; if (keyB == false) {data[i][6] = "";} else {data[i][6] = "2";}
            }
            
            // Поле 2 - vol bid
            if (dataBid.get(rate)!=null){
                sum = dataBid.get(rate); 
                data[i][2] = String.valueOf(sum);
                if (sum > MAX_SUM) {MAX_SUM = sum;}
                if (sum > MAX_BID) {MAX_BID = sum; MAX_PRICE_BID = new Double(data[i][1]);}
            } else  data[i][2] = "";
            
            // Поле 4 - ask
            for (a = asks.length-1; a > 0; a--) {if (MAX_ASK_TOP==asks[a]) break;}
            if (MAX_ASK_TOP==asks[a]){
                
                if (a==0) {
                    data[i][7] = "1"; keyA = true;
                   
                    if (dataAsk.containsKey(rate)){
                        sum = NormalizeDouble((dataAsk.get(rate) + askVols[0]),2);
                        dataAsk.replace(String.format("%3.7s", MAX_ASK_TOP+"00000"), sum); 
                    } else {
                        sum = askVols[0];
                        dataAsk.put(String.format("%3.7s", MAX_ASK_TOP+"00000"), sum);
                    }
                   
                } else {
                    data[i][7] = "2";
                }
                data[i][4] = String.valueOf(askVols[a]);
                if (a > 0) a = a - 1;
            } else {
                data[i][4] = ""; if (keyA == false) {data[i][7] = "2";} else {data[i][7] = "";}
            }
            // Поле 5 - vol ask
            if (dataAsk.get(rate)!=null){
                sum = dataAsk.get(rate); 
                data[i][5] = String.valueOf(sum);
                if (sum > MAX_SUM) {MAX_SUM = sum;}
                if (sum > MAX_ASK) {MAX_ASK = sum; MAX_PRICE_ASK = new Double(data[i][1]);}
            } else  data[i][5] = "";
            
            // Поле 0 - total
            if (dataBid.containsKey(rate) && dataAsk.containsKey(rate)){
                    sum = NormalizeDouble((dataBid.get(rate)+dataAsk.get(rate)),2);
                    data[i][0] = String.valueOf(sum);
            } else if (dataBid.containsKey(rate)){
                    sum = NormalizeDouble(dataBid.get(rate),2);
                    data[i][0] = String.valueOf(sum);
            } else if (dataAsk.containsKey(rate)){
                    sum = NormalizeDouble(dataAsk.get(rate),2);
                    data[i][0] = String.valueOf(sum);                
            } else  data[i][0] = "";          
            //if (dataTot.get(rate)!=null){
            //    sum = dataTot.get(rate); 
            //    data[i][0] = String.valueOf(sum);
            //} else  data[i][0] = ""; 
        }
        tableModel.setData(data); 
     } // 5 sign
     /**
         * updatePrices Function
         * 
         * Update the Labels of bid and ask price
         * 
         * @param tick 
         */
        public void updatePrices(ITick tick){
            if (topVisible == true) {
            tickBid = tick.getBid();
            tickAsk = tick.getAsk();
            BidVolume = tick.getBidVolume();
            AskVolume = tick.getAskVolume();
            //spread = NormalizeDouble((tick.getAsk() - tick.getBid())/pipValue, 1);
            dBid = String.format("%7.7s",Double.toString(tickBid)+"00000");
            dAsk = String.format("%7.7s",Double.toString(tickAsk)+"00000");
            BidTotalVolume = tick.getTotalBidVolume();
            AskTotalVolume = tick.getTotalAskVolume();
            // Кнопка Sell
            String sBid = "<html><div style='border: 1px #CCCCCC solid;background-color:#ffdcdc;height:85;width:140;margin-top:10;'>"
                    + "<div style='background-color:black;color:white;'><span style='font-size:11pt;text-align:left;'>&nbsp;&nbsp;<b>Bid</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+BidVolume+"</span></div>"
                    + "<div style='text-align:center;margin-bottom:-5;'><span style='font-size:16pt;'>"+dBid.substring(0,4)+"</span><span style='font-size:30pt;color:red;'>"+dBid.substring(4,6)+"</span><span style='font-size:16pt;'>"+dBid.substring(6)+"</span></div>"
                    + "<div style='text-align:center;'><span style='font-size:10pt;'><b>SELL</b></span></div>"
                    + "</div></html>";
            btnSell.setText(sBid);
            // Кнопка Buy
            String sAsk = "<html><div style='border: 1px #CCCCCC solid;background-color:#e6ffe6;height:85;width:140;margin-top:10;'>"
                    + "<div style='background-color:black;color:white;text-align:right;'><span style='font-size:11pt;'>"+AskVolume+"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>Ask</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span></div>"
                    + "<div style='text-align:center;margin-bottom:-5;'><span style='font-size:16pt;'>"+dAsk.substring(0,4)+"</span><span style='font-size:30pt;color:green;'>"+dAsk.substring(4,6)+"</span><span style='font-size:16pt;'>"+dAsk.substring(6)+"</span></div>"
                    + "<div style='text-align:center;'><span style='font-size:10pt;'><b>BUY</b></span></div>"
                    + "</div></html>";
            btnBuy.setText(sAsk);
            volumeBid.setText("<html><div style='text-align:center;width:170;'>sum Bid&nbsp;&nbsp;<b>"+BidTotalVolume+"</b></div></html>");
            volumeAsk.setText("<html><div style='text-align:center;width:170;'><b>"+AskTotalVolume+"</b>&nbsp;&nbsp;sum Ask</div></html>");
            if(StopLossPrice == 0) tSLPrice.setText(""); else tSLPrice.setText(String.format("%3.7s",StopLossPrice+"00000"));
            if(TakeProfitPrice == 0) tTPPrice.setText(""); else tTPPrice.setText(String.format("%3.7s",TakeProfitPrice+"00000"));
            
            volAsk = String.format("%2.4s", tick.getAskVolume()-tick.getBidVolume());
            volBid = String.format("%2.4s", tick.getBidVolume()-tick.getAskVolume());
            spread = NormalizeDouble((tickAsk - tickBid)/pipValue, 1);
            if (tick.getAskVolume()==tick.getBidVolume())
               ispred.setText("<html><p>&#8195;</p>"+String.valueOf(spread)+"<p>&#8195;</p></html>");
            else if (tick.getAskVolume()>tick.getBidVolume())  
               ispred.setText("<html><p style='font-size:10pt;text-align:center;'>"+volAsk+"</p><p style='font-size:12pt;color:green;text-align:center;color:green;'>&#9650;</p><b>"+String.valueOf(spread)+"</b><p>&#8195;</p><p style='font-size:10pt;text-align:center;'>&#8195;</p></html>");
            else if (tick.getAskVolume()<tick.getBidVolume())  
               ispred.setText("<html><p style='font-size:10pt;text-align:center;color:red;'></p><p>&#8195;</p><b>"+String.valueOf(spread)+"</b><p style='font-size:12pt;color:red;text-align:center;'>&#9660;</p><p style='font-size:10pt;text-align:center;'>"+volBid+"</p></html>");
            lCountBuy.setText("<html><span style='font-size:12pt;'><b>"+countBuy+"</b><span><html>");
            lCountSell.setText("<html><span style='font-size:12pt;'><b>"+countSell+"</b><span><html>");
            } else {
               BidTotalVolume = tick.getTotalBidVolume();
               AskTotalVolume = tick.getTotalAskVolume();  
               volumeBid.setText("<html><div style='text-align:center;width:170;'>sum Bid&nbsp;&nbsp;<b>"+BidTotalVolume+"</b></div></html>");
               volumeAsk.setText("<html><div style='text-align:center;width:170;'><b>"+AskTotalVolume+"</b>&nbsp;&nbsp;sum Ask</div></html>");  
            }
            //  
        }// end updatePrices
        /**
         * Validate the textfields values and return true if ok
         * 
         * @return true if text fields ok, false otherwise 
         */
        private boolean validateFields(){
            Amount = Double.valueOf(tAmount.getText());
            Slippage = Double.valueOf(tSlip.getText());
            StopLoss = Double.valueOf(tSL.getText());
            TakeProfit = Double.valueOf(tTP.getText());
            //context.getConsole().getOut().println(StopLoss+" - "+TakeProfit);
            return true;
        }// end validateprices
     /* actionPerformed Function
     * 
     * Window events for radiobuttons and buttons
     * 
     * @param e 
    */
     @Override
     public void actionPerformed(ActionEvent ex) {
 
        if(ex.getSource()==btnSell){
           SwingUtilities.invokeLater(new Runnable() {public void run() { 
                boolean fieldsOk = validateFields();
                if (!fieldsOk){
                    return;
                }
                try {
                    submitMarketOrder("Sell");
                    lCountSell.setText("<html><span style='font-size:12pt;'><b>"+countSell+"</b><span><html>");
                } catch (Exception ex) {
                    context.getConsole().getOut().println(ex);
                } 
           } });
        } else if(ex.getSource()==btnBuy){
           SwingUtilities.invokeLater(new Runnable() {public void run() {
                boolean fieldsOk = validateFields();
                if (!fieldsOk){
                    return;
                }
                try {
                    submitMarketOrder("Buy");
                    lCountBuy.setText("<html><span style='font-size:12pt;'><b>"+countBuy+"</b><span><html>");
                } catch (Exception ex) {
                    context.getConsole().getOut().println(ex);
                }  
           } });    
        } else if(ex.getSource()==closeSell){
           SwingUtilities.invokeLater(new Runnable() {public void run() { closeOrders("closeSell"); } }); 
        } else if(ex.getSource()==closeAll){
           SwingUtilities.invokeLater(new Runnable() {public void run() { closeOrders("closeAll"); } });
        } else if(ex.getSource()==closeBuy){
           SwingUtilities.invokeLater(new Runnable() {public void run() { closeOrders("closeBuy"); } });     
        } else if(ex.getSource()==tickBox){
                  // Поверх всех окон
                  if(tickBox.isSelected()) {
                     SwingUtilities.invokeLater(new Runnable() {public void run() { freeForm.setAlwaysOnTop(true);  } });
                       } 
                  else {
                       SwingUtilities.invokeLater(new Runnable() {public void run() { freeForm.setAlwaysOnTop(false);  } });
                       }
         } else if(ex.getSource()==topPanelVisible){
                  // Окно верхнее
                  if(topPanelVisible.isSelected()) {
                     SwingUtilities.invokeLater(new Runnable() {public void run() { topVisible = true; btop.setVisible(true); } });
                       } 
                  else {
                     SwingUtilities.invokeLater(new Runnable() {public void run() { topVisible = false; btop.setVisible(false); } });
                       }
         } else if(ex.getSource()==domVisible){
                  // Окно DOM всегда в середине
                  if(domVisible.isSelected()) {
                     SwingUtilities.invokeLater(new Runnable() {public void run() { windowVisible = true; MAX_LEV_TOP = 0; MAX_TOP_ADD = 0; } });
                       } 
                  else {
                     SwingUtilities.invokeLater(new Runnable() {public void run() { windowVisible = false; } });
                       }
         } else if(ex.getSource()==bestPriceVisible){
                   if(bestPriceVisible.isSelected()) {
                      SwingUtilities.invokeLater(new Runnable() {public void run() { onlyBestPrice = true; } });
                        } 
                   else {
                      SwingUtilities.invokeLater(new Runnable() {public void run() { onlyBestPrice = false; } });
                        }
         }

     } // end actionPerformed
        
        // Ордера закрытия - Closs orders
        //private void closeAll(Map<String, MyOrder> orders) throws JFException {
        //    for (String label : orders.keySet()) {
        //         MyOrder order = orders.get(label);
        //         order.close();
        //    }
        //}   orders.clear();
        public void closeOrders(String command){
       
            if (order == null)
                context.getConsole().getOut().println("No open orders!");
            else {
              //if (order.getState() != FILLED) {
              //    context.getConsole().getOut().println("Can't close order - order not filled: " + order.getState());
                //context.stop();
              //} else
                 if (command == "closeSell"){
                   try {
                         for(IOrder o: engine.getOrders()){
                             //console.getNotif().println("active orders: " + engine.getOrders());
                             if (o.getOrderCommand() == OrderCommand.SELL) {
                                 o.close();
                                 //o.waitForUpdate(1000, CLOSED, CANCELED);
                                 //countSell = 0;
                                 lCountSell.setText("<html><span style='font-size:12pt;'><b>"+countSell+"</b><span><html>");
                                 StopLossPrice = 0.0; tSLPrice.setText(""); 
                                 TakeProfitPrice = 0.0; tTPPrice.setText("");
                              }
                         }
                         //context.getConsole().getOut().println("Close Sell orders");
                       } catch (JFException ex) {
                         context.getConsole().getOut().println(ex);
                       }  
                 } else if (command == "closeAll"){
                   try {
                          for(IOrder o: engine.getOrders()){
                             if (o.getOrderCommand() == OrderCommand.SELL || o.getOrderCommand() == OrderCommand.BUY) {
                                 o.close();
                                 //countSell = 0;
                                 //countBuy = 0;
                                 lCountSell.setText("<html><span style='font-size:12pt;'><b>"+countSell+"</b><span><html>");
                                 lCountBuy.setText("<html><span style='font-size:12pt;'><b>"+countBuy+"</b><span><html>");
                                 StopLossPrice = 0.0; tSLPrice.setText(""); 
                                 TakeProfitPrice = 0.0; tTPPrice.setText("");
                                 
                             } else {
                                 console.getOut().println("Unable to close order in state: " + order.getState());
                             }   
                          }
                
                   } catch (JFException ex) {
                         context.getConsole().getOut().println(ex);
                       }  
                 } else if (command == "closeBuy"){
                   try {
                         for(IOrder o: engine.getOrders()){
                             //context.getConsole().getOut().println("active orders: " + engine.getOrders());
                             if (o.getOrderCommand() == OrderCommand.BUY) {
                                 o.close();
                                 //o.waitForUpdate(1000, CLOSED, CANCELED);
                                 //countBuy = 0;
                                 lCountBuy.setText("<html><span style='font-size:12pt;'><b>"+countBuy+"</b><span><html>");
                                 StopLossPrice = 0.0; tSLPrice.setText(""); 
                                 TakeProfitPrice = 0.0; tTPPrice.setText("");
                             }
                         }
                         //Order.close();
                         //order.waitForUpdate(1000, CANCELED);
                         //context.getConsole().getOut().println("Close Buy orders");
                       } catch (JFException ex) {
                         context.getConsole().getOut().println(ex);          
                       }
      
              }
            }
       }  //End metod closeOrders
     
    } //End class fxDomForm
   
    //
    public class LengthFilter extends DocumentFilter {
       private int currentLength;
       private int maxLength;
       public LengthFilter(int currentLength, int maxLength)
       {
         this.currentLength = currentLength;
         this.maxLength = maxLength;
       }
       public void remove(FilterBypass fb, int offset, int length) throws BadLocationException
       {
         currentLength -= length;
         fb.remove(offset, length);
       }
       public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException
       {
         if((currentLength + string.length()) <= maxLength)
         {
             currentLength += string.length();
             fb.insertString(offset, string, attr);
         }
      }
      public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attr) throws BadLocationException
      {
         if((currentLength - length + string.length()) <= maxLength)
         {
             currentLength += string.length() - length;
             fb.replace(offset, length, string, attr);
         }
      }
    }

    /**
     * Классы DOM
     *
     */
     // Граница с округлением
  
  class CurvedBorder extends AbstractBorder {
  private int r = 1;
  private Color wallColor = Color.decode("0xCCCCCC");
  private int sinkLevel = 1;
  public CurvedBorder() {
  }

  public CurvedBorder(int sinkLevel) {
    this.sinkLevel = sinkLevel;
  }

  public CurvedBorder(Color wall) {
    this.wallColor = wall;
  }
  
  public CurvedBorder(int sinkLevel, Color wall, int radius) {
    this.sinkLevel = sinkLevel;
    this.wallColor = wall;
    this.r = radius;
  }
  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape border = new RoundRectangle2D.Double(x+5, y+1, width-8, height-3, r, r);
        Container parent = c.getParent();
        if (Objects.nonNull(parent)) {
            g2.setPaint(parent.getBackground());
            Area corner = new Area(new Rectangle2D.Double(x, y, width, height));
            corner.subtract(new Area(border));
            g2.fill(corner);
        }
        g2.setPaint(Color.GRAY);
        g2.draw(border);
        g2.dispose();
    }
    
  public Insets getBorderInsets(Component c) {
    return new Insets(sinkLevel, sinkLevel, sinkLevel, sinkLevel);
  }

  public Insets getBorderInsets(Component c, Insets i) {
    i.left = i.right = i.bottom = i.top = sinkLevel;
    return i;
  }

  public boolean isBorderOpaque() {
    return true;
  }

  public int getSinkLevel() {
    return sinkLevel;
  }

  public Color getWallColor() {
    return wallColor;
  }
  public int getRadius() {
    return r;
  }
  }
   
    // Модель данных таблицы
    class MarketDepthTableModel extends AbstractTableModel {
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
        return 6;
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
                return "  Total"; 
            case 1:
                return "  Price";
            case 2:
                return "Bid acc. vol.";        
            case 3:
                return "Bid";    
            case 4:
                return "Ask";
            case 5:
                return "Ask acc. vol.";
            case 6:
                return "Bid0";    
            case 7:
                return "Ask0";
            default:
                return "";
        }
    }
    }
    // Классы DOM
    double NormalizeDouble(double val, int prec) {
       String pattern = "0.0";
       for(int i=1;i<prec;i++){
           if(i==prec-1) pattern += "#";
           else pattern += "0";
       }
       DecimalFormat applydeci = new DecimalFormat(pattern);
       Double ret = new Double(applydeci.format(val).replace(',', '.')).doubleValue();
       return(ret);
    }
    static public String customFormat(String pattern, double value ) {
      //customFormat("###,###.###", 123456.789);  
      DecimalFormat myFormatter = new DecimalFormat(pattern);
      String output = myFormatter.format(value);
      return output;
    }
    /**
     * Sets the foreground/background colors of the cells
     */
    // class Окрашивание окон
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
        if(column==0){
            c.setHorizontalAlignment(JLabel.RIGHT);
            c.setText("<html><div style='border: 1em solid #ffffff;font-size:10.0pt;text-align:right;padding:1px;'>"+value.toString()+"</div></html>");
            //c.setText(value.toString());
        }
        if(column==6){c.setHorizontalAlignment(JLabel.RIGHT);c.setText(value.toString());}
        if(column==7){c.setHorizontalAlignment(JLabel.RIGHT);c.setText(value.toString());}
        // Price
        if(column==1){c.setHorizontalAlignment(JLabel.CENTER);c.setText("<html><b>"+value.toString()+"</b></html>");}
        // Выделение стакана
        if (onlyBestPrice){
            // Показать лучшую цену Bid и Ask.
            if (table.getModel().getValueAt(row, 6).toString() == "1"){if (column==1 || column==3 || column==4){c.setOpaque(true);c.setBackground(Color.PINK);if(column==3){c.setHorizontalAlignment(JLabel.RIGHT);c.setText(value.toString());}}}          //Bid - розовый   
            if (table.getModel().getValueAt(row, 7).toString() == "1"){if (column==1 || column==3 || column==4){c.setOpaque(true);c.setBackground(new Color(0x5aff5a));if(column==4){c.setHorizontalAlignment(JLabel.LEFT);c.setText(value.toString());}}}  //Ask - зелёный
        } else {
            // Показать весь стакан, цены Bid и Ask, разряжение стакана.
            if(column==3){c.setHorizontalAlignment(JLabel.RIGHT);c.setText(value.toString());}
            if(column==4){c.setHorizontalAlignment(JLabel.LEFT);c.setText(value.toString());}
            if (table.getModel().getValueAt(row, 3) != ""){if (column==3 || column==1){c.setOpaque(true);c.setBackground(new Color(0xffe5f5));}}
            if (table.getModel().getValueAt(row, 4) != ""){if (column==1 || column==4){c.setOpaque(true);c.setBackground(new Color(0xe6ffe6));}}
            if (table.getModel().getValueAt(row, 6).toString() == "1"){if (column==1 || column==3 || column==4){c.setOpaque(true);c.setBackground(Color.PINK);}}          //Bid - розовый   
            if (table.getModel().getValueAt(row, 7).toString() == "1"){if (column==1 || column==3 || column==4){c.setOpaque(true);c.setBackground(new Color(0x5aff5a));}} //Ask - зелёный
        }
        // Выделение объёмов BID
        if (table.getModel().getValueAt(row, 6).toString() == "1" || table.getModel().getValueAt(row, 6).toString() == "2"){
            if(column==2){c.setOpaque(true); c.setBackground(new Color(0xdcffff));} // синий
            if(column==5){c.setOpaque(true); c.setBackground(new Color(0xfff6c6));} // оранжевый
        }
        // Выделение объёмов ASK
        if (table.getModel().getValueAt(row, 7).toString() == "1" || table.getModel().getValueAt(row, 7).toString() == "2"){
            if(column==2){c.setOpaque(true); c.setBackground(new Color(0xfff6c6));} // оранжевый
            if(column==5){c.setOpaque(true); c.setBackground(new Color(0xdcffff));} // синий
        }
        // Bid Vol diag
        if (table.getModel().getValueAt(row, 2) != ""){
            double procBid = 0;
            if (column==2){
                procBid = new Double(table.getModel().getValueAt(row,2).toString());
                c.setText("<html><div style='border: 1em solid #ffffff;width:70;height:0;font-size:10.0pt;text-align:right;padding:1px;'>"+procBid
                   +"<div style='border-right: "+procBid*70/MAX_SUM+" solid red;height:6;border-right-color:red;'></div>"
                   +"</div></html>");     
            }    
        }
        // Ask Vol diag
        if (table.getModel().getValueAt(row, 5) != "") {
            double procAsk = 0;
            if (column==5){
                procAsk = new Double(table.getModel().getValueAt(row,5).toString());
                c.setText("<html><div style='border:0px solid #ffffff;width:70;height:0;font-size:10.0pt;padding:1px;'>"+procAsk
                   +"<div style='height:6;width:"+procAsk*100/MAX_SUM+"%;background-color:green;'></div>"
                   +"</div></html>");
            }    
        }
        //if (isSelected){
        //   String key = table.getModel().getValueAt(row, 2).toString();
        //   table.getModel().getValueAt(row, 7) = "0.01";
        //   context.getConsole().getOut().println(key); 
        //}
        //table.getModel().setValueAt(c.getText(), row, column);     
        return c;
        
     }
     
    }
    // End class DOM
    
}