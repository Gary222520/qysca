export const arrToString = (arr) => {
  if (!arr) return
  if (arr?.length === 0) return '-'
  return arr
    .reduce((pre, curr) => {
      return `${pre}; ${curr}`
    }, '')
    .substring(1)
}

export const formatDate = (date) => {
  if (!date) return
  date = date.replace(/T/g, ' ')
  date = date.replace(/Z/g, '')
  return date
}
