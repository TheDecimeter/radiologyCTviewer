package com.radiogramviewer.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.radiogramviewer.MainViewer;
import com.radiogramviewer.relay.Constants;
import com.radiogramviewer.relay.P;

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
            if(Config.isComment(line))
                continue;

            String [] num=line.split(",");
            if(num.length!=7)
            {
                P.e("slidedim.txt: line "+lineNum+" with "+num.length+" elements. "+line);
                continue;
            }

            try {
                int i = Integer.parseInt(num[0].trim());
                int f = Integer.parseInt(num[1].trim());
                int w = Integer.parseInt(num[2].trim());
                int h = Integer.parseInt(num[3].trim());
                int t = Integer.parseInt(num[4].trim());
                boolean monitorClicks= Config.getBoolean(num[5]);
                String ext=num[6].trim();
                if(i<1||i>20)
                {
                    P.e("slidedim.txt: line "+lineNum+" invalid index "+num[0]+" parsed as "+i+". "+line);
                    continue;
                }
                if(f<1||f>20)
                {
                    P.e("slidedim.txt: line "+lineNum+" invalid filenum "+num[1]+" parsed as "+f+". "+line);
                    continue;
                }
                if(t<1){
                    P.e("slidedim.txt: line "+lineNum+" invalid total "+num[3]+" parsed as "+t+" must be greater than 0. "+line);
                    continue;
                }
                else if(t>h*w){
                    P.e("slidedim.txt: line "+lineNum+" invalid total "+num[3]+" parsed as "+t+" is greater than than height("+h+") times width("+w+") = ("+(h*w)+". "+line);
                    continue;
                }

                dims.set(i-1,new Node(i,f,w,h,t,monitorClicks, ext));
            }
            catch (Exception e){
                P.e("slidedim.txt: line "+lineNum+" could not parse "+line);
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



    public final class Node{
        public final int height, width, total;
        public final boolean markClicks, necessary;
        public final String file;

        private Node(int index, int fileNum, int width, int height, int total, boolean markClicks, String extension, boolean necessary){
            this.width=width;
            this.height=height;
            this.total=total;
            this.markClicks=markClicks;
            this.file=fileName(fileNum,extension);
            this.necessary=necessary;
        }
        Node(int index){
            this(index,index,1,1,1, false, ".png",false);
        }

        Node(int index, int fileNum, int width, int height, int total, boolean markClicks, String extension){
            this(index,fileNum,width,height,total, markClicks, extension,true);
        }

        private String fileName(int index,String ext){
            if(index<10)
                return "slides/file0"+index+ext;
            else
                return "slides/file"+index+ext;
        }
    }
}
