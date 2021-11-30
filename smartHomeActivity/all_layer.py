import torch
from torch import nn
from torch.nn import functional as F
from atten_layer import mySelfAttention


class DeepConvLSTM_with_selfAttention(nn.Module):
    def __init__(self, num_labels, num_conv_filters, input_width, LSTM_units, size, num_hops, batch_size):
        super(DeepConvLSTM_with_selfAttention, self).__init__()

        # 过一个卷积层
        self.conv = nn.Conv2d(in_channels=1, out_channels=num_conv_filters, kernel_size=[1, input_width], stride=[1, 1])

        # 过一个rnn层
        self.lstm = nn.LSTM(input_size=num_conv_filters, hidden_size=LSTM_units, batch_first=True)

        # 过一个注意力层
        self.atten = mySelfAttention(size=size, num_hops=num_hops, batch_size=batch_size)

        # 过一个全连接层
        self.full = nn.Linear(size * num_hops,num_labels)

    def forward(self, input):
        # 此时 input是(8, 1, 100, 3) batch,通道数，高，宽

        # print("input data is :",input[0,0,0,:])

        conv_out = self.conv(input)  # torch.Size([8, 3, 100, 1])  通道数变成了3
        conv_out = torch.squeeze(conv_out, dim=3)  # torch.Size([8, 3, 100])
        conv_out = conv_out.transpose(1, 2)  # torch.Size([8, 100, 3])  batch,timesteps,input_dim

        # print("conv_out is :", conv_out[0, 0, :])

        # [8, 100, 32] , [1, 8, 32] , [1, 8, 32]
        lstm_out, (hidden, cell) = self.lstm(conv_out)

        # print("lstm_out is :",lstm_out[0,0,:])

        # embedding_matrix_flattened (8, 320 )
        embedding_matrix_flattened, attention_weights = self.atten(lstm_out)

        # print("attention out is :",embedding_matrix_flattened[:,0])

        out = self.full(embedding_matrix_flattened)

        return out

# if __name__ == '__main__':
#     # device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
#     #
#     #
#     #
#     # input = torch.randn((8,1,100,3))
#     # input = input.to(device)
#     #
#     #
#     # label = torch.tensor([1,1,1,1,0,0,0,0])
#     # label = label.to(device)
#     # print(label.shape)
#     #
#     # model = DeepConvLSTM_with_selfAttention(6,3,3,32,32,10,8)
#     # model = model.to(device)
#     #
#     # # 选择损失函数和优化方法
#     # loss_func = nn.CrossEntropyLoss().to(device)
#     # optimizer = torch.optim.Adam(model.parameters(), lr=1e-4)
#     #
#     #
#     #
#     # logits = model(input)
#     #
#     # loss = loss_func(logits,label)
#     #
#     # optimizer.zero_grad()
#     # loss.backward()
#     # optimizer.step()
#     # test = torch.tensor([[2,0,0,0],[0,2,0,0]])
#     # print(test.argmax(dim=1))




