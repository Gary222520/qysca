<template>
  <div>
    <div style="margin-bottom: 10px">漏洞风险占比</div>
    <a-spin :spinning="data.spinning" tip="图表加载中，请稍等...">
      <div id="vul-chart"></div>
    </a-spin>
  </div>
</template>

<script setup>
import { reactive, defineExpose } from 'vue'
import * as echarts from 'echarts'

const data = reactive({
  spinning: true,
  categoryCountMap: []
})

let chart = null

const draw = (categoryCountMap) => {
  data.spinning = false
  data.categoryCountMap = categoryCountMap
  if (chart !== null) hide()
  chart = echarts.init(document.getElementById('vul-chart'))
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    color: ['#a80022', '#ff7070', '#ffdc60', '#7ed3f4', '#9fe080', '#5c7bd9'],
    series: [
      {
        name: '漏洞风险占比',
        type: 'pie',
        radius: ['50%', '70%'],
        label: {
          formatter: '{b}：{c}'
        },
        data: getData(),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  chart.setOption(option)
}

const getData = () => {
  return [
    { name: '严重', value: data.categoryCountMap.CRITICAL },
    { name: '高危', value: data.categoryCountMap.HIGH },
    { name: '中危', value: data.categoryCountMap.LOW },
    { name: '低危', value: data.categoryCountMap.MEDIUM },
    { name: '无风险', value: data.categoryCountMap.NONE },
    { name: '未知', value: data.categoryCountMap.UNKNOWN }
  ]
}

const hide = () => {
  data.spinning = true
  chart?.dispose()
  chart = null
}

defineExpose({ draw, hide })
</script>

<style>
#vul-chart {
  width: 500px;
  height: 200px;
}
</style>
<style scoped src="@/atdv/spin.css"></style>
