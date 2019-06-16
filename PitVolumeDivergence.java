package jforex;

import com.dukascopy.api.indicators.*;
import java.awt.*;
import com.dukascopy.api.*;

import com.dukascopy.api.IIndicators;
import com.dukascopy.api.indicators.IDrawingIndicator;
import com.dukascopy.api.indicators.IIndicator;
import com.dukascopy.api.indicators.IIndicatorContext;
import com.dukascopy.api.indicators.IIndicatorDrawingSupport;
import com.dukascopy.api.indicators.IndicatorInfo;
import com.dukascopy.api.indicators.IndicatorResult;
import com.dukascopy.api.indicators.InputParameterInfo;
import com.dukascopy.api.indicators.IntegerRangeDescription;
import com.dukascopy.api.indicators.OptInputParameterInfo;
import com.dukascopy.api.indicators.OutputParameterInfo;

import java.awt.Graphics;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.List;
import java.util.Map;
/**
 * Created by: © SpiritFX
 * Review: Sep 18, 2018
 * Version 2.0
 * email: vmm@mail.ru
 */
 
public class PitVolumeDivergence implements IIndicator, IDrawingIndicator {
    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    
    private IBar[] mainChartBars, iBidBars, iAskBars;  
        
    private double[][] outputs = new double[4][];
    private IIndicatorContext context ;
    
                      
    public void onStart(IIndicatorContext context) {
        
        this.context = context ;
        indicatorInfo = new IndicatorInfo("Pit Volumes", "Ask+Bid Volume", "Ask+Bid", false, false, true, 3, 0, 4);
                
        inputParameterInfos = new InputParameterInfo[] {
            new InputParameterInfo("Main", InputParameterInfo.Type.BAR){{                    
            }},            
             new InputParameterInfo("Input data Ask", InputParameterInfo.Type.BAR){{                 
                 setOfferSide(OfferSide.ASK) ;
             }},
             new InputParameterInfo("Input data Bid", InputParameterInfo.Type.BAR)  
             {{                 
                 setOfferSide(OfferSide.BID) ;
             }}           
            };
        
        optInputParameterInfos = new OptInputParameterInfo[] {new OptInputParameterInfo("MA Period", OptInputParameterInfo.Type.OTHER,new IntegerRangeDescription(100, 2, 1000, 1))};
        outputParameterInfos = new OutputParameterInfo[] {
            new OutputParameterInfo("ASK vol", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.HISTOGRAM)
                {{
                     // GREEN UP
                     setOpacityAlpha(1f);
                     setColor(new Color(32,106,6));
                }},
            new OutputParameterInfo("BID vol", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.HISTOGRAM)
                {{
                     // RED UP
                     setOpacityAlpha(0.7f);
                     setColor(new Color(151,8,8));
                     setDrawnByIndicator(true);
                }},    
            new OutputParameterInfo("ASK vol. divergence", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.HISTOGRAM)
                {{
                     // GREEN Down
                     setColor(Color.GREEN);
                }},
            new OutputParameterInfo("Bid vol. divergence", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.HISTOGRAM)
                {{
                     // RED Down
                     setColor(Color.RED);
                }}
                             
        };                        
    }    
    
    
    
    public IndicatorResult calculate(int startIndex, int endIndex) {
        
        double tempv=0, currentAskVolume=0 , currentBidVolume=0;        
        
        if (startIndex - getLookback() < 0) {
            startIndex -= startIndex - getLookback();
        }
                
        if (startIndex > endIndex) {
            return new IndicatorResult(0, 0);
        }
                      
        int i, j;

        for (i = startIndex, j = 0; i <= endIndex; i++, j++) {  
    
            int timeIndex = getTimeIndex(mainChartBars[i].getTime(), iAskBars);            
            int timeIndex2 = getTimeIndex(mainChartBars[i].getTime(), iBidBars);
            
            if(timeIndex == -1 || timeIndex2 == -1)
            {
               outputs[0][j] = Double.NaN; outputs[1][j] = Double.NaN; outputs[2][j] = Double.NaN; outputs[3][j] = Double.NaN;
            } else {
                outputs[0][j] = Double.NaN; outputs[1][j] = Double.NaN; outputs[2][j] = Double.NaN; outputs[3][j] = Double.NaN;
                currentAskVolume = iAskBars[timeIndex].getVolume();                            
                currentBidVolume = iBidBars[timeIndex2].getVolume();
                
                if(outputParameterInfos[0].isShowOutput() && outputParameterInfos[1].isShowOutput()){
                   if(currentAskVolume>=currentBidVolume)
                   {
                       outputs[0][j] = (currentAskVolume) ;
                       outputs[1][j] = (currentBidVolume) ; 
                       outputs[2][j] = (currentAskVolume-currentBidVolume) ;    
                       outputs[3][j] = Double.NaN ;
                   } else {
                       outputs[1][j] = (currentBidVolume) ; 
                       outputs[0][j] = (currentAskVolume) ;
                       outputs[2][j] = Double.NaN ;                
                       outputs[3][j] = - (currentBidVolume-currentAskVolume) ;               
                   }              
                } else if (outputParameterInfos[2].isShowOutput() && outputParameterInfos[3].isShowOutput()) {
                   
                    
                }
            }                                                               
        }
        
        return new IndicatorResult(startIndex, j);
    }

    public IndicatorInfo getIndicatorInfo() {
        return indicatorInfo;
    }

    public InputParameterInfo getInputParameterInfo(int index) {
        if (index <= inputParameterInfos.length) {
            return inputParameterInfos[index];
        }
        return null;
    }

    public int getLookback() {
        return 5 ;
    }

    public int getLookforward() {
        return 0;
    }

    public OptInputParameterInfo getOptInputParameterInfo(int index) {
        if (index <= optInputParameterInfos.length) {
            return optInputParameterInfos[index];
        }
        return null;
    }

    public OutputParameterInfo getOutputParameterInfo(int index) {
        if (index <= outputParameterInfos.length) {
            return outputParameterInfos[index];
        }
        return null;
    }


        
    public void setInputParameter(int index, Object array) {
        switch (index)
        {
            case 0:
                mainChartBars = (IBar[]) array;
                break;                         
            case 1:
                iAskBars = (IBar[]) array;
                break;             
            case 2:
                iBidBars = (IBar[]) array;
                break;               
            default:
                throw new ArrayIndexOutOfBoundsException(" setInputParameter(). Invalid index: "+index);
        }
    }        


    public void setOptInputParameter(int index, Object value) {
        
    }

    public void setOutputParameter(int index, Object array) {
        outputs[index] = (double[]) array;
    }
    
    private void print(String sss)
    {
        context.getConsole().getOut().println(sss) ;
    }
 
    private int getTimeIndex(long time, IBar[] target) {
        if (target == null) {
            return -1;
        }

        int first = 0;
        int upto = target.length;
        
        while (first < upto) {
            int mid = (first + upto) / 2;
            
            IBar data = target[mid];
            
            if (data.getTime() == time) {
                return mid;
            }
            else if (time < data.getTime()) {
                upto = mid;
            } 
            else if (time > data.getTime()) {
                first = mid + 1;
            } 
        }                               
        return -1;
    }
    @Override
    public Point drawOutput(Graphics g, int outputIdx, Object values2, Color color, Stroke stroke,
                           IIndicatorDrawingSupport indicatorDrawingSupport, List<Shape> shapes,
                           Map<Color, List<Point>> handles) {

        int lastX = -1;
        int lastY = -1;

        double[] values = (double[]) values2;
        
        if (values != null) {
            // check for visible candles
            int i, k;
            for (i = indicatorDrawingSupport.getIndexOfFirstCandleOnScreen(), k = i + indicatorDrawingSupport.getNumberOfCandlesOnScreen(); i < k; i++) {
   
                if (values[i] != Double.NaN) {
                    if (outputIdx == 1) { 
                 
                        g.setColor(outputParameterInfos[1].getColor());
                        int x = (int) indicatorDrawingSupport.getMiddleOfCandle(i) - 2;
                        int y = (int) indicatorDrawingSupport.getYForValue(values[i]);
                        int width = 4;
                        int height =  - (int) indicatorDrawingSupport.getYForValue(values[i]) +  (int) indicatorDrawingSupport.getYForValue(0);
                        
                        fillRect(g, x, y, width, height);
                        
                        if (lastX < x + width) {
                            lastX = x + width;
                            lastY = y;
                        }
                    }
                }
            }
        }
        return new Point(lastX, lastY);
    }
    
    private void fillRect(Graphics g, int x, int y, int width, int height){
        if(outputParameterInfos[0].isShowOutput()){
            g.fillRect(x, y, width, height);
        }
        if(outputParameterInfos[1].isShowOutput()){
            g.fillRect(x, y, width, height);
        }
    }
}
