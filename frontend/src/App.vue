<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <div id="mindtrack-app">
    <RouterView />
  </div>
</template>

<style>
#mindtrack-app {
  font-family:
    Inter,
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    Roboto,
    Oxygen,
    Ubuntu,
    Cantarell,
    'Fira Sans',
    'Droid Sans',
    'Helvetica Neue',
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
