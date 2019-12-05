     package dukascopy.strategies.clients;
     
     import com.dukascopy.api.*;
     import com.dukascopy.api.IIndicators.AppliedPrice;
     import static com.dukascopy.api.IOrder.State.*;
     import com.dukascopy.api.IEngine.OrderCommand;

     /***************************************************************************
     * Пример класса вывода ордеров
     *
     * public void GetSellStop(ITick tick) throws JFException {
     *    double AddStepSell = 0;
     *    double tp = 0;
     *    for(int i = 0; i < MaxOpenOrders; i++){
     *        AddStepSell = (i + 1) * priceStepSell;
     *        double pr = getStepPrice(tick, false, AddStepSell);
     *        if(i == 0)  tp = NormalizeDouble(pr - (TakeProfit * pipValue), pipScale);
     *        submitOrderTask task = new submitOrderTask(orderAmount, pr, "SELL_", OrderCommand.SELLSTOP, StopLoss, tp);
     *        context.executeTask(task);
     *    }  
     * }
     * 
     * Version: v3.0
     * Trade system BTLGrid
     * 
     **************************************************************************/
    // 
    // Synchronous execution orders BUYSTOP
    // 
    private class submitOrderTask implements Callable<IOrder> {
        private OrderCommand orderCmd;
        private final double amount;
        private final double price;
        private final double slPips;
        private final double tpPips;
        private final String label;
        private double sl;
        private double tp;
        
        public submitOrderTask(double amount, double price, String label, OrderCommand orderCmd, double slPips, double tpPips) {
            this.amount = amount;
            this.price = price;
            this.slPips = slPips;
            this.tpPips = tpPips;
            this.label = label;
        }
    
        public IOrder call() throws Exception {
            try 
            {
                if (TrailingStop && TrailingStep != 0) {
                    IOrder order = engine.submitOrder(label, instrumentThis, orderCmd, amount, price, slippage);
                } else {
                    sl = orderCmd.isLong()
                       ? NormalizeDouble(price - (slPips * pipValue), pipScale)
                       : NormalizeDouble(price + (slPips * pipValue), pipScale); 
                    tp = tpPips; 
                    IOrder order = engine.submitOrder(label, instrumentThis, orderCmd, amount, price, slippage, sl, tp);
                }
                if (orderCmd == OrderCommand.BUYSTOP || orderCmd == OrderCommand.PLACE_BID)
                    OrdersBuy.add(order);
                else if(orderCmd == OrderCommand.SELLSTOP || orderCmd == OrderCommand.PLACE_OFFER)
                    OrdersSell.add(order);
            } catch (JFException e) {
                 context.getConsole().getOut().println("Error: "+e.getCause());
            }
            //print("Created BUYSTOP order: " + order.getLabel());
            return order;
        }
    }
