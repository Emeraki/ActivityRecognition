import numpy as np
import torch
import torch.nn as nn
from torch.utils.data import Dataset
from torch.utils import data
from this_all_layer import DeepConvLSTM_with_selfAttention
from visdom import Visdom
import random
import pandas as pd

'''处理数据，将所有的动作经过模型后的特征记录下来'''

# l = [[6,6,6],[7,7,7],[8,8,8]]

# name = ['id','name','sex']
#
# test = pd.DataFrame(data=l)
#
# l.append([1,2,3])
#
# print(l)

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

    feature = [0] * (embedding.size(1) + 1)
    feature[0] = pre_data[i, 0]

    # 将数据存入feature
    for k in range(embedding.size(1)):

        for l in range(embedding.size(0)):
            feature[k + 1] += embedding[l, k].item()
        feature[k + 1] = feature[k + 1] / embedding.size(0)

    print("处理完了第" + str(i) + "条")
    all_feature.append(feature)

save = pd.DataFrame(data=all_feature)
save.to_csv("E:/new_320_all_feature.csv")
