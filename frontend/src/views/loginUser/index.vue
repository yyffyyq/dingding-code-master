<template>
  <div class="login-container">
    <h2>欢迎来到登录页面</h2>

    <div v-if="isDingLoggingIn" style="margin-top: 20px; color: #2d8cf0;">
      正在向服务器校验钉钉授权信息，请稍候...
    </div>

    <LoginForm v-else @submit="handleLogin" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import LoginForm from './components/LoginForm.vue';
import { doLogin} from './js/login-api';
import { dingLogin } from '../../api/dingUserController.ts';
import './css/login-style.css';

const router = useRouter();
const route = useRoute();
const isDingLoggingIn = ref(false);

const handleLogin = (username: string) => {
  if (!username) {
      alert("用户名不能为空");
      return;
    }
  doLogin(username);
  };

  // 拦截dingding扫码重定向
  onMounted(async () => {
    const code = route.query.code as string;

    if (code) {
      isDingLoggingIn.value = true;
      console.log("接收到钉钉返回的 code：", code);

      try {
        // 调用 dingUserController 中统一封装的方法
        // 假设后端依然需要你在 body 里传 authCode
        const res = await dingLogin({ authCode: code });

        // 根据你提供的接口文档，成功的状态码为 0
        if (res.data.code === 0) {
          alert(`钉钉登录成功！`);
          // 保存最新的用户信息 (nickName, userRole, avarUrl)
          localStorage.setItem('user_info', JSON.stringify(res.data));

          // 【重要】后端改为 Session 后，登录态存在后端的 Cookie 里。
          // 你不需要再手动 setItem('token', xxx) 了，后续使用 @/request 发请求会自动带上 Cookie。
          window.location.href = '/';
          // 成功后跳转到首页，清除地址栏的 code
          // router.replace({ path: '/' });
        } else {
          // 失败读取新接口的 message 字段
          alert(`登录失败: ${res.data.message || '未知错误'}`);
          router.replace({ path: route.path });
        }
      } catch (error) {
        console.error("请求后端钉钉登录接口异常", error);
        alert("网络异常，请稍后再试");
        router.replace({ path: route.path });
      } finally {
        isDingLoggingIn.value = false;
      }
    }
  });
</script>