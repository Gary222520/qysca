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
        <uploader-btn class="uploader-btn" :attrs="{ accept: '.xml' }">
          <div style="text-align: center">
            <CloudUploadOutlined :style="{ fontSize: '30px' }" />
            <div class="upload_text">pom.xml</div>
          </div>
        </uploader-btn>
      </uploader-drop>
      <!-- <uploader-list>
        <li v-for="file in upload.uploadFileList" :key="file.id">
          <uploader-file :class="'file_' + file.id" ref="files" :file="file" :list="true"></uploader-file>
        </li>
      </uploader-list> -->
      <!-- <uploader-file :file="uploadFile" :list="false"></uploader-file> -->
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
import { reactive, defineExpose } from 'vue'
import { CloudUploadOutlined } from '@ant-design/icons-vue'
import { baseURL } from '@/api/request'
import { API } from '@/api/backend'
import SparkMD5 from 'spark-md5'

const options = {
  // 上传地址
  target: baseURL + API.FILE_UPLOAD,
  // 单文件上传
  singleFile: true,
  // 是否开启服务器分片校验。默认为 true
  testChunks: true,
  // 真正上传的时候使用的 HTTP 方法,默认 POST
  uploadMethod: 'post',
  // 分片大小，20MB
  chunkSize: 20 * 1024 * 1024,
  // 并发上传数，默认为 3
  simultaneousUploads: 3,
  /**
   * 判断分片是否上传，秒传和断点续传基于此方法
   * 这里根据实际业务来 用来判断哪些片已经上传过了 不用再重复上传了 [这里可以用来写断点续传！！！]
   */
  checkChunkUploadedByResponse: (chunk, message) => {
    // message是后台返回
    let messageObj = JSON.parse(message)
    let dataObj = messageObj.data
    if (dataObj.uploaded !== undefined) {
      return dataObj.uploaded
    }
    // 判断文件或分片是否已上传，已上传返回 true
    // 这里的 uploadedChunks 是后台返回]
    return (dataObj.uploadedChunks || []).indexOf(chunk.offset + 1) >= 0
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
  uploadFileList: [],
  uploadFile: null,
  progress: {
    percent: 0,
    status: 'normal'
  }
})
const fileStatusText = (status, response) => {
  if (status === 'md5') {
    return '校验MD5'
  } else {
    return upload.fileStatusTextObj[status]
  }
}
const onFileAdded = (file, event) => {
  upload.uploadFileList.push(file)
  upload.uploadFile = file
  upload.progress.status = 'active'
  console.log('file :>> ', file)
  // 有时 fileType为空，需截取字符
  console.log('文件类型：' + file.fileType)
  // 文件大小
  console.log('文件大小：' + file.size + 'B')
  // 1. todo 判断文件类型是否允许上传
  // 2. 计算文件 MD5 并请求后台判断是否已上传，是则取消上传
  console.log('校验MD5')
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
  let spark = new SparkMD5.ArrayBuffer()
  let fileReader = new FileReader()
  // 获取文件分片对象（注意它的兼容性，在不同浏览器的写法不同）
  let blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice
  // 当前分片下标
  let currentChunk = 0
  // 分片总数(向下取整)
  let chunks = Math.ceil(file.size / options.chunkSize)
  // MD5加密开始时间
  let startTime = new Date().getTime()
  // 暂停上传
  file.pause()
  loadNext()
  // fileReader.readAsArrayBuffer操作会触发onload事件
  fileReader.onload = function (e) {
    // console.log("currentChunk :>> ", currentChunk);
    spark.append(e.target.result)
    if (currentChunk < chunks) {
      currentChunk++
      loadNext()
    } else {
      // 该文件的md5值
      let md5 = spark.end()
      console.log(`MD5计算完毕：${md5}，耗时：${new Date().getTime() - startTime} ms.`)
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
  upload.progress.status = 'success'
  console.log('上传成功')
}
const onFileError = (rootFile, file, message, chunk) => {
  upload.progress.status = 'exception'
  console.log('上传出错：' + message)
}
const onFileProgress = (rootFile, file, chunk) => {
  upload.progress.percent = Math.ceil(file._prevProgress * 100)
  console.log(`当前进度：${Math.ceil(file._prevProgress * 100)}%`)
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
  color: #ff4d4f;
}
.progress {
  margin-top: 10px;
}
</style>
