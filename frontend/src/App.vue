<template>
  <!-- 必须用 #app 包裹，和全局样式对应！-->
  <div id="app">
    <Layout />
  </div>
</template>

<script setup lang="ts">
import Layout from './layout/index.vue';
import { userLoginUserStore} from "@/stors/loginUser.ts";

const loginUserStore = userLoginUserStore()
loginUserStore.fetchLoginUser()
</script>

<style src="./style.css">

</style>
