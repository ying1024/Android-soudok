#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/ml.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <string>
#include <vector>
#include <map>
#include <algorithm>
#include <numeric>
#include <iostream>

using namespace cv;
using namespace std;
using namespace cv::ml;

//extern "C" JNIEXPORT jstring JNICALL
//Java_com_example_solvingsudoku_MainActivity_stringFromJNI(
//        JNIEnv* env,
//        jobject /* this */) {
//    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
//}

//矩阵y排序依据
bool compy(Rect &a,Rect &b){
    return a.y < b.y;
}
//矩阵按x排序依据
bool compx(Rect &a,Rect &b){
    return a.x < b.x;
}

//矩阵数独求解
bool isPlace(int count,vector<vector<int> > &map) {
    int row = count / 9;
    int col = count % 9;
    int j;
    //同一行
    for (j = 0; j < 9; ++j) {
        if (map[row][j] == map[row][col] && j != col) {
            return false;
        }
    }
    //同一列
    for (j = 0; j < 9; ++j) {
        if (map[j][col] == map[row][col] && j != row) {
            return false;
        }
    }
    //同一小格游戏
    int tempRow = row / 3 * 3;
    int tempCol = col / 3 * 3;
    for (j = tempRow; j < tempRow + 3; ++j) {
        for (int k = tempCol; k < tempCol + 3; ++k) {
            if (map[j][k] == map[row][col] && j != row && k != col) {
                return false;
            }
        }
    }
    return true;
}

void backtrace(int count, vector<vector<int> > &map, vector<vector<int> > &res) {
    if (count == 81) {
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                res[i].push_back(map[i][j]);
            }
        }
        return;
    }
    int row = count / 9;
    int col = count % 9;
    if (map[row][col] == 0) {
        for (int i = 1; i <= 9; ++i) {
            map[row][col] = i;//赋值
            if (isPlace(count, map)) {//可以放
                backtrace(count + 1, map,res);//进入下一层
            }
        }
        map[row][col] = 0;//回溯
    }
    else {
        backtrace(count + 1,map,res);
    }
}


extern "C"
JNIEXPORT jintArray JNICALL
Java_com_example_solvingsudoku_MainActivity_Solvmain(JNIEnv *env, jobject thiz, jintArray pixels,
                                                     jint w, jint h, jintArray tpixels, jint tw,jint th) {
    // TODO: implement Solvmain()
    //数独图
    jint *cbuf;
    jboolean ptfalse = false;
    cbuf = env->GetIntArrayElements(pixels, &ptfalse);
    if(cbuf == NULL){
        return 0;
    }

    Mat imData(h, w, CV_8UC4, (unsigned char*)cbuf);
    Mat imTest;

    //训练集图
    jint *tcbuf;
    jboolean tptfalse = false;
    tcbuf = env->GetIntArrayElements(tpixels,&tptfalse);
    if(tcbuf == NULL){
        return 0;
    }
    Mat imTrain(th,tw,CV_8UC4,(unsigned char*)tcbuf);

//灰度化
    cvtColor(imData, imTest, CV_BGRA2GRAY);
//    Canny(imData, imData, 45, 75);
//二值化
    threshold(imTest,imTest,150,255,THRESH_BINARY_INV);

    //腐蚀膨胀
    Mat erodeElement = getStructuringElement(MORPH_CROSS,Size(1,1));
    Mat dilateElement = getStructuringElement(MORPH_CROSS,Size(3,3));
    erode(imTest,imTest,erodeElement);
    dilate(imTest,imTest,dilateElement);

    //轮廓检测
    vector< vector <Point> > contours;
    vector< Vec4i> hierarchy;
    findContours(imTest,contours,hierarchy,RETR_TREE,CHAIN_APPROX_SIMPLE);
    cvtColor(imTest, imTest, CV_GRAY2BGRA);

    vector< vector <Point> > num_contours;
    vector<Rect> RecOfNum;
    Rect tmp;
    vector<pair<Point,Rect> > IndexAndPos;



    auto Round = boundingRect(contours[0]);

    int index_x;
    int index_y;
    Point index_xy;
    vector<Point> PosOfNum_true;

    vector<Mat> Numbers;
    vector<Point> PosOfNum;
    vector<Point> UpLeftPostOfNum;

    vector<Rect> Pos;


    for(int i=0;i<hierarchy.size();i++){
        if(hierarchy[i][3] == 0&&hierarchy[i][2]!=-1){
            num_contours.push_back(contours[hierarchy[i][2]]);

            tmp = boundingRect(contours[hierarchy[i][2]]);
            UpLeftPostOfNum.push_back(Point(tmp.x-tmp.width/5*2,tmp.y+tmp.height));
            index_x = double(tmp.x+tmp.width/2)/Round.width * 9;
            index_y = double(tmp.y + tmp.height /2) /Round.height * 9;
            index_xy.x = index_x;
            index_xy.y = index_y;

            IndexAndPos.push_back(make_pair(index_xy,tmp));

            Mat ImgROI;
            resize(imTest(tmp),ImgROI,Size(20,20));
            Numbers.push_back(ImgROI);

            PosOfNum.push_back(index_xy);
            RecOfNum.push_back(tmp);
        }
    }


    for(int i = 0;i<hierarchy.size(); i++){
        Rect tmp;
        if(hierarchy[i][3] == 0){
            Pos.push_back(boundingRect(contours[i]));
        }
    }


    sort(Pos.begin(),Pos.end(), compy);

    vector<vector < Rect > > PosMat(9,vector<Rect>());
    int  index =0;
    for(int i=0;i<81;i+=9){
        PosMat[index++].assign(Pos.begin()+i,Pos.begin() + i + 9);
    }

    for(int i=0;i<9;i++){
        sort(PosMat[i].begin(),PosMat[i].end(),compx);
    }

    Mat TestData;
    for(auto nums:Numbers){
        TestData.push_back(nums.reshape(1,1));
    }
    TestData.convertTo(TestData,CV_32F);


    //制作训练集
    Mat tmpimt;
    vector<vector<Mat> > TrainImgMat(10,vector<Mat>());
    vector<vector<Point> > cons;
    vector<Vec4i> hies;
    vector<Rect> trainRect;

    imTrain.copyTo(tmpimt);
    //灰度化
    cvtColor(tmpimt,tmpimt,CV_BGRA2GRAY);
    //二值化
    threshold(tmpimt,tmpimt,150,255,THRESH_BINARY_INV);
    //腐蚀膨胀
    Mat tdilateElement = getStructuringElement(MORPH_CROSS,Size(1,1));
    erode(tmpimt,tmpimt,tdilateElement);
    dilate(tmpimt,tmpimt,tdilateElement);
    //轮廓查找
    findContours(tmpimt,cons,hies,RETR_TREE,CHAIN_APPROX_SIMPLE);

    cvtColor(tmpimt, tmpimt, CV_GRAY2BGRA);

    for(int i = 0;i<hies.size();i++){
        if(hies[i][3] == -1){
            trainRect.push_back(boundingRect(cons[i]));
//            Rect trect = boundingRect(cons[i]);
//            rectangle(tmpimt,trect,Scalar(255,0,255));
        }

    }

    for(int i=9;i>-1;i--){
        for(int j=(9-i)*10;j<(9-i)*10+10;j++){
            Mat img20;

            resize(tmpimt(trainRect[j]),img20,Size(20,20));
            TrainImgMat[i].push_back(img20);
        }
    }

    cons.clear();
    hies.clear();
    trainRect.clear();


    //绑定标签
    Mat trainData;
    Mat Label;

    for (int i = 0; i < 10; i++) {
        for(int j = 0;j<10; j++){
            trainData.push_back(TrainImgMat[i][j].reshape(1,1));
            Label.push_back(i);
        }
    }

    trainData.convertTo(trainData,CV_32F);

    //knn分类器
//    Ptr<TrainData> tData = TrainData::create(trainData,ROW_SAMPLE,Label);

    Ptr<ml::KNearest> knn = ml::KNearest::create();
    knn->setDefaultK(4);
    knn->setIsClassifier(true);
    knn->train(trainData,cv::ml::ROW_SAMPLE,Label);

    //预测测试集
    vector<int> PredicrRes;
    for (int i = 0; i < TestData.rows; i++) {
        Mat tmp = TestData.row(i);
        int response = knn->predict(tmp);
        PredicrRes.push_back(response);
    }


    //构造数独矩阵
        vector<vector<int> > SudoukuMat(9,vector<int>());
        for(int i = 0;i<9;i++){
            for(int j=0;j<9;j++){
                SudoukuMat[i].push_back(0);
            }
        }
    for (int i = 0; i < PredicrRes.size(); i++) {
        SudoukuMat[PosOfNum[i].y][PosOfNum[i].x] = PredicrRes[i];
    }

    for (int i = 0; i < PredicrRes.size(); i++) {
        stringstream ss;
        ss << PredicrRes[i];
        string text = ss.str();
        putText(imData,text,UpLeftPostOfNum[i],FONT_ITALIC,0.5,Scalar(255,0,0),1);
    }

    vector<vector<int> > res(9,vector<int>());
    backtrace(0,SudoukuMat,res);
    for (int i = 0; i < 9; i++) {
        for(int j=0;j<9;j++){
            stringstream ss;
            ss << res[i][j];
            string text = ss.str();
            if(SudoukuMat[i][j]==0){
                putText(imData,text,Point(PosMat[i][j].x+PosMat[i][j].width/3,PosMat[i][j].y+PosMat[i][j].height/3*2),FONT_ITALIC,1,Scalar(255,0,0),2);
            }
        }
    }

    int size = w * h;

    jintArray result = env->NewIntArray(size);

    env->SetIntArrayRegion(result, 0, size, (jint*)imData.data);
    env->ReleaseIntArrayElements(pixels, cbuf, 0);
    env->ReleaseIntArrayElements(tpixels, tcbuf, 0);

    contours.clear();
    hierarchy.clear();
    return result;

}


extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_solvingsudoku_MainActivity_Vali(JNIEnv *env, jobject thiz, jintArray pixels,
                                                 jint w, jint h) {
    // TODO: implement Vali()
    jint *cbuf;
    jboolean ptfalse = false;
    cbuf = env->GetIntArrayElements(pixels, &ptfalse);
    if(cbuf == NULL){
        return 0;
    }

    Mat imData(h, w, CV_8UC4, (unsigned char*)cbuf);
    Mat imTest;

    //灰度化
    cvtColor(imData, imTest, CV_BGRA2GRAY);
//    Canny(imData, imData, 45, 75);
//二值化
    threshold(imTest,imTest,150,255,THRESH_BINARY_INV);

    //腐蚀膨胀
    Mat erodeElement = getStructuringElement(MORPH_CROSS,Size(1,1));
    Mat dilateElement = getStructuringElement(MORPH_CROSS,Size(3,3));
    erode(imTest,imTest,erodeElement);
    dilate(imTest,imTest,dilateElement);

    //轮廓检测
    vector< vector <Point> > contours;
    vector< Vec4i> hierarchy;
    vector<Rect> pos;
    vector<Rect> num_con;
    findContours(imTest,contours,hierarchy,RETR_TREE,CHAIN_APPROX_SIMPLE);
    cvtColor(imTest, imTest, CV_GRAY2BGRA);

    for (int i = 0; i < hierarchy.size(); ++i) {
        if(hierarchy[i][3]==0){
            pos.push_back(boundingRect(contours[i]));
            if(hierarchy[i][2]!=-1){
                num_con.push_back(boundingRect(contours[i]));
            }
        }
    }
    if(pos.size() > 80 && num_con.size() > 0){
        return true;
    } else{
        return false;
    }

}