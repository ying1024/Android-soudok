package com.example.solvingsudoku;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class Sudoku extends BaseObservable {
    private SudokuCeil[] sudokuData = new SudokuCeil[81];
    private Stack<Integer> modifyStack = new Stack();
    private int focusLocation;
    private int filledNum;

    @Bindable
    public int getFocusLocation() {
        return focusLocation;
    }

    public void setFocusLocation(int focusLocation) {
        this.focusLocation = focusLocation;
        notifyChange();
    }

    public Sudoku(){
        initSudoku();
        createHardestSudoku();
    }

    public void initSudoku(){
        focusLocation = -1;
        filledNum = 0;
        for (int i=0;i<81;i++){
            if(sudokuData[i] == null){
                sudokuData[i] = new SudokuCeil();
            }
            sudokuData[i].value = 0;
            sudokuData[i].fixedFlag = false;
        }
    }

    public void clear(){
        initSudoku();
        notifyChange();
    }

    public void createHardestSudoku(){
        int position[] = {2,3,9,16,19,22,24,27,32,33,37,40,44,47,48,52,55,57,62,65,70,77,78};
        int value[] = {5,3,8,2,7,1,5,4,5,3,1,7,6,3,2,8,6,5,9,4,3,9,7};
        for (int i=0;i<position.length;i++) {
            sudokuData[position[i]].value = value[i];
            sudokuData[position[i]].fixedFlag = true;
        }
        filledNum = 17;
    }

    public Sudoku(int difficulty){

        //生成随机矩阵
        //挖空，检查唯一性
    }

    /**
     * 只把生成的数字当做初始数独
     * @return
     */
    public boolean SudokuAnswer(){  //查看答案
        modifyStack.clear();
        outeach: for (int i=0;i<81;){
            if( ! isFixed(i)){      //如果能修改
                if(sudokuData[i].value == 0)
                    sudokuData[i].value = 1;
                while (seachInRow(i) || seachInColumn(i) || seachInScratchableLatex(i)){ //如果找到相同的数，直到找不到相同的数
                    sudokuData[i].value ++;
                    while (sudokuData[i].value > 9){  //如果它错了，推出上一个位置, 直到返回一个没错的位置
                        sudokuData[i].value = 0;
                        if(modifyStack.empty()){
                            Log.i("无解","不能弹出");
                            return false;
                        }else {
                            i = modifyStack.pop();          //有无解可能
                        }

                        sudokuData[i].value++;
                    }
                    continue outeach;
                }
                modifyStack.push(i); //压入修改过的位置
            }
            i++;
        }
        filledNum = 81;
        notifyChange();
        return true;
    }

    /**
     * 计算数独 , 把屏幕上所有的数字当做初始数独
     * @return boolean 是否计算出答案
     */
    public boolean calSudoku() throws CloneNotSupportedException {
        for(int i=0;i<81;i++){
            if(seachInRow(i) || seachInColumn(i) || seachInScratchableLatex(i)){
                return false;
            }
        }
        SudokuCeil[] sudokuDataBackUp = new SudokuCeil[81];
        for (int i=0;i<81;i++){
            sudokuDataBackUp[i] = sudokuData[i].clone();
            if(sudokuData[i].value != 0){
                sudokuData[i].fixedFlag = true;
            }
        }
        Boolean result = SudokuAnswer();
        for (int i=0;i<81;i++){
            if( ! result){//如果无法解出
                sudokuData[i] = sudokuDataBackUp[i].clone();
            }else {
                sudokuData[i].fixedFlag = sudokuDataBackUp[i].fixedFlag;
            }
        }
        notifyChange();
        return result;
    }

    /**
     *
     */
    public boolean validate(){
        if(filledNum != 81)
            return false;
        for(int i=0;i<81;i++){
            if(seachInRow(i) || seachInColumn(i) || seachInScratchableLatex(i)){
                return false;
            }
        }
        return true;
    }
    /**
     * 根据数独规则在随机位置上创建十几个随机数字
     */
    public void createRandomSudokuCeil(){
        initSudoku();
        Set<Integer> allCandidateNum = new HashSet<Integer>();
        Set<Integer> filledNumSet = new HashSet<Integer>();
        for (int i =0;i < 20;i++){
            int randomIndex = (int) (Math.random()*81);
            if( ! isFixed(randomIndex)){    //如果还没有填
                for (int j=0;j<9;j++){
                    allCandidateNum.add(j+1);
                }
                filledNumSet.clear();
                Log.i("生成的下标：",String.valueOf(randomIndex));
                seachInRow(randomIndex,filledNumSet);
                seachInColumn(randomIndex,filledNumSet);
                seachInScratchableLatex(randomIndex,filledNumSet);
                allCandidateNum.removeAll(filledNumSet);
                int randomOffest =(int) (Math.random()*allCandidateNum.size());
                Object[] acdnArray = allCandidateNum.toArray();
                sudokuData[randomIndex].value = (int) acdnArray[randomOffest];
                sudokuData[randomIndex].fixedFlag = true;
            }
        }
    }

    /**
     * 创建一个数独
     */
    public void createRandomSudoku(){
        createRandomSudokuCeil();
        while ( ! SudokuAnswer()){
            createRandomSudokuCeil();
        }
        for (int i=0;i<15*4;i++){
            int RandomIndex = (int) (Math.random() * 81);
            Log.i("固定下标",String.valueOf(RandomIndex));
            sudokuData[RandomIndex].fixedFlag = true;
        }
        filledNum = 81;
        for(int i=0;i<81;i++){
            if( ! isFixed(i)){
                sudokuData[i].value = 0;
                filledNum --;
            }
        }

        notifyChange();
    }

    /**
     * 查找同一行中是否有相同的数字
     * @param index
     * @return
     */
    private boolean seachInRow(int index){
        int startNum = (index / 9) * 9; //计算此行的起始下标
        int SudokuCeilNum = getSudokuData(index);  //得到格子中的数值
        if(SudokuCeilNum != 0){
            for(int i = startNum;i<startNum+9;i++){
                if(SudokuCeilNum == sudokuData[i].value && i != index){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取同一行中已经有的数字
     * @param index
     * @param rowFilledNumSet
     * @return
     */
    private Set<Integer> seachInRow(int index ,Set<Integer> rowFilledNumSet){
        int startNum = (index / 9) * 9; //计算此行的起始下标
        for(int i = startNum;i<startNum+9;i++){
            if(sudokuData[i].value != 0 && i != index){
                rowFilledNumSet.add(sudokuData[i].value);
            }
        }
        return rowFilledNumSet;
    }

    /**
     * 查找同一列中是否有相同的数字
     * @param index
     * @return
     */
    private boolean seachInColumn(int index){
        int offest = index % 9; //计算所在列,偏移
        int sudokuCeilNum = getSudokuData(index);
        if(sudokuCeilNum != 0)
            for(int i=0;i<9;i++){
                int otherIndex = i*9+offest;
                if(sudokuCeilNum == sudokuData[otherIndex].value && otherIndex != index){
                    return true;
                }
            }
        return false;
    }

    /***
     * 获取同一列中已经有的数字
     * @param index
     * @param columnFilledNumSet
     * @return
     */
    private Set<Integer> seachInColumn(int index,Set<Integer> columnFilledNumSet){
        int offest = index % 9; //计算所在列,偏移
        for(int i=0;i<9;i++){
            int otherIndex = i*9+offest;
            if(0 != sudokuData[otherIndex].value && otherIndex != index){
                columnFilledNumSet.add(sudokuData[otherIndex].value);
            }
        }
        return columnFilledNumSet;
    }

    /**
     * 查找同一九宫中是否有相同的数字
     * @param index
     * @return
     */
    private boolean seachInScratchableLatex(int index){
        int slRowOffest = index / 9/3*3;  //计算九宫格左上角元素的行偏移
        int slColOffest = index % 9/3*3;    //计算九宫格左上角元素的列偏移
        if(sudokuData[index].value !=0 )
            for(int i=0;i<3;i++){
                for (int j = 0;j<3;j++){
                    int otherIndex = (slRowOffest + i) * 9 + slColOffest + j;
                    if(sudokuData[index].value == sudokuData[otherIndex].value && index != otherIndex){
                        return true;
                    }
                }
            }
        return  false;
    }

    /**
     * 获取同一九宫中已经有的数字
     * @param index
     * @param slFilledNumSet
     * @return
     */
    private Set<Integer> seachInScratchableLatex(int index,Set<Integer> slFilledNumSet){
        int slRowOffest = index / 9/3*3;  //计算九宫格左上角元素的行偏移
        int slColOffest = index % 9/3*3;    //计算九宫格左上角元素的列偏移
        for(int i=0;i<3;i++){
            for (int j = 0;j<3;j++){
                int otherIndex = (slRowOffest + i) * 9 + slColOffest + j;
                if(0 != sudokuData[otherIndex].value && index != otherIndex){
                    slFilledNumSet.add(sudokuData[otherIndex].value);
                }
            }
        }
        return slFilledNumSet;
    }

    private void moidfyStackPush(int index){
        modifyStack.push(index);
    }

    private int modifyStackPop(){
        return modifyStack.pop();
    }


    public void setSudokuData(int index,int value){
        if(value == 0 && sudokuData[index].value != 0){
            filledNum --;
        }else if(value != 0 && sudokuData[index].value == 0){
            filledNum ++;
        }
        sudokuData[index].value = value;
        Log.i("已经填了：",String.valueOf(getFilledNum()));
        notifyChange();
    }

    public int getSudokuData(int index){
        return sudokuData[index].value;
    }

    public void onSudokuCeilClick(int index){
        setFocusLocation(index);
    }

    /**
     * 获取是否有位置被选中
     * @return
     */
    public boolean isFocus(){
        return focusLocation != -1 ? true :false;
    }

    /**
     * 获取index 位置中是否可修改
     * @param index
     * @return
     */
    public boolean isFixed(int index){
        return sudokuData[index].fixedFlag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sudoku)) return false;
        Sudoku sudoku = (Sudoku) o;
        return Arrays.equals(sudokuData, sudoku.sudokuData);
    }

    @NonNull
    @Override
    protected Sudoku clone() throws CloneNotSupportedException {
        Sudoku sudoku = new Sudoku();
        System.arraycopy(this.sudokuData,0,sudoku.sudokuData,0,sudoku.sudokuData.length);
        return sudoku;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(sudokuData);
    }

    public int getFilledNum() {
        return filledNum;
    }

    public void setFilledNum(int filledNum) {
        this.filledNum = filledNum;
    }

    /**
     * 数独单元格的数据类型
     */
    public class SudokuCeil {
        public int value = 0;
        public boolean fixedFlag = false;

        @NonNull
        @Override
        public SudokuCeil clone() throws CloneNotSupportedException {
            SudokuCeil sudokuceil = new SudokuCeil();
            sudokuceil.value = value;
            sudokuceil.fixedFlag =fixedFlag;
            return sudokuceil;
        }
    }
}
