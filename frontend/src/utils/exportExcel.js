import { message } from 'ant-design-vue'
import fs from 'file-saver'
const XLSX = require('xlsx')

const s2ab = (s) => {
  if (typeof ArrayBuffer !== 'undefined') {
    const buf = new ArrayBuffer(s.length)
    const view = new Uint8Array(buf)
    for (let i = 0; i !== s.length; ++i) view[i] = s.charCodeAt(i) & 0xff
    return buf
  } else {
    const buf = new Array(s.length)
    for (let i = 0; i !== s.length; ++i) buf[i] = s.charCodeAt(i) & 0xff
    return buf
  }
}

function getRow(row, columns) {
  const obj = []
  columns.forEach((col) => {
    let val = row[col.dataIndex]

    if (col.formatter) {
      val = col.formatter(row, col, val)
    }
    obj[col.title] = val
  })
  return obj
}

export default function exportExcel(data, columns, filename) {
  const json = data.map((t) => getRow(t, columns))
  const fields = columns.map((t) => {
    return t.title
  })
  try {
    const sheetName = filename
    const wb = XLSX.utils.book_new()
    const ws = XLSX.utils.json_to_sheet(json, { header: fields })
    wb.SheetNames.push(sheetName)
    wb.Sheets[sheetName] = ws
    const defaultCellStyle = {
      font: { name: 'Verdana', sz: 13, color: 'FF00FF88' },
      fill: { fgColor: { rgb: 'FFFFAA00' } }
    }
    const wopts = {
      bookType: 'xlsx',
      bookSST: false,
      type: 'binary',
      cellStyles: true,
      defaultCellStyle,
      showGridLines: false
    }
    const wbout = XLSX.write(wb, wopts)
    const blob = new Blob([s2ab(wbout)], { type: 'application/octet-stream' })
    fs.saveAs(blob, filename + '.xlsx')
  } catch (err) {
    console.error(err)
  }
}
