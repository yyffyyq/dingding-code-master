<template>
  <div class="form-box">
<!--    <input v-model="username" placeholder="请输入用户名" />-->
<!--    <button @click="handleSubmit">普通登录</button>-->

    <hr style="margin: 20px 0; border: 0.5px solid #ccc;" />

    <button @click="goToDingTalkLogin" style="background-color: #2d8cf0; color: white;">
      使用钉钉扫码登录
    </button>
  </div>
</template>

<script setup lang="ts">
// import { ref } from 'vue';
//
// const username = ref('');
// const emit = defineEmits(['submit']);

// const handleSubmit = () => {
//   emit('submit', username.value);
// };

// --- 钉钉跳转登录逻辑 ---
const goToDingTalkLogin = () => {
  // 1. 替换为你应用后台的 AppKey (就是你的 ding929qmi3qamdahca1)
  const clientId = 'ding929qmi3qamdahca1';

  // 2. 扫码成功后重定向的回调地址 (必须在钉钉后台配置为回调域名)
  const redirectUri = encodeURIComponent('http://localhost:5173/user/userLogin');

  // 3. state 防重放攻击标识
  const state = '12345';

  // 4. 【核心修改】使用钉钉新版的 OAuth2 统一登录页面地址
  // 注意：参数名变成了 client_id，scope 变成了 openid
  const url = `https://login.dingtalk.com/oauth2/auth?redirect_uri=${redirectUri}&response_type=code&client_id=${clientId}&scope=openid&state=${state}&prompt=consent`;

  // 5. 让浏览器直接跳转
  window.location.href = url;
};
</script>