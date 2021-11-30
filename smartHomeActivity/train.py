import numpy as np
import torch
import torch.nn as nn
from torch.utils.data import Dataset
from torch.utils import data
from all_layer import DeepConvLSTM_with_selfAttention
from visdom import Visdom
import random


EPOCH = 10
NUM_LABELS = 8
BATCH_SIZE = 8
LSTM_UNITS = 32
CNN_FILTERS = 3
NUM_LSTM_LAYERS = 1
LEARNING_RATE = 1e-4
PATIENCE = 20
SEED = 0
F = 32
D = 10
device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")

viz = Visdom()
viz.line([0.], [0.], win="train_loss", opts=dict(title="train_loss"))
viz.line([0.], [0.], win="test_acc", opts=dict(title="test_acc"))

class MyDataSet(Dataset):
    def __init__(self,data_root,data_label):
        """
        这里传入的一个是data数据一个是label数据，在行维度上要对应。
        :param data_root:  5 x 3维
        :param data_label:  5 x 1维
        """
        self.data = data_root
        self.label = data_label

    def __getitem__(self, index):
        data = self.data[index]
        label = self.label[index]
        return data,label
    def __len__(self):
        return len(self.data)


if __name__ == '__main__':
    # 读数据
    filePath = 'E:\\11-16.csv'

    # (4835, 1201)
    pre_data = np.loadtxt(filePath,dtype=np.float,delimiter=',')

    print(pre_data.shape)

    # (4835, 400, 3)
    X = np.zeros([pre_data.shape[0],400,3])

    # (4835, )
    y = np.zeros(pre_data.shape[0])

    # 填入加速度X轴
    for i in range(pre_data.shape[0]):
        for j in range(400):
            X[i,j,0] = pre_data[i,j+1]
            X[i, j, 1] = pre_data[i, j + 401]
            X[i, j, 2] = pre_data[i, j + 801]

    # # 填入加速度Y轴
    # for i in range(pre_data.shape[0]):
    #     for j in range(400):
    #         X[i, j, 1] = pre_data[i, j + 401]
    #
    # # 填入加速度Z轴
    # for i in range(pre_data.shape[0]):
    #     for j in range(400):
    #         X[i,j,2] = pre_data[i,j+801]

    # 填入标签label
    for i in range(pre_data.shape[0]):
        y[i] = pre_data[i,0]-1

    # (4835, 1, 400, 3)
    X = np.expand_dims(X,axis=1)

    # 划分训练集 测试集
    random_idx = random.sample(range(0, 4835), 4835)
    train_idx = random_idx[0:4352]
    test_idx = random_idx[4352:4832]

    train_data = X[train_idx] # (4352, 1, 400, 3)
    train_label = y[train_idx] # (4352,)
    test_data = X[test_idx] # (480, 1, 400, 3)
    test_label = y[test_idx] # (480,)

    # 定义自己的dataset
    myTrainDataSet = MyDataSet(train_data,train_label)
    myTestDataSet = MyDataSet(test_data,test_label)

    # 定义自己的dataloader
    train_loader = data.DataLoader(dataset=myTrainDataSet, batch_size=BATCH_SIZE, shuffle=True)
    test_loader = data.DataLoader(dataset=myTestDataSet, batch_size=BATCH_SIZE, shuffle=True)

    # 定义模型
    model = DeepConvLSTM_with_selfAttention(num_labels=NUM_LABELS,
                                            num_conv_filters=CNN_FILTERS,
                                            input_width=3,
                                            LSTM_units=LSTM_UNITS,
                                            size=F,
                                            num_hops=D,
                                            batch_size=BATCH_SIZE)
    model = model.to(device)

    # 选择损失函数和优化方法
    loss_func = nn.CrossEntropyLoss().to(device)
    optimizer = torch.optim.Adam(model.parameters(), lr=LEARNING_RATE)

    for epoch in range(100):
        model.train()
        for step, (sensor, label) in enumerate(train_loader):

            sensor = sensor.float()
            sensor = sensor.to(device)
            label = label.long()
            label = label.to(device)

            logits = model(sensor)


            loss = loss_func(logits, label)

            optimizer.zero_grad()
            loss.backward()
            optimizer.step()

            # 每100个step，输出一下训练的情况
            if step % 100 == 0:
                print("train epoch: {} [{}/{} ({:.0f}%)] \t Loss: {:.6f}".
                      format(epoch,
                             step * len(sensor),
                             len(train_loader.dataset),
                             100. * step / len(train_loader),
                             loss.item()))

        # 完成一个epoch，看一下loss
        print("\t===============epoch {} done, the loss is {:.5f}===============\t".format(epoch, loss.item()))
        # 完成一个epoch，画一下图
        viz.line([loss.item()], [epoch], win="train_loss", update="append")

        model.eval()
        with torch.no_grad():
            total_correct = 0
            total_num = 0
            for step, (sensor, label) in enumerate(test_loader):
                sensor = sensor.float()
                sensor = sensor.to(device)
                label = label.long()
                label = label.to(device)

                logits = model(sensor)

                pred = logits.argmax(dim=1)

                # print(pred)
                # print(label)
                # tensor([5, 5, 5, 5, 0, 5, 5, 5], device='cuda:0')
                # tensor([2, 4, 4, 4, 3, 2, 4, 5], device='cuda:0')

                total_correct += torch.eq(pred, label).float().sum()
                total_num += sensor.size(0)

            acc = total_correct / total_num

        # 看一下正确率
        print("\t===============epoch {} done, the ACC is {:.5f}===============\t".format(epoch, acc.item()))
        viz.line([acc.item()], [epoch], win="test_acc", update="append")

        if (acc.item() > 0.95):
            torch.save(model.state_dict(), "11_16_param.pkl")
            break