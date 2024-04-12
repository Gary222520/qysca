<template>
  <div>
    <div style="margin-bottom: 10px">应用许可证风险 TOP 5</div>
    <a-spin :spinning="data.spinning" tip="图表加载中，请稍等...">
      <div id="app-license-chart"></div>
    </a-spin>
  </div>
</template>

<script setup>
import { reactive, defineExpose } from 'vue'
import * as echarts from 'echarts'

const data = reactive({
  spinning: true,
  chart: null,
  compareDTOList: []
})

const draw = (compareDTOList) => {
  data.spinning = false
  data.compareDTOList = compareDTOList
  if (data.chart !== null) hide()
  data.chart = echarts.init(document.getElementById('app-license-chart'))
  const option = {
    legend: {},
    tooltip: {},
    grid: {
      left: '0%',
      top: '15%',
      right: '0%',
      bottom: '5%',
      containLabel: true
    },
    color: ['#ff7070', '#9fe080'],
    dataset: {
      dimensions: ['name', 'risk', 'secure'],
      source: getData()
    },
    xAxis: {
      type: 'category',
      axisLabel: {
        show: true,
        interval: 0
      }
    },
    yAxis: {},
    series: [
      { type: 'bar', barWidth: '24' },
      { type: 'bar', barWidth: '24' }
    ]
  }
  data.chart.setOption(option)
}

const getData = () => {
  return data.compareDTOList
    .map((item) => {
      return {
        name: item.name + '\n' + item.version,
        risk: item.risk,
        secure: item.secure
      }
    })
    .sort((a, b) => {
      if (a.risk === 0 && a.secure === 0) return 1
      if (a.risk > 0 && a.secure === 0) return -1
      if (b.risk === 0 && b.secure === 0) return -1
      if (b.risk > 0 && b.secure === 0) return 1
      return b.risk / b.secure - a.risk / a.secure
    })
}

const hide = () => {
  data.spinning = true
  data.chart?.dispose()
  data.chart = null
}

defineExpose({ draw, hide })
</script>

<style>
#app-license-chart {
  width: 580px;
  height: 200px;
}
</style>
<style scoped src="@/atdv/spin.css"></style>
