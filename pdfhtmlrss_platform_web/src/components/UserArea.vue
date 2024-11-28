<script setup lang="ts">
import { useRouter } from 'vue-router'
import {logout} from "@/api";

let router = useRouter()

async function handleLogout() {
  let res = await logout();
  if(res.ok) {
    await router.push("/login")
  }
}
</script>

<template>
  <div>
    <label>Hello, user!</label>
    <button @click="handleLogout">Logout</button>
  </div>
</template>

<style scoped>
div {
  display: flex;
  flex-direction: row;
  width: 150px;
  justify-content: space-around;
}
</style>