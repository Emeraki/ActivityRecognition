package com.uestc.zl427.newPhone2.Data;

/**
 * Created by 47633 on 2017/5/8.
 */

/**
 * 预处理函数，通过预处理获得动作片段的开始位置和结束位置。处理结果包含以下三种情况：<br>
 * 1.扎到完整的动作片段；<br>
 * 2.找到进入动作片段的标识，但未能找到完整的动作片段；<br>
 * 3.未能找到进入动作片段的标识；<br>
 * 如果找到进入动作片段的标识，则hasSeg标识为true，否则为false。如果没有找到完整的动作片段，end参数为0，否则为结束点位置
 */
public class Pretreat {
    private int size;
    private int end;
    private int start;
    private boolean hasSeg;
    private String TAG = "pretreat";

    public void pret(AccData accData) {
        size = accData.acc_squar.size();
        start = -1;
        end = 0;
        double[] res_acc = new double[size];
        for (int i = 0; i < size; i++) {
            res_acc[i] = accData.acc_squar.get(i);
        }
        hasSeg = false;
        for (int i = 0; i < size; i += 10) {
            int next_slip = i + 10;
            if (next_slip - 1 >= size) {
                break;
            }
            if (!hasSeg && Mean(res_acc, i, next_slip) <= 0.8) {
                start = i;
            }
            if (!hasSeg && Mean(res_acc, i, next_slip) > 1.5) {
                hasSeg = true;
                continue;
            }
            if (hasSeg && Mean(res_acc, i, next_slip) <= 0.8) {
                if (start == -1) {
                    hasSeg = false;
                } else {
                    end = next_slip;
                    break;
                }
            }
        }
    }

    /**
     * 计算array中位置从start到end的数据的平均值（不包括end）
     *
     * @param array 参与运算的数组
     * @param start 参与运算的数据开始位置
     * @param end   参与运算的数据结束位置
     * @return
     */
    public static double Mean(double[] array, int start, int end) {
        double sum = 0;
        int n = end - start;
        for (int i = start; i < end; i++) {
            sum += array[i];
        }
        return sum / n;
    }

    public boolean hasSeg() {
        return hasSeg;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
