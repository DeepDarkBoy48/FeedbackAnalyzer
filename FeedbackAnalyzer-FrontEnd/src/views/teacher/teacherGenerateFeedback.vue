<template>
  <el-card class="page-container">
    <el-row :gutter="20">
      <el-col :span="10">
        <div class="grid-content ep-bg-purple"/>
        <el-form :model="form" label-width="auto" style="max-width: 600px">
          <el-form-item label="Task Title">
            <el-input v-model="form.title"/>
          </el-form-item>
<!--          <el-form-item label="Activity prompt">-->
<!--            <el-input v-model="form.prompt"/>-->
<!--          </el-form-item>-->

          <!-- upload -->
          <el-upload
              class="upload-demo"
              action="/api/teacher/analyze/generate"
              multiple
              :before-remove="beforeRemove"
              :limit="1"
              :on-exceed="handleExceed"
              :headers="{'Authorization': tokenStore.token}"
              :on-success="handleUploadSuccess"
              :on-remove="afterRemove"
          >
            <el-button type="primary">Click to upload</el-button>
            <template #tip>
              <div class="el-upload__tip">only excel file can be uploaded.</div>
            </template>
          </el-upload>

          <!-- course items -->
          <el-form-item label="Module">
            <el-checkbox-group v-model="selectedCourseItems">
              <el-checkbox
                  v-for="item in courseItemsTitles"
                  :key="item"
                  :label="item"
                  :name="item"
              >
                {{ item }}
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>

          <!-- Dynamic form items based on selected checkboxes -->
          <template v-for="item in selectedCourseItems" :key="item">
            <el-form-item :label="`Chart ${item}`" :style="{ display: 'none' }">
              <el-select v-model="getCourseItem(item).chart" placeholder="please select your chart">
                <el-option label="Pie chart" value="pie"/>
                <el-option label="Bar chart" value="bar"/>
              </el-select>
            </el-form-item>
            <el-form-item :label="`Description ${item}`" :style="{ display: 'none' }">
              <el-input v-model="getCourseItem(item).prompt" type="textarea"/>
            </el-form-item>
          </template>

          <!-- submit -->
          <el-form-item>
            <el-button type="primary" @click="onSubmit">Create</el-button>
            <el-button @click="resetForm">Reset</el-button>
          </el-form-item>
        </el-form>
      </el-col>

      <!-- right space -->
      <el-col :span="14">
        <el-card style="max-width: 1000px" class="cardcss" v-for="(item, index) in finalCourseItem" :key="index">
          <h1>{{item.title}}</h1>
          <!-- 图表容器 -->
          <el-row>
            <el-col :span="12"><div :id="`chart-${index}`" style="width: 100%; height: 500px;"></div></el-col>
            <el-col :span="12"><div :id="`chart2-${index}`" style="width: 100%; height: 500px;"></div></el-col>
          </el-row>

        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup>
import {reactive, ref, watch,nextTick} from 'vue'
import {useTokenStore} from '@/stores/token.js'
import {ElMessage, ElMessageBox} from 'element-plus'
import {teacherGenerateFeedbackService} from '@/api/teacher.js'


const form = reactive({
  title: '',
  prompt: '',
  courseItems: [],
  uploadedFile: null // 新增用于保存上传文件数据的字段
})

const selectedCourseItems = ref([])

const getCourseItem = (itemTitle) => {
  let item = form.courseItems.find(i => i.title === itemTitle)
  if (!item) {
    item = {title: itemTitle, chart: '', prompt: ''}
    form.courseItems.push(item)
  }
  return item
}

// 监听 selectedCourseItems 变化，当取消选择时清除相应的数据
watch(selectedCourseItems, (newVal, oldVal) => {
  const removedItems = oldVal.filter(item => !newVal.includes(item));
  removedItems.forEach(rmitem => {
    form.courseItems = form.courseItems.filter(i => i.title !== rmitem)
  });
});

const finalCourseItem = ref([])

// 提交表单
const onSubmit = async () => {
  const formData = new FormData();
  formData.append('form', new Blob([JSON.stringify({
    title: form.title,
    prompt: form.prompt,
    courseItems: form.courseItems
  })], { type: 'application/json' }));

  if (form.uploadedFile) {
    formData.append('file', form.uploadedFile.raw);
  }

  let result = await teacherGenerateFeedbackService(formData);
  if (result && result.data && Array.isArray(result.data)) {
    finalCourseItem.value = result.data; // 保存完整的对象
    await nextTick() // 等待 DOM 更新完成
    generateCharts();
    generateCharts2();
  } else {
    throw new Error('Invalid response format');
  }
  ElMessage.success('Create success!')
};


import * as echarts from 'echarts';



// 创建图表
const generateCharts = () => {
  finalCourseItem.value.forEach((item, index) => {
    const chartDom = document.getElementById(`chart-${index}`);
    if (chartDom) {
      const chart = echarts.init(chartDom);
      const option = {
        tooltip: {
          trigger: 'item'
        },
        legend: {
          top: '5%',
          left: 'center'
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
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 40,
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: [
              { value: item.scoreVo.good, name: 'good', itemStyle: { color: '#4CAF50' } },
              { value: item.scoreVo.normal, name: 'normal', itemStyle: { color: '#FFC107' } },
              { value: item.scoreVo.bad, name: 'bad', itemStyle: { color: '#F44336' } }
            ]
          }
        ]
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
  finalCourseItem.value.forEach((item, index) => {
    const chartDom = document.getElementById(`chart2-${index}`);
    if (chartDom) {
      const chart = echarts.init(chartDom);
      const option = {
        tooltip: {
          trigger: 'item'
        },
        legend: {
          top: '5%',
          left: 'center'
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
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 40,
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: [
              { value: item.sentimentVo.positive, name: 'positive' ,itemStyle: { color: '#4CAF50' }},
              // { value: item.sentimentVo.somewhatNegative, name: 'somewhatNegative',itemStyle: { color: '#FFC300' } },
              { value: item.sentimentVo.neutral, name: 'neutral' ,itemStyle: { color: '#FFC107' }},
              // { value: item.sentimentVo.somewhatPositive, name: 'somewhatPositive' ,itemStyle: { color: '#900C3F' }},
              { value: item.sentimentVo.negative, name: 'negative',itemStyle: { color: '#F44336' } },
            ]
          }
        ]
      };
      chart.setOption(option);
    }
  });
};

//重置表单
const resetForm = () => {
  form.title = ''
  form.prompt = ''
  form.courseItems = []
  form.uploadedFile = null
  selectedCourseItems.value = []
  finalCourseItem.value = []
}

// upload excel后用于显示courseItemsTitles的临时
let courseItemsTitles = ref([])

const tokenStore = useTokenStore()
// 上传超过限制的回调
const handleExceed = (files, uploadFiles) => {
  ElMessage.warning(
      `The limit is 1, you selected ${files.length} files this time, add up to ${files.length + uploadFiles.length} totally`
  )
}

// 删除前的回调
const beforeRemove = (uploadFile, uploadFiles) => {
  return ElMessageBox.confirm(`Cancel the transfer of ${uploadFile.name} ?`).then(
      () => true,
      () => false
  )
}

// 删除后的回调
const afterRemove = () => {
  form.courseItems = []
  form.uploadedFile = null
  selectedCourseItems.value = []
  courseItemsTitles.value = []
}

// 上传成功的回调
const handleUploadSuccess = (response, file) => {
  ElMessage.success('Upload success!')
  let result = response.data.courseItems
  courseItemsTitles.value = result
  selectedCourseItems.value = []
  form.uploadedFile = file // 保存上传的文件数据
}



</script>

<style>
.cardcss {
  margin: auto;
}
</style>




