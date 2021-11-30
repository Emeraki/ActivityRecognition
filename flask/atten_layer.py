import torch
from torch import nn
from torch.nn import functional as F


class mySelfAttention(nn.Module):
    def __init__(self, size, num_hops, batch_size):
        super(mySelfAttention, self).__init__()

        self.size = size # the attention length??????
        self.num_hops = num_hops #
        self.batch_size = batch_size

        self.W1 = nn.Parameter(data=nn.init.kaiming_normal_(torch.empty((1, self.size, self.size))),requires_grad=True)
        self.W2 = nn.Parameter(data=nn.init.kaiming_normal_(torch.empty((1, self.num_hops, self.size))),requires_grad=True)

        self.W1.data = self.W1.data.repeat(self.batch_size, 1, 1)
        self.W2.data = self.W2.data.repeat(self.batch_size, 1, 1)
        # self.W1 = nn.Parameter(data=torch.FloatTensor(1, self.size, self.size), requires_grad=True)
        # self.W2 = nn.Parameter(data=torch.FloatTensor(1, self.num_hops, self.size), requires_grad=True)


    def forward(self, input):
        # # 将W1 W2扩充维度，变为(8,32,32) (8,10,32)
        # self.W1.data = self.W1.data.repeat(self.batch_size, 1, 1)
        # self.W2.data = self.W2.data.repeat(self.batch_size, 1, 1)

        # hidden_states_transposed [8, 32, 100]
        hidden_states_transposed = input.permute(0, 2, 1)

        # print("in attention, the hidden_states_transposed is :",hidden_states_transposed[0,:,0])

        # attention_score (8, 32, 100)
        attention_score = torch.bmm(self.W1, hidden_states_transposed)



        # print("in attention, the attention_score is :",attention_score[0,:,0])

        # attention_score (8, 32, 100)
        attention_score = torch.tanh(attention_score)

        # attention_weights (8, 10, 100)
        attention_weights = torch.bmm(self.W2, attention_score)

        # attention_weights (8, 10, 100)
        attention_weights = F.softmax(attention_weights, dim=-1)

        # embedding_matrix (8, 10, 32)
        embedding_matrix = torch.bmm(attention_weights, input)

        # embedding_matrix_flattened (8, 320 )
        embedding_matrix_flattened = embedding_matrix.view(self.batch_size, -1)

        return embedding_matrix_flattened, attention_weights


# if __name__ == '__main__':
#     input = torch.rand(16, 100, 32)
#     model = mySelfAttention(size=32, num_hops=20, batch_size=16)
#
#     for parameters in model.parameters():
#         print(parameters.shape)
#
#     out1, out2 = model(input)
#     print(out1.shape)
#     print(out2.shape)
