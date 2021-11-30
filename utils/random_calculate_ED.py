import numpy as np
import matplotlib.pyplot as plt
from sklearn import manifold, datasets
import random
import pandas as pd

# 读数据
filePath = 'E:\\new_mean_320_all_feature.csv'

# (8, 321)
feature_data = np.loadtxt(filePath, dtype=np.float, delimiter=',', skiprows=1)

# (8, 320) 8个动作平均特征
feature_data = feature_data[:,1:]


# 随机选取一个动作
filePath2 = 'E:\\new_320_all_feature.csv'
activity = np.loadtxt(filePath2, dtype=np.float, delimiter=',', skiprows=1)
random_num = random.sample(range(0, 4835),1)

label = activity[random_num[0],1]

activity = activity[:,2:]
random_activity = activity[random_num[0], :]



score_1 = 0
score_2 = 0
score_3 = 0
score_4 = 0
score_5 = 0
score_6 = 0
score_7 = 0
score_8 = 0

for i in range(320):
    score_1 += (random_activity[i] - feature_data[0,i]) ** 2
    score_2 += (random_activity[i] - feature_data[1,i]) ** 2
    score_3 += (random_activity[i] - feature_data[2,i]) ** 2
    score_4 += (random_activity[i] - feature_data[3,i]) ** 2
    score_5 += (random_activity[i] - feature_data[4,i]) ** 2
    score_6 += (random_activity[i] - feature_data[5,i]) ** 2
    score_7 += (random_activity[i] - feature_data[6,i]) ** 2
    score_8 += (random_activity[i] - feature_data[7,i]) ** 2

score_1 = score_1 ** 0.5
score_2 = score_2 ** 0.5
score_3 = score_3 ** 0.5
score_4 = score_4 ** 0.5
score_5 = score_5 ** 0.5
score_6 = score_6 ** 0.5
score_7 = score_7 ** 0.5
score_8 = score_8 ** 0.5

print("==========")
print("动作是："+str(label))
print(score_1)
print(score_2)
print(score_3)
print(score_4)
print(score_5)
print(score_6)
print(score_7)
print(score_8)
print("==========")
