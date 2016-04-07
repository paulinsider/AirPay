package com.air.yanrunfa.palmprint;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.Objdetect;
import org.opencv.utils.Converters;
import org.opencv.video.Video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by YanRunFa on 2015/5/9.
 */
public class Palmprint {
    public static final int BITMAP_LENGTH=1000;
    public static final int ROI_LENGTH=160;
    public static final int FEATURE_LENGTH=40;

    private Bitmap bitmap;
    private Mat rgbMat;
    private Mat skinMat;
    private Mat ROIMat;
    private List<Mat> processdMat;
    //构造函数
    public Palmprint(Bitmap bitmap){
        rgbMat=new Mat();
        this.bitmap=bitmap;
        Utils.bitmapToMat(bitmap, rgbMat);
    }


    public Bitmap getROI() throws IOException{
        rgbMat=bicubicInterpolation(rgbMat, BITMAP_LENGTH);
        skinMat=skinDetection(rgbMat);
        ROIMat=ROIExtractor(skinMat);
        processdMat=preprocessing(ROIMat);


        Mat combine=new Mat();
        for (int i=0;i<4;i++){
            combine.push_back(processdMat.get(i));
        }
        Bitmap bm=Bitmap.createBitmap(combine.width(),combine.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(combine,bm);
        return bm;
    }

    public byte[] extraction()throws IOException{
        rgbMat=bicubicInterpolation(rgbMat, BITMAP_LENGTH);
        skinMat=skinDetection(rgbMat);
        ROIMat=ROIExtractor(skinMat);
        processdMat=preprocessing(ROIMat);

        Vector<byte[]> code=new Vector<>();
        byte[] result=new byte[0];
        code=imageSegmentation(processdMat);

        for(byte[] bt:code){
            byte[] aResult=new byte[result.length+bt.length];
            System.arraycopy(result,0,aResult,0,result.length);
            System.arraycopy(bt,0,aResult,result.length,bt.length);
            result=aResult;
        }
        return result;
    }


    public byte[] extractionWithROI(Bitmap bm){
        Utils.bitmapToMat(bm,rgbMat);
        rgbMat=bicubicInterpolation(rgbMat, BITMAP_LENGTH);
        processdMat=preprocessing(rgbMat);
        Vector<byte[]> code=new Vector<>();
        byte[] result=new byte[0];
        code=imageSegmentation(processdMat);

        for(byte[] bt:code){
            byte[] aResult=new byte[result.length+bt.length];
            System.arraycopy(result,0,aResult,0,result.length);
            System.arraycopy(bt,0,aResult,result.length,bt.length);
            result=aResult;
        }
        return result;
    }

//---------------------------------------------私有处理函数---------------------------------//
    //预处理
    private List<Mat> preprocessing(Mat beforeProcessingMat){
        Mat afterProcessingMat=new Mat();
        Size kSize=new Size(3,3);

        //三次插值缩放到160X160
        afterProcessingMat = bicubicInterpolation(beforeProcessingMat, ROI_LENGTH);

        //灰度化
        Imgproc.cvtColor(afterProcessingMat, afterProcessingMat, Imgproc.COLOR_RGB2GRAY);

        //拉普拉斯锐化
        afterProcessingMat=laplaceOperator(afterProcessingMat);

        //高斯模糊
        Imgproc.GaussianBlur(afterProcessingMat, afterProcessingMat, kSize, 0, 0);

        //小波变换压缩到40X40
        //afterProcessingMat=waveletTransformation(afterProcessingMat,2);
        afterProcessingMat=bicubicInterpolation(afterProcessingMat,FEATURE_LENGTH);

        //sobel算子

        List<Mat> list=sobelOperator(afterProcessingMat);
        return list;
    }

    //将图片分割并逐个二值化得到文理特征
    private Vector<byte[]> imageSegmentation(List<Mat> sobelMat){
        //结果直方图数组
        Vector<byte[]> histograms=new Vector<>();
        //分割四个方向的Sobel边缘图像
        for (int i=0;i<4;i++){
                Mat apart=sobelMat.get(i);
                //将每个方向的图像分为9份
                for (int x=0;x<3;x++){
                    for (int y=0;y<3;y++){
                        Mat smallApart=apart.submat(x * 13, (x + 1) * 13, y*13, (y + 1) * 13);
                        //进行局部二值模式处理并将结果加入到直方图数组中
                        histograms.add(localBinaryPatterns(smallApart));
                    }
                }
                //单独的提取出每个方向的图像的中心部分进行局部二值模式处理
                //图像的中心包含更多的纹理信息，可以显著的增加认证的准确率
                Mat uniqueApart=apart.submat(7,32,7,32);
                histograms.add(localBinaryPatterns(uniqueApart));
            }
        return histograms;
    }

    //标准化的旋转不不安局部二值模式
    private byte[] localBinaryPatterns(Mat bitmap){
        int bmWidth=bitmap.width();
        int bmHeight=bitmap.height();
        byte[] histogram=new byte[9];

        for (int y = 1; y < bmHeight - 1; y++) {
            for (int x = 1; x < bmWidth - 1; x++) {

                int num=0; //1的数量
                int temp=3; //前一个点的值
                int u=0;  //边界的个数

                for (int i=-1;i<2;i++){
                    for (int j=-1;j<2;j++){
                        if (!(i==0&&j==0)){

                            double pixel=bitmap.get(x+i,y+j)[0];
                            if (pixel>bitmap.get(x,y)[0]){
                                num++;
                                //出现跳变，边界+1
                                if (temp==0){
                                    u++;
                                }
                                temp=1;
                            }
                            else {
                                if (temp==1){
                                    u++;
                                }
                                temp=0;
                            }
                        }

                    }

                }
                //检测第一个点与最后一个点之间是否有跳变
                if (temp!=(bitmap.get(x-1,y-1)[0]>bitmap.get(x,y)[0]?0:1)){
                    u++;
                }
                //跳变
                if (u<2){
                    histogram[num]++;
                }
            }
        }
        return histogram;
    }

    private Mat laplaceOperator(Mat srcMat){
        Mat result=new Mat();
        Mat element=new Mat(3,3,CvType.CV_32FC1);
        element.put(1, 1, 9);
        element.put(0,0,-1);
        element.put(0,1,-1);
        element.put(0,2,-1);
        element.put(1,0,-1);
        element.put(1,2,-1);
        element.put(2,0,-1);
        element.put(2,1,-1);
        element.put(2,2,-1);
        Imgproc.filter2D(srcMat,result,srcMat.depth(),element);
        return result;
    }

    //Sobel算子
    private List<Mat> sobelOperator(Mat srcMat){
        List<Mat> list=new ArrayList<>();
        Mat element1=new Mat(3,3,CvType.CV_32FC1);
        element1.put(0,0,-1);
        element1.put(0,1,-2);
        element1.put(0,2,-1);
        element1.put(1,0,0);
        element1.put(1,1,0);
        element1.put(1,2,0);
        element1.put(2,0,1);
        element1.put(2,1,2);
        element1.put(2,2,1);
        Mat dstMat1=new Mat();
        Imgproc.filter2D(srcMat, dstMat1, srcMat.depth(), element1);
        list.add(dstMat1);


        Mat element2=new Mat(3,3,CvType.CV_32FC1);
        element2.put(0, 0, -1);
        element2.put(0, 1, 0);
        element2.put(0,2,1);
        element2.put(1,0,-2);
        element2.put(1,1,0);
        element2.put(1,2,2);
        element2.put(2, 0, -1);
        element2.put(2, 1, 0);
        element2.put(2, 2, 1);
        Mat dstMat2=new Mat();
        Imgproc.filter2D(srcMat, dstMat2, srcMat.depth(), element2);
        list.add(dstMat2);

        Mat element3=new Mat(3,3,CvType.CV_32FC1);
        element3.put(0, 0, 0);
        element3.put(0, 1, 1);
        element3.put(0, 2, 2);
        element3.put(1, 0, -1);
        element3.put(1, 1, 0);
        element3.put(1, 2, 1);
        element3.put(2, 0, -2);
        element3.put(2, 1, -1);
        element3.put(2, 2, 0);
        Mat dstMat3=new Mat();
        Imgproc.filter2D(srcMat, dstMat3, srcMat.depth(), element3);
        list.add(dstMat3);


        Mat element4=new Mat(3,3,CvType.CV_32FC1);
        element4.put(0, 0, -2);
        element4.put(0, 1, -1);
        element4.put(0, 2, 0);
        element4.put(1, 0, -1);
        element4.put(1, 1, 0);
        element4.put(1, 2, 1);
        element4.put(2, 0, 0);
        element4.put(2, 1, 1);
        element4.put(2, 2, 2);
        Mat dstMat4=new Mat();
        Imgproc.filter2D(srcMat, dstMat4, srcMat.depth(), element4);
        list.add(dstMat4);
        return list;
    }


    //双三次插值
    private Mat bicubicInterpolation(Mat beforeResizeMat,int length) {
        Mat afterResizeMat = new Mat();
        Size roiSize = new Size(((float)length/beforeResizeMat.height())*beforeResizeMat.width(), length);
        Imgproc.resize(beforeResizeMat, afterResizeMat, roiSize, 0, 0, Imgproc.INTER_CUBIC);
        return afterResizeMat;
    }

    //小波变换
    private Mat waveletTransformation(Mat beforeTransMat,int nLayer){

        int i, x, y, n;
        double fValue=0;
        double fRadius=Math.sqrt(2.0);
        int nWidth=beforeTransMat.width();
        int nHeight=beforeTransMat.height();
        int nHalfW=nWidth / 2;
        int nHalfH=nHeight / 2;
        double[][] pData  = new double[beforeTransMat.height()][beforeTransMat.width()];
        double[]  pRow     = new double[beforeTransMat.width()];
        double[]  pColumn  = new double[beforeTransMat.height()];
        for (i = 0; i < beforeTransMat.height(); i++) {
            for (int j=0;j<beforeTransMat.width();j++){
                pData[i][j]=beforeTransMat.get(i,j)[0];
            }
        }
        // 多层小波变换
        for (n = 0; n < nLayer; n++, nWidth /= 2, nHeight /= 2, nHalfW /= 2, nHalfH /= 2) {
            // 水平变换
            for (y = 0; y < nHeight; y++) {// 奇偶分离
                pRow=pData[y];
                for (i = 0; i < nHalfW; i++) {
                    x = i * 2;
                    pData[y][i] = pRow[x];
                    pData[y][nHalfW + i] = pRow[x + 1];
                }
                // 提升小波变换
                for (i = 0; i < nHalfW - 1; i++) {
                    fValue = (pData[y][i] + pData[y][i + 1]) / 2;
                    pData[y][nHalfW + i] -= fValue;
                }
                fValue = (pData[y][nHalfW - 1] + pData[y][nHalfW - 2]) / 2;
                pData[y][nWidth - 1] -= fValue;
                fValue = (pData[y][nHalfW] + pData[y][nHalfW + 1]) / 4;
                pData[y][0] += fValue;
                for (i = 1; i < nHalfW; i++) {
                    fValue = (pData[y][nHalfW + i] + pData[y][nHalfW + i - 1]) / 4;
                    pData[y][i] += fValue;
                }
                // 频带系数
                for (i = 0; i < nHalfW; i++) {
                    pData[y][i] *= fRadius;
                    pData[y][nHalfW + i] /= fRadius;
                }
            }
            // 垂直变换
            for (x = 0; x < nWidth; x++)
            {
                // 奇偶分离
                for (i = 0; i < nHalfH; i++) {
                    y = i * 2;
                    pColumn[i] = pData[y][x];
                    pColumn[nHalfH + i] = pData[y + 1][x];
                }
                for (i = 0; i < nHeight; i++) {
                    pData[i][x] = pColumn[i];
                }
                // 提升小波变换
                for (i = 0; i < nHalfH - 1; i++) {
                    fValue = (pData[i][x] + pData[i + 1][x]) / 2;
                    pData[nHalfH + i][x] -= fValue;
                }
                fValue = (pData[nHalfH - 1][x] + pData[nHalfH - 2][x]) / 2;
                pData[nHeight - 1][x] -= fValue;
                fValue = (pData[nHalfH][x] + pData[nHalfH + 1][x]) / 4;
                pData[0][x] += fValue;
                for (i = 1; i < nHalfH; i++) {
                    fValue = (pData[nHalfH + i][x] + pData[nHalfH + i - 1][x]) / 4;
                    pData[i][x] += fValue;
                }
                // 频带系数
                for (i = 0; i < nHalfH; i++) {
                    pData[i][x] *= fRadius;
                    pData[nHalfH + i][x] /= fRadius;
                }
            }
        }
        Mat afterTransMat=new Mat(nHeight,nWidth,CvType.CV_8UC1);
        for (int x1=0;x1<nWidth;x1++){
            for (int y1=0;y1<nHeight;y1++){
                afterTransMat.put(y1,x1,pData[y1][x1]);
            }
        }
        Bitmap bm=Bitmap.createBitmap(afterTransMat.width(),afterTransMat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(afterTransMat,bm);
        return afterTransMat;
    }




    //皮肤检测
    private Mat skinDetection(Mat beforeDetectionMat) throws IOException{
        Log.v("status","Skin detection begin");
        int height=beforeDetectionMat.height();
        int width=beforeDetectionMat.width();
        int rows=beforeDetectionMat.rows();
        int cols=beforeDetectionMat.cols();
        Mat dstMat=new Mat();
        Mat ycrcb=new Mat();

        //将RGB通道图像转换为YCrCb图像
        Imgproc.cvtColor(beforeDetectionMat, ycrcb, Imgproc.COLOR_BGR2YCrCb, 3);
        //提取图片中心80X80个像素的子矩阵
        Mat center=ycrcb.submat(rows/2-40,rows/2+40,cols/2-40,cols/2+60);
        List<Mat> mv1=new ArrayList<Mat>();
        //分割子矩阵图像的通道
        Core.split(center, mv1);

        //找到各个通道的平均值
        Scalar averageColor=Core.mean(ycrcb);
        double averageCr=averageColor.val[1];
        double averageCb=averageColor.val[2];

        //粗略判断得到的子矩阵是否处于皮肤区域，否则抛出异常
        if (averageCr<120||averageCr>180){
            //if (averageCb<70||averageCb>130){
                Log.v("error","Hand Color Error");
                throw new IOException("Wrong Hand");
            //}
        }
        Log.v("status","Hand Detected");



        //找到各个通道的最大值与最小值
        Core.MinMaxLocResult minMaxY=Core.minMaxLoc(mv1.get(0));
        Core.MinMaxLocResult minMaxCr=Core.minMaxLoc(mv1.get(1));
        Core.MinMaxLocResult minMaxCb=Core.minMaxLoc(mv1.get(2));



        //设定个通道颜色的最小值和最大值
        Scalar lowerb=new Scalar(minMaxY.minVal-60,minMaxCr.minVal-5,minMaxCb.minVal-5);
        Scalar upperb=new Scalar(minMaxY.maxVal+60,minMaxCr.maxVal+5,minMaxCb.maxVal+5);
        Size kSize=new Size(9,9);
        //高斯滤波平滑图像
        Imgproc.GaussianBlur(ycrcb, ycrcb, kSize, 1);
        //将颜色在限定值以内的像素置1，不在限定值内的置0，生成手掌的二值图
        Core.inRange(ycrcb, lowerb, upperb, dstMat);

        //形态学闭运算，用来填充物体内细小空洞、连接邻近物体、平滑其边界的同时并不明显改变其面积。
        Mat operator=new Mat(10,10,dstMat.type(),new Scalar(1));
        Imgproc.morphologyEx(dstMat, dstMat, Imgproc.MORPH_CLOSE, operator);

        Log.v("status", "Skin detection finished");
        return dstMat;
    }

    //ROI检测
    private Mat ROIExtractor(Mat beforeExtractMat) throws IOException{
        Log.v("status", "ROI Extraction begin");
        int rows=beforeExtractMat.rows();
        int cols=beforeExtractMat.cols();
        Mat dstMat=new Mat();
        MatOfPoint contour=new MatOfPoint();

        //将原图取反，为了更好的进行角点检测
        Core.bitwise_not(beforeExtractMat, dstMat);

        //设定角点检测的区域
        Mat center=new Mat(rows,cols,CvType.CV_8UC1,new Scalar(Color.BLACK));
        Imgproc.rectangle(center,
                new Point(center.width() / 8, center.height() / 4),
                new Point(center.width() - center.width() / 3, center.height() / 2),
                new Scalar(255, 255, 255),
                -1
        );

        //进行Shi-Tomasi角点检测
        Imgproc.goodFeaturesToTrack(dstMat, contour, 3, 0.1, 50, center, 10, false, 0.04);

        //如果检测到的点小于三个，抛出错误
        if (contour.toList().size()<3){
            Log.v("error","handPoint not Enough");
            throw new IOException("Wrong Point");
        }
        Log.v("status","handPoint Enough");

        //去掉三个手谷点中中间的点
        Point[] points=contour.toArray();
        int minX=0,maxX=0;
        for (int i=1;i<3;i++){
            minX=points[i].x<points[minX].x?i:minX;
            maxX=points[i].x>points[maxX].x?i:maxX;
        }
        Point handPoint1=points[minX];
        Point handPoint2=points[maxX];
        Log.v("status", "Get HandPoint Succeed");


        //Imgproc.circle(dstMat, handPoint1, 10, new Scalar(255, 255, 0), -1);
        //Imgproc.circle(dstMat, handPoint2, 10, new Scalar(255, 255, 0), -1);

        //计算两个手谷点之间的距离和角度
        double d = Math.sqrt((handPoint2.x - handPoint1.x) * (handPoint2.x - handPoint1.x) + (handPoint2.y - handPoint1.y) * (handPoint2.y - handPoint1.y)); //p1、p2两点之间的距离
        float k = Math.abs((float) (handPoint2.y - handPoint1.y) / (float) (handPoint2.x - handPoint1.x));   //斜率
        double angle = Math.atan(k);
        if (d<(center.width()-center.width()/4)/2){
            Log.v("error","ROI Length Error");
            throw new IOException("Wrong ROI");
        }




        //根据坐标关系旋转图像并最终截取ROI
        Mat rotateMat;
        if (handPoint2.x>handPoint1.x){
            rotateMat=Imgproc.getRotationMatrix2D(handPoint1,-Math.toDegrees(angle),1);//旋转矩阵
            Imgproc.warpAffine(dstMat,dstMat,rotateMat,dstMat.size());//应用旋转矩阵
            dstMat=rgbMat.submat((int) handPoint1.y, (int) (handPoint1.y + d), (int) handPoint1.x, (int) (handPoint1.x + d));
        }
        else {
            rotateMat=Imgproc.getRotationMatrix2D(handPoint2,(float)Math.toDegrees(angle)-90,1);
            Imgproc.warpAffine(dstMat,dstMat,rotateMat,dstMat.size());
            dstMat=rgbMat.submat((int) handPoint2.y, (int) (handPoint2.y + d), (int) handPoint2.x, (int) (handPoint2.x + d));
        }

        Mat ycrcb=new Mat();
        //将RGB通道图像转换为YCrCb图像
        Imgproc.cvtColor(dstMat, ycrcb, Imgproc.COLOR_BGR2YCrCb, 3);
        Scalar averageColor=Core.mean(dstMat);
        double averageCr=averageColor.val[1];
        double averageCb=averageColor.val[2];
        //粗略判断得到的子矩阵是否处于皮肤区域，否则抛出异常
        if (averageCr<120||averageCr>180){
           // if (averageCb<70||averageCb>130){
                Log.v("error","ROI Color Error");
                throw new IOException("Wrong Hand");
           // }
        }
        Log.v("status","Hand Detected");

        return dstMat;
    }




}
