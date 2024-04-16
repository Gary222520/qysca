<template>
  <div class="container">
    <uploader
      ref="uploader"
      :options="options"
      :autoStart="false"
      :file-status-text="fileStatusText"
      @file-added="onFileAdded"
      @file-success="onFileSuccess"
      @file-error="onFileError"
      @file-progress="onFileProgress"
      class="uploader">
      <uploader-unsupport></uploader-unsupport>
      <uploader-drop class="uploader-drop">
        <uploader-btn class="uploader-btn" :attrs="{ accept: props.accept }">
          <div style="text-align: center">
            <CloudUploadOutlined :style="{ fontSize: '30px' }" />
            <div class="upload_text">{{ props.uploadText }}</div>
          </div>
        </uploader-btn>
      </uploader-drop>
      <div v-if="upload.uploadFile" class="progress">
        <div v-if="upload.progress.status === 'active'">{{ upload.uploadFile?.name }}</div>
        <div v-if="upload.progress.status === 'exception'" class="upload-failed">
          {{ `${upload.uploadFile?.name} 上传失败` }}
        </div>
        <div v-if="upload.progress.status === 'success'" class="upload-success">
          {{ `${upload.uploadFile?.name} 上传成功` }}
        </div>
        <a-progress :percent="upload.progress.percent" :status="upload.progress.status"></a-progress>
      </div>
    </uploader>
  </div>
</template>

<script setup>
import { reactive, defineExpose, defineEmits, defineProps } from 'vue'
import { CloudUploadOutlined } from '@ant-design/icons-vue'
import { baseURL } from '@/api/request'
import { API } from '@/api/backend'
import { FileMerge } from '@/api/frontend'
import SparkMD5 from 'spark-md5'
import { message } from 'ant-design-vue'

const emit = defineEmits(['success'])
const props = defineProps({
  accept: {
    type: String,
    required: true
  },
  uploadText: {
    type: String,
    required: true
  }
})
const options = {
  // 上传地址
  target: baseURL + API.FILE_UPLOAD,
  // 请求头
  headers: { token: sessionStorage.getItem('token') },
  // 单文件上传
  singleFile: true,
  // 是否开启服务器分片校验。默认为 true
  testChunks: true,
  // 真正上传的时候使用的 HTTP 方法,默认 POST
  uploadMethod: 'post',
  // 分片大小，5MB
  chunkSize: 5 * 1024 * 1024,
  // 并发上传数，默认为 3
  simultaneousUploads: 3,
  /**
   * 判断分片是否上传，秒传和断点续传基于此方法
   * 这里根据实际业务来 用来判断哪些片已经上传过了 不用再重复上传了 [这里可以用来写断点续传！！！]
   */
  checkChunkUploadedByResponse: (chunk, msg) => {
    // msg是后台返回
    const res = JSON.parse(msg)
    const resData = res.data
    if (!resData.skipUpload) return false
    if (resData.skipUpload) {
      upload.skip = true
      clear()
      message.info('该文件对应的组件已存在，请勿重复上传')
      return true
    }
    // 判断文件或分片是否已上传，已上传返回 true
    // 这里的 uploaded 是后台返回
    return (resData.uploaded || []).indexOf(chunk.offset + 1) >= 0
  },
  parseTimeRemaining: function (timeRemaining, parsedTimeRemaining) {
    // 格式化时间
    return parsedTimeRemaining
      .replace(/\syears?/, '年')
      .replace(/\days?/, '天')
      .replace(/\shours?/, '小时')
      .replace(/\sminutes?/, '分钟')
      .replace(/\sseconds?/, '秒')
  }
}
const upload = reactive({
  fileStatusTextObj: {
    success: '上传成功',
    error: '上传错误',
    uploading: '正在上传',
    paused: '停止上传',
    waiting: '等待中'
  },
  uploadFile: null,
  uploadInfo: {
    filePath: '',
    scanner: ''
  },
  progress: {
    percent: 0,
    status: 'normal'
  },
  skip: false,
  maxSize: 500 * 1024 * 1024
})
const fileStatusText = (status, response) => {
  if (status === 'md5') {
    return '校验MD5'
  } else {
    return upload.fileStatusTextObj[status]
  }
}
const onFileAdded = (file, event) => {
  // console.log('onFileAdded', file)
  // 1. 判断文件名称与大小是否允许上传
  if (props.uploadText === 'pom.xml' && file.name !== 'pom.xml') {
    message.info('必须上传pom.xml')
    return
  }
  if (props.uploadText === 'go.mod' && file.name !== 'go.mod') {
    message.info('必须上传go.mod')
    return
  }
  if (props.uploadText === 'package.json' && file.name !== 'package.json') {
    message.info('必须上传package.json')
    return
  }
  if (props.uploadText === 'requirements.txt' && file.name !== 'requirements.txt') {
    message.info('必须上传requirements.txt')
    return
  }
  if (file.size > upload.maxSize) {
    message.info('文件大小不能超过100MB')
    return
  }
  upload.uploadFile = file
  upload.progress.status = 'active'
  // 2. 计算文件 MD5 并请求后台判断是否已上传，是则取消上传
  getFileMD5(file, (md5) => {
    if (md5 !== '') {
      // 修改文件唯一标识
      file.uniqueIdentifier = md5
      // 请求后台判断是否上传
      // 恢复上传
      file.resume()
    }
  })
}
const getFileMD5 = (file, callback) => {
  const spark = new SparkMD5.ArrayBuffer()
  const fileReader = new FileReader()
  // 获取文件分片对象（注意它的兼容性，在不同浏览器的写法不同）
  const blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice
  // 当前分片下标
  let currentChunk = 0
  // 分片总数(向上取整)
  const chunks = Math.ceil(file.size / options.chunkSize)
  // MD5加密开始时间
  const startTime = new Date().getTime()
  // 暂停上传
  file.pause()
  loadNext()
  // fileReader.readAsArrayBuffer操作会触发onload事件
  fileReader.onload = function (e) {
    spark.append(e.target.result)
    // 实时同步进度条
    upload.progress.percent = Math.ceil((currentChunk / chunks) * 100)
    if (currentChunk < chunks) {
      currentChunk++
      loadNext()
    } else {
      // 该文件的md5值
      const md5 = spark.end()
      // 回调传值md5
      callback(md5)
    }
  }
  fileReader.onerror = function () {
    this.$message.error('文件读取错误')
    file.cancel()
  }
  // 加载下一个分片
  function loadNext() {
    const start = currentChunk * options.chunkSize
    const end = start + options.chunkSize >= file.size ? file.size : start + options.chunkSize
    // 文件分片操作，读取下一分片(fileReader.readAsArrayBuffer操作会触发onload事件)
    fileReader.readAsArrayBuffer(blobSlice.call(file.file, start, end))
  }
}
const onFileSuccess = (rootFile, file, response, chunk) => {
  const result = JSON.parse(response)
  if (result.success && !upload.skip) {
    FileMerge({
      identifier: file.uniqueIdentifier,
      filename: file.name,
      totalChunks: chunk.offset
    })
      .then((res) => {
        // console.log('FileMerge', res)
        if (res.code !== 200) {
          message.error(res.message)
          return
        }
        upload.progress.status = 'success'
        upload.uploadInfo.filePath = res.data
        upload.uploadInfo.scanner = file.name
        emit('success', upload.uploadInfo)
      })
      .catch((err) => {
        message.error(err)
      })
  }
}
const onFileError = (rootFile, file, msg, chunk) => {
  upload.progress.status = 'exception'
  message.error('文件上传出错：', msg)
}
const onFileProgress = (rootFile, file, chunk) => {
  // upload.progress.percent = Math.ceil(file._prevProgress * 100)
  // console.log(`当前进度：${Math.ceil(file._prevProgress * 100)}%`)
}
const clear = () => {
  upload.uploadFile = null
}
defineExpose({ clear })
</script>

<style scoped>
.uploader-drop {
  border-radius: 8px;
}
.uploader-btn {
  border: 0;
  width: 100%;
  height: 120px;
  padding: 5px 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.upload-success {
  color: #52c41a;
}
.upload-failed {
  color: #ef0137;
}
.progress {
  margin-top: 10px;
}
</style>
