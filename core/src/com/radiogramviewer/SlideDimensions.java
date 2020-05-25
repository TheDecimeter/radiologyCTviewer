package com.radiogramviewer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;

/**
 * Class to get the dimensions of a slide from a csv ish file
 * This allows people to setup new slide sets without rebuilding the whole project
 * This also allows some slides to not be required.
 */
public class SlideDimensions {
    private ArrayList<Node> dims;

    public SlideDimensions(){
        dims=new ArrayList<Node>(20);
        createDummies();
        fillDims();
    }

    private void fillDims(){
        FileHandle file= Gdx.files.internal("slidedim.txt");
        String text = file.readString();
        String lines[] = text.split("\\r?\\n");
        int lineNum=0;
        for(String line : lines){
            lineNum++;
            if(isComment(line))
                continue;

            String [] num=line.split(",");
            if(num.length!=6)
            {
                MainViewer.println("slidedim.txt: line "+lineNum+" with "+num.length+" elements. "+line, Constants.e);
                continue;
            }

            try {
                int i = Integer.parseInt(num[0].trim());
                int w = Integer.parseInt(num[1].trim());
                int h = Integer.parseInt(num[2].trim());
                int t = Integer.parseInt(num[3].trim());
                boolean monitorClicks= Config.getBoolean(num[4]);
                String ext=num[5].trim();
                if(i<1||i>20)
                {
                    MainViewer.println("slidedim.txt: line "+lineNum+" invalid index "+num[0]+" parsed as "+i+". "+line, Constants.e);
                    continue;
                }
                if(t<1){
                    MainViewer.println("slidedim.txt: line "+lineNum+" invalid total "+num[3]+" parsed as "+t+" must be greater than 0. "+line, Constants.e);
                    continue;
                }
                else if(t>h*w){
                    MainViewer.println("slidedim.txt: line "+lineNum+" invalid total "+num[3]+" parsed as "+t+" is greater than than height("+h+") times width("+w+") = ("+(h*w)+". "+line, Constants.e);
                    continue;
                }

                dims.set(i-1,new Node(i,w,h,t,monitorClicks, ext));
            }
            catch (Exception e){
                MainViewer.println("slidedim.txt: line "+lineNum+" could not parse "+line, Constants.e);
            }
        }
    }

    /**
     * Get the dimensions for an index (based at 0)
     * @param index which set of dimensions to get
     * @return a SlideDimensions.Node which contains width, height, and total slide count
     */
    public Node get(int index){
        return dims.get(index);
    }

    public Iterable<Node> dims(){
        return dims;
    }

    private void createDummies(){
        for(int i=0; i<20; ++i)
            dims.add(new Node(i+1));
    }

    private boolean isComment(String s){
        return s.charAt(0)=='/'||s.charAt(0)=='%'||s.charAt(0)=='#';
    }

    public final class Node{
        public final int height, width, total;
        public final boolean markClicks, necessary;
        public final String file;

        private Node(int index, int width, int height, int total, boolean markClicks, String extension, boolean necessary){
            this.width=width;
            this.height=height;
            this.total=total;
            this.markClicks=markClicks;
            this.file=fileName(index,extension);
            this.necessary=necessary;
        }
        Node(int index){
            this(index,1,1,1, false, ".png",false);
        }

        Node(int index, int width, int height, int total, boolean markClicks, String extension){
            this(index,width,height,total, markClicks, extension,true);
        }

        private String fileName(int index,String ext){
            if(index<10)
                return "slides/file0"+index+ext;
            else
                return "slides/file"+index+ext;
        }
    }
}
