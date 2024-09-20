<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue';
import { Edit, Delete, Plus } from '@element-plus/icons-vue';
import { ElMessageBox, ElMessage } from 'element-plus';
import router from '@/router/index.js';
import {
  aiAnalyzeService,
  courseDeleteService,
  courseItemFeedbackListService,
  courseItemListService,
  courseListService,
} from '@/api/teacher.js';

// 用户搜索时选中的分类id
const categoryId = ref('');
// 用户搜索时选中的发布状态
const keyword = ref('');
// 文章列表数据模型
const articles = ref([]);
// title 数据模型
const title = ref('');
// 分页条数据模型
const pageNum = ref(1); // 当前页
const total = ref(20); // 总条数
const pageSize = ref(5); // 每页条数

// 当每页条数发生了变化，调用此函数
const onSizeChange = (size) => {
  pageSize.value = size;
  articleList();
};
// 当前页码发生变化，调用此函数
const onCurrentChange = (num) => {
  pageNum.value = num;
  articleList();
};

// 获取文章列表数据
const articleList = async () => {
  let params = {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    keyword: keyword.value ? keyword.value : null,
  };
  let result = await courseListService(params);
  // 渲染视图
  total.value = result.data.total;
  articles.value = result.data.items;
};
articleList();

// 控制抽屉是否显示
const visibleDrawer = ref(false);
// 添加表单数据模型
const articleModel = ref({
  title: '',
  content: '',
  createTime: '',
  id: '',
});

// 导入 token 给文件上传使用
import { useTokenStore } from '@/stores/token.js';

const tokenStore = useTokenStore();

// 编辑文章回显
const editArticle = (row) => {
  articleModel.value.title = row.title;
  articleModel.value.createTime = row.createTime;
  articleModel.value.id = row.id;
};

const finalCourseItemData = ref([]);
const finalCourseItemFeedbackData = ref([]);
const aicourseId = ref('');
// 选择的情绪类型及对应的emoji
const emotionTypes = [
  { type: 'positive', emoji: '😄' },
  { type: 'neutral', emoji: '😐' },
  { type: 'negative', emoji: '😠' },
];
// 获取情绪类型对应的emoji
const getEmojiForFeedbackType = (feedbackType) => {
  const emotion = [...emotionTypes, { type: 'somewhatpositive', emoji: '🙂' }, { type: 'somewhatnegative', emoji: '🙁' }].find((e) => e.type === feedbackType);
  return emotion ? emotion.emoji : '';
};

// 每个课程项的选中的情绪类型
const selectedEmotions = reactive({});

// 每个课程项的分页状态
const feedbackPagination = reactive({});

// 每个课程项的prompt
const prompts = reactive({});

// 初始化选中的情绪类型和分页状态
const initializeSelectedEmotions = () => {
  finalCourseItemData.value.forEach((item) => {
    selectedEmotions[item.id] = [];
    feedbackPagination[item.id] = { currentPage: 1, pageSize: 6 };
    prompts[item.id] = '';
  });
};

watch(finalCourseItemData, initializeSelectedEmotions);

// 获取 courseitem
const getcourseitem = async (row) => {
  let result = await courseItemListService(row.id);
  // 提取 json 中的 courseitem 的 ids
  let courseitemids = result.data.map((item) => item.id);
  // 将 courseitemids 数组传递给 getcourseitemfeedback
  let result2 = await getcourseitemfeedback(courseitemids);
  finalCourseItemData.value = result.data;
  finalCourseItemFeedbackData.value = result2.data;
  await nextTick();
  generateCharts();
  generateCharts2();
};
// 获取 courseitemfeedback
const getcourseitemfeedback = async (ids) => {
  // 构建 URL 参数
  const params = new URLSearchParams();
  ids.forEach((id) => params.append('courseItemId', id));
  let result = await courseItemFeedbackListService(params);
  return result;
};

// 删除 course
const deleteCourse = (row) => {
  ElMessageBox.confirm(
      'proxy will permanently delete the file. Continue?',
      'Warning',
      {
        confirmButtonText: 'OK',
        cancelButtonText: 'Cancel',
        type: 'warning',
      }
  )
      .then(async () => {
        // 删除
        let result = await courseDeleteService(row.id);
        ElMessage({
          type: 'success',
          message: 'Delete completed',
        });
        // 刷新列表
        articleList();
      })
      .catch(() => {
        ElMessage({
          type: 'info',
          message: 'Delete canceled',
        });
      });
};

// 获取与指定 courseItemId 对应的反馈
const feedbacksForItem = (itemId) => {
  const selected = selectedEmotions[itemId] || [];
  if (selected.length === 0) {
    return finalCourseItemFeedbackData.value.filter((feedback) => feedback.courseitemid === itemId);
  }
  return finalCourseItemFeedbackData.value.filter(
      (feedback) => feedback.courseitemid === itemId && selected.includes(feedback.feedbackType)
  );
};

// 获取当前页显示的反馈
const paginatedFeedbacksForItem = (itemId) => {
  const feedbacks = feedbacksForItem(itemId);
  const { currentPage, pageSize } = feedbackPagination[itemId];
  const start = (currentPage - 1) * pageSize;
  const end = start + pageSize;
  return feedbacks.slice(start, end);
};

const aiResult = ref();
// 发送选中的反馈内容到后端
const sendFeedback = async (itemId) => {
  const item = finalCourseItemData.value.find((i) => i.id === itemId);
  const dataToSend = {
    articleId: articleModel.value.id,
    courseItemId: item.id,
    prompt: prompts[item.id], // 包含 prompt
    selectedEmotions: selectedEmotions[item.id], // 选中的情绪类型名称
  };
  const result = await aiAnalyzeService(dataToSend); // 调用发送到后端的API函数
  aiResult.value = result.data;
  //todo 处理airesult
  ElMessage({
    type: 'success',
    message: 'Feedback sent successfully',
  });
};

//图
import * as echarts from 'echarts';

const generateCharts = () => {
  finalCourseItemData.value.forEach((item, index) => {
    const chartDom = document.getElementById(`chart-${index}`);
    if (chartDom) {
      const chart = echarts.init(chartDom);
      const option = {
        title: {
          text: 'Sentiment Score',
          left: 'center',
        },
        tooltip: {
          trigger: 'item',
        },
        legend: {
          top: '5%',
          left: 'center',
        },
        series: [
          {
            name: item.title,
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2,
            },
            label: {
              show: false,
              position: 'center',
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 40,
                fontWeight: 'bold',
              },
            },
            labelLine: {
              show: false,
            },
            data: [
              { value: item.positive, name: 'positive', itemStyle: { color: '#4CAF50' } },
              { value: item.neutral, name: 'neutral', itemStyle: { color: '#FFC107' } },
              { value: item.negative, name: 'negative', itemStyle: { color: '#F44336' } },
            ],
          },
        ],
      };
      chart.setOption(option);
      // 添加点击事件监听器
      chart.on('click', (params) => {
        if (params.name === 'good') {
          console.log(`You clicked on good with value ${params.value}`);
          // 在这里添加针对 'good' 的特定处理逻辑
        } else if (params.name === 'normal') {
          console.log(`You clicked on normal with value ${params.value}`);
          // 在这里添加针对 'normal' 的特定处理逻辑
        } else if (params.name === 'bad') {
          console.log(`You clicked on bad with value ${params.value}`);
          // 在这里添加针对 'bad' 的特定处理逻辑
        }
      });
    }
  });
};
//创建图表2
const generateCharts2 = () => {
  finalCourseItemData.value.forEach((item, index) => {
    const chartDom = document.getElementById(`chart2-${index}`);
    if (chartDom) {
      const chart = echarts.init(chartDom);
      const option = {
        title: {
          text: 'Student Rating',
          left: 'center',
        },
        tooltip: {
          trigger: 'item',
        },
        legend: {
          top: '5%',
          left: 'center',
        },
        series: [
          {
            name: item.title,
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2,
            },
            label: {
              show: false,
              position: 'center',
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 40,
                fontWeight: 'bold',
              },
            },
            labelLine: {
              show: false,
            },
            data: [
              { value: item.good, name: 'good', itemStyle: { color: '#4CAF50' } },
              { value: item.normal, name: 'normal', itemStyle: { color: '#FFC107' } },
              { value: item.bad, name: 'bad', itemStyle: { color: '#F44336' } },
            ],
          },
        ],
      };
      chart.setOption(option);
    }
  });
};

//抽屉
const onDrawerClosed = () => {
  console.log('Drawer has been closed');
  // 这里可以执行清理操作，如关闭 WebSocket 连接
  if (socket) {
    socket.close();
    messages.value = [];
    aiResult.value = '';
  }
};

// ai分析区域
const selectedCourseItemId = ref('');
const getSelectedCourseItemName = (itemId) => {
  const item = finalCourseItemData.value.find((i) => i.id === itemId);
  return item ? item.title : '';
};
//聊天
import useUserInfoStore from '@/stores/userInfo.js';

const userInfoStore = useUserInfoStore();
import { onMounted, onBeforeUnmount } from 'vue';
// add WebSocket related states and functions
const messages = ref([]); // store chat messages
const currentMessage = ref(''); // current message input
let socket; // WebSocket instance
//build WebSocket connection
const connectWebSocket = (row) => {
  aicourseId.value = row.id;
  socket = new WebSocket('ws://localhost:8080/chat/' + userInfoStore.info.id + '/' + aicourseId.value);
  socket.onopen = () => {
    console.log('WebSocket connection established');
  };
  socket.onmessage = (event) => {
    try {
      const message = JSON.parse(event.data);
      messages.value.push(message);
    } catch (error) {
      console.error('Failed to parse message:', error);
    }
  };
  socket.onclose = () => {
    console.log('WebSocket连接已关闭');
  };
  socket.onerror = (error) => {
    console.error('WebSocket错误:', error);
  };
};

// send message to WebSocket server
const sendMessage = () => {
  const usermessage = `{"user": "${currentMessage.value}"}`;
  socket.send(currentMessage.value);
  messages.value.push(JSON.parse(usermessage));
  currentMessage.value = '';
};

// close WebSocket connection when component is unmounted
onBeforeUnmount(() => {
  if (socket) {
    socket.close();
    messages.value = [];
    aiResult.value = '';
  }
});
</script>

<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <div class="extra">
          <el-button type="primary" @click="router.push('/teacher/GenerateFeedback')">Add analysis file</el-button>
        </div>
      </div>
    </template>
    <!-- Search form -->
    <el-form inline>
      <el-form-item label="search">
        <el-input placeholder="word" v-model="keyword">
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="articleList">search</el-button>
        <el-button @click="keyword = ''">reset</el-button>
      </el-form-item>
    </el-form>
    <!-- Article list -->
    <el-table :data="articles" style="width: 100%">
      <el-table-column label="TaskId" width="400" prop="id"></el-table-column>
      <el-table-column label="TaskTitle" width="400" prop="title"></el-table-column>
      <el-table-column label="Release time" prop="createTime"></el-table-column>
      <el-table-column label="operation" width="100">
        <template #default="{ row }">
          <el-button :icon="Edit" circle plain type="primary"
                     @click="editArticle(row); getcourseitem(row); visibleDrawer=true; title='feedback';connectWebSocket(row)"></el-button>
          <el-button :icon="Delete" circle plain type="danger" @click="deleteCourse(row)"></el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="No data available"/>
      </template>
    </el-table>
    <!-- Pagination -->
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :page-sizes="[3, 5, 10, 15]"
                   layout="jumper, total, sizes, prev, pager, next" background :total="total"
                   @size-change="onSizeChange"
                   @current-change="onCurrentChange" style="margin-top: 20px; justify-content: flex-end"/>
  </el-card>

  <!-- 抽屉 -->
  <el-drawer v-model="visibleDrawer" :title="title" direction="rtl" size="85%" @closed="onDrawerClosed">
    <el-row :gutter="10">
      <!--      左-->
      <el-col :span="12">
        <!-- course 对应的 item 信息 -->
        <div v-for="(item, index) in finalCourseItemData" :key="item.id">
          <el-card style="max-width: 100%" class="mb-4">
            <template #header>
              <div class="card-header">
                <span>{{ item.title }}</span>
              </div>
            </template>
            <!-- 饼状图容器 -->
            <el-row>
              <el-col :span="12">
                <div :id="`chart-${index}`" style="width: 100%; height: 400px;"></div>
              </el-col>
              <el-col :span="12">
                <div :id="`chart2-${index}`" style="width: 100%; height: 400px;"></div>
              </el-col>
            </el-row>
            <div class="feedback-container">
              <div v-for="feedback in paginatedFeedbacksForItem(item.id)" :key="feedback.id" class="feedback-item">
                <div class="feedback-content">
                  <p class="feedback-text">{{ feedback.feedback }}</p>
                  <span class="feedback-type">{{
                      getEmojiForFeedbackType(feedback.feedbackType)
                    }} {{ feedback.feedbackType }}</span>
                </div>
              </div>
              <el-pagination
                  v-model:current-page="feedbackPagination[item.id].currentPage"
                  :page-size="feedbackPagination[item.id].pageSize"
                  :total="feedbacksForItem(item.id).length"
                  @current-change="page => feedbackPagination[item.id].currentPage = page"
                  layout="prev, pager, next"
                  background
                  class="pagination">
              </el-pagination>
            </div>

            <template #footer>Footer content</template>
          </el-card>
        </div>
      </el-col>

      <!--      ai区域-->
      <el-col :span="12" >
          <el-card style="max-width: 100%" class="aiArea">
            <div class="grid-content ep-bg-purple-light"/>
            <!-- AI 分析结果选择和显示区域 -->
            <el-select v-model="selectedCourseItemId" placeholder="Select a course item" style="width: 100%;">
              <el-option
                  v-for="item in finalCourseItemData"
                  :key="item.id"
                  :label="item.title"
                  :value="item.id"
              />
            </el-select>

            <div v-if="selectedCourseItemId">
              <!-- 显示选中的课程项的名称 -->

              <!-- 显示选中的情绪复选框 -->
              <el-checkbox-group v-model="selectedEmotions[selectedCourseItemId]">
                <el-checkbox v-for="emotion in emotionTypes" :label="emotion.type" :key="emotion.type">
                  {{ emotion.emoji }} {{ emotion.type }}
                </el-checkbox>
              </el-checkbox-group>

            </div>


            <!-- Chatbot UI  -->
            <div class="chatbot-container">
              <div class="chat-window">
                <div
                    v-for="(msg, index) in messages"
                    :key="index"
                    class="message">
                  <strong class="user-message">{{msg.user }}</strong>
                  <span class="system-message" v-html="msg.message"></span>
                </div>
              </div>
              <input class="chat-input" v-model="currentMessage" @keyup.enter="sendMessage" placeholder="chat with ai..."/>
              <button @click="sendMessage">send</button>
            </div>
          </el-card>
      </el-col>
    </el-row>
  </el-drawer>
</template>

<style lang="scss" scoped>
.el-row {
  margin-bottom: 20px;
}

.el-row:last-child {
  margin-bottom: 0;
}

.el-col {
  border-radius: 4px;
}

.grid-content {
  border-radius: 4px;
  min-height: 36px;
}

.aiArea{
  position: fixed;
  margin-left: 10px;
  padding-left: 10px;
  width: 750px
}

// chatbot
.chatbot-container {
  border: 1px solid #ccc;
  padding: 10px;
  max-width: 800px;
  margin: 20px auto;
  width: 700px;
}

.chat-window {
  max-height: 750px;
  overflow-y: auto;
  border: 1px solid #ccc;
  padding: 10px;
  margin-bottom: 10px;
}

.message {
  margin-bottom: 6px;
  padding: 4px;
  border-radius: 5px;
}

.user-message {
  color: #0c5460;
  font-weight: bold;
  background-color: #00ffff;
  border-radius: 5px;
}

.system-message {
  color: #555;
  padding: 5px;
  border-radius: 5px;
}

// feedback display
.feedback-container {
  padding-bottom: 20px; /* Space for the pagination */
}

.feedback-item {
  padding: 15px;
  border-bottom: 1px solid #e0e0e0;
}

.feedback-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.feedback-text {
  margin: 0;
  flex-grow: 1;
  margin-right: 20px;
}

.feedback-type {
  font-weight: bold;
}

.pagination {
  margin-top: 20px; /* Space between feedback items and pagination */
  text-align: center; /* Center the pagination */
}

// 使用 ::v-deep 覆盖 el-drawer__header 样式
::v-deep .el-drawer__header {
  margin-bottom: 0; /* 去掉 margin-bottom */
  button {
    display: none; /* 隐藏按钮 */
  }
}

.chat-input {
  width: 100%;
  font-size: 16px; /* 增加字体大小 */
  padding: 10px; /* 增加内边距 */
  box-sizing: border-box; /* 确保 padding 不会影响元素的总宽度 */
}
</style>



