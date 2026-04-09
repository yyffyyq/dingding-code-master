<template>
  <div class="layout-container">
    <Head />

    <div class="main-content">
      <router-view />
    </div>
    <Foot />
  </div>
</template>

<script setup lang="ts">
// 保持你原有的组件导入即可
import Head from './components/Head/index.vue';
import Foot from './components/Foot/index.vue';

</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
/* 你的Body类名是 main-content，必须写这个！ */
:deep(.main-content) {
  flex: 1;
}
</style>