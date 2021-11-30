import numpy as np
import matplotlib.pyplot as plt
from sklearn import manifold, datasets

# 读数据
filePath = 'E:\\new_320_all_feature.csv'

# (4835, 322)
feature_data = np.loadtxt(filePath, dtype=np.float, delimiter=',', skiprows=1)

y = []

for i in range(feature_data.shape[0]):
    y.append(feature_data[i,1])


# (4835, 320)
feature_data = feature_data[:,2:]


tsne = manifold.TSNE(n_components=2, init='pca', random_state=0)

X_tsne = tsne.fit_transform(feature_data)

print("Org data dimension is {}. Embedded data dimension is {}".format(feature_data.shape[-1], X_tsne.shape[-1]))

x_min, x_max = X_tsne.min(0), X_tsne.max(0)

X_norm = (X_tsne - x_min) / (x_max - x_min)  # 归一化

plt.figure()

for i in range(500):
    plt.text(X_norm[i, 0], X_norm[i, 1], str(y[i]), color=plt.cm.Set1(int(y[i])),
             fontdict={'weight': 'bold', 'size': 9})



plt.xticks([])
plt.yticks([])
plt.show()