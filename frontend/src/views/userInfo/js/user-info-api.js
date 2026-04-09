// 这里可以放置获取用户信息的API方法
export function getUserInfo() {
  // 示例返回
  return Promise.resolve({
    username: '张三',
    email: 'zhangsan@example.com',
    role: '管理员'
  });
}
