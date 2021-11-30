import numpy as np
import matplotlib.pyplot as plt
from sklearn import manifold, datasets
import random
import pandas as pd

# 读数据
filePath = 'E:\\new_320_all_feature.csv'

# (4835, 322)
feature_data = np.loadtxt(filePath, dtype=np.float, delimiter=',', skiprows=1)

# y = []
#
# for i in range(feature_data.shape[0]):
#     y.append(feature_data[i, 1])

# (4835, 320)
# feature_data = feature_data[:, 2:]

# 8个动作的平均特征
activity = np.zeros((8, 320))
num_activity = [0] * 8

for i in range(feature_data.shape[0]):
    label = feature_data[i, 1]
    label = int(label)
    num_activity[label - 1] += 1
    for j in range(320):
        activity[label - 1, j] += feature_data[i, j + 2]

print(num_activity)

# 求平均数
for i in range(8):
    num = num_activity[i]
    for j in range(320):
        activity[i,j] /= num



save = pd.DataFrame(data=activity)
save.to_csv("E:/new_mean_320_all_feature.csv")