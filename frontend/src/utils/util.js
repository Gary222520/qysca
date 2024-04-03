export const arrToString = (arr) => {
  if (!arr) return
  return arr
    .reduce((pre, curr) => {
      return `${pre}; ${curr}`
    }, '')
    .substring(1)
}

export const formatDate = (date) => {
  date = date.replace(/T/g, ' ')
  date = date.replace(/Z/g, '')
  return date
}
