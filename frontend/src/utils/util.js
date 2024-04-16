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

export const ROLE = {
  ADMIN: 'Admin',
  BU_REP: 'Bu Rep',
  BU_PO: 'Bu PO',
  APP_LEADER: 'App Leader',
  APP_MEMBER: 'App Member'
}
export const permit = (roleArray) => {
  const roles = JSON.parse(sessionStorage.getItem('user')).userBuAppRoles
  const role = roles.find((item) => item.role !== '-')?.role
  if (!role) return false
  return roleArray.some((item) => item === role)
}
