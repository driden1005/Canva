package io.driden.canva.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ImageInfo implements Parcelable {

    private String filePath;

    private int originalW;
    private int originalH;

    private int croppedW;
    private int croppedH;

    private int startX;
    private int startY;

    private int rowNum;
    private int colNum;

    private int tileWidth;
    private int tileHeight;

    private int sampleSize;

    public ImageInfo() {

    }

    protected ImageInfo(Parcel in) {
        filePath = in.readString();
        originalW = in.readInt();
        originalH = in.readInt();
        croppedW = in.readInt();
        croppedH = in.readInt();
        startX = in.readInt();
        startY = in.readInt();
        rowNum = in.readInt();
        colNum = in.readInt();
        tileWidth = in.readInt();
        tileHeight = in.readInt();
        sampleSize = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(filePath);
        dest.writeInt(originalW);
        dest.writeInt(originalH);
        dest.writeInt(croppedW);
        dest.writeInt(croppedH);
        dest.writeInt(startX);
        dest.writeInt(startY);
        dest.writeInt(rowNum);
        dest.writeInt(colNum);
        dest.writeInt(tileWidth);
        dest.writeInt(tileHeight);
        dest.writeInt(sampleSize);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ImageInfo> CREATOR = new Creator<ImageInfo>() {
        @Override
        public ImageInfo createFromParcel(Parcel in) {
            return new ImageInfo(in);
        }

        @Override
        public ImageInfo[] newArray(int size) {
            return new ImageInfo[size];
        }
    };

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getOriginalW() {
        return originalW;
    }

    public void setOriginalW(int originalW) {
        this.originalW = originalW;
    }

    public int getOriginalH() {
        return originalH;
    }

    public void setOriginalH(int originalH) {
        this.originalH = originalH;
    }

    public int getCroppedW() {
        return croppedW;
    }

    public void setCroppedW(int croppedW) {
        this.croppedW = croppedW;
    }

    public int getCroppedH() {
        return croppedH;
    }

    public void setCroppedH(int croppedH) {
        this.croppedH = croppedH;
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public int getColNum() {
        return colNum;
    }

    public void setColNum(int colNum) {
        this.colNum = colNum;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(int sampleSize) {
        this.sampleSize = sampleSize;
    }

    @Override
    public String toString() {
        return String.format("File Path:%s\nWidth:%d, Scaled W:%d\nHeight%d, Scaled H:%d\nStart X Point:%d\nStart Y Point:%d\n" +
                        "Total Rows:%d\nTotal Cols:%d\nTile Width:%d\nTile Height:%d\nSameple Size:%d",
                filePath, originalW, croppedW, originalH, croppedH, startX, startY, rowNum, colNum, tileWidth, tileHeight, sampleSize);
    }

}
