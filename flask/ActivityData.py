from flask import Flask, request
import torch
from all_layer import DeepConvLSTM_with_selfAttention
import torch.nn.functional as func
import time
from connect_to_database import saveData
import pymysql
import numpy as np

# 声明一些常量
NUM_LABELS = 8
BATCH_SIZE = 8
LSTM_UNITS = 32
CNN_FILTERS = 3
F = 32
D = 10
activity = ['喝水', '抽烟', '站起', '坐下', '拖地', '扫地', '走路', '未知']
device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
app = Flask(__name__)

# 建立模型
model = DeepConvLSTM_with_selfAttention(num_labels=NUM_LABELS,
                                        num_conv_filters=CNN_FILTERS,
                                        input_width=3,
                                        LSTM_units=LSTM_UNITS,
                                        size=F,
                                        num_hops=D,
                                        batch_size=BATCH_SIZE)
model.load_state_dict(torch.load('11_16_param.pkl'))
model = model.to(device)

# 读数据
filePath = 'E:\\new_mean_320_all_feature.csv'
# (8, 321)
feature_data = np.loadtxt(filePath, dtype=np.float, delimiter=',', skiprows=1)
# (8, 320) 8个动作平均特征
feature_data = feature_data[:,1:]



'''处理时间格式'''
def process_time(activity_time):
    # 处理时间 java传来的是毫秒为单位的13位时间戳
    ms_time = activity_time[0]

    # 转化成秒为单位
    s_time = round(ms_time / 1000)

    # 格式化后的时间
    true_time = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(s_time))

    return true_time

'''处理输入数据'''
def process_acc_data(x, y, z):
    # 创建输入
    input = torch.zeros([1, 1, 400, 3])

    # 向input中存入X,Y,Z轴的值
    for i in range(400):
        input[:, :, i, 0] = x[i]
        input[:, :, i, 1] = y[i]
        input[:, :, i, 2] = z[i]

    input = input.repeat(BATCH_SIZE, 1, 1, 1)
    input = input.float()
    input = input.to(device)

    return input

'''计算欧几里得距离'''
def euclidean_distance(embedding):

    # 输入的embedding是一个(8,320维度向量) 首先利用平均值得到一个(320,)维度向量
    feature = [0] * 320

    for i in range(embedding.size(0)):
        for j in range(embedding.size(1)):
            feature[j] += (embedding[i,j].item()/embedding.size(0))

    # 计算八个动作的欧式距离
    distance = [0] * NUM_LABELS

    # 得分
    score = 0

    for i in range(NUM_LABELS):
        for j in range(len(feature)):
            score += (feature[j] - feature_data[i,j]) ** 2
        score = score ** 0.5
        distance[i] = score

    return distance



@app.route('/sendData', methods=['POST'])
def senddata():

    client_json_data = request.get_json()

    # 得到加速度X,Y,Z值
    x = client_json_data.get("x")
    y = client_json_data.get("y")
    z = client_json_data.get("z")

    # 获得手表id
    watch_id = client_json_data.get("deviceId")

    # 得到做动作时间
    activity_time = client_json_data.get("time")

    # 格式化时间
    true_time = process_time(activity_time)

    # 处理xyz,得到输入模型的数据
    input = process_acc_data(x, y, z)

    # 模型输出,隐藏层
    result,embedding = model(input)

    # 得到欧式距离
    distance = euclidean_distance(embedding)


    # 根据flag判断是否识别这个动作
    flag = False
    for i in range(len(distance)):
        if(distance[i] < 4.2):
            flag = True
            break

    # 执行相应判断
    if(flag):

        # 过一个softMax分类器
        result = func.softmax(result, dim=1)

        # 预测结果
        predict = result.argmax(dim=1)

        print(activity[predict[0].item()])  # 预测结果
        print("time: " + true_time)  # 时间

        # 判断一下，存入数据库, ‘未知’动作不存
        # if (predict[0].item() != 7):
        #     saveData(activity_time[0], str(activity[predict[0].item()]),watch_id)
        print("================================")

        # 返回数据
        return "====================识别到的动作是" + str(activity[predict[0].item()]) + "=========================="

    else:
        print("此动作无意义，不识别")
        print("================================")
        return "==============此动作无意义，不识别============"






if __name__ == '__main__':
    app.run(host='127.0.0.1', port=6666, debug=False, threaded=True)
