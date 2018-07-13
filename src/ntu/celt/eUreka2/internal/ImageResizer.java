package ntu.celt.eUreka2.internal;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;

public class ImageResizer {
	
	public static boolean generateThumbnail(File imgFile, File toSaveThumbFile, int thumbSize, boolean allowThumbBiggerThanOriginal ){
		try {
			BufferedImage orgImg = ImageIO.read(imgFile);
			int w = orgImg.getWidth();
			int h = orgImg.getHeight();
			int newW;
			int newH;
			if(w>h){
				newW = thumbSize;
				newH = thumbSize * h/w;
				
				if(thumbSize>w){
					if(!allowThumbBiggerThanOriginal){
						//return false;
						newW = w;
						newH = h;
					}
				}
			}
			else{
				newH = thumbSize;
				newW = thumbSize * w/h;
				if(thumbSize>h){
					if(!allowThumbBiggerThanOriginal){
						newW = w;
						newH = h;
					}
				}
			}
			
			boolean preserveAlpha = true;
			
			//gif, jpg, png
			String ex = (FilenameUtils.getExtension(imgFile.getName())).toLowerCase();
			String formatName ;
			if("gif".equals(ex))
				formatName = "GIF";
			else if("png".equals(ex)){
				formatName = "PNG";
				preserveAlpha = false;
			}
			else if("jpg".equals(ex) || "jpeg".equals(ex) || "jpe".equals(ex))
				formatName = "JPG";
			else
				formatName = "JPG";
			
			BufferedImage thImg = createResizedCopy(orgImg, newW, newH, preserveAlpha);
			ImageIO.write(thImg, formatName, toSaveThumbFile);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static BufferedImage createResizedCopy(Image originalImage, 
            int scaledWidth, int scaledHeight, 
            boolean preserveAlpha)
	{
	    int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
	    BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
	    Graphics2D g = scaledBI.createGraphics();
	    if (preserveAlpha) {
	    	g.setComposite(AlphaComposite.Src);
	    }
	    g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null); 
	    g.dispose();
	    return scaledBI;
	}
	
}
