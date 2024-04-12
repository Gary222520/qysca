<template>
  <div>
    <div style="margin-bottom: 10px">应用漏洞风险 TOP 5</div>
    <a-spin :spinning="data.spinning" tip="图表加载中，请稍等...">
      <div id="app-vul-chart"></div>
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
  data.chart = echarts.init(document.getElementById('app-vul-chart'))
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
    color: ['#ff7070', '#ffdc60', '#7ed3f4', '#9fe080', '#5c7bd9'],
    dataset: {
      dimensions: ['name', 'high', 'medium', 'low', 'none', 'unknown'],
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
      { type: 'bar', barWidth: '12' },
      { type: 'bar', barWidth: '12' },
      { type: 'bar', barWidth: '12' },
      { type: 'bar', barWidth: '12' },
      { type: 'bar', barWidth: '12' }
    ]
  }
  data.chart.setOption(option)
}

const getData = () => {
  return data.compareDTOList.map((item) => {
    return {
      name: item.name + '\n' + item.version,
      high: item.high,
      medium: item.medium,
      low: item.low,
      none: item.none,
      unknown: item.unknown
    }
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
#app-vul-chart {
  width: 580px;
  height: 200px;
}
</style>
<style scoped src="@/atdv/spin.css"></style>
