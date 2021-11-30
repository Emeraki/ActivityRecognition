import numpy as np
import torch
import torch.nn as nn
from torch.utils.data import Dataset
from torch.utils import data
from this_all_layer import DeepConvLSTM_with_selfAttention
from visdom import Visdom
import random
import pandas as pd
import torch.nn.functional as func

'''处理数据，将所有的动作经过模型后的特征记录下来'''

NUM_LABELS = 8
BATCH_SIZE = 8
LSTM_UNITS = 32
CNN_FILTERS = 3
F = 32
D = 10
activity = ['喝水', '抽烟', '站起', '坐下', '拖地', '扫地', '走路', '未知']
device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")

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
filePath = 'E:\\11-16.csv'

# (4835, 1201)
pre_data = np.loadtxt(filePath, dtype=np.float, delimiter=',')

all_feature = []

# input = torch.zeros([1, 1, 400, 3])
#
# num = 0
#
# pre_data = pre_data[num,:]
#
# for j in range(400):
#     input[:, :, j,0] = pre_data[num, j + 1]
#     input[:, :, j,1] = pre_data[num, j + 401]
#     input[:, :, j,2] = pre_data[num, j + 801]
#
# input = input.repeat(BATCH_SIZE, 1, 1, 1)
# input = input.float()
# input = input.to(device)
#
# result,embedding = model(input)
#
# result = func.softmax(result, dim=1)
#
# predict = result.argmax(dim=1)
#
# print(activity[predict[0].item()])

for i in range(pre_data.shape[0]):

    input = torch.zeros([1, 1, 400, 3])

    # 将数据存入input
    for j in range(400):
        input[:, :, j, 0] = pre_data[i, j + 1]
        input[:, :, j, 1] = pre_data[i, j + 401]
        input[:, :, j, 2] = pre_data[i, j + 801]

    input = input.repeat(BATCH_SIZE, 1, 1, 1)
    input = input.float()
    input = input.to(device)

    result, embedding = model(input)

    result = func.softmax(result, dim=1)

    feature = [0] * (result.size(1) + 1)
    feature[0] = pre_data[i, 0]

    # 将数据存入feature
    for k in range(result.size(1)):

        for l in range(result.size(0)):
            feature[k + 1] += result[l, k].item()
        feature[k + 1] = feature[k + 1] / result.size(0)

    print("处理完了第" + str(i) + "条")
    all_feature.append(feature)

save = pd.DataFrame(data=all_feature)
save.to_csv("E:/new_8_softmax_feature.csv")
